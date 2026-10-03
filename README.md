# Expo modular wallets starter — Android + iOS

A small extract of our Expo implementation: official Circle native passkey SDK bridge, reconnect/signing adapter, and public single-owner smart-account builder. No treasury, multisig, agent policy, backend, deployed contracts, or credentials are included. This is a source integration kit, not an independently device-certified application.

## Install into an existing Expo app

1. Copy `modules/circle-modular-wallets` and `src` into the app root. Expo discovers local modules in `./modules`; if you override `expo.autolinking.nativeModulesDir`, point it there. Do not register this module as a config plugin.
2. Install packages:
   ```sh
   npx expo install expo-modules-core expo-dev-client expo-build-properties
   npm install @circle-fin/modular-wallets-core@1.0.13 viem@2.38.3 webauthn-p256
   ```
3. Merge `app.config.example.ts` into your app config; use your own bundle ID/package/domain. Copy `.env.example` to `.env.local` and replace placeholders with your own Circle testnet client configuration. Public client keys ship in the app: restrict them in Circle Console. Never put server API keys or build tokens in `EXPO_PUBLIC_*`.
4. Configure the same passkey domain, iOS bundle identifier, Android package, and signing fingerprint in Circle Console. Publish the two domain association templates over HTTPS at the exact `.well-known` paths, without authentication. Replace every placeholder. Include both development and release signing fingerprints when relevant.
5. Android SDK downloads require `MWSDK_MAVEN_USERNAME` and `MWSDK_MAVEN_PASSWORD` in the build worker, or `mwsdk.maven.username` / `mwsdk.maven.password` in your private `~/.gradle/gradle.properties`. Use your own GitHub package-read credential. Never commit it or add it to app config.
6. Build a native development client:
   ```sh
   npx expo run:ios
   npx expo run:android
   npx expo start --dev-client
   ```
   Expo Go does not contain this module. iOS requires normal signing and associated-domain entitlements; use a physical passkey-capable device for acceptance checks. Native versions retained from our implementation: iOS 1.5.0, Android 1.5.3. Rebuild after native changes.

## Register, reconnect, and build an account

```ts
import { registerOfficialCircleNativeCredential, reconnectOfficialCircleNativeCredential } from './src/native-passkey';
import { createWallet } from './src/wallet';

const config = {
  clientKey: process.env.EXPO_PUBLIC_CIRCLE_CLIENT_KEY!,
  clientUrl: process.env.EXPO_PUBLIC_CIRCLE_CLIENT_URL!,
  expectedRpId: process.env.EXPO_PUBLIC_WEBAUTHN_RP_ID!,
};
// Validate these environment values exist before displaying wallet actions.
const credential = await registerOfficialCircleNativeCredential({ ...config, userName: 'Your user label' });
// Persist credential metadata against the authenticated user using your app's storage.
// Never persist a PIN or private key. On cold restart, reconnect BEFORE signing:
const reconnected = await reconnectOfficialCircleNativeCredential({ ...config, expectedCredentialId: credential.id });
const account = await createWallet(reconnected, publicClient);
console.log(account.address); // Address calculation, not proof of deployment.
```

`publicClient` must use your chosen Circle-supported chain and authenticated RPC transport. Follow Circle's public smart-account sample for transport, bundler, paymaster, and transaction submission. Do not use browser passkey registration helpers on React Native; this kit supplies the native owner instead. The web SDK transport may access `window.location`; test it in Hermes or supply a documented native-compatible HTTP transport. No browser global shim is included.

Submit reviewed calls with viem's `bundlerClient.sendUserOperation({ account, calls, paymaster: true })` only when your Circle Gas Station policy allows sponsorship. Wait for `waitForUserOperationReceipt({ hash })`, check receipt success, then read bytecode before marking deployment ready. Keep signing in a user-confirmed action; prevent concurrent register/login/sign operations in your UI. Start on testnet. No automatic mainnet transaction or demo funds movement is included.

## Acceptance checks before shipping

- Registration succeeds on physical iOS and Android; canceling the prompt leaves no false success.
- Cold-restart reconnect chooses the same credential and produces the same wallet address.
- Wrong RP domain / wrong credential fail closed before account use.
- A testnet transaction returns a successful receipt and deployed bytecode. Test refusal, insufficient funds, sponsorship denial, and network interruption.
- Release signing domain associations work as well as the dev build.

The archive has been checked for excluded private integration and secrets; native compilation and physical-device transaction QA have not been performed for this standalone kit.

## Public references

- Circle public smart-account sample: https://github.com/circlefin/modularwallets-web-sdk/tree/master/examples/circle-smart-account
- Circle iOS SDK: https://github.com/circlefin/modularwallets-ios-sdk
- Circle Android SDK: https://github.com/circlefin/modularwallets-android-sdk
- Expo local native modules: https://docs.expo.dev/modules/get-started/

Third-party dependencies retain their licenses. This starter is licensed under Apache 2.0; see LICENSE. Third-party SDK licensing is independent.
