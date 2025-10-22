package dev.wateralt.mc.tfa_tools;

import net.minecraft.item.ItemStack;

import java.util.Optional;
import java.util.Random;

public class ShardWorldgen {
  public static Optional<ItemStack> generateShard(String lootTableId, long lootTableSeed) {
    ModuleTypes.GenerationInfo info = ModuleTypes.MODULE_GENERATION.get(lootTableId);
    if (info != null) {
      Random rng = new Random(lootTableSeed);
      boolean shouldGenerate = rng.nextDouble() < info.rate();
      ModuleTypes.Type typ = rng.nextDouble() < ModuleTypes.BIAS_TOWARDS_RATE && !info.biasTowards().isEmpty()
        ? info.biasTowards().get(rng.nextInt(info.biasTowards().size()))
        : info.generatable().get(rng.nextInt(ModuleTypes.NUM_TYPES));
      int strength = typ.levelMin();
      for (int i = 0; i < typ.levelMax() - typ.levelMin(); i++) {
        strength += rng.nextDouble() < info.meanStrength() ? 1 : 0;
      }
      strength = Math.min(strength, typ.levelMax());
      if (shouldGenerate) {
        return Optional.of(ToolManip.createModuleItem(ToolManip.moduleFromRawParts(typ.id(), strength)));
      }
    }
    return Optional.empty();
  }
}
