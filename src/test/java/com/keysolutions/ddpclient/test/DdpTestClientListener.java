/*
 * (c)Copyright 2013-2014 Ken Yee, KEY Enterprise Solutions 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.keysolutions.ddpclient.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.jazeee.ddp.listeners.IDdpAllListener;
import com.jazeee.ddp.messages.DdpErrorField;
import com.jazeee.ddp.messages.DdpTopLevelErrorMessage;
import com.jazeee.ddp.messages.client.collections.DdpAddedToCollectionMessage;
import com.jazeee.ddp.messages.client.collections.DdpChangedCollectionMessage;
import com.jazeee.ddp.messages.client.collections.DdpRemovedFromCollectionMessage;
import com.jazeee.ddp.messages.client.collections.IDdpCollectionMessage;
import com.jazeee.ddp.messages.client.connection.DdpConnectedMessage;
import com.jazeee.ddp.messages.client.connection.DdpDisconnectedMessage;
import com.jazeee.ddp.messages.client.connection.IDdpClientConnectionMessage;
import com.jazeee.ddp.messages.client.heartbeat.DdpClientPongMessage;
import com.jazeee.ddp.messages.client.heartbeat.IDdpClientHeartbeatMessage;
import com.jazeee.ddp.messages.client.methodCalls.DdpMethodResultMessage;
import com.jazeee.ddp.messages.client.methodCalls.IDdpMethodCallMessage;
import com.jazeee.ddp.messages.client.subscriptions.DdpNoSubscriptionMessage;
import com.jazeee.ddp.messages.client.subscriptions.DdpSubscriptionReadyMessage;
import com.jazeee.ddp.messages.client.subscriptions.IDdpSubscriptionMessage;
import com.keysolutions.ddpclient.DdpClient;

/**
 * @author kenyee
 *
 *         DDP client observer that handles enough messages for unit tests to work
 */
public class DdpTestClientListener implements IDdpAllListener {
	private final static Logger LOGGER = Logger.getLogger(DdpClient.class.getName());

	public enum DdpState {
		DISCONNECTED, CONNECTED, LOGGED_IN, CLOSED,
	}

	public DdpState ddpState;
	public String resumeToken;
	public String userId;
	public String sessionId;
	public DdpTopLevelErrorMessage ddpTopLevelErrorMessage;
	public DdpErrorField ddpErrorField;
	public int closeCode;
	public String closeReason;
	public boolean isClosedFromRemote;
	public Map<String, Map<String, Object>> collections;
	public String readySubscription;
	public String pingId;

	public DdpTestClientListener(DdpClient ddpClient) {
		ddpState = DdpState.DISCONNECTED;
		collections = new HashMap<String, Map<String, Object>>();
		ddpClient.addHeartbeatListener(this);
		ddpClient.addConnectionListener(this);
		ddpClient.addSubscriptionListener(this);
		ddpClient.addCollectionListener(this);
		ddpClient.addMethodCallListener(this);
		ddpClient.addTopLevelErrorListener(this);
	}

	private void processMessage(DdpAddedToCollectionMessage ddpAddedToCollectionMessage) {
		String collName = ddpAddedToCollectionMessage.getCollection();
		Map<String, Object> collection = collections.get(collName);
		if (!collections.containsKey(collName)) {
			// add new collection
			LOGGER.finer("Added collection " + collName);
			collection = new HashMap<>();
			collections.put(collName, collection);
		}
		String id = ddpAddedToCollectionMessage.getId();
		LOGGER.fine("Added docid " + id + " to collection " + collName);
		collection.put(id, ddpAddedToCollectionMessage.getFields());
	}

	private void processMessage(DdpRemovedFromCollectionMessage ddpRemovedFromCollectionMessage) {
		String removedCollectionName = ddpRemovedFromCollectionMessage.getCollection();
		if (collections.containsKey(removedCollectionName)) {
			// remove IDs from collection
			Map<String, Object> removedCollection = collections.get(removedCollectionName);
			String docId = ddpRemovedFromCollectionMessage.getId();
			LOGGER.fine("Removed doc: " + docId);
			removedCollection.remove(docId);
		} else {
			LOGGER.warning("Received invalid removed msg for collection " + removedCollectionName);
		}
	}

	public void processMessage(DdpChangedCollectionMessage ddpChangedCollectionMessage) {
		// handle document updates
		String changedCollectionName = ddpChangedCollectionMessage.getCollection();
		if (collections.containsKey(changedCollectionName)) {
			Map<String, Object> changedCollection = collections.get(changedCollectionName);
			String docId = ddpChangedCollectionMessage.getId();
			@SuppressWarnings("unchecked")
			Map<String, Object> doc = (Map<String, Object>) changedCollection.get(docId);
			if (doc != null) {
				// take care of field updates
				@SuppressWarnings("unchecked")
				Map<String, Object> fields = (Map<String, Object>) ddpChangedCollectionMessage.getFields();
				if (fields != null) {
					for (Map.Entry<String, Object> field : fields.entrySet()) {
						String fieldname = field.getKey();
						doc.put(fieldname, field.getValue());
					}
				}
				// take care of clearing fields
				List<String> deletedFields = ddpChangedCollectionMessage.getDeletedFields();
				for (String fieldname : deletedFields) {
					if (doc.containsKey(fieldname)) {
						doc.remove(fieldname);
					}
				}
			}
		} else {
			LOGGER.warning("Received invalid changed msg for collection " + changedCollectionName);
		}
	}

