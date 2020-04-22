/*
 * MIT License
 *
 * Copyright (c) 2020 Hiberbee
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.hiberbee.gatling.simulations

import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

class IndexPageSimulation extends Simulation {

  val concurrency: Int = Integer.getInteger("concurrency", 25).toInt
  val rampUpTime: Int = Integer.getInteger("ramp-up", 0).toInt
  val holdForTime: Int = Integer.getInteger("hold-for", 0).toInt
  val throughput: Integer = Integer.getInteger("throughput")
  val iterationLimit: Integer = Integer.getInteger("iterations")
  val baseUrl: String = System.getProperty("base-url", "https://hiberbee.dev")
  val durationLimit: Int = rampUpTime + holdForTime

  var httpConf: HttpProtocolBuilder = http.baseUrl(baseUrl).header(HttpHeaderNames.Connection, HttpHeaderValues.Close).disableCaching

  var execution: ChainBuilder = exec {
    http("/").get("/").header(HttpHeaderNames.ContentType, HttpHeaderValues.TextHtml).check(substring("""Hiberbee""").exists)
  }

  var testScenario: ScenarioBuilder = scenario("Index Scenario")

  if (iterationLimit == null)
    testScenario = testScenario.forever {
      execution
    }
  else
    testScenario = testScenario.repeat(iterationLimit.toInt) {
      execution
    }

  var testSetup: SetUp = setUp(testScenario.inject(atOnceUsers(concurrency)).protocols(httpConf))
  if (throughput != null) testSetup = testSetup.throttle(reachRps(throughput.toInt) in rampUpTime, holdFor(Int.MaxValue))
  if (durationLimit > 0) testSetup.maxDuration(durationLimit)
  testSetup.assertions(
    global.responseTime.max.lte(1000),
    global.requestsPerSec.gt(500),
    global.successfulRequests.percent.gte(95),
  )
}
