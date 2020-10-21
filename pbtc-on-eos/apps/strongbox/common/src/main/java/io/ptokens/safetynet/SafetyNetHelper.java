package io.ptokens.safetynet;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.safetynet.SafetyNetClient;
import com.google.android.gms.safetynet.SafetyNetStatusCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import io.ptokens.data.ProofResult;
import io.ptokens.data.SafetyNetResponse;
import io.ptokens.security.Strongbox;
import io.ptokens.utils.Logger;

public class SafetyNetHelper {

    private static final int SAFETY_NET_API_UNKNOWN_ERROR = 1000;
    private static final int SAFETY_NET_API_EMPTY_RESPONSE = 1001;
    private static final int LOCAL_RESPONSE_VALIDATION_FAILED = 1003;
    private static final int GOOGLE_PLAY_SERVICES_NOT_AVAILABLE = 1004;
    private static final String TAG = SafetyNetHelper.class.getName();

    private static final byte PROOF_VERSION = 0;
    private static final String PROOF_TYPE = "strongbox";


    private byte[] nonce;
    private String packageName;
    private String[] apkCertificateDigests;
    private String apkDigest;

    private SafetyNetWrapperCallback callback;

    private SafetyNetClient client;

    private Integer mTimeoutBetweenRetries;
    private Integer mRetriesMax;
    private Integer retriesCounter = 0;
    private String mApiKey;
    private String attestationJson = "";
    private byte[] cborEncodedState;
    private int commitmentTimestamp;
    private byte[] commitment;

    private SafetyNetHelper(final Builder builder) {
        this.client = builder.client;
        this.packageName = builder.packageName;
        this.apkCertificateDigests = builder.apkCertDigest;
        this.apkDigest = builder.apkDigest;
        this.mTimeoutBetweenRetries = builder.timeoutBetweenRetries;
        this.mRetriesMax = builder.retriesMax;
        this.mApiKey = builder.apiKey;
        this.commitmentTimestamp = builder.commitmentTimestamp;
        this.cborEncodedState = builder.cborEncodedState;

        this.commitment = getCommitment();
        this.nonce = getAttestationNonce(this.commitment);
    }

    public interface SafetyNetWrapperCallback {
        void error(String errorType, String errorValue);
        void success(SafetyNetResponse response, String attestationResult);
    }

    private void requestTest(SafetyNetWrapperCallback safetyNetWrapperCallback) {
        callback = safetyNetWrapperCallback;

        Task<SafetyNetApi.AttestationResponse> task = client.attest(nonce, mApiKey);
        task.addOnSuccessListener(mSuccessListener)
                .addOnFailureListener(mFailureListener);
    }


    private OnSuccessListener<SafetyNetApi.AttestationResponse> mSuccessListener = attestationResponse -> {
        final String jwsResult = attestationResponse.getJwsResult();
        if (!TextUtils.isEmpty(jwsResult)) {
            final SafetyNetResponse response = parseJsonWebSignature(jwsResult);

            if (validateSafetyNetResponsePayload(response)) {
                callback.success(response, jwsResult);
            } else {
                handleError(LOCAL_RESPONSE_VALIDATION_FAILED, jwsResult);
            }
        } else {
            handleError(SAFETY_NET_API_EMPTY_RESPONSE, "");
        }
        Log.d(TAG, "Success! SafetyNet result:\n" + jwsResult + "\n");
    };

    private OnFailureListener mFailureListener = exception -> {
        if (exception instanceof ApiException) {
            // An error with the Google Play Services API contains some additional details.
            ApiException apiException = (ApiException) exception;
            Log.e(TAG, "Error: " +
                    SafetyNetStatusCodes.getStatusCodeString(apiException.getStatusCode()) + ": " +
                    apiException.getStatusCode() + " = " + apiException.getMessage());
            if (apiException.getStatusCode() == SafetyNetStatusCodes.TIMEOUT) {
                handleError(SafetyNetStatusCodes.TIMEOUT, apiException.getMessage());
            } else if (apiException.getStatusCode() == SafetyNetStatusCodes.CANCELED) {
                handleError(apiException.getStatusCode(), apiException.getMessage());
            } else {
                Integer statusCode = apiException.getStatusCode();
                handleError(SAFETY_NET_API_UNKNOWN_ERROR, statusCode.toString());
            }
        } else {
            // A different, unknown type of error occurred.
            Log.e(TAG, "ERROR: " + exception.getClass().getCanonicalName() + ": " + exception.getMessage());
        }
    };


