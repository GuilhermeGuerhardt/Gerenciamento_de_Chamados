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

Na pasta do projeto:

```powershell
mvn spring-boot:run
```

Acesse: **http://localhost:8090**

> A porta é a **8090**, e não a 8080: nesta máquina o Windows reserva a 8080
> (faixa de portas excluídas), e o Tomcat não consegue subir nela. Para mudar,
> edite `server.port` em `application.properties`.

### Acesso inicial

| Perfil | Login | Senha |
|---|---|---|
| Administrador | `admin@digitrix.com.br` | `admin123` |
| Técnico (exemplo) | `tecnico@digitrix.com.br` | `tecnico123` |

> **Troque a senha do administrador no primeiro acesso** (menu *Meu perfil*).
> As credenciais padrão ficam definidas em `src/main/resources/application.properties`.

Para não criar os dados de exemplo (cliente, contatos e chamado de demonstração),
defina no `application.properties`:

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
src/main/java/br/com/digitrix/chamados/
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

## Usar como aplicativo de desktop (PWA)

O sistema é instalável: é a **mesma aplicação web**, mas abrindo em janela própria,
com ícone na área de trabalho e no menu Iniciar — sem barra de endereço.

No Edge ou Chrome, acesse o sistema e clique no ícone de instalar na barra de
endereço (ou menu **⋯ > Aplicativos > Instalar este site como um aplicativo**).

Também ficam disponíveis atalhos de clique direito no ícone: *Abrir chamado* e
*Meus chamados*.

> Requer HTTPS (ou localhost). Funciona no Render; num acesso por IP interno sem
> HTTPS, o sistema continua funcionando pelo navegador, apenas sem a opção de instalar.

**Por que não um programa nativo?** Um desktop em JavaFX conectando direto no
Postgres exigiria a senha do banco em cada máquina — e quem a extraísse leria e
escreveria o banco inteiro, furando todas as permissões por perfil e empresa.
Fazer certo exigiria uma API REST e um segundo cliente para manter. O PWA entrega
a experiência de aplicativo sem nenhum desses custos.

## Deploy no Render + Supabase

O projeto já vem com `Dockerfile` e `render.yaml`.

### 1. Supabase (banco)

Crie um projeto em [supabase.com](https://supabase.com). Em
**Project Settings > Database > Connection string**, copie os dados da conexão.
Use o **Connection pooler** (e não a conexão direta) — o Render abre e fecha
conexões com frequência.

### 2. Render (aplicação)

No painel: **New > Blueprint**, aponte para este repositório. O `render.yaml`
configura o resto. O Render vai pedir as variáveis marcadas como secretas:

| Variável | Exemplo / observação |
|---|---|
| `DATABASE_URL` | `jdbc:postgresql://aws-0-sa-east-1.pooler.supabase.com:5432/postgres?sslmode=require` |
| `DATABASE_USER` | `postgres.abcdefgh` (o Supabase inclui o ID do projeto) |
| `DATABASE_PASSWORD` | a senha do banco definida na criação do projeto |
| `ADMIN_EMAIL` | seu e-mail de administrador |
| `ADMIN_SENHA` | **use uma senha forte** — não a padrão de desenvolvimento |

A `DATABASE_URL` precisa do prefixo `jdbc:` e de `?sslmode=require`. O Supabase
mostra a URL no formato `postgresql://...`; adicione o `jdbc:` na frente.

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
