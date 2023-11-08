# 该镜像需要依赖的基础镜像
FROM registry.cn-hangzhou.aliyuncs.com/zmy-repo/openjdk-17-jre:openjdk-17-jre

# 将当前目录下的jar包复制到docker容器的/目录下
ADD target/cute-server-1.0-SNAPSHOT.jar /app.jar
ADD target/libs /libs

# 指定docker容器启动时运行jar包
ENTRYPOINT ["java", "-Xloggc:/var/gc.log", "-jar","/app.jar"]
# 指定维护者的名字
MAINTAINER zhaomingyu
