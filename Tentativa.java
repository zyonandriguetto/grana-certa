package br.com.granacerta.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/** Resultado de um usuário ao responder o quiz de uma lição. */
@Entity
@Table(name = "tentativas")
public class Tentativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "licao_id")
    private Licao licao;

    @Min(value = 0, message = "Não pode ser negativo")
    private int acertos;

    @Min(value = 1, message = "Precisa ter ao menos 1 pergunta")
    private int totalPerguntas = 1;

    @NotNull(message = "Informe a data")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dataHora = LocalDateTime.now().withSecond(0).withNano(0);

    public Tentativa() {}

    public Tentativa(Usuario usuario, Licao licao, int acertos, int totalPerguntas) {
        this.usuario = usuario;
        this.licao = licao;
        this.acertos = acertos;
        this.totalPerguntas = totalPerguntas;
    }

    @AssertTrue(message = "Acertos não pode passar do total de perguntas")
    public boolean isAcertosValidos() {
        return acertos <= totalPerguntas;
    }

    public int getPercentual() {
        return totalPerguntas == 0 ? 0 : Math.round(acertos * 100f / totalPerguntas);
    }

    /** 10 pontos por acerto + 20 de bônus se gabaritar. */
    public int getPontos() {
        return acertos * 10 + (acertos == totalPerguntas && totalPerguntas > 0 ? 20 : 0);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Licao getLicao() { return licao; }
    public void setLicao(Licao licao) { this.licao = licao; }
    public int getAcertos() { return acertos; }
    public void setAcertos(int acertos) { this.acertos = acertos; }
    public int getTotalPerguntas() { return totalPerguntas; }
    public void setTotalPerguntas(int totalPerguntas) { this.totalPerguntas = totalPerguntas; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
}
