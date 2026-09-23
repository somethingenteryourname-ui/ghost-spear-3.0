package dev.ghostspear;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/** Snapshot of config.yml values. Rebuilt on /ghostspear reload. */
public final class Settings {

    // item
    public final Material material;
    public final String name;
    public final List<String> lore;
    public final boolean unbreakable;
    public final boolean glint;

    // ghost mode
    public final boolean hideMode;
    public final int toggleCooldownTicks;
    public final int maxDurationTicks;
    public final boolean endOnAttack;
    public final boolean showStatus;

    // decoy
    public final boolean decoyShowName;
    public final boolean decoyCopySneaking;
    public final boolean decoyReactToHits;
    public final boolean decoyRevealWhenHit;
    public final boolean decoyPoofOnEnd;

    // charge
    public final boolean chargeRequireGhost;
    public final double chargeDamage;
    public final double reach;
    public final double hitboxSize;
    public final boolean hitPlayers;
    public final boolean hitMobs;

    // hit effect
    public final boolean burstEnabled;
    public final int burstSoulCount;
    public final int spikeCount;
    public final double spikeLength;
    public final int spikeTicks;

    // finisher
    public final boolean finisherEnabled;
    public final boolean finisherPlayersOnly;
    public final float finisherVolume;
    public final float finisherPitch;

    // messages
    public final String msgGhostOn;
    public final String msgGhostOff;
    public final String msgGhostOffAttack;
    public final String msgGhostOffDecoyHit;
    public final String msgGhostOffTimeout;
    public final String msgGhostOffDecoyGone;
    public final String msgGhostStatus;
    public final String msgCooldown;
    public final String msgDecoyFailed;
    public final String msgPierce;

    public Settings(FileConfiguration c, Logger log) {
        material = resolveMaterial(
                c.getString("item.material", "NETHERITE_SPEAR"),
                c.getString("item.fallback-material", "TRIDENT"),
                log);
        name = c.getString("item.name", "<aqua>Ghost Spear");
        lore = c.getStringList("item.lore");
        unbreakable = c.getBoolean("item.unbreakable", true);
        glint = c.getBoolean("item.glint", true);

        String mode = c.getString("ghost-mode.invisibility", "HIDE").trim().toUpperCase(Locale.ROOT);
        if (!mode.equals("POTION") && !mode.equals("HIDE")) {
            log.warning("ghost-mode.invisibility must be HIDE or POTION (got '" + mode + "'), using HIDE.");
        }
        hideMode = !mode.equals("POTION");
        toggleCooldownTicks = Math.max(0, c.getInt("ghost-mode.toggle-cooldown-ticks", 20));
        maxDurationTicks = Math.max(0, c.getInt("ghost-mode.max-duration-ticks", 0));
        endOnAttack = c.getBoolean("ghost-mode.end-on-attack", true);
        showStatus = c.getBoolean("ghost-mode.show-status", true);

        decoyShowName = c.getBoolean("decoy.show-name", true);
        decoyCopySneaking = c.getBoolean("decoy.copy-sneaking", true);
        decoyReactToHits = c.getBoolean("decoy.react-to-hits", true);
        decoyRevealWhenHit = c.getBoolean("decoy.reveal-when-hit", false);
        decoyPoofOnEnd = c.getBoolean("decoy.poof-on-end", true);

        chargeRequireGhost = c.getBoolean("charge.require-ghost-mode", true);
        chargeDamage = c.getDouble("charge.damage", 1000.0);
        reach = c.getDouble("charge.reach", 3.5);
        hitboxSize = c.getDouble("charge.hitbox-size", 0.6);
        hitPlayers = c.getBoolean("charge.hit-players", true);
        hitMobs = c.getBoolean("charge.hit-mobs", true);

        burstEnabled = c.getBoolean("hit-effect.enabled", true);
        burstSoulCount = Math.max(0, c.getInt("hit-effect.soul-particles", 120));
        spikeCount = Math.max(0, c.getInt("hit-effect.spikes", 16));
        spikeLength = Math.max(0.5, c.getDouble("hit-effect.spike-length", 3.5));
        spikeTicks = Math.max(1, c.getInt("hit-effect.spike-ticks", 6));

        finisherEnabled = c.getBoolean("finisher.enabled", true);
        finisherPlayersOnly = c.getBoolean("finisher.players-only", false);
        finisherVolume = (float) c.getDouble("finisher.volume", 2.0);
        finisherPitch = (float) c.getDouble("finisher.pitch", 1.0);

        msgGhostOn = c.getString("messages.ghost-on", "<aqua>Ghost mode on.");
        msgGhostOff = c.getString("messages.ghost-off", "<gray>Ghost mode off.");
        msgGhostOffAttack = c.getString("messages.ghost-off-attack", "<red>You attacked - you've been revealed!");
        msgGhostOffDecoyHit = c.getString("messages.ghost-off-decoy-hit", "<red>Someone hit your decoy - you've been revealed!");
        msgGhostOffTimeout = c.getString("messages.ghost-off-timeout", "<gray>Ghost mode wore off.");
        msgGhostOffDecoyGone = c.getString("messages.ghost-off-decoy-gone", "<gray>Your decoy disappeared, so ghost mode ended.");
        msgGhostStatus = c.getString("messages.ghost-status", "<dark_aqua>Ghost mode");
        msgCooldown = c.getString("messages.cooldown", "<gray>Ghost mode ready in <white><seconds>s");
        msgDecoyFailed = c.getString("messages.decoy-failed", "<red>Couldn't create your decoy.");
        msgPierce = c.getString("messages.pierce", "<dark_aqua>Soul Pierce!");
    }

    private static Material resolveMaterial(String primary, String fallback, Logger log) {
        Material m = primary == null ? null : Material.matchMaterial(primary);
        if (m != null && m.isItem()) {
            return m;
        }
        log.warning("item.material '" + primary + "' isn't a valid item on this server, using fallback '" + fallback + "'.");
        Material f = fallback == null ? null : Material.matchMaterial(fallback);
        return (f != null && f.isItem()) ? f : Material.TRIDENT;
    }
}
