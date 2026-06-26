# Splash 💧 Product & Design Documentation

> **Splash** is a playful, native Android app that reminds you to drink water with a hard-to-ignore
> call-style alert, lets you log every sip, fills up a hero water orb as you go, and rewards you with
> a living system of cards, levels and streaks. Everything runs **fully offline**, no account, no
> backend, no data leaving the phone.

This document is a complete walkthrough of what Splash is, what it does, and how it looks, enough to
show someone the product end-to-end without opening the app.

<p align="center">
  <img src="screenshots/02-home-light.png" width="30%" alt="Home (light)" />
  <img src="screenshots/05-rewards-dark.png" width="30%" alt="Rewards (dark)" />
  <img src="screenshots/09-reminder-call.png" width="30%" alt="Reminder call screen" />
</p>

---

## 1. At a glance

| | |
|---|---|
| **Name** | Splash |
| **Platform** | Android only (phone) |
| **Min / Target OS** | Android 8.0 (API 26) → Android 15 (API 35) |
| **Package** | `com.splash.water` |
| **Version** | 1.0 |
| **Built with** | Kotlin + Jetpack Compose (Material 3) |
| **Architecture** | MVVM · Hilt · Room · DataStore · WorkManager + AlarmManager |
| **Data & privacy** | 100% local/offline. No login, no cloud, no analytics |
| **Cost model** | No ads, no account |

---

## 2. The idea

Most people *know* they should drink more water, they just forget, and the apps that remind them are
easy to swipe away and ignore. Splash fixes the two weak points:

1. **The reminder is unmissable.** Instead of a silent line in the notification shade, a reminder
   takes over the whole screen like an **incoming phone call** and keeps ringing until you respond.
2. **Progress is a game, not a chore.** A big animated water orb fills as you drink, goals are tuned
   to *your* body, and a constantly-refreshing system of daily cards, levels and streaks keeps it fun.

Splash is deliberately friendly and motivational. Hydration tips are encouragement, **not medical
advice**, and the app says so.

---

## 3. Feature overview

**Logging & today**
- One-tap quick-add chips (**+250 / +500 ml**) and a custom amount sheet.
- Hero **water orb** with an animated wave surface + circular progress ring that fills as you log.
- Live "today's log" list with undo (delete a mis-tap).
- **Confetti celebration** the moment you hit your daily goal.
- **Streak chip** (🔥) showing consecutive goal-met days.

**Reminders (the core)**
- Two modes: **Smart** (auto-spaced across your waking hours, pauses once the goal is met) and
  **Manual** (custom intervals and/or fixed clock times).
- Reminders appear as a **full-screen, call-style alert** that shows even over the lock screen.
- Rings until you act, five actions: **I drank · +250 ml · +500 ml · Snooze · I'll drink later**.
- **Respects silent & Do Not Disturb**, it behaves like a phone call, so it won't ring when you've
  silenced the phone.
- **7 built-in tones** + the option to pick any sound on your device, each with an in-app preview.
- A gentle **nudge** if you skip "I'll drink later" five times in a row.

**Goals**
- Auto-calculated from body stats using established hydration guidance, with a plain-English
  "How this is calculated" explainer; always editable and clamped to a safe range.

**Progress & rewards**
- **Daily cards** based on a *percentage of your personal goal* (so any goal can earn them), with a
  **theme that rotates every day**.
- A **Level** system (Droplet → Hydration Legend) driven by your lifetime goal-met days.
- **Weekly** badges and a long-term **Milestones** ladder (3 days → 2 years).

**History**
- Day · Week · Month · Year tabs with a clean bar chart (goal-met days turn **green**) plus totals,
  averages, goal-days and your best day.

**Personalization**
- Light / Dark / System theme, both designed to feel premium.
- Body stats, waking window, snooze length, default log amount, sound & vibration.

---

## 4. Screen-by-screen

### 4.1 Onboarding (first launch)
<img src="screenshots/01-onboarding.png" width="240" align="right" alt="Onboarding" />

A short, friendly flow: a welcome ("Welcome to Splash, your playful hydration buddy"), optional body
stats (weight, age, sex, activity, climate) that preview your computed goal in real time, your waking
window, reminder mode, and the permission grants. Skipping stats falls back to a sensible default goal.

