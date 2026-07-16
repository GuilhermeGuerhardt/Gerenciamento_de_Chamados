package br.com.digitrix.chamados.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Funcionario do cliente: quem solicita ou acompanha o chamado do lado da empresa.
 */
@Entity
@Table(name = "contatos")
public class Contato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do contato e obrigatorio")
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nome;

    @Size(max = 80)
    @Column(length = 80)
    private String cargo;

    @Size(max = 80)
    @Column(length = 80)
    private String setor;

    @Size(max = 20)
    @Column(length = 20)
    private String telefone;

    @Size(max = 20)
    @Column(length = 20)
    private String celular;

    @Size(max = 20)
    @Column(length = 20)
    private String ramal;

    @Email(message = "E-mail invalido")
    @Size(max = 150)
    @Column(length = 150)
    private String email;

    @Size(max = 500)
    @Column(length = 500)
    private String observacoes;

    /** Contato principal aparece primeiro e e o padrao ao abrir um chamado. */
    @Column(nullable = false)
    private boolean principal = false;

    @Column(nullable = false)
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /** Melhor telefone disponivel, na ordem em que a equipe costuma tentar. */
    @Transient
    public String getTelefonePrincipal() {
        if (celular != null && !celular.isBlank()) {
            return celular;
        }
        if (telefone != null && !telefone.isBlank()) {
            return telefone;
        }
        return null;
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

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getRamal() {
        return ramal;
    }

    public void setRamal(String ramal) {
        this.ramal = ramal;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public boolean isPrincipal() {
        return principal;
    }

    public void setPrincipal(boolean principal) {
        this.principal = principal;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
}
