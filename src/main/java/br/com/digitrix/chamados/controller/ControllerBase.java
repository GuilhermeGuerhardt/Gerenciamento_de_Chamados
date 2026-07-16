package br.com.digitrix.chamados.controller;

import br.com.digitrix.chamados.model.Usuario;
import br.com.digitrix.chamados.security.UsuarioAutenticado;
import br.com.digitrix.chamados.service.UsuarioService;

/**
 * O Usuario guardado na sessao vem desconectado do JPA. Antes de usa-lo como
 * autor ou associacao, recarregamos a versao atual do banco.
 */
public abstract class ControllerBase {

    protected final UsuarioService usuarioService;

    protected ControllerBase(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    protected Usuario logado(UsuarioAutenticado principal) {
        return usuarioService.buscarPorId(principal.getUsuario().getId());
    }
}
