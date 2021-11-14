package tech.pegasys.web3signer.core.service.http.handlers.keymanager.eth2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ImportKeystoresRequestBody {
  private final List<Object> keystores;
  private final List<String> passwords;
  private final String slashingProtection;

  @JsonCreator
  ImportKeystoresRequestBody(
      @JsonProperty("keystores") final List<Object> keystores,
      @JsonProperty("passwords") final List<String> passwords,
      @JsonProperty("slashing_protection") final String slashingProtection) {
    this.keystores = keystores;
    this.passwords = passwords;
    this.slashingProtection = slashingProtection;
  }

  @JsonProperty("keystores")
  public List<Object> getKeystores() {
    return keystores;
  }

  @JsonProperty("passwords")
  public List<String> getPasswords() {
    return passwords;
  }

  @JsonProperty("slashing_protection")
  public String getSlashingProtection() {
    return slashingProtection;
  }
}