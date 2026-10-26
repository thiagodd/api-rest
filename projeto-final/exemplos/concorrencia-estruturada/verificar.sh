#!/usr/bin/env bash
# Roda os cenários do DetalheDoPedido (19.06) e confere o desfecho de cada um. Precisa de um JDK 25 no PATH ou em
# JAVA_HOME: a API de StructuredTaskScope muda entre as versões (JEP 505, 525, 533).
set -euo pipefail
cd "$(dirname "$0")"
JAVA="${JAVA_HOME:+$JAVA_HOME/bin/}java"

falhas=0
conferir() {
  local cenario=$1 esperado=$2
  local saida
  saida=$("$JAVA" --enable-preview --source 25 DetalheDoPedido.java "$cenario" 2>&1)
  if grep -q -- "$esperado" <<<"$saida"; then
    echo "ok    $cenario: $esperado"
  else
    echo "FALHA $cenario: esperava '$esperado'"; echo "$saida"; falhas=$((falhas + 1))
  fi
}

conferir sucesso     "detalhe pronto"
conferir sucesso     "req-7f3a         virtual"            # o ScopedValue chega às subtarefas
conferir falha       "frete: cancelado (interrompido)"
conferir falha       'StructuredTaskScope$FailedException'
conferir timeout     'StructuredTaskScope$TimeoutException'
conferir completable "(sem correlação) virtual"            # o ScopedValue não chega às threads do executor
conferir completable "frete: respondeu"                    # ninguém cancelou o frete

exit $falhas
