package br.com.granacerta.service;

import br.com.granacerta.model.Usuario;
import br.com.granacerta.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class UsuarioService extends CrudService<Usuario> {

    private final UsuarioRepository usuarios;

    public UsuarioService(UsuarioRepository usuarios) {
        super(usuarios, "Usuário");
        this.usuarios = usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarios.findAllByOrderByNomeAsc();
    }

    public boolean emailEmUso(Usuario u) {
        return u.getId() == null
                ? usuarios.existsByEmailIgnoreCase(u.getEmail())
                : usuarios.existsByEmailIgnoreCaseAndIdNot(u.getEmail(), u.getId());
    }

    @Override
    @Transactional
    public Usuario salvar(Usuario u) {
        if (u.getId() != null) {
            // Preserva dados que não estão no formulário
            Usuario atual = buscar(u.getId());
            atual.setNome(u.getNome());
            atual.setEmail(u.getEmail());
            atual.setDataNascimento(u.getDataNascimento());
            atual.setRendaMensal(u.getRendaMensal());
            atual.setAvatar(u.getAvatar());
            return usuarios.save(atual);
        }
        return usuarios.save(u);
    }

    @Transactional(readOnly = true)
    public List<Usuario> ranking() {
        return usuarios.findAll().stream()
                .sorted(Comparator.comparingInt(Usuario::getPontuacao).reversed())
                .toList();
    }
}
