# cute-server

## introduction
cute-server is a tiny http server, based on netty, that can embedded in your java application.
you can use annotations to define http api and implement logic by your own code. 

## usage

### create a mapping
```java
public class SimpleMapping {

    @UriMapping("/echo")
    public String echo() {
        return "echo on";
    }

    @UriMapping("/hello")
    public String hello(@UriVariable("name") String name, @UriVariable("age") String age) {
        String date= new Date().toString();
        String out = String.format("hello name=%s age=%s date=%s.", name, age, date);
        return out;
    }
}
```

### bind mapping and start server
```
CuteServer cuteServer 
        = new CuteServer.Builder()
                .withPort(port)
                .withHttpRequestProcessor(new UriMappingProcessor().withMapping(new SimpleMapping()))
                .build();
cuteServer.start();
```

## deploy a demo to docker

### compile
mvn clean package

### write a docker build file
```
# replace with a openjdk image
FROM registry.cn-hangzhou.aliyuncs.com/zmy-repo/openjdk-jre:openjdk-17-jre

ADD target/cute-server-1.0-SNAPSHOT.jar /app.jar
ADD target/libs /libs

ENTRYPOINT ["java", "-Xloggc:/var/gc.log", "-jar","/app.jar"]

MAINTAINER zhaomingyu
```

### then docker it
```shell
docker build -t myz/cute-server:1.0 .
docker run --name=cute-server myz/cute-server:1.0
```

### fire a request
```shell
curl http://<ip-of-container>:9091/
```