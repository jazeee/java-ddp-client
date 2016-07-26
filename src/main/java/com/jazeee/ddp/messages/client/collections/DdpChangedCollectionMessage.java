package com.jazeee.ddp.messages.client.collections;

import java.util.Collections;
import java.util.List;

import com.jazeee.ddp.messages.IDdpClientMessage;

public class DdpChangedCollectionMessage implements IDdpClientMessage {
	private final String collection;
	private final String id;
	private final String fields;
	private final List<String> cleared;

	public DdpChangedCollectionMessage() {
		super();
		this.collection = "";
		this.id = "";
		this.fields = "";
		this.cleared = Collections.emptyList();
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

	public List<String> getDeletedFields() {
		if (cleared != null) {
			return Collections.unmodifiableList(cleared);
		}
		return Collections.emptyList();
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
		builder.append(", cleared=");
		builder.append(cleared);
		builder.append("]");
		return builder.toString();
	}
}
