1. **Fix Route Collision in `astro.config.mjs`:**
   - Run a Python script to add `disable404Route: true` to the `starlight` configuration block. This resolves the route collision warning for the 404 page.
   - Run `cat website/astro.config.mjs` to verify.

2. **Add Interactive Toggle to `pricing.astro`:**
   - Run a Python script to inject a client-side script and UI elements into `website/src/pages/pricing.astro`. This will add a functional toggle to switch pricing between "Monthly Cycle" and "Annual Cycle".
   - Run `cat website/src/pages/pricing.astro` to verify.

3. **Verify Project Compilation:**
   - Run `cd website && npm run build` to ensure the site compiles correctly without errors.

4. **Complete pre-commit steps:**
   - Complete pre-commit steps to ensure proper testing, verification, review, and reflection are done.

5. **Submit PR:**
   - Use the `submit` tool to create the PR.
