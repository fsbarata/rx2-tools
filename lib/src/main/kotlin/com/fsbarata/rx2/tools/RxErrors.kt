package com.fsbarata.rx2.tools

import io.reactivex.*
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

fun <T> Observable<T>.retryWith(transformation: (Throwable) -> Single<Throwable>) = retryWhen { errors -> errors.flatMapSingle(transformation) }
fun <T> Single<T>.retryWith(transformation: (Throwable) -> Single<Throwable>) = retryWhen { errors -> errors.flatMapSingle(transformation) }
fun <T> Maybe<T>.retryWith(transformation: (Throwable) -> Single<Throwable>) = retryWhen { errors -> errors.flatMapSingle(transformation) }
fun Completable.retryWith(transformation: (Throwable) -> Single<Throwable>) = retryWhen { errors -> errors.flatMapSingle(transformation) }

fun <T> Observable<T>.retryDelayed(delay: Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation()) =
		retryWith(delayTransformation(delay, timeUnit, scheduler))

fun Completable.retryDelayed(delay: Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation()) =
		retryWith(delayTransformation(delay, timeUnit, scheduler))

fun <T> Single<T>.retryDelayed(delay: Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation()) =
		retryWith(delayTransformation(delay, timeUnit, scheduler))

fun <T> Maybe<T>.retryDelayed(delay: Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation()) =
		retryWith(delayTransformation(delay, timeUnit, scheduler))

fun predicateErrorTransformation(predicate: (Throwable) -> Boolean) = { throwable: Throwable ->
	if (predicate(throwable)) Single.error(throwable)
	else Single.just(throwable)
}

fun <T> Observable<T>.retryDelayedUntil(delay: Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation(), throwablePredicate: ((Throwable) -> Boolean)) =
		retryWith(combine(delayTransformation(delay, timeUnit, scheduler), predicateErrorTransformation(throwablePredicate)))

fun countErrorTransformation(limit: Int, start: Int = 0, updater: (Int) -> Int = { it + 1 }) =
		AtomicInteger(start).let { counter ->
			{ throwable: Throwable ->
				val count = counter.get()
				val newCount = updater(count)
				counter.set(newCount)
				if (count > limit) Single.error(throwable)
				else Single.just(throwable)
			}
		}


fun timeoutErrorTransformation(timeout: Long, timeUnit: TimeUnit, timeMsSupplier: () -> Long = { System.currentTimeMillis() }) =
		@Suppress("ReplaceSingleLineLet")
		timeMsSupplier().let { timeBefore -> predicateErrorTransformation { _ -> timeMsSupplier() - timeBefore > timeUnit.toMillis(timeout) } }


fun <T> Observable<T>.onErrorReturnAndThrow(value: T) =
		onErrorResumeNext { error: Throwable -> Observable.just(value).concatWith(Observable.error(error)) }

