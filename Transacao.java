package br.com.granacerta.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Movimentação financeira do usuário. Pode ser um depósito em uma meta. */
@Entity
@Table(name = "transacoes")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Descreva a transação")
    @Size(max = 120)
    private String descricao;

    @NotNull(message = "Informe o valor")
    @Positive(message = "O valor precisa ser maior que zero")
    @Column(precision = 12, scale = 2)
    private BigDecimal valor;

    @NotNull(message = "Informe a data")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate data = LocalDate.now();

    /** Marcação de compra por impulso, usada nos alertas educativos. */
    private boolean impulsiva;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    /** Quando preenchida, a transação conta como depósito na meta. */
    @ManyToOne
    @JoinColumn(name = "meta_id")
    private Meta meta;

    public Transacao() {}

    public Transacao(String descricao, BigDecimal valor, LocalDate data, Usuario usuario, Categoria categoria) {
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.usuario = usuario;
        this.categoria = categoria;
    }

    public boolean isReceita() {
        return categoria != null && categoria.getTipo() == TipoMovimento.RECEITA;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public boolean isImpulsiva() { return impulsiva; }
    public void setImpulsiva(boolean impulsiva) { this.impulsiva = impulsiva; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public Meta getMeta() { return meta; }
    public void setMeta(Meta meta) { this.meta = meta; }
}
