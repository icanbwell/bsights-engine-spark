import os
from pathlib import Path
import json
import requests


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
    print(json_content)
    response = requests.post(f'http://fhir:3000/4_0_0/{resourceType}/0/$merge', json=json_content, headers=headers)
    print("Status code: ", response.status_code)
    print("Printing Entire Post Request")
    print(response.json())


def main() -> int:
    print("Starting...")

    data_dir: Path = Path(os.getcwd()).joinpath("./vocabulary")
    for (root, dirs, file_names) in os.walk(data_dir):
        for file_name in file_names:
            full_path = os.path.join(root, file_name)
            print(full_path)
            with open(full_path, "r") as f:
                contents = f.read()
                # print(contents)
                data = json.loads(contents)
                print(data["resourceType"])
                print(data["id"])
                send_resource_to_fhir_server(data)
            # print(file_name)
            # data = json.load(file_name)
            # print(data["resourceType"])


if __name__ == "__main__":
    exit(main())
