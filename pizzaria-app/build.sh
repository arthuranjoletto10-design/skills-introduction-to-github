#!/usr/bin/env bash
# Builds the APK using only Android SDK command-line tools (no Gradle).
set -euo pipefail
cd "$(dirname "$0")"
SDK="${ANDROID_HOME:-$ANDROID_SDK_ROOT}"
BT="$SDK/build-tools/34.0.0"
JAR="$SDK/platforms/android-34/android.jar"
rm -rf build && mkdir -p build/classes build/dex
"$BT/aapt2" compile --dir res -o build/res.zip
"$BT/aapt2" link -I "$JAR" --manifest AndroidManifest.xml -o build/unsigned.apk build/res.zip
javac --release 8 -cp "$JAR" -d build/classes $(find src -name '*.java')
"$BT/d8" --min-api 21 --lib "$JAR" --output build/dex $(find build/classes -name '*.class')
(cd build/dex && zip -q ../unsigned.apk classes.dex)
"$BT/zipalign" -f -p 4 build/unsigned.apk build/aligned.apk
KS=build/key.jks
keytool -genkeypair -keystore "$KS" -alias app -keyalg RSA -keysize 2048 -validity 10000 -storepass bellamassa -keypass bellamassa -dname "CN=Bella Massa, C=BR"
"$BT/apksigner" sign --ks "$KS" --ks-pass pass:bellamassa --key-pass pass:bellamassa \
  --out build/PizzariaBellaMassa.apk build/aligned.apk
"$BT/apksigner" verify --verbose build/PizzariaBellaMassa.apk
