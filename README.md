# MapDrive 

## To Do
- [x] Research Documentation 
- [x]  Planning Documentation 
- [x]  Integrate Firebase Auth and Firestore API
- [x]  Register a user account with email/password to Firebase Auth service
- [x]  Log In into and authenticate successfully to the main content
- [ ]  User settings fragment/activity 
  - [ ]  Switch between metric and imperial system
  - [ ]  Preferred Landmark (historical,modern or popular)
- [x]  Integrate MapBox API 
- [ ]  Display landmarks based on filter applied
- [x]  Display current user location 
- [ ]  Can select landmark can get information about it
- [x]  Calculate the best route 
- [ ]  Estimated time and distance is shown 
- [ ]  Displays distance to destination in preferred unit of measurement 
- [ ]  Have a list of favourite landmarks

## About 
MapDrive (version 2) is a GPS Android application that allows users to search and navigate to points of interests. 
It is a solution designed to fulfill a university module at the IIE's Varsity College.

### Functional Requirements 
- Research Documentation 
-  Planning Documentation 
-  Integrate Firebase Auth and Firestore API
-  Register a user account with email/password to Firebase Auth service
- Log In into and authenticate successfully to the main content
- User settings fragment/activity 
  - Switch between metric and imperial system
  - Preferred Landmark (historical,modern or popular)
- Integrate MapBox API 
- Display landmarks based on filter applied
- Display current user location 
- Can select landmark can get information about it
- Calculate the best route 
- Estimated time and distance is shown 
- Displays distance to destination in preferred unit 

### Non Functional Requirements
- Authentication for OAuth Services like Google and Facebook
  

## System Requirements 
- Running:
  - Android Studio Bumblebee or later
  - A PC with virtualisation enabled in the BIOS to run an emulator.
  - Emulator must run with the Android SDK 25 or later
  - Or alternatively an android device connected via ADB
  - Device is on Android Marshmallow (6.0) or later
  

## Installation 
- Obtaining the code: 
  - Clone the repository from the github repo
  `git clone https://github.com/Nash-s-Ashes/MapDrive/` 
  - Open the directory in Android Studio
- Gradle should automatically download the dependencies
- Refer to FAQ if you have troubles installing

## Getting Started 
### Authentication 
- You will initially be introduced to an authentication activity where the the end-user must enter their credentials to log in or information to create an account. 
- The Firebase Authentication API is very convenient to use and simply takes two parameters to create an account or log in through standard email/password authentication. 
  `mAuth.createUserWithEmailAndPassword(Email,Password)` 
  `mAuth.signInWithEmailAndPassword(Email,Password)`
  Where the email and password are strings obtained from the respective edit text Editable. 
- The end-user will not be able to send an request to the server unless validations are met. 
  - The text is sanitized with `.trim()` to remove whitespaces
  - Email must match the following pattern: `[^@ \\t\\r\\n]+@[^@ \\t\\r\\n]+\\.[^@ \\t\\r\\n]+` which means it must have at one '@' symbol and one '.' for the domain.
  - Password must match the following pattern: `^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,}$` which means that the password must be at least 8 characters long with one lowercase letter, one uppercase letter and one special character. 
  - Confirm Password must match the Password field. The text is matched with `.equals()`. 
  - 


## Interesting Stuff
- The BottomSheetDialog is alternated using a stack where when it pops the oldest sheet (remember pop() returns the object) and clears it 

## FAQ and Troubleshooting
 **Why do some functional requirements not work?**

 The features not checked under to do 

**The app unexpectedly crashes on my real device?**

The application's navigation has not successfully run on a physical device as of this version.


## License and Authors
[MIT](https://choosealicense.com/licenses/mit/)
Siegan Govender 
Ronan Farquharson 
Kishen Naicker 
Luc Botes

## References and Code Attributions 
* Nash Ramckurran - nashramckurran@gmail.com
  
 Mapbox Docs. n.d. Examples. Mapbox. Accessed [18/10/22] : https://docs.mapbox.com/android/legacy/maps/examples/ 

 Mapbox Docs. n.d. Installation. Mapbox. Accessed [18/10/22] :
 https://docs.mapbox.com/android/legacy/maps/guides/install/ 

