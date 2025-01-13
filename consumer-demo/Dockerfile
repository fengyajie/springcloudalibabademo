#使用一个基础镜像
FROM openjdk:8-jdk-alpine

#设置工作目录
WORKDIR /app

#将编译好的jar文件复制到镜像中
COPY target/consumer-demo.jar /app/consumer-demo.jar

#暴漏应用程序端口
EXPOSE 8082

#运行应用程序
CMD ["java","-jar","/app/consumer-demo.jar"]