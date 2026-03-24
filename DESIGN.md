# Design System Document: The Neural Interface

## 1. Overview & Creative North Star

**Creative North Star: "The Digital Curator"**

This design system is not a utility; it is an intelligence. To reflect the "Second Brain" ethos, the
UI must feel like a high-end, tactile instrument—think of a bespoke glass cockpit in a
high-performance spacecraft.

We are moving away from the "flat web" by embracing **Organic Industrialism**. This means breaking
the rigid, predictable grid through intentional asymmetry, overlapping containers, and a focus on
depth over structure. The layout should feel editorial and cinematic, using high-contrast typography
scales and "breathing" white space to guide the user through a curated information flow. Every
element should feel like it was placed with surgical precision, utilizing the interplay between deep
charcoal voids and vibrant neon signals.

---

## 2. Colors

The palette is anchored in high-contrast depths and singular, high-energy accents. We use color to
define focus, not just decoration.

### The Palette (Material Design Convention)

* **Background:** `#0e0e12` (The Void)
* **Surface:** `#0e0e12`
* **Surface Containers:**
* *Lowest:* `#000000` (Recessed areas)
* *Low:* `#131317`
* *High:* `#1f1f24`
* *Highest:* `#25252b` (Prominent cards)
* **Primary (Neon Purple):** `#ba9eff`
* **Primary Dim:** `#8455ef` (For subtle glows and states)
* **On-Surface:** `#fcf8fe` (Crisp white text)

### The "No-Line" Rule

**Explicit Instruction:** Do not use 1px solid borders to section off the page. In this system,
boundaries are defined through **background color shifts**. To separate the Hero from a Features
section, transition from `surface` to `surface-container-low`. Physicality is achieved through tonal
contrast, not "lines."

### Surface Hierarchy & Nesting

Treat the UI as a series of stacked layers. An inner "module" or card should use a higher surface
tier than the section it sits on (e.g., a `surface-container-highest` card sitting on a
`surface-container-low` background). This creates a natural, tactile "lift."

### The "Glass & Gradient" Rule

To achieve the high-end industrial look, use **Glassmorphism** for floating elements (Navigation
bars, Modals). Use semi-transparent surface colors with a `backdrop-blur` of 20px–40px.

* **Signature Textures:** For primary CTAs, do not use flat colors. Use a linear gradient from
  `primary` (#ba9eff) to `primary_dim` (#8455ef) at a 135-degree angle to provide a "lit from
  within" neon glow.

---

## 3. Typography

* **Display (Space Grotesk):** Used for "Hero" moments. The wide tracking and bold weights convey an
  authoritative, high-tech personality.
* *Display-LG:* 3.5rem / Bold / -0.02em tracking.
* **Headings:** Space Grotesk, 700, 24-32sp - Used to introduce content sections. It acts as the "
  industrial"
  anchor of the page.
* **Body:** Outfit, 400, 16sp
* **Small text**: JetBrains Mono, 500, 12sp (uppercase)
* **Labels (Space Grotesk, 600, 16sp):** Small caps or technical labels should always use Space
  Grotesk to
  maintain the "software instrument" vibe.

---

## 4. Elevation & Depth

We eschew traditional shadows in favor of **Tonal Layering** and **Ambient Glows**.

* **The Layering Principle:** Depth is achieved by stacking surface tokens.
* *Level 0:* `surface` (Background)
* *Level 1:* `surface-container-low` (Content Sections)
* *Level 2:* `surface-container-highest` (Interactive Cards)
* **Ambient Shadows:** If a card must "float," use an extra-diffused shadow.
* *Values:* `0px 20px 40px rgba(0, 0, 0, 0.4)`
* *Pro Tip:* Add a 1px inner-glow (top-down) using `outline-variant` at 10% opacity to mimic light
  catching the edge of a glass pane.
* **The "Ghost Border":** High-contrast borders are forbidden. If a boundary is required for
  accessibility, use the `outline-variant` token at **15% opacity**. It should be felt, not seen.
* **Neon Diffusion:** Primary elements should cast a soft `primary-dim` glow onto the surface below
  them, simulating a real-world light source.

---

## 5. Components

### Buttons

* **Primary:** Gradient (`primary` to `primary_dim`), 8dp (0.5rem) rounded corners. Text is
  `on-primary-fixed` (Black) for maximum legibility.
* **Secondary:** `surface-container-highest` background with a "Ghost Border."
* **Tertiary:** Ghost button, Space Grotesk, 0.05em tracking.

### Cards & Modules

* **Rule:** Forbid the use of divider lines.
* **Structure:** Use vertical white space (Spacing scale `8` or `10`) to separate content. Use a
  `surface-container-low` background for the card body and `surface-container-highest` for the
  header of the card to create internal hierarchy.

### Input Fields

* **Style:** Recessed appearance using `surface-container-lowest`.
* **Focus State:** No thick border. Instead, use a subtle 1px "Ghost Border" and a soft `primary`
  external glow (blur 8px).

### Custom Component: The "Intelligence Feed"

A specialized list item for TajsOS. Use a leading icon with a `primary` neon glow. Use `label-sm` (
Space Grotesk) for metadata tags, ensuring the "technical" font is used for data-driven elements.

---

## 6. Do's and Don'ts

### Do

* **DO** use intentional asymmetry. Place a large Display heading on the left and a small body
  paragraph offset to the right.
* **DO** use the Spacing Scale strictly. Generous padding (e.g., scale `12` or `16`) is what makes a
  layout feel "premium."
* **DO** utilize the provided wordmark with enough clear space to act as a "seal of quality."

### Don't

* **DON'T** use 100% opaque borders. They flatten the design and destroy the "glass" aesthetic.
* **DON'T** use standard blue or grey for shadows. Shadows in a dark UI should be deep charcoal or
  tinted with the `primary` hue.
* **DON'T** clutter the UI. If a piece of information isn't vital, hide it behind a progressive
  disclosure pattern. The "Second Brain" is organized, not overwhelmed.
* **DON'T** use sharp 0px corners. Stick to the **xl (0.75rem)** or **lg (0.5rem)** tokens to keep
  the industrial look "tactile" rather than "aggressive."