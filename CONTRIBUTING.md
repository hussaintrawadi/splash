# Contributing to Splash

Thanks for helping. Small, focused pull requests are the easiest to review.

## Getting started

1. Open the project in Android Studio and run it on a phone or emulator with Android 8.0 or later.
2. Read [docs/Splash-Product-and-Design.md](docs/Splash-Product-and-Design.md) for how the
   screens, the reminder system and the rewards fit together.

## Ground rules

- **Offline only.** Splash has no account, no backend and no analytics, and does not ask for
  internet access. Changes that add any of these will not be merged.
- **Reminders must stay reliable.** If you touch `reminder/`, test on a real phone with the
  screen locked, in Doze, and after a reboot. Say which phone and Android version you used.
- **Match the existing structure:** Compose UI in `ui/`, logic without Android dependencies in
  `domain/`, storage in `data/`.
- **Both themes.** Check any UI change in light and dark mode.

## Reporting bugs

Open an issue with your phone model, Android version, what you expected, and what happened.
Phone makers such as Xiaomi, Oppo and Samsung add their own battery savers, so the model matters
for reminder bugs.
