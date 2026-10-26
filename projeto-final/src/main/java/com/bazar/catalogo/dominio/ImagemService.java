package com.bazar.catalogo.dominio;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import com.bazar.compartilhado.Ids;
import com.bazar.compartilhado.erros.CampoInvalidoException;
import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;
import com.bazar.compartilhado.erros.RegraDeNegocioException;
import com.bazar.compartilhado.erros.TipoProblema;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;

/**
 * As imagens dos produtos (22.01). O conteúdo vai para o armazenamento em blocos, sem passar inteiro pela memória, e é
 * conferido pelo que ele é (os primeiros bytes e as dimensões), não pelo que o cliente diz que ele é.
 */
@Service
public class ImagemService {

    public static final DataSize TAMANHO_MAXIMO = DataSize.ofMegabytes(5);
    public static final int MAXIMO_POR_PRODUTO = 10;
    // Uma "bomba de descompressão": um PNG de 50 KB pode declarar 50000 x 50000 pixels, e decodificá-lo pede 10 GB de
    // memória. As dimensões são lidas do cabeçalho, sem decodificar a imagem, e conferidas antes de qualquer uso.
    static final int LADO_MAXIMO = 8000;

    private final ProdutoService produtos;
    private final ImagensDeProduto imagens;
    private final ArmazenamentoDeArquivos armazenamento;
    private final Clock relogio;

    public ImagemService(ProdutoService produtos, ImagensDeProduto imagens, ArmazenamentoDeArquivos armazenamento,
            Clock relogio) {
        this.produtos = produtos;
        this.imagens = imagens;
        this.armazenamento = armazenamento;
        this.relogio = relogio;
    }

    public ImagemProduto adicionar(UUID produtoId, String nomeOriginal, @Nullable String textoAlternativo,
            InputStream conteudo) {
        produtos.detalhar(produtoId);
        if (imagens.quantas(produtoId) >= MAXIMO_POR_PRODUTO) {
            throw new RegraDeNegocioException(TipoProblema.LIMITE_DE_IMAGENS, Map.of(), MAXIMO_POR_PRODUTO);
        }
        var entrada = new BufferedInputStream(new ComLimite(conteudo, TAMANHO_MAXIMO.toBytes()));
        var tipo = reconhecer(entrada);
        // A chave é do Bazar: um UUID e a extensão do formato reconhecido. O nome que o cliente mandou
        // ("../../etc/passwd", "foto.php.jpg") só é guardado como texto, para exibição.
        var id = Ids.novo();
        var chave = id + "." + tipo.extensao();
        var tamanho = armazenamento.gravar(chave, entrada);
        try {
            var dimensoes = dimensoes(armazenamento.caminho(chave));
            var imagem = new ImagemProduto(id, produtoId, chave, nomeSeguro(nomeOriginal), tipo.tipoConteudo(), tamanho,
                    dimensoes[0], dimensoes[1], textoAlternativo, imagens.proximaOrdem(produtoId), relogio.instant());
            imagens.inserir(imagem);
            return imagem;
        }
        catch (RuntimeException ex) {
            // O arquivo foi gravado, mas a imagem não foi aceita (ou o insert falhou): nada de arquivo órfão.
            armazenamento.apagar(chave);
            throw ex;
        }
    }

    /** Uma imagem do produto (22.02). A de outro produto não existe para este: 404, como uma que não existe. */
    public ImagemProduto buscar(UUID produtoId, UUID id) {
        return imagens.buscar(produtoId, id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(TipoProblema.IMAGEM_NAO_ENCONTRADA, id));
    }

    public Path arquivo(ImagemProduto imagem) {
        return armazenamento.caminho(imagem.chave());
    }

    private static TipoDeImagem reconhecer(BufferedInputStream entrada) {
        try {
            entrada.mark(TipoDeImagem.BYTES_DA_ASSINATURA);
            var inicio = entrada.readNBytes(TipoDeImagem.BYTES_DA_ASSINATURA);
            entrada.reset();
            return TipoDeImagem.reconhecer(inicio)
                    .orElseThrow(() -> new RegraDeNegocioException(TipoProblema.IMAGEM_NAO_ACEITA, Map.of()));
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    // Lê só o cabeçalho: largura e altura, sem decodificar os pixels. Um arquivo que começa como PNG mas não é um PNG
    // válido não tem leitor ou falha aqui.
    private static int[] dimensoes(Path arquivo) {
        try (var entrada = ImageIO.createImageInputStream(arquivo.toFile())) {
            var leitores = ImageIO.getImageReaders(entrada);
            if (!leitores.hasNext()) {
                throw new RegraDeNegocioException(TipoProblema.IMAGEM_NAO_ACEITA, Map.of());
            }
            var leitor = leitores.next();
            try {
                leitor.setInput(entrada, true, true);
                int largura = leitor.getWidth(0);
                int altura = leitor.getHeight(0);
                if (largura > LADO_MAXIMO || altura > LADO_MAXIMO) {
                    throw new CampoInvalidoException("arquivo", "bazar.imagem.dimensoes");
                }
                return new int[] { largura, altura };
            }
            finally {
                leitor.dispose();
            }
        }
        catch (IOException ex) {
            throw new RegraDeNegocioException(TipoProblema.IMAGEM_NAO_ACEITA, Map.of());
        }
    }

    // Só para exibição: sem caminho, sem caracteres de controle, com tamanho limitado.
    static String nomeSeguro(String nome) {
        var semCaminho = nome.substring(Math.max(nome.lastIndexOf('/'), nome.lastIndexOf('\\')) + 1);
        var limpo = semCaminho.replaceAll("\\p{Cntrl}", "").strip();
        return limpo.isEmpty() ? "imagem" : limpo.substring(0, Math.min(limpo.length(), 200));
    }

    /**
     * Conta os bytes lidos e para no limite. No multipart, o Tomcat já recusa antes (spring.servlet.multipart); num
     * corpo binário direto, sem Content-Length confiável (chunked), é aqui que o limite vale.
     */
    static final class ComLimite extends FilterInputStream {

        private final long limite;
        private long lidos;

        ComLimite(InputStream entrada, long limite) {
            super(entrada);
            this.limite = limite;
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            if (b >= 0) {
                contar(1);
            }
            return b;
        }

        @Override
        public int read(byte[] destino, int inicio, int quantos) throws IOException {
            int n = super.read(destino, inicio, quantos);
            if (n > 0) {
                contar(n);
            }
            return n;
        }

        private void contar(int n) {
            lidos += n;
            if (lidos > limite) {
                throw new RegraDeNegocioException(TipoProblema.ARQUIVO_GRANDE_DEMAIS, Map.of(),
                        TAMANHO_MAXIMO.toMegabytes());
            }
        }
    }
}
