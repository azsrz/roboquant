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

package org.roboquant.ta

import org.roboquant.brokers.Account
import org.roboquant.common.Asset
import org.roboquant.common.AssetFilter
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import org.roboquant.metrics.Metric
import org.roboquant.metrics.MetricResults

/**
 * Use a technical indicator from the Ta-Lib library as a metric. Metrics will be available under the
 * name: [name].symbol with the symbol in lowercase.
 *
 * @property name the base name of the metric
 * @property history how much history should be kept before invoking the metric
 * @property assetFilter which assets to process, default is [AssetFilter.all]
 * @property block the logic to use as an indicator
 * @constructor Create new metric
 */
class TaLibMetric(
    private val name: String,
    private val history: Int = 15,
    private val assetFilter: AssetFilter = AssetFilter.all(),
    private var block: TaLib.(series: PriceBarSerie) -> Double
) : Metric {

    private val buffers = mutableMapOf<Asset, PriceBarSerie>()
    private val taLib = TaLib()
    // private val logger: Logger = Logging.getLogger(TALibMetric::class)


    /**
     * @see Metric.calculate
     */
    override fun calculate(account: Account, event: Event): MetricResults {
        val metrics = mutableMapOf<String, Double>()
        val actions =
            event.prices.values.filterIsInstance<PriceBar>().filter { assetFilter.filter(it.asset, event.time) }
        for (priceAction in actions) {
            val asset = priceAction.asset
            val buffer = buffers.getOrPut(asset) { PriceBarSerie(history) }
            if (buffer.add(priceAction)) {
                val metric = block.invoke(taLib, buffer)
                val name = "$name.${asset.symbol.lowercase()}"
                metrics[name] = metric
            }
        }
        return metrics
    }

    /**
     * reset state of this metric
     */
    override fun reset() {
        super.reset()
        buffers.clear()
    }

}