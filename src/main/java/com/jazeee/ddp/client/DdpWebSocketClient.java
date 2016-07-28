package com.jazeee.ddp.client;

import java.io.Closeable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.java_websocket.WebSocket.READYSTATE;
import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import com.jazeee.common.utils.nullability.NotNull;
import com.jazeee.common.utils.nullability.Nullable;

public class DdpWebSocketClient implements Closeable {
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
	private final IDdpClient ddpClient;
	private final String serverIpAddress;
	private final int serverPort;
	private final TrustManager[] trustManagers;
	private final AtomicReference<WebSocketClient> webSocketClientAtomicReference;
	/** we can't connect more than once on a new socket */
	private final AtomicBoolean isConnectionStarted = new AtomicBoolean(false);

	DdpWebSocketClient(@NotNull IDdpClient ddpClient, @NotNull String serverIpAddress, @Nullable Integer serverPort, boolean useSSL) throws URISyntaxException {
		assert (ddpClient != null);
		assert (serverIpAddress != null && serverIpAddress.length() > 2);
		this.ddpClient = ddpClient;
		this.serverIpAddress = serverIpAddress;
		if (serverPort == null) {
			serverPort = 3000;
		}
		this.serverPort = serverPort;
		webSocketClientAtomicReference = new AtomicReference<>();
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
		this.trustManagers = trustManagers;
		initWebsocket();
	}

	/**
	 * Initializes a websocket connection
	 * 
	 * @param meteorServerIp IP address of Meteor server
	 * @param meteorServerPort port of Meteor server, if left null it will default to 3000
	 * @param trustManagers array explicitly defined trust managers, can be null
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 */
	private void initWebsocket() throws URISyntaxException {
		synchronized (webSocketClientAtomicReference) {
			String urlScheme = (trustManagers != null ? "wss" : "ws");
			URI serverUri = new URI(urlScheme, null, serverIpAddress, serverPort, "/websocket", null, null);
			createWsClient(serverUri);
			initWsClientSSL();
		}
	}

	/**
	 * initializes WS client's trust managers
	 */
	private void initWsClientSSL() {
		synchronized (webSocketClientAtomicReference) {
			if (trustManagers != null) {
				try {
					SSLContext sslContext = SSLContext.getInstance("TLS");
					sslContext.init(null, trustManagers, null);
					// now we can set the web service client to use this SSL context
					webSocketClientAtomicReference.get().setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sslContext));
				} catch (NoSuchAlgorithmException e) {
					log.warn("Error accessing Java default trustmanager algorithms {}", e);
				} catch (KeyManagementException e) {
					log.warn("Error accessing Java default cacert keys {}", e);
				}
			}
		}
	}

	/**
	 * Creates a web socket client
	 * 
	 * @param meteorServerAddress Websocket address of Meteor server
	 * @throws URISyntaxException URI error
	 */
	private void createWsClient(URI serverUrl) {
		this.webSocketClientAtomicReference.set(new WebSocketClient(serverUrl) {
			@Override
			public void onOpen(ServerHandshake handshakedata) {
				ddpClient.onConnectionOpened();
			}

			@Override
			public void onMessage(String message) {
				ddpClient.onReceived(message);
			}

			@Override
			public void onError(Exception ex) {
				ddpClient.onError(ex);
			}

			@Override
			public void onClose(int code, String reason, boolean remote) {
				ddpClient.onConnectionClosed(code, reason, remote);
			}
		});
		isConnectionStarted.set(false);
	}

	public void connect() {
		synchronized (webSocketClientAtomicReference) {
			WebSocketClient webSocketClient = this.webSocketClientAtomicReference.get();
			if (webSocketClient != null && !isConnectionStarted.getAndSet(true)) {
				webSocketClient.connect();
			}
		}
	}

	@Override
	public void close() {
		synchronized (webSocketClientAtomicReference) {
			WebSocketClient webSocketClient = this.webSocketClientAtomicReference.getAndSet(null);
			if (webSocketClient != null && webSocketClient.getReadyState() == READYSTATE.OPEN) {
				webSocketClient.close();
			}
		}
	}

	public void sendText(String text) {
		WebSocketClient webSocketClient = webSocketClientAtomicReference.get();
		if (webSocketClient != null) {
			webSocketClient.send(text);
		} else {
			throw new WebsocketNotConnectedException();
		}
	}
}
