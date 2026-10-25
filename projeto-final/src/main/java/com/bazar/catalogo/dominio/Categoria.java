package com.bazar.catalogo.dominio;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.jspecify.annotations.Nullable;

/**
 * Uma categoria do catálogo. Pode ter uma categoria pai (Livros → Programação).
 * Os métodos de acesso mantêm os nomes de quando ela era um record (nome(), slug()...).
 */
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    private UUID id;

    @Column(nullable = false, length = 60)
    private String nome;

    @Column(nullable = false, unique = true, length = 60)
    private String slug;

    // A coluna se chamava categoria_pai_id; foi renomeada com expand-contract (V4 a V6, 10.02).
    @Column(name = "pai_id")
    private @Nullable UUID categoriaPaiId;

    @Version
    private @Nullable Long versao;

    protected Categoria() {
    }

    public Categoria(UUID id, String nome, String slug, @Nullable UUID categoriaPaiId) {
        this.id = id;
        this.nome = nome;
        this.slug = slug;
        this.categoriaPaiId = categoriaPaiId;
    }

    // O PUT substitui os campos de uma categoria carregada do banco, em vez de criar outra instância
    // com o mesmo id: assim o @Version continua valendo e o Spring Data não a confunde com uma nova.
    public void substituir(String nome, String slug, @Nullable UUID categoriaPaiId) {
        this.nome = nome;
        this.slug = slug;
        this.categoriaPaiId = categoriaPaiId;
    }

    public UUID id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public String slug() {
        return slug;
    }

    public @Nullable UUID categoriaPaiId() {
        return categoriaPaiId;
    }
}
