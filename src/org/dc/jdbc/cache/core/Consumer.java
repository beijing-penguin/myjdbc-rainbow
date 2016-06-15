package org.dc.jdbc.cache.core;
public class Consumer implements Runnable {
	private Storage s = null;

	public Consumer( Storage s) {
		this.s = s;
	}

	public void run() {
		try {
			while (true) {
				System.out.println("准备消费产品.");
				String product = s.pop();
				System.out.println("已消费(" + product.toString() + ").");
				System.out.println("===============");
				Thread.sleep(500);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
