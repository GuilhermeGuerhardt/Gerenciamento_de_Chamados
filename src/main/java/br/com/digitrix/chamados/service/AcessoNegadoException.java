package br.com.digitrix.chamados.service;

/**
 * Usuario autenticado tentando acessar algo fora do seu perfil.
 * Diferente da NegocioException para que o handler possa responder 403.
 */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
