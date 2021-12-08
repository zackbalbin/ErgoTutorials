# Simple Send

A basic script to send Ergo from a full node wallet to another wallet address using Ergo AppKit.

- Link to Youtube Video Once Created

### Tutorial

Import the libraries

```scala
package simplesend

import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.{ErgoNodeConfig, ErgoToolConfig}
```

Create object and functions

```scala
object SimpleSend {

    def sendTx(configFileName: String): String = {

    }

    def main(args: Array[String]): Unit = {

    }

}
```

Create main function logic
```scala
val txJson: String = sendTx("ergo_config.json")
println(txJson)
```

Final main function logic

```scala
def main(args: Array[String]): Unit = {
    val txJson: String = sendTx("ergo_config.json")
    println(txJson)
}
```


Now create the sendTx function logic

Create config variables
```scala
val config: ErgoToolConfig = ErgoToolConfig.load(configFileName)
val nodeConfig: ErgoNodeConfig = config.getNode()
val ergoClient: ErgoClient = RestApiErgoClient.create(nodeConfig)
```

Create variables based off of config file parameters
```scala
val addressIndex: Int = config.getParameters().get("addressIndex").toInt
val recieverWalletAddress: Address = Address.create(config.getParamters().get("reciverWalletAddress"))
```

Create txJson variable
```scala
val txJson: String = ergoClient.execute((ctx: BlockchainContext) => {

})
```


Create logic inside txJson varaible

Create the prover
```scala
val prover: ErgoProver = ctx.newProverBuilder()
    .withMnemonic(
        SecretString.create(nodeConfig.getWallet().getMnemonic()),
        SecretString.create(nodeConfig.getWallet().getPassword()))
    .withEip3Secret(addressIndex)
    .build()
```

Create the wallet
```scala
val wallet: ErgoWallet = ctx.getWallet()
```

Create variables for how much Erg to spend
```scala
val amountToSpend: Long = Parameters.OneErg
val totoalToSpend: Long = amountToSpend + Parameters.MinFee
```

Load the boxes to spend
```scala
val boxes: java.util.Optional[java.util.List[InputBox]] = wallet.getUnspentBoxes(totalToSpend)
if (!boxes.isPresent())
    throw new ErgoClientException(s"Not enough coins in the wallet to pay $totalToSpend", null)
```

Create the transation builder
```scala
val txBuilder = ctx.newTxBuilder()
```

Create new box to spend
```scala
val newBox = txBuilder.outBoxBuilder()
    .value(amountToSpend)
    .contract(ctx.compileContract(
        ConstantsBuilder.create()
            .item("recPk", reciverWalletAddress.getPubKey())
            .build(),
        "{ recPk "})
    )
    .build()
```

Create a new transaction from box
```scala
val tx: UnsignedTransaction: txBuilder
    .boxesToSpend(boxes.get)
    .outputs(newBox)
    .fee(Parameters.MinFee)
    .sendChangeTo(prover.getP2PKAddress())
    .build()
```

Sign the transaction
```scala
val signed: SignedTransaction = prover.sign(tx)
```

Send the transaction
```scala
val txId: String = ctx.sendTransaction(signed)
```

Convert the transaction information to JSON
```scala
signed.toJson(true)
```

The final txJson logic
```scala
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
```

After txJson logic brackets close, return txJson to complete the sendTx function
```scala
    })
    txJson
}
```

The final sendTx function logic
```scala
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
```

The final full script
```scala
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
```