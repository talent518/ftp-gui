package com.talent518.ftp.dao;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Settings {
	private static Logger log = Logger.getLogger(Settings.class);
	public static final String ROOT_PATH = System.getProperty("user.home") + File.separator + ".ftp-gui";
	private static final String SETTINGS_FILE = ROOT_PATH + File.separator + "settings.json";
	public static final String LOG_PATH = ROOT_PATH + File.separator + "logs" + File.separator;
	private static Settings _instance;
	private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static ResourceBundle language;

	static {
		FileReader reader = null;
		try {
			reader = new FileReader(new File(SETTINGS_FILE));
			_instance = gson.fromJson(reader, Settings.class);
			log.info("Load settings success");
		} catch (Throwable t) {
			log.error("Load settings failure", t);

			_instance = new Settings();
			Site s = new Site("default", "ftp", "192.168.1.100", 21);
			s.setUsername("anonymous");
			s.setPassword("anonymous");
			_instance.getSites().put("default", s);
			_instance.getSiteNames().add("default");
			_instance.save();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Throwable t) {
					log.error("Close reader failure", t);
				}
			}
		}
	}

	public static Gson gson() {
		return gson;
	}

	public static Settings instance() {
		return _instance;
	}

	public static ResourceBundle language() {
		if (language == null) {
			synchronized (Settings.class) {
				if (language == null) {
					language = ResourceBundle.getBundle("language", _instance.getLocale());
				}
			}
		}
		return language;
	}

	private Settings() {
	}

	@Override
	public String toString() {
		return gson.toJson(this);
	}

	public void save() {
		FileWriter writer = null;
		try {
			File path = new File(ROOT_PATH);
			if (!path.isDirectory()) {
				path.mkdir();
			}
			writer = new FileWriter(new File(SETTINGS_FILE));
			writer.write(toString());
			writer.flush();
			log.info("Save settings success");
		} catch (Throwable t) {
			log.error("Save settings failure", t);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException t) {
					log.error("Close writer failure", t);
				}
			}
		}
	}

	private transient Locale locale;

	private boolean watch;
	private String lang = Locale.getDefault().getLanguage();
	private String country = Locale.getDefault().getCountry();

	private int nthreads = 5;

	private Map<String, Site> sites = new HashMap<String, Site>();
	private List<String> siteNames = new ArrayList<String>();

	public Locale getLocale() {
		if (locale == null) {
			synchronized (Settings.class) {
				if (locale == null) {
					locale = new Locale(lang, country);
					Locale.setDefault(locale);
				}
			}
		}
		return locale;
	}

	public void setLocale(Locale locale) {
		if (locale != null) {
			lang = locale.getLanguage();
			country = locale.getCountry();
			this.locale = locale;
			Locale.setDefault(locale);
			language = null;
		}
	}

	public String getLang() {
		return lang;
	}

	public String getCountry() {
		return country;
	}

	public boolean isWatch() {
		return watch;
	}

	public Settings setWatch(boolean watch) {
		this.watch = watch;
		return this;
	}

	public int getNthreads() {
		if(nthreads <= 0)
			nthreads = 1;
		return nthreads;
	}

	public void setNthreads(int nthreads) {
		this.nthreads = nthreads;
	}

	public Map<String, Site> getSites() {
		return sites;
	}

	public List<String> getSiteNames() {
		return siteNames;
	}
}
