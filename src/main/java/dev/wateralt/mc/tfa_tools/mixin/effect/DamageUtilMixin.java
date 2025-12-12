package dev.wateralt.mc.tfa_tools.mixin.effect;

import dev.wateralt.mc.tfa_tools.ModuleEffects;
import dev.wateralt.mc.tfa_tools.ModuleTypes;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DamageUtil.class)
public class DamageUtilMixin {
  @ModifyVariable(method = "getDamageLeft", at = @At(value = "HEAD"), ordinal = 2, argsOnly = true)
  private static float getDamageLeft(float armorToughness, LivingEntity armorWearer, float damageAmount, DamageSource damageSource, float armor, float armorToughness2) {
    if(ModuleEffects.quickGetEffect(damageSource.getWeaponStack(), ModuleTypes.FLINTSLATE) > 0) {
      return 0.0f;
    }
    return armorToughness;
  }
}
