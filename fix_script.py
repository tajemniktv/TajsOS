import re

with open('./androidApp/build.gradle.kts', 'r') as f:
    content = f.read()

# Remove the custom task
content = re.sub(r'abstract class ConditionalGoogleServicesTask.*?dependsOn\(bypassGoogleServicesError\)\n\}\n', '', content, flags=re.DOTALL)

with open('./androidApp/build.gradle.kts', 'w') as f:
    f.write(content)
