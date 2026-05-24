1. **Analyze Failure**:
The CodeScene check failed because `ActionButton` function became a "Complex Method".
Memory says: "To resolve CodeScene 'Complex Method' code health violations in Jetpack Compose UI components, extract nested conditional logic (e.g., resolving colors, borders, or backgrounds) into private `@Composable` helper functions."

2. **Fix**:
We can extract the logic for `buttonBorder`, `containerColor`, `contentColor` and the background gradient modifier into helper properties or functions. Or extract the `ButtonDefaults.buttonColors(...)` call into a `@Composable` helper function.

Let's extract:
```kotlin
@Composable
private fun resolveButtonColors(isPrimary: Boolean, isGhost: Boolean, enabled: Boolean, containerColor: Color, contentColor: Color) = ButtonDefaults.buttonColors(
    containerColor = if (isPrimary && enabled) Color.Transparent else containerColor,
    contentColor = if (isPrimary && contentColor == TajsOSTheme.Text) TajsOSTheme.Background else contentColor,
    disabledContainerColor = if (isGhost) Color.Transparent else TajsOSTheme.SurfaceHighest,
    disabledContentColor = TajsOSTheme.Muted,
)

@Composable
private fun resolveButtonBorder(isPrimary: Boolean, isGhost: Boolean) = if (!isPrimary && !isGhost) {
    androidx.compose.foundation.BorderStroke(1.dp, TajsOSTheme.GhostBorder)
} else {
    null
}
```
Wait, we can just extract `resolveButtonColors` and `resolveButtonBorder` private helper functions outside `ActionButton`. And `resolvePrimaryBackgroundModifier`:

```kotlin
@Composable
private fun resolvePrimaryBackgroundModifier(isPrimary: Boolean, enabled: Boolean): Modifier = if (isPrimary && enabled) {
    Modifier.background(
        brush = Brush.linearGradient(
            colors = listOf(TajsOSTheme.Primary, TajsOSTheme.PrimaryDim),
            start = Offset(0f, 0f),
            end = Offset.Infinite,
        ),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    )
} else {
    Modifier
}
```

Then `ActionButton` will be much simpler:
```kotlin
@Composable
fun ActionButton( ... ) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.98f else 1f, label = "buttonScale")
    val isPrimary = containerColor == TajsOSTheme.Primary
    val isGhost = containerColor == Color.Transparent
    val buttonBorder = resolveButtonBorder(isPrimary, isGhost)

    val finalModifier = modifier
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .height(48.dp)
        .then(resolvePrimaryBackgroundModifier(isPrimary, enabled))

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = finalModifier,
        interactionSource = interactionSource,
        colors = resolveButtonColors(isPrimary, isGhost, enabled, containerColor, contentColor),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        border = buttonBorder,
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        ...
    }
}
```
