package com.talent518.ftp.protocol;

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
	private boolean printHash;
	private int keepAliveTimeout = -1;
	private int controlKeepAliveReplyTimeout = -1;

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
		} else if (s.getProxyHost() != null) {
			ftp = new FTPHTTPClient(s.getProxyHost(), s.getProxyPort(), s.getProxyUser(), s.getProxyPassword());
		} else {
			ftp = new FTPClient();
		}

		if (printHash) {
			ftp.setCopyStreamListener(createListener());
		}
		if (keepAliveTimeout >= 0) {
			ftp.setControlKeepAliveTimeout(keepAliveTimeout);
		}
		if (controlKeepAliveReplyTimeout >= 0) {
			ftp.setControlKeepAliveReplyTimeout(controlKeepAliveReplyTimeout);
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
		if (s.getDefaultDateFormat() != null) {
			config.setDefaultDateFormatStr(s.getDefaultDateFormat());
		}
		if (s.getRecentDateFormat() != null) {
			config.setRecentDateFormatStr(s.getRecentDateFormat());
		}
		if (site.isLenient() || site.getServerTimeZoneId() != null) {
			config.setLenientFutureDates(site.isLenient());
			if (site.getServerTimeZoneId() != null) {
				config.setServerTimeZoneId(site.getServerTimeZoneId());
			}
		}
		ftp.configure(config);
	}

	private CopyStreamListener createListener() {
		return new CopyStreamListener() {
			private long megsTotal = 0;

			@Override
			public void bytesTransferred(CopyStreamEvent event) {
				bytesTransferred(event.getTotalBytesTransferred(), event.getBytesTransferred(), event.getStreamSize());
			}

			@Override
			public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
				long megs = totalBytesTransferred / 1000000;
				for (long l = megsTotal; l < megs; l++) {
					System.err.print("#");
				}
				megsTotal = megs;
			}
		};
	}

	public boolean login() {
		try {
			if (site.getPort() > 0) {
				ftp.connect(site.getHost(), site.getPort());
			} else {
				ftp.connect(site.getHost());
			}

			if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
				log.error("Connect failure: " + ftp.getReplyString());
				ftp.disconnect();
				return false;
			}

			if (!ftp.login(site.getUsername(), site.getPassword())) {
				log.error("login failure: " + ftp.getReplyString());
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

			return true;
		} catch (IOException e) {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException f) {
				}
			}

			log.error("Connect error", e);

			return false;
		}
	}
	
	public String pwd() {
		try {
			return ftp.printWorkingDirectory();
		} catch (IOException e) {
			log.error("pwd failure", e);
			return "/";
		}
	}

	public List<FileTable.Row> ls(String remote) {
		try {
			FTPFile[] files;
			if (site.isMlsd()) {
				files = ftp.mlistDir(remote);
			} else {
				files = ftp.listFiles(remote);
			}

			List<FileTable.Row> rows = new ArrayList<FileTable.Row>();

			if (files != null) {
				for (FTPFile f : files) {
					rows.add(new FileTable.Row(f));
				}
			}

			return rows;
		} catch (IOException e) {
			log.error("ls failure", e);
			return new ArrayList<FileTable.Row>();
		}
	}

	public boolean storeFile(String remote, String local) {
		InputStream input = null;

		try {
			input = new FileInputStream(local);

			return ftp.storeFile(remote, input);
		} catch (FileNotFoundException e) {
			log.error("open local file '" + local + "' failure", e);
		} catch (IOException e) {
			log.error("upload file '" + local + "' failure", e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (Exception e2) {
				}
			}
		}

		return true;
	}

	public boolean retrieveFile(String remote, String local) {
		OutputStream output = null;

		try {
			output = new FileOutputStream(local);

			return ftp.retrieveFile(remote, output);
		} catch (FileNotFoundException e) {
			log.error("open local file '" + local + "' failure", e);
		} catch (IOException e) {
			log.error("upload file '" + local + "' failure", e);
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
		try {
			ftp.noop();
			
			boolean ret = ftp.logout();
			
			ftp.disconnect();
			
			return ret;
		} catch(IOException e) {
			log.error("logout failure", e);
		}
		return false;
	}
	
	public void dispose() {
		if(ftp != null && ftp.isConnected()) {
			logout();
		}
	}
}
