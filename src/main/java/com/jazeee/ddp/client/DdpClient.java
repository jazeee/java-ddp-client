package com.jazeee.ddp.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.java_websocket.WebSocket.READYSTATE;
import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

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
import com.jazeee.ddp.messages.client.connection.DdpConnectedMessage;
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
public class DdpClient implements IDdpHeartbeatListener, IDdpConnectionListener, IDdpTopLevelErrorListener {
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

	/** DDP connection state */
	public enum ConnectionState {
		DISCONNECTED, CONNECTED, CLOSED,
	}

	private ConnectionState connectionState;
	/** current command ID */
	private AtomicLong currentMessageId;
	/** web socket client */
	private WebSocketClient webSocketClient;
	/** web socket address for reconnections */
	private String meteorServerIpAddress;
	/** trust managers for reconnections */
	private TrustManager[] trustManagers;
	/** we can't connect more than once on a new socket */
	private final AtomicBoolean isConnectionStarted = new AtomicBoolean(false);
	/** Google GSON object */
	private final Gson gson;

	private final Notifier<IDdpHeartbeatListener, IDdpClientHeartbeatMessage> heartbeatNotifier = new Notifier<>(new DdpClientHeartbeatNotificationProcessor());
	private final Notifier<IDdpConnectionListener, IDdpClientConnectionMessage> connectionNotifier = new Notifier<>(new DdpConnectionNotificationProcessor());
	private final Notifier<IDdpMethodCallListener, IDdpMethodCallMessage> methodCallNotifier = new Notifier<>(new DdpMethodCallNotificationProcessor());
	private final Notifier<IDdpSubscriptionListener, IDdpSubscriptionMessage> subscriptionNotifier = new Notifier<>(new DdpSubscriptionNotificationProcessor());
	private final Notifier<IDdpCollectionListener, IDdpCollectionMessage> collectionNotifier = new Notifier<>(new DdpCollectionNotificationProcessor());
	private final Notifier<IDdpTopLevelErrorListener, DdpTopLevelErrorMessage> topLevelErrorNotifier = new Notifier<>(new DdpTopLevelErrorNotificationProcessor());

	/**
	 * Instantiates a Meteor DDP client for the Meteor server located at the supplied IP and port (note: running Meteor locally will typically have a port of 3000 but port 80 is
	 * the typical default for publicly deployed servers)
	 * 
	 * @param meteorServerIp IP of Meteor server
	 * @param meteorServerPort Port of Meteor server, if left null it will default to 3000
	 * @param useSSL Whether to use SSL for websocket encryption
	 * @param gson A custom Gson instance to use for serialization
	 * @throws URISyntaxException URI error
	 */
	public DdpClient(String meteorServerIp, Integer meteorServerPort, boolean useSSL, Gson gson) throws URISyntaxException {
		this.gson = gson;
		initWebsocket(meteorServerIp, meteorServerPort, useSSL);
		this.heartbeatNotifier.addListener(this);
		this.connectionNotifier.addListener(this);
	}

	/**
	 * Instantiates a Meteor DDP client for the Meteor server located at the supplied IP and port (note: running Meteor locally will typically have a port of 3000 but port 80 is
	 * the typical default for publicly deployed servers)
	 *
	 * @param meteorServerIp IP of Meteor server
	 * @param meteorServerPort Port of Meteor server, if left null it will default to 3000
	 * @param useSSL Whether to use SSL for websocket encryption
	 * @throws URISyntaxException URI error
	 */
	public DdpClient(String meteorServerIp, Integer meteorServerPort, boolean useSSL) throws URISyntaxException {
		this(meteorServerIp, meteorServerPort, useSSL, new Gson());
	}

