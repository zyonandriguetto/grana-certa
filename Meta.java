package br.com.granacerta.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** Objetivo de poupança (ex.: comprar um notebook, reserva de emergência). */
@Entity
@Table(name = "metas")
public class Meta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Dê um nome para a meta")
    @Size(max = 80)
    private String titulo;

    @Size(max = 255)
    private String descricao;

    @NotNull(message = "Informe o valor da meta")
    @Positive(message = "O valor precisa ser maior que zero")
    @Column(precision = 12, scale = 2)
    private BigDecimal valorAlvo;

    @NotNull(message = "Informe o prazo")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate prazo;

    @Size(max = 10)
    private String emoji = "🎯";

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @OneToMany(mappedBy = "meta")
    private List<Transacao> aportes = new ArrayList<>();

    /** Ao excluir a meta, as transações continuam existindo, só perdem o vínculo. */
    @PreRemove
    private void desvincularAportes() {
        aportes.forEach(t -> t.setMeta(null));
    }

    public Meta() {}

    public Meta(String titulo, String descricao, BigDecimal valorAlvo, LocalDate prazo, String emoji, Usuario usuario) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.valorAlvo = valorAlvo;
        this.prazo = prazo;
        this.emoji = emoji;
        this.usuario = usuario;
    }

    public BigDecimal getValorAtual() {
        return aportes.stream().map(Transacao::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getPercentual() {
        if (valorAlvo == null || valorAlvo.signum() == 0) return 0;
        int p = getValorAtual().multiply(BigDecimal.valueOf(100)).divide(valorAlvo, 0, RoundingMode.DOWN).intValue();
        return Math.min(p, 100);
    }

    public boolean isConcluida() {
        return valorAlvo != null && getValorAtual().compareTo(valorAlvo) >= 0;
    }

    /** Quanto guardar por mês para bater a meta no prazo. */
    public BigDecimal getAporteMensalSugerido() {
        if (valorAlvo == null || prazo == null) return BigDecimal.ZERO;
        BigDecimal falta = valorAlvo.subtract(getValorAtual());
        if (falta.signum() <= 0) return BigDecimal.ZERO;
        long meses = Math.max(1, ChronoUnit.MONTHS.between(LocalDate.now().withDayOfMonth(1), prazo.withDayOfMonth(1)));
        return falta.divide(BigDecimal.valueOf(meses), 2, RoundingMode.UP);
    }

    public long getDiasRestantes() {
        return prazo == null ? 0 : ChronoUnit.DAYS.between(LocalDate.now(), prazo);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getValorAlvo() { return valorAlvo; }
    public void setValorAlvo(BigDecimal valorAlvo) { this.valorAlvo = valorAlvo; }
    public LocalDate getPrazo() { return prazo; }
    public void setPrazo(LocalDate prazo) { this.prazo = prazo; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public List<Transacao> getAportes() { return aportes; }
}
