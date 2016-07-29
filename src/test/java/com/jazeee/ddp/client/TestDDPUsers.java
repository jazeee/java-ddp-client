package com.jazeee.ddp.client;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.TestCase;

import com.jazeee.ddp.client.DdpTestClientListener.DdpState;

/**
 * Test creation of Meteor users
 */
public class TestDDPUsers extends TestCase {

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
	 * Tests that we can create a user and log in
	 * 
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 * @throws UnableToConnectException
	 */
	public void testCreateUser() throws URISyntaxException, InterruptedException, UnableToConnectException {
		// TODO: does this belong inside the Java DDP client?
		// create DDP client instance and hook testobserver to it
		DdpClient ddp = new DdpClient(TestConstants.METEOR_URI);
		DdpTestClientListener obs = new DdpTestClientListener(ddp);
		ddp.connect();

		// we need to wait a bit before the socket is opened but make sure it's successful
		Thread.sleep(500);
		assertTrue(obs.ddpState == DdpState.CONNECTED);

		// subscribe to user collection
		ddp.subscribe("users", new Object[] {});

		// delete old user first in case this test has been run before
		Object[] methodArgs = new Object[1];
		methodArgs[0] = "test2@test.com";
		ddp.callMethod("deleteUser", methodArgs);

		// we need to wait a bit in case there was a deletion
		Thread.sleep(500);

		// make sure user doesn't exist
		Map<String, Object> userColl = obs.collections.get("users");
		assertNotNull(userColl);
		boolean foundUser;
		for (Entry<String, Object> entry : userColl.entrySet()) {
			@SuppressWarnings("unchecked")
			Map<String, Object> fields = (Map<String, Object>) entry.getValue();
			@SuppressWarnings("unchecked")
			ArrayList<Map<String, Object>> emails = (ArrayList<Map<String, Object>>) fields.get("emails");
			assertFalse(emails.get(0).get("address").equals("test2@test.com"));
		}

		// create new user
		Map<String, Object> options = new HashMap<String, Object>();
		methodArgs[0] = options;
		options.put("username", "test2@test.com");
		options.put("email", "test2@test.com");
		options.put("password", "1234");
		ddp.callMethod("createUser", methodArgs);

		// we need to wait a bit for the insertion or error
		Thread.sleep(500);

		// make sure we have no errors
		assertNull(obs.ddpErrorField);

		// check that users collection has this user
		userColl = obs.collections.get("users");
		assertNotNull(userColl);
		foundUser = false;
		for (Entry<String, Object> entry : userColl.entrySet()) {
			@SuppressWarnings("unchecked")
			Map<String, Object> fields = (Map<String, Object>) entry.getValue();
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> emails = (ArrayList<Map<String, Object>>) fields.get("emails");
			if (emails.get(0).get("address").equals("test2@test.com")) {
				foundUser = true;
				break;
			}
		}
		assertTrue(foundUser);
	}

}
