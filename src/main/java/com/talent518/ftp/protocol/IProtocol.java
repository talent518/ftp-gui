package com.talent518.ftp.protocol;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.dao.Site;
import com.talent518.ftp.gui.table.FileTable;

public abstract class IProtocol {
	protected Site site;
	protected String error;

	public IProtocol(Site s) {
		site = s;
	}

	public Site getSite() {
		return site;
	}
	
	public String getError() {
		return error;
	}

	protected String getLogFile() {
		return Settings.LOG_PATH + site.getName() + "-" + site.getProtocol() + ".log";
	}

	protected PrintWriter getPrintWriter() {
		try {
			return new PrintWriter(getLogFile());
		} catch (FileNotFoundException e) {
			return new PrintWriter(System.out);
		}
	}

	public abstract boolean login();

	public abstract String pwd();

	public abstract boolean ls(String remote, List<FileTable.Row> files);

	public abstract boolean storeFile(String remote, String local);

	public abstract boolean retrieveFile(String remote, String local);

	public abstract boolean logout();
}
