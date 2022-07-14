FROM eclipse-temurin:17-jre as builder
MAINTAINER li-guohao
WORKDIR application
ADD build/distributions/myddns-1.0-SNAPSHOT.tar $PWD/
CMD mv myddns-1.0-SNAPSHOT/ $PWD/
CMD chmod +x $PWD/bin/myddns
ENTRYPOINT $PWD/bin/myddns

