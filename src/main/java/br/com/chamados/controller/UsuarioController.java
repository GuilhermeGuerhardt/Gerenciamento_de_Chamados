package br.com.chamados.controller;

import br.com.chamados.model.Perfil;
import br.com.chamados.model.Usuario;
import br.com.chamados.security.UsuarioAutenticado;
import br.com.chamados.service.ClienteService;
import br.com.chamados.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController extends ControllerBase {

    private final ClienteService clienteService;

    public UsuarioController(UsuarioService usuarioService, ClienteService clienteService) {
        super(usuarioService);
        this.clienteService = clienteService;
    }

    @ModelAttribute("perfis")
    public Perfil[] perfis() {
        return Perfil.values();
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listar());
        return "usuarios/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("clientes", clienteService.listarAtivos());
        return "usuarios/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", usuarioService.buscarPorId(id));
        model.addAttribute("clientes", clienteService.listarAtivos());
        return "usuarios/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Usuario usuario,
                         BindingResult resultado,
                         @RequestParam(required = false) String novaSenha,
                         Model model,
                         RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            model.addAttribute("clientes", clienteService.listarAtivos());
            return "usuarios/formulario";
        }
        usuarioService.salvar(usuario, novaSenha);
        redirect.addFlashAttribute("sucesso", "Usuario salvo com sucesso.");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/alternar-ativo")
    public String alternarAtivo(@PathVariable Long id,
                                @AuthenticationPrincipal UsuarioAutenticado principal,
                                RedirectAttributes redirect) {
        usuarioService.alternarAtivo(id, logado(principal));
        redirect.addFlashAttribute("sucesso", "Situacao do usuario atualizada.");
        return "redirect:/usuarios";
    }
}
