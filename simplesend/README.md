# Simple Send

A basic script to send Ergo from a full node wallet to another wallet address using Ergo AppKit.

- Link to Youtube Video Once Created

### Tutorial

Disclaimer:
- Not finished yet
- Will be add more in depth descriptions about each code block soon!

**Step 1:** Import the necessary libraries

```scala
package simplesend

import org.ergoplatform.appkit._
import org.ergoplatform.appkit.config.{ErgoNodeConfig, ErgoToolConfig}
```

**Step 2:** Create the SimpleSend object with two functions inside of it

The first function is the sendTx function. This function will be where all of the code for actually creating
and sending the transaction will be written. The function takes in one parameter which is of type string called configFileName. The configFileName is the name of our config file so we can pass variables from our 
config file to our code. The function will return a string so that we can print out the information from the 
transaction.

The second function is the main function which will call the sendTx function.


```scala
object SimpleSend {

    def sendTx(configFileName: String): String = {

    }

    def main(args: Array[String]): Unit = {

    }

}
```

**Step 3:** Create main function logic

First create a variable called txJson of type string and set it equal to the sendTx function with the name of the config file passed into the paramter. After that print out the txJson. 

That is the entire main function now written. Pretty simple.
```scala
val txJson: String = sendTx("ergo_config.json")
println(txJson)
```

This is what the whole main function will look like.

```scala
def main(args: Array[String]): Unit = {
    val txJson: String = sendTx("ergo_config.json")
    println(txJson)
}
```


Now we will create the sendTx function code

**Step 4:** Create the configuration variables

The first variable we need to create is the config variable, which is of type ErgoToolConfig. We will set this equal to ErgoToolConfig.load(configFileName). What is does is create a new ErgoToolConfig object with the parameters that we set in our config file.

Second, we will create our nodeConfig object. This will be of type ErgoNodeConfig and we will set that equal to config.getNode(). This will get our node that we want to connect to. Our application will know how to connect to it because of the parmaters created in our config file which we passed to our config object.

Last we will create our Ergo client. We create a new variable called ergoClinet of type ErgoClient. Set that variable equal to RestApiErgoClient.create(nodeConfig). What all that does is create a new object of type RestApiErgoClient and passes in the nodeConfig object we created ealier.

With these three objects, we can interact with our node.

```scala
val config: ErgoToolConfig = ErgoToolConfig.load(configFileName)
val nodeConfig: ErgoNodeConfig = config.getNode()
val ergoClient: ErgoClient = RestApiErgoClient.create(nodeConfig)
```

**Step 5:** Create variables from data in our config file

The first variable to create of type Int and is called addressIndex. This variable will be set to the the "addressIndex" variable in our config file. We do this by setting our variable to config.getParameters().get("addressIndex").toInt.

The second variable we will create is our reciverWalletAddress. This is where we will send our funds to later in the script. The recieverWalletAddress is of type Address and we will set it equal to Address.create(). In the create() function we will pass in config.getParameters().get("recieverWalletAddress"). This code will turn the string we create in our config file into an address that we can use later.

```scala
val addressIndex: Int = config.getParameters().get("addressIndex").toInt
val recieverWalletAddress: Address = Address.create(config.getParamters().get("reciverWalletAddress"))
```

**Step 6:** Create txJson variable

The txJson variable is a string object that will store the output created by our ergoClient executing our logic. We will pass in a varaible of type BlockchainContext called ctx into the ergoClient.execute function. 

```scala
val txJson: String = ergoClient.execute((ctx: BlockchainContext) => {

})
```

Now we will create all of the logic in side the txJson object.

**Step 7:** Create the prover

We will now create a new varaible called prover which is of type ErgoProver. We do that by setting it equal to our BlockchainContext object, ctx, .newProverBuilder(). After, we need make sure we use withMnemonic and withEip3Secret. The withMnemonic takes in to parameters which we set equal to our mnemonic phrase and password set in our config file. The Eip3Secret takes in our addressIndex, which is set to 0.

Finally, we call the build function to finish the block of code.

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