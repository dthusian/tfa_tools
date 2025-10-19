package dev.wateralt.mc.tfa_tools.mixin.effect;

import dev.wateralt.mc.tfa_tools.ModuleEffects;
import dev.wateralt.mc.tfa_tools.ModuleTypes;
import dev.wateralt.mc.tfa_tools.ToolManip;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
  @Inject(method = "getDamage", at = @At("RETURN"), cancellable = true)
  private static void getDamage(ServerWorld world, ItemStack stack, Entity target, DamageSource damageSource, float baseDamage, CallbackInfoReturnable<Float> cir) {
    float dmg = baseDamage;
    float sharpLv = ModuleEffects.quickGetEffect(stack, ModuleTypes.SHARPNESS) / 10.0f;
    if(sharpLv > 0) dmg += sharpLv * 0.5f + 1.0f;
    cir.setReturnValue(dmg);
  }
  
  @Inject(method = "modifyKnockback", at = @At("HEAD"), cancellable = true)
  private static void modifyKnockback(ServerWorld world, ItemStack stack, Entity target, DamageSource damageSource, float baseKnockback, CallbackInfoReturnable<Float> cir) {
    float kb = baseKnockback;
    float kbLv = ModuleEffects.quickGetEffect(stack, ModuleTypes.KNOCKBACK) / 10.0f;
    if(kbLv > 0) kb += kbLv + 1.0f;
    cir.setReturnValue(kb);
  }
}
