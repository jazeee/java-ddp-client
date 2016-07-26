package com.jazeee.ddp.messages.client.collections;

import com.jazeee.ddp.messages.IDdpClientMessage;

public class DdpAddedBeforeCollectionMessage implements IDdpClientMessage {
	private final String collection;
	private final String id;
	private final String fields;
	private final String before;

	public DdpAddedBeforeCollectionMessage() {
		super();
		this.collection = "";
		this.id = "";
		this.fields = "";
		this.before = null;
	}

	public String getCollection() {
		return collection;
	}

	public String getId() {
		return id;
	}

	public String getFieldsAsJson() {
		return fields;
	}

	public String getBeforeDocumentId() {
		return before;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName() + " [collection=");
		builder.append(collection);
		builder.append(", id=");
		builder.append(id);
		builder.append(", fieldsAsJson=");
		builder.append(fields);
		builder.append(", getBeforeDocumentId=");
		builder.append(before);
		builder.append("]");
		return builder.toString();
	}
}
