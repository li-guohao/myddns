FROM eclipse-temurin:17-jre as builder
MAINTAINER li-guohao
ENV APPDIR=/app/myddns
WORKDIR $APPDIR
CMD $APPDIR/gradlew clean distTar
CMD tar -xvf $APPDIR/build/distributions/myddns-1.0-SNAPSHOT.tar $APPDIR/
CMD chmod +x $APPDIR/bin/myddns
ENTRYPOINT $APPDIR/bin/myddns

