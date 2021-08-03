# NeverNote
📝 A Simple Note and Task App built to demonstrate the use of Modern Android development tools - (Kotlin, Coroutines, Flow, Architecture Components, MVVM, Room, Material Design Components, Notifications and more). *Made with love ❤️ by [llastkrakw](https://github.com/llastkrakw)*

![GitHub Cards Preview](https://github.com/llastkrakw/NeverNote/blob/master/images/presentation.png?raw=true)

***Try latest NeverNote app from below 👇***

<!-- [***NeverNote APK***](https://github.com/llastkrakw/NeverNote/blob/master/NeverNote.apk) -->

Getting Started
---------------

### Unsplash API key

NeverNote uses the [Unsplash API](https://unsplash.com/developers) to load pictures on note
background. To use the API, you will need to obtain a free developer API key. See the
[Unsplash API Documentation](https://unsplash.com/documentation) for instructions.

Once you have the key, update these lines to the `build.gradle` file, 
usually `~/app/build.gradle` in the project's root folder:

```
        buildConfigField "String", "UNSPLASH_ACCESS_KEY", "\"my_unsplah_acces_key\""
        buildConfigField "String", "UNSPLASH_SECRET_KEY", "\"my_unsplah_secret_key\""
```

### Some Screens

**Light ☀️**

| <img src="https://github.com/llastkrakw/NeverNote/blob/master/images/light/mockup/Screenshot_2021-03-19-14-56-55-305_com.llastkrakw.nevernote_google-pixel5-sortasage-portrait.png" width="200"/>| <img src="https://github.com/llastkrakw/NeverNote/blob/master/images/light/mockup/Screenshot_2021-03-19-14-57-25-891_com.llastkrakw.nevernote_google-pixel5-sortasage-portrait.png" width="200"/> | <img src="https://github.com/llastkrakw/NeverNote/blob/master/images/light/mockup/Screenshot_2021-03-19-14-57-44-497_com.llastkrakw.nevernote_google-pixel5-sortasage-portrait.png" width="200"/> | <img src="https://github.com/llastkrakw/NeverNote/blob/master/images/light/mockup/Screenshot_2021-03-19-14-58-54-653_com.llastkrakw.nevernote_google-pixel5-sortasage-portrait.png" width="200"/> | <img src="https://github.com/llastkrakw/NeverNote/blob/master/images/light/mockup/Screenshot_2021-03-19-14-58-06-426_com.llastkrakw.nevernote_google-pixel5-sortasage-portrait.png" width="200"/> |
| :-------------: | :-------------:  | :-------------:  | :-------------:  | :-------------:  |
|     Home     |    Folder Detail    |    Note Detail     |     Task       |     Editor     |


**Dark 🌑**

| <img src="https://github.com/llastkrakw/NeverNote/blob/master/images/dark/mockup/Screenshot_2021-03-19-15-10-17-700_com.llastkrakw.nevernote_google-pixel5-sortasage-portrait.png" width="200"/>| <img src="https://github.com/llastkrakw/NeverNote/blob/master/images/dark/mockup/Screenshot_2021-03-19-15-10-51-224_com.llastkrakw.nevernote_google-pixel5-sortasage-portrait.png" width="200"/> | <img src="https://github.com/llastkrakw/NeverNote/blob/master/images/dark/mockup/Screenshot_2021-03-19-15-11-06-619_com.llastkrakw.nevernote_google-pixel5-sortasage-portrait.png" width="200"/> | <img src="https://github.com/llastkrakw/NeverNote/blob/master/images/dark/mockup/Screenshot_2021-03-19-15-11-27-155_com.llastkrakw.nevernote_google-pixel5-sortasage-portrait.png" width="200"/> | <img src="https://github.com/llastkrakw/NeverNote/blob/master/images/dark/mockup/Screenshot_2021-03-19-15-10-35-473_com.llastkrakw.nevernote_google-pixel5-sortasage-portrait.png" width="200"/> |
| :-------------: | :-------------:  | :-------------:  | :-------------:  | :-------------:  |
|     Home     |    Search Note   |    Search Task    |     Note With Bg     |     Reminder     |

## Built With 🛠
- [Kotlin](https://kotlinlang.org/) - First class and official programming language for Android development.
- [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) - For asynchronous and more..
- [Broadcast Receiver](https://developer.android.com/guide/components/broadcasts) - To build background alarm trigger
- [Notification Manager and Notification Channel](https://developer.android.com/guide/topics/ui/notifiers/notifications) - To make good pratice notification
- [Android Architecture Components](https://developer.android.com/topic/libraries/architecture) - Collection of libraries that help you design robust, testable, and maintainable apps.
  - [Stateflow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow) - StateFlow is a state-holder observable flow that emits the current and new state updates to its collectors. 
  - [Flow](https://kotlinlang.org/docs/reference/coroutines/flow.html) - A flow is an asynchronous version of a Sequence, a type of collection whose values are lazily produced.
  - [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - Stores UI-related data that isn't destroyed on UI changes. 
  - [Room](https://developer.android.com/topic/libraries/architecture/room) - SQLite object mapping library.
  - [Data Binding](https://developer.android.com/topic/libraries/data-binding/) - Declaratively bind observable data to UI elements.
  - [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) - Jetpack DataStore is a data storage solution that allows you to store key-value pairs or typed objects with protocol buffers. DataStore uses Kotlin coroutines and Flow to store data asynchronously, consistently, and transactionally.
- [Material Components for Android](https://github.com/material-components/material-components-android) - Modular and customizable Material Design UI components for Android.
- [Audio Recording](https://developer.android.com/guide/topics/media) - Record and play audio with android framework.

### Package Structure 📦


```

   📦nevernote
 ┣ 📂core
 ┃ ┣ 📂constants
 ┃ ┣ 📂converters
 ┃ ┗ 📂utilities
 ┣ 📂feature
 ┃ ┣ 📂note
 ┃ ┃ ┣ 📂adapters
 ┃ ┃ ┣ 📂datas
 ┃ ┃ ┃ ┣ 📂dao
 ┃ ┃ ┃ ┣ 📂database
 ┃ ┃ ┃ ┗ 📂entities
 ┃ ┃ ┣ 📂notification
 ┃ ┃ ┣ 📂receiver
 ┃ ┃ ┣ 📂repositories
 ┃ ┃ ┗ 📂viewModels
 ┃ ┗ 📂task
 ┃ ┃ ┣ 📂adapters
 ┃ ┃ ┣ 📂datas
 ┃ ┃ ┃ ┣ 📂dao
 ┃ ┃ ┃ ┣ 📂database
 ┃ ┃ ┃ ┗ 📂entities
 ┃ ┃ ┣ 📂notification
 ┃ ┃ ┣ 📂receiver
 ┃ ┃ ┣ 📂repositories
 ┃ ┃ ┗ 📂viewModels
 ┣ 📂views
 ┃ ┣ 📂notes
 ┃ ┃ ┣ 📂activities
 ┃ ┃ ┗ 📂fragments
 ┃ ┣ 📂splashScreen
 ┃ ┗ 📂task
 ┃ ┃ ┣ 📂activities
 ┃ ┃ ┗ 📂fragments
    
```

## Architecture 🗼
This app uses [***MVVM (Model View View-Model)***](https://developer.android.com/jetpack/docs/guide#recommended-app-arch) architecture.

![](https://developer.android.com/codelabs/android-room-with-a-view-kotlin/img/8e4b761713e3a76b.png)

Also [***Memento***](https://en.wikipedia.org/wiki/Memento_pattern) and [***Command***](https://en.wikipedia.org/wiki/Command_pattern) pattern are use to make text editor.


## Contribute 👏
If you want to contribute to this app, you're always welcome!
See [Contributing Guidelines](https://github.com/llastkrakw/NeverNote/blob/master/CONTRIBUTION.md). 

## Contact
Have an project? DM me at 👇

Drop a mail to:- llastkrakw21@gmail.com

## License 🔖

```
MIT License

Copyright (c) 2021 llastkrakw

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
