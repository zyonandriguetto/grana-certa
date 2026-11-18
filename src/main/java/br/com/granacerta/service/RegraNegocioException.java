package br.com.granacerta.service;

/** Erro de regra de negócio, exibido ao usuário como mensagem amigável. */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
