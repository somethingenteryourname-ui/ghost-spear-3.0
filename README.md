# GhostSpear

A Paper 1.21.11 plugin based on ManePear's ghost spear idea.

- **Ghost Mode** – swing the Ghost Spear and you turn invisible, while a decoy copy of you
  (your skin, armor, held items, name, sneaking, and the exact way you were facing) stays standing
  exactly where you were. To everyone else it looks like nothing happened.
- **Leaving Ghost Mode** – swing the spear again, or attack anything (melee or projectiles).
  The decoy vanishes and you reappear where you actually are.
- **Soul Pierce** – while in Ghost Mode, hold right-click (spear charge) into someone to one-tap them.
  This counts as an attack, so it also reveals you.
- The decoy never takes damage, but flinches, makes the hurt sound, and gets knocked back like a real player.
- **Hit effect** – when a ghost hits something, a huge blast of soul particles goes off and spikes of
  blue, orange and yellow particles shoot out of the target.
- **Finisher** – if that hit kills them or pops their totem, the warden death sound plays.

## Commands
| Command | Permission | Default |
|---|---|---|
| `/ghostspear give [player]` | `ghostspear.give` | OP |
| `/ghostspear reload` | `ghostspear.reload` | OP |
| (using the spear) | `ghostspear.use` | everyone |

Alias: `/gspear`

## Invisibility modes (`ghost-mode.invisibility` in config.yml)
- `HIDE` (default): you're removed from other players' screens entirely. That means no held item, no armor,
  no sprint or landing particles, and no footsteps. You can't be hit by players while hidden, and you
  also disappear from the tab list.
- `POTION`: invisibility effect with your armor and held item hidden. You stay in the tab list and can be hit,
  but other players still see your sprint particles and hear your footsteps.

Updating from an older version? Your old config.yml is automatically backed up to `config-old.yml` and
replaced with the new one.

## Requirements
- **Paper** (or a Paper fork like Purpur) **1.21.11**. Plain Spigot won't work, because the decoy uses Paper's Mannequin API.
- Java 21.

## Building
GitHub Actions builds the plugin automatically every time you push. You can get the jar from either place:
1. The **Releases** section on the right side of the repo page.
2. **Actions** tab → latest run → **Artifacts** → `GhostSpear-jar`.

Local build: `mvn package` → `target/GhostSpear-1.1.0.jar`.
