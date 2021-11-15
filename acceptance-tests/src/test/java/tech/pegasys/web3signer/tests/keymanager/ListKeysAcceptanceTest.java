package tech.pegasys.web3signer.tests.keymanager;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.collection.IsIn.in;

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
  public void onlyValidKeysAreReturnedInPublicKeyResponse() {
    final String[] keys = createBlsKeys(true, PRIVATE_KEYS[0]);
    final String[] invalidKeys = createBlsKeys(false, PRIVATE_KEYS[1]);

    setupSignerWithKeyManagerApi();

    final Response response = callListKeys();
    validateApiResponse(response, contains(keys));
    validateApiResponse(response, everyItem(not(in(invalidKeys))));
  }
}
