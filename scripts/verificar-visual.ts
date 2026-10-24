/**
 * Verificação visual e funcional do site com Playwright.
 *
 * Sobe o `vite preview` (rode `npm run build` antes), abre as páginas principais
 * em tema claro e escuro, no desktop e no celular, salva capturas em verificacao/
 * e confere: diagramas Mermaid renderizados, blocos Shiki, AnimatedFlow, busca,
 * tooltip do glossário, progresso no localStorage e ausência de erros no console.
 *
 * Uso: npm run build && npm run verificar:visual
 */
import { mkdirSync } from 'node:fs'
import { join } from 'node:path'
import { chromium, type Page } from 'playwright'
import { preview } from 'vite'

const pastaSaida = join(process.cwd(), 'verificacao')
mkdirSync(pastaSaida, { recursive: true })

const rotas = [
  { nome: 'inicio', caminho: '/' },
  { nome: 'componentes', caminho: '/componentes' },
  { nome: 'pagina', caminho: '/comecando-e-fundamentos-de-http/status-codes-em-profundidade' },
  { nome: 'glossario', caminho: '/glossario' },
  { nome: 'referencias', caminho: '/referencias' },
]

const telas = [
  { nome: 'desktop', viewport: { width: 1440, height: 900 } },
  { nome: 'celular', viewport: { width: 390, height: 844 }, isMobile: true, hasTouch: true },
]

const temas = ['claro', 'escuro'] as const

const falhas: string[] = []
const ok = (msg: string) => console.log(`  ✔ ${msg}`)
const falha = (msg: string) => {
  falhas.push(msg)
  console.log(`  ✘ ${msg}`)
}

async function esperarMermaid(page: Page, minimo: number) {
  try {
    await page.waitForFunction((n) => document.querySelectorAll('.mermaid-diagram svg').length >= n, minimo, { timeout: 20000 })
    return await page.locator('.mermaid-diagram svg').count()
  } catch {
    return page.locator('.mermaid-diagram svg').count()
  }
}

const servidor = await preview({ preview: { port: 4317, strictPort: true } })
const base = servidor.resolvedUrls?.local[0]?.replace(/\/$/, '') ?? 'http://localhost:4317'
console.log(`Servidor: ${base}`)

const navegador = await chromium.launch()

