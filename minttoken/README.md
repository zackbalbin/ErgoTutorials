# Mint Token

Script currently works

Still need to
- Optimize code
- Update config file paramters
- FInish text based tutorial (like simple send)

### Tutorial

Since this is the second tutorial I am going to skip over the basic setup of the application. If you want to see how its done, check out the fist tutorial: [Simple Send](https://github.com/zackbalbin/ErgoTutorials/tree/master/simplesend). 

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

todo

```scala
val senderProver = BoxOperations.createProver(ctx, mnemonic)
val sender = senderProver.getAddress()
```

**Step 6:** Get UTXOs to spend

todo 

```scala
val unspent = ctx.getUnspentBoxesFor(sender)
val boxesToSpend = BoxOperations.selectTop(unspent, totalToSpend)
```

**Step 7:** Create our new token

todo

```scala
val token = new ErgoToken(boxesToSpend.get(0).getId(), tokenAmount)
```

**Step 8:** Create our txBuilder

todo

```scala
val txBuilder = ctx.newTxBuilder()
```

**Step 9:** Create newBox to spend

todo

```scala
val newBox = txBuilder.outBoxBuilder()
    .value(amountToSpend)
    .mintToken(token, tokenName, tokenDescription, tokenDecimals)
    .contract(ctx.compileContract(
        ConstantsBuilder.create()
        .item("recPk", recieverWalletAddress.getPublicKey())
        .build(),
        "{ recPk }")
    )
    .build()
```

**Step 10:** Write rest of mintToken() function

todo

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

Full txJson object

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
    .contract(ctx.compileContract(
        ConstantsBuilder.create()
        .item("recPk", recieverWalletAddress.getPublicKey())
        .build(),
        "{ recPk }")
    )
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

Full mintToken() function

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
        .contract(ctx.compileContract(
            ConstantsBuilder.create()
            .item("recPk", recieverWalletAddress.getPublicKey())
            .build(),
            "{ recPk }")
        )
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

Full Application

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
        .contract(ctx.compileContract(
          ConstantsBuilder.create()
            .item("recPk", recieverWalletAddress.getPublicKey())
            .build(),
          "{ recPk }")
        )
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