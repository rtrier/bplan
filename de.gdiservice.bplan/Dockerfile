FROM maven:3.8-jdk-11-openj9 as maven
WORKDIR /app
COPY  de.gdiservice.bplan /app/
RUN mvn package

FROM openjdk:11-jre-slim
RUN java -version

RUN groupadd -g 1700 gisadmin && \
  useradd -ms /bin/bash -u 17000 -g 1700 gisadmin

copy --from=maven /app/target/de.gdiservice.bplan-*.jar /gdiservice-lib/main.jar
copy --from=maven /app/target/alternateLocation /gdiservice-lib/

RUN ls -al /gdiservice-lib/*

WORKDIR /gdiservice-lib/

ENTRYPOINT [ "java", "-cp", "/gdiservice-lib/main.jar", "de.gdiservice.bplan.BPlanImportStarter" ]