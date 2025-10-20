package dev.wateralt.mc.tfa_tools.mixin.effect;

import com.llamalad7.mixinextras.sugar.Local;
import dev.wateralt.mc.tfa_tools.ModuleEffects;
import dev.wateralt.mc.tfa_tools.ModuleTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.TagEntry;
import net.minecraft.server.world.ServerWorld;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

  @Inject(method = "getDamage", at = @At("RETURN"), cancellable = true)
  private static void getDamage(ServerWorld world, ItemStack stack, Entity target, DamageSource damageSource, float baseDamage, CallbackInfoReturnable<Float> cir) {
    float dmg = baseDamage;
    float sharpLv = ModuleEffects.quickGetEffectFloat(stack, ModuleTypes.SHARPNESS);
    if(sharpLv > 0) {
      dmg += sharpLv * 0.5f + 0.5f;
      cir.setReturnValue(dmg);
    }
  }
  
  @Inject(method = "modifyKnockback", at = @At("HEAD"), cancellable = true)
  private static void modifyKnockback(ServerWorld world, ItemStack stack, Entity target, DamageSource damageSource, float baseKnockback, CallbackInfoReturnable<Float> cir) {
    float kb = baseKnockback;
    float kbLv = ModuleEffects.quickGetEffectFloat(stack, ModuleTypes.KNOCKBACK);
    if(kbLv > 0) {
      kb += kbLv;
      cir.setReturnValue(kb);
    }
  }
  
  @Inject(method = "getProtectionAmount", at = @At("HEAD"), cancellable = true)
  private static void getProtectionAmount(ServerWorld world, LivingEntity user, DamageSource damageSource, CallbackInfoReturnable<Float> cir) {
    MutableInt modularizedItems = new MutableInt(0);
    int[] effects = ModuleEffects.getArmorEffects(user, modularizedItems);
    if(modularizedItems.intValue() == 0) return;
    ModuleTypes.Type protType = null;
    RegistryEntry<DamageType> damageType = damageSource.getTypeRegistryEntry();
    RegistryKey<DamageType> damageTypeKey = damageSource.getTypeRegistryEntry().getKey().orElse(null);
    boolean isWeaponDamage = List.of(
      DamageTypes.PLAYER_ATTACK,
      DamageTypes.MOB_ATTACK,
      DamageTypes.MOB_ATTACK_NO_AGGRO
    ).contains(damageTypeKey);
    ItemStack weapon = Objects.requireNonNullElse(damageSource.getWeaponStack(), ItemStack.EMPTY);
    boolean isBladedWeapon = ModuleEffects.isBladedItem(weapon.getItem());
    if(damageType.isIn(DamageTypeTags.IS_FIRE)) {
      protType = ModuleTypes.FIRE_PROT;
    }
    if(damageType.isIn(DamageTypeTags.IS_FALL) || Objects.equals(DamageTypes.FLY_INTO_WALL, damageTypeKey)) {
      protType = ModuleTypes.FEATHER_FALLING;
    }
    if(damageType.isIn(DamageTypeTags.IS_EXPLOSION)) {
      protType = ModuleTypes.BLAST_PROT;
    }
    if(damageType.isIn(DamageTypeTags.IS_PROJECTILE)) {
      protType = ModuleTypes.PROJ_PROT;
    }
    if(List.of(
      DamageTypes.CACTUS,
      DamageTypes.STALAGMITE,
      DamageTypes.FALLING_STALACTITE,
      DamageTypes.STING,
      DamageTypes.THORNS
    ).contains(damageTypeKey) || (isWeaponDamage && isBladedWeapon)) {
      protType = ModuleTypes.BLADE_PROT;
    }
    if(List.of(
      DamageTypes.IN_WALL,
      DamageTypes.CRAMMING,
      DamageTypes.FALLING_BLOCK,
      DamageTypes.FALLING_ANVIL,
      DamageTypes.MACE_SMASH
    ).contains(damageTypeKey) || (isWeaponDamage && !isBladedWeapon)) {
      protType = ModuleTypes.BLUNT_PROT;
    }
    if(List.of(
      DamageTypes.LIGHTNING_BOLT,
      DamageTypes.MAGIC,
      DamageTypes.DRAGON_BREATH,
      DamageTypes.INDIRECT_MAGIC,
      DamageTypes.SONIC_BOOM
    ).contains(damageTypeKey)) {
      protType = ModuleTypes.MAGIC_PROT;
    }
    if(protType != null) {
      cir.setReturnValue(effects[protType.id()] * 2.0f);
    }
  }
  
  @Inject(method = "getEquipmentDropChance", at = @At("TAIL"))
  private static void getEquipmentDropChance(ServerWorld world, LivingEntity attacker, DamageSource damageSource, float baseEquipmentDropChance, CallbackInfoReturnable<Float> cir, @Local MutableFloat mutableFloat) {
    ArrayList<ItemStack> items = new ArrayList<>();
    items.add(attacker.getWeaponStack());
    if(damageSource.getAttacker() instanceof LivingEntity source)
      items.add(source.getWeaponStack());
    items.forEach(v -> {
      mutableFloat.add(ModuleEffects.quickGetEffectFloat(v, ModuleTypes.FORTUNE) * 0.01);
    });
  }
}
