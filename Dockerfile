FROM eclipse-temurin:11-jre as builder
WORKDIR application
CMD tar -xvf build/distributions/myddns-1.0-SNAPSHOT.tar ./
CMD mv myddns-1.0-SNAPSHOT/bin ./bin
CMD mv myddns-1.0-SNAPSHOT/lib ./lib
CMD chmod +x ./bin/myddns
ENTRYPOINT bin/myddns

