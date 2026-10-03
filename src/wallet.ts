import { toCircleSmartAccount } from '@circle-fin/modular-wallets-core';
import { createOfficialCircleNativeWebAuthnAccount, type OfficialCircleNativeCredential } from './native-passkey';

// Supply a public RPC client configured for your Circle-supported network.
// Account creation calculates the address; it does not prove onchain deployment.
export async function createWallet(
  credential: OfficialCircleNativeCredential,
  client: Parameters<typeof toCircleSmartAccount>[0]['client'],
) {
  return toCircleSmartAccount({
    client,
    owner: createOfficialCircleNativeWebAuthnAccount(credential),
  });
}
