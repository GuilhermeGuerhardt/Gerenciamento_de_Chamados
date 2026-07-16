package br.com.chamados.service;

import br.com.chamados.model.Perfil;
import br.com.chamados.model.Usuario;
import br.com.chamados.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAllByOrderByNomeAsc();
    }

    /**
     * Quem pode ser responsavel por um chamado. Inclui os administradores, e nao apenas
     * o perfil TECNICO, porque eles tambem atendem — sem isso um chamado assumido por um
     * admin ficaria com um responsavel que nem aparece na lista de selecao.
     */
    @Transactional(readOnly = true)
    public List<Usuario> listarAtendentesAtivos() {
        return usuarioRepository.findByPerfilInAndAtivoTrueOrderByNomeAsc(
                List.of(Perfil.ADMIN, Perfil.TECNICO));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Usuario nao encontrado."));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NegocioException("Usuario nao encontrado: " + email));
    }

    /**
     * Cria ou atualiza um usuario. A senha so e regravada quando vem preenchida,
     * assim editar um cadastro sem tocar no campo mantem a senha atual.
     */
    @Transactional
    public Usuario salvar(Usuario usuario, String senhaEmTextoPuro) {
        String email = usuario.getEmail() == null ? null : usuario.getEmail().trim().toLowerCase();
        usuario.setEmail(email);

        boolean emailDuplicado = usuario.getId() == null
                ? usuarioRepository.existsByEmailIgnoreCase(email)
                : usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, usuario.getId());
        if (emailDuplicado) {
            throw new NegocioException("Ja existe um usuario com este e-mail.");
        }

        if (usuario.getPerfil() == Perfil.SOLICITANTE && usuario.getCliente() == null) {
            throw new NegocioException("Selecione o cliente ao qual o solicitante pertence.");
        }
        if (usuario.getPerfil() != Perfil.SOLICITANTE) {
            usuario.setCliente(null);
        }

        if (usuario.getId() == null) {
            if (senhaEmTextoPuro == null || senhaEmTextoPuro.isBlank()) {
                throw new NegocioException("Informe a senha do novo usuario.");
            }
            validarSenha(senhaEmTextoPuro);
            usuario.setSenha(passwordEncoder.encode(senhaEmTextoPuro));
        } else {
            Usuario atual = buscarPorId(usuario.getId());
            if (senhaEmTextoPuro != null && !senhaEmTextoPuro.isBlank()) {
                validarSenha(senhaEmTextoPuro);
                usuario.setSenha(passwordEncoder.encode(senhaEmTextoPuro));
            } else {
                usuario.setSenha(atual.getSenha());
            }
            usuario.setCriadoEm(atual.getCriadoEm());
            usuario.setUltimoAcesso(atual.getUltimoAcesso());
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void alterarSenha(Long usuarioId, String senhaAtual, String novaSenha, String confirmacao) {
        Usuario usuario = buscarPorId(usuarioId);

        if (!passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
            throw new NegocioException("A senha atual esta incorreta.");
        }
        if (!novaSenha.equals(confirmacao)) {
            throw new NegocioException("A confirmacao nao confere com a nova senha.");
        }
        validarSenha(novaSenha);

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void alternarAtivo(Long id, Usuario usuarioLogado) {
        Usuario usuario = buscarPorId(id);
        if (usuario.getId().equals(usuarioLogado.getId())) {
            throw new NegocioException("Voce nao pode inativar o proprio usuario.");
        }
        if (usuario.isAdmin() && usuario.isAtivo() && usuarioRepository.countByPerfilAndAtivoTrue(Perfil.ADMIN) <= 1) {
            throw new NegocioException("O sistema precisa de pelo menos um administrador ativo.");
        }
        usuario.setAtivo(!usuario.isAtivo());
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void registrarAcesso(String email) {
        usuarioRepository.findByEmailIgnoreCase(email).ifPresent(usuario -> {
            usuario.setUltimoAcesso(LocalDateTime.now());
            usuarioRepository.save(usuario);
        });
    }

    private void validarSenha(String senha) {
        if (senha.length() < 6) {
            throw new NegocioException("A senha deve ter pelo menos 6 caracteres.");
        }
    }
}
