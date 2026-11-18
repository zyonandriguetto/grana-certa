package br.com.granacerta.controller;

import br.com.granacerta.service.RegistroNaoEncontradoException;
import br.com.granacerta.service.RegraNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Converte erros em mensagens amigáveis em vez da página branca de erro. */
@ControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(RegraNegocioException.class)
    public String regraNegocio(RegraNegocioException e, HttpServletRequest req, RedirectAttributes ra) {
        ra.addFlashAttribute("erro", e.getMessage());
        return "redirect:" + voltar(req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String integridade(HttpServletRequest req, RedirectAttributes ra) {
        ra.addFlashAttribute("erro", "Não foi possível concluir: este registro está ligado a outros dados.");
        return "redirect:" + voltar(req);
    }

    @ExceptionHandler(RegistroNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String naoEncontrado(RegistroNaoEncontradoException e, Model model) {
        model.addAttribute("mensagem", e.getMessage());
        return "erro";
    }

    /** Volta para a página de onde veio, ou para a listagem do recurso. */
    private String voltar(HttpServletRequest req) {
        String referer = req.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            try {
                java.net.URI uri = java.net.URI.create(referer);
                String path = uri.getRawPath() + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");
                if (path.startsWith("/")) return path;
            } catch (IllegalArgumentException ignored) {
                // cai no padrão abaixo
            }
        }
        String uri = req.getRequestURI();
        String[] partes = uri.split("/");
        return partes.length > 1 ? "/" + partes[1] : "/";
    }
}
