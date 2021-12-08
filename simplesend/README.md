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

**Step 8:** Create the wallet

Now we will create our wallet object. Our created variable wallet is of type ErgoWallet and set equal to the blockchain context .getWallet() function. This one line of code will execute all the necessary steps to access the needed wallet in order to spend UTXOs.

```scala
val wallet: ErgoWallet = ctx.getWallet()
```

**Step 9:** Create varaibles to for how much Ergo to spend

The first variable is amountToSpend which is of type Long. The appkit library allows use Parameters.OneErg to send one Erg. If we wanted to send a custom amount, we would need to pass in a Long. Parameters.OneErg passes in 1000000000L into our variable.

The second varaible is also of type Long and is called totalToSpend. This variable will be the sum of our amountToSpend plus a fee. We set our fee to Parameters.Min fee. The MinFee is 1000000L or 0.001 Ergs. 

In this case our totalToSpend is 1.001 Ergs.

```scala
val amountToSpend: Long = Parameters.OneErg
val totalToSpend: Long = amountToSpend + Parameters.MinFee
```

**Step 10:** Load the boxes to spend

Now we will start putting together everything we need to create our transaction. First we need to get our UTXOs that we can spend. Remember, a UTXO on Ergo is called a box.

First we will create our boxes varaible. Our boxes variable will be an Optional of a List of InputBox. This means that we can either have a value of null or a List of (InputBox)s. We get this list by calling our wallet's getUnspentBoxes function and passing in our totalToSpend variable.

After we load our boxes we need to test if we have enough funds to spend. We do this by using the .isPresent() function. If there are not enough boxes we will throw a new ErgoClientExecption with the description of the problem that occured.

```scala
val boxes: java.util.Optional[java.util.List[InputBox]] = wallet.getUnspentBoxes(totalToSpend)
if (!boxes.isPresent())
    throw new ErgoClientException(s"Not enough coins in the wallet to pay $totalToSpend", null)
```

**Step 11:** Create the transation builder

The transaction builder is an appkit object that we can use to help craft our transactions. We create it by calling our blockchain context variable's, ctx, .newTxBuilder() function.

```scala
val txBuilder = ctx.newTxBuilder()
```

**Step 12:** Create new box to spend

This step is the most important step in our whole script. Here is where we create our box that we are going to spend.

We need to create a new variable and set it equal to our txBuilder's .outBxBuilder() function. We then will pass different data into this new box by calling a few other functions.

First, we call the .value function and pass in our amountToSpend variable that we create earlier. This will create a box with the necessary amount of erg needed.

Next we create our contract by calling the .contract() function. We will pass in the blockchain context's .compileContract() function with an item called "recPk". The "recPk" will be set to our recieverWalletAddress's public key so we can send our funds to the correct wallet. Finally we build our contract by calling .build().

Then at the end of the whole box we call the .build() function again to build our box. Now the box is created.

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

This is what the full and final script will look like!

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