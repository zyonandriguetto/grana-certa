package br.com.granacerta.config;

import br.com.granacerta.model.*;
import br.com.granacerta.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Random;

import static br.com.granacerta.model.Classificacao.*;
import static br.com.granacerta.model.TipoMovimento.DESPESA;
import static br.com.granacerta.model.TipoMovimento.RECEITA;

/** Popula o banco com exemplos na primeira execução (banco vazio). */
@Component
public class DadosIniciais implements CommandLineRunner {

    private static final String FONTE_BC = "https://www.bcb.gov.br/meubc/faqs";

    private final UsuarioRepository usuarios;
    private final CategoriaRepository categorias;
    private final TransacaoRepository transacoes;
    private final MetaRepository metas;
    private final LicaoRepository licoes;
    private final PerguntaRepository perguntas;
    private final TentativaRepository tentativas;
    private final boolean ativo;

    public DadosIniciais(UsuarioRepository usuarios, CategoriaRepository categorias, TransacaoRepository transacoes,
                         MetaRepository metas, LicaoRepository licoes, PerguntaRepository perguntas,
                         TentativaRepository tentativas, @Value("${granacerta.seed:true}") boolean ativo) {
        this.usuarios = usuarios;
        this.categorias = categorias;
        this.transacoes = transacoes;
        this.metas = metas;
        this.licoes = licoes;
        this.perguntas = perguntas;
        this.tentativas = tentativas;
        this.ativo = ativo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!ativo || categorias.count() > 0) return;

        // ---------------- Categorias ----------------
        Categoria mesada = cat("Mesada", RECEITA, ENTRADA, "#00b894", "bi-cash-coin");
        Categoria estagio = cat("Estágio / Trabalho", RECEITA, ENTRADA, "#00cec9", "bi-briefcase");
        Categoria extra = cat("Renda extra", RECEITA, ENTRADA, "#55efc4", "bi-stars");
        Categoria transporte = cat("Transporte", DESPESA, NECESSIDADE, "#0984e3", "bi-bus-front");
        Categoria alimentacao = cat("Alimentação", DESPESA, NECESSIDADE, "#e17055", "bi-cup-hot");
        Categoria estudos = cat("Estudos", DESPESA, NECESSIDADE, "#6c5ce7", "bi-mortarboard");
        Categoria celular = cat("Celular e internet", DESPESA, NECESSIDADE, "#74b9ff", "bi-phone");
        Categoria lazer = cat("Lazer e rolês", DESPESA, DESEJO, "#fd79a8", "bi-controller");
        Categoria delivery = cat("Delivery e lanches", DESPESA, DESEJO, "#fdcb6e", "bi-bag");
        Categoria assinaturas = cat("Assinaturas e jogos", DESPESA, DESEJO, "#a29bfe", "bi-play-btn");
        Categoria roupas = cat("Roupas e acessórios", DESPESA, DESEJO, "#e84393", "bi-handbag");
        Categoria poupanca = cat("Poupança / Investimento", DESPESA, POUPANCA, "#2d3436", "bi-piggy-bank");

        // ---------------- Usuários ----------------
        Usuario ana = usuarios.save(new Usuario("Ana Beatriz", "ana@granacerta.dev", LocalDate.of(2007, 3, 14), new BigDecimal("1200.00"), "🦊"));
        Usuario lucas = usuarios.save(new Usuario("Lucas Oliveira", "lucas@granacerta.dev", LocalDate.of(2009, 8, 2), new BigDecimal("300.00"), "🐼"));
        Usuario julia = usuarios.save(new Usuario("Júlia Santos", "julia@granacerta.dev", LocalDate.of(2005, 11, 21), new BigDecimal("1800.00"), "🦁"));