	@Override
	public void processMessage(IDdpCollectionMessage ddpCollectionMessage) {
		if (ddpCollectionMessage instanceof DdpAddedToCollectionMessage) {
			processMessage((DdpAddedToCollectionMessage) ddpCollectionMessage);
		} else if (ddpCollectionMessage instanceof DdpRemovedFromCollectionMessage) {
			processMessage((DdpRemovedFromCollectionMessage) ddpCollectionMessage);
		} else if (ddpCollectionMessage instanceof DdpChangedCollectionMessage) {
			processMessage((DdpChangedCollectionMessage) ddpCollectionMessage);
		}
	}

	/**
	 * Helper function to dump a map
	 * 
	 * @param jsonFields JSON field map to dump
	 */
	public void dumpMap(Map<String, Object> jsonFields) {
		for (Entry<String, Object> entry : jsonFields.entrySet()) {
			System.out.printf("key: %s, value: %s (%s)\n", entry.getKey(), entry.getValue(), entry.getValue().getClass());
		}
	}

	@Override
	public void processMessage(IDdpMethodCallMessage ddpMethodCallMessage) {
		if (ddpMethodCallMessage instanceof DdpMethodResultMessage) {
			DdpMethodResultMessage ddpResultMessage = (DdpMethodResultMessage) ddpMethodCallMessage;
			// NOTE: in normal usage, you'd add a listener per command, not a global one like this
			// handle method data collection updated msg
			String methodId = ddpResultMessage.getId();
			if (methodId.equals("1") && ddpResultMessage.getResult() != null) {
				@SuppressWarnings("unchecked")
				Map<String, Object> result = (Map<String, Object>) ddpResultMessage.getResult();
				// login method is always "1"
				// REVIEW: is there a better way to figure out if it's a login result?
				resumeToken = (String) result.get("token");
				userId = (String) result.get("id");
				LOGGER.finer("Resume token: " + resumeToken + " for user " + userId);
				ddpState = DdpState.LOGGED_IN;
			}
			ddpErrorField = ddpResultMessage.getError();
			// TODO: save results for method calls
		}
	}

	@Override
	public void processMessage(IDdpClientHeartbeatMessage ddpClientHeartbeatMessage) {
		if (ddpClientHeartbeatMessage instanceof DdpClientPongMessage) {
			pingId = ddpClientHeartbeatMessage.getId();
		}
	}

	@Override
	public void processMessage(IDdpClientConnectionMessage ddpClientConnectionMessage) {
		if (ddpClientConnectionMessage instanceof DdpConnectedMessage) {
			DdpConnectedMessage ddpConnectedMessage = (DdpConnectedMessage) ddpClientConnectionMessage;
			ddpState = DdpState.CONNECTED;
			sessionId = ddpConnectedMessage.getSession();
		} else if (ddpClientConnectionMessage instanceof DdpDisconnectedMessage) {
			ddpState = DdpState.CLOSED;
			DdpDisconnectedMessage ddpDisconnectedMessage = (DdpDisconnectedMessage) ddpClientConnectionMessage;
			closeCode = Integer.parseInt(ddpDisconnectedMessage.getCode());
			closeReason = ddpDisconnectedMessage.getReason();
			isClosedFromRemote = ddpDisconnectedMessage.getRemote();
		}
	}

	@Override
	public void processMessage(IDdpSubscriptionMessage ddpSubscriptionMessage) {
		if (ddpSubscriptionMessage instanceof DdpSubscriptionReadyMessage) {
			DdpSubscriptionReadyMessage ddpSubscriptionReadyMessage = (DdpSubscriptionReadyMessage) ddpSubscriptionMessage;
			for (String id : ddpSubscriptionReadyMessage.getSubscriptionIds()) {
				// Should be only one for this test
				readySubscription = id;
			}
		} else if (ddpSubscriptionMessage instanceof DdpNoSubscriptionMessage) {
			DdpNoSubscriptionMessage ddpNoSubscriptionMessage = (DdpNoSubscriptionMessage) ddpSubscriptionMessage;
			ddpErrorField = ddpNoSubscriptionMessage.getError();
			if (ddpErrorField == null) {
				// if there's no error, it just means a subscription was unsubscribed
				readySubscription = null;
			}
		}
	}

	@Override
	public void processMessage(DdpTopLevelErrorMessage ddpTopLevelErrorMessage) {
		this.ddpTopLevelErrorMessage = ddpTopLevelErrorMessage;
	}
}
