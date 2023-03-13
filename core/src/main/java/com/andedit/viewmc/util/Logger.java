package com.andedit.viewmc.util;

import static com.badlogic.gdx.Gdx.app;

public class Logger {
	
	public static final int NONE = 0;
	public static final int ERROR = 1;
	public static final int INFO = 2;
	public static final int DEBUG = 3;
	
	private static final int FORCE_LEVEL = NONE;
	
	public final String tag;
	public final int level;
	
	public Logger(Class<?> type, int level) {
		this(type.getSimpleName(), level);
	}
	
	public Logger(String tag, int level) {
		this.tag = tag;
		this.level = level;
	}
	
	public void debug(Object message) {
		if (Math.max(level, FORCE_LEVEL) >= DEBUG) app.debug(tag, String.valueOf(message));
	}

	public void debug(Object message, Exception exception) {
		if (Math.max(level, FORCE_LEVEL) >= DEBUG) app.debug(tag, String.valueOf(message), exception);
	}

	public void info(Object message) {
		if (Math.max(level, FORCE_LEVEL) >= INFO) app.log(tag, String.valueOf(message));
	}

	public void info(Object message, Exception exception) {
		if (Math.max(level, FORCE_LEVEL) >= INFO) app.log(tag, String.valueOf(message), exception);
	}

	public void error(Object message) {
		if (Math.max(level, FORCE_LEVEL) >= ERROR) app.error(tag, String.valueOf(message));
	}

	public void error(Object message, Throwable exception) {
		if (Math.max(level, FORCE_LEVEL) >= ERROR) app.error(tag, String.valueOf(message), exception);
	}
}
