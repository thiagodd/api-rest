package com.bazar.catalogo.dominio;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Onde ficam os arquivos das imagens (22.01): um diretório local. Em produção, com mais de uma instância, um
 * armazenamento de objetos (S3, GCS, Azure Blob), com a mesma interface: gravar e ler por chave.
 */
@Component
public class ArmazenamentoDeArquivos {

    private final Path diretorio;

    public ArmazenamentoDeArquivos(@Value("${bazar.imagens.diretorio}") Path diretorio) {
        this.diretorio = diretorio;
    }

    /**
     * Copia o conteúdo para o arquivo da chave, em blocos, sem carregar tudo na memória. Grava num arquivo temporário
     * e só depois o renomeia: quem lê nunca vê um arquivo pela metade.
     */
    public long gravar(String chave, InputStream conteudo) {
        try {
            Files.createDirectories(diretorio);
            var temporario = Files.createTempFile(diretorio, "upload-", ".parcial");
            try {
                var bytes = Files.copy(conteudo, temporario, StandardCopyOption.REPLACE_EXISTING);
                Files.move(temporario, caminho(chave), StandardCopyOption.ATOMIC_MOVE);
                return bytes;
            }
            finally {
                Files.deleteIfExists(temporario);
            }
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void apagar(String chave) {
        try {
            Files.deleteIfExists(caminho(chave));
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public Path caminho(String chave) {
        // A chave é gerada pelo Bazar (um UUID e a extensão), mas a verificação fica: nada sai do diretório.
        var arquivo = diretorio.resolve(chave).normalize();
        if (!arquivo.getParent().equals(diretorio.normalize())) {
            throw new IllegalArgumentException("Chave fora do diretório: " + chave);
        }
        return arquivo;
    }
}
