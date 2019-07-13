package com.fsbarata.rx2.tools

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

fun <T> Maybe<T>.delayValues(delayProvider: (T) -> Long, timeUnit: TimeUnit, scheduler: Scheduler = Schedulers.computation()) =
		flatMapSingleElement { t ->
			val delay = delayProvider(t)
			if (delay > 0) Single.timer(delay, timeUnit, scheduler).map { t }
			else Single.just(t)
		}

fun <T> List<Single<T>>.zip(defaultIfEmpty: Single<List<T>> = Single.just(emptyList())) =
		if (isNotEmpty()) Single.zip(this) { args -> args.map { it as T } }
		else defaultIfEmpty

fun <T> List<Maybe<T>>.zip() = Maybe.zip(this) { args -> args.map { it as T } }

fun <T> List<Observable<T>>.zip() = Observable.zip<T, List<T>>(this) { args -> args.map { it as T } }

fun <T> List<Observable<T>>.combineLatest() = Observable.combineLatest<T, List<T>>(this) { args -> args.map { it as T } }

fun <T> T?.optionalToMaybe() = if (this != null) Maybe.just(this) else Maybe.empty()

fun <T, U> Observable<T>.mapNotNull(mapper: (T) -> U?) = flatMapMaybe { mapper(it).optionalToMaybe() }
fun <T, U> Single<T>.mapNotNull(mapper: (T) -> U?) = flatMapMaybe { mapper(it).optionalToMaybe() }
fun <T, U> Maybe<T>.mapNotNull(mapper: (T) -> U?) = flatMap { mapper(it).optionalToMaybe() }

inline fun <reified U : Any> Observable<*>.filterOf(): Observable<U> =
		flatMap {
			if (it is U) Observable.just(it)
			else Observable.empty<U>()
		}

fun <T> Maybe<T>.delayIfEmpty(time: Long, timeUnit: TimeUnit, scheduler: Scheduler) =
		switchIfEmpty(Single.timer(time, timeUnit, scheduler).ignoreElement().toMaybe())
