package org.ggp.base.util.statemachine.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/** REFACTORED VERSION
 * This is a generic implementation of a Time-To-Live cache
 * that maps keys of type K to values of type V. It's backed
 * by a hashmap, and whenever a pair (K,V) is accessed, their
 * TTL is reset to the starting TTL (which is the parameter
 * passed to the constructor). On the other hand, when the
 * method prune() is called, the TTL of all of the pairs in the
 * map is decremented, and pairs whose TTL has reached zero are
 * removed.
 *
 * While this class implements the Map interface, keep in mind
 * that it only decrements the TTL of an entry when that entry
 * is accessed directly.
 *
 * @param <K> Key type
 * @param <V> Value type
 */
public final class RefactoredTtlCache<K, V>{

	private final class CacheEntry{
		private int ttl;
		private V value;

		public CacheEntry(V value, int ttl)		{
			this.value = value;
			this.ttl = ttl;
		}

		@Override
		public String toString(){
			return this.ttl + ", " + (this.value != null ? this.value : "null");
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean equals(Object o) {
		    if (o instanceof RefactoredTtlCache.CacheEntry) {
		        return ((CacheEntry)o).value.equals(value);
		    }
		    return false;
		}
	}

	private final Map<K, CacheEntry> contents;
	private final int ttl;

	public RefactoredTtlCache(int ttl){
		this.contents = new HashMap<K, CacheEntry>();
		this.ttl = ttl;
	}

	public synchronized boolean containsKey(K key){
		return contents.containsKey(key);
	}

	public synchronized V get(K key){
		CacheEntry entry = contents.get(key);
		if (entry == null)
		    return null;

		// Reset the TTL when a value is accessed directly.
		entry.ttl = ttl;
		return entry.value;
	}

	public synchronized void prune(){
		Iterator<Entry<K, CacheEntry>> iterator = contents.entrySet().iterator();

		while(iterator.hasNext()){
			Entry<K, CacheEntry> entry = iterator.next();
			if(entry.getValue().ttl == 0){
				iterator.remove();
			}else{
				entry.getValue().ttl--;
			}
		}

		/** OLD METHOD - START
		List<K> toPrune = new ArrayList<K>();
		for (K key : contents.keySet())
		{
			Entry entry = contents.get(key);
			if (entry.ttl == 0)
			{
				toPrune.add(key);
			}
			entry.ttl--;
		}

		for (K key : toPrune)
		{
			contents.remove(key);
		}
		OLD METHOD - END **/
	}

	public synchronized V put(K key, V value){
		CacheEntry x = contents.put(key, new CacheEntry(value, ttl));
		if(x == null) return null;
		return x.value;
	}

	public synchronized int size(){
		return contents.size();
	}

	public synchronized void clear(){
        contents.clear();
    }

	public synchronized boolean containsValue(V value){

        return contents.containsValue(new CacheEntry(value,0));
    }

	public synchronized boolean isEmpty(){
        return contents.isEmpty();
    }

	public synchronized Set<K> keySet(){
        return contents.keySet();
    }

	public synchronized void putAll(Map<? extends K, ? extends V> m){
         for(Map.Entry<? extends K, ? extends V> anEntry : m.entrySet()) {
             this.put(anEntry.getKey(), anEntry.getValue());
         }
    }

	public synchronized V remove(K key){
        return contents.remove(key).value;
    }

	public synchronized Collection<V> values(){
        Collection<V> theValues = new HashSet<V>();
        for (CacheEntry e : contents.values())
            theValues.add(e.value);
        return theValues;
    }

	@Override
	public String toString(){

		String cacheString = "[ ";

		for(Entry<K,CacheEntry> entry : this.contents.entrySet()){

			cacheString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";

		}

		cacheString += "]";

		return cacheString;
	}

  /*  private class entrySetMapEntry implements Map.Entry<K,V> {
        private K key;
        private V value;

        entrySetMapEntry(K k, V v) {
            key = k;
            value = v;
        }

        @Override
		public K getKey() { return key; }
        @Override
		public V getValue() { return value; }
        @Override
		public V setValue(V value) { return (this.value = value); }
    }

	public synchronized Set<java.util.Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K,V>> theEntries = new HashSet<Map.Entry<K, V>>();
        for (Map.Entry<K, CacheEntry> e : contents.entrySet())
            theEntries.add(new entrySetMapEntry(e.getKey(), e.getValue().value));
        return theEntries;
    } */

}
