# Onkyo/Integra receiver plugin

Implementation of the Yatse Audio/Video receiver plugin API.  

# Install

Download the [apk](./sample/sample-release.apk) on your Android device and it should show as an option in Yatse's "Editing Host"->"Advanced"->"Plugins" (see [Yatse's guide for details](https://yatse.tv/redmine/projects/yatse/wiki/Setup)).  

# Develop and Test

To dev/test:  open the project in Android Studio.  Connect your phone to your computer.  Enable [USB debugging](https://developer.android.com/training/basics/firstapp/running-app.html).  You may have to [enable some other stuff](https://developer.android.com/studio/run/device.html) depending on your OS.

Press "Run" and select your device.  It should work!

# Licence

[GPLV3](https://www.gnu.org/licenses/gpl.html).  The Onkyo protocol borrows heavily from [clijk's jEISCP](https://github.com/cljk/jEISCP), which is also under the GPLV3 licence.  You are free to do whatever you want with any code herein except sell it.

# Acknowledgements

* The bulk of the Onkyo protocol is slightly modified from [jEISCP](https://github.com/cljk/jEISCP).  This saved me a lot of time and effort!
* Yatse's [api](https://github.com/Tolriq/yatse-avreceiverplugin-api/tree/master/api) is phenomenal: easy to understand and adapt.

## Yatse Audio/Video receiver plugin API

API and Sample for Yatse Audio/Video receiver plugins

Current version : **1.0** (Stable)

[api](https://github.com/Tolriq/yatse-avreceiverplugin-api/tree/master/api) folder contains the API to include in your project.

[sample](https://github.com/Tolriq/yatse-avreceiverplugin-api/tree/master/sample) folder contains a fully working plugin that shows everything you need to write a complete plugin.

Javadoc of the API and the sample plugin should describe everything you need to get started.

