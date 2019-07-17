package com.fsbarata.rx2.tools

import io.reactivex.Observable
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class RxProcessorsKtTest {
	@Test
	fun test_deltas() {
		Observable.just(
				mapOf(3.0 to 5.0, 4.0 to 2.0),
				mapOf(3.0 to 5.0, 6.0 to 2.0),
				mapOf(3.0 to 5.0, 4.0 to 2.0),
				mapOf(3.0 to 5.0, 4.0 to 2.0),
				mapOf(3.0 to 3.0, 4.0 to 3.0),
				mapOf(4.0 to 3.0, 5.0 to 3.0)
		)
				.deltas()
				.test()
				.apply {
					awaitTerminalEvent()
					assertValueCount(6)
					assertValues(
							mapOf(3.0 to 5.0, 4.0 to 2.0),
							mapOf(6.0 to 2.0),
							mapOf(4.0 to 2.0),
							emptyMap(),
							mapOf(3.0 to 3.0, 4.0 to 3.0),
							mapOf(5.0 to 3.0)
					)
				}
	}

	@Test
	fun test_reuse() {
		val count = AtomicInteger(0)
		Observable.just(
				listOf(3.0, 4.0),
				listOf(3.0, 6.0),
				listOf(3.0, 4.0),
				listOf(3.0, 4.0),
				listOf(4.0, 5.0)
		)
				.reuse { count.incrementAndGet() }
				.test()
				.apply {
					awaitTerminalEvent()
					assertValueCount(5)
					assertValues(
							listOf(1, 2),
							listOf(1, 3),
							listOf(1, 4),
							listOf(1, 4),
							listOf(4, 5)
					)
				}
	}

	@Test
	fun test_reuseSelector() {
		val count = AtomicInteger(0)
		Observable.just(
				listOf(6, 4),
				listOf(3, 6),
				listOf(6, 4),
				listOf(3, 4),
				listOf(4, 5)
		)
				.reuse({ it % 3 }) { count.incrementAndGet() }
				.test()
				.apply {
					awaitTerminalEvent()
					assertValueCount(5)
					assertValues(
							listOf(1, 2),
							listOf(1),
							listOf(1, 3),
							listOf(1, 3),
							listOf(3, 4)
					)
				}
	}

	@Test
	fun test_reuseValues() {
		val count = AtomicInteger(0)
		Observable.just(
				mapOf(3.0 to 5.0, 4.0 to 2.0),
				mapOf(3.0 to 5.0, 6.0 to 2.0),
				mapOf(3.0 to 5.0, 4.0 to 2.0),
				mapOf(3.0 to 5.0, 4.0 to 2.0),
				mapOf(3.0 to 3.0, 4.0 to 3.0),
				mapOf(4.0 to 3.0, 5.0 to 3.0)
		)
				.reuseValues { _, _ -> count.addAndGet(1) }
				.test()
				.apply {
					awaitTerminalEvent()
					assertValueCount(6)
					assertValues(
							mapOf(3.0 to 1, 4.0 to 2),
							mapOf(3.0 to 1, 6.0 to 3),
							mapOf(3.0 to 1, 4.0 to 4),
							mapOf(3.0 to 1, 4.0 to 4),
							mapOf(3.0 to 1, 4.0 to 4),
							mapOf(4.0 to 4, 5.0 to 5)
					)
				}
	}

	@Test
	fun test_reuseOrUpdate() {
		val count = AtomicInteger(0)
		Observable.just(
				mapOf(3.0 to 5.0, 4.0 to 2.0),
				mapOf(3.0 to 5.0, 6.0 to 2.0),
				mapOf(3.0 to 5.0, 4.0 to 2.0),
				mapOf(3.0 to 5.0, 4.0 to 2.0),
				mapOf(3.0 to 3.0, 4.0 to 3.0),
				mapOf(4.0 to 3.0, 5.0 to 3.0)
		)
				.reuseOrUpdate { _, _ -> count.incrementAndGet() }
				.test()
				.apply {
					awaitTerminalEvent()
					assertValueCount(6)
					assertValues(
							mapOf(3.0 to 1, 4.0 to 2),
							mapOf(3.0 to 1, 6.0 to 3),
							mapOf(3.0 to 1, 4.0 to 4),
							mapOf(3.0 to 1, 4.0 to 4),
							mapOf(3.0 to 5, 4.0 to 6),
							mapOf(4.0 to 6, 5.0 to 7)
					)
				}
	}

	@Test
	fun test_scanWith() {
		Observable.just(
				1.0,
				5.1,
				2.9
		)
				.scanWith(initialValueFunction = { it.toInt() + 3 }) { acc, newValue -> newValue.toInt() * 2 - acc }
				.test()
				.apply {
					awaitTerminalEvent()
					assertValueCount(3)
					assertValues(
							4,
							6,
							-2
					)
				}
	}

}