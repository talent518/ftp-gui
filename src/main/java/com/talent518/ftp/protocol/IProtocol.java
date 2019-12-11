package com.talent518.ftp.protocol;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.dao.Site;
import com.talent518.ftp.gui.table.FileTable;

public abstract class IProtocol {
	protected final Site site;
	protected String error = null;
	private boolean isLogined = false;
	protected ProgressListener progressListener = null;
	protected DeleteListener deleteListener = null;

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

	public abstract boolean isConnected();
	
	public boolean isLogined() {
		return isLogined;
	}

	public void setLogined(boolean isLogined) {
		this.isLogined = isLogined;
	}
	
	public void setProgressListener(ProgressListener progressListener) {
		this.progressListener = progressListener;
	}
	
	public void setDeleteListener(DeleteListener deleteListener) {
		this.deleteListener = deleteListener;
	}

	public abstract boolean login();

	public abstract String pwd();

	public abstract boolean ls(String remote, List<FileTable.Row> files);
	
	public abstract boolean rename(String from, String to);
	
	public abstract boolean mkdir(String remote);
	public abstract boolean rmdir(String remote);
	public abstract boolean unlink(String remote);
	
	public abstract boolean storeFile(String remote, String local);

	public abstract boolean retrieveFile(String remote, String local);

	public abstract boolean logout();
	
	public abstract void dispose();
	
	public interface ProgressListener {
		public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize);
	}
	
	public interface DeleteListener {
		public void ls(String remote);
		public void rmdir(String remote);
		public void unlink(String remote);
	}
}
