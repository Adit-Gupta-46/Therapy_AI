# Application Title: Therapy AI

## Configuration Settings
This application uses the GPT-3 api. Before using this app, go to local.properties and add the
following text "apiKey=API_KEY", where API_KEY is the api key generated from 
https://platform.openai.com/account/api-keys.

Since this app uses text-to-speech, following Henry's presentation in class, the compileSdkVersion
in the app's gradle file has been set to 32. This has allowed the androidx.appcompat:appcompat: to 
be upgraded from 1.0.0 to 1.5.1

Additionally, this app utilizes the OKHttp library, which has been added to gradle. This library is
developed by Square and is regarded to be secure, from my research. It supports https URLs 
and "includes verification of the remote webserver with certificates and the privacy of data 
exchanged with strong ciphers."

## Reading the Code:
When grading my app's code, I would reccomend taking the following steps:

1. Begin by reviewing the overall structure of the application, which is composed of three pages: 
the profile page, the chat page, and the about page. These pages are primarily coded in the 
ProfileActivity.java, ChatActivity.Java, and AboutActivity.java files. 

2. Next, take a look at the bottom_nav.xml file, which is used by all three activities to display 
the bottom menu tabs. Also, note that instances of TabView objects, which are defined in 
TabView.java, are used in all three activities. 

3. To understand how the different parts of the application interact with each other, start by 
looking at the ProfileActivity.java file, which is the page the user first sees when they open 
the app. This page sets the content view to the profile_layout.xml file. In this file, you will 
find the user's profile information and the logic for displaying and updating it. 

4. Next, move on to the ChatActivity.java, which sets the content view to the chat_layout.xml file.
In this activity, the view stores various messages between the user and therapist. These messages 
are all instances of the Message object, which is defined in the Message.java file. The messages 
are inflated from either left_side_message.xml or right_side_message.xml in the 
MessagesAdapter.java file. 

5. Finally, review the AboutActivity.java file, which sets the content view to the about_layout.xml
file. This page displays general information about the application.
