package simplesend

import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.{ErgoNodeConfig, ErgoToolConfig}

object SimpleSend {

  def sendTx(configFileName: String): String = {
    val config: ErgoToolConfig = ErgoToolConfig.load(configFileName)
    val nodeConfig: ErgoNodeConfig = config.getNode()
    val ergoClient: ErgoClient = RestApiErgoClient.create(nodeConfig, "https://api-testnet.ergoplatform.com")

    val recieverWalletAddress: Address = Address.create(config.getParameters().get("recieverWalletAddress"))

    val txJson: String = ergoClient.execute((ctx: BlockchainContext) => {
      val walletMnemonic: String = nodeConfig.getWallet().getMnemonic()
      val walletPassword: String = nodeConfig.getWallet().getPassword()
      val mnemonic: Mnemonic = Mnemonic.create(walletMnemonic.toCharArray(), walletPassword.toCharArray())

      val senderProver: ErgoProver = ctx.newProverBuilder()
        .withMnemonic(
          SecretString.create(walletMnemonic),
          SecretString.create(walletPassword))
        .withEip3Secret(0)
        .build()
        
      val senderAddress: Address = Address.createEip3Address(0, NetworkType.TESTNET, SecretString.create(walletMnemonic.toCharArray()), SecretString.create(walletPassword.toCharArray()))

      val amountToSpend: Long = Parameters.OneErg
      val totalToSpend: Long = amountToSpend + Parameters.MinFee

      val unspentBoxes = ctx.getUnspentBoxesFor(senderAddress, 0, 20)
      val boxes = BoxOperations.selectTop(unspentBoxes, totalToSpend)
      if (boxes.isEmpty())
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
        .boxesToSpend(boxes)
        .outputs(newBox)
        .fee(Parameters.MinFee)
        .sendChangeTo(senderAddress.asP2PK())
        .build()

      val signed: SignedTransaction = senderProver.sign(tx)

      val txId: String = ctx.sendTransaction(signed)

      signed.toJson(true)
    })
    txJson
  }

  def main(args: Array[String]): Unit = {
    val txJson: String = sendTx("testnet.json")
    println(txJson)
  }
}