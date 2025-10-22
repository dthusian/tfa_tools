package dev.wateralt.mc.tfa_tools;

import net.minecraft.text.Style;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.UnaryOperator;

public class ModuleTypes {
  public record Type(int id, String name, int levelMin, int levelMax, int slotCost, int capId, int cap, int toolClass, UnaryOperator<Style> fmt) {
    // builder pattern
    static Type defaults(int id, String name, Formatting fmt, int toolClass) {
      return new Type(id, name, 10, 10, 1, NO_CAP, Integer.MAX_VALUE, toolClass, v -> v.withFormatting(fmt));
    }
    static Type defaults(int id, String name, UnaryOperator<Style> fmt, int toolClass) {
      return new Type(id, name, 10, 10, 1, NO_CAP, Integer.MAX_VALUE, toolClass, fmt);
    }
    Type withLevelRange(int levelMin, int levelMax) {
      return new Type(id, name, levelMin, levelMax, slotCost, capId, cap, toolClass, fmt);
    }
    
    Type withSharedCap(int capId) {
      return new Type(id, name, levelMin, levelMax, slotCost, capId, cap, toolClass, fmt);
    }
    
    Type withSelfCap(int cap) {
      return new Type(id, name, levelMin, levelMax, slotCost, capId, cap, toolClass, fmt);
    }
    
    Type withExtraSlots(int extraSlots) {
      return new Type(id, name, levelMin, levelMax, extraSlots, capId, cap, toolClass, fmt);
    }
    
    boolean binary() {
      return levelMax == levelMin && levelMin == 10;
    }
  }
  public static final int NO_CAP = 0;
  public static final int TOOLS = 1;
  public static final int ARMOR = 2;
  
  public static final Type EFFICIENCY =
    Type.defaults(0, "Efficiency", Formatting.GREEN, TOOLS)
      .withLevelRange(10, 15);
  public static final Type SILK_TOUCH = 
    Type.defaults(1, "Silk Touch", Formatting.YELLOW, TOOLS)
      .withExtraSlots(2);
  public static final Type FORTUNE = 
    Type.defaults(2, "Fortune", Formatting.AQUA, TOOLS)
      .withLevelRange(10, 15)
      .withSelfCap(30);
  public static final Type DURABILITY =
    Type.defaults(3, "Resilience", Formatting.GRAY, TOOLS);
  public static final Type SHARPNESS =
    Type.defaults(4, "Sharpness", Formatting.RED, TOOLS)
      .withSharedCap(1)
      .withLevelRange(10, 15);
  public static final Type FLINTSLATE =
    Type.defaults(5, "Flintslate", v -> v.withColor(0xff7700), TOOLS)
      .withSharedCap(1);
  public static final Type KNOCKBACK =
    Type.defaults(6, "Knockback", Formatting.LIGHT_PURPLE, TOOLS)
      .withSelfCap(20)
      .withLevelRange(10, 15);
  
  public static final Type BLUNT_PROT =
    Type.defaults(7, "Blunt Protection", Formatting.DARK_RED, ARMOR)
      .withSelfCap(90)
      .withLevelRange(10, 15);
  public static final Type BLADE_PROT =
    Type.defaults(8, "Blade Protection", Formatting.RED, ARMOR)
      .withSelfCap(90)
      .withLevelRange(10, 15);
  public static final Type FIRE_PROT =
    Type.defaults(9, "Fire Protection", v -> v.withColor(0xff7700), ARMOR)
      .withSelfCap(90)
      .withLevelRange(10, 15);
  public static final Type BLAST_PROT =
    Type.defaults(10, "Blast Protection", v -> v.withColor(Colors.LIGHT_YELLOW), ARMOR)
      .withSelfCap(90)
      .withLevelRange(10, 15);
  public static final Type PROJ_PROT =
    Type.defaults(11, "Projectile Protection", Formatting.GRAY, ARMOR)
      .withSelfCap(90)
      .withLevelRange(10, 15);
  public static final Type MAGIC_PROT =
    Type.defaults(12, "Magic Protection", Formatting.LIGHT_PURPLE, ARMOR)
      .withSelfCap(90)
      .withLevelRange(10, 15);
  public static final Type FEATHER_FALLING =
    Type.defaults(13, "Feather Falling", v -> v.withColor(0xa2c6fc), ARMOR)
      .withSelfCap(60)
      .withLevelRange(10, 15);
  public static final Type AQUA_AFFINITY =
    Type.defaults(14, "Aqua Affinity", Formatting.BLUE, ARMOR);
  public static final Type SOUL_SPEED =
    Type.defaults(15, "Soul Speed", Formatting.DARK_PURPLE, ARMOR);
  public static final Type SWIFT_SNEAK =
    Type.defaults(16, "Swift Sneak", Formatting.DARK_BLUE, ARMOR);
  public static final Type FROST_WALKER =
    Type.defaults(17, "Frost Walker", Formatting.AQUA, ARMOR);

  public static final List<Integer> CAPS = List.of(
    Integer.MAX_VALUE, // No cap
    50 // Sharpness
  );
  
