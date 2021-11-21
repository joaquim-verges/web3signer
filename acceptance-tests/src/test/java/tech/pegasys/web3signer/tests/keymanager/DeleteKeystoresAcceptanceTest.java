package tech.pegasys.web3signer.tests.keymanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;
import tech.pegasys.web3signer.core.service.http.handlers.keymanager.eth2.DeleteKeystoresRequestBody;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;

public class DeleteKeystoresAcceptanceTest extends KeyManagerTestBase {

  @Test
  public void invalidRequestBodyReturnsError() {
    setupSignerWithKeyManagerApi();
    final Response response = callDeleteKeystores("{\"invalid\": \"json body\"}");
    response
        .then()
        .assertThat()
        .statusCode(400);
  }

  @Test
  public void deletingNonExistingKeyReturnNotFound() {
    setupSignerWithKeyManagerApi();
    final Response response = callDeleteKeystores(composeRequestBody());
    response
        .then()
        .contentType(ContentType.JSON)
        .assertThat()
        .statusCode(200)
        .body("data.status", hasItem("NOT_FOUND"));
  }

  @Test
  public void deletingExistingKeyReturnDeleted() throws URISyntaxException {
    createBlsKey("eth2/bls_keystore.json", "somepassword");
    setupSignerWithKeyManagerApi();
    final Response response = callDeleteKeystores(composeRequestBody());
    response
        .then()
        .contentType(ContentType.JSON)
        .assertThat()
        .statusCode(200)
        .body("data.status", hasItem("DELETED"));
  }

  // TODO slashing protection related tests

  @Test
  public void testRequestBodyParsing() throws IOException {
    final ObjectMapper objectMapper = new ObjectMapper();
    final DeleteKeystoresRequestBody parsedBody =
        objectMapper.readValue(composeRequestBody(), DeleteKeystoresRequestBody.class);
    assertThat(parsedBody.getPubkeys().get(0))
        .isEqualTo("0x98d083489b3b06b8740da2dfec5cc3c01b2086363fe023a9d7dc1f907633b1ff11f7b99b19e0533e969862270061d884");
  }

  private String composeRequestBody() {
    final JsonObject requestBody = new JsonObject()
        .put("pubkeys", new JsonArray()
            .add("0x98d083489b3b06b8740da2dfec5cc3c01b2086363fe023a9d7dc1f907633b1ff11f7b99b19e0533e969862270061d884"));
    return requestBody.toString();
  }
}
