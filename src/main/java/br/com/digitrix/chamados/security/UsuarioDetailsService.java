package br.com.digitrix.chamados.security;

import br.com.digitrix.chamados.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .map(usuario -> {
                    // Este Usuario fica guardado na sessao HTTP e sera lido em requisicoes
                    // futuras, ja desconectado do JPA. Carregamos o cliente agora, senao o
                    // proxy lazy estoura ao ser acessado depois.
                    if (usuario.getCliente() != null) {
                        usuario.getCliente().getRazaoSocial();
                    }
                    return new UsuarioAutenticado(usuario);
                })
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado: " + email));
    }
}
