package dev.wateralt.mc.tfa_tools.mixin.worldgen;

import dev.wateralt.mc.tfa_tools.ShardWorldgen;
import net.minecraft.block.vault.VaultConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(targets = "net.minecraft.block.entity.VaultBlockEntity.Server")
public class VaultBlockEntityMixin {
  @Inject(method = "generateLoot", at = @At("RETURN"))
  private static void generateLoot(ServerWorld world, VaultConfig config, BlockPos pos, PlayerEntity player, ItemStack key, CallbackInfoReturnable<List<ItemStack>> cir) {
    ShardWorldgen.generateShard(config.lootTable().getValue().toString(), world.getTime()).ifPresent(v -> cir.getReturnValue().add(v));
  }
}
