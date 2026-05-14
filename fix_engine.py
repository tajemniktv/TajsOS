import os
import json

package_json_path = 'website/package.json'
with open(package_json_path, 'r') as f:
    data = json.load(f)

if 'engines' in data and 'node' in data['engines']:
    data['engines']['node'] = ">=22"

with open(package_json_path, 'w') as f:
    json.dump(data, f, indent=2)
