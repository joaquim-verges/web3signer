package tech.pegasys.web3signer.core.service.http.handlers.keymanager.eth2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ImportKeystoreResult {
  private final ImportKeystoreStatus status;
  private final String message;

  @JsonCreator
  public ImportKeystoreResult(
      @JsonProperty("status") final ImportKeystoreStatus status,
      @JsonProperty("message") final String message) {
    this.status = status;
    this.message = message;
  }

  @JsonProperty("status")
  public ImportKeystoreStatus getStatus() {
    return status;
  }

  @JsonProperty("message")
  public String getMessage() {
    return message;
  }
}