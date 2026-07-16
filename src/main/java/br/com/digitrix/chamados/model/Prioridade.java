package br.com.digitrix.chamados.model;

public enum Prioridade {

    BAIXA("Baixa", "prio-baixa", 72),
    MEDIA("Media", "prio-media", 24),
    ALTA("Alta", "prio-alta", 8),
    URGENTE("Urgente", "prio-urgente", 2);

    private final String descricao;
    private final String cssClass;

    /** Prazo alvo de atendimento em horas, usado para sinalizar chamados atrasados. */
    private final int prazoHoras;

    Prioridade(String descricao, String cssClass, int prazoHoras) {
        this.descricao = descricao;
        this.cssClass = cssClass;
        this.prazoHoras = prazoHoras;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCssClass() {
        return cssClass;
    }

    public int getPrazoHoras() {
        return prazoHoras;
    }
}