### 4.2 Home / Today
<p>
  <img src="screenshots/02-home-light.png" width="240" alt="Home light" />
  <img src="screenshots/03-home-dark.png" width="240" alt="Home dark" />
</p>

The heart of the app. A large gradient canvas with:
- **Header**, "Stay hydrated" + how much is left, and the **🔥 streak chip** top-right.
- **Water orb**, a circular ring around an orb whose water level (with a moving sine-wave surface)
  rises as you drink; the big number shows **% of goal** and `current / goal ml`.
- **Quick add**, `+250 ml` / `+500 ml` chips and a full-width **Log water** button (opens the sheet).
- **Today's log**, each entry with amount, time and a delete icon.
- **Celebration**, confetti burst + "Goal complete!" when the ring fills.

### 4.3 Quick-log sheet
Preset amounts and a custom milliliter entry for one-tap logging.

### 4.4 History
<img src="screenshots/06-history.png" width="240" align="right" alt="History" />

Tabs **Day · Week · Month · Year**. A rounded **bar chart** where each bar represents a period's
intake; bars that reached the goal are **green**, under-goal bars **blue** (with a small legend, no
clutter, no goal line needed). Below: stat cards for **Total, Daily average, Goal days, Best day**.

### 4.5 Rewards
<p>
  <img src="screenshots/04-rewards-light.png" width="240" alt="Rewards light" />
  <img src="screenshots/05-rewards-dark.png" width="240" alt="Rewards dark" />
</p>

- **Level banner**, e.g. "Level 1 · Droplet", a progress bar, and "_N_ more goal-days to _next_".
- **Today · _Theme_**, five daily cards at **25 / 50 / 75 / 100 / 125%** of your goal; unlocked cards
  light up with the day's accent colour and icon, locked ones show a padlock. The theme name and
  flavour change every day.
- **This Week**, 3 / 5 / 7 goal-day badges.
- **Milestones**, the long-term streak ladder.

### 4.6 Settings
<p>
  <img src="screenshots/07-settings-goal.png" width="240" alt="Settings: goal & body stats" />
  <img src="screenshots/08-settings-sounds.png" width="240" alt="Settings: sounds & theme" />
</p>

Grouped cards: **Daily goal** (with the science explainer), **Body stats** (numeric fields + chips),
**Reminders** (mode, waking window, quick-log amount, snooze, manual slots), **Sound & vibration**
(the 7 built-in tones with preview + device sound), **Appearance** (theme), and **About** (version +
the not-medical-advice note).

### 4.7 Reminder call screen
<p>
  <img src="screenshots/09-reminder-call.png" width="240" alt="Reminder call screen" />
  <img src="screenshots/10-reminder-warning.png" width="240" alt="Reminder with skip warning" />
</p>

A branded deep-blue full-screen takeover with a **pulsing water drop**, "Time to drink water!", and the
five action buttons. If you've skipped repeatedly, an amber banner appears: _"you haven't logged water
since 7:25 AM, that's 6 reminders skipped…"_.

---

## 5. The reminder system (deep dive)

**Scheduling.** Reminders are exact **alarm-clock** alarms (`setAlarmClock`) so they survive Doze and
battery optimization. *Smart* mode estimates how many reminders are left in your waking window to hit
the goal and spaces the next one evenly, recomputing after each log and **pausing once the goal is
met**. *Manual* mode fires your chosen fixed times and/or repeating intervals.

**The alert.** When an alarm fires, Splash both launches the full-screen **call screen** directly and
posts a **Call-category** notification with a full-screen intent, so the alert appears whether the
phone is locked or unlocked, not just as a status-bar line. A foreground service plays the chosen tone
on a loop until you pick an action.

