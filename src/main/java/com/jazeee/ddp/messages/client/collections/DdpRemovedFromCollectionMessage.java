package com.jazeee.ddp.messages.client.collections;

import com.jazeee.ddp.messages.IDdpClientMessage;

public class DdpRemovedFromCollectionMessage implements IDdpClientMessage {
	private final String collection;
	private final String id;

	public DdpRemovedFromCollectionMessage() {
		super();
		this.collection = "";
		this.id = "";
	}

	public String getCollection() {
		return collection;
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName() + " [collection=");
		builder.append(collection);
		builder.append(", id=");
		builder.append(id);
		builder.append("]");
		return builder.toString();
	}
}
