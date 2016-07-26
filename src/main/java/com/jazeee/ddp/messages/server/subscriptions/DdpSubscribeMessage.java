package com.jazeee.ddp.messages.server.subscriptions;

import java.util.UUID;

import com.jazeee.ddp.messages.IDdpClientMessage;

public class DdpSubscribeMessage implements IDdpClientMessage {
	private final String id;
	private final String name;
	private final String params;

	public DdpSubscribeMessage(String name) {
		this(name, null);
	}

	public DdpSubscribeMessage(String name, String jsonQueryParams) {
		super();
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.params = jsonQueryParams;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getParams() {
		return params;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName() + " [");
		builder.append("id=");
		builder.append(id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", params=");
		builder.append(params);
		builder.append("]");
		return builder.toString();
	}
}
