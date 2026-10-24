package com.bazar.catalogo.dominio;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

/**
 * Categorias guardadas em memória, enquanto o Bazar não tem banco de dados.
 * Os nomes dos métodos seguem os do Spring Data, que substitui esta classe em 10.01.
 */
@Repository
public class CategoriaRepository {

    private final Map<UUID, Categoria> categorias = new ConcurrentHashMap<>();

    public CategoriaRepository() {
        var livros = new Categoria(UUID.fromString("0192f3a0-5555-7000-8000-000000000001"), "Livros", "livros", null);
        var eletronicos = new Categoria(UUID.fromString("0192f3a0-5555-7000-8000-000000000002"), "Eletrônicos", "eletronicos", null);
        var programacao = new Categoria(UUID.fromString("0192f3a0-5555-7000-8000-000000000003"), "Programação", "programacao", livros.id());
        for (var categoria : List.of(livros, eletronicos, programacao)) {
            categorias.put(categoria.id(), categoria);
        }
    }

    public List<Categoria> findAll() {
        return categorias.values().stream()
                .sorted(Comparator.comparing(Categoria::nome))
                .toList();
    }

    public Optional<Categoria> findById(UUID id) {
        return Optional.ofNullable(categorias.get(id));
    }
}