**Quiet when you are.** The sound uses the phone's **ringtone (call) channel** and **does not bypass
Do Not Disturb**. Before ringing it checks the ringer: on **silent / vibrate / DND** it stays quiet
(still showing the screen, and buzzing only if you're on vibrate). Just like a call you've silenced.

**Actions.** `I drank` (logs your default amount) · `+250 ml` · `+500 ml` · `Snooze` (re-fires after
your snooze length) · `I'll drink later` (dismisses). Logging anything resets the skip counter; five
consecutive dismissals trigger the nudge banner.

**Resilience.** A boot receiver reschedules everything after a restart, and a periodic background
worker re-syncs as a safety net.

---

## 6. Rewards & progression

**Daily cards, goal-relative & revolving.** The five daily steps are **percentages of your personal
goal** (25/50/75/100/125%), so whether your goal is 1.5 L or 4 L you can earn them all. Each day a
**theme** is chosen (deterministically per date, and from a bigger pool as you level up) that re-skins
the five cards with fresh names and colour, so it never feels like the same four cards every day.

Daily theme pool (unlocks by level): **Glow, Focus** (Lv 1) · **Energy, Calm** (Lv 2) ·
**Athlete, Adventure** (Lv 3) · **Bloom** (Lv 4) · **Cosmic** (Lv 5).

**Levels.** Your level is driven by **lifetime goal-met days**, so it's fair for any goal size:

| Lv | Title | Goal-days | Lv | Title | Goal-days |
|----|-------|-----------|----|-------|-----------|
| 1 | Droplet | 0 | 6 | Tide Turner | 60 |
| 2 | Sprinkle | 3 | 7 | Hydro Hero | 100 |
| 3 | Stream | 7 | 8 | Aqua Master | 180 |
| 4 | River | 14 | 9 | Ocean Sage | 365 |
| 5 | Wave Rider | 30 | 10 | Hydration Legend | 730 |

**Weekly badges.** Getting Consistent (3 days) · Hydration Habit (5) · Perfect Week (7).

**Milestones (long-term streaks).** On a Roll (3) · Week Warrior (7) · Monthly Master (30) ·
Quarterly Champion (90) · Half-Year Hero (180) · Eight-Month Elite (240) · One-Year Legend (365) ·
18-Month Master (545) · Two-Year Titan (730).

---

## 7. How the goal is calculated (the science)

The auto goal targets the water you actively **drink** and is built from cited guidance:

- **Base:** ~**30-35 mL per kg** of body weight (the common clinical guideline), 35 under age 30,
  33 for 30-55, 30 over 55.
- **Sex:** women are scaled ×0.95 (lower fraction of body water).
- **Activity:** +0 / +300 / +550 / +800 mL for sedentary → very active (covers sweat losses).
- **Climate:** +0 / +250 / +500 mL for temperate / warm / hot.
- **Clamped** to **1,500-4,000 mL/day**; if weight is unknown it falls back to a sex-based Adequate
  Intake (≈3,000 mL men / 2,300 mL women).

Grounded in the **U.S. National Academies (2004)** and **EFSA (2010)** adequate-intake ranges. The app
shows a short "How this is calculated" note and states these are general estimates, not medical advice.

---

## 8. Design system

### 8.1 Brand & personality
Fresh, playful, confident. The mascot is a single **water drop 💧**; the only other emoji used anywhere
is the **🔥 streak** flame. Visuals lean on water metaphors, orbs, waves, ripples, deep-ocean colour.

### 8.2 Colour

**Brand / water palette**

| Token | Hex | Use |
|---|---|---|
| Aqua Primary | `#1FA2FF` | Primary actions (light) |
| Aqua Secondary | `#12D8FA` | Secondary accents |
| Aqua Deep | `#0B6FB8` | Deep accent / on-container |
| Mint Green | `#06D6A0` | Success / goal-met |
| Water Light → Mid → Deep | `#6FD7FF` → `#2AB6FF` → `#0077C2` | Orb & chart gradients |
| Sunny Yellow | `#FFD166` | Warm highlight |
| Coral Pink | `#FF6B9D` | Accent |

**Light theme**

| Role | Hex |
|---|---|
| Primary / onPrimary | `#1FA2FF` / `#FFFFFF` |
| Secondary | `#12D8FA` |
| Tertiary | `#06D6A0` |
| Background | `#F2FBFF` |
| Surface / onSurface | `#FFFFFF` / `#0A2230` |
| Surface variant | `#E2F3FB` |
| Primary container / on | `#CDEBFF` / `#0B6FB8` |
| Background gradient | `#EAF7FF` → `#D6F0FF` |

**Dark theme** (premium "deep ocean at night")

| Role | Hex |
|---|---|
| Primary / onPrimary | `#3FC6FF` / `#00263B` |
| Secondary | `#59E3E8` |
| Tertiary | `#5BE7A9` |
| Background | `#0A1B33` |
| Surface / onSurface | `#15294A` / `#EAF4FF` |
| Surface variant / on | `#203A63` / `#B7C8E6` |
| Primary container / on | `#124A80` / `#CDEBFF` |
| Secondary container / on | `#134A57` / `#BEF3F6` |
| Outline | `#40557E` |
| Background gradient | `#0C3A5E` → `#0A1B33` |

**Reminder call screen**, its own branded gradient, independent of theme: `#0E63B8` → `#063C73`.

### 8.3 Typography
Splash uses the **system sans-serif** (Roboto on Android) with a customized, slightly heavier Material 3
type scale for a punchy, friendly feel:

| Style | Weight | Size / line |
|---|---|---|
| Display Large | ExtraBold | 48 / 52 |
| Headline Large | ExtraBold | 30 / 36 |
| Headline Medium | Bold | 24 / 30 |
| Title Large | Bold | 20 / 26 |
| Title Medium | SemiBold | 16 / 22 |
| Body Large | Normal | 16 / 24 |
| Body Medium | Normal | 14 / 20 |
| Label Large | Bold | 14 / 20 |

Big numbers (the % on the orb, the goal in Settings) use **Display Large ExtraBold** for impact.

### 8.4 Iconography
Material Symbols (filled) throughout, e.g. WaterDrop, BarChart, EmojiEvents, Settings for the nav;
and a themed set for rewards (drop, waves, sparkle, trophy, medal, shield, diamond, crown, bolt,
target, star). Reward/level icons sit inside **circular tinted badges**.

### 8.5 Shape & elevation
- **Cards / sections:** 24 dp rounded corners (stat cards 20 dp).
- **Buttons:** fully rounded **pill** shape (~30 dp).
- **Reward cards:** 24 dp, ~0.92 aspect ratio, opaque tinted surface so shadows never bleed through;
  unlocked cards get a slight elevation, locked cards are flat and grey.
- **Badges:** perfect circles.

### 8.6 Motion
- Water orb: continuous **sine-wave** surface animation; the level springs up on each log.
- Goal completion: **confetti** (konfetti) burst + bounce.
- Reminder screen: a **pulsing** drop (0.92 ↔ 1.08 scale, ~0.9 s, ease-in-out reverse).
- Reward unlocks: card reveal + a celebratory snackbar.

### 8.7 Components
Water orb (Canvas), rounded bar chart (Canvas), pill buttons, filter chips, selectable sound rows with
preview, level banner with progress bar, reward card grid, and the full-screen call layout.

---

## 9. Navigation & information architecture
A bottom **navigation bar** with four destinations:

```
Home 💧   ·   History 📊   ·   Rewards 🏆   ·   Settings ⚙
```

The reminder call screen and the quick-log sheet sit outside the tab graph (the call screen can appear
over the lock screen).

---

## 10. Data, privacy & permissions
- **Local only.** Intake logs, achievements and reminder slots live in an on-device **Room** database;
  profile and settings in **DataStore**. Nothing is uploaded; there is no account.
- **Permissions** (all in service of reminders): notifications, exact alarms, full-screen intent,
  foreground service, vibrate, wake lock, boot-completed, and an optional battery-optimization
  exemption so alarms fire reliably.

---

## 11. Technical architecture (one-pager)
- **UI:** Jetpack Compose, Material 3, Compose Canvas for custom visuals, konfetti for confetti.
- **Pattern:** MVVM, `ViewModel` + `StateFlow`, unidirectional data flow; theme via `CompositionLocal`.
- **DI:** Hilt. **Persistence:** Room + DataStore. **Background:** WorkManager (rollover/re-sync) +
  AlarmManager (exact alarms) + a foreground service (looping tone) + a full-screen Activity.
- **Single module**, organized by feature: `ui/`, `domain/`, `data/`, `reminder/`, `di/`.

---

## 12. In one sentence
**Splash turns drinking water into a game you can't ignore**, a call that rings until you sip, a goal
shaped to your body, and a colorful, ever-refreshing wall of rewards, all private and offline on your
phone.
