import { defineCollection } from "astro:content";
import { docsLoader } from "@astrojs/starlight/loaders";
import { docsSchema } from "@astrojs/starlight/schema";
import { z } from "astro/zod";
import { blogSchema } from "starlight-blog/schema";
import { videosSchema } from "starlight-videos/schemas";

export const collections = {
  docs: defineCollection({
    loader: docsLoader(),
    schema: docsSchema({
      extend: (context) => {
        return z.object({
          ...blogSchema(context).shape,
          ...videosSchema.shape,
        });
      },
    }),
  }),
};
