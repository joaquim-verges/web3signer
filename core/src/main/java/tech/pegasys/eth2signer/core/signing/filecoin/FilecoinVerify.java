/*
 * Copyright 2020 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.eth2signer.core.signing.filecoin;

import static tech.pegasys.eth2signer.core.signing.FcBlsArtifactSigner.FC_DST;
import static tech.pegasys.teku.bls.hashToG2.HashToCurve.hashToG2;

import tech.pegasys.eth2signer.core.signing.BlsArtifactSignature;
import tech.pegasys.eth2signer.core.signing.SecpArtifactSignature;
import tech.pegasys.eth2signer.core.util.Blake2b;
import tech.pegasys.teku.bls.mikuli.G2Point;
import tech.pegasys.teku.bls.mikuli.PublicKey;
import tech.pegasys.teku.bls.mikuli.Signature;

import java.math.BigInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

public class FilecoinVerify {
  private static final Logger LOG = LogManager.getLogger();

  public static boolean verify(
      final FilecoinAddress address,
      final Bytes message,
      final BlsArtifactSignature artifactSignature) {
    final PublicKey blsPublicKey = PublicKey.fromBytesCompressed(address.getPayload());
    final Signature signature = artifactSignature.getSignatureData().getSignature();
    final G2Point hashInGroup2 = new G2Point(hashToG2(message, FC_DST));
    return signature.verify(blsPublicKey, hashInGroup2);
  }

  public static boolean verify(
      final FilecoinAddress address,
      final Bytes message,
      final SecpArtifactSignature artifactSignature) {
    final byte[] digest = Blake2b.sum256(message).toArrayUnsafe();
    final BigInteger signaturePublicKey = recoverSignature(artifactSignature, digest);
    if (signaturePublicKey == null) {
      LOG.error("Unable to recover public key from signature");
      return false;
    } else {
      final Bytes publicKeyBytes =
          Bytes.concatenate(
              Bytes.of(0x4), Bytes.wrap(Numeric.toBytesPadded(signaturePublicKey, 64)));
      final FilecoinAddress filecoinAddress = FilecoinAddress.secpAddress(publicKeyBytes);
      return address.getPayload().equals(filecoinAddress.getPayload());
    }
  }

  private static BigInteger recoverSignature(
      final SecpArtifactSignature artifactSignature, final byte[] digest) {
    final tech.pegasys.signers.secp256k1.api.Signature signatureData =
        artifactSignature.getSignatureData();
    final ECDSASignature signature = new ECDSASignature(signatureData.getR(), signatureData.getS());
    final ECDSASignature canonicalSignature = signature.toCanonicalised();
    final int recId = signatureData.getV().intValue();
    return Sign.recoverFromSignature(recId, canonicalSignature, digest);
  }
}
