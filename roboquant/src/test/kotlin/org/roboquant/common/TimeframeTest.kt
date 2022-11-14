/*
 * Copyright 2020-2022 Neural Layer
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

package org.roboquant.common

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TimeframeTest {

    @Test
    fun split() {
        val tf = Timeframe.fromYears(1980, 1999)
        val subFrames = tf.split(2.years)
        assertEquals(10, subFrames.size)
        assertEquals(tf.start, subFrames.first().start)
        assertEquals(tf.end, subFrames.last().end)

        val tf2 = Timeframe.past(24.hours)
        val subFrames2 = tf2.split(30.minutes)
        assertEquals(48, subFrames2.size)
    }


    @Test
    fun parse() {
        val tf = Timeframe.parse("2019-01-01T00:00:00Z", "2020-01-01T00:00:00Z")
        assertEquals(tf, Timeframe.parse("2019", "2020"))
        assertEquals(tf, Timeframe.parse("2019-01", "2020-01"))
        assertEquals(tf, Timeframe.parse("2019-01-01", "2020-01-01"))
        assertEquals(tf, Timeframe.parse("2019-01-01T00:00:00", "2020-01-01T00:00:00"))
        assertEquals(tf, Timeframe.parse("2019-01-01T00:00:00Z", "2020-01-01T00:00:00Z"))
    }

    @Test
    fun constants() {
        val tf2 = Timeframe.INFINITE
        assertEquals(Timeframe.INFINITE, tf2)
        assertTrue(tf2.isInfinite())

        assertTrue(Timeframe.blackMonday1987.isSingleDay(ZoneId.of("America/New_York")))
        assertFalse(Timeframe.coronaCrash2020.isSingleDay())
        assertTrue(Timeframe.flashCrash2010.isSingleDay(ZoneId.of("America/New_York")))
        assertFalse(Timeframe.financialCrisis2008.isSingleDay())
        assertFalse(Timeframe.tenYearBullMarket2009.isSingleDay())
    }

    @Test
    fun print() {
        val tf2 = Timeframe.INFINITE

        val s2 = tf2.toString()
        assertTrue(s2.isNotBlank())

        val s3 = tf2.toRawString()
        assertTrue(s3.isNotBlank())
    }

    @Test
    fun creation() {
        val tf = Timeframe.next(1.minutes)
        assertEquals(60, tf.end.epochSecond - tf.start.epochSecond)

        val tf2 = Timeframe.past(2.years)
        assertEquals(tf2.start, (tf2 - 2.years).end)

        assertThrows<IllegalArgumentException> {
            Timeframe.fromYears(1800, 2000)
        }
    }

    @Test
    fun annualize() {
        val tf = Timeframe.fromYears(2019, 2020)
        val x = tf.annualize(0.1)
        assertTrue(x - 0.1 < 0.01)

        val tf2 = Timeframe.fromYears(2019, 2020)
        val y = tf2.annualize(0.1)
        assertTrue(0.05 - y < 0.1)
    }

    @Test
    fun contains() {
        val tf = Timeframe.fromYears(2019, 2020)
        assertFalse(tf.end in tf)
        assertTrue(tf.end in tf.toInclusive())
    }

    @Test
    fun plusMinus() {
        val tf = Timeframe.fromYears(2019, 2020)
        val tf2 = tf + 2.years - 2.years
        assertEquals(tf, tf2)
    }

    @Test
    fun toTimeline() {
        val tf = Timeframe.parse("2020-01-01T18:00:00Z", "2021-12-31T18:00:00Z")
        val timeline = tf.toTimeline(1.days)
        assertTrue(timeline.size > 200)
    }
}