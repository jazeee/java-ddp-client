package com.jazeee.ddp.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

import com.jazeee.common.utils.nullability.NotNull;
import com.jazeee.common.utils.nullability.Nullable;

public class DdpWebSocketClient implements Closeable {
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
	private final IDdpClient ddpClient;
	private final URI serverUri;
	/** we can't connect more than once on a new socket */
	private final AtomicBoolean isConnectionStarted = new AtomicBoolean(false);
	private final WebSocketEndpoint webSocketEndpoint;
	private final ClientManager clientManager;

	DdpWebSocketClient(@NotNull IDdpClient ddpClient, @NotNull String serverIpAddress, @Nullable Integer serverPort, boolean useSSL) throws URISyntaxException {
		assert (ddpClient != null);
		assert (serverIpAddress != null && serverIpAddress.length() > 2);
		this.ddpClient = ddpClient;
		if (serverPort == null) {
			serverPort = 3000;
		}
		String urlScheme = (useSSL ? "wss" : "ws");
		serverUri = new URI(urlScheme, null, serverIpAddress, serverPort, "/websocket", null, null);
		// See https://tyrus.java.net/documentation/1.13/index/getting-started.html
		this.clientManager = ClientManager.createClient();
		webSocketEndpoint = new WebSocketEndpoint();
		isConnectionStarted.set(false);
	}

	private class WebSocketEndpoint extends Endpoint {
		private Session session;

		@Override
		public void onOpen(Session session, EndpointConfig endpointConfig) {
			try {
				this.session = session;
				session.addMessageHandler(new MessageHandler.Whole<String>() {
					@Override
					public void onMessage(String message) {
						ddpClient.onReceived(message);
					}
				});
			} finally {
				log.debug("Client connected: {}", serverUri);
			}
			ddpClient.onConnectionOpened();
		}

		@Override
		public void onClose(Session session, CloseReason closeReason) {
			close();
		}

		@Override
		public void onError(Session session, Throwable throwable) {
			log.error("Error detected: {}", throwable.getMessage());
			ddpClient.onError(throwable);
			close();
		}

		private void send(String message) throws IOException {
			if (this.session != null) {
				synchronized (session) {
					if (session.isOpen()) {
						this.session.getAsyncRemote().sendText(message);
					}
				}
			}
		}
	}

	public void connect() throws DeploymentException, IOException {
		if (!isConnectionStarted.getAndSet(true)) {
			synchronized (clientManager) {
				final ClientEndpointConfig clientEndpointConfig = ClientEndpointConfig.Builder.create().build();
				this.clientManager.connectToServer(webSocketEndpoint, clientEndpointConfig, serverUri);
			}
		}
	}

	@Override
	public void close() {
		if (isConnectionStarted.getAndSet(false)) {
			synchronized (clientManager) {
				clientManager.shutdown();
			}
		}
		ddpClient.onConnectionClosed(0, "Connection Closed", true);
	}

	public void sendText(String text) throws IOException {
		if (isConnectionStarted.get()) {
			webSocketEndpoint.send(text);
		} else {
			throw new IOException("Cannot send to closed connection");
		}
	}
}
