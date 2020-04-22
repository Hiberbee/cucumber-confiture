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

/*
 * Copyright by Piping Rock Health Products, LLC
 * 2020 (C) All Rights Reserved
 *
 * This file is a part of Projects.qa-test-automation.gatling package
 * and is subject to the terms and conditions defined
 * in file 'LICENSE', which is part of this source code.
 * Unauthorized copying of this file, via any medium
 * is strictly prohibited - proprietary and confidential
 */

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class IndexPageSimulation extends Simulation {

  val users: Int = Integer.getInteger("users", 1)
  val period: Int = Integer.getInteger("period", 1)
  val rps: Int = Integer.getInteger("rps", 1)

  def baseUrl = "https://hiberbee.dev"

  setUp(
    scenario(this.getClass.getSimpleName)
      .exec(http("home").get("/").header(HttpHeaderNames.Accept, HttpHeaderValues.TextHtml).check(status.is(200)))
      .inject(rampConcurrentUsers(users / 2) to users during 10)
      .throttle(reachRps(rps) in period)
      .protocols(http.baseUrl(baseUrl))
      .disablePauses)
    .assertions(
      global.responseTime.max.lt(1000),
      global.successfulRequests.percent.gt(95)
    )

}
