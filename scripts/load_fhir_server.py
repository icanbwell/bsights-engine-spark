import os
from pathlib import Path
import json
import requests

def send_resource_to_fhir_server() -> None:

    response = requests.post('http://fhir:3000/', json={'id': 1, 'name': 'Jessa'})
    print("Status code: ", response.status_code)
    print("Printing Entire Post Request")
    print(response.json())

def main() -> int:
    print("Hello")

    data_dir: Path = Path(os.getcwd()).joinpath("./vocabulary")
    for (root, dirs, file_names) in os.walk(data_dir):
        for file_name in file_names:
            full_path = os.path.join(root,file_name)
            print(full_path)
            with open(full_path, "r") as f:
                contents = f.read()
                # print(contents)
                data = json.loads(contents)
                print(data["resourceType"])
                print(data["id"])
            # print(file_name)
            # data = json.load(file_name)
            # print(data["resourceType"])

if __name__ == "__main__":
    exit(main())
