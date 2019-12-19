package com.talent518.ftp.protocol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPHTTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.apache.commons.net.util.TrustManagerUtils;
import org.apache.log4j.Logger;

import com.talent518.ftp.dao.Site;
import com.talent518.ftp.gui.table.FileTable;

public class FTP extends IProtocol {
	private static Logger log = Logger.getLogger(FTP.class);

	private final FTPClient ftp;
	private int keepAliveTimeout = -1;
	private int controlKeepAliveReplyTimeout = -1;
	private int defaultTimeout = 10000;
	private int dataTimeout = 10000;
	private final CopyStreamListener copyStreamListener = new CopyStreamListener() {
		@Override
		public void bytesTransferred(CopyStreamEvent event) {
			bytesTransferred(event.getTotalBytesTransferred(), event.getBytesTransferred(), event.getStreamSize());
		}

		@Override
		public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
			if (progressListener != null) {
				progressListener.bytesTransferred(totalBytesTransferred, bytesTransferred, streamSize);
			}
		}
	};

	public FTP(Site s) {
		super(s);

		if ("ftps".equals(s.getProtocol())) {
			FTPSClient ftps;
			if (s.getSecret() != null) {
				ftps = new FTPSClient(s.getSecret(), s.isImplicit());
			} else {
				ftps = new FTPSClient(s.isImplicit());
			}
			if ("all".equals(s.getTrustmgr())) {
				ftps.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
			} else if ("valid".equals(s.getTrustmgr())) {
				ftps.setTrustManager(TrustManagerUtils.getValidateServerCertificateTrustManager());
			} else if ("none".equals(s.getTrustmgr())) {
				ftps.setTrustManager(null);
			}
			ftp = ftps;
		} else if (s.getProxyHost() != null && s.getProxyHost().length() > 0) {
			ftp = new FTPHTTPClient(s.getProxyHost(), s.getProxyPort(), s.getProxyUser(), s.getProxyPassword());
		} else {
			ftp = new FTPClient();
		}

		ftp.setCopyStreamListener(copyStreamListener);
		if (keepAliveTimeout >= 0) {
			ftp.setControlKeepAliveTimeout(keepAliveTimeout);
		}
		if (controlKeepAliveReplyTimeout >= 0) {
			ftp.setControlKeepAliveReplyTimeout(controlKeepAliveReplyTimeout);
		}
		if (defaultTimeout >= 0) {
			ftp.setDefaultTimeout(defaultTimeout);
		}
		if (dataTimeout >= 0) {
			ftp.setDataTimeout(dataTimeout);
		}
		if (s.getEncoding() != null) {
			ftp.setControlEncoding(s.getEncoding());
		}
		ftp.setListHiddenFiles(s.isHidden());

		// suppress login details
		ftp.addProtocolCommandListener(new PrintCommandListener(getPrintWriter(), true));

		final FTPClientConfig config;
		if (s.getServerType() != null) {
			config = new FTPClientConfig(s.getServerType());
		} else {
			config = new FTPClientConfig();
		}
		config.setUnparseableEntries(s.isSaveUnparseable());

		ftp.configure(config);
	}

	public boolean isConnected() {
		return ftp.isConnected() && error == null;
	}

	public boolean login() {
		error = null;

		try {
			if (site.getPort() > 0) {
				ftp.connect(site.getHost(), site.getPort());
			} else {
				ftp.connect(site.getHost());
			}

			if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
				log.error("Connect failure: " + ftp.getReplyString());
				error = ftp.getReplyString();
				ftp.disconnect();
				return false;
			}

			if (!ftp.login(site.getUsername(), site.getPassword())) {
				log.error("login failure: " + ftp.getReplyString());
				error = ftp.getReplyString();
				ftp.logout();

				return false;
			}

			if (site.isBinaryTransfer()) {
				ftp.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
			} else {
				ftp.setFileType(org.apache.commons.net.ftp.FTP.ASCII_FILE_TYPE);
			}

			if (site.isLocalActive()) {
				ftp.enterLocalActiveMode();
			} else {
				ftp.enterLocalPassiveMode();
			}

			ftp.setUseEPSVwithIPv4(site.isUseEpsvWithIPv4());

			setLogined(true);

			return true;
		} catch (IOException e) {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException f) {
				}
			}

			log.error("Connect error", e);
			error = e.getMessage();

			return false;
		}
	}

	public String pwd() {
		error = null;

		try {
			return ftp.printWorkingDirectory();
		} catch (IOException e) {
			log.error("pwd failure", e);
			error = e.getMessage();
			return "/";
		}
	}

	public boolean ls(String remote, List<FileTable.Row> rows) {
		error = null;

		try {
			FTPFile[] files;
			if (site.isMlsd()) {
				files = ftp.mlistDir(remote);
			} else {
				files = ftp.listFiles(remote);
			}

			if (files != null) {
				for (FTPFile f : files) {
					if (!".".equals(f.getName()) && !"..".equals(f.getName()))
						rows.add(new FileTable.Row(f));
				}
			}

			return true;
		} catch (IOException e) {
			log.error("ls failure", e);
			error = e.getMessage();
			return false;
		}
	}

	@Override
	public boolean rename(String from, String to) {
		error = null;

		try {
			return ftp.rename(from, to);
		} catch (IOException e) {
			log.error("rename " + from + " to " + to + " failure", e);
			error = e.getMessage();
			return false;
		}
	}

	@Override
	public boolean mkdir(String remote) {
		error = null;

		try {
			return ftp.makeDirectory(remote);
		} catch (IOException e) {
			log.error("mkdir " + remote + " failure", e);
			error = e.getMessage();
			return false;
		}
	}

	@Override
	public boolean rmdir(String remote) {
		error = null;
		boolean hidden = ftp.getListHiddenFiles();

		try {
			List<FileTable.Row> rows = new ArrayList<FileTable.Row>();
			ftp.setListHiddenFiles(true);
			if (ls(remote, rows)) {
				ftp.setListHiddenFiles(hidden);
				if (deleteListener != null)
					deleteListener.ls(remote);
				for (FileTable.Row r : rows) {
					if (r.isDir()) {
						if (!rmdir(remote + '/' + r.getName()))
							return false;
						if (deleteListener != null)
							deleteListener.rmdir(remote + '/' + r.getName());
					} else {
						if (!unlink(remote + '/' + r.getName()))
							return false;
						if (deleteListener != null)
							deleteListener.unlink(remote + '/' + r.getName());
					}
				}
				return ftp.removeDirectory(remote);
			} else {
				ftp.setListHiddenFiles(hidden);
				return false;
			}
		} catch (IOException e) {
			log.error("rmdir " + remote + " failure", e);
			error = e.getMessage();
			ftp.setListHiddenFiles(hidden);
			return false;
		}
	}

	@Override
	public boolean unlink(String remote) {
		error = null;

		try {
			return ftp.deleteFile(remote);
		} catch (IOException e) {
			log.error("unlink " + remote + " failure", e);
			error = e.getMessage();
			return false;
		}
	}

	public boolean storeFile(String remote, String local) {
		InputStream input = null;
		error = null;

		if (isResume) {
			try {
				ftp.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
			} catch (IOException e) {
				log.error("Set binary '" + local + "' failure", e);
			}

			ftp.setRestartOffset(0);

			try {
				FTPFile file = ftp.mlistFile(remote);
				if (file != null)
					ftp.setRestartOffset(file.getSize());
			} catch (IOException e) {
				log.error("Set restart offset '" + local + "' failure", e);
			}
		}

		try {
			input = new FileInputStream(local);

			return ftp.storeFile(remote, input);
		} catch (FileNotFoundException e) {
			log.error("open local file '" + local + "' failure", e);
			error = e.getMessage();
		} catch (IOException e) {
			log.error("upload file '" + local + "' failure", e);
			error = e.getMessage();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (Exception e2) {
				}
			}
		}

		return false;
	}

	public boolean retrieveFile(String remote, String local) {
		OutputStream output = null;
		error = null;

		if (isResume) {
			try {
				ftp.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
			} catch (IOException e) {
				log.error("Set binary '" + local + "' failure", e);
			}

			try {
				File f = new File(local);
				ftp.setRestartOffset(f.exists() ? f.length() : 0);
			} catch (Exception e) {
				ftp.setRestartOffset(0);
				log.error("Set restart offset '" + local + "' failure", e);
			}
		}

		try {
			output = new FileOutputStream(local, isResume);

			return ftp.retrieveFile(remote, output);
		} catch (FileNotFoundException e) {
			log.error("open local file '" + local + "' failure", e);
			error = e.getMessage();
		} catch (IOException e) {
			log.error("download file '" + local + "' failure", e);
			error = e.getMessage();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (Exception e2) {
				}
			}
		}

		return false;
	}

	public boolean logout() {
		error = null;

		try {
			ftp.noop();

			boolean ret = ftp.logout();

			ftp.disconnect();

			setLogined(false);

			return ret;
		} catch (IOException e) {
			log.error("logout failure", e);
			error = e.getMessage();
		}
		return false;
	}

	public void dispose() {
		if (ftp.isConnected()) {
			logout();
		}
	}
}
