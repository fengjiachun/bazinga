# bazinga

简单的RPC Demo

模仿[jupiter](https://github.com/fengjiachun/Jupiter)

知识点总结：

##1) netty知识点

 [NIO概念模型](http://blog.csdn.net/linuu/article/details/51836281) 

 [NIO学习](http://ifeve.com/java-nio-all/)
 
 [selector源码解析](http://zhhphappy.iteye.com/blog/2032893)


##2) 设计模式



##3) metrics性能测试工具

  提供者端提供请求性能分析报告，使用metrics

  详细介绍查看：

   [介绍metircs1](http://www.cnblogs.com/nexiyi/p/metrics_sample_1.html)

   [介绍metircs2](http://www.cnblogs.com/nexiyi/p/metrics_sample_2.html)


## 自测时遇到的坑或者bug

① 异常： Reading from a byte array threw an IOException


原因：序列化传入的对象是A,反序列化要求转换的java对象是B,A序列化的bytes无法匹配B,protostuff中的ProtostuffIOUtil.mergeFrom(bytes, message, schema)方法报错

## 没有解决的问题

 client消费端的代码存在设计问题，因为client端有两个职责，一是去连接registry，二是根据registry返回过来的服务提供地址去连接服务提供者，所以他们都需要去继承NettyConnector
 ，所以这边DefaultConsumer继承与DefaultConsumerRegistry这种设计无法完成，无法完成DefaultConsumer继承NettyConnector来简化Netty的初始化工作


