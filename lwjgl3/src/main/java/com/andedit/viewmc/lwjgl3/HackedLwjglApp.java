package com.andedit.viewmc.lwjgl3;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;

import com.andedit.viewmc.Main;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationBase;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration.GLEmulation;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Clipboard;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Net;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SharedLibraryLoader;

/**
 * A hacked lwjgl app that allows freedom to modify whatever we want instead of
 * restrictive backend by libGDX developers
 */
public class HackedLwjglApp extends Lwjgl3Application {
	
	public static HackedLwjglAppConfig config;

	private Files files;
	private Net net;
	private Lwjgl3Clipboard clipboard;
	
	private static Callback glDebugCallback;

	public HackedLwjglApp(ApplicationListener listener) {
		super(listener);
	}

	public HackedLwjglApp(ApplicationListener listener, Lwjgl3ApplicationConfiguration config) {
		super(listener, config);
	}

	@Override
	protected Files createFiles() {
		
		files = Gdx.files = new Lwjgl3Files();
		net = Gdx.net = new Lwjgl3Net(config);
		clipboard = new Lwjgl3Clipboard();
		
		long windowHandle = createGlfwWindow(0);
		var window = Reflections.newObj(Lwjgl3Window.class, Reflections.array(Main.main, config, this), Reflections.array(ApplicationListener.class, Lwjgl3ApplicationConfiguration.class, Lwjgl3ApplicationBase.class));
		Reflections.invoke(window, "create", Reflections.array(windowHandle), Reflections.array(long.class));
		
		var graphics = (Graphics)Reflections.getFeild(window, Lwjgl3Window.class, "graphics");
		
		if (config.glEmulation == GLEmulation.ANGLE_GLES20) {
//			var gl = new Lwjgl3GLES30();
//			graphics.setGL20(gl);
//			graphics.setGL30(gl);
		}

		if (config.glEmulation == Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20) {
			Reflections.invoke(Lwjgl3Application.class, "postLoadANGLE");
		}
		
		window.setVisible(config.initialVisible);
		((Array<Lwjgl3Window>)Reflections.getFeild(this, Lwjgl3Application.class, "windows")).add(window);

		try {
			loop();
			cleanupWindows();
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			cleanup();
		}

		System.exit(0);
		throw new RuntimeException("Exit");
	}
	
	@Override
	protected void loop() {
		super.loop();
	}

	@Override
	public Files getFiles() {
		return files;
	}

	@Override
	public Net getNet() {
		return net;
	}

	@Override
	public Clipboard getClipboard() {
		return clipboard;
	}
	
	@Override
	protected void cleanup() {
		if (glDebugCallback != null) {
			glDebugCallback.free();
			glDebugCallback = null;
		}
		super.cleanup();
	}
	
	static long createGlfwWindow (long sharedContextWindow) {
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, config.windowResizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_MAXIMIZED, config.windowMaximized ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_AUTO_ICONIFY, config.autoIconify ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);

