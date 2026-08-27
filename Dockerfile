# Estágio 1: Build (Usamos uma imagem pesada do Maven com Java 21 para compilar)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copia o código do nosso projeto para dentro do contêiner
COPY pom.xml .
COPY src ./src

# Pede pro Maven compilar e gerar o arquivo .jar (pulamos os testes na construção da imagem)
RUN mvn clean package -DskipTests

# Estágio 2: Execução (Usamos uma imagem leve, apenas com o ambiente de execução do Java 21)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Pega o .jar gerado no Estágio 1 e coloca aqui
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta 8080 (padrão do Spring)
EXPOSE 8080

# Comando para rodar a aplicação quando o contêiner iniciar
ENTRYPOINT ["java", "-jar", "app.jar"]
