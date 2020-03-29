[![Build Status](https://travis-ci.org/patexoid/ZombieLib2.svg?branch=master)](https://travis-ci.org/patexoid/ZombieLib2)

# ZombieLib2

Just another awesome opds server

Features
 - Can work as proxy to other opds server, HTTP and SOCKS proxy are supported.(Hello TOR and I2P)
 - Subscription to changes in external opds catalogs.
 - Book fuzzy comparsion, similar books will be marked as duplicated and will not be displayed in local opds catalog (optimized for russian languague, any other should also works fine, or you can write me and support for other languague will be added)
 - Notifications in Telegram
 - Localzation Ukrainian, English, Russian
 - UI TBD(May be in far future)


##Misc

 - Add user, First request can be unauthorized and first user will have admin permissions
``` json
   POST {API_URL}/user/create/
   {
       "username":"username",
       "password":"password"
   }
```
 - Add extlibrary.
``` json
   POST {API_URL}/extLibrary
    {
        "name":"{external library name, mandatory}",
        "url":"{external library url,  mandatory}",
        "opdsPath":"path to opds, mandatory",
        "login":"{external library username}",
        "password":"{external library password}",
        "proxyHost":"{proxy host}",
        "proxyPort":{proxy port},
        "proxyType":"{proxy type, possible values: DIRECT, HTTP, SOCKS}"
    }
```