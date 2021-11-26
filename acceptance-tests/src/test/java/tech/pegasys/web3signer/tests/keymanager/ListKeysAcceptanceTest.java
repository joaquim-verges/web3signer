package tech.pegasys.web3signer.tests.keymanager;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

public class ListKeysAcceptanceTest extends KeyManagerTestBase {
  private static final String BLS_PRIVATE_KEY_1 =
      "3ee2224386c82ffea477e2adf28a2929f5c349165a4196158c7f3a2ecca40f35";
  private static final String BLS_PRIVATE_KEY_2 =
      "32ae313afff2daa2ef7005a7f834bdf291855608fe82c24d30be6ac2017093a8";

  @Test
  public void noLoadedKeysReturnsEmptyPublicKeyResponse() {
    setupSignerWithKeyManagerApi();
    validateApiResponse(callListKeys(), "data", empty());
  }

  @Test
  public void onlyValidKeysAreReturnedInPublicKeyResponse() throws JsonProcessingException {
    final String pubkey = createKeystoreYamlFile(BLS_PRIVATE_KEY_1);
    setupSignerWithKeyManagerApi();

    validateApiResponse(
        callListKeys(),
        "data.validating_pubkey",
        hasItem(pubkey)
    );
  }

  @Test
  public void additionalPublicKeyAreReportedAfterReload() {
    final String firstPubKey = createKeystoreYamlFile(BLS_PRIVATE_KEY_1);
    setupSignerWithKeyManagerApi();

    validateApiResponse(
        callListKeys(),
        "data.validating_pubkey",
        hasItem(firstPubKey)
    );

    final String secondPubKey = createKeystoreYamlFile(BLS_PRIVATE_KEY_2);;
    signer.callReload().then().statusCode(200);

    // reload is async
    Awaitility.await()
        .atMost(5, SECONDS)
        .untilAsserted(() ->
            validateApiResponse(
                callListKeys(),
                "data.validating_pubkey",
                hasItems(firstPubKey, secondPubKey)
            )
        );
  }

  @Test
  public void nonKeystoreKeysAreReadOnly() throws IOException {
    createRawPrivateKeyFile(BLS_PRIVATE_KEY_1);
    setupSignerWithKeyManagerApi();
    validateApiResponse(
        callListKeys(),
        "data.readonly",
        hasItems(true)
    );
  }

  @Test
  public void canReturnBothReadOnlyAndEditableKeystores() throws IOException {
    createKeystoreYamlFile(BLS_PRIVATE_KEY_1);
    createRawPrivateKeyFile(BLS_PRIVATE_KEY_2);
    setupSignerWithKeyManagerApi();
    validateApiResponse(
        callListKeys(),
        "data.readonly",
        hasItems(true, false)
    );
  }
}
