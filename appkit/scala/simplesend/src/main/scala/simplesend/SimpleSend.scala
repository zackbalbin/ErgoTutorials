package simplesend

import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.{ErgoNodeConfig, ErgoToolConfig}

object SimpleSend {

  def sendTx(configFileName: String): String = {
    val config: ErgoToolConfig = ErgoToolConfig.load(configFileName)
    val nodeConfig: ErgoNodeConfig = config.getNode()
    val ergoClient: ErgoClient = RestApiErgoClient.create(nodeConfig)

    val addressIndex: Int = config.getParameters().get("addressIndex").toInt
    val recieverWalletAddress: Address = Address.create(config.getParameters().get("recieverWalletAddress"))

    val txJson: String = ergoClient.execute((ctx: BlockchainContext) => {
      val prover: ErgoProver = ctx.newProverBuilder()
        .withMnemonic(
          SecretString.create(nodeConfig.getWallet().getMnemonic()),
          SecretString.create(nodeConfig.getWallet().getPassword()))
        .withEip3Secret(addressIndex)
        .build()

      val wallet: ErgoWallet = ctx.getWallet()
      val amountToSpend: Long = Parameters.OneErg
      val totalToSpend: Long = amountToSpend + Parameters.MinFee
      val boxes: java.util.Optional[java.util.List[InputBox]] = wallet.getUnspentBoxes(totalToSpend)
      if (!boxes.isPresent())
       throw new ErgoClientException(s"Not enough coins in the walelt to pay $totalToSpend", null)

      val txBuilder = ctx.newTxBuilder()
      
      val newBox = txBuilder.outBoxBuilder()
        .value(amountToSpend)
        .contract(ctx.compileContract(
          ConstantsBuilder.create()
            .item("recPk", recieverWalletAddress.getPublicKey())
            .build(),
          "{ recPk }")
        ) 
        .build()

      val tx: UnsignedTransaction = txBuilder
        .boxesToSpend(boxes.get)
        .outputs(newBox)
        .fee(Parameters.MinFee)
        .sendChangeTo(prover.getP2PKAddress())
        .build()

      val signed: SignedTransaction = prover.sign(tx)

      val txId: String = ctx.sendTransaction(signed)

      signed.toJson(true)
    })
    txJson
  }

  def main(args: Array[String]): Unit = {
    val txJson: String = sendTx("ergo_config.json")
    println(txJson)
  }
}