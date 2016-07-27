package com.keysolutions.ddpclient;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
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
import com.jazeee.ddp.listeners.IDdpHeartbeatListener;
import com.jazeee.ddp.listeners.IDdpListener;
import com.jazeee.ddp.messages.DdpClientMessageType;
import com.jazeee.ddp.messages.DdpClientMessages;
import com.jazeee.ddp.messages.IDdpClientMessage;
import com.jazeee.ddp.messages.client.heartbeat.DdpClientPingMessage;
import com.jazeee.ddp.messages.client.heartbeat.DdpClientPongMessage;
import com.jazeee.ddp.messages.client.heartbeat.IDdpClientHeartbeatMessage;
import com.jazeee.ddp.messages.client.methodCalls.DdpMethodResultMessage;
import com.jazeee.ddp.messages.client.methodCalls.DdpMethodUpdatedMessage;
import com.jazeee.ddp.messages.client.subscriptions.DdpNoSubscriptionMessage;
import com.jazeee.ddp.messages.client.subscriptions.DdpSubscriptionReadyMessage;
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
public class DdpClient {
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

	/** DDP connection state */
	public enum ConnectionState {
		DISCONNECTED, CONNECTED, CLOSED,
	}

	private ConnectionState connectionState;
	/** current command ID */
	private AtomicLong currentMessageId;
	/** callback tracking for DDP commands */
	private Map<String, IDdpListener> messageListeners;
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

