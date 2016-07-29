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

package com.jazeee.ddp.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jazeee.ddp.client.DdpClient.ConnectionState;
import com.jazeee.ddp.listeners.IDdpConnectionListener;
import com.jazeee.ddp.listeners.IDdpHeartbeatListener;
import com.jazeee.ddp.messages.client.connection.DdpConnectedMessage;
import com.jazeee.ddp.messages.client.connection.IDdpClientConnectionMessage;
import com.jazeee.ddp.messages.client.heartbeat.DdpClientPongMessage;
import com.jazeee.ddp.messages.client.heartbeat.IDdpClientHeartbeatMessage;

public class TestDDPConnections extends TestCase {
	private Logger logger;
	private IDdpConnectionListener ddpConnectionListener;
	private IDdpHeartbeatListener ddpHeartbeatListener;
	private CountDownLatch connectCountDownLatch;
	private CountDownLatch disconnectCountDownLatch;
	private CountDownLatch pingCountDownLatch;
	private IDdpClientConnectionMessage ddpClientConnectionMessage;
	private IDdpClientHeartbeatMessage ddpClientHeartbeatMessage;

	@Override
	protected void setUp() throws Exception {
		System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");
		logger = LoggerFactory.getLogger(TestDDPConnections.class);
		ddpConnectionListener = new IDdpConnectionListener() {
			@Override
			public void processMessage(IDdpClientConnectionMessage ddpClientConnectionMessage) {
				TestDDPConnections.this.ddpClientConnectionMessage = ddpClientConnectionMessage;
				if (ddpClientConnectionMessage instanceof DdpConnectedMessage) {
					if (connectCountDownLatch != null) {
						connectCountDownLatch.countDown();
					}
				}
				if (ddpClientConnectionMessage instanceof DdpDisconnectedMessage) {
					if (disconnectCountDownLatch != null) {
						disconnectCountDownLatch.countDown();
					}
				}
			}
		};
		ddpHeartbeatListener = new IDdpHeartbeatListener() {
			@Override
			public void processMessage(IDdpClientHeartbeatMessage ddpClientHeartbeatMessage) {
				TestDDPConnections.this.ddpClientHeartbeatMessage = ddpClientHeartbeatMessage;
				if (connectCountDownLatch != null) {
					connectCountDownLatch.countDown();
				}
			}
		};

		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Verifies connection closed callback handler works
	 * 
	 * @throws Exception
	 */
	public void testConnectionClosed() throws Exception {
		try (DdpClient ddp = new DdpClient(TestConstants.METEOR_URI)) {
			ddp.addConnectionListener(ddpConnectionListener);
			disconnectCountDownLatch = new CountDownLatch(1);
			ddp.onConnectionClosed(5, "test", true);
			disconnectCountDownLatch.await(10, TimeUnit.MILLISECONDS);
			DdpDisconnectedMessage ddpDisconnectedMessage = (DdpDisconnectedMessage) ddpClientConnectionMessage;
			assertEquals("5", ddpDisconnectedMessage.getCode());
			assertEquals("test", ddpDisconnectedMessage.getReason());
			assertEquals(true, ddpDisconnectedMessage.getRemote().booleanValue());
		}
	}

	/**
	 * Checks that disconnect closes connection properly
	 * 
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 */
	public void testDisconnect() throws UnableToConnectException, InterruptedException, URISyntaxException {
		try (DdpClient ddp = new DdpClient(TestConstants.METEOR_URI)) {
			ddp.addConnectionListener(ddpConnectionListener);
			connectCountDownLatch = new CountDownLatch(1);
			ddp.connect();
			connectCountDownLatch.await(500, TimeUnit.MILLISECONDS);
			assertTrue(ddp.getConnectionState() == ConnectionState.CONNECTED);

			disconnectCountDownLatch = new CountDownLatch(1);
			ddp.disconnect();
			disconnectCountDownLatch.await(500, TimeUnit.MILLISECONDS);
			assertTrue(ddp.getConnectionState() == ConnectionState.CLOSED);
		}
	}

	/**
	 * Tests that we can handle reconnections to the server
	 * 
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 * @throws UnableToConnectException
	 */
	public void testReconnect() throws URISyntaxException, InterruptedException, UnableToConnectException {
		try (DdpClient ddp = new DdpClient(TestConstants.METEOR_URI)) {
			ddp.addConnectionListener(ddpConnectionListener);
			connectCountDownLatch = new CountDownLatch(1);
			ddp.connect();
			connectCountDownLatch.await(500, TimeUnit.MILLISECONDS);
			assertTrue(ddp.getConnectionState() == ConnectionState.CONNECTED);

			disconnectCountDownLatch = new CountDownLatch(1);
			ddp.disconnect();
			disconnectCountDownLatch.await(500, TimeUnit.MILLISECONDS);
			assertTrue(ddp.getConnectionState() == ConnectionState.CLOSED);

			// now test that we can reconnect to the server
			connectCountDownLatch = new CountDownLatch(1);
			ddp.connect();
			connectCountDownLatch.await(500, TimeUnit.MILLISECONDS);
			assertTrue(ddp.getConnectionState() == ConnectionState.CONNECTED);

			disconnectCountDownLatch = new CountDownLatch(1);
			ddp.disconnect();
			disconnectCountDownLatch.await(500, TimeUnit.MILLISECONDS);
			assertTrue(ddp.getConnectionState() == ConnectionState.CLOSED);
		}
	}

	/**
	 * Tests that the server ping command is handled properly
	 * 
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 * @throws UnableToConnectException
	 */
	public void testPing() throws URISyntaxException, InterruptedException, UnableToConnectException {
		try (DdpClient ddp = new DdpClient(TestConstants.METEOR_URI)) {
			ddp.addConnectionListener(ddpConnectionListener);
			ddp.addHeartbeatListener(ddpHeartbeatListener);
			connectCountDownLatch = new CountDownLatch(1);
			ddp.connect();
			connectCountDownLatch.await(500, TimeUnit.MILLISECONDS);
			assertTrue(ddp.getConnectionState() == ConnectionState.CONNECTED);

			pingCountDownLatch = new CountDownLatch(1);
			ddp.ping("ping1");
			pingCountDownLatch.await(500, TimeUnit.MILLISECONDS);
			DdpClientPongMessage ddpClientPongMessage = (DdpClientPongMessage) ddpClientHeartbeatMessage;
			assertNotNull(ddpClientPongMessage);
			assertTrue(ddpClientPongMessage.getId().equals("ping1"));

			disconnectCountDownLatch = new CountDownLatch(1);
			ddp.disconnect();
			disconnectCountDownLatch.await(500, TimeUnit.MILLISECONDS);
			assertTrue(ddp.getConnectionState() == ConnectionState.CLOSED);
		}
	}

	/**
	 * Checks that we can connect to the server using SSL
	 * 
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 * @throws UnableToConnectException
	 */
	public void testUseSSL() throws URISyntaxException, InterruptedException, UnableToConnectException {
		URI meteorUri = TestConstants.METEOR_URI;
		// NOTE: this test will only pass if we're connecting to the server using SSL:
		if (!TestConstants.IS_SSL) {
			meteorUri = new URIBuilder("https://atmospherejs.com/").build();
			logger.debug("Testing SSL using URI: {}", meteorUri);
		}

		try (DdpClient ddp = new DdpClient(meteorUri)) {
			connectCountDownLatch = new CountDownLatch(1);
			ddp.connect();
			connectCountDownLatch.await(500, TimeUnit.MILLISECONDS);
			assertTrue(ddp.getConnectionState() == ConnectionState.CONNECTED);
		}
	}
}
