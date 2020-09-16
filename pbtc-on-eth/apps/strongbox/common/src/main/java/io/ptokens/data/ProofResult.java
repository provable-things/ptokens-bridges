package io.ptokens.data;

public class ProofResult {
    public String type;
    public byte version;
    public byte[] commitment;
    public int commitmentTimestamp;
    public String safetyNetAttestation;
    public String certificateAttestation;

    public ProofResult(
            String type,
            byte version,
            byte[] commitment,
            int commitmentTimestamp,
            String safetyNetAttestation,
            String certificateAttestation
    ) {
        this.type = type;
        this.version = version;
        this.commitment = commitment;
        this.commitmentTimestamp = commitmentTimestamp;
        this.safetyNetAttestation = safetyNetAttestation;
        this.certificateAttestation = certificateAttestation;
    }

}
