package com.andedit.viewmc.util;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Null;

public final class Util {

	public static float lerp(float fromValue, float toValue, float progress, float clamp) {
		final float delta = (toValue - fromValue) * progress;
		return fromValue + MathUtils.clamp(delta, -clamp, clamp);
	}

	public static boolean isGL30() {
		return Gdx.gl30 != null;
	}
	
	public static boolean isMobile() {
		return Gdx.app.getType() != ApplicationType.Desktop;
	}
	
	public static boolean isDesktop() {
		return Gdx.app.getType() == ApplicationType.Desktop;
	}
	
	public static int floor(final double x) {
		final int xi = (int)x;
		return x < xi ? xi-1 : xi;
	}
	
	public static int floor(float f) {
		return (f >= 0 ? (int) f : (int) f - 1);
	}
	
	public static int getMb() {
		long java = Gdx.app.getJavaHeap();
		if (isDesktop()) {
			return (int)(java / 1024L / 1024L);
		}
		return (int)((java + Gdx.app.getNativeHeap()) / 1024L / 1024L);
	}

	/** Returns the width of the client area in logical pixels. */
	public static int getW() {
		return Gdx.graphics.getWidth();
	}

	/** Returns the height of the client area in logical pixels. */
	public static int getH() {
		return Gdx.graphics.getHeight();
	}

	public static float modAngle(float angle) {
		float mod = angle % 360f;
		return MathUtils.clamp(mod < 0f ? mod + 360f : mod, 0f, 360f);
	}

	public static void glClear() {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
	}
	
	public static ShaderProgram newShader(String path) {
		return new ShaderProgram(Gdx.files.internal(path.concat(".vert")), Gdx.files.internal(path.concat(".frag")));
	}
	
	public static float getShade(Color color, float scl) {
		return Color.toFloatBits(color.r * scl, color.g * scl, color.b * scl, 1);
	}
	
	public static void scale(Actor actor, float scl) {
		actor.setSize(actor.getWidth()*scl, actor.getHeight()*scl);
	}
	
	public static TextureRegion flip(TextureRegion region, boolean x, boolean y) {
		TextureRegion var = new TextureRegion(region);
		var.flip(x, y);
		return var;
	}
	
	public static long hashCode(int x, int y, int z) {
        long l = (long)(x * 3129871) ^ (long)z * 116129781L ^ (long)y;
        l = l * l * 42317861L + l * 11L;
        return l >> 16;
    }
	
	public static float bilinear(float num00, float num10, float num01, float num11, float x, float y) {
		float x0 = MathUtils.lerp(num00, num10, x);
	    float x1 = MathUtils.lerp(num01, num11, x);
	    return MathUtils.lerp(x0, x1, y);
	}
	
	/** {@link Pattern} */
	public static Predicate<String> pattern(String regex) {
		return Pattern.compile(regex).asMatchPredicate();
	}