        // ---------------- Metas ----------------
        Meta notebook = metas.save(new Meta("Notebook para a faculdade", "Um notebook bom para programar", new BigDecimal("3500.00"), LocalDate.now().plusMonths(10), "💻", ana));
        Meta reservaAna = metas.save(new Meta("Reserva de emergência", "3 meses dos meus gastos essenciais", new BigDecimal("2000.00"), LocalDate.now().plusMonths(14), "🛟", ana));
        Meta show = metas.save(new Meta("Ingresso do show", "Show em São Paulo no fim do ano", new BigDecimal("450.00"), LocalDate.now().plusMonths(4), "🎤", lucas));
        Meta intercambio = metas.save(new Meta("Intercâmbio", "Curso de inglês fora do país", new BigDecimal("12000.00"), LocalDate.now().plusMonths(24), "✈️", julia));

        // ---------------- Transações (últimos 6 meses) ----------------
        Random rnd = new Random(42);
        YearMonth atual = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth m = atual.minusMonths(i);
            int ultimoDia = i == 0 ? Math.max(1, LocalDate.now().getDayOfMonth()) : m.lengthOfMonth();

            // Ana: estagiária organizada, mas com deslizes no delivery
            tx(ana, estagio, "Bolsa estágio", "1200.00", m, 5, ultimoDia, null, false);
            tx(ana, transporte, "Recarga do cartão de ônibus", "180.00", m, 6, ultimoDia, null, false);
            tx(ana, alimentacao, "Almoço na faculdade", v(rnd, 160, 220), m, 10, ultimoDia, null, false);
            tx(ana, celular, "Plano do celular", "59.90", m, 12, ultimoDia, null, false);
            tx(ana, estudos, "Material e xerox", v(rnd, 30, 70), m, 8, ultimoDia, null, false);
            tx(ana, delivery, "Delivery no fim de semana", v(rnd, 60, 160), m, 15, ultimoDia, null, rnd.nextBoolean());
            tx(ana, lazer, "Cinema com a turma", v(rnd, 40, 90), m, 20, ultimoDia, null, false);
            tx(ana, assinaturas, "Streaming", "29.90", m, 3, ultimoDia, null, false);
            tx(ana, poupanca, "Guardado para o notebook", "150.00", m, 6, ultimoDia, notebook, false);
            tx(ana, poupanca, "Reserva de emergência", "80.00", m, 6, ultimoDia, reservaAna, false);

            // Lucas: 14 anos, mesada pequena, aprende a guardar para o show
            tx(lucas, mesada, "Mesada", "300.00", m, 1, ultimoDia, null, false);
            if (rnd.nextBoolean()) tx(lucas, extra, "Vendeu brigadeiro na escola", v(rnd, 40, 90), m, 18, ultimoDia, null, false);
            tx(lucas, delivery, "Lanche na cantina", v(rnd, 50, 90), m, 9, ultimoDia, null, false);
            tx(lucas, assinaturas, "Passe de batalha do jogo", "34.90", m, 14, ultimoDia, null, true);
            tx(lucas, lazer, "Rolê no shopping", v(rnd, 30, 70), m, 22, ultimoDia, null, rnd.nextInt(3) == 0);
            tx(lucas, poupanca, "Cofrinho do show", v(rnd, 50, 90), m, 2, ultimoDia, show, false);

