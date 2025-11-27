FROM eclipse-temurin:17

WORKDIR /app

COPY target/gestion_de_notes-0.0.1-SNAPSHOT.jar /app/gestion_des_notes.jar

ENTRYPOINT ["java","-jar", "gestion_des_notes.jar" ]