# ptokens-strongbox-pbtc-on-eth

Android App to interact with the pTokens core. 

### Build

A configuration file called `local.properties` must have the following:

 - `sdk.dir`: Android SDK location 
 - `ndk.dir`: Android NDK location
 - `keystore.store_file`: Keystore file location
 - `keystore.store_password`: Password to unlock the keystore
 - `keystore.key_alias`: Keystore's alias
 - `keystore.key_password`: Keystore's password
 - `conf.nativeSymbol`: Native symbol
 - `conf.hostSymbol`: Host symbol

```bash 
./gradlew assembleRelease exportApk
```

### Show nice logs:

```bash
cd logging
./show <deviceId> [D,I]
```



