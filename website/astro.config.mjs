// @ts-check

import mdx from "@astrojs/mdx";
import partytown from "@astrojs/partytown";
import react from "@astrojs/react";
import sitemap from "@astrojs/sitemap";
import starlight from "@astrojs/starlight";
import tailwindcss from "@tailwindcss/vite";
import { defineConfig } from "astro/config";
import starlightBlog from "starlight-blog";
import starlightImageZoom from "starlight-image-zoom";
import starlightLinksValidator from "starlight-links-validator";
import starlightVideos from "starlight-videos";

// https://astro.build/config
export default defineConfig({
  site: "https://tajemniktv.github.io/TajsOS/",
  base: "/TajsOS/",
  integrations: [
    starlight({
      title: "Docs",
      description: "TajsOS Documentation",
      // Disabling default 404 route because a custom src/pages/404.astro exists
      disable404Route: true,

      components: {
        MarkdownContent: "./src/components/MarkdownContent.astro",
      },

      sidebar: [
        {
          label: "Introduction",
          items: [
            { label: "Overview", slug: "overview" },
            { label: "Current Status", slug: "current-status" },
            { label: "Product Philosophy", slug: "product-philosophy" },
            { label: "Roadmap", slug: "roadmap" },
          ],
        },
        {
          label: "Architecture & Model",
          items: [
            { label: "Core Object Model", slug: "core-object-model" },
            {
              label: "Local-First Architecture",
              slug: "local-first-architecture",
            },
            { label: "App Surfaces & Layering", slug: "app-surfaces" },
            { label: "Tech Stack", slug: "tech-stack" },
          ],
        },
        {
          label: "Development & Design",
          items: [
            { label: "Design System", slug: "design-system" },
            { label: "Agent & Contributor Rules", slug: "agent-rules" },
          ],
        },
      ],

      logo: {
        src: "./src/assets/logo.png",
        alt: "TajsOS Logo",
      },
      plugins: [
        starlightBlog(),
        starlightLinksValidator(),
        starlightImageZoom(),
        starlightVideos(),
      ],
    }),
    sitemap(),
    mdx(),
    react(),
    partytown(),
  ],

  vite: {
    plugins: [tailwindcss()],
  },
});
