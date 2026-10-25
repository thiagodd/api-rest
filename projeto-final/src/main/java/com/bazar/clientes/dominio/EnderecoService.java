package com.bazar.clientes.dominio;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;
import com.bazar.compartilhado.erros.RegraDeNegocioException;
import com.bazar.compartilhado.erros.TipoProblema;

import org.springframework.stereotype.Service;

/**
 * As regras dos endereços de um cliente: no máximo {@value #LIMITE}, e exatamente um principal quando há algum.
 * Toda operação começa confirmando que o cliente existe e só enxerga os endereços dele.
 */
@Service
public class EnderecoService {

    public static final int LIMITE = 10;

    private final ClienteRepository clientes;
    private final EnderecoRepository enderecos;

    public EnderecoService(ClienteRepository clientes, EnderecoRepository enderecos) {
        this.clientes = clientes;
        this.enderecos = enderecos;
    }

    public List<Endereco> listar(UUID clienteId) {
        exigirCliente(clienteId);
        return enderecos.findAllByClienteIdOrderByCriadoEm(clienteId);
    }

    public Endereco detalhar(UUID clienteId, UUID enderecoId) {
        exigirCliente(clienteId);
        return buscar(clienteId, enderecoId);
    }

    // synchronized: com os endereços em memória, é o que impede dois "principal" em requisições simultâneas.
    // No banco, um índice único parcial faz esse papel (ver a página 12.03).
    public synchronized Endereco adicionar(UUID clienteId, Endereco.Dados dados) {
        exigirCliente(clienteId);
        var existentes = enderecos.findAllByClienteIdOrderByCriadoEm(clienteId);
        if (existentes.size() >= LIMITE) {
            throw new RegraDeNegocioException(TipoProblema.LIMITE_DE_ENDERECOS, Map.of("limite", LIMITE), LIMITE);
        }
        // O primeiro endereço é o principal, mesmo que o cliente não peça.
        var principal = dados.principal() || existentes.isEmpty();
        if (principal) {
            desmarcarPrincipal(existentes);
        }
        return enderecos.save(Endereco.novo(clienteId, dados).comPrincipal(principal));
    }

    public synchronized Endereco substituir(UUID clienteId, UUID enderecoId, Endereco.Dados dados) {
        exigirCliente(clienteId);
        var atual = buscar(clienteId, enderecoId);
        if (atual.principal() && !dados.principal()) {
            throw new RegraDeNegocioException(TipoProblema.ENDERECO_PRINCIPAL_OBRIGATORIO, Map.of());
        }
        if (dados.principal() && !atual.principal()) {
            desmarcarPrincipal(enderecos.findAllByClienteIdOrderByCriadoEm(clienteId));
        }
        return enderecos.save(atual.com(dados));
    }

    public synchronized void remover(UUID clienteId, UUID enderecoId) {
        exigirCliente(clienteId);
        var endereco = buscar(clienteId, enderecoId);
        enderecos.delete(endereco);
        // Removido o principal, o endereço mais antigo que restou assume.
        if (endereco.principal()) {
            enderecos.findAllByClienteIdOrderByCriadoEm(clienteId).stream()
                    .findFirst()
                    .ifPresent(maisAntigo -> enderecos.save(maisAntigo.comPrincipal(true)));
        }
    }

    private void exigirCliente(UUID clienteId) {
        if (clientes.findById(clienteId).isEmpty()) {
            throw new RecursoNaoEncontradoException(TipoProblema.CLIENTE_NAO_ENCONTRADO, clienteId);
        }
    }

    // A verificação de pertencimento: um endereço de outro cliente é tratado como inexistente (404, não 403).
    private Endereco buscar(UUID clienteId, UUID enderecoId) {
        return enderecos.findByIdAndClienteId(enderecoId, clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(TipoProblema.ENDERECO_NAO_ENCONTRADO, enderecoId));
    }

    private void desmarcarPrincipal(List<Endereco> existentes) {
        existentes.stream()
                .filter(Endereco::principal)
                .forEach(endereco -> enderecos.save(endereco.comPrincipal(false)));
    }
}
