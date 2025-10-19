package dev.wateralt.mc.tfa_tools;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.function.Function;

public class ModuleEffects {
  static final List<Item> UNCAPPED = List.of(
    Items.COPPER_PICKAXE //todo
  );
  
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
    item.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
    
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
    
    // sharpness, flintslate, kb implemented via mixin
  }

  public static int quickGetEffect(ItemStack item, ModuleTypes.ModuleType typ) {
    //todo faster impl?
    return ToolManip.getModuleEffects(ToolManip.getModules(item), ModuleEffects.UNCAPPED.contains(item.getItem()))[typ.id()];
  }
}