	private final Set<IDdpListener> ddpListeners = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<IDdpListener, Boolean>()));

	private final Notifier<IDdpHeartbeatListener, IDdpClientHeartbeatMessage> heartbeatNotifier = new Notifier<>(new HeartbeatNotificationProcessor());

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
		this.messageListeners = new ConcurrentHashMap<String, IDdpListener>();
		ddpListeners.clear();
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
	 * @param remote Whether error is from remote side
	 */
	private void connectionClosed(int code, String reason, boolean remote) {
		// changed formatting to always return a JSON object
		String closeMsg = "{\"msg\":\"closed\",\"code\":\"" + code + "\",\"reason\":\"" + reason + "\",\"remote\":" + remote + "}";
		log.debug("{}", closeMsg);
		onReceived(closeMsg);
	}

	/**
	 * Error handling for any errors over the web-socket connection
	 * 
	 * @param ex exception to convert to event
	 */
	private void handleError(Exception ex) {
		// changed formatting to always return a JSON object
		String errmsg = ex.getMessage();
		if (errmsg == null) {
			errmsg = "Unknown websocket error (exception in callback?)";
		}
		String errorMsg = "{\"msg\":\"error\",\"source\":\"WebSocketClient\",\"errormsg\":\"" + errmsg + "\"}";
		log.debug("{}", errorMsg);
		onReceived(errorMsg);
	}

	/**
	 * Increments and returns the client's current ID
	 * 
	 * @return DDP call ID
	 */
	private long nextId() {
		return currentMessageId.incrementAndGet();
	}

	/**
	 * Registers a client DDP command results callback listener
	 * 
	 * @param resultListener command results callback
	 * @return ID for next command
	 */
	private String addCommmand(IDdpListener resultListener) {
		if (resultListener != null) {
			long id = nextId();
			// store listener for callbacks
			String idString = Long.toString(id);
			messageListeners.put(idString, resultListener);
			return idString;
		}
		return "";
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
	 * @param resultListener DDP command listener for this method call
	 * @return ID for next command
	 */
	public String call(String method, Object[] params, IDdpListener resultListener) {
		String id = addCommmand(resultListener);
		DdpMethodCallMessage ddpMethodCallMessage = new DdpMethodCallMessage(id, method, params);
		send(ddpMethodCallMessage);
		return id;
	}

	/**
	 * Call a meteor method with the supplied parameters
	 * 
	 * @param method name of corresponding Meteor method
	 * @param params arguments to be passed to the Meteor method
	 * @return ID for next command
	 */
	public String call(String method, Object[] params) {
		return call(method, params, null);
	}

	/**
	 * Subscribe to a Meteor record set with the supplied parameters
	 * 
	 * @param name name of the corresponding Meteor subscription
	 * @param params arguments corresponding to the Meteor subscription
	 * @param resultListener DDP command listener for this call
	 * @return ID for next command
	 */
	public String subscribe(String name, Object[] params, IDdpListener resultListener) {
		String id = addCommmand(resultListener);
		DdpSubscribeMessage ddpSubscribeMessage = new DdpSubscribeMessage(id, name, params);
		send(ddpSubscribeMessage);
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
		return subscribe(name, params, null);
	}

	/**
	 * If you have the subscription ID instead of the name, you can use this to unsubscribe
	 * 
	 * @param subId subscription ID from when you subscribed
	 * @param resultListener result listener
	 * @return ID for next command
	 */
	public String unsubscribe(String subId, IDdpListener resultListener) {
		if (subId == null) {
			subId = addCommmand(resultListener);
		}
		DdpUnSubscribeMessage ddpUnSubscribeMessage = new DdpUnSubscribeMessage(subId);
		send(ddpUnSubscribeMessage);
		return subId;
	}

	/**
	 * If you have the subscription ID instead of the name, you can use this to unsubscribe
	 * 
	 * @param subId subscription ID from when you subscribed
	 * @return ID for next command
	 */
	public String unsubscribe(String subId) {
		return unsubscribe(subId, null);
	}

	/**
	 * Inserts document into collection from the client
	 * 
	 * @param collectionName Name of collection
	 * @param insertParams Document fields
	 * @param resultListener DDP command listener for this call
	 * @return Returns command ID
	 */
	public String collectionInsert(String collectionName, Map<String, Object> insertParams, IDdpListener resultListener) {
		Object[] collArgs = new Object[1];
		collArgs[0] = insertParams;
		return call("/" + collectionName + "/insert", collArgs);
	}

	/**
	 * Inserts document into collection from client-side
	 * 
	 * @param collectionName Name of collection
	 * @param insertParams Document fields
	 * @return Returns command ID
	 */
	public String collectionInsert(String collectionName, Map<String, Object> insertParams) {
		return collectionInsert(collectionName, insertParams, null);
	}

	/**
	 * Deletes collection document from the client
	 * 
	 * @param collectionName Name of collection
	 * @param docId _id of document
	 * @param resultListener Callback handler for command results
	 * @return Returns command ID
	 */
	public String collectionDelete(String collectionName, String docId, IDdpListener resultListener) {
		Object[] collArgs = new Object[1];
		Map<String, Object> selector = new HashMap<String, Object>();
		selector.put("_id", docId);
		collArgs[0] = selector;
		return call("/" + collectionName + "/remove", collArgs);
	}

	public String collectionDelete(String collectionName, String docId) {
		return collectionDelete(collectionName, docId, null);
	}

	/**
	 * Updates a collection document from the client NOTE: for security reasons, you can only do this one document at a time.
	 * 
	 * @param collectionName Name of collection
	 * @param docId _id of document
	 * @param updateParams Map w/ mongoDB parameters to pass in for update
	 * @param resultListener Callback handler for command results
	 * @return Returns command ID
	 */
	public String collectionUpdate(String collectionName, String docId, Map<String, Object> updateParams, IDdpListener resultListener) {
		Map<String, Object> selector = new HashMap<String, Object>();
		Object[] collArgs = new Object[2];
		selector.put("_id", docId);
		collArgs[0] = selector;
		collArgs[1] = updateParams;
		return call("/" + collectionName + "/update", collArgs);
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
		return collectionUpdate(collectionName, docId, updateParams, null);
	}

	/**
	 * Pings the server...you'll get a Pong message back in the DDPListener
	 * 
	 * @param pingId of ping message so you can tell if you have data loss
	 * @param resultListener DDP command listener for this call
	 */
	public void ping(String pingId, IDdpListener resultListener) {
		if (resultListener != null) {
			messageListeners.put(pingId, resultListener);
		}
		DdpServerPingMessage ddpServerPingMessage = new DdpServerPingMessage(pingId);
		send(ddpServerPingMessage);
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
			case UPDATED:
				List<String> methodIds = ((DdpMethodUpdatedMessage) ddpClientMessage).getMethods();
				for (String methodId : methodIds) {
					IDdpListener listener = messageListeners.get(methodId);
					if (listener != null) {
						listener.onUpdated(methodId);
					}
				}
				break;
			case READY:
				List<String> subscriptionIds = ((DdpSubscriptionReadyMessage) ddpClientMessage).getSubscriptionIds();
				for (String subscriptionId : subscriptionIds) {
					IDdpListener listener = messageListeners.get(subscriptionId);
					if (listener != null) {
						listener.onSubscriptionReady(subscriptionId);
					}
				}
				break;
			case NOSUB:
				DdpNoSubscriptionMessage ddpNoSubscriptionMessage = (DdpNoSubscriptionMessage) ddpClientMessage;
				String noSubscriptionMessageId = ddpNoSubscriptionMessage.getId();
				if (noSubscriptionMessageId == null) {
					log.warn("No such subscription ID found!");
				} else {
					IDdpListener listener = messageListeners.get(noSubscriptionMessageId);
					if (listener != null) {
						listener.onNoSub(ddpNoSubscriptionMessage);
						messageListeners.remove(noSubscriptionMessageId);
					}
				}
				break;
			case RESULT:
				DdpMethodResultMessage ddpMethodResultMessage = (DdpMethodResultMessage) ddpClientMessage;
				String methodCallId = ddpMethodResultMessage.getId();
				if (methodCallId != null) {
					IDdpListener listener = messageListeners.get(methodCallId);
					if (listener != null) {
						listener.onResult(ddpMethodResultMessage);
						messageListeners.remove(methodCallId);
					}
				}
				break;
			case CONNECTED:
				connectionState = ConnectionState.CONNECTED;
				break;
			case CLOSED:
				connectionState = ConnectionState.CLOSED;
				break;
			case PING:
				DdpClientPingMessage ddpClientPingMessage = ((DdpClientPingMessage) ddpClientMessage);
				// automatically send PONG command back to server
				send(ddpClientPingMessage.createPongResponse());
				notifyHeartbeatListeners(ddpClientPingMessage);
				break;
			case PONG:
				DdpClientPongMessage ddpClientPongMessage = ((DdpClientPongMessage) ddpClientMessage);
				notifyHeartbeatListeners(ddpClientPongMessage);
				break;
			case ERROR:
				log.error("{}", ddpClientMessage);
				break;
			default:
				log.warn("Ignoring message type: {}", ddpClientMessageType);
				break;
			}
		}
		for (IDdpListener ddpListener : ddpListeners) {
			ddpListener.onDdpMessage(ddpClientMessages);
		}
	}

	public void addDDPListener(IDdpListener ddpListener) {
		ddpListeners.add(ddpListener);
	}

	public void removeDDPListener(IDdpListener ddpListener) {
		ddpListeners.remove(ddpListener);
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
}