package com.andedit.viewmc.util;

import static com.badlogic.gdx.Gdx.app;

public class Logger {
	
	public static final int NONE = 0;
	public static final int ERROR = 1;
	public static final int INFO = 2;
	public static final int DEBUG = 3;
	
	public final String tag;
	
	public Logger(Class<?> type) {
		this(type.getSimpleName());
	}
	
	public Logger(String tag) {
		this.tag = tag;
	}
	
	public void debug(Object message) {
		app.debug(tag, String.valueOf(message));
	}

	public void debug(Object message, Throwable throwable) {
		app.debug(tag, String.valueOf(message), throwable);
	}

	public void info(Object message) {
		app.log(tag, String.valueOf(message));
	}

	public void info(Object message, Throwable throwable) {
		if (app.getLogLevel() >= DEBUG) {
			app.error(tag, String.valueOf(message), throwable);
		} else {
			var string = throwable.getMessage();
			app.log(tag, message + (string.indexOf('\n') == -1 ? " - " + string : ""));
		}
	}

	public void error(Object message) {
		app.error(tag, String.valueOf(message));
	}

	public void error(Object message, Throwable exception) {
		app.error(tag, String.valueOf(message), exception);
	}
}
