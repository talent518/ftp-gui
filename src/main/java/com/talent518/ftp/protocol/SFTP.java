package com.talent518.ftp.protocol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.config.hosts.HostConfigEntry;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.NamedResource;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.keyprovider.KeyIdentityProvider;
import org.apache.sshd.common.session.SessionContext;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClient.DirEntry;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.apache.sshd.sftp.client.fs.SftpFileSystem;
import org.apache.sshd.sftp.client.fs.SftpPath;

import com.talent518.ftp.dao.Site;
import com.talent518.ftp.gui.table.FileTable;

public class SFTP extends IProtocol {
	private static Logger log = Logger.getLogger(SFTP.class);

	private SshClient client;
	private ClientSession session;
	private SftpFileSystem fs;

	public SFTP(Site s) {
		super(s);
	}

	@Override
	public boolean isConnected() {
		return !isTimeout() && fs != null && fs.isOpen() && error == null;
	}

	@Override
	public boolean login() {
		error = null;
		makeTime();
		try {
			client = SshClient.setUpDefaultClient();
			client.start();
			
			if (site.getProxyHost() != null && site.getProxyHost().length() > 0) {
				boolean isUser = site.getProxyUser() != null && site.getProxyHost().length() > 0;
//				boolean isPwd = site.getProxyPassword() != null && site.getProxyPassword().length() > 0;
				String proxy = (isUser ? site.getProxyUser()/* + (isPwd ? ":" + site.getProxyPassword() : "") */ + "@" : "") + site.getProxyHost() + ":" + site.getProxyPort();
				session = client.connect(new HostConfigEntry("", site.getHost(), site.getPort(), site.getUsername(), proxy)).verify().getSession();
			} else {
				session = client.connect(site.getUsername(), site.getHost(), site.getPort()).verify().getSession();
			}

			boolean isAuth = false;
			if (site.getPrivateKey() != null && site.getPrivateKey().length() > 0) {
				session.setKeyIdentityProvider(new KeyIdentityProvider() {

					@Override
					public Iterable<KeyPair> loadKeys(SessionContext session) throws IOException, GeneralSecurityException {
						return SecurityUtils.loadKeyPairIdentities(session, NamedResource.ofName(site.getName()), new FileInputStream(site.getPrivateKey()), FilePasswordProvider.of(site.getPassphrase()));
					}
				});
				isAuth = true;
			}

			String pwd = site.getPassword();
			if (pwd != null && pwd.length() > 0) {
				session.addPasswordIdentity(pwd);
				isAuth = true;
			}

			if (isAuth && session.auth().verify(15000).isFailure()) {
				throw new Exception("sftp auth failure");
			}

			fs = SftpClientFactory.instance().createSftpFileSystem(session);

			setLogined(true);
			return true;
		} catch (Exception e) {
			log.error("Connect error", e);
			error = e.getMessage();
			return false;
		}
	}

	@Override
	public String pwd() {
		error = null;
		return fs.getDefaultDir().toString();
	}

	@Override
	public boolean ls(String remote, List<FileTable.Row> files) {
		error = null;
		makeTime();
		try {
			SftpClient sftp = fs.getClient();
			SftpClient.CloseableHandle handle = sftp.openDir(remote);
			Iterable<DirEntry> l = sftp.listDir(handle);
			Iterator<DirEntry> iter = l.iterator();
			while (iter.hasNext()) {
				DirEntry entry = iter.next();
				String f = entry.getFilename();
				if (!".".equals(f) && !"..".equals(f)) {
					files.add(new FileTable.Row(entry));
				}
			}
			return true;
		} catch (IOException e) {
			log.error("ls error", e);
			error = e.getMessage();
			return true;
		}
	}

	@Override
	public boolean rename(String from, String to) {
		error = null;
		makeTime();
		try {
			Files.move(fs.getDefaultDir().resolve(from), fs.getDefaultDir().resolve(to), StandardCopyOption.REPLACE_EXISTING);
			return true;
		} catch (IOException e) {
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
			SftpPath path = fs.getDefaultDir().resolve(remote);
			if (!Files.exists(path)) {
				Files.createDirectory(path);
			}
			return true;
		} catch (IOException e) {
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
			SftpPath path = fs.getDefaultDir().resolve(remote);
			return Files.deleteIfExists(path);
		} catch (IOException e) {
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
			SftpPath path = fs.getDefaultDir().resolve(remote);
			return Files.deleteIfExists(path);
		} catch (IOException e) {
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
			File f = new File(local);
			long size = f == null ? 0 : f.length();
			SftpClient sftp = fs.getClient();
			SftpClient.CloseableHandle handle = sftp.open(remote, EnumSet.of(SftpClient.OpenMode.Write, SftpClient.OpenMode.Create));
			FileInputStream in = new FileInputStream(local);
			byte[] src = new byte[32 * 1024];
			int len;
			long fileOffset = 0l;
			while ((len = in.read(src)) != -1) {
				sftp.write(handle, fileOffset, src, 0, len);
				fileOffset += len;
				if (progressListener != null) {
					progressListener.bytesTransferred(fileOffset, len, size);
				}
			}

			in.close();
			sftp.close(handle);

			return true;
		} catch (IOException e) {
			log.error("upload file '" + local + "' failure", e);
			error = e.getMessage();

			return false;
		}
	}

	@Override
	public boolean retrieveFile(String remote, String local) {
		error = null;
		makeTime();
		try {
			long size = Files.size(fs.getDefaultDir().resolve(remote));
			SftpClient sftp = fs.getClient();
			SftpClient.CloseableHandle handle = sftp.open(remote, SftpClient.OpenMode.Read, SftpClient.OpenMode.Create);
			FileOutputStream out = new FileOutputStream(local);
			byte[] src = new byte[32 * 1024];
			int len;
			long fileOffset = 0l;
			while ((len = sftp.read(handle, fileOffset, src)) != -1) {
				out.write(src, 0, len);
				fileOffset += len;
				if (progressListener != null) {
					progressListener.bytesTransferred(fileOffset, len, size);
				}
			}

			out.close();
			sftp.close(handle);

			return true;
		} catch (IOException e) {
			log.error("download file '" + local + "' failure", e);
			error = e.getMessage();

			return false;
		}
	}

	@Override
	public boolean logout() {
		error = null;
		makeTime();
		if (fs != null) {
			try {
				fs.close();
			} catch (IOException e) {
				log.error("sftp fs close", e);
				error = e.getMessage();
			}
			fs = null;
		}
		if (session != null) {
			if (session.isOpen()) {
				try {
					session.close();
				} catch (IOException e) {
					log.error("sftp session close", e);
					error = e.getMessage();
				}
			}
			session = null;
		}
		if (client != null) {
			client.stop();
			client = null;
		}
		setLogined(false);
		return true;
	}

	@Override
	public void dispose() {
		logout();
	}
}
