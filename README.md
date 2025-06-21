# trivia-game-android
A real-time multiplayer trivia game for Android, built with Java and Firebase Realtime Database.

# 🎮 Multiplayer Trivia Game – Android App

An interactive multiplayer trivia game built using **Java** and **Firebase** on **Android Studio**. The game allows users to compete in real-time trivia sessions with a live leaderboard system and multiple difficulty levels.

---

## ✨ Features

- User registration and login using Firebase Authentication  
- Real-time multiplayer gameplay using Firebase Realtime Database  
- Question randomization and multiple difficulty levels  
- Score tracking and ranking with a live leaderboard  
- Colorful, responsive UI with animations and friendly user experience  
- Single-player mode available for practice  

---

## 🛠️ Tech Stack

- **Language**: Java  
- **IDE**: Android Studio  
- **Database**: Firebase Realtime Database  
- **Authentication**: Firebase Auth  
- **UI**: XML, Material Design, Custom Buttons  
- **Other**: Glide (optional for image loading), Firebase SDKs

---

## 📱 Screenshots

*Add screenshots here (e.g., home screen, gameplay, leaderboard)*  
`![screenshot](images/gameplay.png)`

---

## 🚀 Getting Started

1. Clone the repository.  
2. Open the project in Android Studio.  
3. Connect your Firebase project (replace the `google-services.json` file).  
4. Sync Gradle and run the app on an emulator or Android device.

---

## 🔑 API Key Notice

> ⚠️ **Important:**  
This project previously used an API key for features such as question retrieval or Firebase access.  
For security reasons, the API key has been removed from the public repository.

If you would like to run the app:

1. Create your own Firebase project at [https://console.firebase.google.com](https://console.firebase.google.com)
2. Replace the `google-services.json` file with your own from Firebase.
3. If the project uses a third-party API (e.g., trivia questions), generate your own API key and replace it in the code (usually in a `Constants.java` or config file).
4. NEVER commit sensitive keys or credentials to public repositories.

---

## 📂 Project Structure

/TriviaGame
├── activities/
│ ├── LoginActivity.java
│ ├── GameActivity.java
│ ├── LeaderboardActivity.java
├── models/
├── adapters/
├── xml/
├── Firebase/
└── utils/


---

## 📬 Contact

Developed by **Atiqah Fatinah**  
Email: atiqahawal285@gmail.com  
For academic use and internship portfolio.

---

## 🔒 Disclaimer

This is a student-built project for learning purposes and may not include all production-level features or security measures.
