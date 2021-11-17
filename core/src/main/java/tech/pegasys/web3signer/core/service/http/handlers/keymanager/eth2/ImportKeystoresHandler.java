package tech.pegasys.web3signer.core.service.http.handlers.keymanager.eth2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.pegasys.web3signer.core.signing.ArtifactSignerProvider;
import tech.pegasys.web3signer.core.signing.KeyType;
import tech.pegasys.web3signer.slashingprotection.SlashingProtection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static tech.pegasys.web3signer.core.service.http.handlers.ContentTypes.JSON_UTF_8;

public class ImportKeystoresHandler implements Handler<RoutingContext> {

  private static final Logger LOG = LogManager.getLogger();
  public static final int BAD_REQUEST = 400;
  private static final ObjectMapper YAML_OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

  private final ObjectMapper objectMapper;
  private final Path keystorePath;
  private final Optional<SlashingProtection> slashingProtection;
  private final ArtifactSignerProvider artifactSignerProvider;

  public ImportKeystoresHandler(final ObjectMapper objectMapper,
                                final Path keystorePath,
                                final Optional<SlashingProtection> slashingProtection,
                                final ArtifactSignerProvider artifactSignerProvider) {
    this.objectMapper = objectMapper;
    this.keystorePath = keystorePath;
    this.slashingProtection = slashingProtection;
    this.artifactSignerProvider = artifactSignerProvider;
  }

  @Override
  public void handle(RoutingContext context) {
    final RequestParameters params = context.get("parsedParameters");
    final ImportKeystoresRequestBody parsedBody;
    try {
      parsedBody = parseRequestBody(params);
    } catch (final IllegalArgumentException | JsonProcessingException e) {
      handleInvalidRequest(context, e);
      return;
    }

    final Set<String> existingPubkeys = artifactSignerProvider.availableIdentifiers();
    final List<ImportKeystoreResult> results = new ArrayList<>();
    for (int i = 0; i < parsedBody.getKeystores().size(); i++) {
      try {
        final String jsonKeystoreData = parsedBody.getKeystores().get(i);
        final String password = parsedBody.getPasswords().get(i);
        final String pubkey = new JsonObject(jsonKeystoreData).getString("pubkey");
        if (existingPubkeys.contains(pubkey)) {
          results.add(new ImportKeystoreResult(ImportKeystoreStatus.DUPLICATE, "Pubkey already imported"));
        } else {
          createKeyStoreYamlFileAt(
              pubkey,
              jsonKeystoreData,
              password,
              KeyType.BLS // TODO check if it's always BLS format?
          );
          results.add(new ImportKeystoreResult(ImportKeystoreStatus.IMPORTED, "success"));
        }
      } catch (Exception e) {
        results.add(new ImportKeystoreResult(
            ImportKeystoreStatus.ERROR,
            "Error importing keystore:\n" + e.getMessage())
        );
      }
    }

    // TODO force reload right here via callback into runner
    if (slashingProtection.isPresent()) {
      try {
        // TODO prevent signing for the duration of the import
        final InputStream slashingProtectionData =
            new ByteArrayInputStream(parsedBody.getSlashingProtection().getBytes(StandardCharsets.UTF_8));
        slashingProtection.get().importData(slashingProtectionData);
      } catch (Exception e) {
        context.fail(500, e);
        return;
      }
    }

    try {
      context.response()
          .putHeader(CONTENT_TYPE, JSON_UTF_8)
          .setStatusCode(200)
          .end(objectMapper.writeValueAsString(new ImportKeystoresResponse(results)));
    } catch (JsonProcessingException e) {
      context.fail(500, e);
    }
  }

  private ImportKeystoresRequestBody parseRequestBody(final RequestParameters params)
      throws JsonProcessingException {
    final String body = params.body().toString();
    return objectMapper.readValue(body, ImportKeystoresRequestBody.class);
  }

  private void handleInvalidRequest(final RoutingContext routingContext, final Exception e) {
    LOG.info("Invalid import keystores request - " + routingContext.getBodyAsString(), e);
    routingContext.fail(BAD_REQUEST);
  }

  public void createKeyStoreYamlFileAt(
      final String fileName,
      final String jsonKeystoreData,
      final String password,
      final KeyType keyType) throws IOException {

    final Path yamlFile = keystorePath.resolve(fileName + ".yaml");

    final String keystoreFileName = fileName + ".json";
    final Path keystoreFile = yamlFile.getParent().resolve(keystoreFileName);
    createTextFile(keystoreFile, jsonKeystoreData);

    final String passwordFilename = fileName + ".password";
    final Path passwordFile = yamlFile.getParent().resolve(passwordFilename);
    createTextFile(passwordFile, password);

    final Map<String, String> signingMetadata = new HashMap<>();
    signingMetadata.put("type", "file-keystore");
    signingMetadata.put("keystoreFile", keystoreFile.toString());
    signingMetadata.put("keystorePasswordFile", passwordFile.toString());
    signingMetadata.put("keyType", keyType.name());
    createYamlFile(yamlFile, signingMetadata);
  }

  private void createTextFile(final Path keystoreFile, final String jsonKeystoreData) throws IOException {
    Files.writeString(keystoreFile, jsonKeystoreData, StandardCharsets.UTF_8);
  }

  private void createYamlFile(final Path filePath,
                              final Map<String, ? extends Serializable> signingMetadata) throws IOException {
    YAML_OBJECT_MAPPER.writeValue(filePath.toFile(), signingMetadata);
  }

}
