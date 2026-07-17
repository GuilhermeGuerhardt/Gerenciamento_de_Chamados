# Gerenciamento de Chamados

Sistema de helpdesk em Java (Spring Boot + Thymeleaf + H2) para controlar chamados de
suporte: cadastro de clientes e seus contatos, equipe técnica com permissões, abertura e
atendimento de chamados, comentários e histórico.

## Pré-requisitos (já instalados nesta máquina)

- **JDK 21 LTS** — `C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot`
- **Maven 3.9.9** — `%LOCALAPPDATA%\Programs\apache-maven-3.9.9`

O `JAVA_HOME` e o PATH do usuário já apontam para eles.

> ### ⚠️ Não use o Java 26 neste projeto
>
> A máquina também tem o **Oracle JDK 26** instalado. Ele **não funciona** com o
> Spring Boot 3.4: só a versão 4.1 suporta Java 26. Se o `JAVA_HOME` for alterado
> ou apagado, o Maven cai no Java 26 do PATH e a aplicação não sobe.
>
> Se der erro estranho de inicialização, confira primeiro:
>
> ```powershell
> $env:JAVA_HOME     # deve terminar em jdk-21.0.11.10-hotspot
> mvn -version       # a linha "Java version" deve dizer 21.x
> ```
>
> Para corrigir na sessão atual:
>
> ```powershell
> $env:JAVA_HOME = 'C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot'
> ```

## Como rodar

Na pasta do projeto (o wrapper baixa o Maven certo sozinho — **não precisa
ter o Maven instalado**):

```powershell
.\mvnw spring-boot:run
```

> Em Linux/macOS: `./mvnw spring-boot:run`. Se já tiver o Maven instalado,
> `mvn spring-boot:run` também funciona.

Acesse: **http://localhost:8090**

> A porta é a **8090**, e não a 8080: nesta máquina o Windows reserva a 8080
> (faixa de portas excluídas), e o Tomcat não consegue subir nela. Para mudar,
> edite `server.port` em `application.properties`.

### Acesso inicial

No primeiro start o sistema cria **um único usuário**, o administrador global:

| Login | Senha |
|---|---|
| `admin@suaempresa.com.br` | `admin231@@` |

> **Troque a senha no primeiro acesso** (menu *Meu perfil*). Esses valores são o
> padrão de desenvolvimento, definidos em `application.properties`, e podem ser
> alterados pelas variáveis `ADMIN_EMAIL` e `ADMIN_SENHA`.
>
> Em produção não existe padrão: sem essas duas variáveis a aplicação se recusa
> a subir, justamente para nunca ir ao ar com a senha publicada aqui.

**Os técnicos são cadastrados por você**, entrando como administrador e indo em
**Usuários e técnicos → Novo usuário**, com perfil *Técnico*. É lá que a senha de
cada um é definida. O sistema não cria técnicos sozinho: um usuário gerado
automaticamente teria senha conhecida e seria uma porta aberta.

O mesmo vale para os solicitantes dos clientes — mesma tela, perfil *Solicitante*,
vinculados à empresa deles.

Para não criar o cliente e o chamado de demonstração:

```properties
app.dados-exemplo=false
```

## Perfis de acesso

| Perfil | O que pode fazer |
|---|---|
| **Administrador** | Tudo: cadastra usuários, clientes e contatos, atende e exclui chamados. |
| **Técnico** | Atende chamados (assume, comenta, muda status) e cadastra clientes/contatos. Não cria usuários. |
| **Solicitante** | Abre e acompanha apenas os chamados da própria empresa. Não vê notas internas. |

## Estrutura

```
src/main/java/br/com/chamados/
├── model/        Entidades JPA (Cliente, Contato, Usuario, Chamado, Comentario, Historico)
├── repository/   Consultas via Spring Data
├── service/      Regras de negócio (abertura, atribuição, status, histórico)
├── security/     Login, perfis e autorização
├── controller/   Rotas web
└── config/       Conversores de formulário e carga inicial

src/main/resources/
├── templates/    Telas Thymeleaf
├── static/css/   Folha de estilo
└── application.properties
```

