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

import java.lang.reflect.Method;

import junit.framework.TestCase;

import com.keysolutions.ddpclient.DdpClient;

/**
 * Test misc basic methods in DDP client
 * 
 * @author kenyee
 */
public class TestDDPBasic extends TestCase {

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
	 * Verifies that errors are handled properly
	 * 
	 * @throws Exception
	 */
	public void testHandleError() throws Exception {
		DdpClient ddp = new DdpClient("", 0);
		DdpTestClientListener obs = new DdpTestClientListener(ddp);
		// do this convoluted thing to test a private method
		Method method = DdpClient.class.getDeclaredMethod("handleError", Exception.class);
		method.setAccessible(true);
		method.invoke(ddp, new Exception("ignore exception"));
		assertEquals("JavaWebSocketClient", obs.ddpTopLevelErrorMessage.getJavaSource());
		assertEquals("ignore exception", obs.ddpTopLevelErrorMessage.getReason());
	}
}
