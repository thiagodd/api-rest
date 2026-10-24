declare module '*.mdx' {
  import type { ComponentType } from 'react'
  import type { Frontmatter } from '@/lib/conteudo'
  export const frontmatter: Frontmatter
  const MDXContent: ComponentType<{ components?: Record<string, unknown> }>
  export default MDXContent
}
