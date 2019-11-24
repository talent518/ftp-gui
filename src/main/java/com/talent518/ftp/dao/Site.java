package com.talent518.ftp.dao;

public class Site {
	private String name;
	private String protocol;
	private String host;
	private int port;
	private String remote;
	private String local;
	private String remark;
	private boolean sync;
	private boolean watch;

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

	@Override
	public String toString() {
		return Settings.gson().toJson(this);
	}
}
