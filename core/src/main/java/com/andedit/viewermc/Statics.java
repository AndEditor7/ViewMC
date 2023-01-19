package com.andedit.viewermc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Statics {
	
	public static ExecutorService worldExe;
	
	static void init() {
		worldExe = Executors.newFixedThreadPool(4);
	}
	
	static void dispose() {
		worldExe.shutdown();
	}
}
