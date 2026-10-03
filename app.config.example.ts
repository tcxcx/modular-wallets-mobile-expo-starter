export default {
  expo: {
    name: 'Wallet Demo', slug: 'wallet-demo',
    ios: { bundleIdentifier: 'com.example.walletdemo', associatedDomains: ['webcredentials:wallet.example.com'] },
    android: { package: 'com.example.walletdemo' },
    plugins: [['expo-build-properties', { ios: { deploymentTarget: '16.0' }, android: { minSdkVersion: 26 } }]],
  },
};
