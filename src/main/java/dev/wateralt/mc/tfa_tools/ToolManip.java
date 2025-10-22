package dev.wateralt.mc.tfa_tools;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.stream.IntStreams;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ToolManip {
  static final String MODULES_KEY = "tfa_tools.modules";
  static final String MODULE_EFFECT_KEY = "tfa_tools.effect";
  static final String FILLED_BOXES = "▇▇▇▇▇▇"; // "▁▂▃▅▆▇"
  static final int MODULE_EMPTY = 0;
  
  public static boolean modularizeItem(ItemStack item) {
    AtomicBoolean ret = new AtomicBoolean(false);
    if(canBeModularized(item)) {
      item.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, nbt -> nbt.apply(v -> {
        if(v.getIntArray(MODULES_KEY).map(v2 -> Arrays.stream(v2).allMatch(v3 -> v3 == MODULE_EMPTY)).orElse(true)) {
          v.putIntArray(MODULES_KEY, new int[ToolMaterials.numSlots(item)]);
          ret.set(true);
        }
      }));
      item.addEnchantment(TfaTools.dynamicRegistries.getEntryOrThrow(Enchantments.UNBREAKING), 3);
      item.addEnchantment(TfaTools.dynamicRegistries.getEntryOrThrow(Enchantments.MENDING), 1);
      item.set(DataComponentTypes.LORE, new LoreComponent(getLore(item)));
      TreeSet<ComponentType<?>> set = new TreeSet<>((a, b) -> 0);
      set.add(DataComponentTypes.ENCHANTMENTS);
      item.set(DataComponentTypes.TOOLTIP_DISPLAY, new TooltipDisplayComponent(false, set));
    }
    return ret.get();
  }
  
  public static boolean isModularized(ItemStack item) {
    NbtComponent comp = item.get(DataComponentTypes.CUSTOM_DATA);
    return comp != null && comp.copyNbt().contains(MODULES_KEY);
  }
  
  public static boolean canBeModularized(ItemStack item) {
    return ToolMaterials.SLOTS.containsKey(item.getItem());
  }
  
  public static boolean addModule(ItemStack item, int module) {
    AtomicBoolean ret = new AtomicBoolean(false);
    if(isModularized(item)) {
      ModuleTypes.Type typ = getModuleType(module);
      if(typ == null) return false;
      item.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, nbt -> nbt.apply(v -> {
        v.getIntArray(MODULES_KEY).ifPresent(v2 -> {
          int freeSlots = 0;
          int usedSlots = 0;
          for (int i = 0; i < v2.length; i++) {
            if(v2[i] == MODULE_EMPTY) freeSlots++;
          }
          if(freeSlots < typ.slotCost()) {
            return;
          } else {
            ret.set(true);
          }
          for (int i = 0; i < v2.length; i++) {
            if(v2[i] == MODULE_EMPTY) {
              if(usedSlots == typ.slotCost() - 1) {
                v2[i] = module;
                return;
              } else {
                v2[i] = moduleFromRawParts(typ.id(), 0);
                usedSlots++;
              }
            }
          }
        });
      }));
      updateLore(item);
      ModuleEffects.updateModuleEffects(item);
    }
    return ret.get();
  }
  
  public static void clearModules(ItemStack item) {
    if(isModularized(item)) {
      item.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, nbt -> nbt.apply(v -> {
        v.putIntArray(MODULES_KEY, new int[ToolMaterials.numSlots(item)]);
      }));
      updateLore(item);
      ModuleEffects.updateModuleEffects(item);
    }
  }
  
  public static ArrayList<ItemStack> getModuleItems(ItemStack item) {
    int[] modules = getModules(item);
    ArrayList<ItemStack> items = new ArrayList<>();
    Arrays.stream(modules).forEach(v -> {
      if(getModuleStrength(v) != 0) {
        items.add(createModuleItem(v));
      }
    });
    return items;
  }
  
  public static ModuleTypes.Type getModuleType(int module) {
    if(module == MODULE_EMPTY) return null;
    return ModuleTypes.MODULE_TYPES[module >> 16];
  }

  public static int getModuleStrength(int module) {
    return module & 0xffff;
  }
  
  public static int moduleFromRawParts(int moduleId, int strength) {
    return (moduleId << 16) | (strength & 0xffff);
  }

  public static int getLegalModuleStrength(int module) {
    ModuleTypes.Type typ = getModuleType(module);
    return Math.clamp(getModuleStrength(module), typ.levelMin(), typ.levelMax());
  }
  
  public static int[] getModules(ItemStack item) {
    if(!ToolManip.isModularized(item)) return new int[0];
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
    boolean[] gotCapped = new boolean[ModuleTypes.NUM_TYPES];
    return getModuleEffects(modules, gotCapped, capped);
  }

  public static int[] getModuleEffects(int[] modules, boolean[] gotCapped, boolean capped) {
    int[] total = new int[ModuleTypes.NUM_TYPES];
    int[] sharedCaps = ModuleTypes.CAPS.stream().mapToInt(i->i).toArray();
    int[] selfCaps = IntStreams.range(ModuleTypes.NUM_TYPES).map(v -> ModuleTypes.MODULE_TYPES[v].cap()).toArray();
    addModuleEffects(total, modules, sharedCaps, selfCaps, capped);
    for(int i = 0; i < ModuleTypes.NUM_TYPES; i++) {
      gotCapped[i] = selfCaps[i] == 0 || sharedCaps[ModuleTypes.MODULE_TYPES[i].capId()] == 0;
    }
    return total;
  }
  
  public static void addModuleEffects(int[] total, int[] modules, int[] sharedCaps, int[] selfCaps, boolean capped) {
    Arrays.stream(modules).forEach(module -> {
      ModuleTypes.Type typ = getModuleType(module);
      if(typ == null) return;
      int strength = getLegalModuleStrength(module);
      int cappedStrength;
      if(capped) {
        cappedStrength = Math.min(strength, Math.min(sharedCaps[typ.capId()], selfCaps[typ.id()]));
        sharedCaps[typ.capId()] -= cappedStrength;
        selfCaps[typ.id()] -= cappedStrength;
      } else {
        cappedStrength = strength;
      }
      total[typ.id()] += cappedStrength;
    });
  }
  
  public static ArrayList<Text> getLore(ItemStack item) {
    int[] modules = getModules(item);
    boolean[] gotCapped = new boolean[ModuleTypes.NUM_TYPES];
    int[] moduleEffects = getModuleEffects(modules, gotCapped, !ModuleEffects.UNCAPPED.contains(item.getItem()));
    
    ArrayList<Text> texts = new ArrayList<>();
    MutableText slots = Text.empty().styled(v -> v.withItalic(false));
    int populated = 0;
    for(int i = 0; i < modules.length; i++) {
      ModuleTypes.Type typ = getModuleType(modules[i]);
      if(typ == null) {
        slots.append(Text.literal("-").formatted(Formatting.DARK_GRAY));
      } else {
        populated++;
        int strength = getLegalModuleStrength(modules[i]);
        int boxIdx = typ.binary() ? 5 : Math.clamp(strength - typ.levelMin(), 0, 5);
        slots.append(Text.literal(FILLED_BOXES.substring(boxIdx, boxIdx + 1)).styled(typ.fmt()));
      }
    }
    slots.append(Text.literal(String.format(" %d/%d", populated, ToolMaterials.numSlots(item))).formatted(Formatting.DARK_GRAY));
    texts.add(slots);
    
    for(int i = 0; i < moduleEffects.length; i++) {
      ModuleTypes.Type typ = ModuleTypes.MODULE_TYPES[i];
      if(typ == null) continue;
      if(moduleEffects[i] == 0) continue;
      String capText = gotCapped[i] ? "(cap)" : "";
      String text;
      if(typ.binary()) {
        text = typ.name();
      } else {
        text = String.format("%d.%d %s %s", moduleEffects[i] / 10, moduleEffects[i] % 10, typ.name(), capText);
      }
      texts.add(Text.literal(text).styled(v -> v.withItalic(false)).styled(typ.fmt()));
    }
    
    return texts;
  }
  
  public static void updateLore(ItemStack item) {
    item.set(DataComponentTypes.LORE, new LoreComponent(getLore(item)));
  }
  
  public static ItemStack createModuleItem(int module) {
    return createModuleItem(Objects.requireNonNull(getModuleType(module)), getModuleStrength(module));
  }
  
  public static ItemStack createModuleItem(ModuleTypes.Type typ, int strength) {
    ItemStack stack = new ItemStack(Items.CLOCK, 1);
    stack.set(DataComponentTypes.ITEM_MODEL, Identifier.of("minecraft", "amethyst_shard"));
    stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    NbtCompound nbt = new NbtCompound();
    nbt.putInt(MODULE_EFFECT_KEY, moduleFromRawParts(typ.id(), strength));
    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    stack.set(DataComponentTypes.ITEM_NAME, Text.literal("Enchanted Shard").formatted(Formatting.LIGHT_PURPLE));
    stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
      typ.binary()
        ? Text.literal(typ.name()).styled(v -> v.withItalic(false)).styled(typ.fmt()) 
        : Text.literal("+%d.%d %s".formatted(strength / 10, strength % 10, typ.name()))
          .styled(v -> v.withItalic(false))
          .styled(typ.fmt()),
      typ.slotCost() > 1
        ? Text.literal("(%d slots)").styled(v -> v.withItalic(false).withColor(Colors.DARK_GRAY))
        : Text.literal("")
    )));
    return stack;
  }
  
  public static int getModuleFromItem(ItemStack moduleItem) {
    AtomicInteger ret = new AtomicInteger(0);
    moduleItem.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, nbt -> nbt.apply(v -> {
      ret.set(v.getInt(MODULE_EFFECT_KEY, 0));
    }));
    return ret.get();
  }
}
