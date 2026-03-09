# Habit Tracker App - Agent Skills & Tech Stack

## 1. Development Framework
- **UI Toolkit:** Jetpack Compose (Latest Android standard). Essential for perfectly executing the minimal Notion-like UI and subtle animations.
- **Language:** Kotlin.

## 2. Required Agent Skills for Future Phases
- **UI/UX Translation:** The ability to translate strict design rules (Inter font mapping, exact greyscale palettes, typography weights) into reusable Compose `Theme` and `Modifier` components.
- **Custom View Creation:** 
  - Constructing a complex GitHub-style contribution heat map using Compose Canvas or optimized LazyGrids.
  - Creating a fully custom Calendar view that tightly integrates with To-Do models.
  - Implementing ViewPagers for the dot-indicator onboarding carousel.
- **Local Persistence & Architecture:** 
  - Offline-first Room Database integration (since the app has no login/auth).
  - Effective state management (e.g., MutableStateFlows, ViewModels) to keep the UI deeply responsive.
- **Background Processing / Alarms:** Using Android `WorkManager` or `AlarmManager` for firing off timely habit reminders based on custom intervals (daily, weekly, custom days).
- **Compose Animations:** Mastery of `AnimatedVisibility`, `animateContentSize`, `UpdateTransition` to provide the requested subtle and highly polished transitions without jank.
