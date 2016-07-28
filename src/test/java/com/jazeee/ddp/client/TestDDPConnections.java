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

import java.net.URISyntaxException;

import junit.framework.TestCase;

import com.jazeee.ddp.client.DdpTestClientListener.DdpState;

public class TestDDPConnections extends TestCase {

	@Override
	protected void setUp() throws Exception {
		System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");

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
		DdpClient ddp = new DdpClient(TestConstants.sMeteorHost, TestConstants.sMeteorPort, false);
		DdpTestClientListener obs = new DdpTestClientListener(ddp);
		ddp.connectionClosed(5, "test", true);
		assertEquals(5, obs.closeCode);
		assertEquals("test", obs.closeReason);
		assertEquals(true, obs.isClosedFromRemote);
	}

	/**
	 * Checks that disconnect closes connection properly
	 * 
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 */
	public void testDisconnect() throws URISyntaxException, InterruptedException {
		// create DDP client instance and hook testobserver to it
		DdpClient ddp = new DdpClient(TestConstants.sMeteorHost, TestConstants.sMeteorPort, false);
		DdpTestClientListener obs = new DdpTestClientListener(ddp);
		// make connection to Meteor server
		ddp.connect();

		// we need to wait a bit before the socket is opened but make sure it's successful
		Thread.sleep(500);
		assertTrue(obs.ddpState == DdpState.CONNECTED);

		// try disconnect
		ddp.disconnect();

		// wait a bit to make sure our state has changed to closed
		Thread.sleep(500);
		assertTrue(obs.ddpState == DdpState.CLOSED);
	}

	/**
	 * Tests that we can handle reconnections to the server
	 * 
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 */
	public void testReconnect() throws URISyntaxException, InterruptedException {
		// create DDP client instance and hook testobserver to it
		DdpClient ddp = new DdpClient(TestConstants.sMeteorHost, TestConstants.sMeteorPort, false);
		DdpTestClientListener obs = new DdpTestClientListener(ddp);
		// make connection to Meteor server
		ddp.connect();

		// we need to wait a bit before the socket is opened but make sure it's successful
		Thread.sleep(500);
		assertTrue(obs.ddpState == DdpState.CONNECTED);

		// try disconnect
		ddp.disconnect();

		// wait a bit to make sure our state has changed to closed
		Thread.sleep(500);
		assertTrue(obs.ddpState == DdpState.CLOSED);

		// now test that we can reconnect to the server
		ddp.connect();

		// we need to wait a bit before the socket is opened but make sure it's successful
		Thread.sleep(500);
		assertTrue(obs.ddpState == DdpState.CONNECTED);

		// try disconnect
		ddp.disconnect();

		// wait a bit to make sure our state has changed to closed
		Thread.sleep(500);
		assertTrue(obs.ddpState == DdpState.CLOSED);
	}

	/**
	 * Tests that the server ping command is handled properly
	 * 
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 */
	public void testPing() throws URISyntaxException, InterruptedException {
		// create DDP client instance and hook testobserver to it
		DdpClient ddp = new DdpClient(TestConstants.sMeteorHost, TestConstants.sMeteorPort, false);
		DdpTestClientListener obs = new DdpTestClientListener(ddp);
		// make connection to Meteor server
		ddp.connect();

		// we need to wait a bit before the socket is opened but make sure it's successful
		Thread.sleep(500);
		assertTrue(obs.ddpState == DdpState.CONNECTED);

		// send a ping and verify we got a pong back
		assertTrue(obs.pingId == null);
		ddp.ping("ping1");
		Thread.sleep(500);
		assertNotNull(obs.pingId);
		assertTrue(obs.pingId.equals("ping1"));

		// try disconnect
		ddp.disconnect();

		// wait a bit to make sure our state has changed to closed
		Thread.sleep(500);
		assertTrue(obs.ddpState == DdpState.CLOSED);
	}

	/**
	 * Checks that we can connect to the server using SSL
	 * 
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 */
	public void testUseSSL() throws URISyntaxException, InterruptedException {
		// NOTE: this test will only pass if we're connecting to the server using SSL:
		if (TestConstants.sMeteorPort != 443) {
			return;
		}

		// create DDP client instance and hook testobserver to it
		DdpClient ddp = new DdpClient(TestConstants.sMeteorHost, TestConstants.sMeteorPort, true);
		DdpTestClientListener obs = new DdpTestClientListener(ddp);
		// make connection to Meteor server
		ddp.connect();

		// we need to wait a bit before the socket is opened but make sure it's successful
		Thread.sleep(500);
		assertTrue(obs.ddpState == DdpState.CONNECTED);

		// try disconnect
		ddp.disconnect();
	}
}