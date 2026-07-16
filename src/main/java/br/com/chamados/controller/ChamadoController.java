package br.com.chamados.controller;

import br.com.chamados.model.*;
import br.com.chamados.security.UsuarioAutenticado;
import br.com.chamados.service.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/chamados")
public class ChamadoController extends ControllerBase {

    private final ChamadoService chamadoService;
    private final ClienteService clienteService;

    public ChamadoController(UsuarioService usuarioService,
                             ChamadoService chamadoService,
                             ClienteService clienteService) {
        super(usuarioService);
        this.chamadoService = chamadoService;
        this.clienteService = clienteService;
    }

    @ModelAttribute("statusDisponiveis")
    public StatusChamado[] statusDisponiveis() {
        return StatusChamado.values();
    }

    @ModelAttribute("prioridades")
    public Prioridade[] prioridades() {
        return Prioridade.values();
    }

    @GetMapping
    public String listar(@ModelAttribute ChamadoFiltro filtro,
                         @AuthenticationPrincipal UsuarioAutenticado principal,
                         Model model) {
        Usuario usuario = logado(principal);

        model.addAttribute("chamados", chamadoService.listar(filtro, usuario));
        model.addAttribute("filtro", filtro);
        model.addAttribute("usuario", usuario);

        if (usuario.isEquipeInterna()) {
            model.addAttribute("clientes", clienteService.listarAtivos());
            model.addAttribute("tecnicos", usuarioService.listarAtendentesAtivos());
        }
        return "chamados/lista";
    }

    @GetMapping("/novo")
    public String novo(@AuthenticationPrincipal UsuarioAutenticado principal, Model model) {
        Usuario usuario = logado(principal);
        Chamado chamado = new Chamado();

        // Solicitante nao escolhe empresa: o chamado nasce ja vinculado a dele.
        if (!usuario.isEquipeInterna()) {
            chamado.setCliente(usuario.getCliente());
        }

        model.addAttribute("chamado", chamado);
        prepararFormulario(model, usuario, chamado.getCliente());
        return "chamados/formulario";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute Chamado chamado,
                         BindingResult resultado,
                         @AuthenticationPrincipal UsuarioAutenticado principal,
                         Model model,
                         RedirectAttributes redirect) {
        Usuario usuario = logado(principal);

        if (resultado.hasErrors()) {
            prepararFormulario(model, usuario, chamado.getCliente());
            return "chamados/formulario";
        }

        Chamado salvo;
        if (chamado.getId() == null) {
            salvo = chamadoService.abrir(chamado, usuario);
            redirect.addFlashAttribute("sucesso",
                    "Chamado " + salvo.getProtocolo() + " aberto com sucesso.");
        } else {
            salvo = chamadoService.atualizar(chamado.getId(), chamado, usuario);
            redirect.addFlashAttribute("sucesso", "Chamado atualizado.");
        }
        return "redirect:/chamados/" + salvo.getId();
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id,
                          @AuthenticationPrincipal UsuarioAutenticado principal,
                          Model model) {
        Usuario usuario = logado(principal);
        Chamado chamado = chamadoService.buscarComPermissao(id, usuario);

        model.addAttribute("chamado", chamado);
        model.addAttribute("usuario", usuario);
        model.addAttribute("comentarios", chamadoService.listarComentarios(id, usuario));
        model.addAttribute("historico", chamadoService.listarHistorico(id));
        model.addAttribute("podeEditar", chamadoService.podeEditar(chamado, usuario));

        if (usuario.isEquipeInterna()) {
            model.addAttribute("tecnicos", usuarioService.listarAtendentesAtivos());
        }
        return "chamados/detalhe";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                         @AuthenticationPrincipal UsuarioAutenticado principal,
                         Model model) {
        Usuario usuario = logado(principal);
        Chamado chamado = chamadoService.buscarComPermissao(id, usuario);

        if (!chamadoService.podeEditar(chamado, usuario)) {
            throw new AcessoNegadoException("Voce nao tem permissao para editar este chamado.");
        }

        model.addAttribute("chamado", chamado);
        prepararFormulario(model, usuario, chamado.getCliente());
        return "chamados/formulario";
    }

