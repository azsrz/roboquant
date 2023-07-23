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

package org.roboquant.loggers

import org.roboquant.TestData
import org.roboquant.common.millis
import org.roboquant.common.plus
import java.time.Instant
import kotlin.test.*

internal class LastEntryLoggerTest {

    @Test
    fun test() {
        val metrics = TestData.getMetrics()
        val logger = LastEntryLogger()
        assertFalse(logger.showProgress)

        logger.log(metrics, Instant.now(), "test")
        logger.end("test")
        assertTrue(logger.metricNames.isNotEmpty())
        assertContains(logger.metricNames, metrics.keys.first())

        val m1 = logger.metricNames.first()
        val m = logger.getMetric(m1).latestRun()
        assertTrue(m.isNotEmpty())

        logger.reset()
        assertTrue(logger.metricNames.isEmpty())
    }

    @Test
    fun testSequence() {
        val metrics = TestData.getMetrics()
        val logger = LastEntryLogger()
        assertFalse(logger.showProgress)

        val now = Instant.now()
        repeat(100) {
            logger.log(metrics, now + it.millis, "test")
        }

        logger.end("test")
        assertTrue(logger.metricNames.isNotEmpty())

        val m1 = logger.metricNames.first()
        val m = logger.getMetric(m1).latestRun()
        assertEquals(m.timeline.sorted(), m.timeline)
    }

}