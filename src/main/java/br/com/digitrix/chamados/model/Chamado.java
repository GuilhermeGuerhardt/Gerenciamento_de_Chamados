package br.com.digitrix.chamados.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chamados", indexes = {
        @Index(name = "idx_chamado_status", columnList = "status"),
        @Index(name = "idx_chamado_cliente", columnList = "cliente_id"),
        @Index(name = "idx_chamado_tecnico", columnList = "tecnico_id")
})
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Protocolo visivel ao cliente, no formato AAAA-NNNN. Gerado no ChamadoService. */
    @Column(nullable = false, length = 20, unique = true)
    private String protocolo;

    @NotBlank(message = "O titulo e obrigatorio")
    @Size(max = 150, message = "O titulo deve ter no maximo 150 caracteres")
    @Column(nullable = false, length = 150)
    private String titulo;

    @NotBlank(message = "A descricao e obrigatoria")
    @Size(max = 5000)
    @Column(nullable = false, length = 5000)
    private String descricao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusChamado status = StatusChamado.ABERTO;

    @NotNull(message = "A prioridade e obrigatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Prioridade prioridade = Prioridade.MEDIA;

    @Size(max = 80)
    @Column(length = 80)
    private String categoria;

    @NotNull(message = "Selecione o cliente")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /** Funcionario do cliente que pediu o atendimento. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contato_id")
    private Contato contato;

    /** Usuario do sistema que registrou o chamado. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id")
    private Usuario solicitante;

    /** Tecnico responsavel. Nulo enquanto o chamado esta na fila. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id")
    private Usuario tecnico;

    @Size(max = 5000)
    @Column(length = 5000)
    private String solucao;

    @Column(nullable = false)
    private LocalDateTime abertoEm = LocalDateTime.now();

    private LocalDateTime atualizadoEm;

    private LocalDateTime fechadoEm;

    @OneToMany(mappedBy = "chamado", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("criadoEm ASC")
    private List<Comentario> comentarios = new ArrayList<>();

    @OneToMany(mappedBy = "chamado", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("criadoEm DESC")
    private List<HistoricoChamado> historico = new ArrayList<>();

    /**
     * Chamado em aberto que ja passou do prazo alvo da sua prioridade.
     * Serve para destacar a linha na listagem.
     */
    @Transient
    public boolean isAtrasado() {
        if (status.isEncerrado() || status == StatusChamado.RESOLVIDO) {
            return false;
        }
        return Duration.between(abertoEm, LocalDateTime.now()).toHours() > prioridade.getPrazoHoras();
    }

    @Transient
    public boolean isAtribuido() {
        return tecnico != null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public StatusChamado getStatus() {
        return status;
    }

    public void setStatus(StatusChamado status) {
        this.status = status;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Contato getContato() {
        return contato;
    }

    public void setContato(Contato contato) {
        this.contato = contato;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Usuario solicitante) {
        this.solicitante = solicitante;
    }

    public Usuario getTecnico() {
        return tecnico;
    }

    public void setTecnico(Usuario tecnico) {
        this.tecnico = tecnico;
    }

    public String getSolucao() {
        return solucao;
    }

    public void setSolucao(String solucao) {
        this.solucao = solucao;
    }

    public LocalDateTime getAbertoEm() {
        return abertoEm;
    }

    public void setAbertoEm(LocalDateTime abertoEm) {
        this.abertoEm = abertoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public LocalDateTime getFechadoEm() {
        return fechadoEm;
    }

    public void setFechadoEm(LocalDateTime fechadoEm) {
        this.fechadoEm = fechadoEm;
    }

    public List<Comentario> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<Comentario> comentarios) {
        this.comentarios = comentarios;
    }

    public List<HistoricoChamado> getHistorico() {
        return historico;
    }

    public void setHistorico(List<HistoricoChamado> historico) {
        this.historico = historico;
    }
}
