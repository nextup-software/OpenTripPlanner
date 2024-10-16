FROM maven:3.9.6-amazoncorretto-21
WORKDIR /app
ADD pom.xml .
ADD .git ./.git

RUN mvn verify clean

ADD src ./src


# Package the code into a JAR.
RUN mvn package -Dmaven.test.skip
RUN mv target/*shaded.jar otp.jar