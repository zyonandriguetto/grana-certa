package br.com.granacerta.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.LinkedHashMap;
import java.util.Map;

/** Pergunta de múltipla escolha do quiz de uma lição. */
@Entity
@Table(name = "perguntas")
public class Pergunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Escreva o enunciado")
    @Size(max = 500)
    @Column(length = 500)
    private String enunciado;

    @NotBlank(message = "Preencha a alternativa A") @Size(max = 255)
    private String alternativaA;
    @NotBlank(message = "Preencha a alternativa B") @Size(max = 255)
    private String alternativaB;
    @NotBlank(message = "Preencha a alternativa C") @Size(max = 255)
    private String alternativaC;
    @NotBlank(message = "Preencha a alternativa D") @Size(max = 255)
    private String alternativaD;

    @NotBlank(message = "Escolha a alternativa correta")
    @Pattern(regexp = "[ABCD]", message = "Use A, B, C ou D")
    @Column(length = 1)
    private String correta;

    @Size(max = 500)
    @Column(length = 500)
    private String explicacao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "licao_id")
    private Licao licao;

    public Pergunta() {}

    public Pergunta(String enunciado, String a, String b, String c, String d, String correta, String explicacao, Licao licao) {
        this.enunciado = enunciado;
        this.alternativaA = a;
        this.alternativaB = b;
        this.alternativaC = c;
        this.alternativaD = d;
        this.correta = correta;
        this.explicacao = explicacao;
        this.licao = licao;
    }

    public Map<String, String> getAlternativas() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("A", alternativaA);
        m.put("B", alternativaB);
        m.put("C", alternativaC);
        m.put("D", alternativaD);
        return m;
    }

    public String getTextoCorreto() {
        return getAlternativas().get(correta);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEnunciado() { return enunciado; }
    public void setEnunciado(String enunciado) { this.enunciado = enunciado; }
    public String getAlternativaA() { return alternativaA; }
    public void setAlternativaA(String alternativaA) { this.alternativaA = alternativaA; }
    public String getAlternativaB() { return alternativaB; }
    public void setAlternativaB(String alternativaB) { this.alternativaB = alternativaB; }
    public String getAlternativaC() { return alternativaC; }
    public void setAlternativaC(String alternativaC) { this.alternativaC = alternativaC; }
    public String getAlternativaD() { return alternativaD; }
    public void setAlternativaD(String alternativaD) { this.alternativaD = alternativaD; }
    public String getCorreta() { return correta; }
    public void setCorreta(String correta) { this.correta = correta; }
    public String getExplicacao() { return explicacao; }
    public void setExplicacao(String explicacao) { this.explicacao = explicacao; }
    public Licao getLicao() { return licao; }
    public void setLicao(Licao licao) { this.licao = licao; }
}
