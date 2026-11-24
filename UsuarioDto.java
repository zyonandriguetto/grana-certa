package br.com.granacerta.dto;

import br.com.granacerta.model.Usuario;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class UsuarioDto {

    private UsuarioDto() {}

    public record Request(
            @NotBlank(message = "Informe o nome") @Size(max = 100) String nome,
            @NotBlank(message = "Informe o e-mail") @Email(message = "E-mail inválido") @Size(max = 255) String email,
            @NotNull(message = "Informe a data de nascimento") @Past(message = "A data precisa estar no passado") LocalDate dataNascimento,
            @NotNull(message = "Informe a renda/mesada mensal") @PositiveOrZero(message = "Não pode ser negativa") BigDecimal rendaMensal,
            @Size(max = 30) String avatar) {

        public void aplicarEm(Usuario u) {
            u.setNome(nome);
            u.setEmail(email);
            u.setDataNascimento(dataNascimento);
            u.setRendaMensal(rendaMensal);
            if (avatar != null) u.setAvatar(avatar);
        }
    }

    public record Response(Long id, String nome, String email, LocalDate dataNascimento, int idade,
                           BigDecimal rendaMensal, String avatar, LocalDate criadoEm, int pontuacao) {

        public static Response de(Usuario u) {
            return new Response(u.getId(), u.getNome(), u.getEmail(), u.getDataNascimento(), u.getIdade(),
                    u.getRendaMensal(), u.getAvatar(), u.getCriadoEm(), u.getPontuacao());
        }
    }
}
