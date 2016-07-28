package com.jazeee.ddp.messages;

import java.util.HashMap;
import java.util.Map;

public class DdpClientMessages extends HashMap<DdpClientMessageType, IDdpClientMessage> implements Map<DdpClientMessageType, IDdpClientMessage> {
	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		return super.toString();
	}
}
