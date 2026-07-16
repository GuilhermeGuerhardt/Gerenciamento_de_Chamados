package br.com.chamados.model;

/**
 * Perfis de acesso. O nome da role no Spring Security e "ROLE_" + name().
 */
public enum Perfil {

    /** Acesso total: cadastra clientes, usuarios, tecnicos e mexe em qualquer chamado. */
    ADMIN("Administrador"),

    /** Atende chamados: ve todos, assume, comenta e muda status. Nao cadastra usuarios. */
    TECNICO("Tecnico"),

    /** Usuario do cliente: abre chamados e acompanha apenas os da propria empresa. */
    SOLICITANTE("Solicitante");

    private final String descricao;

    Perfil(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getRole() {
        return "ROLE_" + name();
    }
}
