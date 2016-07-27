package com.jazeee.ddp.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.gson.Gson;
import com.jazeee.ddp.messages.client.heartbeat.DdpClientPingMessage;
import com.jazeee.ddp.messages.deserializers.GsonClientMessagesDeserializer;

public class GsonMessageDeserialiserTest {
	@Test
	public void testMessagesDeserializer() {
		Gson gson = GsonClientMessagesDeserializer.getGsonConverter();
		DdpClientMessages ddpClientMessages = gson.fromJson("{\"msg\": \"ping\", \"id\": \"123\", \"ignoredKey\": \"ignoredValue\"}", DdpClientMessages.class);
		IDdpClientMessage ddpClientMessage = ddpClientMessages.get(DdpClientMessageType.PING);
		assertNotNull("Pong Message is valid", ddpClientMessage);
		assertTrue(ddpClientMessage instanceof DdpClientPingMessage);
		assertEquals("123", ((DdpClientPingMessage) ddpClientMessage).getId());
	}
}
