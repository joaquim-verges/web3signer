package tech.pegasys.web3signer.tests.keymanager;

import com.google.common.io.Resources;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.io.TempDir;
import tech.pegasys.signers.bls.keystore.KeyStore;
import tech.pegasys.signers.bls.keystore.KeyStoreLoader;
import tech.pegasys.signers.bls.keystore.model.KdfFunction;
import tech.pegasys.signers.bls.keystore.model.KeyStoreData;
import tech.pegasys.teku.bls.BLSKeyPair;
import tech.pegasys.teku.bls.BLSPublicKey;
import tech.pegasys.teku.bls.BLSSecretKey;
import tech.pegasys.web3signer.dsl.signer.SignerConfigurationBuilder;
import tech.pegasys.web3signer.dsl.utils.MetadataFileHelpers;
import tech.pegasys.web3signer.tests.AcceptanceTestBase;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;
import static tech.pegasys.web3signer.core.signing.KeyType.BLS;

public class KeyManagerTestBase extends AcceptanceTestBase {
  private static final String KEYSTORE_ENDPOINT = "/eth/v1/keystores";
  private static final Long MINIMAL_ALTAIR_FORK = 0L;
  protected static final MetadataFileHelpers metadataFileHelpers = new MetadataFileHelpers();

  @TempDir
  protected Path testDirectory;

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

  public Response callImportKeystores(final String body) {
    return given().baseUri(signer.getUrl()).contentType(ContentType.JSON).body(body).post(KEYSTORE_ENDPOINT);
  }

  public Response callDeleteKeystores(final String body) {
    return given().baseUri(signer.getUrl()).contentType(ContentType.JSON).body(body).delete(KEYSTORE_ENDPOINT);
  }

  protected void validateApiResponse(final Response response, final String path, final Matcher<?> matcher) {
    response.then().statusCode(200).contentType(ContentType.JSON).body(path, matcher);
  }

  protected void createBlsKey(String keystorePath, String password) throws URISyntaxException {
    final Path keystoreFile = Path.of(
        new File(Resources.getResource(keystorePath).toURI()).getAbsolutePath()
    );
    final KeyStoreData keyStoreData = KeyStoreLoader.loadFromFile(keystoreFile);
    final Bytes privateKey = KeyStore.decrypt(password, keyStoreData);
    createKeystoreYamlFile(privateKey.toHexString());
  }

  protected String createKeystoreYamlFile(final String privateKey) {
    final BLSSecretKey key =
        BLSSecretKey.fromBytes(Bytes32.fromHexString(privateKey));
    final BLSKeyPair keyPair = new BLSKeyPair(key);
    final BLSPublicKey publicKey = keyPair.getPublicKey();
    final String configFilename = publicKey.toString(); //.substring(2); TODO make everything work with and without 0x
    final Path keyConfigFile = testDirectory.resolve(configFilename + ".yaml");
    metadataFileHelpers.createKeyStoreYamlFileAt(keyConfigFile, keyPair, KdfFunction.PBKDF2);
    return publicKey.toString();
  }

  protected Path createRawPrivateKeyFile(final String privateKey) {
    final Path file = testDirectory.resolve(privateKey.hashCode() + ".yaml");
    metadataFileHelpers.createUnencryptedYamlFileAt(file, privateKey, BLS);
    return file;
  }

  protected String readFile(final String filePath) throws IOException, URISyntaxException {
    final Path keystoreFile = Path.of(
        new File(Resources.getResource(filePath).toURI()).getAbsolutePath()
    );
    return Files.readString(keystoreFile);
  }
}
