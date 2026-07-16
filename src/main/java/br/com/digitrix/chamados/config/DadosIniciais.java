package br.com.digitrix.chamados.config;

import br.com.digitrix.chamados.model.*;
import br.com.digitrix.chamados.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria o administrador no primeiro start e, opcionalmente, dados de exemplo
 * para que o sistema abra com algo na tela. Nada e recriado se ja existir.
 */
@Component
public class DadosIniciais implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosIniciais.class);

    /** Igual ao default de app.admin.senha no application.properties. */
    private static final String SENHA_PADRAO = "admin123";

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ChamadoRepository chamadoRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.senha}")
    private String adminSenha;

    @Value("${app.admin.nome}")
    private String adminNome;

    @Value("${app.dados-exemplo:true}")
    private boolean criarDadosExemplo;

    public DadosIniciais(UsuarioRepository usuarioRepository,
                         ClienteRepository clienteRepository,
                         ChamadoRepository chamadoRepository,
                         PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.chamadoRepository = chamadoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        criarAdmin();

        if (criarDadosExemplo && clienteRepository.count() == 0) {
            criarExemplos();
        }
    }

    private void criarAdmin() {
        if (usuarioRepository.existsByEmailIgnoreCase(adminEmail)) {
            return;
        }

        Usuario admin = new Usuario();
        admin.setNome(adminNome);
        admin.setEmail(adminEmail.toLowerCase());
        admin.setSenha(passwordEncoder.encode(adminSenha));
        admin.setPerfil(Perfil.ADMIN);
        usuarioRepository.save(admin);

        log.warn("=================================================================");
        if (SENHA_PADRAO.equals(adminSenha)) {
            // So imprimimos a senha quando ela e a padrao publica, para o primeiro
            // acesso local. Uma senha vinda de ADMIN_SENHA nunca vai para o log.
            log.warn(" Administrador criado -> login: {} / senha: {}", adminEmail, adminSenha);
            log.warn(" Troque essa senha no primeiro acesso (menu Meu perfil).");
        } else {
            log.warn(" Administrador criado -> login: {}", adminEmail);
            log.warn(" A senha veio da variavel de ambiente ADMIN_SENHA.");
        }
        log.warn("=================================================================");
    }

    /**
     * Cria apenas um cliente de demonstracao com contatos e um chamado, para o
     * sistema nao abrir com as telas vazias.
     *
     * Tecnicos NAO sao criados aqui: eles sao cadastrados pelo administrador em
     * "Usuarios e tecnicos", que e onde a senha de cada um e definida. Um tecnico
     * gerado automaticamente teria senha conhecida e viraria uma porta aberta.
     */
    private void criarExemplos() {
        Cliente cliente = new Cliente();
        cliente.setRazaoSocial("Empresa Exemplo Ltda");
        cliente.setNomeFantasia("Empresa Exemplo");
        cliente.setCnpj("12345678000199");
        cliente.setTelefone("(11) 4000-0000");
        cliente.setEmail("contato@exemplo.com.br");
        cliente.setCidade("Sao Paulo");
        cliente.setUf("SP");

        Contato contato = new Contato();
        contato.setNome("Maria Souza");
        contato.setCargo("Gerente Administrativo");
        contato.setSetor("Administrativo");
        contato.setCelular("(11) 99999-0000");
        contato.setEmail("maria@exemplo.com.br");
        contato.setPrincipal(true);
        cliente.adicionarContato(contato);

        Contato contato2 = new Contato();
        contato2.setNome("Joao Lima");
        contato2.setCargo("Analista de TI");
        contato2.setRamal("204");
        contato2.setEmail("joao@exemplo.com.br");
        cliente.adicionarContato(contato2);

        clienteRepository.save(cliente);

        Chamado chamado = new Chamado();
        chamado.setProtocolo(String.format("%s-0001", java.time.Year.now().getValue()));
        chamado.setTitulo("Impressora do setor financeiro nao imprime");
        chamado.setDescricao("A impressora aparece offline nas maquinas do setor. "
                + "Ja foi reiniciada, mas o problema continua.");
        chamado.setPrioridade(Prioridade.ALTA);
        chamado.setCategoria("Hardware");
        chamado.setCliente(cliente);
        chamado.setContato(contato);
        chamadoRepository.save(chamado);

        log.info("Dados de exemplo criados (cliente, contatos e 1 chamado).");
        log.info("Cadastre os tecnicos em 'Usuarios e tecnicos', entrando como administrador.");
        log.info("Para nao criar exemplos, defina app.dados-exemplo=false no application.properties");
    }
}
