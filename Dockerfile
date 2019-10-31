FROM openjdk:8-alpine

COPY target/uberjar/bond-analyzer.jar /bond-analyzer/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/bond-analyzer/app.jar"]