  public static final Type[] MODULE_TYPES = new Type[] {
    EFFICIENCY,
    SILK_TOUCH,
    FORTUNE,
    DURABILITY,
    SHARPNESS,
    FLINTSLATE,
    KNOCKBACK,
    BLUNT_PROT,
    BLADE_PROT,
    FIRE_PROT,
    BLAST_PROT,
    PROJ_PROT,
    MAGIC_PROT,
    FEATHER_FALLING,
    AQUA_AFFINITY,
    SOUL_SPEED,
    SWIFT_SNEAK,
    FROST_WALKER
  };
  public static final int NUM_TYPES = MODULE_TYPES.length;
  static {
    for(int i = 0; i < MODULE_TYPES.length; i++) {
      if(MODULE_TYPES[i].id != i) throw new AssertionError();
    }
  }
  
  public static final double BIAS_TOWARDS_RATE = 0.3;
  
  public record GenerationInfo(List<Type> biasTowards, List<Type> generatable, double rate, double meanStrength) { }
  
  public static final List<Type> OVERWORLD_DEFAULT = List.of(
    EFFICIENCY,
    FORTUNE,
    SILK_TOUCH,
    DURABILITY,
    SHARPNESS,
    KNOCKBACK,

    BLUNT_PROT,
    PROJ_PROT,
    MAGIC_PROT,
    FEATHER_FALLING,
    
    AQUA_AFFINITY,
    FROST_WALKER
  );
  
  public static final HashMap<String, GenerationInfo> MODULE_GENERATION = new HashMap<>();
  static {
    List<Type> allTypes = Arrays.stream(MODULE_TYPES).toList();
    
    // Tier 1A structures
    MODULE_GENERATION.put("minecraft:chests/abandoned_minecraft", new GenerationInfo(List.of(EFFICIENCY), OVERWORLD_DEFAULT, 0.8, 0.25));
    MODULE_GENERATION.put("minecraft:chests/simple_dungeon", new GenerationInfo(List.of(FORTUNE, SILK_TOUCH), OVERWORLD_DEFAULT, 1.0, 0.25));
    MODULE_GENERATION.put("minecraft:chests/stronghold_library", new GenerationInfo(List.of(MAGIC_PROT), OVERWORLD_DEFAULT, 1.0, 0.25));
    MODULE_GENERATION.put("minecraft:chests/trial_chambers/reward", new GenerationInfo(List.of(PROJ_PROT), OVERWORLD_DEFAULT, 0.5, 0.25));
    MODULE_GENERATION.put("minecraft:chests/shipwreck_treasure", new GenerationInfo(List.of(AQUA_AFFINITY), OVERWORLD_DEFAULT, 0.8, 0.25));
    MODULE_GENERATION.put("minecraft:chests/underwater_ruin_big", new GenerationInfo(List.of(AQUA_AFFINITY), OVERWORLD_DEFAULT, 0.3, 0.25));
    MODULE_GENERATION.put("minecraft:chests/underwater_ruin_small", new GenerationInfo(List.of(AQUA_AFFINITY), OVERWORLD_DEFAULT, 0.3, 0.25));

    // Tier 1B structures
    MODULE_GENERATION.put("minecraft:chests/desert_pyramid", new GenerationInfo(List.of(FEATHER_FALLING), OVERWORLD_DEFAULT, 1.0, 0.3));
    MODULE_GENERATION.put("minecraft:chests/jungle_temple", new GenerationInfo(List.of(KNOCKBACK), OVERWORLD_DEFAULT, 1.0, 0.3));
    MODULE_GENERATION.put("minecraft:chests/woodland_mansion", new GenerationInfo(List.of(SHARPNESS), OVERWORLD_DEFAULT, 0.5, 0.3));
    MODULE_GENERATION.put("minecraft:chests/ruined_portal", new GenerationInfo(List.of(), allTypes, 0.05, 0.3));
    
    // Tier 2 structures
    MODULE_GENERATION.put("minecraft:chests/nether_bridge", new GenerationInfo(List.of(FIRE_PROT), allTypes, 0.7, 0.4));
    MODULE_GENERATION.put("minecraft:chests/bastion_bridge", new GenerationInfo(List.of(FLINTSLATE), allTypes, 0.5, 0.4));
    MODULE_GENERATION.put("minecraft:chests/bastion_treasure", new GenerationInfo(List.of(FLINTSLATE), allTypes, 1.0, 0.4));
    MODULE_GENERATION.put("minecraft:chests/bastion_other", new GenerationInfo(List.of(FIRE_PROT), allTypes, 0.5, 0.4));
    MODULE_GENERATION.put("minecraft:chests/trial_chambers/reward_ominous", new GenerationInfo(List.of(SHARPNESS), OVERWORLD_DEFAULT, 0.5, 0.4));
    MODULE_GENERATION.put("minecraft:chests/ancient_city", new GenerationInfo(List.of(SWIFT_SNEAK), OVERWORLD_DEFAULT, 0.7, 0.4));
    
    // Tier 3 structures
    MODULE_GENERATION.put("minecraft:chests/end_city_treasure", new GenerationInfo(List.of(), allTypes, 0.8, 0.5));
    
    // Special cases
    MODULE_GENERATION.put("minecraft:chests/ancient_city_ice_box", new GenerationInfo(List.of(FROST_WALKER), List.of(FROST_WALKER), 1.0, 0.25));
  }
}
