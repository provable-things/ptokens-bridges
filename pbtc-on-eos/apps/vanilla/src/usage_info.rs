pub static USAGE_INFO: &'static str = "
❍ Provable pBTC-on-EOS Core ❍

    Copyright Provable 2020
    Questions: greg@oraclize.it

❍ Info ❍

This Provable pBTC-on-EOS app uses the pToken core in order to manage the cross-chain conversions between pBTC & BTC tokens on the EOS blockchain.

❍ Usage ❍

Usage:  pbtc_on_eos [--help]
        pbtc_on_eos getEnclaveState
        pbtc_on_eos debugGetAllUtxos
        pbtc_on_eos debugGetAllDbKeys
        pbtc_on_eos getLatestBlockNumbers
        pbtc_on_eos debugGetKeyFromDb <key>
        pbtc_on_eos debugSetKeyInDbToValue <key> <value>
        pbtc_on_eos enableEosProtocolFeature <featureHash>
        pbtc_on_eos disableEosProtocolFeature <featureHash>
        pbtc_on_eos submitEosBlock (<blockJson> | --file=<path>)
        pbtc_on_eos submitBtcBlock (<blockJson> | --file=<path>)
        pbtc_on_eos debugUpdateIncremerkle (<eosJson> | --file=<path>)
        pbtc_on_eos debugAddEosSchedule (<scheduleJson> | --file=<path>)
        pbtc_on_eos debugReprocessBtcBlock (<blocksJson> | --file=<path>)
        pbtc_on_eos initializeEos [--accountName=<string>] [--symbol=<string>] [--chainId=<hex>] (<eosJson> | --file=<path>)
        pbtc_on_eos initializeBtc (<blocksJson> | --file=<path>) [--network=<string>] [--difficulty=<uint>] [--fee=<uint>] [--confs=<uint>]

Commands:

    submitEosBlock      ❍ Submit an EOS block (& its receipts) to the enclave.
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
                            `eos_address`:
                                The destination EOS address in hex.
                            `btc_deposit_address`:
                                The `p2sh` BTC deposit address.
                            `eos_address_and_nonce_hash`:
                                The `sha256d` of `eos_address + nonce`
                          }

    initializeEos     ❍ Initialize the enclave with the first trusted EOS
                        block. Ensure the block has NO transactions relevant to
                        the pToken in it, because they'll be ignore by the
                        enclave. Transactions are not verified so you may omit
                        them and include an empty array in their place if needs
                        be. The enclave will initialize its EOS related database
                        from this trusted block, create the EOS private-key and
                        seal it plus any relevant settings from the `config`
                        into the database. This command will return a signed
                        transaction to broadcast, which transaction will deploy
                        the pToken contract to the EOS network.

                        ➔ eosJson Format:
                          {
                            `block`:
                                An EOS block.
                            `active_schedule`:
                                The active schedule for the above block.
                            `blockroot_merkle`:
                                The blockroot-merkles for the above block.
                          }

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

    debugAddEosSchedule ❍ Add an EOS schedule to the database.

    debugGetAllDbKeys   ❍ Returns JSON formatted report of all the database
                          keys used in the core.

    debugGetAllUtxos    ❍ Returns JSON formatted report of all the UTXOs
                          currently held in the DB. This function can only be
                          called if the `debug` flag is set.

    debugGetKeyFromDb    ❍ Get a given <key> from the database. This function
                           can only be called if the `debug` flag is set to
                           true when the tool was built.

    getLatestBlockNumbers ❍ Returns the current lastest EOS & BTC block numbers
                          seen by the enclave.

    enableEosProtocolFeature  ❍ Enable an EOS protocol feature in the core.

    disableEosProtocolFeature ❍ Disable an EOS protocol feature in the core.

    debugSetKeyInDbToValue  ❍ Set a given <key> in the database to a given
                              <value>. This function can only be called if the
                              `debug` flag is set to true when the core is
                              built. Note there there are zero checks on what
                              is passed in to the database: Use at own risk!

    debugUpdateIncremerkle  ❍ Use a trusted block header, blockroot_merkle and
                              blockroot_merkle to udpate the EOS incremerkle in
                              the database, thus effectively moving the chain
                              forward to the submittied block's height.

                              ➔ eosJson Format:
                                {
                                  `block`:
                                      An EOS block.
                                  `active_schedule`:
                                      The active schedule for the above block.
                                  `blockroot_merkle`:
                                      The blockroot-merkles for the above block.
                                }

    debugReprocessBtcBlock ❍ Re-process a BTC block without updating any chain
                             data in the database.

    <key>               ❍ A database key in HEX format.

    <value>             ❍ A database value in HEX format.

    <blockJson>         ❍ Valid JSON string of EOS or BTC block.

    <featureHash>       ❍ A hash as a hex string of an EOS protocol feature.

    <eosJson>           ❍ Valid JSON string of an object with the fields:
                          {
                            `block`:
                                An EOS block.
                            `active_schedule`:
                                The active schedule for the above block.
                            `blockroot_merkle`:
                                The blockroot-merkles for the above block.
                          }

Options:

    --help               ❍ Show this message.

    --file=<path>        ❍ Path to file containing a JSON relevant to the chosen
                           command.

    --fee=<uint>         ❍ BTC fee as measured in Satoshis per byte.
                           [default: 23]

    --difficulty=<path>  ❍ The `difficulty` value above which a BTC block's
                           difficulty should be in order to be considered valid.
                           [default: 1337]

    --confs=<uint>       ❍ The number of confirmations required before signing
                           transactions. This directly affects the length of
                           chain the light client maintains in the database.
                           [default: 0]

    --network=<string>   ❍ Desired BTC network:
                           Bitcoin = Bitcoin Main-Net (default)
                           Testnet  = Bitcoin public test-net
                           [default: Bitcoin]


    --chainId=<hex>      ❍ Hex string of the EOS chain ID.

    --symbol=<string>    ❍ The symbol of the token.
                           [default: PBTC]

    --accountName=<string>  ❍ Account name of the authorized user of the EOS
                              smart contract.
                              [default: pbtctokenxxx]

";
