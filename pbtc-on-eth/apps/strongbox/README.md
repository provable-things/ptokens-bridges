# ptokens-strongbox-pbtc-on-eth

Android App executing the px-on-eth strongbox bridge.

Create a `local.properties` file must have the following variables set:

 - `sdk.dir`: Android SDK location 
 - `ndk.dir`: Android NDK location
 - `bridgeName`: i.e. `pbtc-on-eth`

**Note**: you can avoid the creation of `local.properties` file by setting the ANDROID_HOME 
env variable to the right location of the Android SDK, and by specifying the bridge name through 
the BRIDGE_NAME env variable, like this

```
BRIDGE_NAME=pbtc-on-eth ./gradlew assembleDebug exportApk
```

Also, in order to sign the apk with a custom keystore, create a `signing.properties`
file in the project folder with the following information:

 - `keyPassword`
 - `storeFile`
 - `storePassword`
 - `keyAlias`

## Show nice logs:

```bash
cd logging
./show <deviceId> [D,I]
```

## Build

### Locally

#### Requirements:
  
  - Java 8
  - Rust (v1.46)
  - Android-sdk (v29)
  - Android-ndk (v20.1)

#### Compile

```bash 
./gradlew assembleDebug exportApkDebug
```
