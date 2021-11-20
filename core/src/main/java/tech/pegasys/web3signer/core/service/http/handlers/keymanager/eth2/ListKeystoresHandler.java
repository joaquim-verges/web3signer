package tech.pegasys.web3signer.core.service.http.handlers.keymanager.eth2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import tech.pegasys.web3signer.core.signing.ArtifactSignerProvider;

import java.util.List;
import java.util.stream.Collectors;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static tech.pegasys.web3signer.core.service.http.handlers.ContentTypes.JSON_UTF_8;

public class ListKeystoresHandler implements Handler<RoutingContext> {
  private final ArtifactSignerProvider artifactSignerProvider;
  private final ObjectMapper objectMapper;

  public ListKeystoresHandler(final ArtifactSignerProvider artifactSignerProvider,
                              final ObjectMapper objectMapper) {
    this.artifactSignerProvider = artifactSignerProvider;
    this.objectMapper = objectMapper;
  }

  @Override
  public void handle(final RoutingContext context) {
    // TODO should this only return BLS type keys?
    // TODO include derivation path when available (requires some plumbing to expose it from the artifactSignerProvider)
    // TODO readonly = true for non API imported keys - add metadata to artifactSignerProvider.availableIdentifiers()
    final List<KeystoreInfo> data = artifactSignerProvider.availableIdentifiers()
        .stream()
        .map(key -> new KeystoreInfo(key, null, false))
        .collect(Collectors.toList());
    // TODO cross match with matching pubkeys in specific BLS folder
    // TODO if match -> readOnly = false otherwise true
    final ListKeystoresResponse response = new ListKeystoresResponse(data);
    try {
      context.response().putHeader(CONTENT_TYPE, JSON_UTF_8).end(objectMapper.writeValueAsString(response));
    } catch (JsonProcessingException e) {
      context.fail(500, e);
    }
  }
}
