/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.cm.helper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ImageCacheProvider {

	private static final Map<String, CacheItem> cache = new HashMap<String, CacheItem>();
	private static final long DEADLINE = 15 * 60 * 1000; // 15min

	private ImageCacheProvider() {
	}

	public static synchronized byte[] get(String key) {
		CacheItem result = cache.get(key);
		return (result != null) ? result.getItem() : null;
	}

	public static synchronized void store(String key, byte[] content) {

		CacheItem item = new CacheItem(content, new Date().getTime() + DEADLINE);
		cache.put(key, item);
	}
}
