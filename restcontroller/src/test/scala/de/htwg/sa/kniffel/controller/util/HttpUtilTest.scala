package de.htwg.sa.kniffel.controller.util

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.net.{ConnectException, MalformedURLException}

class HttpUtilTest extends AnyWordSpec with Matchers:
  "HttpUtil" when {
    "a invalid Request is send" should {
      "throw an Exception" in {
        intercept[MalformedURLException] {
          HttpUtil.sendRequest("hfiuew93z", "J7RCU")
        }
        intercept[ConnectException] {
          HttpUtil.sendRequest("http://localhost:54321/", "J7RCU")
        }
        intercept[RuntimeException] {
          HttpUtil.sendPUTRequest("http://localhost/", "J7RCU")
        }
        intercept[MalformedURLException] {
          HttpUtil.sendPUTRequest("hfiuew93z", "J7RCU")
        }
        intercept[ConnectException] {
          HttpUtil.sendPUTRequest("http://localhost:54321/", "J7RCU")
        }
        intercept[RuntimeException] {
          HttpUtil.sendPUTRequest("http://localhost/", "J7RCU")
        }


      }
    }
  }
