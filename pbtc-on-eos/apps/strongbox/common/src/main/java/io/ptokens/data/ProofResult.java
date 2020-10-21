package io.ptokens.data;

public class ProofResult {
    public String type;
    public byte version;
    public byte[] commitment;
    public byte[] signedCommitment;
    public int commitmentTimestamp;
    public String safetyNetAttestation;
    public String certificateAttestation;

    public ProofResult(
            String type,
            byte version,
            byte[] commitment,
            byte[] signedCommitment,
            int commitmentTimestamp,
            String safetyNetAttestation,
            String certificateAttestation
    ) {
        this.type = type;
        this.version = version;
        this.commitment = commitment;
        this.signedCommitment = signedCommitment;
        this.commitmentTimestamp = commitmentTimestamp;
        this.safetyNetAttestation = safetyNetAttestation;
        this.certificateAttestation = certificateAttestation;
    }

}
