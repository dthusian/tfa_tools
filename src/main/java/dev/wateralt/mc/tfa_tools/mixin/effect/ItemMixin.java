package dev.wateralt.mc.tfa_tools.mixin.effect;

import dev.wateralt.mc.tfa_tools.ModuleEffects;
import dev.wateralt.mc.tfa_tools.ModuleTypes;
import dev.wateralt.mc.tfa_tools.ToolManip;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
  @Inject(method = "getDamageSource", at = @At("HEAD"), cancellable = true)
  public void getDamageSource(LivingEntity user, CallbackInfoReturnable<DamageSource> cir) {
    ItemStack stack = user.getWeaponStack();
    if(ModuleEffects.quickGetEffect(stack, ModuleTypes.FLINTSLATE) > 0) {
      cir.setReturnValue(new DamageSource(user.getDamageSources().onFire().getTypeRegistryEntry(), user));
    }
  }
}
