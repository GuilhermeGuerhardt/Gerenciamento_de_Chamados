package br.com.chamados.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Andamento do atendimento escrito por uma pessoa (diferente do historico,
 * que registra automaticamente as mudancas do chamado).
 */
@Entity
@Table(name = "comentarios")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Escreva o comentario")
    @Size(max = 3000, message = "O comentario deve ter no maximo 3000 caracteres")
    @Column(nullable = false, length = 3000)
    private String texto;

    /**
     * Nota interna da equipe: fica escondida do solicitante na tela do chamado.
     * Usado para combinar o atendimento sem expor ao cliente.
     */
    @Column(nullable = false)
    private boolean interno = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public boolean isInterno() {
        return interno;
    }

    public void setInterno(boolean interno) {
        this.interno = interno;
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

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
