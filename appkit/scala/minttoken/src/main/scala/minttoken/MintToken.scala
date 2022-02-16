package minttoken

import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.{ErgoNodeConfig, ErgoToolConfig}
import org.ergoplatform.appkit.impl.ErgoTreeContract

object MintToken {

  def mintToken(configFileName: String): String = {
    val config: ErgoToolConfig = ErgoToolConfig.load(configFileName)
    val nodeConfig: ErgoNodeConfig = config.getNode()
    val ergoClient: ErgoClient = RestApiErgoClient.create(nodeConfig, "https://api-testnet.ergoplatform.com")

    val tokenName: String = "Ergo Tutorials Mint Token 2"
    val tokenDescription: String = "Example for minting a token with appkit"
    val tokenAmount: Long = 1L
    val tokenDecimals: Int = 0
    
    val txJson: String = ergoClient.execute((ctx: BlockchainContext) => {
      val amountToSpend: Long = Parameters.OneErg
      val totalToSpend: Long = amountToSpend + Parameters.MinFee

      val walletMnomonic: String = nodeConfig.getWallet().getMnemonic()
      val walletPassword: String = nodeConfig.getWallet().getPassword()
      val mnemonic = Mnemonic.create(walletMnomonic.toCharArray(), walletPassword.toCharArray())

      val senderProver: ErgoProver = ctx.newProverBuilder()
        .withMnemonic(
          SecretString.create(walletMnomonic),
          SecretString.create(walletPassword))
        .withEip3Secret(0)
        .build()

      val senderAddress: Address = Address.createEip3Address(0, NetworkType.TESTNET, SecretString.create(walletMnomonic.toCharArray()), SecretString.create(walletPassword.toCharArray()))

      val unspent = ctx.getUnspentBoxesFor(senderAddress, 0, 20)
      val boxes = BoxOperations.selectTop(unspent, totalToSpend)
      if (boxes.isEmpty())
        throw new ErgoClientException(s"Not enough funds in the wallet for $totalToSpend", null)

      val token = new ErgoToken(boxes.get(0).getId(), tokenAmount)

      val txBuilder = ctx.newTxBuilder()
      
      val newBox = txBuilder.outBoxBuilder()
        .value(amountToSpend)
        .mintToken(token, tokenName, tokenDescription, tokenDecimals)
        .contract(new ErgoTreeContract(senderAddress.getErgoAddress().script))
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
    val txJson: String = mintToken("config.json")
    println(txJson)
  }
}