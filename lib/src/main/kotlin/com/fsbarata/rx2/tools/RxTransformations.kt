package com.fsbarata.rx2.tools

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

fun <T> delayTransformation(delay: Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation()) = { obj: T ->
	Single.timer(delay, timeUnit, scheduler).map { obj }
}

fun <T> delayTransformation(delayProvider: (T) -> Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation()) = { obj: T ->
	Single.timer(delayProvider(obj), timeUnit, scheduler).map { obj }
}

fun <T> variableDelayTransformation(initialDelayMs: Long, scheduler: Scheduler = Schedulers.computation(), delayUpdater: (Long) -> Long) =
		AtomicLong(initialDelayMs).let { counter ->
			{ obj: T ->
				val count = counter.get()
				val newCount = delayUpdater(count)
				counter.set(newCount)
				Single.timer(newCount, TimeUnit.MILLISECONDS, scheduler).map { obj }
			}
		}

fun <T> combine(vararg transformations: (T) -> Single<T>): (T) -> Single<T> = { obj ->
	transformations.fold(Single.just(obj)) { single, transformation -> single.flatMap(transformation) }
}
