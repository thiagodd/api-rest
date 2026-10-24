import mdx from '@mdx-js/rollup'
import rehypeShiki from '@shikijs/rehype'
import {
  transformerMetaHighlight,
  transformerNotationDiff,
  transformerNotationFocus,
  transformerNotationHighlight,
} from '@shikijs/transformers'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { fileURLToPath, URL } from 'node:url'
import rehypeSlug from 'rehype-slug'
import remarkFrontmatter from 'remark-frontmatter'
import remarkGfm from 'remark-gfm'
import remarkMdxFrontmatter from 'remark-mdx-frontmatter'
import { defineConfig } from 'vite'
import { rehypeTabelas, remarkMermaid, transformerMetaTitle } from './scripts/mdx-plugins.ts'
import { conteudoPlugin } from './scripts/vite-plugin-conteudo.ts'

export default defineConfig({
  plugins: [
    {
      enforce: 'pre',
      ...mdx({
        providerImportSource: '@mdx-js/react',
        remarkPlugins: [remarkFrontmatter, [remarkMdxFrontmatter, { name: 'frontmatter' }], remarkGfm, remarkMermaid],
        rehypePlugins: [
          rehypeSlug,
          rehypeTabelas,
          [
            rehypeShiki,
            {
              themes: { light: 'github-light', dark: 'github-dark-dimmed' },
              defaultColor: false,
              langs: [
                'java', 'kotlin', 'bash', 'shell', 'http', 'json', 'jsonc', 'yaml', 'xml', 'properties', 'sql',
                'javascript', 'typescript', 'tsx', 'html', 'css', 'dockerfile', 'groovy', 'graphql', 'proto',
                'diff', 'ini', 'toml', 'markdown', 'mdx', 'csv', 'nginx',
              ],
              langAlias: { sh: 'bash', console: 'bash', curl: 'bash', httpie: 'bash', yml: 'yaml', js: 'javascript', ts: 'typescript', text: 'txt' },
              fallbackLanguage: 'txt',
              transformers: [
                transformerMetaTitle(),
                transformerMetaHighlight(),
                transformerNotationHighlight(),
                transformerNotationDiff(),
                transformerNotationFocus(),
              ],
            },
          ],
        ],
      }),
    },
    react({ include: /\.(mdx|js|jsx|ts|tsx)$/ }),
    tailwindcss(),
    conteudoPlugin(),
  ],
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
  },
  build: {
    chunkSizeWarningLimit: 1500,
  },
})
