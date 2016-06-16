package org.dc.jdbc.cache.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Storage {
	private static Storage storage = new Storage();
	public static Storage getInstance(){
		return storage;
	}
	private BlockingQueue<String> queues = new LinkedBlockingQueue<String>();

	/**
	 * 生产
	 * 
	 * @param p
	 *            产品
	 * @throws InterruptedException
	 */
	public synchronized void push(String sqlKey) throws InterruptedException {
		if(!queues.contains(sqlKey)){
			queues.put(sqlKey);
		}
	}

	/**
	 * 消费
	 * 
	 * @return 产品
	 * @throws InterruptedException
	 */
	public String pop() throws InterruptedException {
		return queues.take();
	}
}