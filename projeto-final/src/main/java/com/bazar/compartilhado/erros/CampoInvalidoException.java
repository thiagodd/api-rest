package com.bazar.compartilhado.erros;

/**
 * Um campo da requisição que passou pelo Bean Validation, mas é inválido por um motivo que só o servidor
 * conhece (uma categoria que não existe, por exemplo). Vira o mesmo 422 da validação (08.02).
 */
public class CampoInvalidoException extends RuntimeException {

    private final String campo;
    private final String codigoDaMensagem;

    public CampoInvalidoException(String campo, String codigoDaMensagem) {
        super(campo + ": " + codigoDaMensagem);
        this.campo = campo;
        this.codigoDaMensagem = codigoDaMensagem;
    }

    public String getCampo() {
        return campo;
    }

    public String getCodigoDaMensagem() {
        return codigoDaMensagem;
    }
}
