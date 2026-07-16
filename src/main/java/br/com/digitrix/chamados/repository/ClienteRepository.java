package br.com.digitrix.chamados.repository;

import br.com.digitrix.chamados.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByAtivoTrueOrderByRazaoSocialAsc();

    List<Cliente> findAllByOrderByRazaoSocialAsc();

    Optional<Cliente> findByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(String cnpj, Long id);

    boolean existsByCnpj(String cnpj);

    @Query("""
            SELECT c FROM Cliente c
            WHERE LOWER(c.razaoSocial) LIKE LOWER(CONCAT('%', :termo, '%'))
               OR LOWER(COALESCE(c.nomeFantasia, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
               OR COALESCE(c.cnpj, '') LIKE CONCAT('%', :termo, '%')
            ORDER BY c.razaoSocial ASC
            """)
    List<Cliente> buscar(@Param("termo") String termo);

    long countByAtivoTrue();
}
