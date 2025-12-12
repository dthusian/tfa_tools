package dev.wateralt.mc.tfa_tools.mixin.effect;

import dev.wateralt.mc.tfa_tools.ModuleEffects;
import dev.wateralt.mc.tfa_tools.ModuleTypes;
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

import java.util.List;

@Mixin(DamageSource.class)
public abstract class DamageSourceMixin {
  @Shadow public abstract DamageType getType();

  @Shadow @Final private @Nullable Entity source;

  @Inject(method = "getDeathMessage", at = @At("HEAD"), cancellable = true)
  public void getDeathMessage(LivingEntity killed, CallbackInfoReturnable<Text> cir) {
    if(getType().msgId().equals("inFire") && this.source instanceof ServerPlayerEntity spe) {
      Text attackerName = spe.getDisplayName();
      ItemStack weapon = spe.getMainHandStack();
      if(ModuleEffects.quickGetEffect(weapon, ModuleTypes.FLINTSLATE) > 0) {
        Text t = Text.empty();
        List<Text> siblings = t.getSiblings();
        siblings.add(killed.getDisplayName());
        siblings.add(Text.of(" was skewered, shredded, and seared by "));
        siblings.add(attackerName);
        if(!weapon.isEmpty() && weapon.contains(DataComponentTypes.CUSTOM_NAME)) {
          siblings.add(Text.of(" wielding "));
          siblings.add(weapon.toHoverableText());
        }
        cir.setReturnValue(t);
      }
    }
  }
}
