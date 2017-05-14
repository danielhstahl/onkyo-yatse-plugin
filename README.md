# Onkyo/Integra receiver plugin

This plugin is an implementation of the [Yatse](https://yatse.tv/redmine/projects/yatse) Audio/Video receiver plugin API.  For instructions on how to add plugins in Yatse, see [How to select and configure Audio / Video receiver Plugins](https://yatse.tv/redmine/projects/yatse/knowledgebase/articles/21). While Daniel Stahl (phillyfan1138) maintains and supports the plugin, [Tolriq](https://yatse.tv/redmine/users/1) (the creator of Yatse) is authorized to host the plugin on the Google Play Store.  

The plugin allows volume control from the Yatse App.  It also allows for custom commands.  [List of possible commands](http://michael.elsdoerfer.name/onkyo/).

# Compatable Receivers
The following receivers (at the very least) should work with the plugin:

* DTR-20.2	
* DTR-30.2	
* TX-NR708	
* DTR-40.2	
* DHC-40.2	
* TX-NR808	
* DTR-50.2	
* TX-NR1008	
* TX-NR3008	
* TX-NR5008	
* DTR-70.2	
* DTR-80.2	
* DHC-80.2	
* PR-SC5508	
* TX-NR509 (Ether)	
* TX-NR579 (Ether)	
* TX-NR609 (Ether)
* DTR-20.3	
* DTR-30.3	
* TX-NR709	
* DTR-40.3	
* TX-NR809	
* DTR-50.3	
* TX-NR1009	
* TX-NR3009	
* TX-NR5009	
* DTR-70.3	
* DTR-80.3	
* DHC-80.3	
* PR-SC5509
* TX-NR414 (Ether)
* TX-NR515/515AE (Ether)
* DTR-20.4	
* TX-NR616/616AE (Ether)
* DTR-30.4
* TX-NR717 (Ether)
* DTR-40.4	
* TX-NR818/818AE
* DTR-50.4	
* TX-NR1010	
* TX-NR3010
* DTR-70.4	
* TX-NR5010	
* NR-365 (Ether)
* TX-NR525
* HT-RC550 (Ether)
* TX-NR626 
* HT-RC560 (Ether)
* DTR-30.5
* TX-NR727 (Ether)
* DTR-40.5	
* TX-NR828 (Ether)
* DTR-50.5


# Install without the Play Store

Download the [apk](./sample/sample-release.apk) on your Android device and it should show as an option in the advanced settings.  See [How to select and configure Audio / Video receiver Plugins](https://yatse.tv/redmine/projects/yatse/knowledgebase/articles/21).

# Develop and Test

To dev/test:  open the project in Android Studio.  Connect your phone to your computer.  Enable [USB debugging](https://developer.android.com/training/basics/firstapp/running-app.html).  You may have to [enable some other stuff](https://developer.android.com/studio/run/device.html) depending on your OS.

Press "Run" and select your device.  

# Licence

[GPLV3](https://www.gnu.org/licenses/gpl.html).  The Onkyo protocol borrows heavily from [clijk's jEISCP](https://github.com/cljk/jEISCP), which is also under the GPLV3 licence.  You are free to do whatever you want with any code herein except sell it.  

# Acknowledgements

* The bulk of the Onkyo protocol is slightly modified from [jEISCP](https://github.com/cljk/jEISCP).  This saved me a lot of time and effort!
* Yatse's [api](https://github.com/Tolriq/yatse-avreceiverplugin-api/tree/master/api) is phenomenal: easy to understand and adapt.

# Original Yatse API Readme: 
## Yatse Audio/Video receiver plugin API

API and Sample for Yatse Audio/Video receiver plugins

Current version : **1.0** (Stable)

[api](https://github.com/Tolriq/yatse-avreceiverplugin-api/tree/master/api) folder contains the API to include in your project.

[sample](https://github.com/Tolriq/yatse-avreceiverplugin-api/tree/master/sample) folder contains a fully working plugin that shows everything you need to write a complete plugin.

Javadoc of the API and the sample plugin should describe everything you need to get started.

