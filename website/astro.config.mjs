// @ts-check
import { defineConfig } from 'astro/config';

import starlight from '@astrojs/starlight';

// https://astro.build/config
export default defineConfig({
  integrations: [
    starlight({
      title: 'Docs',
      description: 'TajsOS Documentation',
      logo: {
        src: './src/assets/logo.png',
        alt: 'TajsOS Logo'
      }
    })
  ]
});