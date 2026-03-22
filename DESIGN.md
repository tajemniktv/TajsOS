# TajsOS design and themes

## Design System

### Design Direction

Industrial control panel, heavy dark mode, mechanical switches, and vibrant
neon purple accents to guide attention. Blends Material Design 3 patterns with a highly custom,
tactile aesthetic.

### Color Palette

- **Primary:** `#8B5CF6` - Active states, progress bars, primary toggles (Material Primary)
- **Background:** `#0F0F13` - Main app background (Material Background)
- **Surface:** `#1C1C21` - Elevated cards, control modules (Material Surface)
- **Text:** `#F8FAFC` - Primary headings and active data (Material On-Background)
- **Muted:** `#52525B` - Inactive states, secondary labels, borders (Material Outline/Surface
  Variant)
- **Accent:** `#10B981` - Success states, completed indicators

### Typography

Distinctive, technical, and highly legible to reduce cognitive strain.

- **Headings:** `Space Grotesk`, 700, 24-32sp
- **Body:** `Outfit`, 400, 16sp
- **Small text:** `JetBrains Mono`, 500, 12sp (uppercase)
- **Buttons:** `Space Grotesk`, 600, 16sp

**Style notes:** 8dp border radius on all modules (Material shapes adjusted). Heavy use of inner
shadows (custom Modifier) on inactive buttons to look "pressed out". Active buttons use bright
purple drop shadows/elevation.

### Design Tokens

```kotlin
object TactileTheme {
    val Primary = Color(0xFF8B5CF6)
    val Background = Color(0xFF0F0F13)
    val Surface = Color(0xFF1C1C21)
    val Text = Color(0xFFF8FAFC)
    val Muted = Color(0xFF52525B)

    val RadiusSm = 4.dp
    val RadiusMd = 8.dp
    val RadiusLg = 16.dp

    val SpacingSm = 8.dp
    val SpacingMd = 16.dp
    val SpacingLg = 24.dp
    val SpacingXl = 32.dp
}
```

---
