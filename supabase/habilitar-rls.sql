-- GranaCerta: rode UMA vez no SQL Editor do Supabase, depois da primeira execução
-- da aplicação (é quando o Hibernate cria as tabelas).
--
-- Por quê: tabelas criadas por ORM/SQL no schema "public" nascem SEM Row Level Security
-- e, dependendo do projeto, com permissões para os papéis anon/authenticated. Como a
-- chave anon do Supabase é pública, isso deixaria os dados acessíveis pela Data API.
--
-- A aplicação conecta como "postgres" (dono das tabelas), que ignora o RLS,
-- então nada muda para ela.

alter table public.usuarios   enable row level security;
alter table public.categorias enable row level security;
alter table public.transacoes enable row level security;
alter table public.metas      enable row level security;
alter table public.licoes     enable row level security;
alter table public.perguntas  enable row level security;
alter table public.tentativas enable row level security;

revoke all on table
    public.usuarios, public.categorias, public.transacoes, public.metas,
    public.licoes, public.perguntas, public.tentativas
from anon, authenticated;
