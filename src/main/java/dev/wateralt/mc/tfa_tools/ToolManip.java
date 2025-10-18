package dev.wateralt.mc.tfa_tools;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class ToolManip {
  static final String MODULES_KEY = "tfa_tools.modules";
  static final String FILLED_BOXES = "▁▂▃▅▆▇";
  
  static final int NETHERITE_TOOL_SLOTS = 8;
  static final int DIAMOND_TOOL_SLOTS = 7;

  static final int DIAMOND_ARMOR_SLOTS = 4;
  static final int NETHERITE_ARMOR_SLOTS = 4;

  static final int DIAMOND_SWORD_SLOTS = 1; // todo
  static final int NETHERITE_SWORD_SLOTS = 1; // todo
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
  }
  
  public static int numSlots(Item item) {
    return SLOTS.get(item);
  }
  
  public static int numSlots(ItemStack item) {
    return numSlots(item.getItem());
  }
  
  public static boolean modularizeItem(ItemStack item) {
    AtomicBoolean ret = new AtomicBoolean(false);
    if(canBeModularized(item)) {
      item.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, nbt -> nbt.apply(v -> {
        if(v.getIntArray(MODULES_KEY).map(v2 -> Arrays.stream(v2).allMatch(v3 -> v3 == 0)).orElse(true)) {
          v.putIntArray(MODULES_KEY, new int[numSlots(item)]);
          ret.set(true);
        }
      }));
      item.addEnchantment(TfaTools.dynamicRegistries.getEntryOrThrow(Enchantments.UNBREAKING), 3);
      item.addEnchantment(TfaTools.dynamicRegistries.getEntryOrThrow(Enchantments.MENDING), 1);
      item.set(DataComponentTypes.REPAIR_COST, 999);
    }
    return ret.get();
  }
  
  public static boolean isModularized(ItemStack item) {
    NbtComponent comp = item.get(DataComponentTypes.CUSTOM_DATA);
    return comp != null && comp.copyNbt().contains(MODULES_KEY);
  }
  
  public static boolean canBeModularized(ItemStack item) {
    return SLOTS.containsKey(item.getItem());
  }
  
  public static boolean addModule(ItemStack item, int module) {
    AtomicBoolean ret = new AtomicBoolean(false);
    if(isModularized(item)) {
      item.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, nbt -> nbt.apply(v -> {
        v.getIntArray(MODULES_KEY).ifPresent(v2 -> {
          for (int i = 0; i < v2.length; i++) {
            if(v2[i] == 0) {
              v2[i] = module;
              ret.set(true);
              break;
            }
          }
        });
      }));
    }
    return ret.get();
  }
  
  public ArrayList<ItemStack> clearModules(ItemStack item) {
    int[] modules = getModules(item);
    if(isModularized(item)) {
      item.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, nbt -> nbt.apply(v -> {
        v.putIntArray(MODULES_KEY, new int[numSlots(item)]);
      }));
    }
    ArrayList<ItemStack> items = new ArrayList<>();
    Arrays.stream(modules).forEach(v -> {
      if(v != 0) {
        items.add(createModuleItem(v));
      }
    });
    return items;
  }
  
  public static ModuleTypes.ModuleType getModuleType(int module) {
    return ModuleTypes.MODULE_TYPES.get(module >> 16);
  }

  public static int getModuleStrength(int module) {
    return module & 0xffff;
  }

  public static int getLegalModuleStrength(int module) {
    ModuleTypes.ModuleType typ = getModuleType(module);
    return Math.clamp(getModuleStrength(module), typ.levelMin(), typ.levelMax());
  }
  
  public static int[] getModules(ItemStack item) {
    var ref = new Object() {
      int[] modules = new int[0];
    };
    item.get(DataComponentTypes.CUSTOM_DATA).apply(v -> {
      Optional<int[]> oia = v.getIntArray(MODULES_KEY);
      oia.ifPresent(ints -> ref.modules = Arrays.copyOf(ints, ints.length));
    });
    return ref.modules;
  }
  
  public static int[] getModuleEffects(int[] modules, boolean capped) {
    int[] caps = ModuleTypes.CAPS.stream().mapToInt(i->i).toArray();
    return getModuleEffects(modules, capped, caps);
  }

  public static int[] getModuleEffects(int[] modules, boolean capped, int[] caps) {
    int[] total = new int[ModuleTypes.MAX_ID + 1];
    addModuleEffects(total, caps, modules, capped);
    return total;
  }
  
  public static void addModuleEffects(int[] total, int[] caps, int[] modules, boolean capped) {
    Arrays.stream(modules).forEach(module -> {
      ModuleTypes.ModuleType typ = getModuleType(module);
      if(typ == null) return;
      int strength = getLegalModuleStrength(module);
      int cappedStrength;
      if(capped) {
        cappedStrength = Math.min(strength, caps[typ.capId()]);
        caps[typ.capId()] -= cappedStrength;
      } else {
        cappedStrength = strength;
      }
      total[typ.id()] += cappedStrength;
    });
  }
  
  public static ArrayList<Text> getLore(ItemStack item) {
    int[] modules = getModules(item);
    int[] caps = ModuleTypes.CAPS.stream().mapToInt(i->i).toArray();
    int[] moduleEffects = getModuleEffects(modules, true, caps);
    
    ArrayList<Text> texts = new ArrayList<>();
    MutableText slots = Text.empty();
    int populated = 0;
    for(int i = 0; i < modules.length; i++) {
      ModuleTypes.ModuleType typ = getModuleType(modules[i]);
      if(typ == null) {
        slots.append(Text.literal("-").formatted(Formatting.DARK_GRAY));
      } else {
        populated++;
        int strength = getLegalModuleStrength(modules[i]);
        int boxIdx = Math.clamp(strength - typ.levelMin(), 0, 5);
        slots.append(Text.literal(FILLED_BOXES.substring(boxIdx, boxIdx + 1)).formatted(typ.fmt()));
      }
    }
    slots.append(Text.literal(String.format(" %d/%d", populated, numSlots(item))).formatted(Formatting.DARK_GRAY));
    texts.add(slots);
    
    for(int i = 0; i < moduleEffects.length; i++) {
      ModuleTypes.ModuleType typ = ModuleTypes.MODULE_TYPES.get(i);
      if(typ == null) continue;
      String capText = caps[typ.capId()] == 0 ? "(cap)" : "";
      if(typ.binary()) {
        texts.add(Text.literal(typ.name()).formatted(typ.fmt()));
      } else {
        texts.add(Text.literal(String.format("%d.%d %s %s", moduleEffects[i] / 10, moduleEffects[i] % 10, typ.name(), capText)).formatted(typ.fmt()));
      }
    }
    
    return texts;
  }
  
  public static ItemStack createModuleItem(int module) {
    return new ItemStack(Items.CLOCK, 1);
  }
}
