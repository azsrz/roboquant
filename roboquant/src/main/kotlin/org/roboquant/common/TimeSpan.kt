/*
 * Copyright 2020-2023 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused")

package org.roboquant.common

import java.time.*

/**
 * Deprecated, use [TimeSpan] instead
 */
@Deprecated("Renamed to TimeSpan", ReplaceWith("TimeSpan", "org.roboquant.common.TimeSpan"))
typealias TradingPeriod = TimeSpan

/**
 * TimeSpan is an immutable class that unifies the JVM classes Duration and Period and allows to use durations
 * more easily in your code. It can store time-spans as small as nanoseconds.
 *
 * It is loosely modelled after the time duration, as described in ISO 8601.
 */
class TimeSpan internal constructor(internal val period: Period, internal val duration: Duration) {

    /**
     * Create a new instance of TimeSpan
     */
    constructor(
        years: Int = 0,
        months: Int = 0,
        days: Int = 0,
        hours: Int = 0,
        minutes: Int = 0,
        seconds: Int = 0,
        nanos: Long = 0L
    ) : this(Period.of(years, months, days).normalized(), createDuration(hours, minutes, seconds, nanos))

    /**
     * @suppress
     */
    companion object {

        /**
         * TImeSpan of 0
         */
        val ZERO = TimeSpan()


        /**
         * Parse the string that is generated by [TimeSpan.toString] back to an instance of TimeSpan.
         *
         * This is also compliant with the ISO 8601 format for durations:
         *
         * ```
         * P[n]Y[n]M[n]DT[n]H[n]M[n]S
         * ```
         */
        fun parse(text: String): TimeSpan {
            val parts = text.split('T')
            return TimeSpan(Period.parse(parts[0]), Duration.parse("PT" + parts[1]))
        }


        /**
         * Create a [Duration] instance based on hours, seconds and nanos
         */
        private fun createDuration(hours: Int, minutes: Int, seconds: Int, nanos: Long): Duration {
            var result = Duration.ZERO
            if (hours != 0) result = result.plusHours(hours.toLong())
            if (minutes != 0) result = result.plusMinutes(minutes.toLong())
            if (seconds != 0) result = result.plusSeconds(seconds.toLong())
            if (nanos != 0L) result = result.plusNanos(nanos)
            return result
        }


    }

    /**
     * Checks if this is a zero TimeSpan
     */
    val isZero
        get() = period.isZero && duration.isZero

    /**
     * Add an [other] trading period
     */
    operator fun plus(other: TimeSpan) =
        TimeSpan(period.plus(other.period).normalized(), duration.plus(other.duration))

    /**
     * Subtract an [other] trading period
     */
    operator fun minus(other: TimeSpan) =
        TimeSpan(period.minus(other.period).normalized(), duration.minus(other.duration))

    /**
     * String representation
     */
    override fun toString(): String {
        return "$period${duration.toString().substring(1)}"
    }

    /**
     * Only equals if all values are the same
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return if (other is TimeSpan) {
            period == other.period && duration == other.duration
        } else {
            false
        }
    }

    /**
     * @see [Object.hashCode]
     */
    override fun hashCode(): Int {
        return period.hashCode() + duration.hashCode()
    }

}

/*********************************************************************************************
 * Extensions on the Int type to make instantiation of TradingPeriods convenient
 *********************************************************************************************/

/**
 * Convert an Int to a [TimeSpan] of years
 */
val Int.years
    get() = TimeSpan(this)

/**
 * Convert an Int to a [TimeSpan] of months
 */
val Int.months
    get() = TimeSpan(0, this)

/**
 * Convert an Int to a [TimeSpan] of days
 */
val Int.days
    get() = TimeSpan(0, 0, this)

/**
 * Convert an Int to a [TimeSpan] of hours
 */
val Int.hours
    get() = TimeSpan(0, 0, 0, this)

/**
 * Convert an Int to a [TimeSpan] of minutes
 */
val Int.minutes
    get() = TimeSpan(0, 0, 0, 0, this)

/**
 * Convert an Int to a [TimeSpan] of seconds
 */
val Int.seconds
    get() = TimeSpan(0, 0, 0, 0, 0, this, 0L)

/**
 * Convert an Int to a [TimeSpan] of milliseconds
 */
val Int.millis
    get() = (this * 1_000_000L).nanos

/**
 * Convert an Int to a [TimeSpan] of nanoseconds
 */
val Long.nanos
    get() = TimeSpan(0, 0, 0, 0, 0, 0, this)

/**
 * Add a [period] using the provided [zoneId]
 */
fun Instant.plus(period: TimeSpan, zoneId: ZoneId): Instant {
    // Optimized path for HFT
    if (period == TimeSpan.ZERO) return this
    val result = if (period.period == Period.ZERO) this else atZone(zoneId).plus(period.period).toInstant()
    return result.plus(period.duration)
}

/**
 * Subtract a [period] using the provided [zoneId]
 */
fun Instant.minus(period: TimeSpan, zoneId: ZoneId): Instant {
    // Optimized path for HFT
    if (period == TimeSpan.ZERO) return this
    val result = if (period.period == Period.ZERO) this else atZone(zoneId).minus(period.period).toInstant()
    return result.minus(period.duration)
}

/**
 * Add a [period] using UTC
 */
operator fun Instant.plus(period: TimeSpan) = plus(period, ZoneOffset.UTC)

/**
 * Add a [period]
 */
operator fun ZonedDateTime.plus(period: TimeSpan): ZonedDateTime = plus(period.period).plus(period.duration)

/**
 * Subtract a [period] using UTC
 */
operator fun Instant.minus(period: TimeSpan) = minus(period, ZoneOffset.UTC)

/**
 * Subtract a [period]
 */
operator fun ZonedDateTime.minus(period: TimeSpan): ZonedDateTime = minus(period.period).minus(period.duration)
