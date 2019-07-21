package com.fsbarata.rx2.tools

import io.reactivex.Observable

fun <K, V> Observable<Map<K, V>>.deltas() =
		startWith(emptyMap())
				.buffer(2, 1)
				.map { (oldMap, newMap) -> (newMap.toList() - oldMap.toList()).toMap() }

fun <K, V, T> Observable<Map<K, V>>.reuseValues(transform: (K, V) -> T): Observable<Map<K, T>> =
		scan(emptyMap<K, T>()) { oldResult, newMap ->
			val newKeys = newMap.keys
			val oldKeys = oldResult.keys

			val keysToRemove = oldKeys - newKeys
			val retainedMap = oldResult - keysToRemove

			val diffResultMap = (newMap - oldKeys).mapValues { (key, input) -> transform(key, input) }

			retainedMap + diffResultMap
		}.skip(1)

fun <T, U> Observable<List<T>>.reuse(keySelector: (T) -> Any? = { it }, transform: (T) -> U): Observable<List<U>> =
		map { it.associateBy(keySelector) }
				.reuseValues { _, input -> transform(input) }
				.map { it.values.toList() }

fun <K, V, T> Observable<Map<K, V>>.reuseOrUpdate(transform: (K, V) -> T): Observable<Map<K, T>> =
		scan(Pair(emptyMap<K, V>(), emptyMap<K, T>())) { (oldMap, oldResult), newMap ->
			val keysToRemove = oldMap.keys - newMap.keys
			val retainedMap = oldResult - keysToRemove

			val diffToTransform = newMap.toList() - oldMap.toList()

			val diffResultMap = diffToTransform.associate { (key, input) -> key to transform(key, input) }

			Pair(newMap, retainedMap + diffResultMap)
		}
				.skip(1)
				.map { it.second }


private class Optional<T>(val value: T?)
fun <T, R> Observable<T>.scanWith(initialValueFunction: (T) -> R?, accumulator: (R, T) -> R): Observable<R> =
		scan(Optional<R?>(null)) { last, newValue ->
			val lastValue = last.value
			Optional(
					if (lastValue != null) accumulator(lastValue, newValue)
					else initialValueFunction(newValue)
			)
		}.mapNotNull { it.value }

