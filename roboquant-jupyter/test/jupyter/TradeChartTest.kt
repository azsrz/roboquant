/*
 * Copyright 2021 Neural Layer
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

package org.roboquant.jupyter

import org.junit.jupiter.api.Test
import org.roboquant.TestData
import kotlin.test.assertTrue

internal class TradeChartTest {

    @Test
    fun test() {
        val account = TestData.usAccount()
        val chart = TradeChart(account.trades)
        assertTrue(chart.asHTML().isNotBlank())
    }


    @Test
    fun test2() {
        val account = TestData.usAccount()
        val chart = TradeChartByAsset(account.trades)
        assertTrue(chart.asHTML().isNotBlank())
    }

}