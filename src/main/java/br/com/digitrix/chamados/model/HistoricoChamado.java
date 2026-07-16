package br.com.digitrix.chamados.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Trilha de auditoria do chamado: quem mudou o que e quando.
 * Gerado pelo ChamadoService, nunca preenchido direto por formulario.
 */
@Entity
@Table(name = "historico_chamados")
public class HistoricoChamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    /** Frase pronta para exibir, ex.: "Status alterado de Aberto para Em andamento". */
    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(length = 100)
    private String campo;

    @Column(length = 150)
    private String valorAnterior;

    @Column(length = 150)
    private String valorNovo;

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public HistoricoChamado() {
    }

    public HistoricoChamado(Chamado chamado, Usuario autor, String descricao) {
        this.chamado = chamado;
        this.autor = autor;
        this.descricao = descricao;
    }

    public HistoricoChamado(Chamado chamado, Usuario autor, String descricao,
                            String campo, String valorAnterior, String valorNovo) {
        this(chamado, autor, descricao);
        this.campo = campo;
        this.valorAnterior = valorAnterior;
        this.valorNovo = valorNovo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Chamado getChamado() {
        return chamado;
    }

    public void setChamado(Chamado chamado) {
        this.chamado = chamado;
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getValorAnterior() {
        return valorAnterior;
    }

    public void setValorAnterior(String valorAnterior) {
        this.valorAnterior = valorAnterior;
    }

    public String getValorNovo() {
        return valorNovo;
    }

    public void setValorNovo(String valorNovo) {
        this.valorNovo = valorNovo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
