1. Install ControlPad on an Android device that will be used to control the ADT-1
2. Remotely connect ADB (on some host machine, etc) to your ADT-1 (ie. adb connect IP_ADDRESS:4321)
3. Run service.py via monkeyrunner on the same machine as in #2 (monkeyrunner service/server.py) and wait until the server starts
4. Launch ControlPad on your controller device
5. Connect to the service running on the host machine (see #2, #3) in ControlPad
	* Swipe down to show the ActionBar
	* Menu > Connect > Enter the host IP Address
