package tech.pegasys.web3signer.core.service.http.handlers.keymanager.eth2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ListKeystoresResponse {
  private final List<KeystoreInfo> data;

  @JsonCreator
  public ListKeystoresResponse(@JsonProperty("data") List<KeystoreInfo> data) {
    this.data = data;
  }

  @JsonProperty("data")
  public List<KeystoreInfo> getData() {
    return data;
  }
}
