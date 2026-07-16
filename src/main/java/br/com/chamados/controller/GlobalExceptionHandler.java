package br.com.chamados.controller;

import br.com.chamados.service.AcessoNegadoException;
import br.com.chamados.service.NegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Transforma erro de regra de negocio em mensagem na tela anterior, em vez de
 * uma pagina de stack trace.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NegocioException.class)
    public String negocio(NegocioException ex, HttpServletRequest request, RedirectAttributes redirect) {
        redirect.addFlashAttribute("erro", ex.getMessage());
        return "redirect:" + origemOuInicio(request);
    }

    @ExceptionHandler(AcessoNegadoException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String acessoNegado(AcessoNegadoException ex) {
        log.warn("Acesso negado: {}", ex.getMessage());
        return "erro/acesso-negado";
    }

    /** Volta para a pagina de origem; se nao houver Referer, cai no dashboard. */
    private String origemOuInicio(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return "/";
        }
        int corte = referer.indexOf(request.getContextPath() + "/");
        return corte >= 0 ? referer.substring(corte) : "/";
    }
}
