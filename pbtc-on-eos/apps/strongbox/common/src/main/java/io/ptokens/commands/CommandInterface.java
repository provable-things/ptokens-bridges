package io.ptokens.commands;
import android.content.Context;
import android.util.Log;

import org.sqlite.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.ptokens.commands.CommandNames.*;
import static io.ptokens.utils.Operations.readFile;

public abstract class CommandInterface {
    protected static final String TAG = CommandInterface.class.getName();
    protected Map<String, Command> map;
    protected Context context;
    private Command builder;


    public SQLiteDatabase getDatabase() {
        return Objects.requireNonNull(
                map.get(builder.getCommand())
        ).getDatabase();
    }

    public Object execute() throws Exception {
        Command command = Objects.requireNonNull(
                map.get(builder.getCommand())
        );
        Log.d(TAG, "execute: async" + command.isAsync());
        return command.invokeCommand();
    }

    CommandInterface(Context context, Command builder) {
        this.context = context;
        this.builder = builder;
        map = new HashMap<>();
    }

    Command getReadableDbBuilder() {
        return builder.copy()
                .needsReadableDatabase();
    }

    Command getGenerateProofBuilder() {
        Command c = null;
        try {
            c = builder.copy()
                    .needsReadableDatabase()
                    .addIntentArg(INTENT_PROOF_TYPE, "", String.class)
                    .addIntentArg(INTENT_SAFETYNET_APIKEY, "", String.class)
                    .async();
        } catch (InvalidCommandException e) {
            Log.v(TAG, "Addition of intent "
                    + INTENT_SAFETYNET_INCLUDED
                    + " failed:", e
            );
        }
        return c;
    }

    Command getNativeAndReadabledDbBuilder() {
        return builder.copy()
                .isNative()
                .needsReadableDatabase();
    }

    Command getNativeAndWriteableDbBuilder() {
        return builder.copy()
                .isNative()
                .needsWritableDatabase();
    }
    
    Command getReadPayloadAndWriteableDbBuilder() {
        return builder.copy()
            .isNative()
            .needsWritableDatabase()
            .addValueArg(readFile(DEFAULT_PAYLOAD_NAME));
    }

    Command getReadPayloadAndReadableDbBuilder() {
        return builder.copy()
            .isNative()
            .needsReadableDatabase()
            .addValueArg(readFile(DEFAULT_PAYLOAD_NAME));
    }

    Command getInitializeEthBuilder() {
        Command c = null;
        try {
            c = builder.copy()
                .isNative()
                .needsWritableDatabase()
                .addValueArg(readFile(DEFAULT_PAYLOAD_NAME))
                .addIntentArg(INTENT_CHAIN_ID_NAME, DEFAULT_ETH_CHAINID, Byte.class)
                .addIntentArg(INTENT_GAS_PRICE_NAME, DEFAULT_ETH_GAS_PRICE, Long.class)
                .addIntentArg(INTENT_CONFS_NAME, DEFAULT_ETH_CONFS, Long.class);
        } catch(InvalidCommandException e) {
            Log.v(TAG, e.getMessage());
        }
        return c;
    }

    Command getInitializeEosBuilder() {
        Command c = null;
        try {
            c = builder.copy()
            .isNative()
            .needsWritableDatabase()
            .addIntentArg(INTENT_CHAIN_ID_NAME, DEFAULT_EOS_CHAIN_ID, String.class)
            .addIntentArg(INTENT_ACCOUNT_NAME, DEFAULT_ACCOUNT_NAME, String.class)
            .addIntentArg(INTENT_SYMBOL_NAME, DEFAULT_SYMBOL, String.class)
            .addValueArg(readFile(DEFAULT_PAYLOAD_NAME));
        } catch(InvalidCommandException e) {
            Log.e(TAG, e.getMessage());
        }
        return c;
    }

    Command getInitializeBtcBuilder() {
        Command c = null;
        try {
            c = builder.copy()
            .isNative()
            .needsWritableDatabase()
            .addValueArg(readFile(DEFAULT_PAYLOAD_NAME))
            .addIntentArg(INTENT_FEE_NAME, DEFAULT_BTC_FEE, Long.class)
            .addIntentArg(INTENT_DIFFICULTY_NAME, DEFAULT_BTC_DIFFICULTY, Long.class)
            .addIntentArg(INTENT_NETWORK_NAME, DEFAULT_BTC_NETWORK, String.class)
            .addIntentArg(INTENT_CONFS_NAME, DEFAULT_BTC_CONFS, Long.class);
        } catch(InvalidCommandException e) {
            Log.e(TAG, e.getMessage());
        }
        return c;
    }

    Command getChildPaysForParentTxDbBuilder() {
        Command c = null;
        try {
            c = builder.copy()
            .isNative()
            .needsWritableDatabase()
            .addIntentArg(INTENT_FEE_NAME, null, Long.class)
            .addIntentArg(INTENT_TXID_NAME, null, String.class)
            .addIntentArg(INTENT_VOUT_NAME, null, Integer.class);
        } catch(InvalidCommandException e) {
            Log.e(TAG, e.getMessage());
        }
        return c;
    }

    Command getGetKeyBuilder() {
        Command c = null;
        try {
            c = builder.copy()
            .isNative()
            .needsReadableDatabase()
            .addIntentArg(INTENT_KEY, null, String.class);
        } catch(InvalidCommandException e) {
            Log.e(TAG, e.getMessage());
        }
        return c;
    }

    Command getSetKeyBuilder() {
        Command c = null;
        try {
            c = builder.copy()
            .isNative()
            .needsWritableDatabase()
            .addIntentArg(INTENT_KEY, null, String.class)
            .addIntentArg(INTENT_VALUE, null, String.class);
        } catch(InvalidCommandException e) {
            Log.e(TAG, e.getMessage());
        }
        return c;
    }

    Command getProtocolFeaturesBuilder() {
        Command c = null;
        try {
            c = builder.copy()
            .isNative()
            .needsWritableDatabase()
            .addIntentArg(INTENT_PARAM, null, String.class);
        } catch(InvalidCommandException e) {
            Log.e(TAG, e.getMessage());
        }
        return c;
    }

    Command getWriteableDbWithAddressParameterBuilder() {
        Command c = null;
        try {
            c = builder.copy()
            .isNative()
            .needsWritableDatabase()
            .addIntentArg(INTENT_ADDRESS, null, String.class);
        } catch(InvalidCommandException e) {
            Log.e(TAG, e.getMessage());
        }
        return c;
    }

    Command getSignMessageWithKeyBuilder() {
        Command c = null;
        try {
            c = builder.copy()
                .isNative()
                .needsReadableDatabase()
                .addIntentArg(INTENT_MESSAGE, "", String.class);
        } catch(InvalidCommandException e) {
            Log.e(TAG, e.getMessage());
        }
        return c;
    }

    Command getMintTransactionBuilder() {
        Command c = null;
        try {
            c = builder.copy()
                .isNative()
                .needsReadableDatabase()
                .addIntentArg(INTENT_AMOUNT_NAME, null, Long.class)
                .addIntentArg(INTENT_NONCE_NAME, null, String.class)
                .addIntentArg(INTENT_ETH_NETWORK_NAME, null, Long.class)
                .addIntentArg(INTENT_GAS_PRICE_NAME, null, Long.class)
                .addIntentArg(INTENT_RECIPIENT_NAME, null, String.class);
        } catch(InvalidCommandException e) {
            Log.e(TAG, e.getMessage());   
        }

        return c;
    }
}
