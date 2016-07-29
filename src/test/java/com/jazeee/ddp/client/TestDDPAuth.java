package com.jazeee.ddp.client;

import junit.framework.TestCase;

import com.jazeee.ddp.auth.EmailAuth;
import com.jazeee.ddp.auth.TokenAuth;
import com.jazeee.ddp.client.DdpTestClientListener.DdpState;

/**
 * Tests for authentication
 * 
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
		DdpClient ddp = new DdpClient(TestConstants.METEOR_URL);
		DdpTestClientListener obs = new DdpTestClientListener(ddp);
		ddp.connect();

		// we need to wait a bit before the socket is opened but make sure it's successful
		Thread.sleep(500);
		assertTrue(obs.ddpState == DdpState.CONNECTED);

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
		String methodId = ddp.callMethod("login", methodArgs);
		assertEquals("1", methodId); // first ID should be 1
		Thread.sleep(500);
		assertTrue(obs.ddpState == DdpState.CONNECTED);
		assertEquals(403, obs.ddpErrorField.getErrorCodeIfPossible());
		assertEquals("User not found", obs.ddpErrorField.getReason());
		assertEquals("User not found [403]", obs.ddpErrorField.getMessage());
		assertEquals("Meteor.Error", obs.ddpErrorField.getErrorType());
	}

	/**
	 * Verifies that a bad password is rejected
	 * 
	 * @throws Exception
	 */
	public void testBadPassword() throws Exception {
		// create DDP client instance and hook testobserver to it
		DdpClient ddp = new DdpClient(TestConstants.METEOR_URL);
		DdpTestClientListener obs = new DdpTestClientListener(ddp);
		// make connection to Meteor server
		ddp.connect();

		// we need to wait a bit before the socket is opened but make sure it's successful
		Thread.sleep(500);
		assertTrue(obs.ddpState == DdpState.CONNECTED);

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
		String methodId = ddp.callMethod("login", methodArgs);
		assertEquals("1", methodId); // first ID should be 1
		Thread.sleep(500);
		assertTrue(obs.ddpState == DdpState.CONNECTED);
		assertEquals(403, obs.ddpErrorField.getErrorCodeIfPossible());
		assertEquals("User not found", obs.ddpErrorField.getReason());
		assertEquals("User not found [403]", obs.ddpErrorField.getMessage());
		assertEquals("Meteor.Error", obs.ddpErrorField.getErrorType());
	}

	/**
	 * Verifies that email/password login and resume tokens work
	 * 
	 * @throws Exception
	 */
	public void testLogin() throws Exception {
		// TODO: does this belong inside the Java DDP client?
		// create DDP client instance and hook testobserver to it
		DdpClient ddp = new DdpClient(TestConstants.METEOR_URL);
		DdpTestClientListener obs = new DdpTestClientListener(ddp);
		// make connection to Meteor server
		ddp.connect();

		// we need to wait a bit before the socket is opened but make sure it's successful
		Thread.sleep(500);
		assertTrue(obs.ddpState == DdpState.CONNECTED);

		// [password: passwordstring,
		// user: {
		// username: usernamestring
		// or
		// email: emailstring
		// or
		// resume: resumetoken (no password required)
		// }]
		Object[] methodArgs = new Object[1];
		EmailAuth emailpass = new EmailAuth(TestConstants.METEOR_USERNAME, TestConstants.METEOR_PASSWORD);
		methodArgs[0] = emailpass;
		String methodId = ddp.callMethod("login", methodArgs);
		assertEquals("1", methodId); // first ID should be 1

		// we should get a message back after a bit..make sure it's successful
		// we need to grab the "token" from the result for the next test
		Thread.sleep(500);
		assertTrue(obs.ddpState == DdpState.LOGGED_IN);

		// verify that we have the user in the users collection after login
		assertTrue(obs.collections.get("users").size() == 1);

		// // test out resume token
		String resumeToken = obs.resumeToken;
		ddp = new DdpClient(TestConstants.METEOR_URL);
		obs = new DdpTestClientListener(ddp);
		// make connection to Meteor server
		ddp.connect();

		// we need to wait a bit before the socket is opened but make sure it's successful
		Thread.sleep(500);
		assertTrue(obs.ddpState == DdpState.CONNECTED);

		TokenAuth token = new TokenAuth(resumeToken);
		methodArgs[0] = token;
		methodId = ddp.callMethod("login", methodArgs);
		assertEquals("1", methodId); // first ID should be 1
		Thread.sleep(500);
		assertTrue(obs.ddpState == DdpState.LOGGED_IN);

		// verify that we have the user in the users collection after login
		assertTrue(obs.collections.get("users").size() == 1);
	}
}
