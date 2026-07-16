package br.com.chamados.service;

import br.com.chamados.model.Prioridade;
import br.com.chamados.model.StatusChamado;

/**
 * Filtros da tela de listagem. Todos os campos sao opcionais: nulo significa "nao filtrar".
 */
public class ChamadoFiltro {

    private StatusChamado status;
    private Prioridade prioridade;
    private Long clienteId;
    private Long tecnicoId;

    /** Busca livre por protocolo, titulo ou descricao. */
    private String termo;

    /** Atalho para "chamados ainda sem tecnico responsavel". */
    private boolean semTecnico;

    /** Atalho para "apenas os que ainda estao em aberto". */
    private boolean somenteAbertos;

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

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getTecnicoId() {
        return tecnicoId;
    }

    public void setTecnicoId(Long tecnicoId) {
        this.tecnicoId = tecnicoId;
    }

    public String getTermo() {
        return termo;
    }

    public void setTermo(String termo) {
        this.termo = termo;
    }

    public boolean isSemTecnico() {
        return semTecnico;
    }

    public void setSemTecnico(boolean semTecnico) {
        this.semTecnico = semTecnico;
    }

    public boolean isSomenteAbertos() {
        return somenteAbertos;
    }

    public void setSomenteAbertos(boolean somenteAbertos) {
        this.somenteAbertos = somenteAbertos;
    }
}
