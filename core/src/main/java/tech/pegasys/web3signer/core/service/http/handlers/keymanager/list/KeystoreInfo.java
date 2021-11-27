package tech.pegasys.web3signer.core.service.http.handlers.keymanager.list;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class KeystoreInfo {
  private final String validatingPubkey;
  private final String derivationPath;
  private final boolean readOnly;

  @JsonCreator
  public KeystoreInfo(
      @JsonProperty("validating_pubkey") final String validatingPubkey,
      @JsonProperty("derivation_path") final String derivationPath,
      @JsonProperty("readonly") final boolean readOnly) {
    this.validatingPubkey = validatingPubkey;
    this.derivationPath = derivationPath;
    this.readOnly = readOnly;
  }

  @JsonProperty("validating_pubkey")
  public String getValidatingPubkey() {
    return validatingPubkey;
  }

  @JsonProperty("derivation_path")
  public String getDerivationPath() {
    return derivationPath;
  }

  @JsonProperty("readonly")
  public boolean isReadOnly() {
    return readOnly;
  }
}