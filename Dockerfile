FROM java:8-jdk-alpine
MAINTAINER Franklyn Ogbonna

COPY ./target/card-scheme-producer-0.0.1-SNAPSHOT.jar /usr/app/

WORKDIR /usr/app

RUN sh -c 'touch card-scheme-producer-0.0.1-SNAPSHOT.jar'

ENTRYPOINT ["java","-jar","card-scheme-producer-0.0.1-SNAPSHOT.jar"]