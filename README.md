# bazinga

简单的RPC Demo

模仿jupiter @https://github.com/fengjiachun/Jupiter

知识点总结：

1）netty知识点



2）promise设计模式



3）metrics性能测试工具

  提供者端提供请求性能分析报告，使用metrics

  详细介绍查看：

    http://www.cnblogs.com/nexiyi/p/metrics_sample_1.html

    http://www.cnblogs.com/nexiyi/p/metrics_sample_2.html


自测时遇到的坑或者bug

① 异常： Reading from a byte array threw an IOException


原因：序列化传入的对象是A,反序列化要求转换的java对象是B,A序列化的bytes无法匹配B,protostuff中的ProtostuffIOUtil.mergeFrom(bytes, message, schema)方法报错


