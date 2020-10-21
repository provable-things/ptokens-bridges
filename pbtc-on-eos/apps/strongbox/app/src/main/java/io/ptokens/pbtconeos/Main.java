package io.ptokens.pbtconeos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;


import io.ptokens.activities.BaseActivity;
import io.ptokens.commands.Command;
import io.ptokens.commands.PBtcOnEosCommands;
import io.ptokens.data.PBtcOnEosState;
import io.ptokens.database.DatabaseWiring;
import io.ptokens.security.Strongbox;
import io.ptokens.utils.Logger;

public class Main extends BaseActivity {
    public static final String TAG = Main.class.getName();
    public static final String INTENT = "io.ptokens.pbtconeos.INTENT";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.i(TAG,"✔ App created");
        setContentView(R.layout.activity_main);

        Strongbox.initializeKeystore();

        IntentFilter filter = new IntentFilter(INTENT);
        registerReceiver(mReceiver, filter);

        FLAG_WRITE_STATE_HASH = true;
        FLAG_VERIFY_STATE_HASH = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent mainIntent = getIntent();
        Intent workIntent = new Intent(INTENT);
        workIntent.putExtras(mainIntent);

        sendBroadcast(workIntent);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "✔ Unregister broadcast receiver");
        super.onDestroy();
        Log.i(TAG, "✔ Activity destroyed!");
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent workIntent) {
            unregisterReceiver(this);

            builder = new Command(mContext, workIntent);

            Log.i(TAG, "✔ Command selected: "
                    + builder.getCommand()
                    + "["
                    + builder.getMarker()
                    + "]"
            );

            logger = new Logger(
                    mContext,
                    builder
            );

            commandImpl = new PBtcOnEosCommands(
                    mContext,
                    builder
            );
            
            try {
                wiring = new DatabaseWiring(
                        context,
                        commandImpl.getDatabase(),
                        FLAG_WRITE_STATE_HASH,
                        FLAG_VERIFY_STATE_HASH 
                );

                jsonResult = (String) commandImpl.execute();
            } catch (Exception e) {
                Log.e(TAG, "✘ onReceive: Failed to parse the command", e);
            } finally {
                wiring.close();
                Log.i(TAG, "✔ Command "
                        + builder.getCommand()
                        + "["
                        + builder.getMarker()
                        + "] handled"
                );

                // TODO: should call builder.isAsync() instead,
                //  sadly it is false when generateProof is called, NEEDS FIX
                if (builder.getCommand().equals("generateProof")) {
                    Log.d(TAG, "✔ finish()");
                    finish();
                } else {
                    logResult(jsonResult);
                    Log.d(TAG, "✔ System.exit(0)");
                    System.exit(0);
                }
            }
        }
    };

    @SuppressWarnings("unused")
    public void generateProof(boolean safetyNetIncluded) {
        String apiKey = BuildConfig.SAFETY_NET_APIKEY;
        String enclaveStateJson = getEnclaveState(this);
        byte[] cborEnclaveState = getCborEnclaveState(
            enclaveStateJson, 
            PBtcOnEosState.class
        );
        super.generateProof(apiKey, cborEnclaveState, safetyNetIncluded);
    }

    @SuppressWarnings("unused")
    public static native String getLatestBlockNumbers(Main callback);
    @SuppressWarnings("unused")
    public static native String getEnclaveState(Main callback);
    @SuppressWarnings("unused")
    public static native String debugGetAllUtxos(Main callback);
    @SuppressWarnings("unused")
    public static native String debugClearAllUtxos(Main callback);
    @SuppressWarnings("unused")
    public static native String debugGetKeyFromDb(Main callback, String key);
    @SuppressWarnings("unused")
    public static native String debugReprocessEosBlock(Main callback, String payload);
    @SuppressWarnings("unused")
    public static native String submitEosBlock(Main callback, String payload);
    @SuppressWarnings("unused")
    public static native String submitBtcBlock(Main callback, String payload);
    @SuppressWarnings("unused")
    public static native String debugReprocessBtcBlock(Main callback, String payload);
    @SuppressWarnings("unused")
    public static native String initializeEos(
        Main callback,
        String chainId,
        String accountName,
        String tokenSymbol,
        String eosInitJson
    );
    @SuppressWarnings("unused")
    public static native String initializeBtc(
        Main callback, 
        String payload,
        long fee,
        long difficulty,
        String network,
        long canonToTipLength
    );
    @SuppressWarnings("unused")
    public static native String debugSetKeyInDbToValue(Main callback, String key, String value);
    @SuppressWarnings("unused")
    public static native String debugAddNewEosSchedule(Main callback, String scheduleJson);
    @SuppressWarnings("unused")
    public static native String debugUpdateIncremerkle(Main callback, String eosJson);
    @SuppressWarnings("unused")
    public static native String enableEosProtocolFeature(Main callback, String featureHash);
    @SuppressWarnings("unused")
    public static native String disableEosProtocolFeature(Main callback, String featureHash);
    @SuppressWarnings("unused")
    public static native String debugGetAllDbKeys(Main callback);
    
}
