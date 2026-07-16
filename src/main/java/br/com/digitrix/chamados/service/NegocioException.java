package br.com.digitrix.chamados.service;

/**
 * Regra de negocio violada. A mensagem e escrita para ser mostrada ao usuario.
 */
public class NegocioException extends RuntimeException {

    public NegocioException(String mensagem) {
        super(mensagem);
    }
}