    private boolean validateSafetyNetResponsePayload(SafetyNetResponse response) {
        if (response == null) {
            Log.e(TAG, "SafetyNetResponse is null.");
            return false;
        }

        String requestNonceBase64 = Base64.getEncoder().encodeToString(nonce).trim()
                .replace("\n", "");

        if (!requestNonceBase64.equals(response.getNonce())) {
            Log.e(TAG, "invalid nonce, expected = \"" + requestNonceBase64 + "\"");
            Log.e(TAG, "invalid nonce, response   = \"" + response.getNonce() + "\"");
            return false;
        }

        if (!packageName.equalsIgnoreCase(response.getApkPackageName())) {
            Log.e(TAG, "invalid packageName, expected = \"" + packageName + "\"");
            Log.e(TAG, "invalid packageName, response = \"" + response.getApkPackageName() + "\"");
            return false;
        }

        if (apkCertificateDigests == null) {
            Log.e(TAG, "apkCertificateDigest is null");
        }

        if (!Arrays.equals(apkCertificateDigests, response.getApkCertificateDigestSha256())) {
            Log.e(TAG, "invalid apkCertificateDigest, local/expected = " + Arrays.asList(apkCertificateDigests));
            Log.e(TAG, "invalid apkCertificateDigest, response = " + Arrays.asList(response.getApkCertificateDigestSha256()));
            return false;
        }

        if (!apkDigest.equals(response.getApkDigestSha256())) {
            Log.e(TAG, "invalid ApkDigest, local/expected = \"" + apkDigest + "\"");
            Log.e(TAG, "invalid ApkDigest, response = \"" + response.getApkDigestSha256() + "\"");
            return false;
        }

        if (!response.isCtsProfileMatch()) {
            Log.e(TAG, "CtsProfileMatch is false");
            return false;
        }

        if (!response.isBasicIntegrity()) {
            Log.e(TAG, "BasicIntegrity is false");
            return false;
        }

        Log.d(TAG, response.toString());
        return true;
    }

    @Nullable
    private SafetyNetResponse parseJsonWebSignature(@NonNull String jwsResult) {
        final String[] jwtParts = jwsResult.split("\\.");

        if (jwtParts.length == 3) {
            String decodedPayload = new String(Base64.getDecoder().decode(jwtParts[1]));

            return SafetyNetResponse.parse(decodedPayload);
        } else {
            return null;
        }
    }

    private void handleError(int errorCode, String errorValue) {

        switch (errorCode) {
            default:
            case SafetyNetHelper.SAFETY_NET_API_UNKNOWN_ERROR:
                Log.e(TAG, "SafetyNet Request: fail\n");
                callback.error("failed_safetynet_response", errorValue);
                retry();
                break;
            case SafetyNetHelper.SAFETY_NET_API_EMPTY_RESPONSE:
                Log.e(TAG, "SafetyNet Request: empty response\n");
                callback.error("empty_safetynet_response", "");
                retry();
                break;
            case SafetyNetStatusCodes.CANCELED:
                Log.e(TAG, "SafetyNet Request: cancelled\n");
                callback.error("request cancelled", errorValue);
                retry();
                break;
            case SafetyNetHelper.GOOGLE_PLAY_SERVICES_NOT_AVAILABLE:
                Log.e(TAG, "SafetyNet Request: fail\n");
                callback.error("google play services not available", errorValue);
                break;
            case SafetyNetStatusCodes.TIMEOUT:
                Log.e(TAG, "SafetyNet Request: timeout\n");
                callback.error("safety_net_request_timeout", "");
                break;
            case SafetyNetHelper.LOCAL_RESPONSE_VALIDATION_FAILED:
                Log.e(TAG, "SafetyNet Request: success\n");
                Log.e(TAG, "Local response validation: fail\n");
                callback.error("local_response_validation_fail", errorValue);
                break;
        }
    }

    private void retry() {
        if ((retriesCounter < mRetriesMax)) {
            retriesCounter++;

            try {
                Log.d(TAG, "Retrying SafetyNet Request, waiting for " + mTimeoutBetweenRetries + "ms");
                Thread.sleep(mTimeoutBetweenRetries);
            } catch (InterruptedException e) {
                Log.e(TAG, "Sleep failed...", e);
                callback.error(e.getMessage(), "Timeout between retries failed");
                Thread.currentThread().interrupt();
            }

            Log.d(TAG, "SafetyNet Request retry: " + retriesCounter);

            Task<SafetyNetApi.AttestationResponse> task = client.attest(nonce, mApiKey);
            task.addOnSuccessListener(mSuccessListener)
                    .addOnFailureListener(mFailureListener);

        } else {
            Log.e(TAG, "Maximum retries reached with FAIL");
            callback.error("max_safety_net_retries_reached", retriesCounter.toString());
        }
    }

