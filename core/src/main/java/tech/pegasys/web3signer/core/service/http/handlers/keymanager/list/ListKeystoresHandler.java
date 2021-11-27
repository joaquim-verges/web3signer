package tech.pegasys.web3signer.core.service.http.handlers.keymanager.list;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import tech.pegasys.web3signer.core.multikey.metadata.SignerOrigin;
import tech.pegasys.web3signer.core.signing.ArtifactSignerProvider;
import tech.pegasys.web3signer.core.signing.BlsArtifactSigner;

import java.util.List;
import java.util.stream.Collectors;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static tech.pegasys.web3signer.core.service.http.handlers.ContentTypes.JSON_UTF_8;

public class ListKeystoresHandler implements Handler<RoutingContext> {
  public static final int SUCCESS = 200;
  public static final int SERVER_ERROR = 500;

  private final ArtifactSignerProvider artifactSignerProvider;
  private final ObjectMapper objectMapper;

  public ListKeystoresHandler(final ArtifactSignerProvider artifactSignerProvider,
                              final ObjectMapper objectMapper) {
    this.artifactSignerProvider = artifactSignerProvider;
    this.objectMapper = objectMapper;
  }

  @Override
  public void handle(final RoutingContext context) {
    final List<KeystoreInfo> data = artifactSignerProvider.availableIdentifiers()
        .stream()
        .map(artifactSignerProvider::getSigner)
        .filter(signer -> signer.isPresent() && signer.get() instanceof BlsArtifactSigner)
        .map(signer -> (BlsArtifactSigner) signer.get())
        .map(signer -> new KeystoreInfo(signer.getIdentifier(), null, isReadOnly(signer)))
        .collect(Collectors.toList());
    final ListKeystoresResponse response = new ListKeystoresResponse(data);
    try {
      context.response().putHeader(CONTENT_TYPE, JSON_UTF_8)
          .setStatusCode(SUCCESS)
          .end(objectMapper.writeValueAsString(response));
    } catch (JsonProcessingException e) {
      context.fail(SERVER_ERROR, e);
    }
  }

  // only signers loaded from key store files are editable, everything else is read only
  private boolean isReadOnly(BlsArtifactSigner signer) {
    return signer.getOrigin() != SignerOrigin.FILE_KEYSTORE;
  }
}
