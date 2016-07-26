package com.keysolutions.ddpclient;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
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
import com.jazeee.ddp.IDDPListener;

/**
 * Java Meteor DDP websocket client
 * 
 */
public class DDPClient {
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

	/** Field names supported in the DDP protocol */
	public static class DdpMessageField {
		public static final String MSG = "msg";
		public static final String ID = "id";
		public static final String METHOD = "method";
		public static final String METHODS = "methods";
		public static final String SUBS = "subs";
		public static final String PARAMS = "params";
		public static final String RESULT = "result";
		public static final String NAME = "name";
		public static final String SERVER_ID = "server_id";
		public static final String ERROR = "error";
		public static final String SESSION = "session";
		public static final String VERSION = "version";
		public static final String SUPPORT = "support";
		public static final String SOURCE = "source";
		public static final String ERRORMSG = "errormsg";
		public static final String CODE = "code";
		public static final String REASON = "reason";
		public static final String REMOTE = "remote";
		public static final String COLLECTION = "collection";
		public static final String FIELDS = "fields";
		public static final String CLEARED = "cleared";
	}

	/** Message types supported in the DDP protocol */
	public static class DdpMessageType {
		// client -> server
		public static final String CONNECT = "connect";
		public static final String METHOD = "method";
		// server -> client
		public static final String CONNECTED = "connected";
		public static final String UPDATED = "updated";
		public static final String READY = "ready";
		public static final String NOSUB = "nosub";
		public static final String RESULT = "result";
		public static final String SUB = "sub";
		public static final String UNSUB = "unsub";
		public static final String ERROR = "error";
		public static final String CLOSED = "closed";
		public static final String ADDED = "added";
		public static final String REMOVED = "removed";
		public static final String CHANGED = "changed";
		public static final String PING = "ping";
		public static final String PONG = "pong";
	}

	/** DDP protocol version */
	private final static String DDP_PROTOCOL_VERSION = "1";

	/** DDP connection state */
	public enum CONNSTATE {
		Disconnected, Connected, Closed,
	}

	private CONNSTATE mConnState;
	/** current command ID */
	private AtomicLong mCurrentId;
	/** callback tracking for DDP commands */
	private Map<String, IDDPListener> mMsgListeners;
	/** web socket client */
	private WebSocketClient mWsClient;
	/** web socket address for reconnections */
	private String mMeteorServerAddress;
	/** trust managers for reconnections */
	private TrustManager[] mTrustManagers;
	/** we can't connect more than once on a new socket */
	private boolean mConnectionStarted;
	/** Google GSON object */
	private final Gson mGson;