		GLFW.glfwWindowHint(GLFW.GLFW_RED_BITS, config.r);
		GLFW.glfwWindowHint(GLFW.GLFW_GREEN_BITS, config.g);
		GLFW.glfwWindowHint(GLFW.GLFW_BLUE_BITS, config.b);
		GLFW.glfwWindowHint(GLFW.GLFW_ALPHA_BITS, config.a);
		GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, config.stencil);
		GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, config.depth);
		GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, config.samples);

		if (config.glEmulation == Lwjgl3ApplicationConfiguration.GLEmulation.GL30) {
			GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, config.gles30ContextMajorVersion);
			GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, config.gles30ContextMinorVersion);
			if (SharedLibraryLoader.isMac) {
				// hints mandatory on OS X for GL 3.2+ context creation, but fail on Windows if the
				// WGL_ARB_create_context extension is not available
				// see: http://www.glfw.org/docs/latest/compat.html
				GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
				GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
			}
		} else {
			if (config.glEmulation == Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20) {
				GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_CREATION_API, GLFW.GLFW_EGL_CONTEXT_API);
				GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_OPENGL_ES_API);
				GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 2);
				GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 0);
			}
		}

		if (config.transparentFramebuffer) {
			GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, GLFW.GLFW_TRUE);
		}

		if (config.debug) {
			GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);
		}

		long windowHandle = 0;

		if (config.fullscreenMode != null) {
			GLFW.glfwWindowHint(GLFW.GLFW_REFRESH_RATE, config.fullscreenMode.refreshRate);
			windowHandle = GLFW.glfwCreateWindow(config.fullscreenMode.width, config.fullscreenMode.height, config.title,
				config.fullscreenMode.getMonitor(), sharedContextWindow);
		} else {
			GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, config.windowDecorated ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
			windowHandle = GLFW.glfwCreateWindow(config.windowWidth, config.windowHeight, config.title, 0, sharedContextWindow);
		}
		if (windowHandle == 0) {
			throw new GdxRuntimeException("Couldn't create window");
		}
		setSizeLimits(windowHandle, config.windowMinWidth, config.windowMinHeight, config.windowMaxWidth,
			config.windowMaxHeight);
		if (config.fullscreenMode == null) {
			if (config.windowX == -1 && config.windowY == -1) {
				int windowWidth = Math.max(config.windowWidth, config.windowMinWidth);
				int windowHeight = Math.max(config.windowHeight, config.windowMinHeight);
				if (config.windowMaxWidth > -1) windowWidth = Math.min(windowWidth, config.windowMaxWidth);
				if (config.windowMaxHeight > -1) windowHeight = Math.min(windowHeight, config.windowMaxHeight);

				long monitorHandle = GLFW.glfwGetPrimaryMonitor();
				if (config.windowMaximized && config.maximizedMonitor != null) {
					monitorHandle = config.maximizedMonitor.getMonitorHandle();
				}

				IntBuffer areaXPos = BufferUtils.createIntBuffer(1);
				IntBuffer areaYPos = BufferUtils.createIntBuffer(1);
				IntBuffer areaWidth = BufferUtils.createIntBuffer(1);
				IntBuffer areaHeight = BufferUtils.createIntBuffer(1);
				GLFW.glfwGetMonitorWorkarea(monitorHandle, areaXPos, areaYPos, areaWidth, areaHeight);

				GLFW.glfwSetWindowPos(windowHandle, Math.max(0, areaXPos.get(0) + areaWidth.get(0) / 2 - windowWidth / 2),
					Math.max(0, areaYPos.get(0) + areaHeight.get(0) / 2 - windowHeight / 2));
			} else {
				GLFW.glfwSetWindowPos(windowHandle, config.windowX, config.windowY);
			}

			if (config.windowMaximized) {
				GLFW.glfwMaximizeWindow(windowHandle);
			}
		}
		if (config.windowIconPaths != null) {
			Reflections.invoke(Lwjgl3Window.class, "setIcon", Reflections.array(windowHandle, config.windowIconPaths, config.windowIconFileType), Reflections.array(long.class, String[].class, FileType.class));
		}
		GLFW.glfwMakeContextCurrent(windowHandle);
		GLFW.glfwSwapInterval(config.vSyncEnabled ? 1 : 0);
		if (config.glEmulation == Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20) {
			try {
				Class gles = Class.forName("org.lwjgl.opengles.GLES");
				gles.getMethod("createCapabilities").invoke(gles);
			} catch (Throwable e) {
				throw new GdxRuntimeException("Couldn't initialize GLES", e);
			}
		} else {
			GL.createCapabilities();
		}

		Reflections.invoke(Lwjgl3Application.class, "initiateGL", Reflections.array(config.glEmulation == Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20), Reflections.array(boolean.class));

		if (config.debug) {
			glDebugCallback = GLUtil.setupDebugMessageCallback(config.debugStream);
			setGLDebugMessageControl(GLDebugMessageSeverity.NOTIFICATION, false);
		}

		return windowHandle;
	}
	
	static void setSizeLimits (long windowHandle, int minWidth, int minHeight, int maxWidth, int maxHeight) {
		GLFW.glfwSetWindowSizeLimits(windowHandle, minWidth > -1 ? minWidth : GLFW.GLFW_DONT_CARE,
			minHeight > -1 ? minHeight : GLFW.GLFW_DONT_CARE, maxWidth > -1 ? maxWidth : GLFW.GLFW_DONT_CARE,
			maxHeight > -1 ? maxHeight : GLFW.GLFW_DONT_CARE);
	}
}
