package io.ptokens.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EnclaveState {
    private boolean _debugMode;
    private String _dbKeyPrefix;
    private boolean _coreIsValidating;

    @JsonProperty("debug_mode")
    boolean getDebugMode() { return _debugMode; }

    @JsonProperty("db_key_prefix")
    String getDbKeyPrefix() { return _dbKeyPrefix; }

    @JsonProperty("core_is_validating")
    boolean getCoreIsValidating() { return _coreIsValidating; }

    public void setDebugMode(boolean _debugMode) {
        this._debugMode = _debugMode;
    }

    public void setDbKeyPrefix(String _dbKeyPrefix) {
        this._dbKeyPrefix = _dbKeyPrefix;
    }
    
    public void setCoreIsValidating(boolean _coreIsValidating) {
        this._coreIsValidating = _coreIsValidating;
    }
}
