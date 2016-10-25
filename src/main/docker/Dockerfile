FROM frolvlad/alpine-oraclejdk8:slim
MAINTAINER Librairy <librairy.framework@gmail.com>

ENV LIBRAIRY_HOME /librairy

VOLUME /tmp

ADD waitFor.sh /
RUN chmod +x /waitFor.sh

ADD annotator-tokenizer.jar app.jar

VOLUME /librairy

RUN sh -c 'touch /app.jar'

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]