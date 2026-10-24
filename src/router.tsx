import { lazy, Suspense } from 'react'
import { createBrowserRouter, Navigate } from 'react-router'
import { AppLayout } from '@/components/layout/app-layout'
import { paginaPorId } from '@/lib/navegacao'
import { DocPage } from '@/pages/doc-page'
import { Home } from '@/pages/home'
import { NotFound } from '@/pages/not-found'
import { Referencias } from '@/pages/referencias'

// A página de componentes é grande e pouco visitada: fica em um chunk próprio
const Componentes = lazy(() => import('@/pages/componentes').then((m) => ({ default: m.Componentes })))

// O glossário é o Apêndice D (32.04); /glossario é só um atalho
const glossario = paginaPorId('32.04')!

export const router = createBrowserRouter([
  {
    element: <AppLayout />,
    children: [
      { index: true, element: <Home /> },
      {
        path: 'componentes',
        element: (
          <Suspense>
            <Componentes />
          </Suspense>
        ),
      },
      { path: 'referencias', element: <Referencias /> },
      { path: 'glossario', element: <Navigate to={glossario.url} replace /> },
      { path: ':parte/:pagina', element: <DocPage /> },
      { path: '*', element: <NotFound /> },
    ],
  },
])
