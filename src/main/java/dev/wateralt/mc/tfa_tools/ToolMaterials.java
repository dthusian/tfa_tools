package dev.wateralt.mc.tfa_tools;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.HashMap;

public class ToolMaterials {
  static final int NETHERITE_TOOL_SLOTS = 8;
  static final int DIAMOND_TOOL_SLOTS = 7;
  static final int COPPER_TOOL_SLOTS = 3;
  static final int GOLD_TOOL_SLOTS = 10;
  static final int IRON_TOOL_SLOTS = 4;

  static final int NETHERITE_ARMOR_SLOTS = 4;
  static final int DIAMOND_ARMOR_SLOTS = 4;
  static final int COPPER_ARMOR_SLOTS = 3;
  static final int GOLD_ARMOR_SLOTS = 5;
  static final int IRON_ARMOR_SLOTS = 3;

  static final int NETHERITE_SWORD_SLOTS = 8;
  static final int DIAMOND_SWORD_SLOTS = 7;
  static final int COPPER_SWORD_SLOTS = 3;
  static final int GOLD_SWORD_SLOTS = 10;
  static final int IRON_SWORD_SLOTS = 4;

  static final HashMap<Item, Integer> SLOTS = new HashMap<>();
  static {
    SLOTS.put(Items.DIAMOND_AXE, DIAMOND_TOOL_SLOTS);
    SLOTS.put(Items.DIAMOND_PICKAXE, DIAMOND_TOOL_SLOTS);
    SLOTS.put(Items.DIAMOND_SWORD, DIAMOND_SWORD_SLOTS);
    SLOTS.put(Items.DIAMOND_HELMET, DIAMOND_ARMOR_SLOTS);
    SLOTS.put(Items.DIAMOND_CHESTPLATE, DIAMOND_ARMOR_SLOTS);
    SLOTS.put(Items.DIAMOND_LEGGINGS, DIAMOND_ARMOR_SLOTS);
    SLOTS.put(Items.DIAMOND_BOOTS, DIAMOND_ARMOR_SLOTS);

    SLOTS.put(Items.NETHERITE_AXE, NETHERITE_TOOL_SLOTS);
    SLOTS.put(Items.NETHERITE_PICKAXE, NETHERITE_TOOL_SLOTS);
    SLOTS.put(Items.NETHERITE_SWORD, NETHERITE_SWORD_SLOTS);
    SLOTS.put(Items.NETHERITE_HELMET, NETHERITE_ARMOR_SLOTS);
    SLOTS.put(Items.NETHERITE_CHESTPLATE, NETHERITE_ARMOR_SLOTS);
    SLOTS.put(Items.NETHERITE_LEGGINGS, NETHERITE_ARMOR_SLOTS);
    SLOTS.put(Items.NETHERITE_BOOTS, NETHERITE_ARMOR_SLOTS);

    SLOTS.put(Items.COPPER_AXE, COPPER_TOOL_SLOTS);
    SLOTS.put(Items.COPPER_PICKAXE, COPPER_TOOL_SLOTS);
    SLOTS.put(Items.COPPER_SWORD, COPPER_SWORD_SLOTS);
    SLOTS.put(Items.COPPER_HELMET, COPPER_ARMOR_SLOTS);
    SLOTS.put(Items.COPPER_CHESTPLATE, COPPER_ARMOR_SLOTS);
    SLOTS.put(Items.COPPER_LEGGINGS, COPPER_ARMOR_SLOTS);
    SLOTS.put(Items.COPPER_BOOTS, COPPER_ARMOR_SLOTS);
    
    SLOTS.put(Items.IRON_AXE, IRON_TOOL_SLOTS);
    SLOTS.put(Items.IRON_PICKAXE, IRON_TOOL_SLOTS);
    SLOTS.put(Items.IRON_SWORD, IRON_SWORD_SLOTS);
    SLOTS.put(Items.IRON_HELMET, IRON_ARMOR_SLOTS);
    SLOTS.put(Items.IRON_CHESTPLATE, IRON_ARMOR_SLOTS);
    SLOTS.put(Items.IRON_LEGGINGS, IRON_ARMOR_SLOTS);
    SLOTS.put(Items.IRON_BOOTS, IRON_ARMOR_SLOTS);

    SLOTS.put(Items.GOLDEN_AXE, GOLD_TOOL_SLOTS);
    SLOTS.put(Items.GOLDEN_PICKAXE, GOLD_TOOL_SLOTS);
    SLOTS.put(Items.GOLDEN_SWORD, GOLD_SWORD_SLOTS);
    SLOTS.put(Items.GOLDEN_HELMET, GOLD_ARMOR_SLOTS);
    SLOTS.put(Items.GOLDEN_CHESTPLATE, GOLD_ARMOR_SLOTS);
    SLOTS.put(Items.GOLDEN_LEGGINGS, GOLD_ARMOR_SLOTS);
    SLOTS.put(Items.GOLDEN_BOOTS, GOLD_ARMOR_SLOTS);
  }

  public static int numSlots(ItemStack item) {
    return SLOTS.get(item.getItem());
  }
}
