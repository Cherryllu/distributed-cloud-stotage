import java.net.*;
import java.io.*;
import java.util.*;

/**
 * 提供线程池，面向更多的客户端操作
 */
public class AdvancedSupport implements IOStrategy {
	private ArrayList threads = new ArrayList();
	private final int INIT_THREADS = 10;
	private final int MAX_THREADS = 100;
	private IOStrategy ios = null;

	public AdvancedSupport(IOStrategy ios) {
		this.ios = ios;

		for (int i = 0; i < INIT_THREADS; i++) {
			IOThread t = new IOThread(ios);
			t.start();
			threads.add(t);
		}
		try {
			Thread.sleep(300);
		} catch (Exception e) {
		}
	}

	public void service(Socket socket,FileServer fs) {
		IOThread t = null;
		boolean found = false;
		for (int i = 0; i < threads.size(); i++) {
			t = (IOThread) threads.get(i);
			if (t.isIdle()) {
				found = true;
				break;
			}
		}
		if (!found) {
			t = new IOThread(ios);
			t.start();
			try {
				Thread.sleep(30);
			} catch (Exception e) {
			}
			threads.add(t);
		}

		t.setSocket(socket,fs);
	}
}

class IOThread extends Thread {
	private Socket socket = null;
	private IOStrategy ios = null;
	private FileServer fs = null;

	public IOThread(IOStrategy ios) {
		this.ios = ios;
	}

	public boolean isIdle() {
		return socket == null;
	}

	public synchronized void setSocket(Socket socket,FileServer fs) {
		this.socket = socket;
		this.fs = fs;
		notify();
	}

	public synchronized void run() {
		while (true) {
			try {
				wait();
				ios.service(socket,fs);
				socket = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
};
