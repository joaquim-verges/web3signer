package tech.pegasys.web3signer.tests.keymanager;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import tech.pegasys.web3signer.core.service.http.handlers.keymanager.eth2.KeystoreInfo;

import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.empty;

public class ListKeysAcceptanceTest extends KeyManagerTestBase {
  private static final String BLS_PRIVATE_KEY_1 =
      "3ee2224386c82ffea477e2adf28a2929f5c349165a4196158c7f3a2ecca40f35";
  private static final String BLS_PRIVATE_KEY_2 =
      "32ae313afff2daa2ef7005a7f834bdf291855608fe82c24d30be6ac2017093a8";
  private static final String[] PRIVATE_KEYS = new String[]{BLS_PRIVATE_KEY_1, BLS_PRIVATE_KEY_2};

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

    final List<KeystoreInfo> expectedResponse = new ArrayList<>();
    expectedResponse.add(new KeystoreInfo(keys[0], null, false));
    assertApiResponse(callListKeys(), expectedResponse);
  }

  @Test
  public void additionalPublicKeyAreReportedAfterReload() throws JsonProcessingException {
    final String[] prvKeys = PRIVATE_KEYS;
    final String firstPubKey = createBlsKeys(true, prvKeys[0])[0];
    setupSignerWithKeyManagerApi();

    final List<KeystoreInfo> expectedResponse = new ArrayList<>();
    expectedResponse.add(new KeystoreInfo(firstPubKey, null, false));
    assertApiResponse(callListKeys(), expectedResponse);

    final String secondPubKey = createBlsKeys(true, prvKeys[1])[0];
    signer.callReload().then().statusCode(200);

    expectedResponse.add(new KeystoreInfo(secondPubKey, null, false));
    // reload is async
    Awaitility.await()
        .atMost(5, SECONDS)
        .untilAsserted(() -> assertApiResponse(callListKeys(), expectedResponse));
  }
}
