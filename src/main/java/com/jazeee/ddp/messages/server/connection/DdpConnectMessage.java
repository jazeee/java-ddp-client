package com.jazeee.ddp.messages.server.connection;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.jazeee.ddp.messages.IDdpClientMessage;

public class DdpConnectMessage implements IDdpClientMessage {
	private final static String DDP_PROTOCOL_VERSION = "1";
	private final String session;
	private final String version;
	private final List<String> support;

	public DdpConnectMessage() {
		this(null);
	}

	public DdpConnectMessage(String session) {
		super();
		this.session = session;
		this.version = DDP_PROTOCOL_VERSION;
		this.support = Arrays.asList(DDP_PROTOCOL_VERSION);
	}

	public String getSession() {
		return session;
	}

	public String getDdpVersion() {
		return version;
	}

	public List<String> getSupportedDdpVersions() {
		return Collections.unmodifiableList(support);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName() + " [");
		builder.append("session=");
		builder.append(session);
		builder.append(", getDdpVersion=");
		builder.append(version);
		builder.append(", getSupportedDdpVersions=");
		builder.append(support);
		builder.append("]");
		return builder.toString();
	}
}
