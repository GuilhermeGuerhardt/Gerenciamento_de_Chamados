# ---------- Etapa 1: build ----------
# O JDK completo so e necessario para compilar; ele nao vai para a imagem final.
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Copiamos o pom sozinho primeiro: enquanto ele nao mudar, o Docker reaproveita
# a camada com as dependencias ja baixadas e o build fica bem mais rapido.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Etapa 2: execucao ----------
# Só o JRE: imagem final menor e com menos superficie de ataque.
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# A aplicacao nao precisa de root para rodar.
RUN addgroup -S app && adduser -S app -G app

COPY --from=build /build/target/*.jar app.jar
RUN chown app:app app.jar

USER app

EXPOSE 8090

# Esta imagem so existe para ser implantada, entao o padrao dela e producao.
# Sem isto, um servico criado sem SPRING_PROFILES_ACTIVE ignora as variaveis do
# banco e tenta subir com o H2 local, que nem consegue gravar em disco aqui —
# o erro que aparece e "Unable to determine Dialect", que nao diz nada sobre a
# causa real. O render.yaml tambem define a variavel; esta e a rede de seguranca
# para quando o servico for criado na mao, sem o Blueprint.
ENV SPRING_PROFILES_ACTIVE=prod

# MaxRAMPercentage faz a JVM respeitar o limite de memoria do container
# (no free tier do Render sao 512 MB) em vez de assumir a RAM da maquina toda.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"

# "exec" faz a JVM virar o PID 1 e receber o SIGTERM do Render, permitindo
# que o Spring encerre as conexoes com o banco antes de morrer.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
