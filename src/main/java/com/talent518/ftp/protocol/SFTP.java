package com.talent518.ftp.protocol;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.dao.Site;
import com.talent518.ftp.gui.table.FileTable.Row;

public class SFTP extends IProtocol {
	private static Logger log = Logger.getLogger(SFTP.class);
	private static final SimpleDateFormat logFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static PrintWriter printWriter;
	static {
		try {
			printWriter = new PrintWriter(Settings.LOG_PATH + "sftp.log");
		} catch (FileNotFoundException e) {
			printWriter = new PrintWriter(System.out);
		}

		JSch.setLogger(new com.jcraft.jsch.Logger() {
			@Override
			public boolean isEnabled(int level) {
				return true;
			}

			@Override
			public void log(int level, String message) {
				String strLevel = "Unkown";
				switch (level) {
					case DEBUG:
						strLevel = "DEBUG";
						break;
					case INFO:
						strLevel = "INFO";
						break;
					case WARN:
						strLevel = "WARN";
						break;
					case ERROR:
						strLevel = "ERROR";
						break;
					case FATAL:
						strLevel = "FATAL";
						break;
				}
				printWriter.write(logFormat.format(new Date()) + ' ' + strLevel + ' ' + message + '\n');
				printWriter.flush();
			}
		});
	}
	private final SftpProgressMonitor monitor = new SftpProgressMonitor() {
		long written, total;

		@Override
		public void init(int op, String src, String dest, long max) {
			total = max;
			written = 0;
			makeTime();
		}

		@Override
		public void end() {
			makeTime();
			if (progressListener != null && total != UNKNOWN_SIZE) {
				progressListener.bytesTransferred(total, 0, -1);
			}
		}

		@Override
		public boolean count(long count) {
			makeTime();
			written += count;
			if (progressListener != null) {
				progressListener.bytesTransferred(written, 0, -1);
			}
			return true;
		}
	};

	private ChannelSftp sftp = null;
	private Session session = null;

	public SFTP(Site s) {
		super(s);
	}

	@Override
	public boolean isConnected() {
		return !isTimeout() && sftp != null && sftp.isConnected() && error == null;
	}

	@Override
	public boolean login() {
		error = null;
		makeTime();
		try {
			JSch jsch = new JSch();
			if (site.getPrivateKey() != null && site.getPrivateKey().length() > 0) {
				jsch.addIdentity(site.getPrivateKey());
			}

			session = jsch.getSession(site.getUsername(), site.getHost(), site.getPort());
			if (site.getPassword() != null && site.getPassword().length() > 0) {
				session.setPassword(site.getPassword());
			}

			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			config.put("ConnectTimeout", "10000");
			config.put("ServerAliveInterval", "10000");
			session.setConfig(config);

			if (site.getProxyHost() != null && site.getProxyHost().length() > 0) {
				ProxyHTTP proxy = new ProxyHTTP(site.getProxyHost(), site.getProxyPort());
				if (site.getProxyUser() != null && site.getProxyHost().length() > 0 && site.getProxyPassword() != null && site.getProxyPassword().length() > 0)
					proxy.setUserPasswd(site.getProxyUser(), site.getProxyPassword());
				session.setProxy(proxy);
			}

			session.connect();

			Channel channel = session.openChannel("sftp");
			channel.connect();

			sftp = (ChannelSftp) channel;

			setLogined(true);
			return true;
		} catch (JSchException e) {
			log.error("Connect error", e);
			error = e.getMessage();
			return false;
		}
	}

	@Override
	public String pwd() {
		error = null;
		makeTime();
		try {
			return sftp.pwd();
		} catch (SftpException e) {
			log.error("pwd error", e);
			error = e.getMessage();
			return "/";
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean ls(String remote, List<Row> files) {
		error = null;
		makeTime();
		try {
			Vector<LsEntry> vector = sftp.ls(remote);
			for (LsEntry entry : vector)
				if (!".".equals(entry.getFilename()) && !"..".equals(entry.getFilename()))
					files.add(new Row(entry));
			return true;
		} catch (SftpException e) {
			if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE)
				return true;

			log.error("ls error", e);
			error = e.getMessage();
			return false;
		}
	}

	@Override
	public boolean rename(String from, String to) {
		error = null;
		makeTime();
		try {
			sftp.rename(from, to);
			return true;
		} catch (SftpException e) {
			log.error("rename error", e);
			error = e.getMessage();
			return false;
		}
	}

	@Override
	public boolean mkdir(String remote) {
		error = null;
		makeTime();
		try {
			sftp.mkdir(remote);
			return true;
		} catch (SftpException e) {
			if (e.id == ChannelSftp.SSH_FX_FAILURE)
				return true;
			log.error("mkdir error", e);
			error = e.getMessage();
			return false;
		}
	}

	@Override
	public boolean rmdir(String remote) {
		error = null;
		makeTime();
		try {
			List<Row> rows = new ArrayList<Row>();
			if (ls(remote, rows)) {
				if (deleteListener != null)
					deleteListener.ls(remote);
				for (Row r : rows) {
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
				sftp.rmdir(remote);
				return true;
			} else {
				return false;
			}
		} catch (SftpException e) {
			if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE)
				return true;

			log.error("rmdir error", e);
			error = e.getMessage();
			return false;
		}
	}

	@Override
	public boolean unlink(String remote) {
		error = null;
		makeTime();
		try {
			sftp.rm(remote);
			return true;
		} catch (SftpException e) {
			if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE)
				return true;

			log.error("unlink error", e);
			error = e.getMessage();
			return false;
		}
	}

	@Override
	public boolean storeFile(String remote, String local) {
		error = null;
		makeTime();
		try {
			if (isResume)
				sftp.put(local, remote, monitor, ChannelSftp.RESUME);
			else
				sftp.put(local, remote, monitor);
			return true;
		} catch (SftpException e) {
			log.error("upload file '" + local + "' failure", e);
			error = e.getMessage();
		}

		return false;
	}

	@Override
	public boolean retrieveFile(String remote, String local) {
		error = null;
		makeTime();
		try {
			if (isResume)
				sftp.get(remote, local, monitor, ChannelSftp.RESUME);
			else
				sftp.get(remote, local, monitor);
			return true;
		} catch (SftpException e) {
			log.error("download file '" + local + "' failure", e);
			error = e.getMessage();
		}

		return false;
	}

	@Override
	public boolean logout() {
		error = null;
		makeTime();
		if (sftp != null) {
			if (sftp.isConnected()) {
				sftp.disconnect();
			}
			sftp = null;
		}
		if (session != null) {
			if (session.isConnected()) {
				session.disconnect();
			}
			session = null;
		}
		setLogined(false);
		return true;
	}

	@Override
	public void dispose() {
		logout();
	}
}
