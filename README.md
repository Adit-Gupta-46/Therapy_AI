# Application Title: Therapy AI

## Configuration Settings

_Include any gradle or configuration settings that are required for your libraries or app to work_

This application uses the GPT-3 api. Before using this app, go to local.properties and add the
following text "apiKey=API_KEY", where API_KEY is the api key sent to Professor Bricker.

Since this app uses text-to-speech, following Henry's presentation in class, the compileSdkVersion
in the app's gradle file has been set to 32. This has allowed the androidx.appcompat:appcompat: to 
be upgraded from 1.0.0 to 1.5.1

Additionally, this app utilizes the OKHttp library, which has been added to gradle. This library is
developed by Square and is regarded to be secure, from my research. It supports https URLs 
and "includes verification of the remote webserver with certificates and the privacy of data 
exchanged with strong ciphers."

## API Key

_Let the course staff know whether or not your app requires an API Key and to whom it was sent
via email_

The api key I used was been emailed to Professor Bricker. 
If you would like, it is also easy to generate an api key from OpenAI here: 
https://platform.openai.com/account/api-keys

## Reading the Code:

_Detail how you would like the person who grades your app's code to do so. This could include
giving them a UML diagram, or what is the best order to read the application files in_

When grading my app's code, I would appreciate if you could take the following steps:

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

## Other information

_Anything else you think is relevant for us to use when testing your code._

Since this app uses the microphone for speech-to-text, please ensure that the microphone is 
enabled in settings and the google app is allowed to access the microphone, as mentioned in 
Henry's presentation, if an emulator is being used for testing. Additionally, I've worked very hard
to make sure everything functions in this app so please don't hesitate to reach out to me, either 
by email (aditg46@uw.edu) or call/text (425-420-4933) if there are any issues in running the
app for grading.