            // Júlia: trabalha, mas gasta demais com desejos
            tx(julia, estagio, "Salário (jovem aprendiz)", "1800.00", m, 5, ultimoDia, null, false);
            tx(julia, transporte, "Aplicativo de transporte", v(rnd, 180, 320), m, 7, ultimoDia, null, false);
            tx(julia, alimentacao, "Mercado", v(rnd, 250, 350), m, 10, ultimoDia, null, false);
            tx(julia, celular, "Celular e internet", "99.90", m, 11, ultimoDia, null, false);
            tx(julia, roupas, "Compras online", v(rnd, 200, 520), m, 16, ultimoDia, null, true);
            tx(julia, lazer, "Bares e festas", v(rnd, 200, 380), m, 21, ultimoDia, null, rnd.nextBoolean());
            tx(julia, delivery, "Delivery", v(rnd, 120, 260), m, 25, ultimoDia, null, true);
            tx(julia, poupanca, "Guardado para o intercâmbio", v(rnd, 50, 150), m, 6, ultimoDia, intercambio, false);
        }

        // ---------------- Lições e perguntas ----------------
        Licao orcamento = licao(1, "Orçamento pessoal e a regra 50-30-20", "Planejamento", Nivel.INICIANTE, "bi-wallet2",
                "Aprenda a dividir o seu dinheiro entre o que precisa, o que quer e o que vai guardar.",
                """
                Orçamento é simplesmente saber quanto dinheiro entra, quanto sai e para onde ele vai. Parece óbvio, mas a maioria das pessoas que se endivida não sabe responder essas três perguntas.

                Uma forma fácil de começar é a regra 50-30-20: até 50% da renda para necessidades (transporte, alimentação, estudos), até 30% para desejos (lazer, delivery, roupas, jogos) e pelo menos 20% para poupar ou investir.

                O segredo é "pagar a si mesmo primeiro": assim que a mesada ou o salário cair, separe a parte da poupança. Se você deixar para guardar "o que sobrar", quase nunca sobra.

                Registre todos os gastos, até os pequenos. Um lanche de R$ 15 por dia vira R$ 450 por mês. É no detalhe que o orçamento escapa.
                """);
        perg(orcamento, "Na regra 50-30-20, qual percentual da renda deve ir para poupança ou investimentos?",
                "50%", "30%", "20%", "Nada, só se sobrar", "C",
                "Pelo menos 20% para o seu futuro: reserva de emergência, metas e investimentos.");
        perg(orcamento, "Qual destes gastos é um DESEJO, e não uma necessidade?",
                "Passagem de ônibus para a escola", "Assinatura de streaming", "Material escolar", "Conta de luz", "B",
                "Streaming é ótimo, mas dá para viver sem. Desejos são os primeiros a cortar quando o dinheiro aperta.");
        perg(orcamento, "O que significa \"pagar a si mesmo primeiro\"?",
                "Comprar algo para você assim que receber", "Separar a parte da poupança logo que o dinheiro entra",
                "Pagar as dívidas dos amigos", "Gastar tudo e guardar o troco", "B",
                "Guardar antes de gastar transforma a poupança em hábito, e não em sobra.");
        perg(orcamento, "Um lanche de R$ 15 todos os dias custa aproximadamente quanto em um mês?",
                "R$ 45", "R$ 150", "R$ 450", "R$ 1.500", "C",
                "15 x 30 dias = R$ 450. Pequenos gastos repetidos são grandes gastos disfarçados.");

        Licao juros = licao(2, "Juros compostos: o tempo trabalhando por você", "Investimentos", Nivel.INICIANTE, "bi-graph-up-arrow",
                "Entenda por que começar a guardar cedo vale mais do que guardar muito depois.",
                """
                Juros são o "aluguel" do dinheiro. Quando você investe, recebe juros. Quando pega emprestado, paga juros.

                Nos juros compostos, os juros de cada mês passam a render juros nos meses seguintes. É a famosa "bola de neve": no começo cresce devagar, depois acelera.

                Por isso o tempo é o maior aliado de quem é jovem. Quem guarda R$ 100 por mês dos 16 aos 26 anos e para pode terminar com mais dinheiro do que quem começa aos 26 e guarda até os 36, mesmo tendo colocado o mesmo valor.

                A mesma bola de neve funciona contra você nas dívidas. Por isso: use juros compostos a seu favor (investindo) e fuja deles quando estiverem contra você (rotativo, cheque especial).
                """);
        perg(juros, "O que são juros compostos?",
                "Juros que só incidem sobre o valor inicial", "Juros calculados sobre o valor inicial mais os juros já acumulados",
                "Uma taxa fixa cobrada pelo banco", "Um tipo de imposto", "B",
                "É juros sobre juros: o rendimento de um mês passa a render nos meses seguintes.");
        perg(juros, "Para quem é jovem, qual é o maior aliado dos juros compostos?",
                "A sorte", "O tempo", "O cartão de crédito", "O cheque especial", "B",
                "Quanto mais cedo você começa, mais meses a bola de neve tem para crescer.");
        perg(juros, "Os juros compostos podem trabalhar contra você quando...",
                "Você investe na poupança", "Você deixa uma dívida crescer sem pagar",
                "Você guarda dinheiro no cofrinho", "Você recebe a mesada", "B",
                "Em dívidas, os juros também se acumulam sobre juros, e a dívida cresce rápido.");

        Licao cartao = licao(3, "Cartão de crédito e a armadilha do rotativo", "Crédito", Nivel.INTERMEDIARIO, "bi-credit-card",
                "Cartão não é dinheiro extra. Veja como o pagamento mínimo vira uma bola de neve.",
                """
                O cartão de crédito é um meio de pagamento: você compra hoje e paga na fatura. Usado com controle, ajuda a organizar as contas. Usado sem controle, vira uma das dívidas mais caras do Brasil.

                Se você paga só o mínimo ou uma parte da fatura, o restante entra no crédito rotativo, que tem juros altíssimos. Pelas regras do Banco Central, o rotativo só pode ser usado até o vencimento da fatura seguinte; depois disso, o banco precisa oferecer um parcelamento, normalmente com juros menores.

                Desde janeiro de 2024 (Lei 14.690/2023), os juros e encargos do rotativo e do parcelamento da fatura não podem ultrapassar 100% do valor original da dívida. Ou seja: uma dívida de R$ 500 não pode passar de R$ 1.000. Ainda assim, dobrar uma dívida é péssimo negócio.

                Dicas: pague sempre o total da fatura, acompanhe os gastos pelo aplicativo, não trate o limite como se fosse seu dinheiro e desconfie de compras parceladas que comprometem vários meses do seu orçamento.
                """);
        perg(cartao, "O que acontece quando você paga apenas o valor mínimo da fatura?",
                "Nada, a dívida some", "O restante entra no crédito rotativo, com juros altos",
                "O banco perdoa o resto", "O limite do cartão aumenta", "B",
                "O saldo não pago vira crédito rotativo, uma das linhas de crédito mais caras.");
        perg(cartao, "Desde 2024, qual é o limite dos juros e encargos do rotativo do cartão?",
                "Não há limite", "10% da dívida", "100% do valor original da dívida", "O dobro da renda do cliente", "C",
                "A Lei 14.690/2023 limitou juros e encargos a 100% do valor original: a dívida pode, no máximo, dobrar.");
        perg(cartao, "Qual é a melhor forma de usar o cartão de crédito?",
                "Pagar sempre o mínimo", "Usar todo o limite todo mês",
                "Pagar o total da fatura e controlar os gastos", "Ter vários cartões para aumentar o limite", "C",
                "Pagando o total, você não paga juros e usa o cartão só como meio de pagamento.");
        perg(cartao, "O limite do cartão de crédito é...",
                "Dinheiro extra que o banco te deu", "Um empréstimo que precisará ser pago",
                "Parte do seu salário", "Um investimento", "B",
                "Limite é crédito: tudo que você usa vira conta para pagar depois.");

        Licao pix = licao(4, "Pix seguro: como não cair em golpes", "Segurança", Nivel.INICIANTE, "bi-shield-check",
                "Os golpes mais comuns com Pix e o que fazer se acontecer com você.",
                """
                O Pix é rápido, gratuito para pessoas físicas e funciona 24 horas. Justamente por ser instantâneo, virou alvo de golpistas, que usam engenharia social: eles enganam a pessoa para que ela mesma faça a transferência.

                Golpes comuns: falso parente ou amigo pedindo dinheiro por um número novo de WhatsApp, falsa central do banco ligando para "confirmar dados", links de promoções imperdíveis, QR Codes adulterados e o falso "Pix agendado por engano" pedindo devolução.

                Regras de ouro: o Banco Central não liga, não manda mensagem e não pede seus dados. Funcionários de banco não pedem senha nem códigos. Desconfie de urgência. Antes de pagar, confira o nome e a instituição de quem vai receber. Ligue para a pessoa no número antigo antes de transferir.

                Se cair em um golpe, avise o seu banco imediatamente e registre boletim de ocorrência. Existe o Mecanismo Especial de Devolução (MED), que permite ao banco bloquear e tentar devolver os valores em casos de fraude. Quanto mais rápido você agir, maior a chance de recuperar.
                """);
        perg(pix, "Você recebe uma ligação de alguém dizendo ser do Banco Central pedindo seus dados. O que fazer?",
                "Passar os dados, pois é o Banco Central", "Desligar: o Banco Central não liga nem pede dados de clientes",
                "Passar só a senha do cartão", "Fazer um Pix de teste", "B",
                "O BC não tem contato direto com clientes de bancos para pedir dados. É golpe.");
        perg(pix, "Um amigo manda mensagem de um número novo pedindo um Pix urgente. Qual é a atitude correta?",
                "Fazer o Pix logo, amigo é amigo", "Ligar para o número antigo dele e confirmar antes",
                "Mandar metade do valor", "Pedir o QR Code", "B",
                "O golpe do falso amigo/parente é muito comum. Confirme sempre por outro canal.");
        perg(pix, "O que é o MED (Mecanismo Especial de Devolução)?",
                "Um tipo de investimento", "Um recurso que permite bloquear e devolver valores em casos de fraude no Pix",
                "Uma taxa cobrada no Pix", "Um aplicativo de banco", "B",
                "O MED ajuda a recuperar valores em fraudes. Avise o banco o quanto antes.");
        perg(pix, "Antes de confirmar um Pix, o que você deve conferir?",
                "Só o valor", "O nome do recebedor e a instituição", "A cor do aplicativo", "Nada, Pix é sempre seguro", "B",
                "Conferir nome e instituição ajuda a perceber QR Codes adulterados e chaves trocadas.");

        Licao emprestimo = licao(5, "Empréstimos, CET e endividamento", "Crédito", Nivel.AVANCADO, "bi-bank",
                "Como comparar empréstimos de verdade e evitar que a dívida saia do controle.",
                """
                Empréstimo não é renda: é dinheiro de amanhã que você usa hoje, pagando por isso. Antes de pegar, pergunte: preciso mesmo? Consigo pagar a parcela sem apertar as necessidades do mês?

                Para comparar ofertas, não olhe só a taxa de juros. Olhe o CET, Custo Efetivo Total, que inclui juros, tarifas, seguros e impostos. As instituições são obrigadas a informar o CET antes da contratação.

                As linhas mais caras costumam ser o rotativo do cartão e o cheque especial. Se já estiver endividado, troque dívidas caras por mais baratas (portabilidade ou renegociação), priorize as de juros maiores e nunca pegue um empréstimo para pagar outro sem fazer as contas.

                O Banco Central oferece o Registrato, onde você consulta gratuitamente os empréstimos, financiamentos e chaves Pix registrados no seu CPF. É uma boa forma de descobrir se alguém fez dívida no seu nome.
                """);
        perg(emprestimo, "O que é o CET?",
                "Cadastro Especial de Tarifas", "Custo Efetivo Total: juros + tarifas + seguros + impostos",
                "A taxa básica de juros do país", "Um tipo de conta digital", "B",
                "O CET mostra o custo real do crédito e é o número certo para comparar ofertas.");
        perg(emprestimo, "Quais costumam ser as linhas de crédito mais caras para o consumidor?",
                "Financiamento imobiliário", "Rotativo do cartão e cheque especial", "Crédito consignado", "Financiamento estudantil", "B",
                "São práticas, mas caríssimas. Devem ser usadas só em emergências e por pouco tempo.");
        perg(emprestimo, "Qual serviço do Banco Central mostra os empréstimos registrados no seu CPF?",
                "Pix", "Registrato", "Drex", "Tesouro Direto", "B",
                "O Registrato é gratuito e mostra empréstimos, financiamentos, contas e chaves Pix ligadas ao CPF.");

        Licao reserva = licao(6, "Reserva de emergência e primeiros investimentos", "Investimentos", Nivel.INTERMEDIARIO, "bi-safe",
                "O primeiro passo antes de investir: um colchão para os imprevistos.",
                """
                Reserva de emergência é um dinheiro guardado para imprevistos: celular quebrado, perda do estágio, um problema de saúde. Ela evita que você precise recorrer a empréstimos caros quando algo dá errado.

                Uma referência comum é juntar de 3 a 6 meses dos seus gastos essenciais. Esse dinheiro precisa estar em um lugar seguro e com liquidez, ou seja, que você consiga sacar rápido, mesmo que renda pouco.

                Só depois de montar a reserva vale pensar em investimentos com prazos maiores. Desconfie de promessas de ganho alto, rápido e garantido: retorno alto sem risco não existe, e esse é o discurso clássico de pirâmides financeiras.

                Ah, e uma curiosidade: o Banco Central tem o sistema Valores a Receber, onde você confere se tem algum dinheiro esquecido em bancos, como saldo de conta antiga ou tarifa cobrada indevidamente.
                """);
        perg(reserva, "Para que serve a reserva de emergência?",
                "Para comprar o lançamento do celular", "Para cobrir imprevistos sem precisar de empréstimo caro",
                "Para emprestar aos amigos", "Para apostar", "B",
                "Ela é seu colchão: quando o imprevisto chega, você não precisa se endividar.");
        perg(reserva, "Uma característica importante do lugar onde deixar a reserva é...",
                "Alto risco", "Liquidez: poder sacar rápido", "Ficar presa por 10 anos", "Ser em criptomoeda", "B",
                "A reserva precisa estar disponível quando você precisar, com segurança.");
        perg(reserva, "Alguém promete que você vai dobrar o dinheiro em um mês, sem risco. Isso é...",
                "Uma ótima oportunidade", "Um sinal clássico de golpe ou pirâmide financeira",
                "Um investimento do Tesouro", "Normal no mercado", "B",
                "Retorno alto, rápido e garantido não existe. Desconfie e não indique para amigos.");

        // ---------------- Tentativas de exemplo ----------------
        tentativa(ana, orcamento, 4, 4, 20);
        tentativa(ana, juros, 2, 3, 15);
        tentativa(ana, cartao, 3, 4, 8);
        tentativa(lucas, orcamento, 3, 4, 12);
        tentativa(lucas, pix, 4, 4, 5);
        tentativa(julia, orcamento, 2, 4, 9);
        tentativa(julia, cartao, 2, 4, 3);
    }

    // ---------------- auxiliares ----------------

    private Categoria cat(String nome, TipoMovimento tipo, Classificacao c, String cor, String icone) {
        return categorias.save(new Categoria(nome, tipo, c, cor, icone));
    }

    private void tx(Usuario u, Categoria c, String desc, String valor, YearMonth m, int dia, int ultimoDia, Meta meta, boolean impulsiva) {
        if (dia > ultimoDia) return; // não cria transação no futuro
        Transacao t = new Transacao(desc, new BigDecimal(valor), m.atDay(Math.min(dia, m.lengthOfMonth())), u, c);
        t.setMeta(meta);
        t.setImpulsiva(impulsiva);
        transacoes.save(t);
        if (meta != null) meta.getAportes().add(t);
        u.getTransacoes().add(t);
    }

    private static String v(Random rnd, int min, int max) {
        int centavos = (min * 100) + rnd.nextInt((max - min) * 100 + 1);
        return BigDecimal.valueOf(centavos, 2).toPlainString();
    }

    private Licao licao(int ordem, String titulo, String tema, Nivel nivel, String icone, String resumo, String conteudo) {
        return licoes.save(new Licao(titulo, tema, resumo, conteudo.strip(), nivel, FONTE_BC, icone, ordem));
    }

    private void perg(Licao l, String enunciado, String a, String b, String c, String d, String correta, String explicacao) {
        Pergunta p = perguntas.save(new Pergunta(enunciado, a, b, c, d, correta, explicacao, l));
        l.getPerguntas().add(p);
    }

    private void tentativa(Usuario u, Licao l, int acertos, int total, int diasAtras) {
        Tentativa t = new Tentativa(u, l, acertos, total);
        t.setDataHora(LocalDateTime.now().minusDays(diasAtras).withSecond(0).withNano(0));
        tentativas.save(t);
        u.getTentativas().add(t);
    }
}
