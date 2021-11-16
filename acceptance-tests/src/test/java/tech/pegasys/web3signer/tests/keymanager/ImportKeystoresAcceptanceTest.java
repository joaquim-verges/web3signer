package tech.pegasys.web3signer.tests.keymanager;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

public class ImportKeystoresAcceptanceTest extends KeyManagerTestBase {

  private static final String VALID_REQUEST_BODY =
      "{\n" +
      "  \"keystores\": [\n" +
      "    \"{\\\"version\\\":4,\\\"uuid\\\":\\\"9f75a3fa-1e5a-49f9-be3d-f5a19779c6fa\\\",\\\"path\\\":\\\"m/12381/3600/0/0/0\\\",\\\"pubkey\\\":\\\"0x93247f2209abcacf57b75a51dafae777f9dd38bc7053d1af526f220a7489a6d3a2753e5f3e8b1cfe39b56f43611df74a\\\",\\\"crypto\\\":{\\\"kdf\\\":{\\\"function\\\":\\\"pbkdf2\\\",\\\"params\\\":{\\\"dklen\\\":32,\\\"c\\\":262144,\\\"prf\\\":\\\"hmac-sha256\\\",\\\"salt\\\":\\\"8ff8f22ef522a40f99c6ce07fdcfc1db489d54dfbc6ec35613edf5d836fa1407\\\"},\\\"message\\\":\\\"\\\"},\\\"checksum\\\":{\\\"function\\\":\\\"sha256\\\",\\\"params\\\":{},\\\"message\\\":\\\"9678a69833d2576e3461dd5fa80f6ac73935ae30d69d07659a709b3cd3eddbe3\\\"},\\\"cipher\\\":{\\\"function\\\":\\\"aes-128-ctr\\\",\\\"params\\\":{\\\"iv\\\":\\\"31b69f0ac97261e44141b26aa0da693f\\\"},\\\"message\\\":\\\"e8228bafec4fcbaca3b827e586daad381d53339155b034e5eaae676b715ab05e\\\"}}}\"\n" +
      "  ],\n" +
      "  \"passwords\": [\n" +
      "    \"ABCDEFGH01234567ABCDEFGH01234567\"\n" +
      "  ],\n" +
      "  \"slashing_protection\": \"{\\\"metadata\\\":{\\\"interchange_format_version\\\":\\\"5\\\",\\\"genesis_validators_root\\\":\\\"0xcf8e0d4e9587369b2301d0790347320302cc0943d5a1884560367e8208d920f2\\\"},\\\"data\\\":[{\\\"pubkey\\\":\\\"0x93247f2209abcacf57b75a51dafae777f9dd38bc7053d1af526f220a7489a6d3a2753e5f3e8b1cfe39b56f43611df74a\\\",\\\"signed_blocks\\\":[],\\\"signed_attestations\\\":[]}]}\"\n" +
      "}";

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
  public void validRequestBodyReturnsSuccess() {
    setupSignerWithKeyManagerApi();
    final Response response = callImportKeystores(VALID_REQUEST_BODY);
    response
        .then()
        .assertThat()
        .statusCode(200);
  }
}
