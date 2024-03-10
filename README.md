# Tabby

This is tool for Benesse tablets, with less bloats than original.

## Install
Install apk, using adb or PackageInstaller.
Doing those 2 adb commands are recommended as some features are limited without it.
```
adb shell pm grant com.saradabar.cpadcustomizetool android.permission.WRITE_SECURE_SETTINGS
adb shell dpm set-device-owner com.saradabar.cpadcustomizetool/.Receiver.AdministratorReceiver
```
