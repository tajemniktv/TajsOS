with open('shared/src/commonTest/kotlin/com/tajemniktv/tajsos/data/AppRepositoryTest.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
skip = False
for line in lines:
    if line.startswith('<<<<<<< HEAD'):
        continue
    elif line.startswith('======='):
        skip = True
        continue
    elif line.startswith('>>>>>>> origin/main'):
        skip = False
        continue

    if not skip:
        new_lines.append(line)

with open('shared/src/commonTest/kotlin/com/tajemniktv/tajsos/data/AppRepositoryTest.kt', 'w') as f:
    f.writelines(new_lines)
