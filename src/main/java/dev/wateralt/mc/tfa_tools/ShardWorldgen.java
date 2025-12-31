package dev.wateralt.mc.tfa_tools;

import net.minecraft.item.ItemStack;
import static dev.wateralt.mc.tfa_tools.ModuleTypes.*;

import java.util.*;

public class ShardWorldgen {
  public static final double BIAS_TOWARDS_RATE = 0.3;

  public record GenerationInfo(List<ModuleTypes.Type> biasTowards, List<ModuleTypes.Type> generatable, double rate, double meanStrength) { }

  public static final List<ModuleTypes.Type> OVERWORLD_DEFAULT = List.of(
    EFFICIENCY,
    FORTUNE,
    SILK_TOUCH,
    DURABILITY,

    SHARPNESS,
    KNOCKBACK,

    BLUNT_PROT,
    BLADE_PROT,
    BLAST_PROT,
    PROJ_PROT,
    MAGIC_PROT,
    FEATHER_FALLING
  );
  public static final List<ModuleTypes.Type> NETHER_EXCLUSIVES = List.of(
    FIRE_PROT,
    FLINTSLATE
  );
  public static final List<ModuleTypes.Type> REMOVE_FROM_CHEST_LOOT = List.of(
    SWIFT_SNEAK,
    SOUL_SPEED,
    FROST_WALKER,
    AQUA_AFFINITY
  );
  public static final List<ModuleTypes.Type> ALL_CHEST_LOOT = Arrays.stream(MODULE_TYPES)
    .filter(v -> !REMOVE_FROM_CHEST_LOOT.contains(v))
    .toList();

  public static final HashMap<String, GenerationInfo> MODULE_GENERATION = new HashMap<>();
  static {
    final double t1Strength = 0.4;
    final double t2Strength = 0.5;
    final double t3Strength = 0.6;

    // Tier 1 structures
    MODULE_GENERATION.put("minecraft:chests/abandoned_mineshaft", new GenerationInfo(List.of(EFFICIENCY), OVERWORLD_DEFAULT, 0.8, t1Strength));
    MODULE_GENERATION.put("minecraft:chests/simple_dungeon", new GenerationInfo(List.of(FORTUNE, SILK_TOUCH), OVERWORLD_DEFAULT, 1.0, t1Strength));
    MODULE_GENERATION.put("minecraft:chests/stronghold_library", new GenerationInfo(List.of(MAGIC_PROT), OVERWORLD_DEFAULT, 1.0, t1Strength));
    MODULE_GENERATION.put("minecraft:chests/pillager_outpost", new GenerationInfo(List.of(PROJ_PROT), OVERWORLD_DEFAULT, 1.0, t1Strength));
    MODULE_GENERATION.put("minecraft:chests/trial_chambers/reward", new GenerationInfo(List.of(), OVERWORLD_DEFAULT, 0.8, t1Strength));
    MODULE_GENERATION.put("minecraft:chests/desert_pyramid", new GenerationInfo(List.of(FEATHER_FALLING), OVERWORLD_DEFAULT, 1.0, t1Strength));
    MODULE_GENERATION.put("minecraft:chests/jungle_temple", new GenerationInfo(List.of(KNOCKBACK), OVERWORLD_DEFAULT, 1.0, t1Strength));

    // Tier 2 structures
    MODULE_GENERATION.put("minecraft:chests/ruined_portal", new GenerationInfo(List.of(), NETHER_EXCLUSIVES, 0.05, t2Strength));
    MODULE_GENERATION.put("minecraft:chests/woodland_mansion", new GenerationInfo(List.of(SHARPNESS), OVERWORLD_DEFAULT, 0.8, t2Strength));
    MODULE_GENERATION.put("minecraft:chests/nether_bridge", new GenerationInfo(List.of(FIRE_PROT), ALL_CHEST_LOOT, 0.8, t2Strength));
    MODULE_GENERATION.put("minecraft:chests/bastion_hoglin_stable", new GenerationInfo(List.of(BLADE_PROT), ALL_CHEST_LOOT, 0.8, t2Strength));
    MODULE_GENERATION.put("minecraft:chests/bastion_bridge", new GenerationInfo(List.of(BLADE_PROT), ALL_CHEST_LOOT, 0.8, t2Strength));
    MODULE_GENERATION.put("minecraft:chests/bastion_treasure", new GenerationInfo(List.of(FLINTSLATE), ALL_CHEST_LOOT, 1.0, t2Strength));
    MODULE_GENERATION.put("minecraft:chests/bastion_other", new GenerationInfo(List.of(BLADE_PROT), ALL_CHEST_LOOT, 0.8, t2Strength));
    MODULE_GENERATION.put("minecraft:chests/trial_chambers/reward_ominous", new GenerationInfo(List.of(), OVERWORLD_DEFAULT, 1.0, t2Strength));
    MODULE_GENERATION.put("minecraft:chests/ancient_city", new GenerationInfo(List.of(SWIFT_SNEAK), OVERWORLD_DEFAULT, 0.8, t2Strength));

    // Tier 3 structures
    MODULE_GENERATION.put("minecraft:chests/end_city_treasure", new GenerationInfo(List.of(), ALL_CHEST_LOOT, 0.8, t3Strength));

    // Special cases
    MODULE_GENERATION.put("minecraft:chests/ancient_city_ice_box", new GenerationInfo(List.of(), List.of(FROST_WALKER), 1.0, 0.3));
    MODULE_GENERATION.put("minecraft:chests/shipwreck_treasure", new GenerationInfo(List.of(), List.of(AQUA_AFFINITY), 0.8, 0.3));
    MODULE_GENERATION.put("minecraft:chests/underwater_ruin_big", new GenerationInfo(List.of(), List.of(AQUA_AFFINITY), 0.8, 0.3));
    MODULE_GENERATION.put("minecraft:chests/underwater_ruin_small", new GenerationInfo(List.of(), List.of(AQUA_AFFINITY), 0.8, 0.3));
  }
  
  public static Optional<ItemStack> generateShard(String lootTableId, long lootTableSeed) {
    //TfaTools.LOGGER.info("Generating shard for {}", lootTableId);
    GenerationInfo info = MODULE_GENERATION.get(lootTableId);
    if (info != null) {
      Random rng = new Random(lootTableSeed);
      boolean shouldGenerate = rng.nextDouble() < info.rate();
      ModuleTypes.Type typ = rng.nextDouble() < BIAS_TOWARDS_RATE && !info.biasTowards().isEmpty()
        ? info.biasTowards().get(rng.nextInt(info.biasTowards().size()))
        : info.generatable().get(rng.nextInt(info.generatable().size()));
      int strength = typ.levelMin();
      for (int i = 0; i < typ.levelMax() - typ.levelMin(); i++) {
        strength += rng.nextDouble() < info.meanStrength() ? 1 : 0;
      }
      strength = Math.min(strength, typ.levelMax());
      if (shouldGenerate) {
        int module = ToolManip.moduleFromRawParts(typ.id(), strength);
        //TfaTools.LOGGER.info("Generated module: {} {}", typ.name(), strength);
        return Optional.of(ToolManip.createModuleItem(module));
      }
    }
    return Optional.empty();
  }
}