    public static class Builder {
        private SafetyNetClient client;
        private String packageName;
        private String[] apkCertDigest;
        private String apkDigest;
        private Integer timeoutBetweenRetries;
        private Integer retriesMax;
        private String apiKey;
        private int commitmentTimestamp;
        private byte[] cborEncodedState = null;

        public Builder(SafetyNetClient client, String apiKey) {
            this.client = client;
            this.apiKey = apiKey;
        }

        public Builder setCurrentState(byte[] cborEncodedState) {
            this.cborEncodedState = cborEncodedState;
            return this;
        }

        public Builder setCommitmentTimestamp(int commitmentTimestamp) {
            this.commitmentTimestamp = commitmentTimestamp;
            return this;
        }

        public Builder setPackageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public Builder setApkCertDigest(String[] apkCertDigest) {
            this.apkCertDigest = apkCertDigest;
            return this;
        }

        public Builder setApkDigest(String apkDigest) {
            this.apkDigest = apkDigest;
            return this;
        }

        public Builder setTimeoutBetweenRetries(Integer timeoutBetweenRetries) {
            this.timeoutBetweenRetries = timeoutBetweenRetries;
            return this;
        }

        public Builder setRetriesMax(Integer retriesMax) {
            this.retriesMax = retriesMax;
            return this;
        }

        public SafetyNetHelper build() {
            return new SafetyNetHelper(this);
        }
    }

    private byte[] getCommitment() {
        if (cborEncodedState == null) {
            throw new RuntimeException("✘ State not defined!");
        }

        if (commitmentTimestamp == 0) {
            throw new RuntimeException("✘ Commitment timestamp not defined!");
        }

        ByteBuffer commitment = ByteBuffer
                .allocate(1 + 4 + cborEncodedState.length);

        commitment
                .put(PROOF_VERSION)
                .putInt(commitmentTimestamp)
                .put(ByteBuffer.wrap(cborEncodedState));

        return commitment.array();
    }


    private byte[] getAttestationNonce(byte[] commitment) {
        // Even if we do not explicitly hash the commitment
        // here, the commitment will be sha256-digested by
        // the signature function, check that out for further
        // details
        return Strongbox.signWithAttestionKey(commitment);
    }

    public void getAttestation(Logger logger, boolean safetynetIncluded) {
        Context context = client.getApplicationContext();
        final int isGoogleAvailable = checkGoogleServicesAvailability(context);

        if (safetynetIncluded) {
            Log.d(TAG, "Safety is included, performing call to Google...");
            requestTest(new SafetyNetHelper.SafetyNetWrapperCallback() {
                @Override
                public void error(String errorType, String errorValue) {
                    Log.e(TAG, "✘ Failed to make the request, error type:"
                            + errorType
                            + " isGoogleAvailable: "
                            + isGoogleAvailable
                    );
                }

                @Override
                public void success(SafetyNetResponse response, String attestationResult) {
                    String serializedAttestation = Utils.serializeAttestationResult(attestationResult);
                    String serializedCertChain = Strongbox.getCertificateAttestation();

                    ProofResult proofResult = new ProofResult(
                            PROOF_TYPE,
                            PROOF_VERSION,
                            commitment,
                            nonce,
                            commitmentTimestamp,
                            serializedAttestation,
                            serializedCertChain
                    );

                    ObjectMapper mapper = new ObjectMapper();

                    try {
                        attestationJson = mapper.writeValueAsString(proofResult);
                        logger.logResult(attestationJson);
                    } catch (JsonProcessingException e) {
                        Log.e(TAG, "✘ Failed to write the attestation json response", e);
                        logger.logError("Failed to write the resp", 1);
                    }
                }
            });
        } else {
            Log.d(TAG, "Safety not included, delivering Strongbox...");

            String serializedCertChain = Strongbox.getCertificateAttestation();
            String safetyNetAttestion = "";
            ProofResult proofResult = new ProofResult(
                    PROOF_TYPE,
                    PROOF_VERSION,
                    commitment,
                    nonce,
                    commitmentTimestamp,
                    safetyNetAttestion,
                    serializedCertChain
            );

            ObjectMapper mapper = new ObjectMapper();

            try {
                attestationJson = mapper.writeValueAsString(proofResult);
                logger.logResult(attestationJson);
            } catch (JsonProcessingException e) {
                Log.e(TAG, "✘ Failed to write the attestation json response", e);
                logger.logError("Failed to write the resp", 1);
            }
        }

    }

    private int checkGoogleServicesAvailability(Context context) {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
    }
}
