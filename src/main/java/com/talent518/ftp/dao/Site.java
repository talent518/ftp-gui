package com.talent518.ftp.dao;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.talent518.ftp.protocol.FTP;
import com.talent518.ftp.protocol.IProtocol;
import com.talent518.ftp.protocol.SFTP;

public class Site implements Cloneable {
	private static final Logger log = Logger.getLogger(Site.class);
	private static final Map<String, Class<?>> protocols = new HashMap<String, Class<?>>();
	static {
		protocols.put("ftp", FTP.class);
		protocols.put("ftps", FTP.class);
		protocols.put("sftp", SFTP.class);
	}

	public static String[] getProtocols() {
		String[] keys = new String[protocols.size()];
		protocols.keySet().toArray(keys);
		return keys;
	}

	private String name;
	private String protocol;
	private String host;
	private int port;
	private String username;
	private String password;
	private String remote;
	private String local;
	private String remark;
	private boolean sync;
	private boolean watch;

	private boolean isImplicit;
	private String proxyHost;
	private int proxyPort;
	private String proxyUser;
	private String proxyPassword;

	private String secret; // SSL/TLS
	private String trustmgr; // all/valid/none
	private String encoding;
	private boolean hidden;

	private String serverType; // UNIX/VMS/WINDOWS
	private boolean saveUnparseable;

	private boolean binaryTransfer;
	private boolean localActive;
	private boolean useEpsvWithIPv4;
	private boolean isMlsd;

	private String privateKey;
	private String transferMode;
	private String prot;

	private Map<String, Favorite> favorites;

	public Site(String name, String protocol, String host, int port) {
		super();
		this.name = name;
		this.protocol = protocol;
		this.host = host;
		this.port = port;
	}

	public Site(String name, String protocol, String host, int port, String remote, String local) {
		super();
		this.name = name;
		this.protocol = protocol;
		this.host = host;
		this.port = port;
		this.remote = remote;
		this.local = local;
	}

	public IProtocol create() {
		if (protocols.containsKey(protocol)) {
			try {
				return (IProtocol) protocols.get(protocol).getDeclaredConstructor(Site.class).newInstance(this);
			} catch (Exception e) {
				log.error("new " + protocol + " failure", e);
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	public String getName() {
		return name;
	}

	public Site setName(String name) {
		this.name = name;
		return this;
	}

	public String getProtocol() {
		return protocol;
	}

	public Site setProtocol(String protocol) {
		this.protocol = protocol;
		return this;
	}

	public String getHost() {
		return host;
	}

	public Site setHost(String host) {
		this.host = host;
		return this;
	}

	public int getPort() {
		return port;
	}

	public Site setPort(int port) {
		this.port = port;
		return this;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRemote() {
		return remote;
	}

	public Site setRemote(String remote) {
		this.remote = remote;
		return this;
	}

	public String getLocal() {
		return local;
	}

	public Site setLocal(String local) {
		this.local = local;
		return this;
	}

	public String getRemark() {
		return remark;
	}

	public Site setRemark(String remark) {
		this.remark = remark;
		return this;
	}

	public boolean isSync() {
		return sync;
	}

	public Site setSync(boolean sync) {
		this.sync = sync;
		return this;
	}

	public boolean isWatch() {
		return watch;
	}

	public Site setWatch(boolean watch) {
		this.watch = watch;
		return this;
	}

	public boolean isImplicit() {
		return isImplicit;
	}

	public void setImplicit(boolean isImplicit) {
		this.isImplicit = isImplicit;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyUser() {
		return proxyUser;
	}

	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getTrustmgr() {
		return trustmgr;
	}

	public void setTrustmgr(String trustmgr) {
		this.trustmgr = trustmgr;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public String getServerType() {
		return serverType;
	}

	public void setServerType(String serverType) {
		this.serverType = serverType;
	}

	public boolean isSaveUnparseable() {
		return saveUnparseable;
	}

	public void setSaveUnparseable(boolean saveUnparseable) {
		this.saveUnparseable = saveUnparseable;
	}

	public boolean isBinaryTransfer() {
		return binaryTransfer;
	}

	public void setBinaryTransfer(boolean binaryTransfer) {
		this.binaryTransfer = binaryTransfer;
	}

	public boolean isLocalActive() {
		return localActive;
	}

	public void setLocalActive(boolean localActive) {
		this.localActive = localActive;
	}

	public boolean isUseEpsvWithIPv4() {
		return useEpsvWithIPv4;
	}

	public void setUseEpsvWithIPv4(boolean useEpsvWithIPv4) {
		this.useEpsvWithIPv4 = useEpsvWithIPv4;
	}

	public boolean isMlsd() {
		return isMlsd;
	}

	public void setMlsd(boolean isMlsd) {
		this.isMlsd = isMlsd;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public Map<String, Favorite> getFavorites() {
		if (favorites == null) {
			favorites = new HashMap<String, Favorite>();
		}

		return favorites;
	}

	public void setFavorites(Map<String, Favorite> favorites) {
		if (favorites != null) {
			this.favorites = favorites;
		}
	}

	public String getTransferMode() {
		return transferMode;
	}

	public void setTransferMode(String transferMode) {
		this.transferMode = transferMode;
	}

	public String getProt() {
		return prot;
	}

	public void setProt(String prot) {
		this.prot = prot;
	}

	@Override
	public Site clone() {
		try {
			Site s = (Site) super.clone();
			Map<String, Favorite> fav = new HashMap<String, Favorite>();
			for (Map.Entry<String, Favorite> entry : getFavorites().entrySet())
				fav.put(entry.getKey(), entry.getValue());
			s.setFavorites(fav);
			return s;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public String toString() {
		return Settings.gson().toJson(this);
	}

	public static class Favorite {
		private String name;
		private String local;
		private String remote;

		public Favorite(String name, String local, String remote) {
			super();
			this.name = name;
			this.local = local;
			this.remote = remote;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getLocal() {
			return local;
		}

		public void setLocal(String local) {
			this.local = local;
		}

		public String getRemote() {
			return remote;
		}

		public void setRemote(String remote) {
			this.remote = remote;
		}

		@Override
		public String toString() {
			return Settings.gson().toJson(this);
		}
	}
}
