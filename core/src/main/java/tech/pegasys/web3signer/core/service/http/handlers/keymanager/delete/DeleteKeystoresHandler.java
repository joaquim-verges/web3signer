package tech.pegasys.web3signer.core.service.http.handlers.keymanager.delete;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.pegasys.web3signer.core.signing.ArtifactSignerProvider;
import tech.pegasys.web3signer.slashingprotection.SlashingProtection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static tech.pegasys.web3signer.core.service.http.handlers.ContentTypes.JSON_UTF_8;

public class DeleteKeystoresHandler implements Handler<RoutingContext> {

  private static final Logger LOG = LogManager.getLogger();
  public static final int SUCCESS = 200;
  public static final int BAD_REQUEST = 400;
  public static final int SERVER_ERROR = 500;

  private final ObjectMapper objectMapper;
  private final Path keystorePath;
  private final Optional<SlashingProtection> slashingProtection;
  private final ArtifactSignerProvider signerProvider;

  public DeleteKeystoresHandler(final ObjectMapper objectMapper,
                                final Path keystorePath,
                                final Optional<SlashingProtection> slashingProtection,
                                final ArtifactSignerProvider signerProvider) {
    this.objectMapper = objectMapper;
    this.keystorePath = keystorePath;
    this.slashingProtection = slashingProtection;
    this.signerProvider = signerProvider;
  }

  @Override
  public void handle(RoutingContext context) {
    final RequestParameters params = context.get("parsedParameters");
    final DeleteKeystoresRequestBody parsedBody;
    try {
      parsedBody = parseRequestBody(params);
    } catch (final IllegalArgumentException | JsonProcessingException e) {
      handleInvalidRequest(context, e);
      return;
    }

    List<String> pubkeysToDelete = parsedBody.getPubkeys();
    List<DeleteKeystoreResult> results = new ArrayList<>();
    for (String pubkey : pubkeysToDelete) {
      try {
        // Remove key from memory
        // TODO check that other validators are not using this key as well?
        signerProvider.removeSigner(pubkey);
        // Delete corresponding keystore file
        // TODO inspect inside the file and match the pubkey
        final boolean deleted = Files.deleteIfExists(keystorePath.resolve(pubkey + ".yaml"));
        results.add(new DeleteKeystoreResult(
            deleted ? DeleteKeystoreStatus.DELETED : DeleteKeystoreStatus.NOT_FOUND,
            ""));
      } catch (IOException e) {
        results.add(new DeleteKeystoreResult(
            DeleteKeystoreStatus.ERROR,
            "Error deleting keystore file: " + e.getMessage()));
      }
    }

    String slashingProtectionExport = null;
    if (slashingProtection.isPresent()) {
      try {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        slashingProtection.get().export(outputStream);
        slashingProtectionExport = outputStream.toString(StandardCharsets.UTF_8);
      } catch (Exception e) {
        LOG.debug("Failed to export slashing data", e);
        context.fail(SERVER_ERROR, e);
        return;
      }
    }

    try {
      context.response()
          .putHeader(CONTENT_TYPE, JSON_UTF_8)
          .setStatusCode(SUCCESS)
          .end(objectMapper.writeValueAsString(new DeleteKeystoresResponse(results, slashingProtectionExport)));
    } catch (JsonProcessingException e) {
      context.fail(SERVER_ERROR, e);
    }

  }

  private DeleteKeystoresRequestBody parseRequestBody(final RequestParameters params)
      throws JsonProcessingException {
    final String body = params.body().toString();
    return objectMapper.readValue(body, DeleteKeystoresRequestBody.class);
  }

  private void handleInvalidRequest(final RoutingContext routingContext, final Exception e) {
    LOG.debug("Invalid delete keystores request - " + routingContext.getBodyAsString(), e);
    routingContext.fail(BAD_REQUEST);
  }
}