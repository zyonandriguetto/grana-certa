package br.com.granacerta.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;

/** Conteúdo educativo baseado nos temas do "Meu BC" do Banco Central. */
@Entity
@Table(name = "licoes")
public class Licao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Informe o título")
    @Size(max = 100)
    private String titulo;

    @NotBlank(message = "Informe o tema")
    @Size(max = 60)
    private String tema;

    @NotBlank(message = "Escreva um resumo")
    @Size(max = 255)
    private String resumo;

    @NotBlank(message = "Escreva o conteúdo")
    @Column(columnDefinition = "TEXT")
    private String conteudo;

    @NotNull(message = "Escolha o nível")
    @Enumerated(EnumType.STRING)
    private Nivel nivel;

    @Size(max = 255)
    private String fonte;

    @Size(max = 40)
    private String icone = "bi-book";

    @Min(value = 1, message = "A ordem começa em 1")
    private int ordem = 1;

    @OneToMany(mappedBy = "licao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pergunta> perguntas = new ArrayList<>();

    @OneToMany(mappedBy = "licao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tentativa> tentativas = new ArrayList<>();

    public Licao() {}

    public Licao(String titulo, String tema, String resumo, String conteudo, Nivel nivel, String fonte, String icone, int ordem) {
        this.titulo = titulo;
        this.tema = tema;
        this.resumo = resumo;
        this.conteudo = conteudo;
        this.nivel = nivel;
        this.fonte = fonte;
        this.icone = icone;
        this.ordem = ordem;
    }

    /** Parágrafos do conteúdo (separados por linha em branco). */
    public String[] getParagrafos() {
        return conteudo == null ? new String[0] : conteudo.split("\\r?\\n\\s*\\r?\\n");
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getTema() { return tema; }
    public void setTema(String tema) { this.tema = tema; }
    public String getResumo() { return resumo; }
    public void setResumo(String resumo) { this.resumo = resumo; }
    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }
    public Nivel getNivel() { return nivel; }
    public void setNivel(Nivel nivel) { this.nivel = nivel; }
    public String getFonte() { return fonte; }
    public void setFonte(String fonte) { this.fonte = fonte; }
    public String getIcone() { return icone; }
    public void setIcone(String icone) { this.icone = icone; }
    public int getOrdem() { return ordem; }
    public void setOrdem(int ordem) { this.ordem = ordem; }
    public List<Pergunta> getPerguntas() { return perguntas; }
    public List<Tentativa> getTentativas() { return tentativas; }
}
