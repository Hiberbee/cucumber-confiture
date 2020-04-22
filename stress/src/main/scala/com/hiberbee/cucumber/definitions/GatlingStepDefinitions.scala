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

package com.hiberbee.cucumber.definitions

import java.security.Permission

import com.typesafe.scalalogging.Logger
import io.cucumber.scala.{EN, ScalaDsl}
import io.gatling.app.Gatling

class GatlingStepDefinitions extends ScalaDsl with EN {

  object GatlingSecurityManager extends SecurityManager {
    override def checkExit(status: Int): Unit = {
      throw new SecurityException("Tried to exit.")
    }

    override def checkPermission(perm: Permission): Unit = {
    }
  }

  Given("""{int} users running {int} rps in {int} seconds""") { (users: Int, rps: Int, period: Int) =>
    val sm = System.getSecurityManager
    System.setSecurityManager(GatlingSecurityManager)
    val args = Array(
      "--simulation",
      "com.hiberbee.gatling.simulations.IndexPageSimulation",
      "--results-folder",
      "build/reports/gatling",
      "-Dusers=" + users,
      "-Drps=" + rps,
      "-Dperiod=" + period,
    )
    try Gatling.main(args)
    catch {
      case se: SecurityException => Logger -> "Gatling test finished"
    }
    System.setSecurityManager(sm)
  }
}
