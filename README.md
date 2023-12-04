# SFU Lounge
With SFU Lounge, you can discover like-minded students who share your interests and academic goals, fostering meaningful connections within the SFU community. Stay informed about the most exciting events on campus, ensuring you never miss out on opportunities. Initiate conversations and meet new friends directly through the in-app chat feature.   

## Introduction
SFU Lounge is an innovative Android application designed specifically for Simon Fraser University (SFU) students. The app facilitates making new friends and connections within the SFU community, particularly addressing the challenge of socializing in a predominantly commuting school. SFU Lounge stands out with its unique features like blind profiles, interest-based matching, and icebreaker chats.

## App demo 
Click on the thumbnail below to watch the video:

[![SFU lounge Demo](https://img.youtube.com/vi/7PUvE_8b7w8/0.jpg)](https://youtu.be/9_zn5c5CQzg)

## Features

### User Profile
- **User Database Fields**: ID, First Name, Last Name (hidden), Gender, SFU Email, Password, Interests, Depth Questions (3), Photos (up to 5), Online Status.
- **Swipe Mechanism**: Users can swipe left or right on other user profiles. Swipe actions are stored with user IDs.
- **Chatrooms**: Each match creates a new chatroom with IDs for users and messages.
- **Message System**: Messages in chatrooms are identified by IDs and include timestamps and sender details.
- **Notification system**: When a message is recieved user will be notified. 

### View Pages
- **Login/Signup Page**: Start page with options to log in or sign up.
- **Photo Page**: Users upload 2 to 4 photos.
- **Personality Page**: Select up to 4 interests and answer up to 3 depth questions.
- **Swipe Page**: Displays user profiles for swiping.
- **Chat Rooms**: List view of all DMs.
- **Settings Page**: Options to edit user information, interests, depth questions, and photos.

### ViewModel Functions
- **UserViewModel**: Handles login, signup, and settings update functionalities.
- **MatchesViewModel**: Manages the swiping mechanism and user recommendations.
- **ChatViewModel**: Queries chat rooms and messages, handles chat functionalities.

### The Thread Diagram
[thread diagram](https://github.com/kianhk6/SFULounge/files/13471130/thread_diagram_3.4.pdf)

### The mvmm model 
<img width="799" alt="image" src="https://github.com/kianhk6/SFULounge/assets/96752380/180317c1-84f1-45c3-8253-6a0b12197de7">

## Implementation Details

### User Profile
- **Images**: 1 to 5 photos allowed.
- **Depth Questions**: Users answer selected questions to add depth to their profiles.
- **Interest Tags**: Users select from a list of interests.
- **Optional Fields**: Major, course list, age, pronouns, voice profiles, location, languages, zodiac sign, SFU club memberships.

### App Mechanics
- **User Recommendations**: Based on shared interests and preferences.
- **Matching Algorithm**: Matches users based on interests, values, and depth question answers.
- **Icebreaker Chat**: Facilitates easy conversation initiation.

## Getting Started

### Prerequisites
- Android Studio with Kotlin support.
- Firebase account for database and authentication services.

### Installation
1. Clone the repository.
2. Open the project in Android Studio.
3. Configure Firebase and other dependencies.
4. Build and run the application on an emulator or physical device.

## Support and Contact
For support, contact sfuLounge@gmail.com. Join our community and make your SFU experience unforgettable!
- The app website: [SFU lounge](https://m-max-cell.github.io/sfu-lounge/)

## Some of the highlights of mechanisms used
<img width="485" alt="image" src="https://github.com/kianhk6/SFULounge/assets/96752380/7dd0646e-a38f-4020-a39b-3fb5d1416cd5">

