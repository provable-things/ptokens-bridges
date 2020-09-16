package io.ptokens.commands;

class CommandNames {

    /*
     * Function names
     */
    static final String GENERATE_PROOF = "generateProof";
    static final String GET_LATEST_BLOCK_NUMBERS = "getLatestBlockNumbers";
    static final String DEBUG_GET_ALL_DB_KEYS = "debugGetAllDbKeys";
    static final String GET_ENCLAVE_STATE = "getEnclaveState";
    static final String DEBUG_GET_ALL_UTXOS = "debugGetAllUtxos";
    static final String INITIALIZE_EOS = "initializeEos";
    static final String INITIALIZE_BTC = "initializeBtc";
    static final String INITIALIZE_ETH = "initializeEth";
    static final String SUBMIT_EOS_BLOCK = "submitEosBlock";
    static final String SUBMIT_ETH_BLOCK = "submitEthBlock";
    static final String SUBMIT_BTC_BLOCK = "submitBtcBlock";
    static final String DEBUG_ADD_NEW_EOS_SCHEDULE = "debugAddNewEosSchedule";
    static final String DEBUG_UPDATE_INCREMERKLE = "debugUpdateIncremerkle";
    static final String DEBUG_GET_KEY_FROM_DB = "debugGetKeyFromDb";
    static final String DEBUG_SET_KEY_IN_DB_TO_VALUE = "debugSetKeyInDbToValue";
    static final String DEBUG_CLEAR_ALL_UTXOS = "debugClearAllUtxos";
    static final String DEBUG_ADD_UTXO_TO_DB = "debugAddUtxoToDb";
    static final String DEBUG_REPROCESS_BTC_BLOCK = "debugReprocessBtcBlock";
    static final String DEBUG_REPROCESS_ETH_BLOCK = "debugReprocessEthBlock";
    static final String DEBUG_REPROCESS_EOS_BLOCK = "debugReprocessEosBlock";
    static final String DEBUG_MINT_PBTC = "debugMintTransaction";
    static final String ENABLE_EOS_PROTOCOL_FEATURE = "enableEosProtocolFeature";
    static final String DISABLE_EOS_PROTOCOL_FEATURE = "disableEosProtocolFeature";
    static final String SIGN_MESSAGE_WITH_ETH_KEY = "signMessageWithEthKey";

    static final String DEBUG_CHANGE_PNETWORK_TX = "debugErc777ChangePNetwork";
    static final String DEBUG_PROXY_CHANGE_PNETWORK_TX = "debugErc777ProxyChangePNetwork";
    static final String DEBUG_PROXY_CHANGE_PNETWORK_BY_PROXY_TX = "debugErc777ProxyChangePNetworkByProxy";

    /*
     * Default intent values
     */
    static final String DEFAULT_SYMBOL = "PBTC";
    static final String DEFAULT_PAYLOAD_NAME = "data.payload";
    static final String DEFAULT_ACCOUNT_NAME = "tbtcptokens1";
    static final String DEFAULT_EOS_CHAIN_ID = "2a02a0053e5a8cf73a56ba0fda11e4d92e0238a4a2aa74fccf46d5a910746840";
    static final String DEFAULT_ETH_CHAINID = "3";
    static final String DEFAULT_ETH_CONFS = "0";
    static final String DEFAULT_ETH_GAS_PRICE = "20000000000";

    static final String DEFAULT_BTC_FEE = "23";
    static final String DEFAULT_BTC_DIFFICULTY = "1337";
    static final String DEFAULT_BTC_NETWORK = "Testnet";
    static final String DEFAULT_BTC_CONFS = "0";

    /* 
     * Intent's parameters names
     */
    static final String INTENT_GAS_PRICE_NAME = "gasPrice";
    static final String INTENT_CHAIN_ID_NAME = "chainId";
    static final String INTENT_SYMBOL_NAME = "symbol";
    static final String INTENT_ACCOUNT_NAME = "accountName";

    static final String INTENT_FEE_NAME = "fee";
    static final String INTENT_DIFFICULTY_NAME = "difficulty";
    static final String INTENT_NETWORK_NAME = "network";
    static final String INTENT_CONFS_NAME = "confs";


    static final String INTENT_KEY = "key";
    static final String INTENT_VALUE = "value";

    static final String INTENT_PARAM = "param";
    static final String INTENT_ADDRESS = "address";

    static final String INTENT_MESSAGE = "message";

    static final String INTENT_AMOUNT_NAME = "amount";
    static final String INTENT_NONCE_NAME = "nonce";
    static final String INTENT_ETH_NETWORK_NAME = "ethNetwork";
    static final String INTENT_RECIPIENT_NAME = "recipient";
}
