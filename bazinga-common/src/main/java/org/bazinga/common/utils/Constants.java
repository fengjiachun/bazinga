package org.bazinga.common.utils;






/**
 * 常量池
 * @author BazingaLyn
 * @copyright fjc
 * @time
 */
public class Constants {
	
	public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
	
	public static final int PROCESSOR_CORE_NUM_WORKERS = SystemPropertyUtil.getInt("bazinga.processor.executor.core.num.workers", AVAILABLE_PROCESSORS << 1);

	/** 默认的调用超时时间为3秒 **/
    public static final long DEFAULT_TIMEOUT = SystemPropertyUtil.getInt("bazinga.reader.connection.timeout", 3 * 1000);
    
    /** 默认地无效低水位线 **/
    public static final int NO_AVAILABLE_WRITEBUFFER_LOWWATERMARK = SystemPropertyUtil.getInt("bazinga.invalid.writebuffer.lowerwatermark", -1);
    
    /** 默认地无效高水位线 **/
    public static final int NO_AVAILABLE_WRITEBUFFER_HIGHWATERMARK = SystemPropertyUtil.getInt("bazinga.invalid.writebuffer.highwatermark", -1);

    /** 链路read空闲检测 默认60秒, 60秒没读到任何数据会强制关闭连接 */
    public static final int READER_IDLE_TIME_SECONDS = SystemPropertyUtil.getInt("bazinga.reader.idle.time.seconds", 60);
    
    /** 链路write空闲检测 默认30秒, 30秒没有向链路中写入任何数据时客户端会主动向对端发送心跳 */
    public static final int WRITER_IDLE_TIME_SECONDS = SystemPropertyUtil.getInt("bazinga.writer.idle.time.seconds", 30);
    
    /** Load balancer 默认权重, 建议最大100 **/
    public static final int DEFAULT_WEIGHT = SystemPropertyUtil.getInt("bazinga.load-balancer.weight", 50);
    
    /** Suggest that the count of connections **/
    public static final int DEFAULT_CONNECTION_COUNT = SystemPropertyUtil.getInt("bazinga.suggest.connection.count", AVAILABLE_PROCESSORS);
}