	private final Set<IDDPListener> ddpListeners = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<IDDPListener, Boolean>()));

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
	public DDPClient(String meteorServerIp, Integer meteorServerPort, boolean useSSL, Gson gson) throws URISyntaxException {
		mGson = gson;
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
	public DDPClient(String meteorServerIp, Integer meteorServerPort, boolean useSSL) throws URISyntaxException {
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
	public DDPClient(String meteorServerIp, Integer meteorServerPort, TrustManager[] trustManagers, Gson gson) throws URISyntaxException {
		mGson = gson;
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
	public DDPClient(String meteorServerIp, Integer meteorServerPort, TrustManager[] trustManagers) throws URISyntaxException {
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
	public DDPClient(String meteorServerIp, Integer meteorServerPort, Gson gson) throws URISyntaxException {
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
	public DDPClient(String meteorServerIp, Integer meteorServerPort) throws URISyntaxException {
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
		mConnState = CONNSTATE.Disconnected;
		if (meteorServerPort == null)
			meteorServerPort = 3000;
		mMeteorServerAddress = (trustManagers != null ? "wss://" : "ws://") + meteorServerIp + ":" + meteorServerPort.toString() + "/websocket";
		this.mCurrentId = new AtomicLong(0);
		this.mMsgListeners = new ConcurrentHashMap<String, IDDPListener>();
		ddpListeners.clear();
		createWsClient(mMeteorServerAddress);

		mTrustManagers = trustManagers;
		initWsClientSSL();
	}

	/**
	 * initializes WS client's trust managers
	 */
	private void initWsClientSSL() {
		if (mTrustManagers != null) {
			try {
				SSLContext sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, mTrustManagers, null);
				// now we can set the web service client to use this SSL context
				mWsClient.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sslContext));
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
		this.mWsClient = new WebSocketClient(new URI(meteorServerAddress)) {

			@Override
			public void onOpen(ServerHandshake handshakedata) {
				connectionOpened();
			}

			@Override
			public void onMessage(String message) {
				received(message);
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
		mConnectionStarted = false;
	}

	/**
	 * Called after initial web-socket connection. Sends back a connection confirmation message to the Meteor server.
	 */
	private void connectionOpened() {
		log.trace("WebSocket connection opened");
		// reply to Meteor server with connection confirmation message ({"msg":
		// "connect"})
		Map<String, Object> connectMsg = new HashMap<String, Object>();
		connectMsg.put(DdpMessageField.MSG, DdpMessageType.CONNECT);
		connectMsg.put(DdpMessageField.VERSION, DDP_PROTOCOL_VERSION);
		connectMsg.put(DdpMessageField.SUPPORT, new String[] { DDP_PROTOCOL_VERSION });
		send(connectMsg);
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
		received(closeMsg);
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
		received(errorMsg);
	}

	/**
	 * Increments and returns the client's current ID
	 * 
	 * @return DDP call ID
	 */
	private long nextId() {
		return mCurrentId.incrementAndGet();
	}

	/**
	 * Registers a client DDP command results callback listener
	 * 
	 * @param resultListener command results callback
	 * @return ID for next command
	 */
	private long addCommmand(IDDPListener resultListener) {
		long id = nextId();
		if (resultListener != null) {
			// store listener for callbacks
			mMsgListeners.put(Long.toString(id), resultListener);
		}
		return id;
	}

	/**
	 * Initiate connection to meteor server
	 */
	public void connect() {
		if (this.mWsClient.getReadyState() == READYSTATE.CLOSED) {
			// we need to create a new wsClient because a closed websocket cannot be reused
			try {
				createWsClient(mMeteorServerAddress);
				initWsClientSSL();
			} catch (URISyntaxException e) {
				// we shouldn't get URI exceptions because the address was validated in initWebsocket
			}
		}
		if (!mConnectionStarted) {
			// only do the connect if no connection attempt has been done for this websocket client
			this.mWsClient.connect();
			mConnectionStarted = true;
		}
	}

	/**
	 * Closes an open websocket connection. This is async, so you'll get a close notification callback when it eventually closes.
	 */
	public void disconnect() {
		if (this.mWsClient.getReadyState() == READYSTATE.OPEN) {
			this.mWsClient.close();
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
	public long call(String method, Object[] params, IDDPListener resultListener) {
		Map<String, Object> callMsg = new HashMap<String, Object>();
		callMsg.put(DdpMessageField.MSG, DdpMessageType.METHOD);
		callMsg.put(DdpMessageField.METHOD, method);
		callMsg.put(DdpMessageField.PARAMS, params);

		long id = addCommmand(resultListener);
		callMsg.put(DdpMessageField.ID, Long.toString(id));
		send(callMsg);
		return id;
	}

	/**
	 * Call a meteor method with the supplied parameters
	 * 
	 * @param method name of corresponding Meteor method
	 * @param params arguments to be passed to the Meteor method
	 * @return ID for next command
	 */
	public long call(String method, Object[] params) {
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
	public long subscribe(String name, Object[] params, IDDPListener resultListener) {
		Map<String, Object> subMsg = new HashMap<String, Object>();
		subMsg.put(DdpMessageField.MSG, DdpMessageType.SUB);
		subMsg.put(DdpMessageField.NAME, name);
		subMsg.put(DdpMessageField.PARAMS, params);

		long id = addCommmand(resultListener);
		subMsg.put(DdpMessageField.ID, Long.toString(id));
		send(subMsg);
		return id;
	}

	/**
	 * Subscribe to a Meteor record set with the supplied parameters
	 * 
	 * @param name name of the corresponding Meteor subscription
	 * @param params arguments corresponding to the Meteor subscription
	 * @return ID for next command
	 */
	public long subscribe(String name, Object[] params) {
		return subscribe(name, params, null);
	}

	/**
	 * If you have the subscription ID instead of the name, you can use this to unsubscribe
	 * 
	 * @param subId subscription ID from when you subscribed
	 * @param resultListener result listener
	 * @return ID for next command
	 */
	public long unsubscribe(long subId, IDDPListener resultListener) {
		Map<String, Object> unsubMsg = new HashMap<String, Object>();
		unsubMsg.put(DdpMessageField.MSG, DdpMessageType.UNSUB);
		unsubMsg.put(DdpMessageField.ID, Long.toString(subId));

		long id = addCommmand(resultListener);
		send(unsubMsg);
		return id;
	}

	/**
	 * If you have the subscription ID instead of the name, you can use this to unsubscribe
	 * 
	 * @param subId subscription ID from when you subscribed
	 * @return ID for next command
	 */
	public long unsubscribe(long subId) {
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
	public long collectionInsert(String collectionName, Map<String, Object> insertParams, IDDPListener resultListener) {
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
	public long collectionInsert(String collectionName, Map<String, Object> insertParams) {
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
	public long collectionDelete(String collectionName, String docId, IDDPListener resultListener) {
		Object[] collArgs = new Object[1];
		Map<String, Object> selector = new HashMap<String, Object>();
		selector.put("_id", docId);
		collArgs[0] = selector;
		return call("/" + collectionName + "/remove", collArgs);
	}

	public long collectionDelete(String collectionName, String docId) {
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
	public long collectionUpdate(String collectionName, String docId, Map<String, Object> updateParams, IDDPListener resultListener) {
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
	public long collectionUpdate(String collectionName, String docId, Map<String, Object> updateParams) {
		return collectionUpdate(collectionName, docId, updateParams, null);
	}

	/**
	 * Pings the server...you'll get a Pong message back in the DDPListener
	 * 
	 * @param pingId of ping message so you can tell if you have data loss
	 * @param resultListener DDP command listener for this call
	 */
	public void ping(String pingId, IDDPListener resultListener) {
		Map<String, Object> pingMsg = new HashMap<String, Object>();
		pingMsg.put(DdpMessageField.MSG, DdpMessageType.PING);
		if (pingId != null) {
			pingMsg.put(DdpMessageField.ID, pingId);
		}
		send(pingMsg);
		if (resultListener != null) {
			// store listener for callbacks
			mMsgListeners.put(pingId, resultListener);
		}
	}

	/**
	 * Converts DDP-formatted message to JSON and sends over web-socket
	 * 
	 * @param msgParams parameters for DDP msg
	 */
	public void send(Map<String, Object> msgParams) {
		String json = mGson.toJson(msgParams);
		log.debug("Sending {}...", json.substring(0, Math.min(1000, json.length())));
		try {
			this.mWsClient.send(json);
		} catch (WebsocketNotConnectedException ex) {
			handleError(ex);
			mConnState = CONNSTATE.Closed;
		}
	}

	/**
	 * Notifies observers of this DDP client of messages received from the Meteor server
	 * 
	 * @param msg received msg from websocket
	 */
	@SuppressWarnings("unchecked")
	public void received(String msg) {
		log.debug("Received response: {}...", msg.substring(0, Math.min(1000, msg.length())));
		// generic object deserialization is from
		// http://programmerbruce.blogspot.com/2011/06/gson-v-jackson.html
		Map<String, Object> jsonFields = mGson.fromJson(msg, HashMap.class);

		// notify any command listeners if we get updated or result msgs
		String msgtype = (String) jsonFields.get(DdpMessageField.MSG);
		if (msgtype == null) {
			// ignore {"server_id":"GqrKrbcSeDfTYDkzQ"} web socket msgs
			return;
		}
		if (msgtype.equals(DdpMessageType.UPDATED)) {
			ArrayList<String> methodIds = (ArrayList<String>) jsonFields.get(DdpMessageField.METHODS);
			for (String methodId : methodIds) {
				IDDPListener listener = mMsgListeners.get(methodId);
				if (listener != null) {
					listener.onUpdated(methodId);
				}
			}
		} else if (msgtype.equals(DdpMessageType.READY)) {
			ArrayList<String> subscriptionIds = (ArrayList<String>) jsonFields.get(DdpMessageField.SUBS);
			for (String subscriptionId : subscriptionIds) {
				IDDPListener listener = mMsgListeners.get(subscriptionId);
				if (listener != null) {
					listener.onSubscriptionReady(subscriptionId);
				}
			}
		} else if (msgtype.equals(DdpMessageType.NOSUB)) {
			String msgId = (String) jsonFields.get(DdpMessageField.ID);
			if (msgId == null) {
				log.warn("No such subscription ID found!");
			} else {
				IDDPListener listener = mMsgListeners.get(msgId);
				if (listener != null) {
					listener.onNoSub(msgId, (Map<String, Object>) jsonFields.get(DdpMessageField.ERROR));
					mMsgListeners.remove(msgId);
				}
			}
		} else if (msgtype.equals(DdpMessageType.RESULT)) {
			String msgId = (String) jsonFields.get(DdpMessageField.ID);
			if (msgId != null) {
				IDDPListener listener = mMsgListeners.get(msgId);
				if (listener != null) {
					listener.onResult(jsonFields);
					mMsgListeners.remove(msgId);
				}
			}
		} else if (msgtype.equals(DdpMessageType.CONNECTED)) {
			mConnState = CONNSTATE.Connected;
		} else if (msgtype.equals(DdpMessageType.CLOSED)) {
			mConnState = CONNSTATE.Closed;
		} else if (msgtype.equals(DdpMessageType.PING)) {
			String pongId = (String) jsonFields.get(DdpMessageField.ID);
			// automatically send PONG command back to server
			Map<String, Object> pongMsg = new HashMap<String, Object>();
			pongMsg.put(DdpMessageField.MSG, DdpMessageType.PONG);
			if (pongId != null) {
				pongMsg.put(DdpMessageField.ID, pongId);
			}
			send(pongMsg);
		} else if (msgtype.equals(DdpMessageType.PONG)) {
			String pingId = (String) jsonFields.get(DdpMessageField.ID);
			// let listeners know a Pong happened
			IDDPListener listener = mMsgListeners.get(pingId);
			if (listener != null) {
				listener.onPong(pingId);
				mMsgListeners.remove(pingId);
			}
		} else {
			log.warn("Ignorming message type: {}", msgtype);
		}
		for (IDDPListener ddpListener : ddpListeners) {
			ddpListener.onDDPMessage(jsonFields);
		}
	}

	public void addDDPListener(IDDPListener ddpListener) {
		ddpListeners.add(ddpListener);
	}

	public void removeDDPListener(IDDPListener ddpListener) {
		ddpListeners.remove(ddpListener);
	}

	/**
	 * @return current DDP connection state (disconnected/connected/closed)
	 */
	public CONNSTATE getState() {
		return mConnState;
	}
}