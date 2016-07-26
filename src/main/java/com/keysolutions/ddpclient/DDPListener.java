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

package com.keysolutions.ddpclient;

import java.util.Map;

import com.jazeee.ddp.IDDPListener;

/**
 * Listener for method errors/results/updates
 * 
 * @author kenyee
 */
public abstract class DDPListener implements IDDPListener {
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.keysolutions.ddpclient.IDDPListener#onResult(java.util.Map)
	 */
	@Override
	public void onResult(Map<String, Object> resultFields) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.keysolutions.ddpclient.IDDPListener#onUpdated(java.lang.String)
	 */
	@Override
	public void onUpdated(String callId) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.keysolutions.ddpclient.IDDPListener#onReady(java.lang.String)
	 */
	@Override
	public void onSubscriptionReady(String callId) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.keysolutions.ddpclient.IDDPListener#onNoSub(java.lang.String, java.util.Map)
	 */
	@Override
	public void onNoSub(String callId, Map<String, Object> errorFields) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.keysolutions.ddpclient.IDDPListener#onPing(java.lang.String)
	 */
	@Override
	public void onPing(String pongId) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.keysolutions.ddpclient.IDDPListener#onPong(java.lang.String)
	 */
	@Override
	public void onPong(String pingId) {
	}
}
