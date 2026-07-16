package br.com.chamados.repository;

import br.com.chamados.model.Contato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContatoRepository extends JpaRepository<Contato, Long> {

    List<Contato> findByClienteIdOrderByPrincipalDescNomeAsc(Long clienteId);

    List<Contato> findByClienteIdAndAtivoTrueOrderByPrincipalDescNomeAsc(Long clienteId);

    /** Garante um unico contato principal por cliente ao marcar um novo. */
    @Modifying
    @Query("UPDATE Contato c SET c.principal = false WHERE c.cliente.id = :clienteId AND c.id <> :contatoId")
    void desmarcarOutrosPrincipais(@Param("clienteId") Long clienteId, @Param("contatoId") Long contatoId);

    long countByClienteId(Long clienteId);
}
