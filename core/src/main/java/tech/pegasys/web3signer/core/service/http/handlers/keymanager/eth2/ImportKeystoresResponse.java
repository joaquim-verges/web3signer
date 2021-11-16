package tech.pegasys.web3signer.core.service.http.handlers.keymanager.eth2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ImportKeystoresResponse {
  private final List<ImportKeystoreResult> data;

  @JsonCreator
  public ImportKeystoresResponse(@JsonProperty("data") List<ImportKeystoreResult> data) {
    this.data = data;
  }

  @JsonProperty("data")
  public List<ImportKeystoreResult> getData() {
    return data;
  }
}