	/** Create a new change listener using java 8 lambda. */
	public static EventListener newListener(Consumer<Event> listener) {
		return new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				listener.accept(event);
			}
		};
	}

	/** Create a new change listener without Event. */
	public static EventListener newListener(Runnable runnable) {
		return new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				runnable.run();
			}
		};
	}
	
	public static ArrayList<String> split(String string, char delimiter) {
		var array = new ArrayList<String>((string.length() / 3) + 2);
		int i = 0;
		int j = string.indexOf(delimiter, 0); // first substring

		while (j >= 0) {
			array.add(string.substring(i, j));
			i = j + 1;
			j = string.indexOf(delimiter, i); // rest of substrings
		}

		array.add(string.substring(i)); // last substring

		return array;
	}
	
	public static int[] toIntArray(IntBuffer buffer) {
		var ints = new int[buffer.remaining()];
		for (int i = buffer.position(); i < buffer.limit(); i++) {
			ints[i - buffer.position()] = buffer.get(i);
		}
		return ints;
	}
	
	@Null
	public static <T> T get(T[] array, int i) {
		if (array == null || i < 0 || i >= array.length) {
			return null;
		}
		return array[i];
	}
	
	@Null
	public static <T> T get(List<T> list, int i) {
		if (list == null || i < 0 || i >= list.size()) {
			return null;
		}
		return list.get(i);
	}
	
	public static Format vaildFormat(Format format) {
		return switch (format) {
		case Alpha -> Format.RGBA8888;
		default -> format;
		};
	}
	
	public static <T> T get(List<T> list, int i, T defaultValue) {
		if (list == null || i < 0 || i >= list.size()) {
			return defaultValue;
		}
		return list.get(i);
	}

	public static void mul(BoundingBox box, Matrix4 transform) {
		final float a = 0.5f;
		final float x0 = box.min.x-a, y0 = box.min.y-a, z0 = box.min.z-a, x1 = box.max.x-a, y1 = box.max.y-a, z1 = box. max.z-a;
		var vec = new Vector3();
		box.inf();
		box.ext(vec.set(x0, y0, z0).mul(transform));
		box.ext(vec.set(x0, y0, z1).mul(transform));
		box.ext(vec.set(x0, y1, z0).mul(transform));
		box.ext(vec.set(x0, y1, z1).mul(transform));
		box.ext(vec.set(x1, y0, z0).mul(transform));
		box.ext(vec.set(x1, y0, z1).mul(transform));
		box.ext(vec.set(x1, y1, z0).mul(transform));
		box.ext(vec.set(x1, y1, z1).mul(transform));
		box.min.add(a);
		box.max.add(a);
		box.update();
	}

	public static <T> T make(T obj, Consumer<T> consumer) {
		consumer.accept(obj);
		return obj;
	}
	

	/**
	 * Unescapes a string that contains standard Java escape sequences.
	 * <ul>
	 * <li><strong>&#92;b &#92;f &#92;n &#92;r &#92;t &#92;" &#92;'</strong> : BS,
	 * FF, NL, CR, TAB, double and single quote.</li>
	 * <li><strong>&#92;X &#92;XX &#92;XXX</strong> : Octal character specification
	 * (0 - 377, 0x00 - 0xFF).</li>
	 * <li><strong>&#92;uXXXX</strong> : Hexadecimal based Unicode character.</li>
	 * </ul>
	 * 
	 * @param chars A CharSequence optionally containing standard java escape sequences.
	 * @return The translated string.
	 */
	public static String unescapeJavaString(CharSequence chars) {
		if (chars.isEmpty()) return chars.toString();
		
		StringBuilder sb = new StringBuilder(chars.length());

		for (int i = 0; i < chars.length(); i++) {
			char ch = chars.charAt(i);
			if (ch == '\\') {
				char nextChar = (i == chars.length() - 1) ? '\\' : chars.charAt(i + 1);
				// Octal escape?
				if (nextChar >= '0' && nextChar <= '7') {
					String code = "" + nextChar;
					i++;
					if ((i < chars.length() - 1) && chars.charAt(i + 1) >= '0' && chars.charAt(i + 1) <= '7') {
						code += chars.charAt(i + 1);
						i++;
						if ((i < chars.length() - 1) && chars.charAt(i + 1) >= '0' && chars.charAt(i + 1) <= '7') {
							code += chars.charAt(i + 1);
							i++;
						}
					}
					sb.append((char) Integer.parseInt(code, 8));
					continue;
				}
				switch (nextChar) {
				case '\\':
					ch = '\\';
					break;
				case 'b':
					ch = '\b';
					break;
				case 'f':
					ch = '\f';
					break;
				case 'n':
					ch = '\n';
					break;
				case 'r':
					ch = '\r';
					break;
				case 't':
					ch = '\t';
					break;
				case '\"':
					ch = '\"';
					break;
				case '\'':
					ch = '\'';
					break;
				// Hex Unicode: u????
				case 'u':
					if (i >= chars.length() - 5) {
						ch = 'u';
						break;
					}
					int code = Integer.parseInt(
							"" + chars.charAt(i + 2) + chars.charAt(i + 3) + chars.charAt(i + 4) + chars.charAt(i + 5), 16);
					sb.append(Character.toChars(code));
					i += 5;
					continue;
				}
				i++;
			}
			sb.append(ch);
		}
		return sb.toString();
	}
}
