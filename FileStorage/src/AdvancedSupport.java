import java.net.*;
import java.io.*;
import java.util.*;

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

	public void service(Socket socket) {
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

		t.setSocket(socket);
	}
}

class IOThread extends Thread {
	private Socket socket = null;
	private IOStrategy ios = null;

	public IOThread(IOStrategy ios) {
		this.ios = ios;
	}

	public boolean isIdle() {
		return socket == null;
	}

	public synchronized void setSocket(Socket socket) {
		this.socket = socket;
		notify();
	}

	public synchronized void run() {
		while (true) {
			try {
				wait();
				ios.service(socket);
				socket = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
};
