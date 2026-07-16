package br.com.digitrix.chamados.service;

import br.com.digitrix.chamados.model.Chamado;
import br.com.digitrix.chamados.model.StatusChamado;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Monta a consulta da listagem a partir dos filtros preenchidos, ignorando os vazios.
 */
public final class ChamadoSpecs {

    /** Status que representam um chamado ainda em andamento. */
    public static final List<StatusChamado> STATUS_EM_ABERTO = List.of(
            StatusChamado.ABERTO,
            StatusChamado.EM_ANDAMENTO,
            StatusChamado.AGUARDANDO_CLIENTE);

    private ChamadoSpecs() {
    }

    public static Specification<Chamado> comFiltro(ChamadoFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (filtro.getStatus() != null) {
                predicados.add(cb.equal(root.get("status"), filtro.getStatus()));
            }
            if (filtro.isSomenteAbertos()) {
                predicados.add(root.get("status").in(STATUS_EM_ABERTO));
            }
            if (filtro.getPrioridade() != null) {
                predicados.add(cb.equal(root.get("prioridade"), filtro.getPrioridade()));
            }
            if (filtro.getClienteId() != null) {
                predicados.add(cb.equal(root.get("cliente").get("id"), filtro.getClienteId()));
            }
            if (filtro.getTecnicoId() != null) {
                predicados.add(cb.equal(root.get("tecnico").get("id"), filtro.getTecnicoId()));
            }
            if (filtro.isSemTecnico()) {
                predicados.add(cb.isNull(root.get("tecnico")));
            }
            if (filtro.getTermo() != null && !filtro.getTermo().isBlank()) {
                String like = "%" + filtro.getTermo().trim().toLowerCase() + "%";
                predicados.add(cb.or(
                        cb.like(cb.lower(root.get("protocolo")), like),
                        cb.like(cb.lower(root.get("titulo")), like),
                        cb.like(cb.lower(root.get("descricao")), like)));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }

    /** Restringe a visao do solicitante aos chamados da propria empresa. */
    public static Specification<Chamado> doCliente(Long clienteId) {
        return (root, query, cb) -> cb.equal(root.get("cliente").get("id"), clienteId);
    }
}
