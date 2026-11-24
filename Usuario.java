package br.com.granacerta.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/** Jovem que usa a plataforma para aprender e organizar o próprio dinheiro. */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Informe o nome")
    @Size(max = 100)
    private String nome;

    @NotBlank(message = "Informe o e-mail")
    @Email(message = "E-mail inválido")
    @Column(unique = true, nullable = false)
    private String email;

    @NotNull(message = "Informe a data de nascimento")
    @Past(message = "A data precisa estar no passado")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataNascimento;

    @NotNull(message = "Informe a renda/mesada mensal")
    @PositiveOrZero(message = "Não pode ser negativa")
    @Column(precision = 12, scale = 2)
    private BigDecimal rendaMensal;

    @Size(max = 30)
    private String avatar = "🙂";

    private LocalDate criadoEm = LocalDate.now();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("data DESC")
    private List<Transacao> transacoes = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Meta> metas = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataHora DESC")
    private List<Tentativa> tentativas = new ArrayList<>();

    public Usuario() {}

    public Usuario(String nome, String email, LocalDate dataNascimento, BigDecimal rendaMensal, String avatar) {
        this.nome = nome;
        this.email = email;
        this.dataNascimento = dataNascimento;
        this.rendaMensal = rendaMensal;
        this.avatar = avatar;
    }

    public int getIdade() {
        return dataNascimento == null ? 0 : Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    /** Soma dos pontos de todos os quizzes respondidos. */
    public int getPontuacao() {
        return tentativas.stream().mapToInt(Tentativa::getPontos).sum();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public BigDecimal getRendaMensal() { return rendaMensal; }
    public void setRendaMensal(BigDecimal rendaMensal) { this.rendaMensal = rendaMensal; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public LocalDate getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDate criadoEm) { this.criadoEm = criadoEm; }
    public List<Transacao> getTransacoes() { return transacoes; }
    public List<Meta> getMetas() { return metas; }
    public List<Tentativa> getTentativas() { return tentativas; }
}
