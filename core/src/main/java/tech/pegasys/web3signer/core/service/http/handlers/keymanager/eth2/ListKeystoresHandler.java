package tech.pegasys.web3signer.core.service.http.handlers.keymanager.eth2;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import tech.pegasys.web3signer.core.signing.ArtifactSignerProvider;

import java.util.List;
import java.util.stream.Collectors;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static tech.pegasys.web3signer.core.service.http.handlers.ContentTypes.JSON_UTF_8;

public class ListKeystoresHandler implements Handler<RoutingContext> {
    private final ArtifactSignerProvider artifactSignerProvider;

    public ListKeystoresHandler(final ArtifactSignerProvider artifactSignerProvider) {
        this.artifactSignerProvider = artifactSignerProvider;
    }

    @Override
    public void handle(final RoutingContext context) {
        // TODO include derivation path when available (requires some plumbing)
        final List<KeystoreInfo> data = artifactSignerProvider.availableIdentifiers()
                .stream()
                .map(key -> new KeystoreInfo(key, null, false))
                .collect(Collectors.toList());
        final String jsonEncodedKeys = new JsonArray(data).encode();
        context.response().putHeader(CONTENT_TYPE, JSON_UTF_8).end(jsonEncodedKeys);
    }
}
