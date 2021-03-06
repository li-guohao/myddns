FROM eclipse-temurin:17-jre as builder
MAINTAINER li-guohao
ARG TAR_FAILE=build/distributions/*.tar
WORKDIR /mydns
ADD ${TAR_FAILE} /mydns
RUN ["/bin/ls", "/mydns"]
RUN ["/bin/mv", "/mydns/myddns-0.0.1", "/mydns/app"]
RUN ["/bin/ls", "/mydns/app"]
RUN ["/bin/chmod", "+x", "/mydns/app/bin/myddns"]
ENTRYPOINT ["/bin/bash", "-c", "/mydns/app/bin/myddns"]

