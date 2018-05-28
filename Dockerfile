FROM registry.i.hosaka.io/bootstrap
COPY ./target/uberjar/registrar.jar /srv/registrar.jar
WORKDIR /srv

EXPOSE 8080 8079

ENTRYPOINT /usr/bin/bootstrap /usr/bin/java -jar /srv/registrar.jar