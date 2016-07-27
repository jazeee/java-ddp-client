package com.jazeee.ddp.messages.server.subscriptions;

import com.jazeee.ddp.messages.DdpServerMessageType;
import com.jazeee.ddp.messages.server.AbstractDdpServerMessage;

public class DdpSubscribeMessage extends AbstractDdpServerMessage {
	private final String id;
	private final String name;
	private final Object params;

	public DdpSubscribeMessage(String id, String name) {
		this(id, name, null);
	}

	public DdpSubscribeMessage(String id, String name, Object queryParams) {
		super();
		this.id = id;
		this.name = name;
		this.params = queryParams;
	}

	@Override
	protected DdpServerMessageType getDdpServerMessageType() {
		return DdpServerMessageType.SUB;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Object getParams() {
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
