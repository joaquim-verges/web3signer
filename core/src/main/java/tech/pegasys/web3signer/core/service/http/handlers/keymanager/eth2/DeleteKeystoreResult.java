package tech.pegasys.web3signer.core.service.http.handlers.keymanager.eth2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DeleteKeystoreResult {
    private final DeleteKeystoreStatus status;
    private final String message;

    @JsonCreator
    public DeleteKeystoreResult(
            @JsonProperty("status") DeleteKeystoreStatus status,
            @JsonProperty("message") String message) {
        this.status = status;
        this.message = message;
    }

    @JsonProperty("status")
    public DeleteKeystoreStatus getStatus() {
        return status;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }
}
