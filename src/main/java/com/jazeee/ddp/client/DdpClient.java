package com.jazeee.ddp.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.websocket.DeploymentException;

import org.apache.http.client.utils.URIBuilder;

import com.google.gson.Gson;
import com.jazeee.common.notifier.Notifier;
import com.jazeee.ddp.client.notificationProcessors.DdpClientHeartbeatNotificationProcessor;
import com.jazeee.ddp.client.notificationProcessors.DdpCollectionNotificationProcessor;
import com.jazeee.ddp.client.notificationProcessors.DdpConnectionNotificationProcessor;
import com.jazeee.ddp.client.notificationProcessors.DdpMethodCallNotificationProcessor;
import com.jazeee.ddp.client.notificationProcessors.DdpSubscriptionNotificationProcessor;
import com.jazeee.ddp.client.notificationProcessors.DdpTopLevelErrorNotificationProcessor;
import com.jazeee.ddp.listeners.IDdpCollectionListener;
import com.jazeee.ddp.listeners.IDdpConnectionListener;
import com.jazeee.ddp.listeners.IDdpHeartbeatListener;
import com.jazeee.ddp.listeners.IDdpMethodCallListener;
import com.jazeee.ddp.listeners.IDdpSubscriptionListener;
import com.jazeee.ddp.listeners.IDdpTopLevelErrorListener;
import com.jazeee.ddp.messages.DdpClientMessageType;
import com.jazeee.ddp.messages.DdpClientMessages;
import com.jazeee.ddp.messages.DdpTopLevelErrorMessage;
import com.jazeee.ddp.messages.IDdpClientMessage;
import com.jazeee.ddp.messages.client.collections.IDdpCollectionMessage;
import com.jazeee.ddp.messages.client.connection.IDdpClientConnectionMessage;
import com.jazeee.ddp.messages.client.heartbeat.DdpClientPingMessage;
import com.jazeee.ddp.messages.client.heartbeat.IDdpClientHeartbeatMessage;
import com.jazeee.ddp.messages.client.methodCalls.IDdpMethodCallMessage;
import com.jazeee.ddp.messages.client.subscriptions.IDdpSubscriptionMessage;
import com.jazeee.ddp.messages.deserializers.GsonClientMessagesDeserializer;
import com.jazeee.ddp.messages.server.IDdpServerMessage;
import com.jazeee.ddp.messages.server.connection.DdpConnectMessage;
import com.jazeee.ddp.messages.server.heartbeat.DdpServerPingMessage;
import com.jazeee.ddp.messages.server.methodCalls.DdpMethodCallMessage;
import com.jazeee.ddp.messages.server.subscriptions.DdpSubscribeMessage;
import com.jazeee.ddp.messages.server.subscriptions.DdpUnSubscribeMessage;

/**
 * Java Meteor DDP websocket client
 * 
 */
public class DdpClient implements IDdpHeartbeatListener, IDdpTopLevelErrorListener, IDdpClient, Closeable {
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
	private final AtomicLong currentMessageId = new AtomicLong(0);
	private final URI meteorUri;
	private final AtomicReference<DdpWebSocketClient> ddpWebSocketClientAtomicReference;

	private final Notifier<IDdpHeartbeatListener, IDdpClientHeartbeatMessage> heartbeatNotifier = new Notifier<>(new DdpClientHeartbeatNotificationProcessor());
	private final Notifier<IDdpConnectionListener, IDdpClientConnectionMessage> connectionNotifier = new Notifier<>(new DdpConnectionNotificationProcessor());
	private final Notifier<IDdpMethodCallListener, IDdpMethodCallMessage> methodCallNotifier = new Notifier<>(new DdpMethodCallNotificationProcessor());
	private final Notifier<IDdpSubscriptionListener, IDdpSubscriptionMessage> subscriptionNotifier = new Notifier<>(new DdpSubscriptionNotificationProcessor());
	private final Notifier<IDdpCollectionListener, IDdpCollectionMessage> collectionNotifier = new Notifier<>(new DdpCollectionNotificationProcessor());
	private final Notifier<IDdpTopLevelErrorListener, DdpTopLevelErrorMessage> topLevelErrorNotifier = new Notifier<>(new DdpTopLevelErrorNotificationProcessor());

	/** DDP connection state */
	public enum ConnectionState {
		DISCONNECTED, CONNECTED, CLOSED,
	}

	private final AtomicReference<ConnectionState> connectionState;

