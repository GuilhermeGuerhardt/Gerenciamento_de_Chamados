package br.com.chamados.service;

import br.com.chamados.model.Cliente;
import br.com.chamados.model.Contato;
import br.com.chamados.repository.ChamadoRepository;
import br.com.chamados.repository.ClienteRepository;
import br.com.chamados.repository.ContatoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ContatoRepository contatoRepository;
    private final ChamadoRepository chamadoRepository;

    public ClienteService(ClienteRepository clienteRepository,
                          ContatoRepository contatoRepository,
                          ChamadoRepository chamadoRepository) {
        this.clienteRepository = clienteRepository;
        this.contatoRepository = contatoRepository;
        this.chamadoRepository = chamadoRepository;
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar(String termo) {
        if (termo == null || termo.isBlank()) {
            return clienteRepository.findAllByOrderByRazaoSocialAsc();
        }
        return clienteRepository.buscar(termo.trim());
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarAtivos() {
        return clienteRepository.findByAtivoTrueOrderByRazaoSocialAsc();
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Cliente nao encontrado."));
    }

    /**
     * Ao editar, os dados do formulario sao copiados sobre a entidade carregada do banco,
     * em vez de salvar o objeto que veio da tela. O formulario nao traz a lista de contatos,
     * e como ela e mapeada com orphanRemoval, salvar o objeto do formulario direto apagaria
     * todos os contatos do cliente.
     */
    @Transactional
    public Cliente salvar(Cliente cliente) {
        String cnpj = normalizarCnpj(cliente.getCnpj());
        cliente.setCnpj(cnpj);

        if (cnpj != null) {
            boolean duplicado = cliente.getId() == null
                    ? clienteRepository.existsByCnpj(cnpj)
                    : clienteRepository.existsByCnpjAndIdNot(cnpj, cliente.getId());
            if (duplicado) {
                throw new NegocioException("Ja existe um cliente cadastrado com este CNPJ.");
            }
        }

        if (cliente.getId() == null) {
            return clienteRepository.save(cliente);
        }

        Cliente atual = buscarPorId(cliente.getId());
        atual.setRazaoSocial(cliente.getRazaoSocial());
        atual.setNomeFantasia(cliente.getNomeFantasia());
        atual.setCnpj(cliente.getCnpj());
        atual.setTelefone(cliente.getTelefone());
        atual.setEmail(cliente.getEmail());
        atual.setEndereco(cliente.getEndereco());
        atual.setCidade(cliente.getCidade());
        atual.setUf(cliente.getUf());
        atual.setObservacoes(cliente.getObservacoes());
        atual.setAtivo(cliente.isAtivo());

        return clienteRepository.save(atual);
    }

    /**
     * Cliente com chamados nao pode ser apagado, senao o historico perde a referencia.
     * Nesse caso apenas o inativamos, o que o tira das listas de selecao.
     */
    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscarPorId(id);
        if (chamadoRepository.countByClienteId(id) > 0) {
            throw new NegocioException(
                    "Este cliente possui chamados registrados e nao pode ser excluido. Inative-o.");
        }
        clienteRepository.delete(cliente);
    }

    @Transactional
    public void alternarAtivo(Long id) {
        Cliente cliente = buscarPorId(id);
        cliente.setAtivo(!cliente.isAtivo());
        clienteRepository.save(cliente);
    }

    // ---------- Contatos ----------

    @Transactional(readOnly = true)
    public List<Contato> listarContatos(Long clienteId) {
        return contatoRepository.findByClienteIdOrderByPrincipalDescNomeAsc(clienteId);
    }

    @Transactional(readOnly = true)
    public List<Contato> listarContatosAtivos(Long clienteId) {
        return contatoRepository.findByClienteIdAndAtivoTrueOrderByPrincipalDescNomeAsc(clienteId);
    }

    @Transactional(readOnly = true)
    public Contato buscarContato(Long id) {
        return contatoRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Contato nao encontrado."));
    }

    @Transactional
    public Contato salvarContato(Long clienteId, Contato contato) {
        Cliente cliente = buscarPorId(clienteId);
        contato.setCliente(cliente);
        Contato salvo = contatoRepository.save(contato);

        if (salvo.isPrincipal()) {
            contatoRepository.desmarcarOutrosPrincipais(clienteId, salvo.getId());
        }
        return salvo;
    }

    @Transactional
    public void excluirContato(Long id) {
        contatoRepository.delete(buscarContato(id));
    }

    /** Deixa apenas digitos, para que a checagem de duplicidade nao dependa da mascara. */
    private String normalizarCnpj(String cnpj) {
        if (cnpj == null || cnpj.isBlank()) {
            return null;
        }
        String digitos = cnpj.replaceAll("\\D", "");
        return digitos.isBlank() ? null : digitos;
    }
}
