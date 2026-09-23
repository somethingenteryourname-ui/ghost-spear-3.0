package dev.ghostspear;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The hit effect: a blast of soul particles plus spikes of blue / orange / yellow
 * particles that shoot outward from whoever got hit. Also plays the warden death
 * sound when a ghost's hit kills someone or pops their totem.
 */
public final class SoulBurst {

    // Custom-colored dust in the three theme colors
    private static final Particle.DustOptions BLUE = new Particle.DustOptions(Color.fromRGB(0x3FA9FF), 1.5f);
    private static final Particle.DustOptions ORANGE = new Particle.DustOptions(Color.fromRGB(0xFF8A1F), 1.5f);
    private static final Particle.DustOptions YELLOW = new Particle.DustOptions(Color.fromRGB(0xFFE23D), 1.5f);
    private static final Particle.DustTransition ORANGE_TO_YELLOW =
            new Particle.DustTransition(Color.fromRGB(0xFF6A00), Color.fromRGB(0xFFF04D), 1.4f);
    private static final Particle.DustTransition BLUE_TO_CYAN =
            new Particle.DustTransition(Color.fromRGB(0x2F6BFF), Color.fromRGB(0x7FF3FF), 1.4f);

    /** One particle type (+ optional data) in the spike palette. */
    private record Style(Particle particle, Object data) { }

    // Cycled along each spike: soul particles mixed with blue, orange and yellow ones
    private static final Style[] PALETTE = {
            new Style(Particle.SOUL, null),
            new Style(Particle.DUST, BLUE),
            new Style(Particle.SOUL_FIRE_FLAME, null),                  // blue flame
            new Style(Particle.DUST, ORANGE),
            new Style(Particle.FLAME, null),                            // orange/yellow flame
            new Style(Particle.DUST, YELLOW),
            new Style(Particle.SCRAPE, null),                           // blue sparkle
            new Style(Particle.WAX_ON, null),                           // orange sparkle
            new Style(Particle.DUST_COLOR_TRANSITION, BLUE_TO_CYAN),
            new Style(Particle.TRIAL_SPAWNER_DETECTION, null),          // orange/yellow flame puff
            new Style(Particle.DUST_COLOR_TRANSITION, ORANGE_TO_YELLOW),
            new Style(Particle.TRIAL_SPAWNER_DETECTION_OMINOUS, null),  // blue flame puff
            new Style(Particle.GLOW, null),
            new Style(Particle.SCULK_SOUL, null),
    };

    private final GhostSpearPlugin plugin;
    private final Map<UUID, Integer> pendingFinisher = new HashMap<>();
    private UUID piercingTarget;

    public SoulBurst(GhostSpearPlugin plugin) {
        this.plugin = plugin;
    }

    // ---------------------------------------------------- Soul Pierce tracking

    /** Set while ChargeTask is dealing Soul Pierce damage, so the listener knows the hit is a pierce. */
    public void setPiercingTarget(UUID target) {
        this.piercingTarget = target;
    }

    public boolean isPiercing(UUID target) {
        return target != null && target.equals(piercingTarget);
    }

    // ---------------------------------------------------- finisher (warden sound)

    /** Remember that a ghost just hit this entity; if it dies or pops a totem right after, play the finisher. */
    public void markHit(LivingEntity target) {
        pendingFinisher.put(target.getUniqueId(), Bukkit.getCurrentTick());
    }

    /** Called on death / totem pop. */
    public void onKilledOrPopped(LivingEntity entity) {
        Integer tick = pendingFinisher.remove(entity.getUniqueId());
        if (tick == null || Bukkit.getCurrentTick() - tick > 1) {
            return;
        }
        Settings s = plugin.settings();
        if (!s.finisherEnabled) return;
        if (s.finisherPlayersOnly && !(entity instanceof Player)) return;

        Location at = entity.getLocation();
        at.getWorld().playSound(at, Sound.ENTITY_WARDEN_DEATH, s.finisherVolume, s.finisherPitch);
    }

    // ---------------------------------------------------- the particle burst

    public void play(LivingEntity target) {
        Settings s = plugin.settings();
        if (!s.burstEnabled) {
            return;
        }
        World world = target.getWorld();
        Location center = target.getLocation().add(0, target.getHeight() * 0.55, 0);

        // Instant blast in the middle
        world.spawnParticle(Particle.SONIC_BOOM, center, 1, 0, 0, 0, 0, null, true);
        world.spawnParticle(Particle.SOUL, center, s.burstSoulCount, 0.35, 0.6, 0.35, 0.12, null, true);
        world.spawnParticle(Particle.SCULK_SOUL, center, s.burstSoulCount / 2, 0.3, 0.5, 0.3, 0.1, null, true);
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, center, 40, 0.15, 0.3, 0.15, 0.22, null, true);
        world.spawnParticle(Particle.FLAME, center, 30, 0.15, 0.3, 0.15, 0.2, null, true);
        world.spawnParticle(Particle.DUST, center, 25, 0.5, 0.7, 0.5, 0, YELLOW, true);
        world.spawnParticle(Particle.DUST, center, 25, 0.5, 0.7, 0.5, 0, ORANGE, true);
        world.spawnParticle(Particle.DUST, center, 25, 0.5, 0.7, 0.5, 0, BLUE, true);
        world.spawnParticle(Particle.CRIT, center, 30, 0.3, 0.5, 0.3, 0.6, null, true);
        world.playSound(center, Sound.ITEM_TRIDENT_HIT, 1.0f, 0.6f);
        world.playSound(center, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.7f);

        // Pick random outward directions for the spikes
        List<Vector> directions = new ArrayList<>();
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < s.spikeCount; i++) {
            double y = -0.3 + rnd.nextDouble() * 1.3;   // mostly sideways/up, a few slightly down
            double angle = rnd.nextDouble() * Math.PI * 2;
            double flat = Math.sqrt(Math.max(0, 1 - y * y));
            directions.add(new Vector(Math.cos(angle) * flat, y, Math.sin(angle) * flat));
        }

        final double length = s.spikeLength;
        final int steps = s.spikeTicks;

        // Grow the spikes outward over a few ticks so they "shoot" out
        new BukkitRunnable() {
            int step = 0;
            int paletteIndex = rnd.nextInt(PALETTE.length);

            @Override
            public void run() {
                if (step >= steps) {
                    cancel();
                    return;
                }
                double from = length * step / steps;
                double to = length * (step + 1) / steps;

                for (Vector dir : directions) {
                    for (double d = from; d < to; d += 0.22) {
                        Location point = center.clone().add(dir.clone().multiply(d));
                        spawn(world, point, PALETTE[paletteIndex % PALETTE.length]);
                        spawn(world, point, PALETTE[(paletteIndex + 5) % PALETTE.length]);
                        paletteIndex++;
                    }
                    // Flames flying off the tip of each spike
                    Location tip = center.clone().add(dir.clone().multiply(to));
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, tip, 0, dir.getX(), dir.getY(), dir.getZ(), 0.25, null, true);
                    world.spawnParticle(Particle.FLAME, tip, 0, dir.getX(), dir.getY(), dir.getZ(), 0.18, null, true);
                }
                step++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void spawn(World world, Location at, Style style) {
        world.spawnParticle(style.particle(), at, 1, 0.03, 0.03, 0.03, 0, style.data(), true);
    }
}
