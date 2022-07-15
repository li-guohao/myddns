FROM eclipse-temurin:17-jre as builder
MAINTAINER li-guohao
WORKDIR /app/myddns
RUN ["/app/myddns/gradlew", "clean", "distTar"]
RUN ["tar", "-xvf", "/app/myddns/build/distributions/myddns-1.0-SNAPSHOT.tar", "-C", "/app/myddns/"]
RUN ["chmod", "+x", "/app/myddns/bin/myddns"]
ENTRYPOINT /app/myddns/bin/myddns

