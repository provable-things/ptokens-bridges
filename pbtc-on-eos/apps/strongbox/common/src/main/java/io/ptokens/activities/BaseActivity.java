package io.ptokens.activities;

import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetClient;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;

import io.ptokens.commands.Command;
import io.ptokens.commands.CommandInterface;
import io.ptokens.database.DatabaseException;
import io.ptokens.database.DatabaseWiring;
import io.ptokens.database.SQLiteHelper;
import io.ptokens.safetynet.SafetyNetHelper;
import io.ptokens.safetynet.Utils;
import io.ptokens.utils.Logger;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getName();

    protected static boolean FLAG_WRITE_STATE_HASH = false;
    protected static boolean FLAG_VERIFY_STATE_HASH = false;

    protected static final int SAFETY_NET_TIMEOUT_BETWEEN_RETRIES = 1000;
    protected static final int SAFETY_NET_MAX_RETRIES = 5;

    protected Command builder;
    protected Logger logger;
    protected CommandInterface commandImpl;

    protected Context mContext = this;

    protected DatabaseWiring wiring;

    protected String jsonResult;

    static {
        System.loadLibrary("app_jni");
        System.loadLibrary("shathree");
        System.loadLibrary("sqliteX");
    }

    protected byte[] getCborEnclaveState(String jsonState, Class stateClass) {
        CBORFactory cborFactory = new CBORFactory();
        ObjectMapper jsonMapper = new ObjectMapper();
        ObjectMapper cborMapper = new ObjectMapper(cborFactory);

        try {
            Object enclaveState = jsonMapper.readValue(jsonState, stateClass);
            return cborMapper.writeValueAsBytes(enclaveState);
        } catch (IOException e) {
            Log.e(TAG, "✘ Failed to convert the state to CBOR encoding", e);
        }

        return null;
    }

    @SuppressWarnings("unused")
    public String debugExportDatabaseToSd() {
        SQLiteHelper.copyDatabaseToSdCard(mContext);

        return "✔ Database successfully copied to sdcard/";
    }

    @SuppressWarnings("unused")
    public String debugImportDatabaseFromSd() {
        SQLiteHelper.copyDatabaseFromSdCard(mContext);

        return "✔ Database successfully imported from sdcard/";
    }

    @SuppressWarnings("unused")
    public void generateProof(String apiKey, byte[] cborState, boolean safetyNetIncluded) {
        String[] apkCertificateDigest = Utils.calcApkCertificateDigests(mContext);
        String apkDigest = Utils.calcApkDigest(getApplicationContext());
        int timestamp = (int) Instant
                .now(Clock.systemUTC())
                .getEpochSecond();

        SafetyNetClient client = SafetyNet.getClient(mContext);
        SafetyNetHelper safetyNetHelper = new SafetyNetHelper.Builder(client, apiKey)
                .setPackageName(getPackageName())
                .setCurrentState(cborState)
                .setApkCertDigest(apkCertificateDigest)
                .setApkDigest(apkDigest)
                .setCommitmentTimestamp(timestamp)
                .setTimeoutBetweenRetries(SAFETY_NET_TIMEOUT_BETWEEN_RETRIES)
                .setRetriesMax(SAFETY_NET_MAX_RETRIES)
                .build();

        safetyNetHelper.getAttestation(logger, safetyNetIncluded);
    }

    @SuppressWarnings("unused")
    public byte[] get(byte[] key, byte dataSensitivity) {
        return wiring.get(key, dataSensitivity);
    }

    @SuppressWarnings("unused")
    public void put(byte[] key, byte[] value, byte dataSensitivity) {
        wiring.put(key, value, dataSensitivity);
    }

    @SuppressWarnings("unused")
    public void delete(byte[] key) {
        wiring.delete(key);
    }

    @SuppressWarnings("unused")
    public void startTransaction() {
        try {
            wiring.startTransaction();
        } catch (DatabaseException e) {
            Log.e(TAG, "✘ Failed to start the transaction", e);
        }
    }

    @SuppressWarnings("unused")
    public void endTransaction() {
        try {
            wiring.endTransaction();
        } catch (DatabaseException e) {
            Log.e(TAG, "✘ Failed to end the transaction", e);
        }
    }

    @SuppressWarnings("unused")
    public void logResult(String result) {
        logger.logResult(result);
    }

    @SuppressWarnings("unused")
    public void logError(String error, int error_code) {
        logger.logError(error, error_code);
    }
}
