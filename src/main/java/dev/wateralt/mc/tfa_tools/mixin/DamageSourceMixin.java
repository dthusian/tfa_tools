package dev.wateralt.mc.tfa_tools.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageSource.class)
public abstract class DamageSourceMixin {
  @Shadow public abstract DamageType getType();

  @Shadow @Final private @Nullable Entity source;

  @Inject(method = "getDeathMessage", at = @At("HEAD"), cancellable = true)
  public void getDeathMessage(LivingEntity killed, CallbackInfoReturnable<Text> cir) {
    String translationKey = "death.attack." + getType().msgId();
    if(this.source instanceof ServerPlayerEntity spe) {
      Text attackerName = spe.getDisplayName();
      ItemStack weapon = spe.getMainHandStack();
      cir.setReturnValue(!weapon.isEmpty() && weapon.contains(DataComponentTypes.CUSTOM_NAME)
        ? Text.translatable(translationKey + ".item", killed.getDisplayName(), attackerName, weapon.toHoverableText())
        : Text.translatable(translationKey + ".player", killed.getDisplayName(), attackerName));
    }
  }
}
