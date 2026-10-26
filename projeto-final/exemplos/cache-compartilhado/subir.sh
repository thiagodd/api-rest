#!/usr/bin/env bash
# Sobe o Varnish na porta 6081, na rede do host, encaminhando para o Bazar em localhost:8080 (20.01).
set -euo pipefail
cd "$(dirname "$0")"
exec docker run --rm --name bazar-varnish --network host \
  -e VARNISH_HTTP_PORT=6081 \
  -v "$PWD/default.vcl:/etc/varnish/default.vcl:ro" \
  varnish:9.0.4
