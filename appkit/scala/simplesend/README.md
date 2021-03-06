# Simple Send

A basic script to send Ergo from a full node wallet to another wallet address using Ergo AppKit.


- Link to Youtube Video Once Created

### Tutorial

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
val txJson: String = sendTx("config.json")
println(txJson)
```

This is what the whole main function will look like.

```scala
def main(args: Array[String]): Unit = {
    val txJson: String = sendTx("config.json")
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

The variable we will create is our reciverWalletAddress. This is where we will send our funds to later in the script. The recieverWalletAddress is of type Address and we will set it equal to Address.create(). In the create() function we will pass in config.getParameters().get("recieverWalletAddress"). This code will turn the string we create in our config file into an address that we can use later.

```scala
val recieverWalletAddress: Address = Address.create(config.getParamters().get("reciverWalletAddress"))
```

**Step 6:** Create txJson variable

The txJson variable is a string object that will store the output created by our ergoClient executing our logic. We will pass in a varaible of type BlockchainContext called ctx into the ergoClient.execute function. 

```scala
val txJson: String = ergoClient.execute((ctx: BlockchainContext) => {

})
```

Now we will create all of the logic in side the txJson object.

**Step 7** Create Mnemonic

The mnemonic will be used to create our prover and get our EIP3 Address in later steps. In order to do this, we need two variables: walletMnemonic and walletPassword. Both of type string. We get these values by calling the nodeConfig functions get().getMnemonic() and getWallet().getPassword() of the nodeConfig. Finally, we can create the mnemonic. This is of type Mnemonic. We do this by createing a new Mnemonic object and pass in our two strings converted to character arrays.

```scala
val walletMnemonic: String = nodeConfig.getWallet().getMnemonic()
val walletPassword: String = nodeConfig.getWallet().getPassword()
val mnemonic: Mnemonic = Mnemonic.create(walletMnemonic.toCharArray(), walletPassword.toCharArray())

```

**Step 8:** Create the prover

We will now create a new varaible called prover which is of type ErgoProver. We do that by setting it equal to our BlockchainContext object, ctx, .newProverBuilder(). After, we need make sure we use withMnemonic and withEip3Secret. The withMnemonic takes in to parameters which we set equal to our mnemonic objects we created in step 7. The Eip3Secret takes in our addressIndex, which is set to 0.

Finally, we call the build function to finish the block of code.

```scala
val senderProver: ErgoProver = ctx.newProverBuilder()
  .withMnemonic(
    SecretString.create(walletMnemonic),
    SecretString.create(walletPassword))
  .withEip3Secret(0)
  .build()
```

**Step 9:** Get our wallet address

Now we will get our EIP3 address. We will create a new object of type Address and call the createEip3Address function. We need to pass in the address index, which is 0, and the NetworkType, which in this case is TESTNET. After that, we can pass in our walletMnemonic and walletPassword object as character arrays and converted to a SecretString.

```scala
val senderAddress: Address = Address.createEip3Address(0, NetworkType.TESTNET, SecretString.create(walletMnemonic.toCharArray()), SecretString.create(walletPassword.toCharArray()))
```

**Step 10:** Create varaibles to for how much Ergo to spend

The first variable is amountToSpend which is of type Long. The appkit library allows use Parameters.OneErg to send one Erg. If we wanted to send a custom amount, we would need to pass in a Long. Parameters.OneErg passes in 1000000000L into our variable.

The second varaible is also of type Long and is called totalToSpend. This variable will be the sum of our amountToSpend plus a fee. We set our fee to Parameters.Min fee. The MinFee is 1000000L or 0.001 Ergs. 

In this case our totalToSpend is 1.001 Ergs.

```scala
val amountToSpend: Long = Parameters.OneErg
val totalToSpend: Long = amountToSpend + Parameters.MinFee
```

**Step 11:** Load the boxes to spend

Now we will start putting together everything we need to create our transaction. First we need to get our UTXOs that we can spend. Remember, a UTXO on Ergo is called a box.

First we need to get a list of unspentboxes. Our unspentBoxes variable will be an of a List of InputBox. We get this list by calling our wallet's getUnspentBoxesFor function and passing in our senderAddress, 0 for the offset and 20 for the limit.

Then we can get the boxes we will use by creating a new variable, boxes. We get these boxes by calling the BoxOperations .selectTop function and passing in our unspentBoxes and our totalToSpend.

After we load our boxes we need to test if we have enough funds to spend. We do this by using the .isEmpty() function. If there are not enough boxes we will throw a new ErgoClientExecption with the description of the problem that occured.

```scala
val unspentBoxes = ctx.getUnspentBoxesFor(senderAddress, 0, 20)
val boxes = BoxOperations.selectTop(unspentBoxes, totalToSpend)
if (boxes.isEmpty())
  throw new ErgoClientException(s"Not enough coins in the walelt to pay $totalToSpend", null)
```

**Step 12:** Create the transation builder

The transaction builder is an appkit object that we can use to help craft our transactions. We create it by calling our blockchain context variable's, ctx, .newTxBuilder() function.

```scala
val txBuilder = ctx.newTxBuilder()
```

**Step 13:** Create new box to spend

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

**Step 14:** Create a new transaction from box

Now we need to create our transaction. First, we will create a variable called tx of type UnsignedTransaction. We will use the txBuilder again to create our transaction. Just like when we built the box, we will have multiple functions we call in order to build our transaction.

The first one is .boxesToSpend() and we pass in our boxes' .get() function. This is pass our List of InputBoxes to our txBuilder in order to creat our transaction.

The second function to call is the .outputs() function and we pass in the newBox. Our new box will be the output of the transaction that we want to send.

Our fee will be Parameters.MinFee and we will send our change to our own node wallet address. We get this by calling our prover's .getP2PKAddress(), which will return our wallet address.

Finally we build our transaction by calling .build().

```scala
val tx: UnsignedTransaction: txBuilder
    .boxesToSpend(boxes.get)
    .outputs(newBox)
    .fee(Parameters.MinFee)
    .sendChangeTo(prover.getP2PKAddress())
    .build()
```

All of the code is pretty much written at this point. Now we just have to sign and send our transaction

**Step 15:** Sign the transaction

We will create a new variable of type SignedTransaction called signed: The prover has a built in function that we can use to sign transcations. All we need to do is call it and pass in our transaction that we want to sign.

```scala
val signed: SignedTransaction = prover.sign(tx)
```

**Step 16:** Send the transaction

Now we can actaully send our transaction! All we have to do is call our blockchain context's .sendTransaction() function and pass in a SignedTransaction. In this example we create a new variable of type String called txId which will the information about our transaction so we can view it on the block explorer.

```scala
val txId: String = ctx.sendTransaction(signed)
```

The transaction is now sent on the blockchain!

**Step 17:** Convert the transaction information to JSON

By converting the transaction information to JSON it becomes much easier for us to read. We do this by calling our signed variable's function called .toJson() and passing the boolean true.

```scala
signed.toJson(true)
```

Your complete txJson string object should look like this.

```scala
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

  val amountToSpend: Long = Parameters.MinChangeValue
  val totalToSpend: Long = amountToSpend + Parameters.MinFee

  val unspentBoxes: java.util.List[InputBox] = ctx.getUnspentBoxesFor(senderAddress, 0, 20)
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
```

**Step 18:** Return the txJson string

Recall that our sendTx function reuturns a string. In order to satisfy this, we will return our txJson. The txJson is the information about our transaction in JSON form. We created this information in the code **signed.toJson(true).**

```scala
    })
    txJson
}
```

Now the sendTx() function is complete and should look like this.

```scala
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

    val amountToSpend: Long = Parameters.MinChangeValue
    val totalToSpend: Long = amountToSpend + Parameters.MinFee

    val unspentBoxes: java.util.List[InputBox] = ctx.getUnspentBoxesFor(senderAddress, 0, 20)
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
```

Thats all of the code now written. The final script will look like this!

```scala
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

      val amountToSpend: Long = Parameters.MinChangeValue
      val totalToSpend: Long = amountToSpend + Parameters.MinFee

      val unspentBoxes: java.util.List[InputBox] = ctx.getUnspentBoxesFor(senderAddress, 0, 20)
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
    val txJson: String = sendTx("config.json")
    println(txJson)
  }
}
```