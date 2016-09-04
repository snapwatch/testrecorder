package net.amygdalum.testrecorder;

import java.util.concurrent.ThreadFactory;

public class TestrecorderThreadFactory implements ThreadFactory {

	public static final ThreadGroup RECORDING = new ThreadGroup("RECORDING");
	
	private String name;

	public TestrecorderThreadFactory(String name) {
		this.name = name;
	}

	@Override
	public Thread newThread(Runnable runnable) {
		Thread thread = new Thread(RECORDING, runnable, name, 0);
		thread.setDaemon(true);
		return thread;
	}

}
