package br.com.chamados.controller;

import br.com.chamados.model.Chamado;
import br.com.chamados.model.StatusChamado;
import br.com.chamados.model.Usuario;
import br.com.chamados.repository.ChamadoRepository;
import br.com.chamados.repository.ClienteRepository;
import br.com.chamados.repository.UsuarioRepository;
import br.com.chamados.security.UsuarioAutenticado;
import br.com.chamados.service.ChamadoSpecs;
import br.com.chamados.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
public class DashboardController extends ControllerBase {

    private final ChamadoRepository chamadoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    public DashboardController(UsuarioService usuarioService,
                               ChamadoRepository chamadoRepository,
                               ClienteRepository clienteRepository,
                               UsuarioRepository usuarioRepository) {
        super(usuarioService);
        this.chamadoRepository = chamadoRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/")
    public String dashboard(@AuthenticationPrincipal UsuarioAutenticado principal, Model model) {
        Usuario usuario = logado(principal);

        model.addAttribute("usuario", usuario);
        model.addAttribute("abertos", chamadoRepository.countByStatus(StatusChamado.ABERTO));
        model.addAttribute("emAndamento", chamadoRepository.countByStatus(StatusChamado.EM_ANDAMENTO));
        model.addAttribute("aguardando", chamadoRepository.countByStatus(StatusChamado.AGUARDANDO_CLIENTE));
        model.addAttribute("resolvidos", chamadoRepository.countByStatus(StatusChamado.RESOLVIDO));

        if (usuario.isEquipeInterna()) {
            model.addAttribute("semTecnico",
                    chamadoRepository.countByTecnicoIsNullAndStatus(StatusChamado.ABERTO));
            model.addAttribute("minhaFila",
                    chamadoRepository.countByTecnicoIdAndStatusIn(usuario.getId(), ChamadoSpecs.STATUS_EM_ABERTO));
            model.addAttribute("abertosHoje",
                    chamadoRepository.contarAbertosDesde(LocalDate.now().atStartOfDay()));
            model.addAttribute("totalClientes", clienteRepository.countByAtivoTrue());
            model.addAttribute("totalUsuarios", usuarioRepository.countByAtivoTrue());

            List<Chamado> meus = chamadoRepository.findTop10ByTecnicoIdAndStatusInOrderByAbertoEmDesc(
                    usuario.getId(), ChamadoSpecs.STATUS_EM_ABERTO);
            model.addAttribute("meusChamados", meus);
            model.addAttribute("recentes", chamadoRepository.findTop10ByOrderByAbertoEmDesc());
        } else {
            Long clienteId = usuario.getCliente() != null ? usuario.getCliente().getId() : -1L;
            model.addAttribute("minhaFila",
                    chamadoRepository.countByClienteIdAndStatusIn(clienteId, ChamadoSpecs.STATUS_EM_ABERTO));
            model.addAttribute("recentes", chamadoRepository.findTop10ByClienteIdOrderByAbertoEmDesc(clienteId));
        }

        model.addAttribute("saudacao", saudacao());
        return "dashboard";
    }

    private String saudacao() {
        LocalTime agora = LocalTime.now();
        if (agora.isBefore(LocalTime.NOON)) {
            return "Bom dia";
        }
        if (agora.isBefore(LocalTime.of(18, 0))) {
            return "Boa tarde";
        }
        return "Boa noite";
    }
}
