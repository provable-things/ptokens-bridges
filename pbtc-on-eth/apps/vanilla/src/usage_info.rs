pub static USAGE_INFO: &'static str = "
❍ Provable pBTC Enclave ❍

    Copyright Provable 2019
    Questions: greg@oraclize.it

❍ Info ❍

This Provable pBTC app uses the pToken core in order to manage the cross-chain
conversions between pBTC & BTC tokens.

❍ Usage ❍

Usage:  pbtc [--help]
        pbtc getEnclaveState
        pbtc debugGetAllUtxos
        pbtc debugGetAllDbKeys
        pbtc debugClearAllUtxos
        pbtc getLatestBlockNumbers
        pbtc debugAddUtxoToDb <utxo>
        pbtc debugGetKeyFromDb <key>
        pbtc debugErc777ChangePNetwork <address>
        pbtc debugSetKeyInDbToValue <key> <value>
        pbtc debugErc777ProxyChangePNetwork <address>
        pbtc debugErc777ProxyChangePNetworkByProxy <address>
        pbtc submitEthBlock (<blockJson> | --file=<path>)
        pbtc submitBtcBlock (<blockJson> | --file=<path>)
        pbtc debugReprocessBtcBlock (<blockJson> | --file=<path>)
        pbtc debugReprocessEthBlock (<blockJson> | --file=<path>)
        pbtc initializeEth (<blocksJson> | --file=<path>) (<path>) [--chainId=<uint>] [--gasPrice=<uint>] [--confs=<uint>]
        pbtc initializeBtc (<blocksJson> | --file=<path>) [--network=<string>] [--difficulty=<uint>] [--fee=<uint>] [--confs=<uint>]

Commands:

    submitEthBlock      ❍ Submit an ETH block (& its receipts) to the enclave.
                          NOTE: The enclave must first have been initialized!

                        ➔ blockJson Format:
                        A valid JSON string of an object containing the fields:
                          `Block`    ➔ The block header itself.
                          `Receipts` ➔ An array containing the block's receipts.

    submitBtcBlock      ❍ Submit an BTC block & its transactions to the enclave.
                          The submission material must also include an array of
                          deposit information for `p2sh` addresses.
                          NOTE: The enclave must first have been initialized!

                        ➔ blockJson Format:
                        A valid JSON string of an object containing the fields:
                          `block`        ➔ The BTC block in JSON format.
                          `transactions` ➔ The transactions in HEX format.
                          `deposit_address_list` ➔ An array of objects:
                          {
                            `nonce`:
                                An integer nonce.
                            `eth_address`:
                                The destination ETH address in hex.
                            `btc_deposit_address`:
                                The `p2sh` BTC deposit address.
                            `eth_address_and_nonce_hash`:
                                The `sha256d` of `eth_address + nonce`
                          }

    initializeEth       ❍ Initialize the enclave with the first trusted ETH
                        block. Ensure the block has NO transactions relevant to
                        the pToken in it, because they'll be ignore by the
                        enclave. Transactions are not verified so you may omit
                        them and include an empty array in their place if needs
                        be. The enclave will initialize its ETH related database
                        from this trusted block, create the ETH private-key and
                        seal it plus any relevant settings from the `config`
                        into the database. This command will return a signed
                        transaction to broadcast, which transaction will deploy
                        the pToken contract to the ETH network.

                        ➔ blocksJson Format:
                        A valid JSON string of an object containing the fields:
                          `eth_block_and_receipts` ➔ The ETH block & receipts
                        NOTE: See `submitETHBlock` for breakdown of JSON.

    initializeBtc       ❍ Initialize the enclave with the first trusted BTC
                        block. Ensure the block has NO transactions relevant to
                        the pToken in it, because they'll be ignore by the
                        enclave. Transactions are not verified so you may omit
                        them and include an empty array in their place if needs
                        be. The enclave will initialize its BTC related database
                        from this trusted block, create the BTC private-key and
                        seal it plus any relevant settings from the `config`
                        into the database.

                        ➔ blocksJson Format:
                        A valid JSON string of an object containing the fields:
                          `btc_block_and_txs` ➔ The BTC block & transactions.
                        NOTE: See `submitBTCBlock` for breakdown of JSON.

    getEnclaveState     ❍ Returns the current state of the enclave as pulled
                          from the database.

    debugChangePnetwork  ❍ Make the core output a tx which when broadcast will
                           change the pNetwork address in the ERC777 contract.

    debugGetAllDbKeys    ❍ Returns JSON formatted report of all the database
                           keys used in the core.

    debugGetAllUtxos     ❍ Returns JSON formatted report of all the UTXOs
                          currently held in the DB. This function can only be
                          called if the `debug` flag is set.

    debugGetKeyFromDb    ❍ Get a given <key> from the database. This function
                           can only be called if the `debug` flag is set to
                           true when the tool was built.

    debugClearAllUtxos   ❍ Clear all the UTXOs set stored inside the database

    getLatestBlockNumbers ❍ Returns the current lastest ETH & BTC block numbers
                          seen by the enclave.

    debugAddUtxoToDb      ❍ Adds a UTXO to the UTXOs set

    debugReprocessBtcBlock ❍ Submit BTC block submisson material for re-processing.

    debugReprocessEthBlock ❍ Submit ETH block submisson material for re-processing.

    debugSetKeyInDbToValue  ❍ Set a given <key> in the database to a given
                              <value>. This function can only be called if the
                              `debug` flag is set to true when the core is
                              built. Note there there are zero checks on what
                              is passed in to the database: Use at own risk!

    <key>               ❍ A database key in HEX format.

    <value>             ❍ A database value in HEX format.

    <address>           ❍ A valid etheruem address.

    <blockJson>         ❍ Valid JSON string of ETH or BTC block.

    <path>              ❍ Path to the ETH smart-contract bytecode.

Options:

    --help               ❍ Show this message.

    --file=<path>        ❍ Path to file containg an ETH or BTC block JSON.

    --fee=<uint>         ❍ BTC fee as measured in Satoshis per byte.
                           [default: 23]

    --difficulty=<path>  ❍ The `difficulty` value above which a BTC block's
                           difficulty should be in order to be considered valid.
                           [default: 1337]

    --gasPrice=<uint>    ❍ The gas price to be used in ETH transactions.
                           [default: 20000000000]

    --confs=<uint>       ❍ The number of confirmations required before signing
                           transactions. This directly affects the length of
                           chain the light client maintains in the database.
                           [default: 0]

    --network=<string>   ❍ Desired BTC network:
                           Bitcoin = Bitcoin Main-Net (default)
                           Testnet  = Bitcoin public test-net
                           [default: Bitcoin]

    --chainId=<uint>     ❍ ID of desired chain for transaction:
                           1  = Ethereum Main-Net (default)
                           3  = Ropsten Test-Net
                           4  = Rinkeby Test-Net
                           42 = Kovan Test-Net
                           [default: 1]

    --nonce=<uint>       ❍ Transaction nonce

    --ethNetwork=<str>   ❍ Transaction network name
                            - mainnet
                            - ropsten
                            - rinkeby
                            - kovan

    --gasPrice=<uint>    ❍ Transaction gas price

    --recipient=<str>    ❍ Transaction eth address
";
