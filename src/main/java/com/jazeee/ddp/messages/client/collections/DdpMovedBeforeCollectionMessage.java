package com.jazeee.ddp.messages.client.collections;


public class DdpMovedBeforeCollectionMessage implements IDdpCollectionMessage {
	private final String collection;
	private final String id;
	private final String before;

	public DdpMovedBeforeCollectionMessage() {
		super();
		this.collection = "";
		this.id = "";
		this.before = null;
	}

	public String getCollection() {
		return collection;
	}

	public String getId() {
		return id;
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
		builder.append(", getBeforeDocumentId=");
		builder.append(before);
		builder.append("]");
		return builder.toString();
	}
}
