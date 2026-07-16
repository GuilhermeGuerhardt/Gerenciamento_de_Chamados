package br.com.chamados.repository;

import br.com.chamados.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByChamadoIdOrderByCriadoEmAsc(Long chamadoId);

    /** Visao do solicitante: notas internas da equipe ficam de fora. */
    List<Comentario> findByChamadoIdAndInternoFalseOrderByCriadoEmAsc(Long chamadoId);
}
