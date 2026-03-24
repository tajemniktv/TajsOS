with open(".github/workflows/codacy.yml", "r") as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if "category: codacy" in line:
        continue
    new_lines.append(line)

with open(".github/workflows/codacy.yml", "w") as f:
    f.writelines(new_lines)
