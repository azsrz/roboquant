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

package org.roboquant.brokers

import org.roboquant.common.*
import java.math.BigDecimal
import java.time.Instant

/**
 * Class that holds the position of an asset in the portfolio. This implementation makes no assumptions about the
 * asset class, so it supports any type of asset class, ranging from stocks and options to cryptocurrencies.
 *
 * The actual [size] of the position is precise (doesn't lose precision like is the case with double) using the [Size]
 * class.
 *
 * @property asset the asset of the position
 * @property size size of the position, not including any contract multiplier defined at asset level
 * @property avgPrice average price paid, in the currency denoted by the asset
 * @property mktPrice Last known market price for this asset
 * @property lastUpdate When was the market price last updated
 * @constructor Create a new Position
 */
data class Position(
    val asset: Asset,
    val size: Size,
    val avgPrice: Double = 0.0,
    val mktPrice: Double = avgPrice,
    val lastUpdate: Instant = Instant.MIN
) {

    constructor(
        asset: Asset,
        size: Int,
        avgPrice: Double = 0.0,
        mktPrice: Double = avgPrice,
        lastUpdate: Instant = Instant.MIN
    ) : this(asset, Size(BigDecimal.valueOf(size.toLong())), avgPrice, mktPrice, lastUpdate)


    /**
     * The currency of this position, aka the currency of the underlying asset
     */
    val currency: Currency
        get() = asset.currency

    /**
     * @suppress
     */
    companion object {

        /**
         * Create an empty position for the provided [asset] and return this.
         */
        fun empty(asset: Asset): Position = Position(asset, Size.ZERO, 0.0, 0.0)
    }

    /**
     * Add another position [p] to this position and return the result.
     */
    operator fun plus(p: Position): Position {

        val newSize = size + p.size

        return when {
            size.sign != newSize.sign -> p.copy(size = newSize)

            newSize.absoluteValue > size.absoluteValue -> {
                val newAvgPrice = (size * avgPrice + p.size * p.avgPrice) / newSize.toDouble()
                p.copy(size = newSize, avgPrice = newAvgPrice)
            }

            else -> p.copy(size = newSize, avgPrice = avgPrice)
        }

    }

    /**
     * How much PNL would be realized when [update] a position. This doesn't change the position itself, just
     * calculates the potential realized PNL.
     */
    fun realizedPNL(update: Position): Amount {
        val newSize = size + update.size
        return when {
            size.sign != newSize.sign -> asset.value(size, update.avgPrice - avgPrice)
            newSize.absoluteValue > size.absoluteValue -> Amount(asset.currency, 0.0)
            else -> asset.value(update.size, avgPrice - update.avgPrice)
        }
    }

    /**
     * Is this a closed position, or in other words is the size equal to 0
     */
    val closed: Boolean
        get() = size.iszero

    /**
     * Is this a short position
     */
    val short: Boolean
        get() = size < 0

    /**
     * Is this a long position
     */
    val long: Boolean
        get() = size > 0

    /**
     * Is this an open position
     */
    val open: Boolean
        get() = !size.iszero

    /**
     * The unrealized profit & loss for this position based on the [avgPrice] and last known market [mktPrice],
     * in the currency denoted by the asset
     */
    val unrealizedPNL: Amount
        get() = asset.value(size, mktPrice - avgPrice)

    /**
     * The total market value for this position based on last known market price, in the currency denoted by the asset.
     * Short positions will typically return a negative value.
     */
    val marketValue: Amount
        get() = asset.value(size, mktPrice)

    /**
     * The gross exposure for this position based on last known market price, in the currency denoted by the asset.
     * The difference with the [marketValue] property is that short positions also result in a positive exposure.
     */
    val exposure: Amount
        get() = marketValue.absoluteValue

    /**
     * The total cost of this position, in the currency denoted by the asset. Short positions will typically return
     * a negative value.
     */
    val totalCost: Amount
        get() = asset.value(size, avgPrice)

}

/**
 * Return the total market value for a collection of positions
 */
val Collection<Position>.marketValue: Wallet
    get() {
        return sumOf { it.marketValue }
    }

/**
 * Return the total market value for this portfolio
 */
val Map<Asset, Position>.marketValue: Wallet
    get() {
        return values.marketValue
    }

/**
 * Return the total exposure for this portfolio
 */
val Map<Asset, Position>.exposure: Wallet
    get() {
        return values.sumOf { it.exposure }
    }

/**
 * Return the total exposure for a collection of positions
 */
val Collection<Position>.exposure: Wallet
    get() {
        return sumOf { it.exposure }
    }
