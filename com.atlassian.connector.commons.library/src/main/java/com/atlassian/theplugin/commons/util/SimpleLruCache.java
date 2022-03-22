/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.theplugin.commons.util;


import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An LRU cache, based on <code>LinkedHashMap</code>.<br>
 * This cache has a fixed maximum number of elements (<code>cacheSize</code>).
 * If the cache is full and another entry is added, the LRU (least recently used) entry is dropped.
 * <p/>
 * This class is thread-safe.
 * <p>
 * <h3>Synchronization Policy</h3>
 * This cache is synchronized on <code>this</<code>, so while extending this class, ensure that all actions
 * use the same synchronization policy.
 * </p>
 * <p>
 * Inspired by http://blogs.sun.com/swinger/entry/collections_trick_i_lru_cache and href="http://www.source-code.biz
 * </p>
 */
public class SimpleLruCache<K, V> {

	private static final float LOAD_FACTOR = 0.75f;
	private final int cacheSize;
	protected final LinkedHashMap<K, V> map;

	/**
	 * Creates a new LRU cache.
	 *
	 * @param cacheSize the maximum number of entries that will be kept in this cache.
	 */
	public SimpleLruCache(int cacheSize) {
		this.cacheSize = cacheSize;
		final int capacity = (int) Math.ceil(cacheSize / LOAD_FACTOR) + 1;
		map = new LinkedHashMap<K, V>(capacity, LOAD_FACTOR, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
				return size() > SimpleLruCache.this.cacheSize;
			}
		};
	}

	/**
	 * Retrieves an entry from the cache.<br>
	 * The retrieved entry becomes the MRU (most recently used) entry.
	 *
	 * @param key the key whose associated value is to be returned.
	 * @return the value associated to this key, or null if no value with this key exists in the cache.
	 */
	public synchronized V get(K key) {
		return map.get(key);
	}

	public synchronized int size() {
		return map.size();
	}

	/**
	 * Adds an entry to this cache.
	 * If the cache is full, the LRU (least recently used) entry is dropped.
	 *
	 * @param key   the key with which the specified value is to be associated.
	 * @param value a value to be associated with the specified key.
	 * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
	 */
	public synchronized V put(K key, V value) {
		return map.put(key, value);
	}

	/**
	 * Returns the number of used entries in the cache.
	 *
	 * @return the number of entries currently in the cache.
	 */
	public synchronized int getUsedEntriesCount() {
		return map.size();
	}

//	public synchronized V putIfAbsent(final K key, final V value) {
//		if (!map.containsKey(key)) {
//			return map.put(key, value);
//		} else {
//			return map.get(key);
//		}
//	}

    public LinkedHashMap<K, V> getMap() {
        return map;
    }
}
