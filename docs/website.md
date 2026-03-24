# TajsOS Website

The `website/` directory contains the source code for the public-facing documentation and marketing site for TajsOS.

It is built using [Astro](https://astro.build/), a modern web framework optimized for speed and content-driven sites.

## Running the Website Locally

You need Node.js and `npm` (or `yarn`/`pnpm`) installed to run the website.

1.  **Navigate to the directory:**
    ```bash
    cd website
    ```
2.  **Install dependencies:**
    ```bash
    npm install
    ```
3.  **Start the development server:**
    ```bash
    npm run dev &
    ```

The site will typically be available at `http://localhost:4321`.

## Structure

*   `src/`: Contains the Astro components (`.astro`), layouts, and markdown (`.md` or `.mdx`) content files for the pages.
*   `public/`: Static assets like images and fonts.
