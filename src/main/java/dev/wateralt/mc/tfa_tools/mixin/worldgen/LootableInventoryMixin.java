package dev.wateralt.mc.tfa_tools.mixin.worldgen;

import dev.wateralt.mc.tfa_tools.ModuleTypes;
import dev.wateralt.mc.tfa_tools.ShardWorldgen;
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
      long seed = that.getLootTableSeed();
      Random rng = new Random(seed);
      RegistryKey<LootTable> savedLootTable = that.getLootTable();
      that.setLootTable(null);
      String lootTableId = savedLootTable.getValue().toString();
      ShardWorldgen.generateShard(lootTableId, seed).ifPresent(v -> that.setStack(rng.nextInt(0, that.size()), v));
      that.setLootTable(savedLootTable);
    }
  }
}
