package br.com.digitrix.chamados.config;

import br.com.digitrix.chamados.model.Cliente;
import br.com.digitrix.chamados.model.Contato;
import br.com.digitrix.chamados.model.Usuario;
import br.com.digitrix.chamados.repository.ClienteRepository;
import br.com.digitrix.chamados.repository.ContatoRepository;
import br.com.digitrix.chamados.repository.UsuarioRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Conversao entre entidade e id nos formularios, nos dois sentidos:
 *
 * - String -> Entidade: o <select> envia so o id e o controller recebe o objeto.
 * - Entidade -> String: o th:field imprime o id no HTML, para que o Thymeleaf
 *   marque a opcao correta ao editar. Sem isso ele cairia no toString() padrao.
 *
 * Sao classes nomeadas (e nao lambdas) porque o Spring precisa ler os tipos
 * genericos em tempo de execucao para registrar cada converter.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ClienteRepository clienteRepository;
    private final ContatoRepository contatoRepository;
    private final UsuarioRepository usuarioRepository;

    public WebConfig(ClienteRepository clienteRepository,
                     ContatoRepository contatoRepository,
                     UsuarioRepository usuarioRepository) {
        this.clienteRepository = clienteRepository;
        this.contatoRepository = contatoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void addFormatters(@NonNull FormatterRegistry registry) {
        registry.addConverter(new ClienteConverter());
        registry.addConverter(new ContatoConverter());
        registry.addConverter(new UsuarioConverter());

        registry.addConverter(new ClienteParaId());
        registry.addConverter(new ContatoParaId());
        registry.addConverter(new UsuarioParaId());
    }

    /** Campo vazio no formulario vira null (ex.: chamado ainda sem tecnico). */
    @Nullable
    private static Long paraId(String origem) {
        if (origem == null || origem.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(origem.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private class ClienteConverter implements Converter<String, Cliente> {
        @Override
        @Nullable
        public Cliente convert(@NonNull String origem) {
            Long id = paraId(origem);
            return id == null ? null : clienteRepository.findById(id).orElse(null);
        }
    }

    private class ContatoConverter implements Converter<String, Contato> {
        @Override
        @Nullable
        public Contato convert(@NonNull String origem) {
            Long id = paraId(origem);
            return id == null ? null : contatoRepository.findById(id).orElse(null);
        }
    }

    private class UsuarioConverter implements Converter<String, Usuario> {
        @Override
        @Nullable
        public Usuario convert(@NonNull String origem) {
            Long id = paraId(origem);
            return id == null ? null : usuarioRepository.findById(id).orElse(null);
        }
    }

    private static class ClienteParaId implements Converter<Cliente, String> {
        @Override
        public String convert(@NonNull Cliente cliente) {
            return cliente.getId() == null ? "" : String.valueOf(cliente.getId());
        }
    }

    private static class ContatoParaId implements Converter<Contato, String> {
        @Override
        public String convert(@NonNull Contato contato) {
            return contato.getId() == null ? "" : String.valueOf(contato.getId());
        }
    }

    private static class UsuarioParaId implements Converter<Usuario, String> {
        @Override
        public String convert(@NonNull Usuario usuario) {
            return usuario.getId() == null ? "" : String.valueOf(usuario.getId());
        }
    }
}
