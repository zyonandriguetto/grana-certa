package br.com.granacerta.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;

/** Categoria de receita ou despesa (ex.: Lanche, Transporte, Mesada). */
@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Informe o nome")
    @Size(max = 60)
    private String nome;

    @NotNull(message = "Escolha o tipo")
    @Enumerated(EnumType.STRING)
    private TipoMovimento tipo;

    @NotNull(message = "Escolha a classificação")
    @Enumerated(EnumType.STRING)
    private Classificacao classificacao;

    @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Cor no formato #RRGGBB")
    private String cor = "#6c5ce7";

    @Size(max = 40)
    private String icone = "bi-tag";

    @OneToMany(mappedBy = "categoria")
    private List<Transacao> transacoes = new ArrayList<>();

    public Categoria() {}

    public Categoria(String nome, TipoMovimento tipo, Classificacao classificacao, String cor, String icone) {
        this.nome = nome;
        this.tipo = tipo;
        this.classificacao = classificacao;
        this.cor = cor;
        this.icone = icone;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public TipoMovimento getTipo() { return tipo; }
    public void setTipo(TipoMovimento tipo) { this.tipo = tipo; }
    public Classificacao getClassificacao() { return classificacao; }
    public void setClassificacao(Classificacao classificacao) { this.classificacao = classificacao; }
    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }
    public String getIcone() { return icone; }
    public void setIcone(String icone) { this.icone = icone; }
    public List<Transacao> getTransacoes() { return transacoes; }
}
