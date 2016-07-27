package com.jazeee.ddp.messages.client.collections;

import com.jazeee.ddp.messages.IDdpClientMessage;

public class DdpAddedToCollectionMessage implements IDdpClientMessage {
	private final String collection;
	private final String id;
	private final Object fields;

	public DdpAddedToCollectionMessage() {
		super();
		this.collection = "";
		this.id = "";
		this.fields = null;
	}

	public String getCollection() {
		return collection;
	}

	public String getId() {
		return id;
	}

	public Object getFields() {
		return fields;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName() + " [collection=");
		builder.append(collection);
		builder.append(", id=");
		builder.append(id);
		builder.append(", fields=");
		builder.append(fields);
		builder.append("]");
		return builder.toString();
	}
}
