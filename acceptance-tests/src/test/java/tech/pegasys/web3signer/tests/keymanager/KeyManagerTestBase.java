package tech.pegasys.web3signer.tests.keymanager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.tuweni.bytes.Bytes32;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.io.TempDir;
import tech.pegasys.teku.bls.BLSKeyPair;
import tech.pegasys.teku.bls.BLSSecretKey;
import tech.pegasys.web3signer.core.service.http.SigningObjectMapperFactory;
import tech.pegasys.web3signer.dsl.signer.SignerConfigurationBuilder;
import tech.pegasys.web3signer.dsl.utils.MetadataFileHelpers;
import tech.pegasys.web3signer.tests.AcceptanceTestBase;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static tech.pegasys.web3signer.core.signing.KeyType.BLS;

public class KeyManagerTestBase extends AcceptanceTestBase {
  private static final String KEYSTORE_ENDPOINT = "/eth/v1/keystores";
  private static final Long MINIMAL_ALTAIR_FORK = 0L;
  protected static final MetadataFileHelpers metadataFileHelpers = new MetadataFileHelpers();
  protected static final ObjectMapper objectMapper = SigningObjectMapperFactory.createObjectMapper();

  protected @TempDir Path testDirectory;

  protected void setupSignerWithKeyManagerApi() {
    final SignerConfigurationBuilder builder = new SignerConfigurationBuilder();
    builder
        .withKeyStoreDirectory(testDirectory)
        .withMode("eth2")
        .withAltairForkEpoch(MINIMAL_ALTAIR_FORK)
        .withKeyManagerApiEnabled(true);
    startSigner(builder.build());
  }

  public Response callListKeys() {
    return given().baseUri(signer.getUrl()).get(KEYSTORE_ENDPOINT);
  }

  protected String toJson(final Object object) throws JsonProcessingException {
    return objectMapper.writeValueAsString(object);
  }

  protected void validateApiResponse(final Response response, final Matcher<?> matcher) {
    response.then().statusCode(200).contentType(ContentType.JSON).body("", matcher);
  }

  protected String[] createBlsKeys(boolean isValid, final String... privateKeys) {
    return Stream.of(privateKeys)
        .map(
            privateKey -> {
              final BLSKeyPair keyPair =
                  new BLSKeyPair(BLSSecretKey.fromBytes(Bytes32.fromHexString(privateKey)));
              final String publicKey = keyPair.getPublicKey().toString();
              final Path keyConfigFile = testDirectory.resolve(publicKey + ".yaml");
              if (isValid) {
                metadataFileHelpers.createUnencryptedYamlFileAt(keyConfigFile, privateKey, BLS);
              } else {
                createInvalidFile(keyConfigFile);
              }
              return publicKey;
            })
        .toArray(String[]::new);
  }

  private void createInvalidFile(final Path keyConfigFile) {
    try {
      Files.createFile(keyConfigFile);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
