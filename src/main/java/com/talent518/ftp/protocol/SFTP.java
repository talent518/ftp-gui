package com.talent518.ftp.protocol;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.talent518.ftp.dao.Site;
import com.talent518.ftp.gui.table.FileTable.Row;

public class SFTP extends IProtocol {
	private static Logger log = Logger.getLogger(SFTP.class);
	private final SftpProgressMonitor monitor = new SftpProgressMonitor() {
		@Override
		public void init(int op, String src, String dest, long max) {
		}
		
		@Override
		public void end() {
		}
		
		@Override
		public boolean count(long count) {
			progressListener.bytesTransferred(count, 0, -1);
			return true;
		}
	};

	private ChannelSftp sftp;
	private Session session;

	public SFTP(Site s) {
		super(s);
	}

	@Override
	public boolean isConnected() {
		return sftp.isConnected() && error == null;
	}

	@Override
	public boolean login() {
		error = null;

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

			session.setConfig(config);
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
		try {
			Vector<LsEntry> vector = sftp.ls(remote);
			for (LsEntry entry : vector)
				if (!".".equals(entry.getFilename()) && !"..".equals(entry.getFilename()))
					files.add(new Row(entry));
			return true;
		} catch (SftpException e) {
			log.error("ls error", e);
			error = e.getMessage();
			return false;
		}
	}

	@Override
	public boolean rename(String from, String to) {
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
		try {
			sftp.mkdir(remote);
			return true;
		} catch (SftpException e) {
			log.error("mkdir error", e);
			error = e.getMessage();
			return false;
		}
	}

	@Override
	public boolean rmdir(String remote) {
		try {
			List<Row> rows = new ArrayList<Row>();
			if (ls(remote, rows)) {
				for (Row r : rows) {
					if (r.isDir()) {
						if (!rmdir(remote + '/' + r.getName()))
							return false;
					} else {
						if (!unlink(remote + '/' + r.getName()))
							return false;
					}
				}
				sftp.rmdir(remote);
				return true;
			} else {
				return false;
			}
		} catch (SftpException e) {
			log.error("rmdir error", e);
			error = e.getMessage();
			return false;
		}
	}

	@Override
	public boolean unlink(String remote) {
		try {
			sftp.rm(remote);
			return true;
		} catch (SftpException e) {
			log.error("unlink error", e);
			error = e.getMessage();
			return false;
		}
	}

	@Override
	public boolean storeFile(String remote, String local) {
		InputStream input = null;
		error = null;

		try {
			input = new FileInputStream(local);
			sftp.put(input, remote, monitor);
			return true;
		} catch (FileNotFoundException e) {
			log.error("open local file '" + local + "' failure", e);
			error = e.getMessage();
		} catch (SftpException e) {
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

	@Override
	public boolean retrieveFile(String remote, String local) {
		OutputStream output = null;
		error = null;

		try {
			output = new FileOutputStream(local);
			sftp.get(remote, output, monitor);
			return true;
		} catch (FileNotFoundException e) {
			log.error("open local file '" + local + "' failure", e);
			error = e.getMessage();
		} catch (SftpException e) {
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

	@Override
	public boolean logout() {
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
		return true;
	}

	@Override
	public void dispose() {
		logout();
	}
}
