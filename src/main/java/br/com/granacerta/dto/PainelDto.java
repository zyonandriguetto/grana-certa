package br.com.granacerta.dto;

import br.com.granacerta.service.PainelService;
import br.com.granacerta.service.PainelService.FatiaCategoria;
import br.com.granacerta.service.PainelService.Insight;
import br.com.granacerta.service.PainelService.Regra503020;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public final class PainelDto {

    private PainelDto() {}

    public record Response(UsuarioDto.Response usuario, YearMonth mes,
                           BigDecimal receitas, BigDecimal despesas, BigDecimal saldo,
                           BigDecimal impulsivas, int score, String scoreRotulo,
                           List<FatiaCategoria> porCategoria, List<Regra503020> regra,
                           List<String> mesesRotulos, List<BigDecimal> receitasSerie, List<BigDecimal> despesasSerie,
                           List<Insight> insights, List<TransacaoDto.Response> ultimas,
                           List<MetaDto.Response> metas) {

        public static Response de(PainelService.Painel p) {
            return new Response(UsuarioDto.Response.de(p.usuario()), p.mes(),
                    p.receitas(), p.despesas(), p.saldo(), p.impulsivas(), p.score(), p.scoreRotulo(),
                    p.porCategoria(), p.regra(), p.mesesRotulos(), p.receitasSerie(), p.despesasSerie(),
                    p.insights(),
                    p.ultimas().stream().map(TransacaoDto.Response::de).toList(),
                    p.metas().stream().map(MetaDto.Response::de).toList());
        }
    }
}
