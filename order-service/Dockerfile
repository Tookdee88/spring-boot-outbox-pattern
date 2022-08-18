# Use official base image of Java Runtime
FROM eclipse-temurin:17

RUN apt-get install -y libfontconfig

# Set volume point to /tmp
VOLUME /tmp

# Make port 9081 and 5555 available to the world outside container
EXPOSE 9081 5555

# Set application's JAR file
ARG JAR_FILE=order-service-0.0.1-SNAPSHOT.jar

# Add the application's JAR file to the container
ADD order-service/target/${JAR_FILE} app.jar

# Run the JAR file
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dcom.sun.management.jmxremote=true", "-Dcom.sun.management.jmxremote.local.only=false", "-Dcom.sun.management.jmxremote.authenticate=false", "-Dcom.sun.management.jmxremote.ssl=false", "-Djava.rmi.server.hostname=localhost", "-Dcom.sun.management.jmxremote.port=5555", "-Dcom.sun.management.jmxremote.rmi.port=5555", "-jar", "/app.jar"]