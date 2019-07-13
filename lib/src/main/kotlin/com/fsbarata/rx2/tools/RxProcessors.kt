package com.fsbarata.rx2.tools

import io.reactivex.Observable

fun <K, V> Observable<Map<K, V>>.deltas() =
		startWith(emptyMap()).buffer(2, 1)
				.map { Pair(it[0], it[1]) }
				.map { (oldMap, newMap) ->
					val oldItems = oldMap.toList()
					newMap.toList().filter { !oldItems.contains(it) }.toMap()
				}

fun <T, U> Observable<List<T>>.reuse(keySelector: (T) -> Any? = { it }, transform: (T) -> U): Observable<List<U>> =
		scan(emptyMap<Any?, U>()) { oldResult, newList ->
			oldResult.filterKeys { newList.map(keySelector).contains(it) } +
					newList.filter { !oldResult.containsKey(keySelector(it)) }
							.map { Pair(keySelector(it), transform(it)) }
							.toMap()
		}.skip(1).map { it.values.toList() }

fun <K, V, T> Observable<Map<K, V>>.reuseValues(transform: (K, V) -> T): Observable<Map<K, T>> =
		map { it.toList() }
				.reuse({ (key, _) -> key }) { (key, value) -> key to transform(key, value) }
				.map { it.toMap() }

fun <K, V, T> Observable<Map<K, V>>.reuseOrUpdate(transform: (K, V) -> T): Observable<Map<K, T>> =
		map { it.toList() }
				.reuse { (key, value) -> key to transform(key, value) }
				.map { it.toMap() }
