package com.jazeee.ddp.messages.deserializers;

import java.lang.reflect.Type;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.jazeee.ddp.messages.DdpClientMessageType;
import com.jazeee.ddp.messages.DdpClientMessages;
import com.jazeee.ddp.messages.IDdpClientMessage;

/*
 * Example usage:
 * DdpClientMessages ddpClientMessages = gson.fromJson("{\"msg\": \"ping\", \"id\": \"123\", \"ignoredKey\": \"ignoredValue\"}", DdpClientMessages.class);
 * See tests on usage
 * Consider using http://flexjson.sourceforge.net/
 */
public final class GsonClientMessagesDeserializer implements JsonDeserializer<DdpClientMessages> {
	public static Gson getGsonConverter() {
		return registerTypeAdapter(new GsonBuilder()).create();
	}

	public static GsonBuilder registerTypeAdapter(GsonBuilder gsonBuilder) {
		return gsonBuilder.registerTypeAdapter(DdpClientMessages.class, new GsonClientMessagesDeserializer());
	}

	private GsonClientMessagesDeserializer() {
		super();
	}

	@Override
	public DdpClientMessages deserialize(JsonElement jsonElement, Type parentType, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
		DdpClientMessages ddpClientMessages = new DdpClientMessages();
		Set<Entry<String, JsonElement>> entrySet = jsonElement.getAsJsonObject().entrySet();
		DdpClientMessageType ddpClientMessageType = null;
		for (Entry<String, JsonElement> entry : entrySet) {
			if (entry.getKey().equals("msg")) {
				ddpClientMessageType = DdpClientMessageType.fromKey(entry.getValue().getAsString());
				break;
			}
		}
		if (ddpClientMessageType != null) {
			IDdpClientMessage ddpClientMessage = jsonDeserializationContext.deserialize(jsonElement, ddpClientMessageType.getDdpMessageClass());
			ddpClientMessages.put(ddpClientMessageType, ddpClientMessage);
		}
		return ddpClientMessages;
	}
}
