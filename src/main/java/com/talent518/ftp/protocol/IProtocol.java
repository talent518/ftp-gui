package com.talent518.ftp.protocol;

import java.util.List;

import com.talent518.ftp.dao.Site;
import com.talent518.ftp.gui.table.FileTable;

public abstract class IProtocol {
	protected Site site;

	public IProtocol(Site s) {
		site = s;
	}
	
	public Site getSite() {
		return site;
	}

	public abstract boolean login();
	public abstract String pwd();
	public abstract List<FileTable.Row> ls(String remote);
	public abstract boolean storeFile(String remote, String local);
	public abstract boolean retrieveFile(String remote, String local);
	public abstract boolean logout();
}
