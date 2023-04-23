package com.andedit.viewmc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Statics {
	
	public static ExecutorService chunkExe, meshExe, theadExe;
	
	static void init() {
		chunkExe = Executors.newFixedThreadPool(2, new DaemonThreadFactory("Chunk Loader"));
//		meshExe = Executors.newFixedThreadPool(2, new DaemonThreadFactory("Mesh Builder"));
		meshExe = Executors.newSingleThreadExecutor(new DaemonThreadFactory("Mesh Builder"));
		theadExe = Executors.newSingleThreadExecutor(new DaemonThreadFactory("Thead"));
	}
	
	static void dispose() {
		shutdown(chunkExe);
		shutdown(meshExe);
		shutdown(theadExe);
	}
	
	private static void shutdown(ExecutorService service) {
		service.shutdown();
		try {
			service.awaitTermination(5, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
     * The default thread factory.
     */
    private static class DaemonThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String name;

        DaemonThreadFactory(String name) {
            this.name = name;
        }

        public Thread newThread(Runnable runnable) {
            var thread = new Thread(runnable, name + " - " + threadNumber.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    }
}
