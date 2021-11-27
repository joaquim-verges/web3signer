package tech.pegasys.web3signer.core.service.http.handlers.keymanager.delete;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DeleteKeystoresResponse {
  private final List<DeleteKeystoreResult> data;
  private final String slashingProtection;

  @JsonCreator
  public DeleteKeystoresResponse(
      @JsonProperty("data") List<DeleteKeystoreResult> data,
      @JsonProperty("slashing_protection") String slashingProtection) {
    this.data = data;
    this.slashingProtection = slashingProtection;
  }

  @JsonProperty("data")
  public List<DeleteKeystoreResult> getData() {
    return data;
  }

  @JsonProperty("slashing_protection")
  public String getSlashingProtection() {
    return slashingProtection;
  }
}
