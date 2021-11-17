package tech.pegasys.web3signer.tests.keymanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import tech.pegasys.web3signer.core.service.http.handlers.keymanager.eth2.ImportKeystoresRequestBody;

import java.io.IOException;
import java.net.URISyntaxException;

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
    // TODO validate response
  }

  @Test
  public void importAndReloadReturnsNewKeys() throws IOException, URISyntaxException {
    setupSignerWithKeyManagerApi();

    callImportKeystores(composeRequestBody()).then().statusCode(200);
    signer.callReload().then().statusCode(200);

    // reload is async
    Awaitility.await()
        .atMost(5, SECONDS)
        .untilAsserted(() ->
            validateApiResponse(
                callListKeys(),
                "data.validating_pubkey",
                hasItem("0x98d083489b3b06b8740da2dfec5cc3c01b2086363fe023a9d7dc1f907633b1ff11f7b99b19e0533e969862270061d884")
            )
        );
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
