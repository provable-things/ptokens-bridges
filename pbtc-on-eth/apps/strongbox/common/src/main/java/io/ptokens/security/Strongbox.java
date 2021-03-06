package io.ptokens.security;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;


import org.apache.commons.codec.binary.Hex;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Enumeration;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;

import io.ptokens.data.AttestationCertificate;

public class Strongbox {
    private static final String TAG = Strongbox.class.getName();
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String ALIAS_SECRET_KEY = "io.ptokens.secretkey";
    public static final String ALIAS_STATE_SIGNING_KEY_PREFIX = "io.ptokens.ecdsa";
    public static final String ALIAS_STATE_SIGNING_KEY_SEPARATOR = "-";
    private static final String ALIAS_ATTESTATION_KEY = "io.ptokens.attestation";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";

    private static void generateSecretKey() {

        try {
            KeyStore ks = KeyStore.getInstance(Strongbox.ANDROID_KEY_STORE);
            ks.load(null);
            if (!ks.containsAlias(ALIAS_SECRET_KEY)) {
                KeyGenerator generator;
                generator = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES,
                        ANDROID_KEY_STORE
                );
                generator.init(new KeyGenParameterSpec.Builder(ALIAS_SECRET_KEY,
                        KeyProperties.PURPOSE_ENCRYPT
                                | KeyProperties.PURPOSE_DECRYPT)
                        .setKeySize(128)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setIsStrongBoxBacked(true)
                        .build());

                SecretKey key = generator.generateKey();

                SecretKeyFactory factory = SecretKeyFactory.getInstance(
                        key.getAlgorithm(),
                        "AndroidKeyStore"
                );

                KeyInfo info = (KeyInfo) factory.getKeySpec(key, KeyInfo.class);

                Log.d(TAG, "✔ New secret key generated with alias: " + ALIAS_SECRET_KEY);
                Log.d(TAG, "✔ Is inside secure hardware? " + info.isInsideSecureHardware());

            }
            else {
                Log.d(TAG, "✔ Secret key with alias " + ALIAS_SECRET_KEY + " already existing");
            }
        } catch (Exception e) {
            Log.e(TAG, "✘ generateSecretKey: Failed to generate the secret key", e);
        }
    }

    public static void generateSigningKey(String alias) {
        try {
            KeyStore ks = KeyStore.getInstance(Strongbox.ANDROID_KEY_STORE);
            ks.load(null);
            if (!ks.containsAlias(alias)) {
                KeyPairGenerator generator;

                generator = KeyPairGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_EC,
                        ANDROID_KEY_STORE
                );

                String attestationChallenge = "ptokens";
                byte[] challenge = attestationChallenge.getBytes(StandardCharsets.UTF_8);

                KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                        alias,
                        KeyProperties.PURPOSE_SIGN |
                                KeyProperties.PURPOSE_VERIFY)
                        .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                        .setDigests(KeyProperties.DIGEST_SHA256)
                        .setUserAuthenticationRequired(false)
                        .setIsStrongBoxBacked(true)
                        .setAttestationChallenge(challenge)
                        .build();

                generator.initialize(spec);
                generator.generateKeyPair();
                Log.d(TAG, "✔ New key pair with alias " + alias + " created");
            } else {
                Log.d(TAG, "✔ Key Pair with alias " + alias + " already existing");
            }
        } catch (Exception e) {
            Log.e(TAG, "✘ generateSigningKey: Failed to generate the keypair ", e);
        }
    }

    public static void initializeKeystore ()  {
        try {
            KeyStore ks = KeyStore.getInstance(ANDROID_KEY_STORE);
            ks.load(null);
            generateSecretKey();
            generateSigningKey(ALIAS_ATTESTATION_KEY);
            Log.d(TAG, "✔ Keystore initialized");
        } catch (Exception e) {
            Log.e(TAG,"✘ initializeKeystore: Failed to initialize the keystore", e);
        }
    }

    private static Key getSecretKey()
            throws
            KeyStoreException,
            NoSuchAlgorithmException,
            UnrecoverableEntryException {

        KeyStore ks = KeyStore.getInstance(ANDROID_KEY_STORE);
        try {
            ks.load(null);
        } catch (Exception e) {
            Log.e(TAG, "✘ getSecretKey: Failed to load the keystore", e);
        }

        return ks.getKey(ALIAS_SECRET_KEY, null);
    }

    public static void removeKey(String alias) {
        try {
            KeyStore ks = KeyStore.getInstance(ANDROID_KEY_STORE);
            ks.load(null);

            ks.deleteEntry(alias);
        } catch (Exception e) {
            Log.e(TAG, "✘ removeKey: Failed to remove key from keystore", e);
        }

    }

    public static byte[] encrypt(byte[] data) {

        try {
            Key key = getSecretKey();
            final Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] iv = cipher.getIV();

            //Log.d(TAG, "✔ encrypt: iv length:" + iv.length);
            //Log.d(TAG, "✔ encrypt: iv is:" + Base64.encodeToString(iv, Base64.DEFAULT));

            byte[] encryptedData = cipher.doFinal(data);
            int RESULT_LENGTH = iv.length + encryptedData.length;
            byte[] result = new byte[RESULT_LENGTH];


            int k = 0;
            for (byte b : iv) {
                result[k++] = b;
            }
            for (byte b : encryptedData) {
                result[k++] = b;
            }

            return result;
        } catch (InvalidKeyException
                | BadPaddingException
                | IllegalBlockSizeException
                | NoSuchAlgorithmException
                | NoSuchPaddingException
                | UnrecoverableEntryException
                | KeyStoreException e) {
            Log.e(TAG, "✘ encrypt: Failed to encrypt data", e);
        }
        return new byte[1];
    }

    public static byte[] decrypt(byte[] data) {
        try {
            Key key = getSecretKey();
            byte[] iv = new byte[12];
            int ENCRYPTED_DATA_LEN = data.length - iv.length;
            byte[] encryptedData = new byte[ENCRYPTED_DATA_LEN];

            // Extracting iv and the encrypted data
            int i = 0;
            for (; i < iv.length; i++) {
                iv[i] = data[i];
            }

            // Log.d(TAG, "✔ decrypt:  iv is:" + Base64.encodeToString(iv, Base64.DEFAULT));

            for (int k = 0; k < ENCRYPTED_DATA_LEN; k++) {
                encryptedData[k] = data[i++];
            }

            final Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            final GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            return cipher.doFinal(encryptedData);
        } catch (InvalidKeyException
                | BadPaddingException
                | IllegalBlockSizeException
                | NoSuchAlgorithmException
                | NoSuchPaddingException
                | InvalidAlgorithmParameterException
                | UnrecoverableEntryException
                | KeyStoreException e) {
            Log.e(TAG, "✘ decrypt: Failed to decrypt data", e);
        }
        return new byte[1];
    }

    public static byte[] signWithAttestionKey(byte[] data) {
        return sign(ALIAS_ATTESTATION_KEY, data);
    }

    public static byte[] sign(String alias, byte[] data) {
        byte[] signature = null;
        try {
            KeyStore ks = KeyStore.getInstance(ANDROID_KEY_STORE);
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry(alias, null);
            if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
                Log.w(TAG, "sign: Not an instance of a PrivateKeyEntry " + alias);
                return null;
            }
            Signature s = Signature.getInstance("SHA256withECDSA");
            PrivateKey privateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
            s.initSign(privateKey);
            s.update(data);

            signature = s.sign();

            Log.d(TAG, "✔ Message signed successfully");
        } catch (Exception e) {
            Log.e(TAG, "✘ sign: Failed to sign data", e);
        }

        return signature;
    }

    public static boolean verify(String alias, byte[] message, byte[] signature) {
        try {
            KeyStore ks = KeyStore.getInstance(ANDROID_KEY_STORE);
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry(alias, null);
            if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
                Log.w(TAG, "✘ verify: Not an instance of a PrivateKeyEntry " + alias);
                return false;
            }
            Signature s = Signature.getInstance("SHA256withECDSA");
            s.initVerify(((KeyStore.PrivateKeyEntry) entry).getCertificate());  
            s.update(message);

            return s.verify(signature);
        } catch (Exception e) {
            Log.e(TAG, "✘ verify: Exception while verifying signature", e);
        }
        return false;
    }

    public static int getLatestAliasNumber() {
        int latestAlias = -1;

        try {
            KeyStore ks = KeyStore.getInstance(ANDROID_KEY_STORE);
            ks.load(null);
            Enumeration<String> aliases = ks.aliases();
            Log.d(TAG, "✔ Listing aliases");
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Log.d(TAG, " found " + alias);
                if(alias.startsWith(ALIAS_STATE_SIGNING_KEY_PREFIX)) {
                    int value = Integer.parseInt(
                            alias.split(ALIAS_STATE_SIGNING_KEY_SEPARATOR)[1]
                    );

                    if (value > latestAlias) {
                        latestAlias = value;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "✔ getLatestAlias: Failed to get aliases", e);
        }

        return latestAlias;
    }

    public static String getCertificateAttestation() {
        try {
            KeyStore ks = KeyStore.getInstance(ANDROID_KEY_STORE);
            ks.load(null);
            // Certificate c = ks.getCertificate(ALIAS_ATTESTATION_KEY);
            // Log.d(TAG, "✔ " + c.toString());

            Certificate[] certificateChain = ks.getCertificateChain(ALIAS_ATTESTATION_KEY);

            // Log.d(TAG, "✔ certificate chain len is: " + certificateChain.length);

            byte[] leaf = certificateChain[0].getEncoded();
            byte[] intermediate = certificateChain[1].getEncoded();
            byte[] root = certificateChain[2].getEncoded();

            AttestationCertificate attestationCertificate = new AttestationCertificate(
                    leaf,
                    intermediate,
                    root
            );

            CBORFactory cborFactory = new CBORFactory();
            ObjectMapper mapper = new ObjectMapper(cborFactory);
            byte[] data = mapper.writeValueAsBytes(attestationCertificate);
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            ba.write(data);

            return Base64
                    .encodeToString(ba.toByteArray(), Base64.DEFAULT)
                    .replaceAll("\n", "");

        } catch (Exception e) {
            Log.e(TAG, "✘ getCertificateAttestation: Failed to generate the certificate ", e);
            return "";
        }
    }
}
