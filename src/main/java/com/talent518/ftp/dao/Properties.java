package com.talent518.ftp.dao;

import java.io.IOException;
import java.util.HashMap;

public class Properties {
	private static HashMap<String, Properties> map = new HashMap<String, Properties>();

	public static Properties instance(String name) {
		if (!map.containsKey(name)) {
			synchronized (map) {
				if (map.containsKey(name))
					return map.get(name);

				Properties properties = new Properties(name);
				map.put(name, properties);
				return properties;
			}
		}

		return map.get(name);
	}

	private final java.util.Properties properties = new java.util.Properties();

	private Properties(String name) {
		try {
			properties.load(Properties.class.getResourceAsStream("/" + name + ".properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String get(String key) {
		return properties.getProperty(key);
	}

	public String get(String key, String def) {
		return properties.getProperty(key, def);
	}

	public int getInt(String key, int def) {
		String val = get(key);
		return val == null ? def : Integer.parseInt(val);
	}

	public int getInt(String key) {
		return getInt(key, 0);
	}

	public long getLong(String key, long def) {
		String val = get(key);
		return val == null ? def : Long.parseLong(val);
	}

	public long getLong(String key) {
		return getLong(key, 0);
	}

	public float getFloat(String key, float def) {
		String val = get(key);
		return val == null ? def : Float.parseFloat(val);
	}

	public float getFloat(String key) {
		return getFloat(key, 0);
	}

	public double getDouble(String key, double def) {
		String val = get(key);
		return val == null ? def : Double.parseDouble(val);
	}

	public double getDouble(String key) {
		return getLong(key, 0);
	}

	public boolean getBoolean(String key, boolean def) {
		String val = get(key);
		return val == null ? def : Boolean.getBoolean(val);
	}

	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}
}
