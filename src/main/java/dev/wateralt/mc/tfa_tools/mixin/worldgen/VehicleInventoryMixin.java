package dev.wateralt.mc.tfa_tools.mixin.worldgen;

import dev.wateralt.mc.tfa_tools.ShardWorldgen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.VehicleInventory;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(VehicleInventory.class)
public interface VehicleInventoryMixin {
  @Shadow World getEntityWorld();

  @Inject(method = "generateInventoryLoot", at = @At("HEAD"))
  default void generateLoot(PlayerEntity player, CallbackInfo ci) {
    VehicleInventory that = (VehicleInventory) this;
    if(getEntityWorld() != null && getEntityWorld().isClient()) return;
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
