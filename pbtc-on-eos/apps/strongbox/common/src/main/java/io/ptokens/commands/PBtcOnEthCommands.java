package io.ptokens.commands;

import android.content.Context;

import static io.ptokens.commands.CommandNames.*;

public class PBtcOnEthCommands extends CommandInterface {


    public PBtcOnEthCommands(Context context, Command builder) {
        super(context, builder);

        map.put(GENERATE_PROOF, getGenerateProofBuilder());
        map.put(GET_LATEST_BLOCK_NUMBERS, getNativeAndReadabledDbBuilder());
        map.put(DEBUG_GET_ALL_DB_KEYS, getNativeAndReadabledDbBuilder());
        map.put(GET_ENCLAVE_STATE, getNativeAndReadabledDbBuilder());
        map.put(DEBUG_CLEAR_ALL_UTXOS, getNativeAndWriteableDbBuilder());
        map.put(DEBUG_ADD_UTXO_TO_DB, getReadPayloadAndWriteableDbBuilder());
        map.put(DEBUG_GET_ALL_UTXOS, getNativeAndReadabledDbBuilder());
        map.put(INITIALIZE_ETH, getInitializeEthBuilder());
        map.put(INITIALIZE_BTC, getInitializeBtcBuilder());
        map.put(SUBMIT_ETH_BLOCK, getReadPayloadAndWriteableDbBuilder());
        map.put(SUBMIT_BTC_BLOCK, getReadPayloadAndWriteableDbBuilder());
        map.put(DEBUG_GET_KEY_FROM_DB, getGetKeyBuilder());
        map.put(DEBUG_SET_KEY_IN_DB_TO_VALUE, getSetKeyBuilder());
        map.put(SIGN_MESSAGE_WITH_ETH_KEY, getSignMessageWithKeyBuilder());
        map.put(DEBUG_CHANGE_PNETWORK_TX, getWriteableDbWithAddressParameterBuilder());
        map.put(DEBUG_PROXY_CHANGE_PNETWORK_TX, getWriteableDbWithAddressParameterBuilder());
        map.put(DEBUG_PROXY_CHANGE_PNETWORK_BY_PROXY_TX, getWriteableDbWithAddressParameterBuilder());
        map.put(DEBUG_REPROCESS_ETH_BLOCK, getReadPayloadAndWriteableDbBuilder());
        map.put(DEBUG_REPROCESS_BTC_BLOCK, getReadPayloadAndWriteableDbBuilder());
        map.put(DEBUG_MINT_PBTC, getMintTransactionBuilder());
        map.put(DEBUG_BACKUP_DATABASE, getReadableDbBuilder());
        map.put(DEBUG_IMPORT_DATABASE, getReadableDbBuilder());
    }
}
