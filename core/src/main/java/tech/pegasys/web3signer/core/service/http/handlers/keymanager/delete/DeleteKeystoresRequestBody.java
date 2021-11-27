package tech.pegasys.web3signer.core.service.http.handlers.keymanager.delete;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DeleteKeystoresRequestBody {
  private final List<String> pubkeys;

  @JsonCreator
  public DeleteKeystoresRequestBody(
      @JsonProperty("pubkeys") List<String> pubkeys) {
    this.pubkeys = pubkeys;
  }

  @JsonProperty("pubkeys")
  public List<String> getPubkeys() {
    return pubkeys;
  }
}
