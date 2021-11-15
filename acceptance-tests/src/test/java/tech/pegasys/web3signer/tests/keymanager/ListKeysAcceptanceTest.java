package tech.pegasys.web3signer.tests.keymanager;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import tech.pegasys.web3signer.core.service.http.handlers.keymanager.eth2.KeystoreInfo;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.empty;

public class ListKeysAcceptanceTest extends KeyManagerTestBase {
  private static final String BLS_PRIVATE_KEY_1 =
      "3ee2224386c82ffea477e2adf28a2929f5c349165a4196158c7f3a2ecca40f35";
  private static final String BLS_PRIVATE_KEY_2 =
      "32ae313afff2daa2ef7005a7f834bdf291855608fe82c24d30be6ac2017093a8";
  private static final String[] PRIVATE_KEYS = new String[] {BLS_PRIVATE_KEY_1, BLS_PRIVATE_KEY_2};

  @Test
  public void noLoadedKeysReturnsEmptyPublicKeyResponse() {
    setupSignerWithKeyManagerApi();
    validateApiResponse(callListKeys(), empty());
  }

  @Test
  public void invalidKeysReturnsEmptyPublicKeyResponse() {
    createBlsKeys(false, PRIVATE_KEYS);
    setupSignerWithKeyManagerApi();
    validateApiResponse(callListKeys(), empty());
  }

  @Test
  public void onlyValidKeysAreReturnedInPublicKeyResponse() throws JsonProcessingException {
    final String[] keys = createBlsKeys(true, PRIVATE_KEYS[0]);
    createBlsKeys(false, PRIVATE_KEYS[1]); // add invalid key
    setupSignerWithKeyManagerApi();

    final Response response = callListKeys();
    final List<KeystoreInfo> expectedResponse = new ArrayList<>();
    expectedResponse.add(new KeystoreInfo(keys[0], null, false));
    assertApiResponse(response, expectedResponse);
  }
}
