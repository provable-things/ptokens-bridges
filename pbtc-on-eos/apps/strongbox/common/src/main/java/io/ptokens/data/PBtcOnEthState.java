package io.ptokens.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PBtcOnEthState extends BtcState {
    private long _ethTailBlockNumber;
    private long _ethGasPrice;
    private String _ethAddress;
    private int _ethTailLength;
    private long _anySenderNonce;
    private long _ethAccountNonce;
    private String _ethLinkerHash;
    private String _ethSafeAddress;
    private String _btcSafeAddress;
    private String _ethTailBlockHash;
    private String _ethCanonBlockHash;
    private String _ethAnchorBlockHash;
    private String _ethLatestBlockHash;
    private long _ethCanonBlockNumber;
    private long _ethAnchorBlockNumber;
    private String _smartContractAddress;
    private int _ethCanonToTipLength;
    private long _btcAnchorBlockNumber;
    private long _ethLatestBlockNumber;
    private String _erc777ProxyContractAddress;

    @JsonProperty("eth_gas_price")
    long getEthGasPrice() { return _ethGasPrice; }

    @JsonProperty("eth_address")
    String getEthAddress() { return _ethAddress; }

    @JsonProperty("eth_tail_length")
    int getEthTailLength() { return _ethTailLength; }

    @JsonProperty("any_sender_nonce")
    long getAnySenderNonce() { return _anySenderNonce; }

    
    @JsonProperty("eth_account_nonce")
    long getEthAccountNonce() { return _ethAccountNonce; }

    @JsonProperty("eth_linker_hash")
    String getEthLinkerHash() { return _ethLinkerHash; }

    @JsonProperty("btc_safe_address")
    String getBtcSafeAddress() { return _btcSafeAddress; }

    @JsonProperty("eth_safe_address")
    String getEthSafeAddress() { return _ethSafeAddress; }

    @JsonProperty("eth_tail_block_hash")
    String getEthTailBlockHash() { return _ethTailBlockHash; }

    @JsonProperty("eth_tail_block_number")
    long getEthTailBlockNumber() { return _ethTailBlockNumber; }

    @JsonProperty("eth_canon_block_hash")
    String getEthCanonBlockHash() { return _ethCanonBlockHash; }

    @JsonProperty("eth_anchor_block_hash")
    String getEthAnchorBlockHash() { return _ethAnchorBlockHash; }

    @JsonProperty("eth_latest_block_hash")
    String getEthLatestBlockHash() { return _ethLatestBlockHash; }

    @JsonProperty("eth_canon_block_number")
    long getEthCanonBlockNumber() { return _ethCanonBlockNumber; }

    @JsonProperty("eth_anchor_block_number")
    long getEthAnchorBlockNumber() { return _ethAnchorBlockNumber; }

    @JsonProperty("smart_contract_address")
    String getSmartContractAddress() { return _smartContractAddress; }

    @JsonProperty("eth_canon_to_tip_length")
    int getEthCanonToTipLength() { return _ethCanonToTipLength; }

    @JsonProperty("eth_latest_block_number")
    long getEthLatestBlockNumber() { return _ethLatestBlockNumber; }

    @JsonProperty("erc777_proxy_contract_address")
    String getErc777ProxyContractAddress() { return _erc777ProxyContractAddress; }
    
    public void setEthGasPrice(long _ethGasPrice) {
        this._ethGasPrice = _ethGasPrice;
    }

    public void setBtcSafeAddress(String _btcSafeAddress) {
        this._btcSafeAddress = _btcSafeAddress;
    }

    public void setEthAddress(String _ethAddress) {
        this._ethAddress = _ethAddress;
    }

    public void setEthTailLength(int _ethTailLength) {
        this._ethTailLength = _ethTailLength;
    }

    public void setAnySenderNonce(long _anySenderNonce) {
        this._anySenderNonce = _anySenderNonce;
    }

    public void setEthAccountNonce(long _ethAccountNonce) {
        this._ethAccountNonce = _ethAccountNonce;
    }

    public void setEthLinkerHash(String _ethLinkerHash) {
        this._ethLinkerHash = _ethLinkerHash;
    }

    public void setEthSafeAddress(String _ethSafeAddress) {
        this._ethSafeAddress = _ethSafeAddress;
    }

    public void setEthTailBlockHash(String _ethTailBlockHash) {
        this._ethTailBlockHash = _ethTailBlockHash;
    }

    public void setEthTailBlockNumber(long _ethTailBlockNumber) {
        this._ethTailBlockNumber = _ethTailBlockNumber;
    }

    public void setEthCanonBlockHash(String _ethCanonBlockHash) {
        this._ethCanonBlockHash = _ethCanonBlockHash;
    }

    public void setEthAnchorBlockHash(String _ethAnchorBlockHash) {
        this._ethAnchorBlockHash = _ethAnchorBlockHash;
    }

    public void setEthLatestBlockHash(String _ethLatestBlockHash) {
        this._ethLatestBlockHash = _ethLatestBlockHash;
    }

    public void setEthCanonBlockNumber(long _ethCanonBlockNumber) {
        this._ethCanonBlockNumber = _ethCanonBlockNumber;
    }

    public void setEthAnchorBlockNumber(long _ethAnchorBlockNumber) {
        this._ethAnchorBlockNumber = _ethAnchorBlockNumber;
    }

    public void setSmartContractAddress(String _smartContractAddress) {
        this._smartContractAddress = _smartContractAddress;
    }

    public void setEthCanonToTipLength(int _ethCanonToTipLength) {
        this._ethCanonToTipLength = _ethCanonToTipLength;
    }

    public void setEthLatestBlockNumber(long _ethLatestBlockNumber) {
        this._ethLatestBlockNumber = _ethLatestBlockNumber;
    }

    public void setErc777ProxyContractAddress(String _erc777ProxyContractAddress) {
        this._erc777ProxyContractAddress = _erc777ProxyContractAddress;
    }

}

