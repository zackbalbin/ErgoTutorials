# Mint Token

A simple script to mint a transaction and send it to an address that the user specified.

### Tutorial

Since this is the second tutorial I am going to skip over the basic setup of the application. If you want to see how its done, check out the fist tutorial: [Simple Send](https://github.com/zackbalbin/ErgoTutorials/tree/master/appkit/scala/simplesend). 

**Step 1:** Setup File

We are going to import the libraries and create the two functions we need. The main function code will also be written during this step.

```scala
package minttoken

import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.{ErgoNodeConfig, ErgoToolConfig}

object MintToken {

  def mintToken(configFileName: String): String = {
    
  }

  def main(args: Array[String]): Unit = {
    val txJson: String = mintToken("ergo_config.json")
    println(txJson)
  }
}
```

**Step 2:** Set up basics of mintToken() function

Just like in the first tutorial we need our configuration tools in order to interact with our node and subsequently the Ergo blockchain. We also create our variables from that we set in our config file here.


```scala
val config: ErgoToolConfig = ErgoToolConfig.load(configFileName)
val nodeConfig: ErgoNodeConfig = config.getNode()
val ergoClient: ErgoClient = RestApiErgoClient.create(nodeConfig)

val addressIndex: Int = config.getParameters().get("addressIndex").toInt
val tokenName: String = config.getParameters().get("tokenName")
val tokenDescription: String = config.getParameters().get("tokenDescription")
val tokenAmount: Long = config.getParameters().get("tokenAmount").toLong
val tokenDecimals: Int = config.getParameters().get("tokenDecimals").toInt
val recieverWalletAddress: Address = Address.create(config.getParameters().get("recieverWalletAddress"))
```

**Step 3:** Create txJson object with the amount of Ergs we want to send with the token

todo

```scala
val txJson: String = ergoClient.execute((ctx: BlockchainContext) => {
    val amountToSpend: Long = (Parameters.OneErg / 10)
    val totalToSpend: Long = amountToSpend + Parameters.MinFee
})
```

**Step 4:** Create our mnemonic key

Now we will create our mnemonic key using the variables we created in our config file. 

```scala
val mnemonicString = nodeConfig.getWallet().getMnemonic()
val mnemonicPasswordString = nodeConfig.getWallet().getMnemonicPassword()
val mnemonic = Mnemonic.create(mnemonicString.toCharArray(), mnemonicPasswordString.toCharArray())
```

**Step 5:** Create our prover

In this step we are going to create our prover in a different way. We will use the BoxOperations.createProver() function and pass in our blockchain context along with our mnemonic object into the parameters.

After creating our prover, we obtain the address of the sender by calling senderProver.getAddress() and storing it in a variable called sender.

```scala
val senderProver = BoxOperations.createProver(ctx, mnemonic)
val sender = senderProver.getAddress()
```

**Step 6:** Get UTXOs to spend

We are going to find the UTXOs that we can spend by calling .getUnspentBoxesFor() and passing in our sender variable. This finds all the boxes for the address we loaded with our mnemonic object.

We then store those boxes in a variable called boxesToSpend and find the boxes with enough funds to satisfy our totalToSpend variable.

```scala
val unspent = ctx.getUnspentBoxesFor(sender)
val boxesToSpend = BoxOperations.selectTop(unspent, totalToSpend)
```

**Step 7:** Create our new token

Here we will actually create our new token. We pass in two parameters: the id of the first box in our boxesToSpend list and the amount of tokens we want to mint.

```scala
val token = new ErgoToken(boxesToSpend.get(0).getId(), tokenAmount)
```

**Step 8:** Create our txBuilder

Now we need to create our txBuilder just like in the SimpleSend tutorial.

```scala
val txBuilder = ctx.newTxBuilder()
```

**Step 9:** Create newBox to spend

Now we create our newBox. This box will create a new token(s) and send them along with a user specified amount of erg to a wallet. The wallet is set in the config file. This box is the same box we made in the SimpleSend tutorial with one minor change. There is a new line that calls .mintToken().

The .mintToken() function takes in 4 parameters. The first is the token we made in step 7. The next 3 parameters are the variables set equal to their parts of the config file. We made these variables earlier in the tutorial. We pass all these parameters into the .mintToken() function in order to create the token(s).

We also will use a premade contract instead of making our own logic. The new contract line will send the tokens in the box to our specified address.

```scala
val newBox = txBuilder.outBoxBuilder()
    .value(amountToSpend)
    .mintToken(token, tokenName, tokenDescription, tokenDecimals)
    .contract(ErgoContracts.sendToPK(ctx, recieverWalletAddress))
    .build()
```

**Step 10:** Write rest of mintToken() function

The rest of our code is the same as in the previous tutorial. Here we are creating a UnsignedTransaction, signing it, and then sending the transaction on the Ergo blockchain. After that, we put the transaction information into a string, convert it to JSON, and return it so it can be called by our main function.

```scala
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
```

This is the full txJson object.

```scala
val txJson: String = ergoClient.execute((ctx: BlockchainContext) => {
    val amountToSpend: Long = (Parameters.OneErg / 10)
    val totalToSpend: Long = amountToSpend + Parameters.MinFee

    val mnemonicString = nodeConfig.getWallet().getMnemonic()
    val mnemonicPasswordString = nodeConfig.getWallet().getMnemonicPassword()
    val mnemonic = Mnemonic.create(mnemonicString.toCharArray(), mnemonicPasswordString.toCharArray())

    val senderProver = BoxOperations.createProver(ctx, mnemonic)
    val sender = senderProver.getAddress()
    val unspent = ctx.getUnspentBoxesFor(sender)
    val boxesToSpend = BoxOperations.selectTop(unspent, totalToSpend)

    val token = new ErgoToken(boxesToSpend.get(0).getId(), tokenAmount)

    val txBuilder = ctx.newTxBuilder()
    
    val newBox = txBuilder.outBoxBuilder()
    .value(amountToSpend)
    .mintToken(token, tokenName, tokenDescription, tokenDecimals)
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
```

Our entire mintToken() function should look like this.

```scala
def mintToken(configFileName: String): String = {
    val config: ErgoToolConfig = ErgoToolConfig.load(configFileName)
    val nodeConfig: ErgoNodeConfig = config.getNode()
    val ergoClient: ErgoClient = RestApiErgoClient.create(nodeConfig)

    val addressIndex: Int = config.getParameters().get("addressIndex").toInt
    val tokenName: String = config.getParameters().get("tokenName")
    val tokenDescription: String = config.getParameters().get("tokenDescription")
    val tokenAmount: Long = config.getParameters().get("tokenAmount").toLong
    val tokenDecimals: Int = config.getParameters().get("tokenDecimals").toInt
    val recieverWalletAddress: Address = Address.create(config.getParameters().get("recieverWalletAddress"))

    val txJson: String = ergoClient.execute((ctx: BlockchainContext) => {
        val amountToSpend: Long = (Parameters.OneErg / 10)
        val totalToSpend: Long = amountToSpend + Parameters.MinFee

        val mnemonicString = nodeConfig.getWallet().getMnemonic()
        val mnemonicPasswordString = nodeConfig.getWallet().getMnemonicPassword()
        val mnemonic = Mnemonic.create(mnemonicString.toCharArray(), mnemonicPasswordString.toCharArray())

        val senderProver = BoxOperations.createProver(ctx, mnemonic)
        val sender = senderProver.getAddress()
        val unspent = ctx.getUnspentBoxesFor(sender)
        val boxesToSpend = BoxOperations.selectTop(unspent, totalToSpend)

        val token = new ErgoToken(boxesToSpend.get(0).getId(), tokenAmount)

        val txBuilder = ctx.newTxBuilder()
        
        val newBox = txBuilder.outBoxBuilder()
        .value(amountToSpend)
        .mintToken(token, tokenName, tokenDescription, tokenDecimals)
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
```

And finally the entire application is below.

```scala
package minttoken

import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.{ErgoNodeConfig, ErgoToolConfig}

object MintToken {

  def mintToken(configFileName: String): String = {
    val config: ErgoToolConfig = ErgoToolConfig.load(configFileName)
    val nodeConfig: ErgoNodeConfig = config.getNode()
    val ergoClient: ErgoClient = RestApiErgoClient.create(nodeConfig)

    val addressIndex: Int = config.getParameters().get("addressIndex").toInt
    val tokenName: String = config.getParameters().get("tokenName")
    val tokenDescription: String = config.getParameters().get("tokenDescription")
    val tokenAmount: Long = config.getParameters().get("tokenAmount").toLong
    val tokenDecimals: Int = config.getParameters().get("tokenDecimals").toInt
    val recieverWalletAddress: Address = Address.create(config.getParameters().get("recieverWalletAddress"))
    
    val txJson: String = ergoClient.execute((ctx: BlockchainContext) => {
      val amountToSpend: Long = (Parameters.OneErg / 10)
      val totalToSpend: Long = amountToSpend + Parameters.MinFee

      val mnemonicString = nodeConfig.getWallet().getMnemonic()
      val mnemonicPasswordString = nodeConfig.getWallet().getMnemonicPassword()
      val mnemonic = Mnemonic.create(mnemonicString.toCharArray(), mnemonicPasswordString.toCharArray())

      val senderProver = BoxOperations.createProver(ctx, mnemonic)
      val sender = senderProver.getAddress()
      val unspent = ctx.getUnspentBoxesFor(sender)
      val boxesToSpend = BoxOperations.selectTop(unspent, totalToSpend)

      val token = new ErgoToken(boxesToSpend.get(0).getId(), tokenAmount)

      val txBuilder = ctx.newTxBuilder()
      
      val newBox = txBuilder.outBoxBuilder()
        .value(amountToSpend)
        .mintToken(token, tokenName, tokenDescription, tokenDecimals)
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
    val txJson: String = mintToken("ergo_config.json")
    println(txJson)
  }
}

```