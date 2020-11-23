package io.ptokens.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PBtcOnEosState extends BtcState {
    private String _eosSymbol;
    private String _eosChainId;
    private String _eosPublicKey;
    private String _eosAccountName;
    private String _eosLastSeenBlockId;
    private String _btcSafeAddress;
    private String _eosSafeAddress;
    private String _btcSignatureNonce;
    private String _eosSignatureNonce;
    private String _eosLastSeenBlockNum;
    private List<KnownSchedule> _eosKnownSchedules;
    private List<ProtocolFeature>_eosEnabledProtocolFeatures;

    private class KnownSchedule {
        private String _scheduleDbKey;
        private int _scheduleVersion;

        @JsonProperty("schedule_db_key")
        public String getScheduleDbKey() { return _scheduleDbKey; }

        @JsonProperty("schedule_version")
        public int getScheduleVersion() { return _scheduleVersion; }

        public void  setScheduleDbKey(String _scheduleDbKey) {
            this._scheduleDbKey = _scheduleDbKey;
        }

        public void  setScheduleVersion(int _scheduleVersion) {
            this._scheduleVersion = _scheduleVersion;
        }
    }

    private class ProtocolFeature {
        private String _featureName;
        private String _featureHash;

        @JsonProperty("feature_name")
        public String getfeatureName() { return _featureName; }

        @JsonProperty("feature_hash")
        public String getfeatureHash() { return _featureHash; }

        public void setFeatureName(String _featureName) {
            this._featureName = _featureName;
        }

        public void setFeatureHash(String _featureHash) {
            this._featureHash = _featureHash;
        }

    }

    @JsonProperty("eos_symbol")
    public String getEosSymbol() { return _eosSymbol; }

    @JsonProperty("eos_chain_id")
    public String getEosChainId() { return _eosChainId; }

    @JsonProperty("eos_public_key")
    public String getEosPublicKey() { return _eosPublicKey; }

    @JsonProperty("eos_account_name")
    public String getEosAccountName() { return _eosAccountName; }

    @JsonProperty("eos_last_seen_block_id")
    public String getEosLastSeenBlockId() { return _eosLastSeenBlockId; }

    @JsonProperty("eos_safe_address")
    public String getEosSafeAddress() { return _eosSafeAddress; }

    @JsonProperty("btc_signature_nonce")
    public String getBtcSignatureNonce() { return _btcSignatureNonce; }

    @JsonProperty("eos_signature_nonce")
    public String getEosSignatureNonce() { return _eosSignatureNonce; }

    @JsonProperty("eos_last_seen_block_num")
    public String getEosLastSeenBlockNum() { return _eosLastSeenBlockNum; }

    @JsonProperty("eos_known_schedules")
    public List<KnownSchedule> getEosKnownSchedules() { return _eosKnownSchedules; }

    @JsonProperty("eos_enabled_protocol_features")
    public List<ProtocolFeature> getEosEnabledProtocolFeatures() { return _eosEnabledProtocolFeatures; }

    @JsonProperty("btc_safe_address")
    public String getBtcSafeAddress() { return _btcSafeAddress; }

    public  void setEosSymbol(String _eosSymbol) {
        this._eosSymbol = _eosSymbol;
    }

    public  void setEosChainId(String _eosChainId) {
        this._eosChainId = _eosChainId;
    }

    public  void setEosPublicKey(String _eosPublicKey) {
        this._eosPublicKey = _eosPublicKey;
    }

    public  void setEosAccountName(String _eosAccountName) {
        this._eosAccountName = _eosAccountName;
    }

    public  void setEosLastSeenBlockId(String _eosLastSeenBlockId) {
        this._eosLastSeenBlockId = _eosLastSeenBlockId;
    }

    public  void setEosSafeAddress(String _eosSafeAddress) {
        this._eosSafeAddress = _eosSafeAddress;
    }

    public  void setBtcSignatureNonce(String _btcSignatureNonce) {
        this._btcSignatureNonce = _btcSignatureNonce;
    }

    public  void setEosSignatureNonce(String _eosSignatureNonce) {
        this._eosSignatureNonce = _eosSignatureNonce;
    }

    public  void setEosLastSeenBlockNum(String _eosLastSeenBlockNum) {
        this._eosLastSeenBlockNum = _eosLastSeenBlockNum;
    }

    public  void setEosKnownSchedules(List _eosKnownSchedules) {
        this._eosKnownSchedules = _eosKnownSchedules;
    }

    public  void setEosEnabledProtocolFeatures(List _eosEnabledProtocolFeatures) {
        this._eosEnabledProtocolFeatures = _eosEnabledProtocolFeatures;
    }

    public void setBtcSafeAddress(String _btcSafeAddress) {
        this._btcSafeAddress = _btcSafeAddress;
    }
}
