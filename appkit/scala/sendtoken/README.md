# Send Token Readme

A simple script for sending already minted tokens.

### Tutorial

Now that we are in the third tutorial I am going to assume you know the basics like creating a prover and setting the values of a new box. This tutorial is very similiar to the Mint Token tutorial wth a few minor differences. Instead of creating a new token and setting all those values, we just need to specify which token to send by setting the tokenId. After, we can specify the amount of token we want to send.

**Step 1** Setup File

```scala
package sendtoken

import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.{ErgoNodeConfig, ErgoToolConfig}
import org.ergoplatform.appkit.impl.ErgoTreeContract

object SendToken {

  def sendToken(configFileName: String): String = {
    val config: ErgoToolConfig = ErgoToolConfig.load(configFileName)
    val nodeConfig: ErgoNodeConfig = config.getNode()
    val ergoClient: ErgoClient = RestApiErgoClient.create(nodeConfig, "https://api-testnet.ergoplatform.com")

    val txJson: String = ergoClient.execute((ctx: BlockchainContext) => {
      val amountToSpend: Long = Parameters.MinChangeValue
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

      val txBuilder = ctx.newTxBuilder()

      val token = new ErgoToken(tokenId, tokenAmount)

      val newBox = txBuilder.outBoxBuilder()
        .value(amountToSpend)
        .contract(new ErgoTreeContract(recieverWalletAddress.getErgoAddress().script))
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
    val txJson: String = sendToken("config.json")
    println(txJson)
  }
}
```

**Step 2** Set values

Here we will set the values of the token ID, the amount of the token we want to send, and the address of the reciever.

```scala
val tokenId: String = "713f44acce34381cc6a83ef228a6c2cbb31f6452b7edc6f9fc18fb5c8f1bbeb0"
val tokenAmount: Long = 1L
val recieverWalletAddress: Address = Address.create("3WycHxEz8ExeEWpUBwvu1FKrpY8YQCiH1S9PfnAvBX1K73BXBXZa")
```

**Step 3** Create the token object

Now we need to create a token object that we can use put into the newBox. The ErgoToken object takes in a id of type String and an amount of type Long. We can use the values we set in the last step.

```scala
val token = new ErgoToken(tokenId, tokenAmount)
```

**Step 4** Create the newBox

Now we can create the newBox. We use the transaction builder to do this. We can add the token to the new box by calling .tokens() and pass our new token into it. Finally, we can set the reciever address by creating a new ErgoTreeContract and passing in the recieverWalletAddress from step 2. We need to access the ergoAddress.script property in order to do this.

```scala
val newBox = txBuilder.outBoxBuilder()
    .value(amountToSpend)
    .tokens(token)
    .contract(new ErgoTreeContract(recieverWalletAddress.getErgoAddress().script))
    .build()
```

This is what our entire script will look like once its done.

```scala
package sendtoken

import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.{ErgoNodeConfig, ErgoToolConfig}
import org.ergoplatform.appkit.impl.ErgoTreeContract

object SendToken {

  def sendToken(configFileName: String): String = {
    val config: ErgoToolConfig = ErgoToolConfig.load(configFileName)
    val nodeConfig: ErgoNodeConfig = config.getNode()
    val ergoClient: ErgoClient = RestApiErgoClient.create(nodeConfig, "https://api-testnet.ergoplatform.com")

    val tokenId: String = ""
    val tokenAmount: Long = 0L
    val recieverWalletAddress: Address = Address.create("")

    val txJson: String = ergoClient.execute((ctx: BlockchainContext) => {
      val amountToSpend: Long = Parameters.MinChangeValue
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

      val txBuilder = ctx.newTxBuilder()

      val token = new ErgoToken(tokenId, tokenAmount)

      val newBox = txBuilder.outBoxBuilder()
        .value(amountToSpend)
        .tokens(token)
        .contract(new ErgoTreeContract(recieverWalletAddress.getErgoAddress().script))
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
    val txJson: String = sendToken("config.json")
    println(txJson)
  }
}
```