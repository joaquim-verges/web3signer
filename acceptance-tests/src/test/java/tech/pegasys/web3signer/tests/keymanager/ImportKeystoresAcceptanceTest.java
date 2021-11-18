package tech.pegasys.web3signer.tests.keymanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.tuweni.bytes.Bytes;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import tech.pegasys.signers.bls.keystore.KeyStore;
import tech.pegasys.signers.bls.keystore.KeyStoreLoader;
import tech.pegasys.signers.bls.keystore.model.KeyStoreData;
import tech.pegasys.web3signer.core.service.http.handlers.keymanager.eth2.ImportKeystoresRequestBody;
import tech.pegasys.web3signer.core.signing.KeyType;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;

public class ImportKeystoresAcceptanceTest extends KeyManagerTestBase {

  @Test
  public void invalidRequestBodyReturnsError() {
    setupSignerWithKeyManagerApi();
    final Response response = callImportKeystores("{\"invalid\": \"json body\"}");
    response
        .then()
        .assertThat()
        .statusCode(400);
  }

  @Test
  public void validRequestBodyReturnsSuccess() throws IOException, URISyntaxException {
    setupSignerWithKeyManagerApi();
    final Response response = callImportKeystores(composeRequestBody());
    response
        .then()
        .contentType(ContentType.JSON)
        .assertThat()
        .statusCode(200)
        .body("data.status", hasItem("IMPORTED"));
  }

  @Test
  public void existingKeyReturnsDuplicate() throws IOException, URISyntaxException {
    final Path keystoreFile = Path.of(
        new File(Resources.getResource("eth2/bls_keystore.json").toURI()).getAbsolutePath()
    );
    final KeyStoreData keyStoreData = KeyStoreLoader.loadFromFile(keystoreFile);
    String password = "somepassword";
    final Bytes privateKey = KeyStore.decrypt(password, keyStoreData);
    createBlsKeys(true, privateKey.toHexString());


    setupSignerWithKeyManagerApi();

    assertThat(signer.listPublicKeys(KeyType.BLS).size()).isEqualTo(1);

    final Response response = callImportKeystores(composeRequestBody());
    response
        .then()
        .contentType(ContentType.JSON)
        .assertThat()
        .statusCode(200)
        .body("data.status", hasItem("DUPLICATE"));

    assertThat(signer.listPublicKeys(KeyType.BLS).size()).isEqualTo(1);
  }

  @Test
  public void importAndReloadReturnsNewKeys() throws IOException, URISyntaxException {
    setupSignerWithKeyManagerApi();
    assertThat(signer.listPublicKeys(KeyType.BLS).size()).isEqualTo(0);

    callImportKeystores(composeRequestBody()).then().statusCode(200);

    assertThat(signer.listPublicKeys(KeyType.BLS).size()).isEqualTo(1);
    assertThat(signer.listPublicKeys(KeyType.BLS).get(0))
        .isEqualTo("0x98d083489b3b06b8740da2dfec5cc3c01b2086363fe023a9d7dc1f907633b1ff11f7b99b19e0533e969862270061d884");
  }

  @Test
  public void testRequestBodyParsing() throws IOException, URISyntaxException {
    final ObjectMapper objectMapper = new ObjectMapper();
    final ImportKeystoresRequestBody parsedBody =
        objectMapper.readValue(composeRequestBody(), ImportKeystoresRequestBody.class);
    assertThat(new JsonObject(parsedBody.getKeystores().get(0)).getInteger("version")).isEqualTo(4);
    assertThat(new JsonObject(parsedBody.getKeystores().get(0)).getString("pubkey"))
        .isEqualTo("98d083489b3b06b8740da2dfec5cc3c01b2086363fe023a9d7dc1f907633b1ff11f7b99b19e0533e969862270061d884");
    assertThat(parsedBody.getPasswords().get(0)).isEqualTo("somepassword");
    assertThat(new JsonObject(parsedBody.getSlashingProtection())
        .getJsonArray("data")
        .getJsonObject(0)
        .getString("pubkey")
    ).isEqualTo("0x8f3f44b74d316c3293cced0c48c72e021ef8d145d136f2908931090e7181c3b777498128a348d07b0b9cd3921b5ca537");
  }

  private String composeRequestBody() throws IOException, URISyntaxException {
    String keystoreData = readFile("eth2/bls_keystore.json");
    String password = "somepassword";
    String slashingProtectionData = readFile("slashing/slashingImport.json");
    final JsonObject requestBody = new JsonObject()
        .put("keystores", new JsonArray().add(keystoreData))
        .put("passwords", new JsonArray().add(password))
        .put("slashing_protection", slashingProtectionData);
    return requestBody.toString();
  }
}
