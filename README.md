# Flesh Terror — Minecraft Forge Mod

Adds the **Flesh Horror**: a growing, wither-storm-style monster made of living flesh.
It starts small, and its tentacles rip blocks out of nearby structures (planks, stone,
bricks, glass, wool, ore blocks, etc.) to feed and grow through 5 stages, ending as a
massive colossus with a boss health bar.

- Summon it by crafting a **Flesh Heart** block and right-clicking it (see recipe below).
- Comes with a **Flesh Horror Spawn Egg** for creative testing.
- Custom procedurally-painted flesh texture, custom tentacled model, custom AI.

This is source code for **Minecraft 1.20.1 + Forge 47.2.0**. You need to compile it
into a .jar yourself (explained below) — I can't download Mojang/Forge's proprietary
game files in this sandbox to compile it for you, but the steps below are copy-paste simple.

---

## 1. What you need installed

1. **Java Development Kit 17** — download from https://adoptium.net/temurin/releases/?version=17
   (pick the JDK, not just JRE, for your OS).
2. That's it — Gradle itself gets downloaded automatically by the wrapper script in the
   Forge MDK (see next step).

---

## 2. Get the official Forge MDK (Mod Development Kit)

This gives you the Gradle wrapper + Forge/Minecraft dependencies that my source code needs.

1. Go to https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html
2. Under the **Recommended** or a **47.2.0**-ish build, download **"Mdk"** (Mod Developer Kit) — it's a zip.
3. Extract that zip into a new empty folder, e.g. `FleshTerrorProject/`.

---

## 3. Drop my source files into it

1. Unzip the `fleshterror-mod-source.zip` I gave you.
2. Copy these two things from it **into** `FleshTerrorProject/`, overwriting when asked:
   - `src/` (this replaces the MDK's example `src/` folder — the "ExampleMod" is gone,
     replaced by Flesh Terror)
   - `build.gradle` (overwrite the MDK's default one — mine is preconfigured with the mod's
     info so you don't have to edit anything)

   Your `FleshTerrorProject/` folder should now look like:
   ```
   FleshTerrorProject/
     build.gradle          <- mine
     settings.gradle       <- from MDK (keep it, or use mine, either works)
     gradlew, gradlew.bat  <- from MDK (needed to build)
     gradle/               <- from MDK (needed to build)
     src/main/java/...     <- mine
     src/main/resources/...<- mine
   ```

---

## 4. (Optional) Test it in a dev environment first

From inside `FleshTerrorProject/`, open a terminal and run:

```
./gradlew runClient
```
(Windows: `gradlew.bat runClient`)

The first run takes a while (downloads Minecraft + Forge + decompiles), then it opens
a Minecraft window with the mod already loaded. Good way to check everything works
before making the final jar.

---

## 5. Build the actual .jar file

In the same terminal, run:

```
./gradlew build
```
(Windows: `gradlew.bat build`)

When it finishes, your mod jar is at:

```
FleshTerrorProject/build/libs/fleshterror-1.0.0.jar
```

That's the file you drop into your Minecraft `mods` folder (with Forge 1.20.1 + Forge
mod loader already installed on your Minecraft client/server).

---

## 6. In game

1. Craft a **Flesh Heart**:
   ```
   [Rotten Flesh] [Redstone] [Rotten Flesh]
   [Redstone]     [Nether Wart Block] [Redstone]
   [Rotten Flesh] [Redstone] [Rotten Flesh]
   ```
2. Place the Flesh Heart block down.
3. Right-click it. It'll consume itself in a burst of particles/sound and spawn the
   small Flesh Horror (Stage 0 — "Spawnling") on top of it.
4. Leave it near any builds — it'll wander over, grab blocks out of walls/floors with
   its tentacles, and grow. It goes through 5 stages: Spawnling → Juvenile → Adult →
   Elder → Colossus. From Adult onward, a boss health bar appears.
5. Or spawn one instantly anywhere with the Flesh Horror Spawn Egg from creative mode.

---

## Notes / things you can easily tweak

- `FleshMonsterEntity.POINTS_TO_NEXT_STAGE` — how many blocks it must eat to grow.
- `FleshMonsterEntity.applyStageAttributes()` — health/damage/speed scaling per stage.
- `data/fleshterror/tags/blocks/grabbable_structure_blocks.json` — add/remove which
  blocks count as "structure" it can rip out.
- `TentacleGrabGoal` — grab radius, grab speed, cooldown.
- The model (`FleshMonsterModel.java`) is a hand-coded 6-tentacle blob. If you want a
  fancier shape, open the mesh in Blockbench (blockbench.net), tweak it visually, and
  export as a Java entity model — you can drop the generated cube list straight into
  `createBodyLayer()`.
- Texture is at `src/main/resources/assets/fleshterror/textures/entity/flesh_monster.png`
  — feel free to hand-paint over the procedural one in any image editor (keep it 128x64).
