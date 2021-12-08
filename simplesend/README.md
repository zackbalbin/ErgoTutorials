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

Create object

```
object SimpleSend {

}
```

Create the sendTx function and main funtion and put in SimpleSend object

```
def sendTx(configFileName: String): String = {

}

def main(args: Array[String]): Unit = {

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