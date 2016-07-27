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

import junit.framework.TestCase;

import com.jazeee.ddp.auth.EmailAuth;
import com.jazeee.ddp.auth.TokenAuth;
import com.keysolutions.ddpclient.DDPClient;
import com.keysolutions.ddpclient.test.DDPTestClientObserver.DDPSTATE;

/**
 * Tests for authentication
 * 
 * @author kenyee
 */
public class TestDDPAuth extends TestCase {

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
	 * Verifies that a bad login is rejected
	 * 
	 * @throws Exception
	 */
	public void testBadLogin() throws Exception {
		// create DDP client instance and hook testobserver to it
		DDPClient ddp = new DDPClient(TestConstants.sMeteorHost, TestConstants.sMeteorPort);
		DDPTestClientObserver obs = new DDPTestClientObserver();
		ddp.addDDPListener(obs);
		// make connection to Meteor server
		ddp.connect();

		// we need to wait a bit before the socket is opened but make sure it's successful
		Thread.sleep(500);
		assertTrue(obs.mDdpState == DDPSTATE.Connected);

		// [password: passwordstring,
		// user: {
		// username: usernamestring
		// or
		// email: emailstring
		// or
		// resume: resumetoken (no password required)
		// }]
		Object[] methodArgs = new Object[1];
		EmailAuth emailpass = new EmailAuth("invalid@invalid.com", "password");
		methodArgs[0] = emailpass;
		String methodId = ddp.call("login", methodArgs, obs);
		assertEquals("1", methodId); // first ID should be 1
		Thread.sleep(500);
		assertTrue(obs.mDdpState == DDPSTATE.Connected);
		assertEquals(403, obs.mErrorCode);
		assertEquals("User not found", obs.mErrorReason);
		assertEquals("User not found [403]", obs.mErrorMsg);
		assertEquals("Meteor.Error", obs.mErrorType);
	}

	/**
	 * Verifies that a bad password is rejected
	 * 
	 * @throws Exception
	 */
	public void testBadPassword() throws Exception {
		// create DDP client instance and hook testobserver to it
		DDPClient ddp = new DDPClient(TestConstants.sMeteorHost, TestConstants.sMeteorPort);
		DDPTestClientObserver obs = new DDPTestClientObserver();
		ddp.addDDPListener(obs);
		// make connection to Meteor server
		ddp.connect();

		// we need to wait a bit before the socket is opened but make sure it's successful
		Thread.sleep(500);
		assertTrue(obs.mDdpState == DDPSTATE.Connected);

		// [password: passwordstring,
		// user: {
		// username: usernamestring
		// or
		// email: emailstring
		// or
		// resume: resumetoken (no password required)
		// }]
		Object[] methodArgs = new Object[1];
		EmailAuth emailpass = new EmailAuth("invalid@invalid.com", "password");
		methodArgs[0] = emailpass;
		String methodId = ddp.call("login", methodArgs, obs);
		assertEquals("1", methodId); // first ID should be 1
		Thread.sleep(500);
		assertTrue(obs.mDdpState == DDPSTATE.Connected);
		assertEquals(403, obs.mErrorCode);
		assertEquals("User not found", obs.mErrorReason);
		assertEquals("User not found [403]", obs.mErrorMsg);
		assertEquals("Meteor.Error", obs.mErrorType);
	}

	/**
	 * Verifies that email/password login and resume tokens work
	 * 
	 * @throws Exception
	 */
	public void testLogin() throws Exception {
		// TODO: does this belong inside the Java DDP client?
		// create DDP client instance and hook testobserver to it
		DDPClient ddp = new DDPClient(TestConstants.sMeteorHost, TestConstants.sMeteorPort);
		DDPTestClientObserver obs = new DDPTestClientObserver();
		ddp.addDDPListener(obs);
		// make connection to Meteor server
		ddp.connect();

		// we need to wait a bit before the socket is opened but make sure it's successful
		Thread.sleep(500);
		assertTrue(obs.mDdpState == DDPSTATE.Connected);

		// [password: passwordstring,
		// user: {
		// username: usernamestring
		// or
		// email: emailstring
		// or
		// resume: resumetoken (no password required)
		// }]
		Object[] methodArgs = new Object[1];
		EmailAuth emailpass = new EmailAuth(TestConstants.sMeteorUsername, TestConstants.sMeteorPassword);
		methodArgs[0] = emailpass;
		String methodId = ddp.call("login", methodArgs, obs);
		assertEquals("1", methodId); // first ID should be 1

		// we should get a message back after a bit..make sure it's successful
		// we need to grab the "token" from the result for the next test
		Thread.sleep(500);
		assertTrue(obs.mDdpState == DDPSTATE.LoggedIn);

		// verify that we have the user in the users collection after login
		assertTrue(obs.mCollections.get("users").size() == 1);

		// // test out resume token
		String resumeToken = obs.mResumeToken;
		ddp = new DDPClient(TestConstants.sMeteorHost, TestConstants.sMeteorPort);
		obs = new DDPTestClientObserver();
		ddp.addDDPListener(obs);
		// make connection to Meteor server
		ddp.connect();

		// we need to wait a bit before the socket is opened but make sure it's successful
		Thread.sleep(500);
		assertTrue(obs.mDdpState == DDPSTATE.Connected);

		TokenAuth token = new TokenAuth(resumeToken);
		methodArgs[0] = token;
		methodId = ddp.call("login", methodArgs, obs);
		assertEquals("1", methodId); // first ID should be 1
		Thread.sleep(500);
		assertTrue(obs.mDdpState == DDPSTATE.LoggedIn);

		// verify that we have the user in the users collection after login
		assertTrue(obs.mCollections.get("users").size() == 1);
	}
}
