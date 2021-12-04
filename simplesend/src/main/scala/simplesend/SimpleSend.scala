package simplesend

// Appkit Libraries
import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.{ErgoNodeConfig, ErgoToolConfig}

object SimpleSend {

  // Function to send transaction
  def sendTx(configFileName: String): String = {
    // Configuration Setup
    val config: ErgoToolConfig = ErgoToolConfig.load(configFileName)
    val nodeConfig: ErgoNodeConfig = config.getNode()
    val ergoClient: ErgoClient = RestApiErgoClient.create(nodeConfig)

    // Variable imports from config file
    val addressIndex: Int = config.getParameters().get("addressIndex").toInt
    val recieverWalletAddress: Address = Address.create(config.getParameters().get("recieverWalletAddress"))

    // All the logic to create and send the transaction
    val txJson: String = ergoClient.execute((ctx: BlockchainContext) => {
      // Create a prover object with data from nodeConfig
      val prover: ErgoProver = ctx.newProverBuilder()
        .withMnemonic(
          SecretString.create(nodeConfig.getWallet().getMnemonic()),
          SecretString.create(nodeConfig.getWallet().getPassword()))
        .withEip3Secret(addressIndex)
        .build()

      // Get funds info from wallet
      val wallet: ErgoWallet = ctx.getWallet()
      val amountToSpend: Long = Parameters.OneErg
      val totalToSpend: Long = amountToSpend + Parameters.MinFee
      val boxes: java.util.Optional[java.util.List[InputBox]] = wallet.getUnspentBoxes(totalToSpend)
      if (!boxes.isPresent())
       throw new ErgoClientException(s"Not enough coins in the walelt to pay $totalToSpend", null)

      val txBuilder = ctx.newTxBuilder()
      
      // Create new UTXO to spend
      val newBox = txBuilder.outBoxBuilder()
        .value(amountToSpend)
        .contract(ctx.compileContract(
          ConstantsBuilder.create()
            .item("recPk", recieverWalletAddress.getPublicKey())
            .build(),
          "{ recPk }")
        ) 
        .build()

      // Create new transaction from the created UTXO
      val tx: UnsignedTransaction = txBuilder
        .boxesToSpend(boxes.get)
        .outputs(newBox)
        .fee(Parameters.MinFee)
        .sendChangeTo(prover.getP2PKAddress())
        .build()

      // Sign the new transaction
      val signed: SignedTransaction = prover.sign(tx)

      // Send the new transaction
      val txId: String = ctx.sendTransaction(signed)

      // Convert the transaction information to JSON
      signed.toJson(true)
    })
    // Return transaction JSON
    txJson
  }

  def main(args: Array[String]): Unit = {
    // Call sendTx function with the config file name as the parameter
    val txJson: String = sendTx("ergo_config.json")
    // Print out JSON information
    System.out.println(txJson)
  }
}