	/**
	 * Instantiates a Meteor DDP client for the Meteor server located at the supplied IP and port (note: running Meteor locally will typically have a port of 3000 but port 80 is
	 * the typical default for publicly deployed servers)
	 * 
	 * @param meteorServerIp IP of Meteor server
	 * @param meteorServerPort Port of Meteor server, if left null it will default to 3000
	 * @param trustManagers Explicitly defined trust managers, if null no SSL encryption would be used.
	 * @param gson A custom Gson instance to use for serialization
	 * @throws URISyntaxException URI error
	 */
	public DdpClient(String meteorServerIp, Integer meteorServerPort, TrustManager[] trustManagers, Gson gson) throws URISyntaxException {
		this.gson = gson;
		initWebsocket(meteorServerIp, meteorServerPort, trustManagers);
		this.heartbeatNotifier.addListener(this);
	}

	/**
	 * Instantiates a Meteor DDP client for the Meteor server located at the supplied IP and port (note: running Meteor locally will typically have a port of 3000 but port 80 is
	 * the typical default for publicly deployed servers)
	 *
	 * @param meteorServerIp IP of Meteor server
	 * @param meteorServerPort Port of Meteor server, if left null it will default to 3000
	 * @param trustManagers Explicitly defined trust managers, if null no SSL encryption would be used.
	 * @throws URISyntaxException URI error
	 */
	public DdpClient(String meteorServerIp, Integer meteorServerPort, TrustManager[] trustManagers) throws URISyntaxException {
		this(meteorServerIp, meteorServerPort, trustManagers, new Gson());
	}

	/**
	 * Instantiates a Meteor DDP client for the Meteor server located at the supplied IP and port (note: running Meteor locally will typically have a port of 3000 but port 80 is
	 * the typical default for publicly deployed servers)
	 * 
	 * @param meteorServerIp - IP of Meteor server
	 * @param meteorServerPort - Port of Meteor server, if left null it will default to 3000
	 * @param gson - A custom Gson instance to use for serialization
	 * @throws URISyntaxException URI error
	 */
	public DdpClient(String meteorServerIp, Integer meteorServerPort, Gson gson) throws URISyntaxException {
		this(meteorServerIp, meteorServerPort, false, gson);
	}

	/**
	 * Instantiates a Meteor DDP client for the Meteor server located at the supplied IP and port (note: running Meteor locally will typically have a port of 3000 but port 80 is
	 * the typical default for publicly deployed servers)
	 *
	 * @param meteorServerIp - IP of Meteor server
	 * @param meteorServerPort - Port of Meteor server, if left null it will default to 3000
	 * @throws URISyntaxException URI error
	 */
	public DdpClient(String meteorServerIp, Integer meteorServerPort) throws URISyntaxException {
		this(meteorServerIp, meteorServerPort, false);
	}

	/**
	 * Initializes a websocket connection
	 * 
	 * @param meteorServerIp IP address of Meteor server
	 * @param meteorServerPort port of Meteor server, if left null it will default to 3000
	 * @param useSSL whether to use SSL
	 * @throws URISyntaxException
	 */
	private void initWebsocket(String meteorServerIp, Integer meteorServerPort, boolean useSSL) throws URISyntaxException {
		TrustManager[] trustManagers = null;
		if (useSSL) {
			try {
				// set up trustkeystore w/ Java's default trusted
				TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				trustManagerFactory.init((KeyStore) null);
				trustManagers = trustManagerFactory.getTrustManagers();
			} catch (KeyStoreException e) {
				log.warn("Error accessing Java default cacerts keystore {}", e);
			} catch (NoSuchAlgorithmException e) {
				log.warn("Error accessing Java default trustmanager algorithms {}", e);
			}
		}

		initWebsocket(meteorServerIp, meteorServerPort, trustManagers);
	}

