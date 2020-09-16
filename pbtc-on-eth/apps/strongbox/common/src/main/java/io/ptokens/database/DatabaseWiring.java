package io.ptokens.database;

import android.content.Context;
import android.database.CursorIndexOutOfBoundsException;

import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import org.apache.commons.codec.binary.Hex;
import org.sqlite.database.sqlite.SQLiteDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ptokens.security.Strongbox;
import io.ptokens.security.StrongboxException;
import io.ptokens.utils.Operations;

/**
 * Implements the database interface to be exposed
 * to jni.
 */
public class DatabaseWiring implements DatabaseInterface {

    public static final String TAG = DatabaseWiring.class.getName();
    private static final String NAME_SIGNED_STATE_HASH = "state-hash.sig";

    private boolean START_TX_IN_PROGRESS = false;
    private boolean END_TX_IN_PROGRESS = false;

    private Context context;
    private SQLiteDatabase db;
    private Map<String, byte[]> cache;
    private List<String> removedKeys;
    private Strongbox strongbox;
    private boolean verifySignedStateHashEnabled;
    private boolean writeSignedStateHashEnabled;

    public DatabaseWiring(
        Context context, 
        SQLiteDatabase db, 
        boolean verifyStateHash
    ) {
        this.db = db;
        this.context = context;
        this.removedKeys = new ArrayList<>();
        this.cache = Collections.synchronizedMap(new HashMap<>());
        this.verifySignedStateHashEnabled = verifyStateHash;
        this.strongbox = new Strongbox();

        SQLiteHelper.loadExtension(db);
    }

    public DatabaseWiring(
        Context context, 
        SQLiteDatabase db, 
        boolean verifyStateHash, 
        boolean writeSignedStateHash
    ) {
        this(context, db, verifyStateHash);
        this.writeSignedStateHashEnabled = writeSignedStateHash;
    }


    @Override
    public void put(byte[] key, byte[] value, byte dataSensitivity) {
        String hexKey = new String(Hex.encodeHex(key));

        removedKeys.remove(hexKey);

        if (dataSensitivity == (byte) 255) {
            cache.put(hexKey, strongbox.encrypt(value));
        } else {
            cache.put(hexKey, value);
        }
    }

    @Override
    public byte[] get(byte[] key, byte dataSensitivity) {
        byte[] error = { 0x0F };
        String hexKey = new String(Hex.encodeHex(key));

        try {
            if (removedKeys.contains(hexKey)) {
                Log.d(TAG, "✔ get: value for " + hexKey + " was removed");
                return error;
            } else if (dataSensitivity == (byte) 255) {
                try {
                    return strongbox.decrypt(readCache(hexKey));
                } catch (NullPointerException e) {
                    return strongbox.decrypt(readDatabase(hexKey));
                }
            } else {
                try {
                    return readCache(hexKey);
                } catch (NullPointerException e) {
                    return readDatabase(hexKey);
                }
            }
        } catch (CursorIndexOutOfBoundsException e) {
            Log.v(TAG, "✘ get: Key " + hexKey + " not stored inside the db");
        } catch (Exception e) {
            Log.e(TAG, "✘ get: Failed to get the value from database, on key " + hexKey, e);
        }

        return error;
    }

    @Override
    public void delete(byte[] key) {
        String hexKey = new String(Hex.encodeHex(key));

        removedKeys.add(hexKey);
    }

    @Override
    public void startTransaction() throws DatabaseException {
        Log.i(TAG, "✔ Start transaction in progress!");
        if (START_TX_IN_PROGRESS) {
            return;
        }
        START_TX_IN_PROGRESS = true;

        if (verifySignedStateHashEnabled) {
            try {
                verifySignedStateHash();
            } catch (StrongboxException e) {
                Log.e(TAG, "Signed state hash verification failed!", e);
                throw new DatabaseException("Start transaction failed");
            }
        } else {
            Log.i(TAG, "✔ Signed state hash verification skipped");
        }

        db.beginTransaction();
    }

