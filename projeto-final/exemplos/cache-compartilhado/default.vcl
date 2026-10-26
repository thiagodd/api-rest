vcl 4.1;

# Um cache compartilhado de laboratório na frente do Bazar (20.01), no papel de uma CDN. Fora as sub-rotinas
# abaixo, vale a VCL embutida do Varnish: não guarda respostas com private, no-store, no-cache ou Set-Cookie, e
# nem consulta o cache para requisições com Authorization ou Cookie.

backend bazar {
    .host = "127.0.0.1";
    .port = "8080";
}

sub vcl_backend_response {
    # Guarda a cópia por mais 10 minutos depois de vencida (e do stale-while-revalidate), só para revalidar: com
    # ETag ou Last-Modified, a próxima ida ao Bazar é condicional, e um 304 renova a cópia sem trazer o corpo (20.02).
    set beresp.keep = 10m;
}

sub vcl_deliver {
    # Para ver o que aconteceu: HIT (servido do cache) ou MISS (foi até o Bazar).
    if (obj.hits > 0) {
        set resp.http.X-Cache = "HIT";
    } else {
        set resp.http.X-Cache = "MISS";
    }
}
