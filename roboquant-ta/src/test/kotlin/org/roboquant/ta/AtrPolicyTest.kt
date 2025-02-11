/*
 * Copyright 2020-2023 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.ta

import org.junit.jupiter.api.assertThrows
import org.roboquant.brokers.Account
import org.roboquant.brokers.sim.execution.InternalAccount
import org.roboquant.common.*
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import org.roboquant.orders.BracketOrder
import org.roboquant.orders.LimitOrder
import org.roboquant.orders.Order
import org.roboquant.orders.StopOrder
import org.roboquant.policies.FlexPolicy
import org.roboquant.strategies.Rating
import org.roboquant.strategies.Signal
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AtrPolicyTest {

    private fun usAccount(amount: Amount = 100_000.USD): Account {
        val account = InternalAccount(amount.currency)
        account.cash.deposit(amount)
        account.buyingPower = amount
        account.lastUpdate = Instant.now()
        return account.toAccount()
    }

    private fun run(policy: FlexPolicy): List<Order> {
        val asset = Asset("TEST")
        val signals = listOf(Signal(asset, Rating.BUY))
        val now = Instant.now()
        val account = usAccount()
        val orders = mutableListOf<Order>()

        // Create an ATR of $1
        repeat(12) {
            val p = 5.0
            val priceBar = PriceBar(asset, p + it, p + it, p + it, p + it)
            val event = Event(listOf(priceBar), now + it.millis)
            val o = policy.act(signals, account, event)
            orders.addAll(o)
        }
        return orders
    }

    @Test
    fun bracketATR() {
        val p = AtrPolicy(10, 4.0, 2.0, null) {
            orderPercentage = 0.02
        }
        val orders = run(p)
        assertTrue(orders.isNotEmpty())

        val order = orders.last()
        assertTrue(order is BracketOrder)
        assertEquals(Size(125), order.entry.size)

        val stopLoss = order.stopLoss
        assertTrue(stopLoss is StopOrder)
        assertEquals(14.0, stopLoss.stop)

        val takeProfit = order.takeProfit
        assertTrue(takeProfit is LimitOrder)
        assertEquals(20.0, takeProfit.limit)
    }

    @Test
    fun bracketSizingAtr() {
        val p = AtrPolicy(10, 4.0, 2.0, 0.1) {
            orderPercentage = 0.02
        }
        val orders = run(p)
        assertTrue(orders.isNotEmpty())
        val order = orders.last()
        assertTrue(order is BracketOrder)
        assertEquals(Size(100), order.entry.size)

    }

    @Test
    fun bracketSizingAtrValidation() {
        assertThrows<IllegalArgumentException> {
            AtrPolicy(10, 4.0, 2.0, 1.3) {
                orderPercentage = 0.02
            }
        }
    }

}
