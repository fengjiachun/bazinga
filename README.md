# bazinga

简单的RPC Demo

模仿jupiter @https://github.com/fengjiachun/Jupiter

知识点总结：


自测时遇到的坑或者bug

① 异常： Reading from a byte array threw an IOException


原因：序列化传入的对象是A,反序列化要求转换的java对象是B,A序列化的bytes无法匹配B,protostuff中的ProtostuffIOUtil.mergeFrom(bytes, message, schema)方法报错