	/**
	 * Initializes a websocket connection
	 * 
	 * @param meteorServerIp IP address of Meteor server
	 * @param meteorServerPort port of Meteor server, if left null it will default to 3000
	 * @param trustManagers array explicitly defined trust managers, can be null
	 * @throws URISyntaxException
	 */
	private void initWebsocket(String meteorServerIp, Integer meteorServerPort, TrustManager[] trustManagers) throws URISyntaxException {
		connectionState = ConnectionState.DISCONNECTED;
		if (meteorServerPort == null)
			meteorServerPort = 3000;
		meteorServerIpAddress = (trustManagers != null ? "wss://" : "ws://") + meteorServerIp + ":" + meteorServerPort.toString() + "/websocket";
		this.currentMessageId = new AtomicLong(0);
		createWsClient(meteorServerIpAddress);

		this.trustManagers = trustManagers;
		initWsClientSSL();
	}

	/**
	 * initializes WS client's trust managers
	 */
	private void initWsClientSSL() {
		if (trustManagers != null) {
			try {
				SSLContext sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, trustManagers, null);
				// now we can set the web service client to use this SSL context
				webSocketClient.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sslContext));
			} catch (NoSuchAlgorithmException e) {
				log.warn("Error accessing Java default trustmanager algorithms {}", e);
			} catch (KeyManagementException e) {
				log.warn("Error accessing Java default cacert keys {}", e);
			}
		}
	}

	/**
	 * Creates a web socket client
	 * 
	 * @param meteorServerAddress Websocket address of Meteor server
	 * @throws URISyntaxException URI error
	 */
	public void createWsClient(String meteorServerAddress) throws URISyntaxException {
		this.webSocketClient = new WebSocketClient(new URI(meteorServerAddress)) {

			@Override
			public void onOpen(ServerHandshake handshakedata) {
				connectionOpened();
			}

			@Override
			public void onMessage(String message) {
				onReceived(message);
			}

			@Override
			public void onError(Exception ex) {
				handleError(ex);
			}

			@Override
			public void onClose(int code, String reason, boolean remote) {
				connectionClosed(code, reason, remote);
			}
		};
		isConnectionStarted.set(false);
	}

	/**
	 * Called after initial web-socket connection. Sends back a connection confirmation message to the Meteor server.
	 */
	private void connectionOpened() {
		log.trace("WebSocket connection opened");
		// reply to Meteor server with connection confirmation message ({"msg":
		// "connect"})
		send(new DdpConnectMessage());
		// we'll get a msg:connected from the Meteor server w/ a session ID when we connect
		// note that this may return an error that the DDP protocol isn't correct
	}

	/**
	 * Called when connection is closed
	 * 
	 * @param code WebSocket Error code
	 * @param reason Reason msg for error
	 * @param isDisconnectedByRemote Whether error is from remote side
	 */
	private void connectionClosed(int code, String reason, boolean isDisconnectedByRemote) {
		DdpDisconnectedMessage ddpDisconnectedMessage = new DdpDisconnectedMessage(Integer.toString(code), reason, isDisconnectedByRemote);
		log.debug("Java client closed: {}", ddpDisconnectedMessage);
		notifyConnectionListeners(ddpDisconnectedMessage);
	}

	/**
	 * Error handling for any errors over the web-socket connection
	 * 
	 * @param ex exception to convert to event
	 */
	private void handleError(Exception ex) {
		String reason = ex.getMessage();
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
	 * Initiate connection to meteor server
	 */
	public void connect() {
		// FIXME - Note that this is not thread safe. Two connections could occur within the next 6 lines.
		if (this.webSocketClient.getReadyState() == READYSTATE.CLOSED) {
			// we need to create a new wsClient because a closed websocket cannot be reused
			try {
				createWsClient(meteorServerIpAddress);
				initWsClientSSL();
			} catch (URISyntaxException e) {
				// we shouldn't get URI exceptions because the address was validated in initWebsocket
			}
		}
		// FIXME - Note that this is not thread safe. Two connections could occur within the next 4 lines.
		if (!isConnectionStarted.get()) {
			// only do the connect if no connection attempt has been done for this websocket client
			this.webSocketClient.connect();
			isConnectionStarted.set(true);
		}
	}

	/**
	 * Closes an open websocket connection. This is async, so you'll get a close notification callback when it eventually closes.
	 */
	public void disconnect() {
		if (this.webSocketClient.getReadyState() == READYSTATE.OPEN) {
			this.webSocketClient.close();
		}
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
	public String collectionInsert(String collectionName, Map<String, Object> insertParams) {
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
	public String collectionDelete(String collectionName, String docId) {
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
	public String collectionUpdate(String collectionName, String docId, Map<String, Object> updateParams) {
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

	/**
	 * Converts DDP-formatted message to JSON and sends over web-socket
	 * 
	 * @param msgParams parameters for DDP msg
	 */
	public void send(Map<String, Object> msgParams) {
		String json = gson.toJson(msgParams);
		sendJson(json);
	}

	public void send(IDdpServerMessage ddpServerMessage) {
		sendJson(ddpServerMessage.toJson());
	}

	private void sendJson(String json) {
		log.debug("Sending {}...", json.substring(0, Math.min(1000, json.length())));
		try {
			this.webSocketClient.send(json);
		} catch (WebsocketNotConnectedException ex) {
			handleError(ex);
			connectionState = ConnectionState.CLOSED;
		}
	}

	/**
	 * Notifies observers of this DDP client of messages received from the Meteor server
	 * 
	 * @param jsonMessage received msg from websocket
	 */
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
	public ConnectionState getState() {
		return connectionState;
	}

	public void addHeartbeatListener(IDdpHeartbeatListener ddpHeartbeatListener) {
		this.heartbeatNotifier.addListener(ddpHeartbeatListener);
	}

	public void notifyHeartbeatListeners(IDdpClientHeartbeatMessage ddpClientHeartbeatMessage) {
		this.heartbeatNotifier.notifyListeners(ddpClientHeartbeatMessage);
	}

	public void addConnectionListener(IDdpConnectionListener ddpConnectionListener) {
		this.connectionNotifier.addListener(ddpConnectionListener);
	}

	public void notifyConnectionListeners(IDdpClientConnectionMessage ddpClientConnectionMessage) {
		this.connectionNotifier.notifyListeners(ddpClientConnectionMessage);
	}

	public void addMethodCallListener(IDdpMethodCallListener ddpMethodCallListener) {
		this.methodCallNotifier.addListener(ddpMethodCallListener);
	}

	public void notifyMethodCallListeners(IDdpMethodCallMessage ddpMethodCallMessage) {
		this.methodCallNotifier.notifyListeners(ddpMethodCallMessage);
	}

	public void addSubscriptionListener(IDdpSubscriptionListener ddpSubscriptionListener) {
		this.subscriptionNotifier.addListener(ddpSubscriptionListener);
	}

	public void notifySubscriptionListeners(IDdpSubscriptionMessage ddpSubscriptionMessage) {
		this.subscriptionNotifier.notifyListeners(ddpSubscriptionMessage);
	}

	public void addCollectionListener(IDdpCollectionListener ddpCollectionListener) {
		this.collectionNotifier.addListener(ddpCollectionListener);
	}

	public void notifyCollectionListeners(IDdpCollectionMessage ddpCollectionMessage) {
		this.collectionNotifier.notifyListeners(ddpCollectionMessage);
	}

	public void addTopLevelErrorListener(IDdpTopLevelErrorListener ddpTopLevelErrorListener) {
		this.topLevelErrorNotifier.addListener(ddpTopLevelErrorListener);
	}

	public void notifyTopLevelErrorListeners(DdpTopLevelErrorMessage ddpTopLevelErrorMessage) {
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
	public void processMessage(IDdpClientConnectionMessage ddpClientConnectionMessage) {
		if (ddpClientConnectionMessage instanceof DdpConnectedMessage) {
			connectionState = ConnectionState.CONNECTED;
		} else {
			connectionState = ConnectionState.CLOSED;
		}
	}

	@Override
	public void processMessage(DdpTopLevelErrorMessage ddpTopLevelErrorMessage) {
		log.error("{}", ddpTopLevelErrorMessage);
	}
}