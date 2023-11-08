# 该镜像需要依赖的基础镜像
FROM openjdk-17-jre:openjdk-17-jre
# 将当前目录下的jar包复制到docker容器的/目录下
ADD target/cute-server-1.0-SNAPSHOT-jar-with-dependencies.jar /app.jar
# 指定docker容器启动时运行jar包
ENTRYPOINT ["java", "-Xloggc:/var/logs/gc.log", "-jar","/app.jar"]
# 指定维护者的名字
MAINTAINER zhaomingyu
