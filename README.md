<p align="center">
  <img src="app-logo.png" width="150" alt="Habi Wabi Logo">
</p>

# Habi Wabi 禅

Habi Wabi is a minimal, beautifully animated, open-source habit and health tracker built for Android with Jetpack Compose. It embraces the philosophy of Wabi-Sabi—finding beauty in imperfection and impermanence—by encouraging small, consistent, daily habits.

## 🌟 Philosophy

> "In the beginner's mind there are many possibilities, but in the expert's mind there are few." — Shunryu Suzuki

Habi Wabi respects your privacy and your time:
*   **100% On-Device:** No cloud sync, no accounts, no tracking. Your data is yours, stored locally in a Room database.
*   **Wabi-Sabi Aesthetics:** Minimal, distraction-free interface locked in a calming dark mode.
*   **Simple Yet Powerful:** Deep customization for habits without the mental overhead.

## 🚀 Features

*   **Habit Builder:** Create structured habits with custom icons, colors, daily goals, and specific day frequencies.
*   **Smart Reminders:** Local push notifications that nudge you at your preferred time. Users with reminders are 3x more consistent.
*   **Water Tracking:** Visual, interactive wave-fill ring to log your daily intake across various vessel sizes, all saved offline.
*   **To-Do List:** Grouped, filterable daily tasks with smooth animations and swipe-to-delete.
*   **Health Stats:** Quick-log your weight directly from the home screen and view a 30-day trend chart.
*   **Global Settings:** Quiet hours, global notification toggles, and customizable daily water goals.

## 🛠️ Technology Stack

*   **100% Kotlin**
*   **UI:** Jetpack Compose (Material 3), custom Canvas animations, Compose Navigation.
*   **Data Tier:** Room Database (SQLite), DataStore (Preferences), Coroutines & StateFlow.
*   **Architecture:** MVVM.
*   **Background:** AlarmManager, BroadcastReceivers.

## 🌱 Getting Started

Just clone, build, and run!

```bash
git clone https://github.com/Atharva-3000/habi-wabi.git
cd habi-wabi
./gradlew assembleDebug
```

Open the project in **Android Studio Meerkat** (or newer) and deploy to your connected device.

## ⚖️ License

This project is licensed under the **MIT License**. 

You are free to use, modify, and distribute this software, as long as the original copyright and license notice are included. 

**If you use this code in your own projects, please provide attribution linking back to this repository:**
`https://github.com/Atharva-3000/habi-wabi`
