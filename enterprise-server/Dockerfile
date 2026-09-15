# 与项目 JDK 17 基线一致（见根 pom.xml 的 java.version=17）
FROM eclipse-temurin:17-jre

WORKDIR /enterprise-server

# 构建产物名由 enterprise-server/pom.xml 的 finalName 决定：enterprise-server.jar
COPY ./target/enterprise-server.jar app.jar

ENV TZ=Asia/Shanghai
# 可通过 docker run -e "JAVA_OPTS=..." 覆盖
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom"

ENV ARGS=""

EXPOSE 48080

CMD java ${JAVA_OPTS} -jar app.jar $ARGS
