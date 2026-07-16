package br.com.digitrix.chamados.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Quem acessa o sistema. O perfil define o que a pessoa pode fazer:
 * ADMIN administra tudo, TECNICO atende chamados e SOLICITANTE so enxerga
 * os chamados do proprio cliente.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome e obrigatorio")
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nome;

    @NotBlank(message = "O e-mail e obrigatorio")
    @Email(message = "E-mail invalido")
    @Size(max = 150)
    @Column(nullable = false, length = 150, unique = true)
    private String email;

    /** Hash BCrypt. Nunca recebe a senha em texto puro vinda do formulario. */
    @Column(nullable = false, length = 100)
    private String senha;

    @NotNull(message = "O perfil e obrigatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfil = Perfil.SOLICITANTE;

    @Size(max = 20)
    @Column(length = 20)
    private String telefone;

    /** Ex.: "Redes", "Servidores". So faz sentido para TECNICO. */
    @Size(max = 80)
    @Column(length = 80)
    private String especialidade;

    /**
     * Empresa do usuario. Obrigatorio para SOLICITANTE (define quais chamados ele ve)
     * e nulo para a equipe interna.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    private LocalDateTime ultimoAcesso;

    @Transient
    public boolean isAdmin() {
        return perfil == Perfil.ADMIN;
    }

    @Transient
    public boolean isTecnico() {
        return perfil == Perfil.TECNICO;
    }

    /** Admin e tecnico enxergam todos os chamados; solicitante so os da sua empresa. */
    @Transient
    public boolean isEquipeInterna() {
        return perfil == Perfil.ADMIN || perfil == Perfil.TECNICO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getUltimoAcesso() {
        return ultimoAcesso;
    }

    public void setUltimoAcesso(LocalDateTime ultimoAcesso) {
        this.ultimoAcesso = ultimoAcesso;
    }
}
