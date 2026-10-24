/**
 * Plugins remark/rehype próprios usados na compilação do MDX.
 */
import type { Element, Root as HastRoot } from 'hast'
import type { Code, Root as MdastRoot } from 'mdast'
import type { ShikiTransformer } from 'shiki'
import { visit } from 'unist-util-visit'

/**
 * Transforma blocos ```mermaid em <Mermaid chart="..." />, para que o autor
 * escreva diagramas como bloco de código comum.
 */
export function remarkMermaid() {
  return (tree: MdastRoot) => {
    visit(tree, 'code', (node: Code, index, parent) => {
      if (node.lang !== 'mermaid' || !parent || index === undefined) return
      const titulo = node.meta?.match(/title="([^"]+)"/)?.[1]
      parent.children[index] = {
        type: 'mdxJsxFlowElement',
        name: 'Mermaid',
        attributes: [
          { type: 'mdxJsxAttribute', name: 'chart', value: node.value },
          ...(titulo ? [{ type: 'mdxJsxAttribute' as const, name: 'title', value: titulo }] : []),
        ],
        children: [],
      } as unknown as Code
    })
  }
}

/**
 * Lê `title="Arquivo.java"` do meta do bloco de código e o expõe no <pre>
 * como `data-title`, junto com a linguagem em `data-lang`. O componente
 * CodeBlock usa esses atributos para o cabeçalho e o botão de copiar.
 */
export function transformerMetaTitle(): ShikiTransformer {
  return {
    name: 'bazar:meta-title',
    pre(node: Element) {
      const meta = this.options.meta?.__raw ?? ''
      const titulo = meta.match(/title="([^"]+)"/)?.[1]
      if (titulo) node.properties['data-title'] = titulo
      node.properties['data-lang'] = this.options.lang
      if (/\bnoCopy\b/.test(meta)) node.properties['data-no-copy'] = 'true'
    },
  }
}

/** Marca tabelas markdown para receber o estilo de tabela do site. */
export function rehypeTabelas() {
  return (tree: HastRoot) => {
    visit(tree, 'element', (node: Element) => {
      if (node.tagName === 'table') {
        node.properties.className = [...((node.properties.className as string[]) ?? []), 'md-table']
      }
    })
  }
}