    @PostMapping("/{id}/atribuir")
    public String atribuir(@PathVariable Long id,
                           @RequestParam(required = false) Long tecnicoId,
                           @AuthenticationPrincipal UsuarioAutenticado principal,
                           RedirectAttributes redirect) {
        Usuario autor = logado(principal);
        Usuario tecnico = tecnicoId != null ? usuarioService.buscarPorId(tecnicoId) : null;

        chamadoService.atribuir(id, tecnico, autor);
        redirect.addFlashAttribute("sucesso", tecnico != null
                ? "Chamado atribuido a " + tecnico.getNome() + "."
                : "Atribuicao removida.");
        return "redirect:/chamados/" + id;
    }

    /** Atalho do tecnico para pegar um chamado da fila para si. */
    @PostMapping("/{id}/assumir")
    public String assumir(@PathVariable Long id,
                          @AuthenticationPrincipal UsuarioAutenticado principal,
                          RedirectAttributes redirect) {
        Usuario autor = logado(principal);
        chamadoService.atribuir(id, autor, autor);
        redirect.addFlashAttribute("sucesso", "Chamado assumido por voce.");
        return "redirect:/chamados/" + id;
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id,
                                @RequestParam StatusChamado status,
                                @RequestParam(required = false) String solucao,
                                @AuthenticationPrincipal UsuarioAutenticado principal,
                                RedirectAttributes redirect) {
        chamadoService.alterarStatus(id, status, solucao, logado(principal));
        redirect.addFlashAttribute("sucesso", "Status atualizado para " + status.getDescricao() + ".");
        return "redirect:/chamados/" + id;
    }

    @PostMapping("/{id}/reabrir")
    public String reabrir(@PathVariable Long id,
                          @AuthenticationPrincipal UsuarioAutenticado principal,
                          RedirectAttributes redirect) {
        chamadoService.reabrir(id, logado(principal));
        redirect.addFlashAttribute("sucesso", "Chamado reaberto.");
        return "redirect:/chamados/" + id;
    }

    @PostMapping("/{id}/comentar")
    public String comentar(@PathVariable Long id,
                           @RequestParam String texto,
                           @RequestParam(defaultValue = "false") boolean interno,
                           @AuthenticationPrincipal UsuarioAutenticado principal,
                           RedirectAttributes redirect) {
        chamadoService.comentar(id, texto, interno, logado(principal));
        redirect.addFlashAttribute("sucesso", "Comentario adicionado.");
        return "redirect:/chamados/" + id + "#comentarios";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id,
                          @AuthenticationPrincipal UsuarioAutenticado principal,
                          RedirectAttributes redirect) {
        chamadoService.excluir(id, logado(principal));
        redirect.addFlashAttribute("sucesso", "Chamado excluido.");
        return "redirect:/chamados";
    }

    /**
     * Lista os contatos do cliente escolhido, para o <select> de contato do
     * formulario ser recarregado sem sair da pagina.
     */
    @GetMapping("/contatos")
    @ResponseBody
    public List<ContatoResumo> contatosDoCliente(@RequestParam Long clienteId) {
        return clienteService.listarContatosAtivos(clienteId).stream()
                .map(c -> new ContatoResumo(c.getId(), c.getNome(), c.getCargo()))
                .toList();
    }

    public record ContatoResumo(Long id, String nome, String cargo) {
    }

    private void prepararFormulario(Model model, Usuario usuario, Cliente clienteSelecionado) {
        model.addAttribute("usuario", usuario);
        model.addAttribute("clientes", clienteService.listarAtivos());

        if (usuario.isEquipeInterna()) {
            model.addAttribute("tecnicos", usuarioService.listarAtendentesAtivos());
        }

        Cliente cliente = clienteSelecionado != null ? clienteSelecionado : usuario.getCliente();
        model.addAttribute("contatos", cliente != null
                ? clienteService.listarContatosAtivos(cliente.getId())
                : List.of());
    }
}
