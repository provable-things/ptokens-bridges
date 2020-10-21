package io.ptokens.commands;

import android.content.Context;

import static io.ptokens.commands.CommandNames.*;

public class PBtcOnEosCommands extends CommandInterface {
    public static final String TAG = PBtcOnEosCommands.class.getName();

    public PBtcOnEosCommands(Context context, Command builder) {
        super(context, builder);

        map.put(GENERATE_PROOF, getGenerateProofBuilder());
        map.put(GET_LATEST_BLOCK_NUMBERS, getNativeAndReadabledDbBuilder());
        map.put(DEBUG_GET_ALL_DB_KEYS, getNativeAndReadabledDbBuilder());
        map.put(GET_ENCLAVE_STATE, getNativeAndReadabledDbBuilder());
        map.put(DEBUG_CLEAR_ALL_UTXOS, getNativeAndWriteableDbBuilder());
        map.put(DEBUG_GET_ALL_UTXOS, getNativeAndReadabledDbBuilder());
        map.put(INITIALIZE_EOS, getInitializeEosBuilder());
        map.put(INITIALIZE_BTC, getInitializeBtcBuilder());
        map.put(SUBMIT_EOS_BLOCK, getReadPayloadAndWriteableDbBuilder());
        map.put(SUBMIT_BTC_BLOCK, getReadPayloadAndWriteableDbBuilder());
        map.put(DEBUG_ADD_NEW_EOS_SCHEDULE, getReadPayloadAndWriteableDbBuilder());
        map.put(DEBUG_UPDATE_INCREMERKLE, getReadPayloadAndWriteableDbBuilder());
        map.put(DEBUG_GET_KEY_FROM_DB, getGetKeyBuilder());
        map.put(DEBUG_SET_KEY_IN_DB_TO_VALUE, getSetKeyBuilder());
        map.put(ENABLE_EOS_PROTOCOL_FEATURE, getProtocolFeaturesBuilder());
        map.put(DISABLE_EOS_PROTOCOL_FEATURE, getProtocolFeaturesBuilder());
        map.put(DEBUG_REPROCESS_BTC_BLOCK, getReadPayloadAndWriteableDbBuilder());
        map.put(DEBUG_REPROCESS_EOS_BLOCK, getReadPayloadAndWriteableDbBuilder());
    }
}
