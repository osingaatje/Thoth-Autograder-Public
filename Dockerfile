# inspired from https://www.richyhbm.co.uk/posts/kotlin-docker-spring-oh-my/

################ BUILDING FROM SCRATCH ################
## Build the project jar:
## any recent version of gradle should be fine
#FROM gradle:8.7-jdk21-alpine as builder
#USER root
#WORKDIR /builder
## copy over the files to the builder dir:
#ADD . /builder
#RUN gradle build --stacktrace --no-daemon
#
## Compile the project jar:
#FROM openjdk:21-jdk
#WORKDIR /app
#ADD ./build/libs/*.jar /app/app.jar
#CMD ["java", "-jar", "app.jar"]

WORKDIR /app
RUN echo "Thoth booted up" # no further config necessary, since we clone all files and manually build gradle outside Docker (is faster)