## Deploy no Render + Supabase

O projeto já vem com `Dockerfile` e `render.yaml`.

### 1. Supabase (banco)

Projeto: **gerenciamento-chamados** (`hiakuwkqthqwrwdrdzpp`), região `sa-east-1`,
PostgreSQL 17.

Em **Project Settings > Database > Connection string**, escolha a aba **JDBC** e a
opção **Session pooler**.

> **Session pooler, não Transaction pooler.** O modo *transaction* (porta 6543) não
> suporta prepared statements, que o Hibernate usa o tempo todo — a aplicação
> quebraria em tempo de execução. O modo *session* (porta 5432 do pooler) é
> compatível e é o certo para um pool do HikariCP como o desta aplicação.
>
> Também não use a conexão direta (`db.<ref>.supabase.co`): ela tem poucas
> conexões disponíveis e o Render as recicla com frequência.

### 2. Render (aplicação)

No painel: **New > Blueprint**, aponte para este repositório. O `render.yaml`
configura o resto. O Render vai pedir as variáveis marcadas como secretas:

| Variável | Valor |
|---|---|
| `DATABASE_URL` | `jdbc:postgresql://aws-1-sa-east-1.pooler.supabase.com:5432/postgres?sslmode=require` |
| `DATABASE_USER` | `postgres.hiakuwkqthqwrwdrdzpp` |
| `DATABASE_PASSWORD` | a senha do banco (painel do Supabase → Database) |
| `ADMIN_EMAIL` | seu e-mail de administrador |
| `ADMIN_SENHA` | **use uma senha forte** — não a padrão de desenvolvimento |

Três detalhes que costumam derrubar o primeiro deploy:

- O Supabase mostra a URL como `postgresql://...`. O JDBC precisa do prefixo:
  vira `jdbc:postgresql://...`.
- O `?sslmode=require` no fim é obrigatório — o Supabase recusa conexão sem TLS.
- O host é `aws-1`, e não `aws-0` como aparece na maioria dos exemplos na
  internet. Confira sempre no painel do seu projeto.

O usuário **não** é apenas `postgres`: o pooler exige `postgres.<ref-do-projeto>`,
e a senha vai separada, fora da URL (o Spring a injeta pelo
`spring.datasource.password`).

### Limitações do plano gratuito

- **Render**: hiberna após 15 min sem acesso; o próximo acesso leva ~1 min.
- **Supabase**: o banco pausa após 7 dias de inatividade (basta reativar no painel).

### Sobre o schema em produção

O perfil `prod` usa `ddl-auto=update`: o Hibernate cria e ajusta as tabelas
sozinho. É prático para começar e nunca apaga dados, mas não versiona as
mudanças. Quando o sistema tiver dados reais, migre para **Flyway** e mude
`DDL_AUTO` para `validate`.

## Banco de dados

H2 em arquivo, criado automaticamente em `data/chamados.mv.db`. Os dados sobrevivem ao
reinício da aplicação.

Console web (somente administrador): http://localhost:8090/h2-console
JDBC URL: `jdbc:h2:file:./data/chamados` · usuário `sa` · senha em branco.

### Migrar para PostgreSQL ou MySQL

Troque a dependência no `pom.xml` e ajuste o `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/chamados
spring.datasource.username=postgres
spring.datasource.password=suasenha
```

O restante do código não muda — o JPA cuida da diferença.

## Regras de negócio implementadas

- Protocolo automático no formato `AAAA-NNNN`, reiniciando a cada ano.
- Prazo por prioridade (Urgente 2h, Alta 8h, Média 24h, Baixa 72h); chamados fora do prazo
  aparecem marcados como atrasados.
- Assumir um chamado da fila muda o status para *Em andamento* automaticamente.
- Resolver ou fechar exige o registro da solução.
- Chamado encerrado não aceita edição nem comentário — precisa ser reaberto.
- Toda mudança de status, prioridade e responsável entra no histórico com autor e data.
- Notas internas ficam invisíveis para o solicitante.
- Cliente com chamados não pode ser excluído, apenas inativado.
- O sistema não permite ficar sem nenhum administrador ativo.
