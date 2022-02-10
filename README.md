# 🛑 THIS REPOSITORY IS OFFICIALLY NO LONGER UNDER MAINTENANCE since 10/02/2022 🛑

# Bonfire [![](https://raw.githubusercontent.com/novoda/novoda/master/assets/btn_apache_lisence.png)](LICENSE.txt)

One of the big announcements of Google IO 2016 was Firebase. No longer "just" a database, the Firebase umbrella now includes integrated Analytics, Crash Reporting, Push Messaging, Dynamic Links, Storage, Hosting, and more. Is this new platform ready for all your projects? Should you spend time learning everything about it?

We like to keep on top of new technology at Novoda, so we decided to dedicate some time to explore the new Firebase. With a small team of four developers (two for iOS and two for Android) we took the idea of a chat application shown in most sample code and expanded it into a more feature-rich example. Today we want to share with you our findings and the demo app that was built during this exploration. Say hello to Bonfire!

<img src="/android/app/src/main/ic_launcher-web.png" alt="Bonfire logo" width="200">

### Bonfire features:
  * Real time chat organised into channels
  * Authentication using Google Sign-In
  * Channel names limited to one emoji on database level
  * Public and private channels
  * Any user can create a channel
  * Channel members can add and remove members of a private channel
  * Remote configuration of the order of channels in the channels screen
  * Invite users to the app with a customised welcome screen

Bonfire is built on top of the new [Firebase][1] from Google.
It was built by a joint team working on the [iOS][2] and the [Android][3] version simultatiously.

<a href="https://play.google.com/store/apps/details?id=com.novoda.bonfire&amp;utm_source=global_co&amp;utm_medium=prtnr&amp;utm_content=Mar2515&amp;utm_campaign=PartBadge&amp;pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1" style="border: 0 none;"><img width="25%" height="25%" style="vertical-align:middle;text-decoration: none;" alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png"></a>
<br/>
<sub>Android, Google Play and the Google Play logo are trademarks of Google Inc.</sub>
<br/>
The iOS app hasn’t made it through the app review process, but you can [sign up to our beta][4]

## License

    Copyright 2016 Novoda Ltd

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

[1]: https://firebase.google.com/
[2]: /ios
[3]: /android
[4]: https://docs.google.com/forms/d/1UGU1w4QohXgyFKFN1panr_2r1R5FxVEPGfJ-uNtEoPE/viewform