    @Override
    public void endTransaction() throws DatabaseException {
        Log.i(TAG, "✔ endTransaction in progress... ");

        try {
            if (END_TX_IN_PROGRESS) {
                return;
            } if (!START_TX_IN_PROGRESS) {
                throw new DatabaseException(
                        "✘ Invalid order, call startTransaction first!"
                );
            } else {
                START_TX_IN_PROGRESS = false;
            }

            END_TX_IN_PROGRESS = true;

            Log.v(TAG, "✔ Writing keys: ");

            for (String key : cache.keySet()) {
                SQLiteHelper.insertOrReplace(db, key, cache.get(key));
            }

            for (String key : removedKeys) {
                SQLiteHelper.deleteKey(db, key);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        try {
            if (writeSignedStateHashEnabled) {
                writeSignedStateHash();    
            } else {
                Log.w(TAG, "✔ Skipping state hash writing...");                        
            }
        } catch (DatabaseException e) {
            Log.e(TAG, "✘ Failed to write the state hash", e);
        } finally {
            START_TX_IN_PROGRESS = false;
        }
    }

    private void writeSignedStateHash() throws DatabaseException {

        byte[] hash = getCurrentStateHash();
        if (hash == null) {
            throw new DatabaseException("Write signed state failed, hash not found");
        }

        int aliasNumber = strongbox.getLatestAliasNumber();

        String oldAlias = Strongbox.ALIAS_STATE_SIGNING_KEY_PREFIX
                + Strongbox.ALIAS_STATE_SIGNING_KEY_SEPARATOR
                + aliasNumber;
        String newAlias = Strongbox.ALIAS_STATE_SIGNING_KEY_PREFIX
                + Strongbox.ALIAS_STATE_SIGNING_KEY_SEPARATOR
                + (aliasNumber + 1);
        Strongbox.generateSigningKey(newAlias);
        Log.i(TAG, "✔ Switch key " + oldAlias + " <=> " + newAlias);
        byte[] signedState = strongbox.sign(newAlias, hash);

        Log.i(TAG, "✔ New signed state hash " +
                Base64.encodeToString(signedState, Base64.DEFAULT)
        );

        Operations.writeBytes(context, NAME_SIGNED_STATE_HASH, signedState);

        strongbox.removeKey(oldAlias);
    }

    private byte[] getCurrentStateHash() {
        try {
            ArrayList<Pair<String, byte[]>> keyValuePairs = SQLiteHelper.getKeysAndHashedValues(db);

            if (keyValuePairs.isEmpty()) {
                Log.w(TAG, "✔ No keys found!");
                return null;
            }

            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

            for (Pair<String, byte[]> keyValue : keyValuePairs){
                String key = keyValue.first;
                sha1.update(key.getBytes());
                sha1.update(keyValue.second);
            }

            byte[] currentStateHash = sha1.digest();
            Log.i(TAG, "✔ Current state hash"
                    + Base64.encodeToString(currentStateHash, Base64.DEFAULT)
            );

            return currentStateHash;
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG,"✘ SHA-1 not supported", e);
        } catch (Exception e) {
            Log.e(TAG,"✘ Current state hash error:" + e.getMessage());
        }

        return null;
}

    private void verifySignedStateHash() throws StrongboxException {

        byte[] signature = Operations.readBytes(context, NAME_SIGNED_STATE_HASH);
        byte[] hash = getCurrentStateHash();

        boolean signatureExists = (signature != null && signature.length > 0);
        boolean hashExists = (hash != null);
        if (!signatureExists && hashExists) {
            throw new StrongboxException("✘ Missing signature for existing state!");
        } else if (signatureExists && !hashExists) {
            throw new StrongboxException("✘ Existing signature for missing state!");
        } else if (!signatureExists) {
            Log.i(TAG, "✔ First run!");
            return;
        }

        // Reached this point the we have a signature and
        // a state hash
        int aliasNumber = strongbox.getLatestAliasNumber();
        if (aliasNumber == -1) {
            throw new StrongboxException("✘ Unverifiable signature for existing state! Aborting!");
        }
        String alias = Strongbox.ALIAS_STATE_SIGNING_KEY_PREFIX
                + Strongbox.ALIAS_STATE_SIGNING_KEY_SEPARATOR
                + aliasNumber;
        if (!strongbox.verify(alias, hash, signature)) {
            throw new StrongboxException("✘ Invalid signature for existing state!");
        } else {
            Log.i(TAG, "✔ Signed state hash verified");
        }
    }


    @Override
    public void close() {
        SQLiteHelper helper = new SQLiteHelper(context);
        db.close();
        helper.close();
        Log.w(TAG, "Db closed");
    }

    private byte[] readCache(String hexKey) {
        byte[] value = cache.get(hexKey);
        if (value == null)
            throw new NullPointerException("Value not found in the cache");
        return value;
    }

    private byte[] readDatabase(String hexKey) {
        byte[] value = SQLiteHelper.getBytesFromKey(db, hexKey);

        if (value == null)
            throw new NullPointerException("Value not found in the database");
        return value;
    }
}
