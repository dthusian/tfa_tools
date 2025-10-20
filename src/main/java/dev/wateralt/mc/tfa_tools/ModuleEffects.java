package dev.wateralt.mc.tfa_tools;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.stream.IntStreams;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ModuleEffects {
  static final List<Item> UNCAPPED = List.of(
    Items.COPPER_PICKAXE,
    Items.COPPER_SWORD,
    Items.COPPER_SHOVEL,
    Items.COPPER_HOE,
    Items.COPPER_AXE,
    
    Items.COPPER_HELMET,
    Items.COPPER_CHESTPLATE,
    Items.COPPER_LEGGINGS,
    Items.COPPER_BOOTS
  );
  
  public static boolean isBladedItem(@Nullable Item item) {
    boolean isSword = TfaTools.dynamicRegistries
      .getOrThrow(RegistryKeys.ITEM)
      .getOrThrow(ItemTags.SWORDS)
      .stream()
      .anyMatch(v -> v.value().equals(item));
    boolean isAxe = TfaTools.dynamicRegistries
      .getOrThrow(RegistryKeys.ITEM)
      .getOrThrow(ItemTags.AXES)
      .stream()
      .anyMatch(v -> v.value().equals(item));
    return isSword || isAxe || Items.TRIDENT.equals(item);
  }
  
  static void addAttribute(ItemStack item, Identifier id, RegistryEntry<EntityAttribute> attr, double value, EntityAttributeModifier.Operation op, AttributeModifierSlot slot) {
    item.apply(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT, v ->
      v.with(
        attr,
        new EntityAttributeModifier(id, value, op),
        slot
      )
    );
  }
  
  public static void updateModuleEffects(ItemStack item) {
    int[] moduleEffects = ToolManip.getModuleEffects(ToolManip.getModules(item), !UNCAPPED.contains(item.getItem()));
    Function<RegistryKey<Enchantment>, RegistryEntry<Enchantment>> getEnchant = TfaTools.dynamicRegistries::getEntryOrThrow;
    
    item.set(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
    item.addEnchantment(getEnchant.apply(Enchantments.UNBREAKING), 3);
    item.addEnchantment(getEnchant.apply(Enchantments.MENDING), 1);
    item.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, item.getItem().getComponents().get(DataComponentTypes.ATTRIBUTE_MODIFIERS));
    
    int efficiencyLv = moduleEffects[ModuleTypes.EFFICIENCY.id()];
    if(efficiencyLv > 0) {
      double efficiencyLvD = efficiencyLv / 10.0;
      addAttribute(item, Identifier.of("tfatools", "efficiency"),
        EntityAttributes.MINING_EFFICIENCY,
        efficiencyLvD * efficiencyLvD + 1,
        EntityAttributeModifier.Operation.ADD_VALUE,
        AttributeModifierSlot.MAINHAND);
    }
    
    if(moduleEffects[ModuleTypes.SILK_TOUCH.id()] > 0) {
      item.addEnchantment(getEnchant.apply(Enchantments.SILK_TOUCH), 1);
    }
    
    // todo fortune
    
    // todo durability
    
    // sharpness, flintslate, kb, prots implemented via mixin
    
    if(moduleEffects[ModuleTypes.AQUA_AFFINITY.id()] > 0 && item.isIn(ItemTags.HEAD_ARMOR)) {
      item.addEnchantment(getEnchant.apply(Enchantments.AQUA_AFFINITY), 1);
      item.addEnchantment(getEnchant.apply(Enchantments.RESPIRATION), 3);
    }
    if(moduleEffects[ModuleTypes.AQUA_AFFINITY.id()] > 0 && item.isIn(ItemTags.FOOT_ARMOR)) {
      item.addEnchantment(getEnchant.apply(Enchantments.DEPTH_STRIDER), 3);
    }
    if(moduleEffects[ModuleTypes.SOUL_SPEED.id()] > 0 && item.isIn(ItemTags.FOOT_ARMOR)) {
      item.addEnchantment(getEnchant.apply(Enchantments.SOUL_SPEED), 3);
    }
    if(moduleEffects[ModuleTypes.SWIFT_SNEAK.id()] > 0 && item.isIn(ItemTags.LEG_ARMOR)) {
      item.addEnchantment(getEnchant.apply(Enchantments.SWIFT_SNEAK), 3);
    }
    if(moduleEffects[ModuleTypes.FROST_WALKER.id()] > 0 &&
      moduleEffects[ModuleTypes.AQUA_AFFINITY.id()] == 0 && item.isIn(ItemTags.FOOT_ARMOR)) {
      item.addEnchantment(getEnchant.apply(Enchantments.FROST_WALKER), 2);
    }
  }

  public static int quickGetEffect(ItemStack item, ModuleTypes.Type typ) {
    //todo faster impl?
    if(ToolManip.isModularized(item))
      return ToolManip.getModuleEffects(ToolManip.getModules(item), !ModuleEffects.UNCAPPED.contains(item.getItem()))[typ.id()];
    return 0;
  }
  
  public static float quickGetEffectFloat(ItemStack item, ModuleTypes.Type typ) {
    return quickGetEffect(item, typ) / 10f;
  }
  
  public static int[] getArmorEffects(LivingEntity ent, MutableInt modularizedItems) {
    modularizedItems.setValue(0);
    int[] total = new int[ModuleTypes.NUM_TYPES];
    int[] sharedCaps = ModuleTypes.CAPS.stream().mapToInt(i->i).toArray();
    int[] selfCaps = IntStreams.range(ModuleTypes.NUM_TYPES).map(v -> ModuleTypes.MODULE_TYPES[v].cap()).toArray();
    EquipmentSlot.VALUES.stream()
      .map(ent::getEquippedStack)
      .filter(Objects::nonNull)
      .forEach(v -> {
        if(ToolManip.isModularized(v)) modularizedItems.add(1);
        ToolManip.addModuleEffects(total, ToolManip.getModules(v), sharedCaps, selfCaps, !UNCAPPED.contains(v.getItem()));
      });
    return total;
  }
}
