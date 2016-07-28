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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.jazeee.ddp.auth.EmailAuth;
import com.jazeee.ddp.client.DdpTestClientListener.DdpState;

/**
 * Tests for collections
 * 
 * @author kenyee
 */
public class TestDDPCollections {
	private DdpClient ddp;
	private DdpTestClientListener obs;
	public final static String NEWLINE_SEPARATOR = System.getProperty("line.separator");

	@Before
	public void setUp() throws Exception {
		System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");

		// create DDP client instance and hook testobserver to it
		ddp = new DdpClient(TestConstants.sMeteorHost, TestConstants.sMeteorPort, false);
		obs = new DdpTestClientListener(ddp);
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
		EmailAuth emailpass = new EmailAuth(TestConstants.sMeteorUsername, TestConstants.sMeteorPassword);
		methodArgs[0] = emailpass;
		String methodId = ddp.callMethod("login", methodArgs);
		assertEquals("1", methodId); // first ID should be 1

		// we should get a message back after a bit..make sure it's successful
		// we need to grab the "token" from the result for the next test
		Thread.sleep(500);
		assertTrue(obs.ddpState == DdpState.LOGGED_IN);

		// verify that we have the user in the users collection after login
		assertTrue(obs.collections.get("users").size() == 1);
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Tests that an invalid subscription name is rejected
	 * 
	 * @throws Exception
	 */
	@Ignore
	// Meteor no longer throws a 404 when the subscription is invalid.
	@Test
	public void testInvalidSubscription() throws Exception {
		// test error handling for invalid subscription
		ddp.subscribe("nosuchsubscription", new Object[] {});
		// wait a bit to get an error
		Thread.sleep(500);
		// make sure we see the right error
		assertEquals(404, obs.ddpErrorField.getErrorCodeIfPossible());
		assertEquals("Subscription not found", obs.ddpErrorField.getReason());
	}

	/**
	 * Tests whether unsubscribe works
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUnsubscribe() throws Exception {
		// test error handling for invalid subscription
		String subId = ddp.subscribe("testData", new Object[] {});
		// wait a bit to get confirmation
		Thread.sleep(500);
		// make sure we see subscriptions
		assertTrue(obs.readySubscription != null);
		// test unsubscribe
		ddp.unsubscribe(subId);
		// wait a bit to get confirmation
		Thread.sleep(500);
		// make sure we see unsubscription
		assertTrue(obs.readySubscription == null);
	}

	/**
	 * Test CRUD on a collection
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCollectionCRUD() throws Exception {
		// put collection in clean state
		ddp.callMethod("clearCollection", new Object[] {});
		// subscribe to TestCollection
		ddp.subscribe("testData", new Object[] {});
		// add a few documents to collection
		Object[] methodArgs = new Object[1];
		Map<String, Object> options = new HashMap<String, Object>();
		options.put("value", "a");
		options.put("docnum", 1);
		methodArgs[0] = options;
		ddp.callMethod("addDoc", methodArgs);
		options.put("value", "b");
		options.put("docnum", 2);
		ddp.callMethod("addDoc", methodArgs);
		// wait a bit to get it sync'd back down
		Thread.sleep(1000);
		assertTrue(obs.collections.containsKey("TestCollection"));
		assertEquals(2, obs.collections.get("TestCollection").size());
		// check field values are correct
		Map<String, Object> coll = obs.collections.get("TestCollection");
		for (Entry<String, Object> doc : coll.entrySet()) {
			Map<String, Object> fields = (Map<String, Object>) doc.getValue();
			Map<String, Object> testarray = (Map<String, Object>) fields.get("testarray");
			int docnum = (int) Math.floor((Double) testarray.get("docnum"));
			assertEquals(Character.toString(((char) ('a' + docnum - 1))), fields.get("testfield"));
			assertTrue((docnum == 1) || (docnum == 2));
		}
		// update first document
		Entry<String, Object> docEntry = coll.entrySet().iterator().next();
		String docId = docEntry.getKey();
		options.put("value", "test");
		options.put("id", docId);
		ddp.callMethod("updateDoc", methodArgs);
		Thread.sleep(1000);
		// verify doc was updated
		coll = obs.collections.get("TestCollection");
		Map<String, Object> doc = (Map<String, Object>) coll.get(docId);
		assertEquals("test", doc.get("testfield"));
		// delete a document
		ddp.callMethod("deleteDoc", methodArgs);
		Thread.sleep(1000);
		// verify doc was deleted
		assertEquals(1, obs.collections.get("TestCollection").size());
	}

	/**
	 * Test CRUD on a collection
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testLargeCollectionCRUD() throws Exception {
		// put collection in clean state
		ddp.callMethod("clearCollection", new Object[] {});
		// subscribe to TestCollection
		ddp.subscribe("testData", new Object[] {});
		// add a few documents to collection
		Object[] methodArgs = new Object[1];
		Map<String, Object> options = new HashMap<String, Object>();
		StringBuilder largeStringBuilder = new StringBuilder(5_000_000);
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 100; j++) {
				largeStringBuilder.append(" 123456789");
			}
		}
		String largeString = largeStringBuilder.toString();
		options.put("value", largeString);
		options.put("docnum", 1);
		methodArgs[0] = options;
		ddp.callMethod("addDoc", methodArgs);
		// wait a bit to get it sync'd back down
		Thread.sleep(1000);
		assertTrue(obs.collections.containsKey("TestCollection"));
		assertEquals(1, obs.collections.get("TestCollection").size());
		// check field values are correct
		Map<String, Object> coll = obs.collections.get("TestCollection");
		for (Entry<String, Object> doc : coll.entrySet()) {
			Map<String, Object> fields = (Map<String, Object>) doc.getValue();
			Map<String, Object> testarray = (Map<String, Object>) fields.get("testarray");
			int docnum = (int) Math.floor((Double) testarray.get("docnum"));
			assertEquals(largeString.length(), ((String) fields.get("testfield")).length());
			assertEquals(largeString, fields.get("testfield"));
			assertTrue((docnum == 1) || (docnum == 2));
		}
		// update first document
		Entry<String, Object> docEntry = coll.entrySet().iterator().next();
		String docId = docEntry.getKey();
		options.put("value", "test");
		options.put("id", docId);
		ddp.callMethod("updateDoc", methodArgs);
		Thread.sleep(1000);
		// verify doc was updated
		coll = obs.collections.get("TestCollection");
		Map<String, Object> doc = (Map<String, Object>) coll.get(docId);
		assertEquals("test", doc.get("testfield"));
		// delete a document
		ddp.callMethod("deleteDoc", methodArgs);
		Thread.sleep(1000);
		// verify doc was deleted
		assertEquals(0, obs.collections.get("TestCollection").size());
	}

	/**
	 * Test collection update from client
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testClientSideUpdate() throws Exception {
		// put collection in clean state
		ddp.callMethod("clearCollection", new Object[] {});
		// subscribe to TestCollection
		ddp.subscribe("testData", new Object[] {});
		// add a doc to collection
		Map<String, Object> options = new HashMap<String, Object>();
		options.put("value", "a");
		options.put("docnum", 1);
		options.put("testfield", "test");
		options.put("userid", obs.userId);
		// you need to specify the _id if you're creating doc on client
		options.put("_id", UUID.randomUUID().toString());
		ddp.insertIntoCollection("TestCollection", options);
		// wait a bit to get it sync'd back down
		Thread.sleep(500);
		assertTrue(obs.collections.containsKey("TestCollection"));
		assertEquals(1, obs.collections.get("TestCollection").size());
		// update document
		Map<String, Object> coll = obs.collections.get("TestCollection");
		Entry<String, Object> docEntry = coll.entrySet().iterator().next();
		String docId = docEntry.getKey();
		// do RESTful API call to simulate client-side update
		options.clear();
		Map<String, Object> setOptions = new HashMap<String, Object>();
		setOptions.put("testfield", "hello");
		options.put("$set", setOptions);
		ddp.updateCollection("TestCollection", docId, options);
		// wait a bit to get it sync'd back down
		Thread.sleep(500);
		// verify that collection got updated
		coll = obs.collections.get("TestCollection");
		Map<String, Object> doc = (Map<String, Object>) coll.get(docId);
		assertEquals("hello", doc.get("testfield"));
		// try to do a delete
		ddp.deleteFromCollection("TestCollection", docId);
		// verify that the collection is now empty
		Thread.sleep(500);
		assertEquals(0, obs.collections.get("TestCollection").size());
	}

	/**
	 * Test server side delete field and add field
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testServerSideFieldUpdate() throws Exception {
		org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
		if (log.isDebugEnabled()) {
			log.debug("debug enabled");
		}

		// put collection in clean state
		ddp.callMethod("clearCollection", new Object[] {});
		// subscribe to TestCollection
		ddp.subscribe("testData", new Object[] {});
		// add a doc to collection
		Map<String, Object> options = new HashMap<String, Object>();
		options.put("value", "a");
		options.put("docnum", 1);
		options.put("testfield", "test");
		options.put("userid", obs.userId);
		// you need to specify the _id if you're creating doc on client
		options.put("_id", UUID.randomUUID().toString());
		ddp.insertIntoCollection("TestCollection", options);
		// wait a bit to get it sync'd back down
		Thread.sleep(500);
		assertTrue(obs.collections.containsKey("TestCollection"));
		assertEquals(1, obs.collections.get("TestCollection").size());
		// check that field added on server comes down
		Map<String, Object> coll = obs.collections.get("TestCollection");
		Entry<String, Object> docEntry = coll.entrySet().iterator().next();
		String docId = docEntry.getKey();
		Map<String, Object> doc = (Map<String, Object>) coll.get(docId);
		assertNull(doc.get("newField"));
		options.clear();
		options.put("id", docId);
		options.put("value", "hello");
		ddp.callMethod("addField", new Object[] { options });
		// wait a bit to get it sync'd back down
		Thread.sleep(500);
		assertEquals("hello", doc.get("newfield"));
		// check that field deleted on server deletes it in local doc
		ddp.callMethod("deleteField", new Object[] { options });
		// wait a bit to get delete field sync'd back down
		Thread.sleep(500);
		assertNull(doc.get("newfield"));
	}
}
