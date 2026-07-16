package br.com.digitrix.chamados.controller;

import br.com.digitrix.chamados.model.Cliente;
import br.com.digitrix.chamados.model.Contato;
import br.com.digitrix.chamados.service.ClienteService;
import br.com.digitrix.chamados.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ClienteController extends ControllerBase {

    private final ClienteService clienteService;

    public ClienteController(UsuarioService usuarioService, ClienteService clienteService) {
        super(usuarioService);
        this.clienteService = clienteService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String busca, Model model) {
        model.addAttribute("clientes", clienteService.listar(busca));
        model.addAttribute("busca", busca);
        return "clientes/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "clientes/formulario";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clienteService.buscarPorId(id));
        model.addAttribute("contatos", clienteService.listarContatos(id));
        return "clientes/detalhe";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clienteService.buscarPorId(id));
        return "clientes/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Cliente cliente,
                         BindingResult resultado,
                         RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            return "clientes/formulario";
        }
        Cliente salvo = clienteService.salvar(cliente);
        redirect.addFlashAttribute("sucesso", "Cliente salvo com sucesso.");
        return "redirect:/clientes/" + salvo.getId();
    }

    @PostMapping("/{id}/alternar-ativo")
    public String alternarAtivo(@PathVariable Long id, RedirectAttributes redirect) {
        clienteService.alternarAtivo(id);
        redirect.addFlashAttribute("sucesso", "Situacao do cliente atualizada.");
        return "redirect:/clientes/" + id;
    }

    @PostMapping("/{id}/excluir")
    @PreAuthorize("hasRole('ADMIN')")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        clienteService.excluir(id);
        redirect.addFlashAttribute("sucesso", "Cliente excluido.");
        return "redirect:/clientes";
    }

    // ---------- Contatos (funcionarios do cliente) ----------

    @GetMapping("/{clienteId}/contatos/novo")
    public String novoContato(@PathVariable Long clienteId, Model model) {
        model.addAttribute("cliente", clienteService.buscarPorId(clienteId));
        model.addAttribute("contato", new Contato());
        return "clientes/formulario-contato";
    }

    @GetMapping("/{clienteId}/contatos/{contatoId}/editar")
    public String editarContato(@PathVariable Long clienteId, @PathVariable Long contatoId, Model model) {
        model.addAttribute("cliente", clienteService.buscarPorId(clienteId));
        model.addAttribute("contato", clienteService.buscarContato(contatoId));
        return "clientes/formulario-contato";
    }

    @PostMapping("/{clienteId}/contatos/salvar")
    public String salvarContato(@PathVariable Long clienteId,
                                @Valid @ModelAttribute Contato contato,
                                BindingResult resultado,
                                Model model,
                                RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            model.addAttribute("cliente", clienteService.buscarPorId(clienteId));
            return "clientes/formulario-contato";
        }
        clienteService.salvarContato(clienteId, contato);
        redirect.addFlashAttribute("sucesso", "Contato salvo com sucesso.");
        return "redirect:/clientes/" + clienteId;
    }

    @PostMapping("/{clienteId}/contatos/{contatoId}/excluir")
    public String excluirContato(@PathVariable Long clienteId,
                                 @PathVariable Long contatoId,
                                 RedirectAttributes redirect) {
        clienteService.excluirContato(contatoId);
        redirect.addFlashAttribute("sucesso", "Contato excluido.");
        return "redirect:/clientes/" + clienteId;
    }
}
