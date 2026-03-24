file_path = "composeApp/src/commonMain/kotlin/com/tajemniktv/tajsos/ui/screens/DecisionsScreen.kt"

with open(file_path, "r") as f:
    content = f.read()

# I see the imports ARE there actually in DecisionsScreen.kt. The issue might be that cd_decision_outcome string resource from strings.xml wasn't used correctly. Let's fix that nitpick.

if 'contentDescription = null,' in content and 'Icons.Default.CheckCircle,' in content:
    content = content.replace('contentDescription = null,\n                tint = TactileTheme.Success',
                              'contentDescription = stringResource(Res.string.cd_decision_outcome),\n                tint = TactileTheme.Success')
elif 'contentDescription = null' in content and 'Icons.Default.CheckCircle' in content:
    content = content.replace('contentDescription = null', 'contentDescription = stringResource(Res.string.cd_decision_outcome)')

with open(file_path, "w") as f:
    f.write(content)
