FROM frolvlad/alpine-oraclejdk8:slim
MAINTAINER Librairy <librairy.framework@gmail.com>

RUN apk --update add git tar bash

ENV LIBRAIRY_HOME /librairy

VOLUME /tmp
VOLUME /librairy

ADD waitFor.sh /
RUN chmod +x /waitFor.sh

ADD annotator-tokenizer.jar app.jar

RUN sh -c 'touch /app.jar'

ENTRYPOINT ["java","-jar","/app.jar"]