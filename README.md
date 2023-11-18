# SFULounge
With SFU Lounge, you can discover like-minded students who share your interests and academic goals, fostering meaningful connections within the SFU community. Stay informed about the most exciting events on campus, ensuring you never miss out on opportunities. Initiate conversations and meet new friends directly through the in-app chat feature.   

## Introduction
SFU Lounge is an innovative Android application designed specifically for Simon Fraser University (SFU) students. The app facilitates making new friends and connections within the SFU community, particularly addressing the challenge of socializing in a predominantly commuting school. SFU Lounge stands out with its unique features like blind profiles, interest-based matching, and icebreaker chats.

## Features

### User Profile
- **User Database Fields**: ID, First Name, Last Name (hidden), Gender, SFU Email, Password, Interests, Depth Questions (3), Photos (up to 5), Online Status.
- **Swipe Mechanism**: Users can swipe left or right on other user profiles. Swipe actions are stored with user IDs.
- **Chatrooms**: Each match creates a new chatroom with IDs for users and messages.
- **Message System**: Messages in chatrooms are identified by IDs and include timestamps and sender details.

### UI Pages
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

## Implementation Details

### User Profile
- **Images**: 1 to 5 photos allowed.
- **Depth Questions**: Users answer selected questions to add depth to their profiles.
- **Interest Tags**: Users select from a list of interests.
- **Optional Fields**: Major, course list, age, pronouns, voice profiles, location, languages, zodiac sign, SFU club memberships.

### App Mechanics
- **User Recommendations**: Based on shared interests and preferences.
- **Blind Profiles**: Anonymous profiles shown during certain hours, focusing on character rather than appearance.
- **Matching Algorithm**: Matches users based on interests, values, and depth question answers.
- **Icebreaker Chat**: Facilitates easy conversation initiation.

## Challenges and Future Developments
- **Anticipated Challenges**: Implementing real-time chatting, email verification, complex UI designs, Firebase database integration, image uploads, and swiping mechanisms.
- **Skill Development**: The team will focus on learning and implementing the above-mentioned features.
- **Future Plans**: Post-semester deployment, advertisement through SFU clubs, and continuous app development for enhancing user experience.

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
For support, contact sfu_Lounge_support@gmail.com. Join our community and make your SFU experience unforgettable!
