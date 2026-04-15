// @ts-check
import { defineConfig } from 'astro/config';

import starlight from '@astrojs/starlight';

import tailwindcss from '@tailwindcss/vite';

import sitemap from '@astrojs/sitemap';
import mdx from '@astrojs/mdx';
import react from '@astrojs/react';
import partytown from '@astrojs/partytown';
import starlightBlog from 'starlight-blog';
import starlightLinksValidator from 'starlight-links-validator';
import starlightImageZoom from 'starlight-image-zoom';
import starlightVideos from 'starlight-videos';

// https://astro.build/config
export default defineConfig({
  site: 'https://tajemniktv.github.io/TajsOS/',
  base: '/TajsOS/',
  integrations: [starlight({
    title: 'Docs',
    description: 'TajsOS Documentation',
    logo: {
      src: './src/assets/logo.png',
      alt: 'TajsOS Logo'
    },
    plugins: [
      starlightBlog(),
      starlightLinksValidator(),
      starlightImageZoom(),
      starlightVideos()
    ]
  }), sitemap(), mdx(), react(), partytown()],

  vite: {
    plugins: [tailwindcss()]
  }
});