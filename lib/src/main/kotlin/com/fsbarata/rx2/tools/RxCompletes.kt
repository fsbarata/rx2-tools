package com.fsbarata.rx2.tools

import io.reactivex.*
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

fun <T> Observable<T>.repeatWith(transformation: (Any) -> Single<Any>) = repeatWhen { it.flatMapSingle(transformation) }
fun <T> Single<T>.repeatWith(transformation: (Any) -> Single<Any>) = repeatWhen { errors -> errors.flatMapSingle(transformation) }
fun <T> Maybe<T>.repeatWith(transformation: (Any) -> Single<Any>) = repeatWhen { errors -> errors.flatMapSingle(transformation) }
fun Completable.repeatWith(transformation: (Any) -> Single<Any>) = repeatWhen { errors -> errors.flatMapSingle(transformation) }

fun <T> Observable<T>.repeatDelayed(delay: Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation()) =
		repeatWith(delayTransformation(delay, timeUnit, scheduler))

fun <T> Single<T>.repeatDelayed(delay: Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation()) =
		repeatWith(delayTransformation(delay, timeUnit, scheduler))

fun <T> Maybe<T>.repeatDelayed(delay: Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation()) =
		repeatWith(delayTransformation(delay, timeUnit, scheduler))

fun Completable.repeatDelayed(delay: Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation()) =
		repeatWith(delayTransformation(delay, timeUnit, scheduler))

