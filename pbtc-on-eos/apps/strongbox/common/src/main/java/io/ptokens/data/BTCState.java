package io.ptokens.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BTCState {
    private long _btcDifficulty;
    private String _btcNetwork;
    private String _btcAddress;
    private long _btcUtxoNonce;
    private int _btcTailLength;
    private String _btcPublicKey;
    private int _btcSatsPerByte;
    private String _btcLinkerHash;
    private long _btcNumberOfUtxos;
    private long _btcUtxoTotalValue;
    private String _btcTailBlockHash;
    private String _btcCanonBlockHash;
    private long _btcTailBlockNumber;
    private String _btcLatestBlockHash;
    private long _btcCanonBlockNumber;
    private String _btcAnchorBlockHash;
    private int _btcCanonToTipLength;
    private long _btcLatestBlockNumber;
    private long _btcAnchorBlockNumber;

    @JsonProperty("btc_difficulty")
    long getBtcDifficulty() { return _btcDifficulty; }
    
    @JsonProperty("btc_network")
    String getBtcNetwork() { return _btcNetwork; }
    
    @JsonProperty("btc_address")
    String getBtcAddress() { return _btcAddress; }
    
    @JsonProperty("btc_utxo_nonce")
    long getBtcUtxoNonce() { return _btcUtxoNonce; }
    
    @JsonProperty("btc_tail_length")
    int getBtcTailLength() { return _btcTailLength; }
    
    @JsonProperty("btc_public_key")
    String getBtcPublicKey() { return _btcPublicKey; }
    
    @JsonProperty("btc_sats_per_byte")
    int getBtcSatsPerByte() { return _btcSatsPerByte; }
    
    @JsonProperty("btc_linker_hash")
    String getBtcLinkerHash() { return _btcLinkerHash; }
    
    @JsonProperty("btc_number_of_utxos")
    long getBtcNumberOfUtxos() { return _btcNumberOfUtxos; }
    
    @JsonProperty("btc_utxo_total_value")
    long getBtcUtxoTotalValue() { return _btcUtxoTotalValue; }
    
    @JsonProperty("btc_tail_block_hash")
    String getBtcTailBlockHash() { return _btcTailBlockHash; }
    
    @JsonProperty("btc_canon_block_hash")
    String getBtcCanonBlockHash() { return _btcCanonBlockHash; }
    
    @JsonProperty("btc_tail_block_number")
    long getBtcTailBlockNumber() { return _btcTailBlockNumber; }
    
    @JsonProperty("btc_latest_block_hash")
    String getBtcLatestBlockHash() { return _btcLatestBlockHash; }
    
    @JsonProperty("btc_canon_block_number")
    long getBtcCanonBlockNumber() { return _btcCanonBlockNumber; }
    
    @JsonProperty("btc_anchor_block_hash")
    String getBtcAnchorBlockHash() { return _btcAnchorBlockHash; }
    
    @JsonProperty("btc_latest_block_number")
    long getBtcLatestBlockNumber() { return _btcLatestBlockNumber; }
    
    @JsonProperty("btc_anchor_block_number")
    long getBtcAnchorBlockNumber() { return _btcAnchorBlockNumber; }
    
    @JsonProperty("btc_canon_to_tip_length")
    int getBtcCanonToTipLength() { return _btcCanonToTipLength; }

    public void setBtcDifficulty(long _btcDifficulty) {
        this._btcDifficulty = _btcDifficulty;
    }
    
    public void setBtcNetwork(String _btcNetwork) {
        this._btcNetwork = _btcNetwork;
    }
    
    public void setBtcAddress(String _btcAddress) {
        this._btcAddress = _btcAddress;
    }
    
    public void setBtcUtxoNonce(long _btcUtxoNonce) {
        this._btcUtxoNonce = _btcUtxoNonce;
    }
    
    public void setBtcTailLength(int _btcTailLength) {
        this._btcTailLength = _btcTailLength;
    }
    
    public void setBtcPublicKey(String _btcPublicKey) {
        this._btcPublicKey = _btcPublicKey;
    }
    
    public void setBtcSatsPerByte(int _btcSatsPerByte) {
        this._btcSatsPerByte = _btcSatsPerByte;
    }
    
    public void setBtcLinkerHash(String _btcLinkerHash) {
        this._btcLinkerHash = _btcLinkerHash;
    }
    
    public void setBtcNumberOfUtxos(long _btcNumberOfUtxos) {
        this._btcNumberOfUtxos = _btcNumberOfUtxos;
    }
    
    public void setBtcUtxoTotalValue(long _btcUtxoTotalValue) {
        this._btcUtxoTotalValue = _btcUtxoTotalValue;
    }
    
    public void setBtcTailBlockHash(String _btcTailBlockHash) {
        this._btcTailBlockHash = _btcTailBlockHash;
    }
    
    public void setBtcCanonBlockHash(String _btcCanonBlockHash) {
        this._btcCanonBlockHash = _btcCanonBlockHash;
    }
    
    public void setBtcTailBlockNumber(long _btcTailBlockNumber) {
        this._btcTailBlockNumber = _btcTailBlockNumber;
    }
    
    public void setBtcLatestBlockHash(String _btcLatestBlockHash) {
        this._btcLatestBlockHash = _btcLatestBlockHash;
    }
    
    public void setBtcCanonBlockNumber(long _btcCanonBlockNumber) {
        this._btcCanonBlockNumber = _btcCanonBlockNumber;
    }
    
    public void setBtcAnchorBlockHash(String _btcAnchorBlockHash) {
        this._btcAnchorBlockHash = _btcAnchorBlockHash;
    }
    
    public void setBtcLatestBlockNumber(long _btcLatestBlockNumber) {
        this._btcLatestBlockNumber = _btcLatestBlockNumber;
    }
    
    public void setBtcAnchorBlockNumber(long _btcAnchorBlockNumber) {
        this._btcAnchorBlockNumber = _btcAnchorBlockNumber;
    }
    
    public void setBtcCanonToTipLength(int _btcCanonToTipLength) {
        this._btcCanonToTipLength = _btcCanonToTipLength;
    }
}