import {
  Activity, BookOpen, Boxes, Container, Database, FileText, FlaskConical, Gauge, Leaf, Network,
  PenTool, Plug, Rocket, ShieldCheck, Store, Workflow, Zap, type LucideIcon,
} from 'lucide-react'

const icones: Record<string, LucideIcon> = {
  Activity, BookOpen, Boxes, Container, Database, FileText, FlaskConical, Gauge, Leaf, Network,
  PenTool, Plug, Rocket, ShieldCheck, Store, Workflow, Zap,
}

/** Ícone de uma parte, pelo nome definido em navigation.ts. */
export function IconeParte({ nome, className }: { nome: string; className?: string }) {
  const Icone = icones[nome] ?? BookOpen
  return <Icone className={className} aria-hidden />
}
