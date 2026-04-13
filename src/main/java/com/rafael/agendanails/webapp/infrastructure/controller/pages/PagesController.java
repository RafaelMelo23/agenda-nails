package com.rafael.agendanails.webapp.infrastructure.controller.pages;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PagesController {

    @RequestMapping({
            "/",
            "/{tenantId:^(?!index\\.html$)[^.]+$}",
            "/{tenantId}/agendar",
            "/{tenantId}/entrar",
            "/{tenantId}/cadastro",
            "/{tenantId}/perfil",
            "/{tenantId}/admin/servicos",
            "/{tenantId}/admin/configuracoes",
            "/{tenantId}/profissional/agenda",
            "/{tenantId}/redefinir-senha",
            "/{tenantId}/offline"
    })
    public String index() {
        return "forward:/index.html";
    }
}