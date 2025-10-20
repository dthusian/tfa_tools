package dev.wateralt.mc.tfa_tools.mixin;

import dev.wateralt.mc.tfa_tools.ModuleTypes;
import dev.wateralt.mc.tfa_tools.ToolManip;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(LootableInventory.class)
public interface LootableInventoryMixin {
  @Inject(method = "generateLoot", at = @At("HEAD"))
  private void generateLoot(PlayerEntity player, CallbackInfo ci) {
    LootableInventory that = (LootableInventory) this;
    if(that.getWorld() != null && that.getWorld().isClient()) return;
    if(that.getLootTable() != null) {
      RegistryKey<LootTable> savedLootTable = that.getLootTable();
      that.setLootTable(null);
      String lootTableId = savedLootTable.getValue().toString();
      ModuleTypes.GenerationInfo info = ModuleTypes.MODULE_GENERATION.get(lootTableId);
      if(info != null) {
        Random rng = new Random(that.getLootTableSeed());
        boolean shouldGenerate = rng.nextDouble() < info.rate();
        ModuleTypes.Type typ = rng.nextDouble() < ModuleTypes.BIAS_TOWARDS_RATE && !info.biasTowards().isEmpty()
          ? info.biasTowards().get(rng.nextInt(info.biasTowards().size()))
          : ModuleTypes.MODULE_TYPES[rng.nextInt(0, ModuleTypes.NUM_TYPES)];
        int strength = typ.levelMin();
        for(int i = 0; i < typ.levelMax() - typ.levelMin(); i++) {
          strength += rng.nextDouble() < info.meanStrength() ? 1 : 0;
        }
        if(shouldGenerate) {
          that.setStack(rng.nextInt(0, that.size()), ToolManip.createModuleItem(ToolManip.moduleFromRawParts(typ.id(), strength)));
        }
      }
      that.setLootTable(savedLootTable);
    }
  }
}
