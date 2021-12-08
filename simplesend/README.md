# Simple Send

A basic script to send Ergo from a full node wallet to another wallet address using Ergo AppKit.

- Link to Youtube Video Once Created

### Tutorial

Import the libraries

```
package simplesend

import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.{ErgoNodeConfig, ErgoToolConfig}
```

Create object and functions

```
object SimpleSend {

    def sendTx(configFileName: String): String = {

    }

    def main(args: Array[String]): Unit = {

    }

}
```

Create main function logic
```
val txJson: String = sendTx("ergo_config.json")
println(txJson)
```

Final main function logic

```
def main(args: Array[String]): Unit = {
    val txJson: String = sendTx("ergo_config.json")
    println(txJson)
}
```


Now create the sendTx function logic

Create config variables
```
val config: ErgoToolConfig = ErgoToolConfig.load(configFileName)
val nodeConfig: ErgoNodeConfig = config.getNode()
val ergoClient: ErgoClient = RestApiErgoClient.create(nodeConfig)
```

Create variables based off of config file parameters
```
val addressIndex: Int = config.getParameters().get("addressIndex").toInt
val recieverWalletAddress: Address = Address.create(config.getParamters().get("reciverWalletAddress"))
```

Create txJson variable
```
val txJson: String = ergoClient.execute((ctx: BlockchainContext) => {

})
```


Create logic inside txJson varaible

Create the prover
```
val prover: ErgoProver = ctx.newProverBuilder()
    .withMnemonic(
        SecretString.create(nodeConfig.getWallet().getMnemonic()),
        SecretString.create(nodeConfig.getWallet().getPassword()))
    .withEip3Secret(addressIndex)
    .build()
```

Create the wallet
```
val wallet: ErgoWallet = ctx.getWallet()
```

Create variables for how much Erg to spend
```
val amountToSpend: Long = Parameters.OneErg
val totoalToSpend: Long = amountToSpend + Parameters.MinFee
```

Load the boxes to spend
```
val boxes: java.util.Optional[java.util.List[InputBox]] = wallet.getUnspentBoxes(totalToSpend)
if (!boxes.isPresent())
    throw new ErgoClientException(s"Not enough coins in the wallet to pay $totalToSpend", null)
```

Create the transation builder
```
val txBuilder = ctx.newTxBuilder()
```

Create new box to spend
```
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
```
val tx: UnsignedTransaction: txBuilder
    .boxesToSpend(boxes.get)
    .outputs(newBox)
    .fee(Parameters.MinFee)
    .sendChangeTo(prover.getP2PKAddress())
    .build()
```

Sign the transaction
```
val signed: SignedTransaction = prover.sign(tx)
```

Send the transaction
```
val txId: String = ctx.sendTransaction(signed)
```

Convert the transaction information to JSON
```
signed.toJson(true)
```

The final txJson logic
```
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
```
    })
    txJson
}
```

The final sendTx function logic
```
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
```
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