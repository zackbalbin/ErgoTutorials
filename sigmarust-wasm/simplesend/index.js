import { Wallet, Address, BlockHeaders, BoxId, BoxValue, Contract, DataInputs, DerivationPath, ErgoBoxCandidate, ErgoBoxCandidateBuilder, ErgoBoxCandidates, ErgoStateContext, ExtSecretKey, I64, Mnemonic, NetworkAddress, NetworkPrefix, PreHeader, SecretKey, SecretKeys, TxBuilder, UnsignedInput, UnsignedInputs, UnsignedTransaction, ErgoBoxes } from 'ergo-lib-wasm-nodejs/ergo_lib_wasm.js';
import { key } from './testnet-keys';

function createTransaction(recipientAddress, changeAddress, transferAmount, feeAmount, changeAmount, inputIds, currentHeight) {
    let payTo = new ErgoBoxCandidateBuilder(
        BoxValue.from_i64(I64.from_str(transferAmount)),
        Contract.pay_to_address(recipientAddress),
        currentHeight
    ).build();

    let change = new ErgoBoxCandidateBuilder(
        changeAmount,
        Contract.pay_to_address(changeAddress),
        currentHeight
    ).build();

    let fee = ErgoBoxCandidate.new_miner_fee_box(feeAmount, currentHeight);

    let unsignedInputArray = inputIds.map(BoxId.from_str).map(UnsignedInput.from_box_id);
    let unsignedInputs = new UnsignedInputs();
    unsignedInputArray.forEach(element => {
        unsignedInputs.add(element);
    });

    let outputs = new ErgoBoxCandidates(payTo);
    
    if (change.value().as_i64().as_num() > 0) {
        outputs.add(change);
    }

    outputs.add(fee);

    return new UnsignedTransaction(unsignedInputs, new DataInputs(), outputs);
}

async function sendTransaction() {
    let recieverAddress = Address.from_testnet_str("3WycHxEz8ExeEWpUBwvu1FKrpY8YQCiH1S9PfnAvBX1K73BXBXZa");

    let seed = Mnemonic.to_seed(
        testnetKey,
        ""
    );

    let extendedSecretKey = ExtSecretKey.derive_master(seed);
    let changePath = DerivationPath.from_string("m/44'/429'/0'/0/0");
    let changeSk = extendedSecretKey.derive(changePath);

    let baseAddress = changeSk.public_key().to_address();
    let myAddress = NetworkAddress.new(NetworkPrefix.Testnet, baseAddress);

    let transferAmount = "250000000";
    let feeAmount = TxBuilder.SUGGESTED_TX_FEE();
    let changeAmount = BoxValue.SAFE_USER_MIN();

    let myInputs = [
        "",
        "",
    ];

    let currentHeight = 173780;

    let unsignedTransaction = createTransaction(
        recieverAddress,
        myAddress.address(),
        transferAmount,
        feeAmount,
        changeAmount,
        myInputs,
        currentHeight
    );

    let blockContext = "";
    let inputBoxesJson = "";

    let blockHeaders = BlockHeaders.from_json(blockContext);
    let preHeader = PreHeader.from_block_header(blockHeaders.get(0));
    let stateContext = new ErgoStateContext(preHeader, blockHeaders);

    let dLogSecret = SecretKey.dlog_from_bytes(changeSk.secret_key_bytes());
    let secretKeys = new SecretKeys();
    secretKeys.add(dLogSecret);

    let wallet = Wallet.from_secrets(secretKeys);
    let inputBoxes = ErgoBoxes.from_boxes_json(inputBoxesJson);
    let dataInputs = ErgoBoxes.empty();

    let signedTransaction = wallet.sign_transaction(stateContext, unsignedTransaction, inputBoxes, dataInputs);

    console.log(signedTransaction.to_json());
}

sendTransaction();