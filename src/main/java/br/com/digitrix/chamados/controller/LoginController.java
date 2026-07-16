package br.com.digitrix.chamados.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/erro/acesso-negado")
    public String acessoNegado() {
        return "erro/acesso-negado";
    }
}
