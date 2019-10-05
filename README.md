# `Redis-MQ`

## 一、是什么

一个简单，轻量，基于`Redis`的消息队列实现。

## 二、有什么用

不依赖于任何框架，不仅可以应用于`Spring`，自行研发的其他`IOC`框架也可以无缝衔接。可以满足想要使用消息队列的场景，但是业务又不是特别大，不想再引入主流`MQ`时的替代品。
<br>使用`Redis-MQ`需要引入日志包的实现依赖。

## 三、怎么用

### 1、导入依赖

```xml
<dependency>
    <groupId>com.mihone</groupId>
    <artifactId>redis-mq</artifactId>
    <version>0.1</version>
</dependency>
```

**注意，`jedis`必须是3.0版本以上**

### 2、配置`Redis`

```properties
redis.url=192.168.3.17
redis.port=6379
redis.password=newpass
```

### 3、启动类添加代码

```java
RedisMQ.start(bean-> BeanUtils.getBean(bean),RedisMQApplication.class);
```

```java
RedisMQ.start(Function<Class, R> beanProvider, Class<T> clazz)
```

**`beanProvider`**：根据给定`Class`提供该`Class`实例化对象的方法。

**`clazz`**：启动类的`Class`对象。

### 4、添加监听注解

```java
@Queue("queueName1")
public void listen(Message message){
    //xxxx...
}

@Queue("queueName2")
public void listen(Bean bean){
    //xxxx...
}
```

监听方法的参数有两种：

**`Message`**：消息对象，包括`id`、时间戳、消息体的类名、消息内容。

**`Bean`**：消息体对象