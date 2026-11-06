# FastPvP (Fabric, Minecraft 1.21.11, Yarn)

Client-side mod that removes use-cooldowns for End Crystals / Respawn Anchors,
charges anchors to 4 instantly, and attacks a freshly placed crystal the moment it spawns.

## Build
1. Install JDK 21 and Gradle (or copy the Gradle wrapper from the Fabric example mod template).
2. Check versions at https://fabricmc.net/develop and update gradle.properties if needed.
3. Run: `gradle build`  (or `./gradlew build` with the wrapper). Jar appears in build/libs/.
4. If a Yarn name fails to resolve, run `gradle genSources` and check the decompiled sources.

## Commands
/fastpvp crystal | trigger | anchor | detonate   (toggles)

## Warning
Auto-attacking crystals and macro-style anchor charging are cheats on most multiplayer servers.
Use only in singleplayer or where explicitly allowed.