	/**
	 * Instantiates a Meteor DDP client for the Meteor server located at the supplied IP and port (note: running Meteor locally will typically have a port of 3000 but port 80 is
	 * the typical default for publicly deployed servers)
	 *
	 * @param serverIpAddress IP of Meteor server
	 * @param serverPort Port of Meteor server, if left null it will default to 3000
	 * @param useSSL Whether to use SSL for websocket encryption
	 * @throws URISyntaxException URI error
	 * @throws MalformedURLException
	 */
	public DdpClient(URI meteorUri) {
		this.meteorUri = meteorUri;
		this.ddpWebSocketClientAtomicReference = new AtomicReference<>();
		this.connectionState = new AtomicReference<>(ConnectionState.DISCONNECTED);
		this.heartbeatNotifier.addListener(this);
	}

	private void connectToWebSocketClient() {
		synchronized (connectionState) {
			connectionState.set(ConnectionState.DISCONNECTED);
			DdpWebSocketClient ddpWebSocketClient = new DdpWebSocketClient(this, getWebSocketURI());
			DdpWebSocketClient priorDdpWebSocketClient = ddpWebSocketClientAtomicReference.getAndSet(ddpWebSocketClient);
			if (priorDdpWebSocketClient != null) {
				priorDdpWebSocketClient.close();
			}
		}
	}

	private URI getWebSocketURI() {
		String httpScheme = meteorUri.getScheme();
		String webSocketScheme = "ws";
		if (httpScheme.equalsIgnoreCase("https")) {
			webSocketScheme = "wss";
		}
		URIBuilder uriBuilder = new URIBuilder(meteorUri);
		try {
			return uriBuilder.setScheme(webSocketScheme).setPath(meteorUri.getPath() + "/websocket").build();
		} catch (URISyntaxException e) {
			// This means a significant error in user setup. Throw as runtime
			throw new IllegalArgumentException("Bad Meteor URI. Cannot convert to WebSocket URI", e);
		}
	}

	/**
	 * Initiate connection to meteor server
	 * 
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 */
	public void connect() throws UnableToConnectException {
		synchronized (connectionState) {
			try {
				if (!connectionState.get().equals(ConnectionState.CONNECTED)) {
					connectToWebSocketClient();
				}
				ddpWebSocketClientAtomicReference.get().connect();
			} catch (IOException | DeploymentException e) {
				log.error("Unable to connect", e);
				throw new UnableToConnectException(e);
			}
		}
	}

	@Override
	public void close() {
		DdpWebSocketClient priorDdpWebSocketClient = ddpWebSocketClientAtomicReference.get();
		if (priorDdpWebSocketClient != null) {
			priorDdpWebSocketClient.close();
		}
	}

