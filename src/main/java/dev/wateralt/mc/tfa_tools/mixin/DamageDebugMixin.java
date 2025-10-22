package dev.wateralt.mc.tfa_tools.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class DamageDebugMixin {
  @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setHealth(F)V"))
  void damage(ServerWorld world, DamageSource source, float amount, CallbackInfo ci, @Local(ordinal = 1) float var10) {
    if(true && source.getAttacker() instanceof ServerPlayerEntity spe) {
      spe.sendMessage(Text.of("Damage: %.3f".formatted(var10)), true);
    }
  }
}
