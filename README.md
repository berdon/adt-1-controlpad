# Getting Started
1. Install ControlPad on an Android device that will be used to control the ADT-1
2. Remotely connect ADB (on some host machine, etc) to your ADT-1 (ie. adb connect IP_ADDRESS:4321)
3. Run service.py via monkeyrunner on the same machine as in #2 (monkeyrunner service/server.py) and wait until the server starts
4. Launch ControlPad on your controller device
5. Connect to the service running on the host machine (see #2, #3) in ControlPad
	* Swipe down to show the ActionBar
	* Menu > Connect > Enter the host IP Address

# What Now?
* Install Order & Chaos and start playing via your ADT-1
* Install Chrome Beta and actually get it to work
* Install any other non-ADT-1 ready application

## How to install other apps?
My method requires a rooted device and doesn't guarantee the application will work:

1. On a rooted device with an application saught after
2. adb shell
3. su
4. cd /data/app
5. Locate the .apk corresponding to the app (google is likely going to be your friend)
6. cp APK_NAME.apk /sdcard/
7. exit; exit
8. adb pull /sdcard/APK_NAME.apk /tmp
9. adb install /tmp/APK_NAME.apk
9. Wait until the install is finished

## How to launch non-ADT-1 apps?
Settings > Apps > Select the app > Open
