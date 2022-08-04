本篇文章使用一个简单的示例来演示Spring Cloud Gateway的使用方法。

## 网关的应用场景

网关是所有微服务的门户，我总结了一些网关的常用应用场景。

![网关的常用应用场景](https://raw.githubusercontent.com/wanggch/oss/master/PicGo%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20220730080534.jpg)

## Spring Cloud Gateway中几个比较重要的概念

- 路由（Route）：它是网关的基本组件，由ID、目标URI、Predicate集合、Filter集合组成。
- 断言（Predicate）：参照Java8的新特性Predicate，允许开发人员匹配HTTP请求中的任何内容，比如头或参数。
- 过滤器（Filter）：可以在返回请求之前或之后修改请求和响应的内容。

### 断言

断言是Java 8提供的一个函数式接口，它允许接收一个参数并返回一个布尔值，可以用于条件过滤、请求参数的校验。

Spring Cloud Gateway 默认提供了许多断言，这些断言的源码在`org.springframework.cloud.gateway.handler.predicate`包中。这些断言会分别匹配 HTTP 请求的不同属性，并且多个断言可以通过 and 逻辑进行组合。

![image-20220730135758117](https://raw.githubusercontent.com/wanggch/oss/master/PicGoimage-20220730135758117.png)

### 过滤器

过滤器分为Pre类型的过滤器和Post类型的过滤器。

- Pre类型的过滤器在请求转发到后端微服务之前执行，在Pre类型过滤器链中可以做鉴权、限流等操作。
- Post类型的过滤器在请求执行完之后、将结果返回给客户端之前执行。

在 Spring Cloud Gateway 中内置了很多过滤器，过滤器有两种实现，分别是`GatewayFilter`和`GlobalFilter`。`GlobalFilter`会应用到所有的路由上，而`GatewayFilter`只会应用到单个路由或者一个分组的路由上。

## 环境搭建

搭建项目前需要先启动 Nacos 服务。我比较喜欢使用 Docker 安装、启动 Nacos 。如果你对这种方式感兴趣，可以参考我之前写的一篇文章[传送门](https://juejin.cn/post/7125227354728693791)。

项目结构

| 项目名称        | 端口号 | 说明                      |
| --------------- | ------ | ------------------------- |
| jasmine-cloud   | --     | 父工程                    |
| jasmine-gateway | 9010   | 网关服务，服务注册到nacos |
| jasmine-auth    | 9012   | 权限服务，服务注册到nacos |

## 创建jasmine-cloud项目

`jasmine-cloud`是一个父工程，没有代码，只有一个`pom.xml`文件。`pom.xml`文件的具体内容如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.5.RELEASE</version>
    </parent>

    <groupId>org.jasmine.cloud</groupId>
    <artifactId>jasmine-cloud</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>Hoxton.SR3</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>2.2.1.RELEASE</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

</project>
```

`Spring Cloud`、`Spring Cloud Alibaba`、`Spring Boot`三者的版本需要注意下，`Spring Cloud`、`Spring Cloud Alibaba`、`Spring Boot`三者的版本应该是有对应关系的，如果版本对应不上，可能会有意想不到的BUG。我只尝试了如下两套版本。

|        | Spring Cloud | Spring Cloud Alibaba |  Spring Boot  | 是否可行 |
| :----: | :----------: | :------------------: | :-----------: | :------: |
| 第一套 |   2020.0.0   |        2021.1        |     2.4.2     |   报错   |
| 第二套 |  Hoxton.SR3  |    2.2.1.RELEASE     | 2.2.5.RELEASE |   可行   |

最初的时候，我使用的是第一套版本。执行`curl`命令报错。我没有深究错误原因，然后就尝试了第二套版本，没有再报错。

```bash
curl http://127.0.0.1:9010/auth/hello
{"timestamp":"2022-07-30T00:51:40.182+00:00","path":"/auth/hello","status":503,"error":"Service Unavailable","message":"","requestId":"ef67c669-1"}
```

## 创建jasmine-gateway子项目

`jasmine-gateway`是网关服务，服务注册到`Nacos`，使用`Nacos`的配置管理功能实现了配置信息动态变更。如果不了解与`Nacos`整合，可以参考我之前写的一篇文章[传送门](https://juejin.cn/post/7125227354728693791)。

1. 项目依赖

```xml
<dependency>
	<groupId>com.alibaba.cloud</groupId>
	<artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
<dependency>
	<groupId>com.alibaba.cloud</groupId>
	<artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```

2. 配置文件（bootstrap.yml）

```yaml
server:
  port: 9010
spring:
  profiles:
    active: dev
  application:
    name: jasmine-gateway
  cloud:
    nacos:
      config:
        file-extension: yaml
        group: ${spring.profiles.active}
        prefix: ${spring.application.name}
        server-addr: 127.0.0.1:8848
    gateway:
      discovery:
        locator:
          enabled: true
          # 是否使用service-id的小写，默认是大写
          lower-case-service-id: true
```

3. Nacos新增配置

Data Id：jasmine-gateway.yaml

Group：dev

配置内容：

```yaml
spring:
  cloud:
    gateway:
      ## 路由
      routes:
        - id: jasmine-auth
          uri: lb://jasmine-auth
          predicates:
            - Path=/auth/**
```

上面配置中字段的含义简单说明下。

- id：自定义路由ID，保持唯一。
- uri：目标服务地址，支持普通URI及lb://应用注册服务名称，后者表示从注册中心获取集群服务地址。
- predicates：路由条件，根据匹配的结果决定是否执行该请求路由。

## 创建jasmine-auth子项目

1. 项目依赖

```xml
<dependency>
	<groupId>com.alibaba.cloud</groupId>
	<artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
<dependency>
	<groupId>com.alibaba.cloud</groupId>
	<artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

2. 配置文件（bootstrap.yml）

```yaml
server:
  port: 9012
spring:
  profiles:
    active: dev
  application:
    name: jasmine-auth
  cloud:
    nacos:
      config:
        file-extension: yaml
        group: ${spring.profiles.active}
        prefix: ${spring.application.name}
        server-addr: 127.0.0.1:8848
    gateway:
      discovery:
        locator:
          enabled: true
          # 是否使用service-id的小写，默认是大写
          lower-case-service-id: true
```

3. 控制器

一个简单的用于测试接口的控制器类。

```java
@RestController
@RequestMapping("/auth")
public class HelloController {

    @GetMapping("/hello")
    public String index() {
        return "hello world.";
    }
}
```

## 测试

1. 依次启动`jasmine-gateway`、`jasmine-auth`项目；
2. 打开命令行工具（我用的是`cmder`），输入命令：`curl http://127.0.0.1:9010/auth/hello`。



Spring Cloud Gateway夺命连环10问？：https://juejin.cn/post/7025397970916868103



