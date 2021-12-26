package sendtoken

import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.{ErgoNodeConfig, ErgoToolConfig}

object SendToken {

  def sendToken(configFileName: String): String = {
    val config: ErgoToolConfig = ErgoToolConfig.load(configFileName)
    val nodeConfig: ErgoNodeConfig = config.getNode()
    val ergoClient: ErgoClient = RestApiErgoClient.create(nodeConfig)

    val addressIndex: Int = config.getParameters().get("addressIndex").toInt
    val tokenId: String = config.getParameters().get("tokenId")
    val recieverWalletAddress: Address = Address.create(config.getParameters().get("recieverWalletAddress"))
    
    val txJson: String = ergoClient.execute((ctx: BlockchainContext) => {
      val amountToSpend: Long = 0L
      val totalToSpend: Long = amountToSpend + Parameters.MinFee

      val mnemonicString = nodeConfig.getWallet().getMnemonic()
      val mnemonicPasswordString = nodeConfig.getWallet().getMnemonicPassword()
      val mnemonic = Mnemonic.create(mnemonicString.toCharArray(), mnemonicPasswordString.toCharArray())

      val senderProver = BoxOperations.createProver(ctx, mnemonic)
      val sender = senderProver.getAddress()
      val unspent = ctx.getUnspentBoxesFor(sender)
      val boxesToSpend = BoxOperations.selectTop(unspent, totalToSpend)

      //val token = new ErgoToken(boxesToSpend.get(0).getId(), tokenAmount)

      val txBuilder = ctx.newTxBuilder()
      
      val newBox = txBuilder.outBoxBuilder()
        .value(amountToSpend)
        //.mintToken(token, tokenName, tokenDescription, tokenDecimals)
        .contract(ErgoContracts.sendToPK(ctx, recieverWalletAddress))
        .build()

      val tx: UnsignedTransaction = txBuilder
        .boxesToSpend(boxesToSpend)
        .outputs(newBox)
        .fee(Parameters.MinFee)
        .sendChangeTo(senderProver.getP2PKAddress())
        .build()

      val signed: SignedTransaction = senderProver.sign(tx)

      val txId: String = ctx.sendTransaction(signed)

      signed.toJson(true)
    })
    txJson
  }

  def main(args: Array[String]): Unit = {
    val txJson: String = sendToken("ergo_config.json")
    println(txJson)
  }
}