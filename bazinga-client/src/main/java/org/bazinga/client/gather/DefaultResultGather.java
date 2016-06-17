package org.bazinga.client.gather;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.bazinga.common.message.Status.CLIENT_TIMEOUT;
import static org.bazinga.common.message.Status.OK;
import static org.bazinga.common.message.Status.SERVER_TIMEOUT;
import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.bazinga.client.comsumer.DefaultConsumerRegistry;
import org.bazinga.common.exception.RemoteException;
import org.bazinga.common.exception.TimeoutException;
import org.bazinga.common.message.Request;
import org.bazinga.common.message.Response;
import org.bazinga.common.message.ResultMessageWrapper;
import org.bazinga.common.message.Status;
import org.bazinga.common.utils.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 结果聚集地
 * 远程端的结果聚集地
 *
 */
public class DefaultResultGather {
	
	protected static final Logger logger = LoggerFactory.getLogger(DefaultConsumerRegistry.class);
	
	private static final ConcurrentMap<Long, DefaultResultGather> resultsGathers = new ConcurrentHashMap<Long, DefaultResultGather>();
	
	private final ReentrantLock lock = new ReentrantLock();
	
    private final Condition doneCondition = lock.newCondition();
    
    public static final long DEFAULT_TIMEOUT =  5 * 1000;
    
    private final long invokeId;
    private final Channel channel;
    private final Request request;
    private final long timeoutMillis;
    private final long startTimestamp = SystemClock.millisClock().now();
    
    private volatile long sentTimestamp;
    private volatile Response response;
    
    public DefaultResultGather(Channel channel, Request request) {
    	this(channel, request, DEFAULT_TIMEOUT);
    }
    
    public DefaultResultGather(Channel channel, Request request, long timeoutMillis) {
    	
    	invokeId = request.invokeId();
        this.channel = channel;
        this.request = request;
        this.timeoutMillis = timeoutMillis > 0 ? timeoutMillis : DEFAULT_TIMEOUT;
        
        resultsGathers.put(invokeId, this);
	}
    
    public static boolean received(Channel channel, Response response) {
    	
    	logger.warn("这里执行了~", response, channel);
    	
    	long invokeId = response.id();
    	DefaultResultGather defaultResultGather = resultsGathers.remove(invokeId);
    	
    	if (defaultResultGather == null) {
            logger.warn("A timeout response [{}] finally returned on {}.", response, channel);
            return false;
        }
    	defaultResultGather.doReceived(response);
        return true;
    	
	}

    private void doReceived(Response response) {
    	
    	this.response = response;
    	
    	final ReentrantLock _lock = lock;
        _lock.lock();
        try {
            doneCondition.signal();
        } finally {
            _lock.unlock();
        }
	}

	/**
     * 获取
     * @return
     */
	public Object getResult() throws Throwable {
		if (!isDone()) {
            long start = System.nanoTime();
            final ReentrantLock _lock = lock;
            _lock.lock();
            try {
                while (!isDone()) {
                    doneCondition.await(timeoutMillis, MILLISECONDS);

                    if (isDone() || (System.nanoTime() - start) > MILLISECONDS.toNanos(timeoutMillis)) {
                        break;
                    }
                }
            } finally {
                _lock.unlock();
            }

            if (!isDone()) {
                throw new TimeoutException(channel.remoteAddress(), sentTimestamp > 0 ? SERVER_TIMEOUT : CLIENT_TIMEOUT);
            }
        }
        return resultFromResponse();
	}

	private Object resultFromResponse() {
		final Response _response = this.response;
        byte status = _response.status();
        if (status == OK.value()) {
        	ResultMessageWrapper wrapper = _response.result();
            return wrapper.getResult();
        }

        throw new RemoteException(_response.toString(), channel.remoteAddress());
	}

	private boolean isDone() {
		return response != null;
	}
	
	private static class TimeoutScanner implements Runnable {
		
		public void run() {
			for (;;) {
				try {
					for(DefaultResultGather defaultResultGather:resultsGathers.values()){
						if(null == defaultResultGather || defaultResultGather.isDone()){
							continue;
						}
						if (SystemClock.millisClock().now() - defaultResultGather.startTimestamp > defaultResultGather.timeoutMillis) {
                            processingTimeoutFuture(defaultResultGather);
                        }
					}
				} catch (Exception e) {
				}
			}
		}

		private void processingTimeoutFuture(DefaultResultGather defaultResultGather) {
			ResultMessageWrapper result = new ResultMessageWrapper();
            Status status = defaultResultGather.sentTimestamp > 0 ? SERVER_TIMEOUT : CLIENT_TIMEOUT;
            result.setError(new TimeoutException(defaultResultGather.channel.remoteAddress(), status));

            Response r = Response.newInstance(defaultResultGather.invokeId, status, result);
            DefaultResultGather.received(defaultResultGather.channel, r);
		}
		
	}
	
//	static {
//        Thread t = new Thread(new TimeoutScanner(), "timeout.scanner");
//        t.setDaemon(true);
//        t.start();
//    }

}
