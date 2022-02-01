FROM openjdk:11-slim

ENV SBT_VERSION 1.6.1

RUN export DEBIAN_FRONTEND="noninteractive" \
    && apt-get update \
    && apt-get --quiet --yes dist-upgrade \
    && apt-get install --yes curl \
    && curl -sSLo sbt-$SBT_VERSION.deb https://repo.scala-sbt.org/scalasbt/debian/sbt-$SBT_VERSION.deb \
    && apt-get install --yes ./sbt-$SBT_VERSION.deb \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && rm sbt-$SBT_VERSION.deb

WORKDIR /app

COPY ./target/scala-2.13/schema-validator-assembly-0.1.jar /app/app.jar
COPY dockerfiles/server/entrypoint.sh /entrypoint.sh

EXPOSE 8000
ENTRYPOINT ["/entrypoint.sh"]