package br.com.chamados.controller;

import br.com.chamados.security.UsuarioAutenticado;
import br.com.chamados.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Area do proprio usuario: ver os dados e trocar a senha.
 */
@Controller
@RequestMapping("/meu-perfil")
public class PerfilController extends ControllerBase {

    public PerfilController(UsuarioService usuarioService) {
        super(usuarioService);
    }

    @GetMapping
    public String meuPerfil(@AuthenticationPrincipal UsuarioAutenticado principal, Model model) {
        model.addAttribute("usuario", logado(principal));
        return "perfil";
    }

    @PostMapping("/senha")
    public String alterarSenha(@AuthenticationPrincipal UsuarioAutenticado principal,
                               @RequestParam String senhaAtual,
                               @RequestParam String novaSenha,
                               @RequestParam String confirmacao,
                               RedirectAttributes redirect) {
        usuarioService.alterarSenha(principal.getUsuario().getId(), senhaAtual, novaSenha, confirmacao);
        redirect.addFlashAttribute("sucesso", "Senha alterada com sucesso.");
        return "redirect:/meu-perfil";
    }
}
