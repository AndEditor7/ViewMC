package com.andedit.viewmc.lwjgl3;

import java.io.PrintStream;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics.Lwjgl3DisplayMode;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.HdpiMode;

public class HackedLwjglAppConfig extends Lwjgl3ApplicationConfiguration {
	
	int audioDeviceSimultaneousSources = 16;
	int audioDeviceBufferSize = 512;
	int audioDeviceBufferCount = 9;

	GLEmulation glEmulation = GLEmulation.GL20;
	int gles30ContextMajorVersion = 3;
	int gles30ContextMinorVersion = 2;

	int r = 8, g = 8, b = 8, a = 8;
	int depth = 16, stencil = 0;
	int samples = 0;
	boolean transparentFramebuffer;
	
	boolean debug = false;
	PrintStream debugStream = System.err;
	HdpiMode hdpiMode = HdpiMode.Logical;
	String title;
	
	int windowX = -1;
	int windowY = -1;
	int windowWidth = 640;
	int windowHeight = 480;
	int windowMinWidth = -1, windowMinHeight = -1, windowMaxWidth = -1, windowMaxHeight = -1;
	boolean windowResizable = true;
	boolean windowDecorated = true;
	boolean windowMaximized = false;
	boolean autoIconify = true;
	boolean initialVisible = true;
	boolean vSyncEnabled = true;
	Color initialBackgroundColor = Color.BLACK;
	Lwjgl3Graphics.Lwjgl3Monitor maximizedMonitor;
	FileType windowIconFileType;
	String[] windowIconPaths;
	Lwjgl3DisplayMode fullscreenMode;
	
	public void setInitialVisible (boolean visibility) {
		super.setInitialVisible(visibility);
		this.initialVisible = visibility;
	}

	@Override
	public void setOpenGLEmulation(GLEmulation glVersion, int gles3MajorVersion, int gles3MinorVersion) {
		super.setOpenGLEmulation(glVersion, gles3MajorVersion, gles3MinorVersion);
		this.glEmulation = glVersion;
		this.gles30ContextMajorVersion = gles3MajorVersion;
		this.gles30ContextMinorVersion = gles3MinorVersion;
	}

	@Override
	public void setBackBufferConfig(int r, int g, int b, int a, int depth, int stencil, int samples) {
		super.setBackBufferConfig(r, g, b, a, depth, stencil, samples);
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		this.depth = depth;
		this.stencil = stencil;
		this.samples = samples;
	}

	@Override
	public void setTransparentFramebuffer(boolean transparentFramebuffer) {
		super.setTransparentFramebuffer(transparentFramebuffer);
		this.transparentFramebuffer = transparentFramebuffer;
	}

	@Override
	public void setForegroundFPS(int fps) {
		super.setForegroundFPS(0);
	}

	@Override
	public void setHdpiMode(HdpiMode mode) {
		super.setHdpiMode(mode);
		this.hdpiMode = mode;
	}

	@Override
	public void enableGLDebugOutput(boolean enable, PrintStream debugOutputStream) {
		super.enableGLDebugOutput(enable, debugOutputStream);
		debug = enable;
		debugStream = debugOutputStream;
	}

	@Override
	public void setWindowedMode(int width, int height) {
		super.setWindowedMode(width, height);
		this.windowWidth = width;
		this.windowHeight = height;
	}

	@Override
	public void setResizable(boolean resizable) {
		super.setResizable(resizable);
		this.windowResizable = resizable;
	}

	@Override
	public void setDecorated(boolean decorated) {
		super.setDecorated(decorated);
		this.windowDecorated = decorated;
	}

	@Override
	public void setMaximized(boolean maximized) {
		super.setMaximized(maximized);
		this.windowMaximized = maximized;
	}

	@Override
	public void setMaximizedMonitor(Monitor monitor) {
		super.setMaximizedMonitor(monitor);
		this.maximizedMonitor = (Lwjgl3Graphics.Lwjgl3Monitor)monitor;
	}

	@Override
	public void setAutoIconify(boolean autoIconify) {
		super.setAutoIconify(autoIconify);
		this.autoIconify = autoIconify;
	}

	@Override
	public void setWindowPosition(int x, int y) {
		super.setWindowPosition(x, y);
		windowX = x;
		windowY = y;
	}

	@Override
	public void setWindowSizeLimits(int minWidth, int minHeight, int maxWidth, int maxHeight) {
		super.setWindowSizeLimits(minWidth, minHeight, maxWidth, maxHeight);
		windowMinWidth = minWidth;
		windowMinHeight = minHeight;
		windowMaxWidth = maxWidth;
		windowMaxHeight = maxHeight;
	}

	@Override
	public void setWindowIcon(FileType fileType, String... filePaths) {
		super.setWindowIcon(fileType, filePaths);
		windowIconFileType = fileType;
		windowIconPaths = filePaths;
	}

	@Override
	public void setFullscreenMode(DisplayMode mode) {
		super.setFullscreenMode(mode);
		this.fullscreenMode = (Lwjgl3DisplayMode)mode;
	}

	@Override
	public void setTitle(String title) {
		super.setTitle(title);
		this.title = title;
	}

	@Override
	public void setInitialBackgroundColor(Color color) {
		super.setInitialBackgroundColor(color);
		initialBackgroundColor = color;
	}

	@Override
	public void useVsync(boolean vsync) {
		super.useVsync(vsync);
		this.vSyncEnabled = vsync;
	}
}
