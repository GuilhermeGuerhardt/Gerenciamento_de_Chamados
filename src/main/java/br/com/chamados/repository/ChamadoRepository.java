package br.com.chamados.repository;

import br.com.chamados.model.Chamado;
import br.com.chamados.model.StatusChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChamadoRepository extends JpaRepository<Chamado, Long>, JpaSpecificationExecutor<Chamado> {

    Optional<Chamado> findByProtocolo(String protocolo);

    List<Chamado> findTop10ByOrderByAbertoEmDesc();

    List<Chamado> findTop10ByTecnicoIdAndStatusInOrderByAbertoEmDesc(Long tecnicoId, Collection<StatusChamado> status);

    List<Chamado> findTop10ByClienteIdOrderByAbertoEmDesc(Long clienteId);

    long countByStatus(StatusChamado status);

    long countByStatusIn(Collection<StatusChamado> status);

    long countByTecnicoIdAndStatusIn(Long tecnicoId, Collection<StatusChamado> status);

    long countByClienteIdAndStatusIn(Long clienteId, Collection<StatusChamado> status);

    long countByTecnicoIsNullAndStatus(StatusChamado status);

    long countByClienteId(Long clienteId);

    /**
     * Maior protocolo ja emitido no ano, usado para montar o proximo.
     * Como o sequencial e zero-padded (AAAA-NNNN), a ordem alfabetica coincide
     * com a numerica e o MAX de texto resolve sem precisar de CAST no banco.
     */
    @Query("SELECT MAX(c.protocolo) FROM Chamado c WHERE c.protocolo LIKE CONCAT(:ano, '-%')")
    String ultimoProtocoloDoAno(@Param("ano") String ano);

    @Query("SELECT COUNT(c) FROM Chamado c WHERE c.abertoEm >= :inicio")
    long contarAbertosDesde(@Param("inicio") LocalDateTime inicio);

    /** Contagem por status para os cards do dashboard, em uma unica consulta. */
    @Query("SELECT c.status, COUNT(c) FROM Chamado c GROUP BY c.status")
    List<Object[]> contarPorStatus();
}
