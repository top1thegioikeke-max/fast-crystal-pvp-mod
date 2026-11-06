package dev.fastpvp;

/** Runtime toggles, changed in-game with /fastpvp <module>. */
public final class FastPvpConfig {
    public static volatile boolean fastCrystal = true;        // zero use-cooldown while holding a crystal
    public static volatile boolean crystalTrigger = true;     // attack a freshly placed crystal the instant it spawns
    public static volatile boolean fastAnchor = true;         // zero cooldown + instant full charge
    public static volatile boolean anchorAutoDetonate = false; // swap to a totem and detonate after charging (off by default)

    private FastPvpConfig() {}
}
