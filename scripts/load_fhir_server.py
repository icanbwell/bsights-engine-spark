import os
from pathlib import Path
import json
import requests
import base64


def send_resource_to_fhir_server(data) -> None:
    resourceType: str = data["resourceType"]
    id_: str = data["id"]
    headers = {"Content-Type": "application/fhir+json"}

    # add security tag
    data["meta"] = {
        "source": "https://icanbwell.com",
        "security": [
            {
                "system": "https://www.icanbwell.com/access",
                "code": "bwell"
            },
            {
                "system": "https://www.icanbwell.com/owner",
                "code": "bwell"
            }
        ]
    }

    json_content = {
        "resourceType": "Bundle",
        "entry": [
            {
                "resource": data
            }
        ]
    }

    # for design time only - comment next two lines before release
    # print("Printing Entire Post Request")
    # print(json_content)

    response = requests.post(f'http://fhir:3000/4_0_0/{resourceType}/0/$merge', json=json_content, headers=headers)
    print("Status code: ", response.status_code)
    print("Printing Entire Post Request")
    print(response.json())


def send_cql_to_fhir_server(library_name: str, library_version: str,  cql: str) -> None:
    # wrap cql in Library resource
    resource = {
        "resourceType": "Library",
        "id": library_name,
        "version": library_version,
        "type": {
            "coding": [
                {
                    "system": "http://terminology.hl7.org/CodeSystem/library-type",
                    "code": "logic-library"
                }
            ]
        },
        "status": "active",
        "url": f"http://localhost:3000/4_0_0/Library/{library_name}",
        "name": library_name,
        "content": [
            {
                "contentType": "text/cql",
                "data": base64.b64encode(cql.encode('ascii')).decode('ascii')
            }
        ]
    }
    send_resource_to_fhir_server(resource)

parent_path = "/data"

def main() -> int:
    print("Starting...")
    load_cql()
    load_terminology()


def load_cql() -> None:
    data_dir: Path = Path(parent_path).joinpath("./cql")

    print(data_dir)

    for (root, dirs, file_names) in os.walk(data_dir):
        for file_name in file_names:
            if file_name.endswith(".cql"):
                full_path = os.path.join(root, file_name)
                print(full_path)
                with open(full_path, "r") as f:
                    contents = f.read()
                    # print(contents) - for design time only. comment before release.
                    file_name_without_extension = file_name.replace(".cql", "")
                    library_name = file_name_without_extension.split("-")[0]
                    library_version = file_name_without_extension.split("-")[1]
                    send_cql_to_fhir_server(library_name, library_version,  contents)


def load_terminology() -> None:
    data_dir: Path = Path(parent_path).joinpath("./terminology")

    print(data_dir)

    for (root, dirs, file_names) in os.walk(data_dir):
        for file_name in file_names:
            if file_name.endswith(".json"):
                full_path = os.path.join(root, file_name)
                print(full_path)
                with open(full_path, "r") as f:
                    contents = f.read()
                    # print(contents) - for design time only. comment before release.
                    data = json.loads(contents)
                    print(data["resourceType"])
                    print(data["id"])
                    send_resource_to_fhir_server(data)

if __name__ == "__main__":
    exit(main())
