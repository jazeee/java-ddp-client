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

import com.jazeee.ddp.listeners.IDdpListener;
import com.jazeee.ddp.messages.DdpClientMessageType;
import com.jazeee.ddp.messages.DdpClientMessages;
import com.jazeee.ddp.messages.DdpErrorField;
import com.jazeee.ddp.messages.DdpTopLevelErrorMessage;
import com.jazeee.ddp.messages.IDdpClientMessage;
import com.jazeee.ddp.messages.client.collections.DdpAddedToCollectionMessage;
import com.jazeee.ddp.messages.client.collections.DdpChangedCollectionMessage;
import com.jazeee.ddp.messages.client.collections.DdpRemovedFromCollectionMessage;
import com.jazeee.ddp.messages.client.connection.DdpConnectedMessage;
import com.jazeee.ddp.messages.client.connection.DdpDisconnectedMessage;
import com.jazeee.ddp.messages.client.heartbeat.DdpClientPongMessage;
import com.jazeee.ddp.messages.client.heartbeat.IDdpClientHeartbeatMessage;
import com.jazeee.ddp.messages.client.methodCalls.DdpMethodResultMessage;
import com.jazeee.ddp.messages.client.subscriptions.DdpNoSubscriptionMessage;
import com.keysolutions.ddpclient.DdpClient;

/**
 * @author kenyee
 *
 *         DDP client observer that handles enough messages for unit tests to work
 */
public class DdpTestClientListener implements IDdpListener {
	private final static Logger LOGGER = Logger.getLogger(DdpClient.class.getName());

	public enum DdpState {
		Disconnected, Connected, LoggedIn, Closed,
	}

	public DdpState mDdpState;
	public String mResumeToken;
	public String mUserId;
	public int mErrorCode;
	public String mErrorType;
	public String mErrorReason;
	public String mErrorMsg;
	public String mErrorSource;
	public String mSessionId;
	public int mCloseCode;
	public String mCloseReason;
	public boolean mCloseFromRemote;
	public Map<String, Map<String, Object>> mCollections;
	public String mReadySubscription;
	public String mPingId;

	public DdpTestClientListener(DdpClient ddpClient) {
		mDdpState = DdpState.Disconnected;
		mCollections = new HashMap<String, Map<String, Object>>();
		ddpClient.addDDPListener(this);
		ddpClient.addHeartbeatListener(this);
	}

	@Override
	public void onDdpMessage(DdpClientMessages ddpClientMessages) {
		for (DdpClientMessageType ddpClientMessageType : ddpClientMessages.keySet()) {
			IDdpClientMessage ddpClientMessage = ddpClientMessages.get(ddpClientMessageType);
			if (ddpClientMessageType == null) {
				// ignore {"server_id":"GqrKrbcSeDfTYDkzQ"} web socket msgs
				continue;
			}
			switch (ddpClientMessageType) {
			case ERROR:
				DdpTopLevelErrorMessage ddpTopLevelErrorMessage = (DdpTopLevelErrorMessage) ddpClientMessage;
				mErrorSource = ddpTopLevelErrorMessage.getSource();
				mErrorMsg = ddpTopLevelErrorMessage.getErrorMessage();
				break;
			case CONNECTED:
				mDdpState = DdpState.Connected;
				mSessionId = ((DdpConnectedMessage) ddpClientMessage).getSession();
				break;
			case CLOSED:
				mDdpState = DdpState.Closed;
				DdpDisconnectedMessage ddpDisconnectedMessage = (DdpDisconnectedMessage) ddpClientMessage;
				mCloseCode = Integer.parseInt(ddpDisconnectedMessage.getCode());
				mCloseReason = ddpDisconnectedMessage.getReason();
				mCloseFromRemote = ddpDisconnectedMessage.getRemote();
				break;
			case ADDED:
				DdpAddedToCollectionMessage ddpAddedToCollectionMessage = (DdpAddedToCollectionMessage) ddpClientMessage;
				String collName = ddpAddedToCollectionMessage.getCollection();
				Map<String, Object> collection = mCollections.get(collName);
				if (!mCollections.containsKey(collName)) {
					// add new collection
					LOGGER.finer("Added collection " + collName);
					collection = new HashMap<>();
					mCollections.put(collName, collection);
				}
				String id = ddpAddedToCollectionMessage.getId();
				LOGGER.fine("Added docid " + id + " to collection " + collName);
				collection.put(id, ddpAddedToCollectionMessage.getFields());
				break;
			case REMOVED:
				DdpRemovedFromCollectionMessage ddpRemovedFromCollectionMessage = (DdpRemovedFromCollectionMessage) ddpClientMessage;
				String removedCollectionName = ddpRemovedFromCollectionMessage.getCollection();
				if (mCollections.containsKey(removedCollectionName)) {
					// remove IDs from collection
					Map<String, Object> removedCollection = mCollections.get(removedCollectionName);
					String docId = ddpRemovedFromCollectionMessage.getId();
					LOGGER.fine("Removed doc: " + docId);
					removedCollection.remove(docId);
				} else {
					LOGGER.warning("Received invalid removed msg for collection " + removedCollectionName);
				}
				break;
			case CHANGED:
				// handle document updates
				DdpChangedCollectionMessage ddpChangedCollectionMessage = (DdpChangedCollectionMessage) ddpClientMessage;
				String changedCollectionName = ddpChangedCollectionMessage.getCollection();
				if (mCollections.containsKey(changedCollectionName)) {
					Map<String, Object> changedCollection = mCollections.get(changedCollectionName);
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
				break;
			default:
				break;
			}
		}
		// TODO: handle addedBefore, movedBefore
		// dumpMap(jsonFields);
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
	public void onResult(DdpMethodResultMessage ddpResultMessage) {
		// NOTE: in normal usage, you'd add a listener per command, not a global one like this
		// handle method data collection updated msg
		String methodId = ddpResultMessage.getId();
		if (methodId.equals("1") && ddpResultMessage.getResult() != null) {
			@SuppressWarnings("unchecked")
			Map<String, Object> result = (Map<String, Object>) ddpResultMessage.getResult();
			// login method is always "1"
			// REVIEW: is there a better way to figure out if it's a login result?
			mResumeToken = (String) result.get("token");
			mUserId = (String) result.get("id");
			LOGGER.finer("Resume token: " + mResumeToken + " for user " + mUserId);
			mDdpState = DdpState.LoggedIn;
		}
		DdpErrorField error = ddpResultMessage.getError();
		processError(error);
		// TODO: save results for method calls
	}

	private void processError(DdpErrorField error) {
		if (error != null) {
			mErrorCode = (int) error.getErrorCodeIfPossible();
			mErrorMsg = error.getMessage();
			mErrorType = error.getErrorType();
			mErrorReason = error.getReason();
		}
	}

	@Override
	public void onNoSub(DdpNoSubscriptionMessage ddpNoSubscriptionMessage) {
		DdpErrorField error = ddpNoSubscriptionMessage.getError();
		processError(error);
		if (error == null) {
			// if there's no error, it just means a subscription was unsubscribed
			mReadySubscription = null;
		}
	}

	@Override
	public void onSubscriptionReady(String id) {
		mReadySubscription = id;
	}

	@Override
	public void onUpdated(String callId) {
	}

	@Override
	public void processMessage(IDdpClientHeartbeatMessage ddpClientHeartbeatMessage) {
		if (ddpClientHeartbeatMessage instanceof DdpClientPongMessage) {
			mPingId = ddpClientHeartbeatMessage.getId();
		}
	}
}
