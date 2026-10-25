#!/usr/bin/env bash
# Confere o contrato antes do merge (17.04): o guia de estilo (Spectral) e as mudanças incompatíveis em relação a uma
# versão anterior (oasdiff). Uso: scripts/verificar-contrato.sh [referência do git, padrão origin/main]
set -euo pipefail

BASE="${1:-origin/main}"
CONTRATO="docs/openapi/bazar-api.yaml"
cd "$(dirname "$0")/.."

echo "== Guia de estilo (Spectral)"
npx --yes @stoplight/spectral-cli@6.17.0 lint "$CONTRATO" --fail-severity error

echo "== Mudanças em relação a $BASE (oasdiff)"
anterior="$(mktemp -d)"
trap 'rm -rf "$anterior"' EXIT
# O caminho do contrato no repositório, que pode não ser a raiz (git show quer o caminho completo).
git show "$BASE:$(git rev-parse --show-prefix)$CONTRATO" > "$anterior/bazar-api.yaml"
cp "$CONTRATO" "$anterior/atual.yaml"
docker run --rm -v "$anterior:/specs:ro" tufin/oasdiff:v1.34.1 \
    breaking /specs/bazar-api.yaml /specs/atual.yaml --fail-on ERR
