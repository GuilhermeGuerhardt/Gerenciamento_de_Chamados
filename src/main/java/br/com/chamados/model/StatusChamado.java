package br.com.chamados.model;

public enum StatusChamado {

    ABERTO("Aberto", "status-aberto"),
    EM_ANDAMENTO("Em andamento", "status-andamento"),
    AGUARDANDO_CLIENTE("Aguardando cliente", "status-aguardando"),
    RESOLVIDO("Resolvido", "status-resolvido"),
    FECHADO("Fechado", "status-fechado"),
    CANCELADO("Cancelado", "status-cancelado");

    private final String descricao;
    private final String cssClass;

    StatusChamado(String descricao, String cssClass) {
        this.descricao = descricao;
        this.cssClass = cssClass;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCssClass() {
        return cssClass;
    }

    /** Chamado encerrado nao aceita mais alteracao de atendimento. */
    public boolean isEncerrado() {
        return this == FECHADO || this == CANCELADO;
    }
}
