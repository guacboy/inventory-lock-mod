# Lockdown

A Fabric mod for Minecraft 1.16.1. Every interval (2 minutes by default) one more of your
inventory slots locks, until all 37 are gone.

No Fabric API. Everything runs through mixins, so the only dependency is Fabric Loader.
The mod is needed on both the server and the client.

## How it works

- **Lock order** (`Lockdown.ORDER`, PlayerInventory indices): top inventory row left to right
  (9..17), middle row right to left (26..18), bottom row left to right (27..35), hotbar right
  to left (8..0), and the offhand (40) last.
- **Eviction.** When a slot locks, its item moves to the next open slot in lock order. If no
  open slot is empty, it tops up matching stacks, and anything left over drops on the floor.
- **Locked slots are unusable.** Clicks, drags, shift clicks, number key and F swaps (inside
  screens and in the world), and item pickups all skip them. A server tick enforcer also moves
  out anything that ends up in one anyway, for example through `/replaceitem` or creative mode.
- **Overlay.** Locked slots get a dark red box with a pixel X in every container screen
  (creative included), on the HUD hotbar, and on the offhand slot.
- **Timer.** Top left, in the vanilla font: `Next lock in 1:47` and `Locked X/37` under it.
  It is hidden while F3 is open, and when nothing is locked and the timer is stopped.
- **Persistence.** State is stored per player in player NBT under `lockdown`, and survives
  death, relog, and dimension changes.
- **Sync.** Every tick the server sends the state on channel `lockdown:state` through a
  vanilla `CustomPayloadS2CPacket`.

## Commands

Requires op level 2. Each command applies to every online player.

| Command | Effect |
| --- | --- |
| `/lockdown start` | Start or resume the countdown |
| `/lockdown stop` | Pause the countdown, keeping existing locks |
| `/lockdown reset` | Pause and unlock everything |
| `/lockdown interval <seconds>` | Set the time between locks |

## Building

Requires JDK 17 or newer to run Gradle. The mod itself compiles to Java 8 bytecode.

```
./gradlew build        # jar in build/libs/
./gradlew runClient    # dev client
./gradlew runServer    # dev server
```

Toolchain: Gradle 8.14.3 wrapper, Loom 1.10-SNAPSHOT (resolves to 1.10.5), yarn
1.16.1+build.21, Fabric Loader 0.16.14.

### Pre-push gate

`.githooks/pre-push` runs `./gradlew build` and blocks the push if it fails. After `git init`,
enable it with:

```
git config core.hooksPath .githooks
```

## Source layout

| Path | Role |
| --- | --- |
| `Lockdown` | Constants, lock order, lock lookups shared by every mixin |
| `LockState` | Per player state, NBT and packet encoding |
| `LockEnforcer` | Server tick: advance timer, evict locked slots, send sync packet |
| `LockdownCommand` | Brigadier command tree |
| `ClientLockState` | Last state received by the client |
| `client/LockOverlay` | Slot overlay and HUD text drawing |
| `mixin/SlotMixin` | Locked slots refuse insert and take |
| `mixin/ScreenHandlerMixin` | Cancels clicks on, and swaps into, locked slots (both sides) |
| `mixin/PlayerInventoryMixin` | `getEmptySlot` skips locked slots, which covers pickups |
| `mixin/ServerPlayerEntityMixin` | Holds state, ticks it, NBT save and load, copy on respawn |
| `mixin/ServerPlayNetworkHandlerMixin` | Blocks F swap in the world when a side is locked |
| `mixin/CommandManagerMixin` | Registers `/lockdown` |
| `mixin/client/*` | HUD, container overlay, packet receive, creative screen fixes |

## Known limits

- When the cursor is over a locked slot, the vanilla white hover highlight tints the red
  overlay. Hiding it would mean returning false from `Slot.doDrawHoveringEffect`, which in
  1.16.1 also stops the slot (and the overlay) from being drawn.
- Some creative and pick block paths write to a slot directly on the client. The server
  enforcer moves such items out on the next tick.
