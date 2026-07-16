package br.com.chamados.repository;

import br.com.chamados.model.Perfil;
import br.com.chamados.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    List<Usuario> findAllByOrderByNomeAsc();

    List<Usuario> findByPerfilAndAtivoTrueOrderByNomeAsc(Perfil perfil);

    List<Usuario> findByPerfilInAndAtivoTrueOrderByNomeAsc(Collection<Perfil> perfis);

    List<Usuario> findByClienteIdOrderByNomeAsc(Long clienteId);

    long countByPerfilAndAtivoTrue(Perfil perfil);

    long countByAtivoTrue();
}
