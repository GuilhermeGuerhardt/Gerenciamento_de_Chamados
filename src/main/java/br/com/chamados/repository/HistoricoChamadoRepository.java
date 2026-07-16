package br.com.chamados.repository;

import br.com.chamados.model.HistoricoChamado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoChamadoRepository extends JpaRepository<HistoricoChamado, Long> {

    List<HistoricoChamado> findByChamadoIdOrderByCriadoEmDesc(Long chamadoId);
}
