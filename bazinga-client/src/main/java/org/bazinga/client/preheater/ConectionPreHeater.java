package org.bazinga.client.preheater;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.bazinga.common.exception.ConnectFailedException;
import org.bazinga.common.logger.InternalLogger;
import org.bazinga.common.logger.InternalLoggerFactory;
import org.bazinga.common.utils.SystemClock;

/**
 * 链接越热工具，模仿getresult时的promise的方式
 * @author BazingaLyn
 * @time 2016年6月14日
 */
public class ConectionPreHeater {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(ConectionPreHeater.class);

	private static final ConcurrentMap<String, ConectionPreHeater> preHeaterGather = new ConcurrentHashMap<String, ConectionPreHeater>();

	private final ReentrantLock lock = new ReentrantLock();

	private final long startTimestamp = SystemClock.millisClock().now();

	private final Condition doneCondition = lock.newCondition();

	private final String serviceName;

	public static final long DEFAULT_TIMEOUT = 5 * 1000;

	private final long timeoutMillis;

	private Object readyState;

	public ConectionPreHeater(String serviceName, long timeoutMillis) {
		this.serviceName = serviceName;
		this.timeoutMillis = timeoutMillis > 0 ? timeoutMillis : DEFAULT_TIMEOUT;

		preHeaterGather.put(this.serviceName, this);
	}

	public static boolean finishPreConnection(String serviceName) {

		logger.info("serivceName {} link to provider 预热成功~", serviceName);

		ConectionPreHeater conectionPreHeater = preHeaterGather.remove(serviceName);

		if (conectionPreHeater == null) {
			logger.warn("A timeout serviceName {} link provider.", serviceName);
			return false;
		}
		conectionPreHeater.doFinishPreHeat(serviceName);
		return true;
	}

	private void doFinishPreHeat(String serviceName) {

		this.readyState = new Object();

		final ReentrantLock _lock = lock;
		_lock.lock();
		try {
			doneCondition.signal();
		} finally {
			_lock.unlock();
		}

	}

	public Object getPreHeatReady() throws Throwable {
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
				throw new ConnectFailedException("link time out");
			}
		}
		return this.readyState;
	}

	private boolean isDone() {
		return readyState != null;
	}

	private static class TimeoutScanner implements Runnable {

		public void run() {
			for (;;) {
				try {
					for (ConectionPreHeater conectionPreHeater : preHeaterGather.values()) {
						if (null == conectionPreHeater || conectionPreHeater.isDone()) {
							continue;
						}
						if (SystemClock.millisClock().now() - conectionPreHeater.startTimestamp > conectionPreHeater.timeoutMillis) {
							throw new ConnectFailedException("link time out");
						}
					}
				} catch (Exception e) {
				}
			}
		}

	}

	static {
		Thread t = new Thread(new TimeoutScanner(), "timeout.scanner");
		t.setDaemon(true);
		t.start();
	}

}