try {
  for (const tela of telas) {
    for (const tema of temas) {
      console.log(`\n[${tela.nome} · ${tema}]`)
      const contexto = await navegador.newContext({ viewport: tela.viewport, isMobile: tela.isMobile, hasTouch: tela.hasTouch, colorScheme: tema === 'escuro' ? 'dark' : 'light' })
      await contexto.addInitScript((t) => localStorage.setItem('bazar-docs:tema', t), tema)
      const page = await contexto.newPage()
      const erros: string[] = []
      page.on('pageerror', (e) => erros.push(`pageerror: ${e.message}`))
      page.on('console', (m) => m.type() === 'error' && erros.push(`console: ${m.text()}`))

      for (const rota of rotas) {
        await page.goto(base + rota.caminho, { waitUntil: 'networkidle' })
        const escuro = await page.evaluate(() => document.documentElement.classList.contains('dark'))
        if (escuro !== (tema === 'escuro')) falha(`${rota.nome}: tema aplicado errado`)
        if (rota.nome === 'componentes') {
          const diagramas = await esperarMermaid(page, 3)
          if (diagramas >= 3) ok(`componentes: ${diagramas} diagramas Mermaid renderizados`)
          else falha(`componentes: só ${diagramas} diagramas Mermaid`)
          const shiki = await page.locator('pre.shiki').count()
          if (shiki >= 8) ok(`componentes: ${shiki} blocos com Shiki`)
          else falha(`componentes: só ${shiki} blocos com Shiki`)
          await page.waitForSelector('figure:has-text("Passo 1 de 7")', { timeout: 10000 }).catch(() => undefined)
        }
        // Rolagem para disparar carregamentos sob demanda antes da captura
        await page.evaluate(async () => {
          for (let y = 0; y < document.body.scrollHeight; y += 800) {
            window.scrollTo(0, y)
            await new Promise((r) => setTimeout(r, 40))
          }
          window.scrollTo(0, 0)
        })
        await page.waitForTimeout(300)
        const larguraExcedente = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth)
        if (larguraExcedente > 1) falha(`${rota.nome}: rolagem horizontal de ${larguraExcedente}px`)
        await page.screenshot({ path: join(pastaSaida, `${tela.nome}-${tema}-${rota.nome}.png`), fullPage: true })
        await page.screenshot({ path: join(pastaSaida, `${tela.nome}-${tema}-${rota.nome}-topo.png`) })
      }
      ok('capturas salvas')

      // Checagens funcionais (uma vez por tela, no tema claro)
      if (tema === 'claro') {
        // AnimatedFlow
        await page.goto(`${base}/componentes`, { waitUntil: 'networkidle' })
        const fluxo = page.locator('figure:has-text("O caminho de GET /v1/produtos/42")')
        await fluxo.getByRole('button', { name: 'Próximo' }).click()
        if (await page.locator('text=Passo 2 de 7').count()) ok('AnimatedFlow avança')
        else falha('AnimatedFlow não avançou')
        await fluxo.scrollIntoViewIfNeeded()
        await page.screenshot({ path: join(pastaSaida, `${tela.nome}-animated-flow.png`), clip: (await fluxo.boundingBox()) ?? undefined })

        // Tooltip / popover do glossário
        const termo = page.locator('button.cursor-help').first()
        await termo.scrollIntoViewIfNeeded()
        if (tela.isMobile) await termo.tap()
        else await termo.hover()
        try {
          await page.waitForSelector('text=Propriedade de uma operação', { timeout: 4000 })
          ok('definição do glossário aparece no termo')
        } catch {
          falha('tooltip do glossário não apareceu')
        }
        await page.keyboard.press('Escape')

        // Busca
        if (tela.isMobile) await page.getByRole('button', { name: 'Buscar na documentação' }).tap()
        else await page.keyboard.press('Control+K')
        await page.getByPlaceholder(/Buscar:/).fill('idempotencia')
        try {
          await page.waitForSelector('[cmdk-item]:has-text("Idempot")', { timeout: 8000 })
          const n = await page.locator('[cmdk-item]').count()
          ok(`busca: ${n} resultados para "idempotencia"`)
          await page.screenshot({ path: join(pastaSaida, `${tela.nome}-busca.png`) })
          await page.keyboard.press('Enter')
          await page.waitForURL(/\/[^/]+\/[^/]+/, { timeout: 5000 })
          ok(`busca: Enter abre ${new URL(page.url()).pathname}`)
        } catch {
          falha('busca não retornou resultados')
          await page.screenshot({ path: join(pastaSaida, `${tela.nome}-busca-falha.png`) })
        }

        // Progresso
        await page.goto(`${base}/comecando-e-fundamentos-de-http/status-codes-em-profundidade`, { waitUntil: 'networkidle' })
        await page.getByRole('button', { name: 'Marcar como lida' }).first().click()
        const salvo = await page.evaluate(() => JSON.parse(localStorage.getItem('bazar-docs:progresso') ?? '{}')?.state?.lidas ?? {})
        if (salvo['02.03']) ok('progresso: página marcada como lida no localStorage')
        else falha('progresso não foi salvo')
        await page.goto(base, { waitUntil: 'networkidle' })
        const continuar = await page.getByRole('link', { name: /Continuar de onde parei|Começar a trilha/ }).getAttribute('href')
        ok(`início: botão de continuar aponta para ${continuar}`)

        if (!tela.isMobile) {
          await page.goto(`${base}/componentes`, { waitUntil: 'networkidle' })
          await page.waitForSelector('nav[aria-label="Nesta página"] a', { timeout: 5000 }).catch(() => undefined)
          const toc = await page.locator('nav[aria-label="Nesta página"] a').count()
          if (toc > 10) ok(`índice "Nesta página" com ${toc} itens`)
          else falha(`índice "Nesta página" com ${toc} itens`)
        } else {
          await page.getByRole('button', { name: 'Abrir navegação' }).tap()
          await page.waitForSelector('[role="dialog"] nav[aria-label="Navegação da documentação"]', { timeout: 4000 })
          await page.screenshot({ path: join(pastaSaida, 'celular-menu.png') })
          ok('menu lateral abre no celular')
        }
      }

      const relevantes = erros.filter((e) => !/favicon/.test(e))
      if (relevantes.length) falha(`erros no console: ${[...new Set(relevantes)].slice(0, 5).join(' | ')}`)
      else ok('sem erros no console')
      await contexto.close()
    }
  }
} finally {
  await navegador.close()
  await new Promise<void>((r) => servidor.httpServer.close(() => r()))
}

console.log(`\nCapturas em ${pastaSaida}`)
if (falhas.length) {
  console.log(`\n${falhas.length} falha(s):\n- ${falhas.join('\n- ')}`)
  process.exit(1)
}
console.log('\nTudo certo.')
