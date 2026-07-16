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
