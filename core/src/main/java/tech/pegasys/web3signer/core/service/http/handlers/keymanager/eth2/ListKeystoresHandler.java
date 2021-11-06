package tech.pegasys.web3signer.core.service.http.handlers.keymanager.eth2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import tech.pegasys.web3signer.core.signing.ArtifactSignerProvider;

import java.util.ArrayList;
import java.util.List;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static tech.pegasys.web3signer.core.service.http.handlers.ContentTypes.JSON_UTF_8;

public class ListKeystoresHandler implements Handler<RoutingContext> {
    private final ArtifactSignerProvider artifactSignerProvider;

    public ListKeystoresHandler(final ArtifactSignerProvider artifactSignerProvider) {
        this.artifactSignerProvider = artifactSignerProvider;
    }

    @Override
    public void handle(final RoutingContext context) {
        final List<String> availableIdentifiers = new ArrayList<>(artifactSignerProvider.availableIdentifiers());
        // TODO format response, pull derivation path + readonly status
        // [ { validating_pubkey: "", derivation_path: "", readonly: true/false } ]
        final String jsonEncodedKeys = new JsonArray(availableIdentifiers).encode();
        context.response().putHeader(CONTENT_TYPE, JSON_UTF_8).end(jsonEncodedKeys);
    }

    class KeystoreInfo {
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
}
