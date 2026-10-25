package com.bazar.compartilhado.web;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.bazar.compartilhado.Lista;
import org.jspecify.annotations.Nullable;

import org.springframework.core.ResolvableType;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;

/**
 * Escreve qualquer Lista de records como CSV (RFC 4180): uma linha de cabeçalho com os nomes dos componentes
 * e uma linha por item. Só escreve; a API não recebe CSV.
 */
public class ListaCsvHttpMessageConverter extends AbstractGenericHttpMessageConverter<Lista<?>> {

    public static final MediaType TEXT_CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    public ListaCsvHttpMessageConverter() {
        super(TEXT_CSV);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return Lista.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean canRead(Type type, @Nullable Class<?> contextClass, @Nullable MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(@Nullable Type type, Class<?> clazz, @Nullable MediaType mediaType) {
        return supports(clazz) && tipoDoItem(type) != null && canWrite(mediaType);
    }

    @Override
    protected void writeInternal(Lista<?> lista, @Nullable Type type, HttpOutputMessage saida) throws IOException {
        var componentes = Objects.requireNonNull(tipoDoItem(type)).getRecordComponents();   // garantido pelo canWrite
        Writer escritor = new OutputStreamWriter(saida.getBody(), StandardCharsets.UTF_8);
        escreverLinha(escritor, Arrays.stream(componentes).map(RecordComponent::getName).toList());
        for (Object item : lista.itens()) {
            escreverLinha(escritor, Arrays.stream(componentes).map(c -> celula(valor(c, item))).toList());
        }
        escritor.flush();
    }

    // O tipo do item vem do tipo genérico declarado no controller (Lista<ProdutoResponse>), não dos itens:
    // assim o cabeçalho sai mesmo com a lista vazia.
    private static @Nullable Class<?> tipoDoItem(@Nullable Type type) {
        if (type == null) {
            return null;
        }
        var item = ResolvableType.forType(type).as(Lista.class).getGeneric(0).resolve();
        return item != null && item.isRecord() ? item : null;
    }

    private static @Nullable Object valor(RecordComponent componente, Object item) {
        try {
            return componente.getAccessor().invoke(item);
        }
        catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    // Mapas viram "chave=valor; chave=valor", em ordem alfabética; null vira célula vazia.
    private static String celula(@Nullable Object valor) {
        if (valor == null) {
            return "";
        }
        if (valor instanceof Map<?, ?> mapa) {
            return new TreeMap<>(mapa).entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("; "));
        }
        return valor.toString();
    }

    // RFC 4180: campos com vírgula, aspas ou quebra de linha vão entre aspas, com as aspas internas dobradas.
    private static void escreverLinha(Writer escritor, List<String> campos) throws IOException {
        var linha = campos.stream()
                .map(c -> c.contains(",") || c.contains("\"") || c.contains("\n") || c.contains("\r")
                        ? "\"" + c.replace("\"", "\"\"") + "\""
                        : c)
                .collect(Collectors.joining(","));
        escritor.write(linha);
        escritor.write("\r\n");
    }

    @Override
    public Lista<?> read(Type type, @Nullable Class<?> contextClass, HttpInputMessage entrada) {
        throw new HttpMessageNotReadableException("CSV não é aceito como entrada", entrada);
    }

    @Override
    protected Lista<?> readInternal(Class<? extends Lista<?>> clazz, HttpInputMessage entrada) {
        throw new HttpMessageNotReadableException("CSV não é aceito como entrada", entrada);
    }
}
