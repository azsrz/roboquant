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

package org.roboquant.strategies

import org.roboquant.common.Timeframe
import org.roboquant.feeds.Event

/**
 * Combine the output of several strategies into a single list of signals. There is no logic included to filter
 * conflicting signals, like simultaneously BUY and SELL signals for the same asset. Also, the strategies are
 * run sequential. For parallel execution see [ParallelStrategy]
 *
 * @property strategies the strategies to combine
 * @constructor Create a new instance of a Combined Strategy
 */
open class CombinedStrategy(val strategies: Collection<Strategy>) : Strategy {

    constructor(vararg strategies: Strategy) : this(strategies.toList())

    override fun generate(event: Event): List<Signal> {
        val signals = mutableListOf<Signal>()

        for (strategy in strategies) {
            val s = strategy.generate(event)
            signals.addAll(s)
        }
        return signals
    }

    override fun start(run: String, timeframe: Timeframe) {
        for (strategy in strategies) strategy.start(run, Timeframe.INFINITE)
    }

    override fun end(run: String) {
        for (strategy in strategies) strategy.end(run)
    }

    override fun reset() {
        for (strategy in strategies) strategy.reset()
    }

    override fun getMetrics(): Map<String, Double> {
        val result = mutableMapOf<String, Double>()
        strategies.forEach { result += it.getMetrics() }
        return result
    }
}
