package sendtoken

import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.{ErgoNodeConfig, ErgoToolConfig}

object SendToken {

  def sendToken(configFileName: String): String = {
    "d"
  }

  def main(args: Array[String]): Unit = {
    val txJson: String = sendToken("ergo_config.json")
    println(txJson)
  }
}