	/**
	 * Closes an open websocket connection. This is async, so you'll get a close notification callback when it eventually closes.
	 */
	public void disconnect() {
		close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jazeee.ddp.client.IDdpClient#connectionOpened()
	 */
	@Override
	public void onConnectionOpened() {
		log.trace("WebSocket connection opened");
		// reply to Meteor server with connection confirmation message ({"msg": "connect"})
		send(new DdpConnectMessage());
		// we'll get a msg:connected from the Meteor server w/ a session ID when we connect
		// note that this may return an error that the DDP protocol isn't correct
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jazeee.ddp.client.IDdpClient#connectionClosed(int, java.lang.String, boolean)
	 */
	@Override
	public void onConnectionClosed(int code, String reason, boolean isDisconnectedByRemote) {
		DdpDisconnectedMessage ddpDisconnectedMessage = new DdpDisconnectedMessage(Integer.toString(code), reason, isDisconnectedByRemote);
		log.debug("Java client closed: {}", ddpDisconnectedMessage);
		connectionState.set(ConnectionState.CLOSED);
		notifyConnectionListeners(ddpDisconnectedMessage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jazeee.ddp.client.IDdpClient#handleError(java.lang.Exception)
	 */
	@Override
	public void onError(Throwable throwable) {
		String reason = throwable.getMessage();
		if (reason == null) {
			reason = "Unknown websocket error (exception in callback?)";
		}
		DdpTopLevelErrorMessage ddpTopLevelErrorMessage = new DdpTopLevelErrorMessage(reason, "JavaWebSocketClient");
		log.debug("{}", ddpTopLevelErrorMessage);
		notifyTopLevelErrorListeners(ddpTopLevelErrorMessage);
	}

	/**
	 * Increments and returns the client's current ID
	 * 
	 * @return DDP call ID
	 */
	private String getNextId() {
		return Long.toString(currentMessageId.incrementAndGet());
	}

	/**
	 * Call a meteor method with the supplied parameters
	 * 
	 * @param method name of corresponding Meteor method
	 * @param params arguments to be passed to the Meteor method
	 * @return ID for next command
	 */
	public String callMethod(String method, Object[] params) {
		String id = getNextId();
		DdpMethodCallMessage ddpMethodCallMessage = new DdpMethodCallMessage(id, method, params);
		send(ddpMethodCallMessage);
		return id;
	}

	/**
	 * Subscribe to a Meteor record set with the supplied parameters
	 * 
	 * @param name name of the corresponding Meteor subscription
	 * @param params arguments corresponding to the Meteor subscription
	 * @return ID for next command
	 */
	public String subscribe(String name, Object[] params) {
		String id = getNextId();
		DdpSubscribeMessage ddpSubscribeMessage = new DdpSubscribeMessage(id, name, params);
		send(ddpSubscribeMessage);
		return id;
	}

	/**
	 * If you have the subscription ID instead of the name, you can use this to unsubscribe
	 * 
	 * @param subId subscription ID from when you subscribed
	 * @return ID for next command
	 */
	public String unsubscribe(String subId) {
		DdpUnSubscribeMessage ddpUnSubscribeMessage = new DdpUnSubscribeMessage(subId);
		send(ddpUnSubscribeMessage);
		return subId;
	}

	/**
	 * Inserts document into collection from client-side
	 * 
	 * @param collectionName Name of collection
	 * @param insertParams Document fields
	 * @return Returns command ID
	 */
	public String insertIntoCollection(String collectionName, Map<String, Object> insertParams) {
		Object[] collArgs = new Object[1];
		collArgs[0] = insertParams;
		return callMethod("/" + collectionName + "/insert", collArgs);
	}

	/**
	 * Deletes collection document from the client
	 * 
	 * @param collectionName Name of collection
	 * @param docId _id of document
	 * @return Returns command ID
	 */
	public String deleteFromCollection(String collectionName, String docId) {
		Object[] collArgs = new Object[1];
		Map<String, Object> selector = new HashMap<String, Object>();
		selector.put("_id", docId);
		collArgs[0] = selector;
		return callMethod("/" + collectionName + "/remove", collArgs);
	}

	/**
	 * Updates a collection document from the client NOTE: for security reasons, you can only do this one document at a time.
	 * 
	 * @param collectionName Name of collection
	 * @param docId _id of document
	 * @param updateParams Map w/ mongoDB parameters to pass in for update
	 * @return Returns command ID
	 */
	public String updateCollection(String collectionName, String docId, Map<String, Object> updateParams) {
		Map<String, Object> selector = new HashMap<String, Object>();
		Object[] collArgs = new Object[2];
		selector.put("_id", docId);
		collArgs[0] = selector;
		collArgs[1] = updateParams;
		return callMethod("/" + collectionName + "/update", collArgs);
	}

	/**
	 * Pings the server...you'll get a Pong message back in the DDPListener
	 * 
	 * @param pingId of ping message so you can tell if you have data loss
	 */
	public void ping(String pingId) {
		send(new DdpServerPingMessage(pingId));
	}

	public void send(IDdpServerMessage ddpServerMessage) {
		sendJson(ddpServerMessage.toJson());
	}

	private void sendJson(String json) {
		String substring = json.substring(0, Math.min(1000, json.length()));
		if (json.length() > 1000) {
			substring += "...";
		}
		log.debug("Sending {}", substring);
		try {
			this.ddpWebSocketClientAtomicReference.get().sendText(json);
		} catch (IOException ex) {
			onError(ex);
			connectionState.set(ConnectionState.CLOSED);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jazeee.ddp.client.IDdpClient#onReceived(java.lang.String)
	 */
	@Override
	public void onReceived(String jsonMessage) {
		log.debug("Received response: {}...", jsonMessage.substring(0, Math.min(1000, jsonMessage.length())));
		Gson gson = GsonClientMessagesDeserializer.getGsonConverter();
		DdpClientMessages ddpClientMessages = gson.fromJson(jsonMessage, DdpClientMessages.class);
		for (DdpClientMessageType ddpClientMessageType : ddpClientMessages.keySet()) {
			IDdpClientMessage ddpClientMessage = ddpClientMessages.get(ddpClientMessageType);
			if (ddpClientMessageType == null) {
				// ignore {"server_id":"GqrKrbcSeDfTYDkzQ"} web socket msgs
				continue;
			}
			if (ddpClientMessageType.equals(DdpClientMessageType.CONNECTED)) {
				connectionState.set(ConnectionState.CONNECTED);
			} else if (ddpClientMessageType.equals(DdpClientMessageType.FAILED)) {
				connectionState.set(ConnectionState.CLOSED);
			}
			switch (ddpClientMessageType) {
			case CONNECTED:
			case FAILED:
				notifyConnectionListeners((IDdpClientConnectionMessage) ddpClientMessage);
				break;
			case READY:
			case NOSUB:
				notifySubscriptionListeners((IDdpSubscriptionMessage) ddpClientMessage);
				break;
			case RESULT:
			case UPDATED:
				notifyMethodCallListeners((IDdpMethodCallMessage) ddpClientMessage);
				break;
			case ADDED:
			case ADDED_BEFORE:
			case CHANGED:
			case REMOVED:
			case MOVED_BEFORE:
				notifyCollectionListeners((IDdpCollectionMessage) ddpClientMessage);
				break;
			case PING:
			case PONG:
				notifyHeartbeatListeners((IDdpClientHeartbeatMessage) ddpClientMessage);
				break;
			case ERROR:
				notifyTopLevelErrorListeners((DdpTopLevelErrorMessage) ddpClientMessage);
				break;
			default:
				log.warn("Ignoring message type: {}", ddpClientMessageType);
				break;
			}
		}
	}

	/**
	 * @return current DDP connection state (disconnected/connected/closed)
	 */
	public ConnectionState getConnectionState() {
		return connectionState.get();
	}

	public void addHeartbeatListener(IDdpHeartbeatListener ddpHeartbeatListener) {
		this.heartbeatNotifier.addListener(ddpHeartbeatListener);
	}

	private void notifyHeartbeatListeners(IDdpClientHeartbeatMessage ddpClientHeartbeatMessage) {
		this.heartbeatNotifier.notifyListeners(ddpClientHeartbeatMessage);
	}

	public void addConnectionListener(IDdpConnectionListener ddpConnectionListener) {
		this.connectionNotifier.addListener(ddpConnectionListener);
	}

	private void notifyConnectionListeners(IDdpClientConnectionMessage ddpClientConnectionMessage) {
		this.connectionNotifier.notifyListeners(ddpClientConnectionMessage);
	}

	public void addMethodCallListener(IDdpMethodCallListener ddpMethodCallListener) {
		this.methodCallNotifier.addListener(ddpMethodCallListener);
	}

	private void notifyMethodCallListeners(IDdpMethodCallMessage ddpMethodCallMessage) {
		this.methodCallNotifier.notifyListeners(ddpMethodCallMessage);
	}

	public void addSubscriptionListener(IDdpSubscriptionListener ddpSubscriptionListener) {
		this.subscriptionNotifier.addListener(ddpSubscriptionListener);
	}

	private void notifySubscriptionListeners(IDdpSubscriptionMessage ddpSubscriptionMessage) {
		this.subscriptionNotifier.notifyListeners(ddpSubscriptionMessage);
	}

	public void addCollectionListener(IDdpCollectionListener ddpCollectionListener) {
		this.collectionNotifier.addListener(ddpCollectionListener);
	}

	private void notifyCollectionListeners(IDdpCollectionMessage ddpCollectionMessage) {
		this.collectionNotifier.notifyListeners(ddpCollectionMessage);
	}

	public void addTopLevelErrorListener(IDdpTopLevelErrorListener ddpTopLevelErrorListener) {
		this.topLevelErrorNotifier.addListener(ddpTopLevelErrorListener);
	}

	private void notifyTopLevelErrorListeners(DdpTopLevelErrorMessage ddpTopLevelErrorMessage) {
		this.topLevelErrorNotifier.notifyListeners(ddpTopLevelErrorMessage);
	}

	@Override
	public void processMessage(IDdpClientHeartbeatMessage ddpClientHeartbeatMessage) {
		if (ddpClientHeartbeatMessage instanceof DdpClientPingMessage) {
			// automatically send PONG command back to server
			DdpClientPingMessage ddpClientPingMessage = (DdpClientPingMessage) ddpClientHeartbeatMessage;
			send(ddpClientPingMessage.createPongResponse());
		}
	}

	@Override
	public void processMessage(DdpTopLevelErrorMessage ddpTopLevelErrorMessage) {
		log.error("{}", ddpTopLevelErrorMessage);
	}
}