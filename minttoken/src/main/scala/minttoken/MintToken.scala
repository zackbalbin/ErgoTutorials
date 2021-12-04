package minttoken

import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.{ErgoNodeConfig, ErgoToolConfig}

object MintToken {

  def mintToken(configFileName: String): String = {
    val config: ErgoToolConfig = ErgoToolConfig.load(configFileName)
    val nodeConfig: ErgoNodeConfig = config.getNode()
    val ergoClient: ErgoClient = RestApiErgoClient.create(nodeConfig)

    val addressIndex: Int = config.getParameters().get("addressIndex").toInt
    
    val txJson: String = ergoClient.execute((ctx: BlockchainContext) => {
      val prover: ErgoProver = ctx.newProverBuilder()
        .withMnemonic(
          SecretString.create(nodeConfig.getWallet().getMnemonic()),
          SecretString.create(nodeConfig.getWallet().getPassword()))
        .withEip3Secret(addressIndex)
        .build()

      val wallet: ErgoWallet = ctx.getWallet()
      val amountToSpend: Long = Parameters.OneErg 

      "D"
    })
    txJson
  }

  def main(args: Array[String]): Unit = {
    println("Mint token tutorial")
  }
}