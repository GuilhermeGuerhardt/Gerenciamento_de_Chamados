package br.com.digitrix.chamados.service;

import br.com.digitrix.chamados.model.*;
import br.com.digitrix.chamados.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final ComentarioRepository comentarioRepository;
    private final HistoricoChamadoRepository historicoRepository;

    public ChamadoService(ChamadoRepository chamadoRepository,
                          ComentarioRepository comentarioRepository,
                          HistoricoChamadoRepository historicoRepository) {
        this.chamadoRepository = chamadoRepository;
        this.comentarioRepository = comentarioRepository;
        this.historicoRepository = historicoRepository;
    }

    /**
     * Listagem ja respeitando o perfil: solicitante enxerga apenas a propria empresa.
     */
    @Transactional(readOnly = true)
    public List<Chamado> listar(ChamadoFiltro filtro, Usuario usuarioLogado) {
        Specification<Chamado> spec = ChamadoSpecs.comFiltro(filtro);

        if (!usuarioLogado.isEquipeInterna()) {
            Long clienteId = usuarioLogado.getCliente() != null ? usuarioLogado.getCliente().getId() : -1L;
            spec = spec.and(ChamadoSpecs.doCliente(clienteId));
        }

        Sort ordem = Sort.by(Sort.Direction.DESC, "abertoEm");
        return chamadoRepository.findAll(spec, ordem);
    }

    @Transactional(readOnly = true)
    public Chamado buscarPorId(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Chamado nao encontrado."));
    }

    /**
     * Carrega o chamado garantindo que o usuario tem direito de ve-lo.
     * Toda tela de detalhe/edicao passa por aqui.
     */
    @Transactional(readOnly = true)
    public Chamado buscarComPermissao(Long id, Usuario usuarioLogado) {
        Chamado chamado = buscarPorId(id);
        if (!podeVisualizar(chamado, usuarioLogado)) {
            throw new AcessoNegadoException("Voce nao tem permissao para acessar este chamado.");
        }
        return chamado;
    }

    public boolean podeVisualizar(Chamado chamado, Usuario usuario) {
        if (usuario.isEquipeInterna()) {
            return true;
        }
        return usuario.getCliente() != null
                && chamado.getCliente() != null
                && usuario.getCliente().getId().equals(chamado.getCliente().getId());
    }

    /** Solicitante nao mexe no atendimento: quem edita e a equipe interna. */
    public boolean podeEditar(Chamado chamado, Usuario usuario) {
        return usuario.isEquipeInterna();
    }

    @Transactional
    public Chamado abrir(Chamado chamado, Usuario autor) {
        chamado.setProtocolo(gerarProtocolo());
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setAbertoEm(LocalDateTime.now());
        chamado.setAtualizadoEm(LocalDateTime.now());
        chamado.setSolicitante(autor);

        // Solicitante so abre chamado para a propria empresa, independente do que vier no formulario.
        if (!autor.isEquipeInterna()) {
            if (autor.getCliente() == null) {
                throw new NegocioException("Seu usuario nao esta vinculado a nenhum cliente.");
            }
            chamado.setCliente(autor.getCliente());
            chamado.setTecnico(null);
        }

        Chamado salvo = chamadoRepository.save(chamado);
        registrar(salvo, autor, "Chamado aberto por " + autor.getNome());

        if (salvo.getTecnico() != null) {
            registrar(salvo, autor, "Atribuido a " + salvo.getTecnico().getNome());
        }
        return salvo;
    }

    /**
     * Atualiza os campos editaveis e registra no historico apenas o que realmente mudou.
     */
    @Transactional
    public Chamado atualizar(Long id, Chamado dados, Usuario autor) {
        Chamado atual = buscarComPermissao(id, autor);

        if (!podeEditar(atual, autor)) {
            throw new AcessoNegadoException("Voce nao tem permissao para editar este chamado.");
        }
        if (atual.getStatus().isEncerrado()) {
            throw new NegocioException("Chamado " + atual.getStatus().getDescricao().toLowerCase()
                    + " nao pode ser alterado. Reabra-o antes de editar.");
        }

        if (!atual.getTitulo().equals(dados.getTitulo())) {
            registrar(atual, autor, "Titulo alterado", "titulo", atual.getTitulo(), dados.getTitulo());
            atual.setTitulo(dados.getTitulo());
        }
        if (!atual.getDescricao().equals(dados.getDescricao())) {
            registrar(atual, autor, "Descricao alterada");
            atual.setDescricao(dados.getDescricao());
        }
        if (atual.getPrioridade() != dados.getPrioridade()) {
            registrar(atual, autor,
                    "Prioridade alterada de " + atual.getPrioridade().getDescricao()
                            + " para " + dados.getPrioridade().getDescricao(),
                    "prioridade", atual.getPrioridade().getDescricao(), dados.getPrioridade().getDescricao());
            atual.setPrioridade(dados.getPrioridade());
        }
        if (!java.util.Objects.equals(atual.getCategoria(), dados.getCategoria())) {
            atual.setCategoria(dados.getCategoria());
        }
        if (dados.getCliente() != null && !dados.getCliente().getId().equals(atual.getCliente().getId())) {
            registrar(atual, autor, "Cliente alterado para " + dados.getCliente().getNomeExibicao(),
                    "cliente", atual.getCliente().getNomeExibicao(), dados.getCliente().getNomeExibicao());
            atual.setCliente(dados.getCliente());
            atual.setContato(dados.getContato());
        } else {
            atual.setContato(dados.getContato());
        }

        atual.setAtualizadoEm(LocalDateTime.now());
        return chamadoRepository.save(atual);
    }

    @Transactional
    public void atribuir(Long chamadoId, Usuario tecnico, Usuario autor) {
        Chamado chamado = buscarComPermissao(chamadoId, autor);

        if (!autor.isEquipeInterna()) {
            throw new AcessoNegadoException("Apenas a equipe interna atribui chamados.");
        }
        if (chamado.getStatus().isEncerrado()) {
            throw new NegocioException("Nao e possivel atribuir um chamado encerrado.");
        }
        if (tecnico != null && tecnico.getPerfil() == Perfil.SOLICITANTE) {
            throw new NegocioException("Apenas tecnicos e administradores podem receber chamados.");
        }

        String anterior = chamado.getTecnico() != null ? chamado.getTecnico().getNome() : "ninguem";
        String novo = tecnico != null ? tecnico.getNome() : "ninguem";

        if (anterior.equals(novo)) {
            return;
        }

        chamado.setTecnico(tecnico);
        chamado.setAtualizadoEm(LocalDateTime.now());

        // Assumir um chamado que estava na fila ja o coloca em atendimento.
        if (tecnico != null && chamado.getStatus() == StatusChamado.ABERTO) {
            chamado.setStatus(StatusChamado.EM_ANDAMENTO);
            registrar(chamado, autor, "Status alterado de Aberto para Em andamento",
                    "status", "Aberto", "Em andamento");
        }

        registrar(chamado, autor, "Responsavel alterado de " + anterior + " para " + novo,
                "tecnico", anterior, novo);
        chamadoRepository.save(chamado);
    }

    @Transactional
    public void alterarStatus(Long chamadoId, StatusChamado novoStatus, String solucao, Usuario autor) {
        Chamado chamado = buscarComPermissao(chamadoId, autor);

        if (!autor.isEquipeInterna()) {
            throw new AcessoNegadoException("Apenas a equipe interna altera o status do chamado.");
        }
        if (chamado.getStatus() == novoStatus) {
            return;
        }

        StatusChamado anterior = chamado.getStatus();

        if ((novoStatus == StatusChamado.RESOLVIDO || novoStatus == StatusChamado.FECHADO)
                && (solucao == null || solucao.isBlank())
                && (chamado.getSolucao() == null || chamado.getSolucao().isBlank())) {
            throw new NegocioException("Descreva a solucao antes de marcar o chamado como "
                    + novoStatus.getDescricao().toLowerCase() + ".");
        }

        if (solucao != null && !solucao.isBlank()) {
            chamado.setSolucao(solucao);
        }

        chamado.setStatus(novoStatus);
        chamado.setAtualizadoEm(LocalDateTime.now());
        chamado.setFechadoEm(novoStatus.isEncerrado() ? LocalDateTime.now() : null);

        registrar(chamado, autor,
                "Status alterado de " + anterior.getDescricao() + " para " + novoStatus.getDescricao(),
                "status", anterior.getDescricao(), novoStatus.getDescricao());
        chamadoRepository.save(chamado);
    }

    @Transactional
    public void reabrir(Long chamadoId, Usuario autor) {
        Chamado chamado = buscarComPermissao(chamadoId, autor);

        if (!autor.isEquipeInterna()) {
            throw new AcessoNegadoException("Apenas a equipe interna reabre chamados.");
        }
        if (!chamado.getStatus().isEncerrado() && chamado.getStatus() != StatusChamado.RESOLVIDO) {
            throw new NegocioException("Este chamado ja esta em aberto.");
        }

        StatusChamado anterior = chamado.getStatus();
        chamado.setStatus(StatusChamado.EM_ANDAMENTO);
        chamado.setFechadoEm(null);
        chamado.setAtualizadoEm(LocalDateTime.now());

        registrar(chamado, autor, "Chamado reaberto (estava " + anterior.getDescricao().toLowerCase() + ")",
                "status", anterior.getDescricao(), StatusChamado.EM_ANDAMENTO.getDescricao());
        chamadoRepository.save(chamado);
    }

    @Transactional
    public void excluir(Long chamadoId, Usuario autor) {
        if (!autor.isAdmin()) {
            throw new AcessoNegadoException("Apenas administradores excluem chamados.");
        }
        chamadoRepository.delete(buscarPorId(chamadoId));
    }

    // ---------- Comentarios ----------

    @Transactional(readOnly = true)
    public List<Comentario> listarComentarios(Long chamadoId, Usuario usuarioLogado) {
        if (usuarioLogado.isEquipeInterna()) {
            return comentarioRepository.findByChamadoIdOrderByCriadoEmAsc(chamadoId);
        }
        return comentarioRepository.findByChamadoIdAndInternoFalseOrderByCriadoEmAsc(chamadoId);
    }

    @Transactional
    public Comentario comentar(Long chamadoId, String texto, boolean interno, Usuario autor) {
        Chamado chamado = buscarComPermissao(chamadoId, autor);

        if (texto == null || texto.isBlank()) {
            throw new NegocioException("Escreva o comentario antes de enviar.");
        }
        if (chamado.getStatus().isEncerrado()) {
            throw new NegocioException("Chamado encerrado nao aceita novos comentarios. Reabra-o primeiro.");
        }

        Comentario comentario = new Comentario();
        comentario.setChamado(chamado);
        comentario.setAutor(autor);
        comentario.setTexto(texto.trim());
        // Nota interna e um recurso da equipe; do solicitante nunca vem marcada.
        comentario.setInterno(interno && autor.isEquipeInterna());

        Comentario salvo = comentarioRepository.save(comentario);

        chamado.setAtualizadoEm(LocalDateTime.now());
        chamadoRepository.save(chamado);

        return salvo;
    }

    @Transactional(readOnly = true)
    public List<HistoricoChamado> listarHistorico(Long chamadoId) {
        return historicoRepository.findByChamadoIdOrderByCriadoEmDesc(chamadoId);
    }

    // ---------- Apoio ----------

    private void registrar(Chamado chamado, Usuario autor, String descricao) {
        historicoRepository.save(new HistoricoChamado(chamado, autor, descricao));
    }

    private void registrar(Chamado chamado, Usuario autor, String descricao,
                           String campo, String anterior, String novo) {
        historicoRepository.save(new HistoricoChamado(chamado, autor, descricao, campo, anterior, novo));
    }

    /**
     * Protocolo no formato AAAA-NNNN, com a sequencia reiniciando a cada ano.
     */
    private String gerarProtocolo() {
        String ano = String.valueOf(Year.now().getValue());
        String ultimo = chamadoRepository.ultimoProtocoloDoAno(ano);

        int proximo = 1;
        if (ultimo != null) {
            try {
                proximo = Integer.parseInt(ultimo.substring(ano.length() + 1)) + 1;
            } catch (NumberFormatException e) {
                // Protocolo fora do padrao (importado a mao, por exemplo): recomeca a contagem
                // pela quantidade de chamados do ano, evitando colidir com o que ja existe.
                proximo = (int) chamadoRepository.contarAbertosDesde(
                        java.time.LocalDate.of(Year.now().getValue(), 1, 1).atStartOfDay()) + 1;
            }
        }
        return String.format("%s-%04d", ano, proximo);
    }
}
