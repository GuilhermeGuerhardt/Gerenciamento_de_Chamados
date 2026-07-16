package br.com.digitrix.chamados.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Empresa atendida. Cada chamado pertence a um cliente, e os contatos sao os
 * funcionarios dessa empresa com quem o tecnico fala.
 */
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A razao social e obrigatoria")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String razaoSocial;

    @Size(max = 150)
    @Column(length = 150)
    private String nomeFantasia;

    @Size(max = 20)
    @Column(length = 20, unique = true)
    private String cnpj;

    @Size(max = 20)
    @Column(length = 20)
    private String telefone;

    @Email(message = "E-mail invalido")
    @Size(max = 150)
    @Column(length = 150)
    private String email;

    @Size(max = 255)
    @Column(length = 255)
    private String endereco;

    @Size(max = 100)
    @Column(length = 100)
    private String cidade;

    @Size(max = 2)
    @Column(length = 2)
    private String uf;

    @Size(max = 1000)
    @Column(length = 1000)
    private String observacoes;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("principal DESC, nome ASC")
    private List<Contato> contatos = new ArrayList<>();

    public void adicionarContato(Contato contato) {
        contato.setCliente(this);
        contatos.add(contato);
    }

    /** Nome mais curto para exibir em listas e no cabecalho do chamado. */
    @Transient
    public String getNomeExibicao() {
        return (nomeFantasia != null && !nomeFantasia.isBlank()) ? nomeFantasia : razaoSocial;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
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

    public List<Contato> getContatos() {
        return contatos;
    }

    public void setContatos(List<Contato> contatos) {
        this.contatos = contatos;
    }
}
