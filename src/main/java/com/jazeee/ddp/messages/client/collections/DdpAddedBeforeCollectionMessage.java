package com.jazeee.ddp.messages.client.collections;


public class DdpAddedBeforeCollectionMessage implements IDdpCollectionMessage {
	private final String collection;
	private final String id;
	private final Object fields;
	private final String before;

	public DdpAddedBeforeCollectionMessage() {
		super();
		this.collection = "";
		this.id = "";
		this.fields = null;
		this.before = null;
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
		builder.append(", fields=");
		builder.append(fields);
		builder.append(", getBeforeDocumentId=");
		builder.append(before);
		builder.append("]");
		return builder.toString();
	}
}
