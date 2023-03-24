package com.andedit.viewmc.lwjgl3;

public class Reflections {
	
	@SuppressWarnings("unchecked")
	public static <T> T getFeild(Object obj, Class<?> type, String feildName) {
		try {
			var field = type.getDeclaredField(feildName);
			field.setAccessible(true);
			return (T) field.get(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void invoke(Class<?> type, String methodName) {
		try {
			var method = type.getDeclaredMethod(methodName);
			method.setAccessible(true);
			method.invoke(type);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void invoke(Object obj, Class<?> type, String methodName) {
		try {
			var method = type.getDeclaredMethod(methodName);
			method.setAccessible(true);
			method.invoke(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void invoke(Class<?> type, String methodName, Object[] objs, Class<?>[] types) {
		try {
			var method = type.getDeclaredMethod(methodName, types);
			method.setAccessible(true);
			method.invoke(type, objs);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void invoke(Object obj, String methodName, Object[] objs, Class<?>[] types) {
		try {
			var method = obj.getClass().getDeclaredMethod(methodName, types);
			method.setAccessible(true);
			method.invoke(obj, objs);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T newObj(Class<T> type) {
		try {
			var constructor = type.getDeclaredConstructor();
	        constructor.setAccessible(true);
	        return constructor.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T newObj(Class<T> type, Object[] objs, Class<?>[] types) {
		try {
			var constructor = type.getDeclaredConstructor(types);
	        constructor.setAccessible(true);
	        return constructor.newInstance(objs);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Class<?>[] array(Class<?>... t) {
		return t;
	}
	
	public static Object[] array(Object... t) {
		return t;
	}
}
