package dev.wateralt.mc.tfa_tools;

import net.minecraft.util.Formatting;
import org.apache.http.cookie.SM;

import java.util.HashMap;
import java.util.List;

public class ModuleTypes {
  public record ModuleType(int id, String name, int levelMin, int levelMax, int extraSlots, int capId, int cap, int toolClass, Formatting fmt) {
    // builder pattern
    static ModuleType defaults(int id, String name, Formatting fmt, int toolClass) {
      return new ModuleType(id, name, 10, 10, 0, NO_CAP, Integer.MAX_VALUE, toolClass, fmt);
    }
    ModuleType withLevelRange(int levelMin, int levelMax) {
      return new ModuleType(id, name, levelMin, levelMax, extraSlots, capId, cap, toolClass, fmt);
    }
    
    ModuleType withSharedCap(int capId) {
      return new ModuleType(id, name, levelMin, levelMax, extraSlots, capId, cap, toolClass, fmt);
    }
    
    ModuleType withSelfCap(int cap) {
      return new ModuleType(id, name, levelMin, levelMax, extraSlots, capId, cap, toolClass, fmt);
    }
    
    ModuleType withExtraSlots(int extraSlots) {
      return new ModuleType(id, name, levelMin, levelMax, extraSlots, capId, cap, toolClass, fmt);
    }
    
    boolean binary() {
      return levelMax == levelMin && levelMin == 1;
    }
  }
  public static final int NO_CAP = 0;
  public static final int TOOLS = 1;
  public static final int ARMOR = 2;
  public static final int NUM_TYPES = 10;
  
  public static final ModuleType EFFICIENCY =
    ModuleType.defaults(0, "Efficiency", Formatting.GREEN, TOOLS)
      .withLevelRange(10, 15);
  public static final ModuleType SILK_TOUCH = 
    ModuleType.defaults(1, "Silk Touch", Formatting.YELLOW, TOOLS)
      .withExtraSlots(1);
  public static final ModuleType FORTUNE = 
    ModuleType.defaults(2, "Fortune", Formatting.AQUA, TOOLS)
      .withLevelRange(10, 15)
      .withSelfCap(30);
  public static final ModuleType DURABILITY =
    ModuleType.defaults(3, "Resilience", Formatting.GRAY, TOOLS);
  public static final ModuleType SHARPNESS =
    ModuleType.defaults(4, "Sharpness", Formatting.RED, TOOLS)
      .withSharedCap(1)
      .withLevelRange(10, 15);
  public static final ModuleType BANE_OF_ARTHROPODS =
    ModuleType.defaults(5, "Bane of Arthropods", Formatting.BLUE, TOOLS)
      .withSharedCap(1)
      .withLevelRange(10, 15);
  public static final ModuleType SMITE =
    ModuleType.defaults(6, "Smite", Formatting.LIGHT_PURPLE, TOOLS)
      .withSharedCap(1)
      .withLevelRange(10, 15);
  public static final ModuleType FLINTSLATE =
    ModuleType.defaults(7, "Flintslate", Formatting.GOLD, TOOLS)
      .withSharedCap(1);
  public static final ModuleType KNOCKBACK =
    ModuleType.defaults(8, "Knockback", Formatting.GOLD, TOOLS)
      .withSelfCap(20)
      .withLevelRange(10, 15);
  
  public static final ModuleType BLUNT_PROT =
    ModuleType.defaults(9, "Blunt Protection", Formatting.DARK_RED, ARMOR);

  public static final List<Integer> CAPS = List.of(
    Integer.MAX_VALUE, // No cap
    50 // Sharpness
  );
  
  public static final ModuleType[] MODULE_TYPES = new ModuleType[] {
    EFFICIENCY,
    SILK_TOUCH,
    FORTUNE,
    DURABILITY,
    SHARPNESS,
    BANE_OF_ARTHROPODS,
    SMITE,
    FLINTSLATE,
    KNOCKBACK,
    BLUNT_PROT
  };
  static {
    for(int i = 0; i < MODULE_TYPES.length; i++) {
      if(MODULE_TYPES[i].id != i) throw new AssertionError();
    }
  }
  
  public static final double BIAS_TOWARDS_RATE = 0.3;
  
  public record GenerationInfo(List<ModuleType> biasTowards, double rate, double meanStrength) { }
  
  public static final HashMap<String, GenerationInfo> MODULE_GENERATION = new HashMap<>();
  static {
    // Tier 1 structures
    MODULE_GENERATION.put("minecraft:chests/abandoned_minecraft", new GenerationInfo(List.of(EFFICIENCY), 1.0, 0.25));
    MODULE_GENERATION.put("minecraft:chests/simple_dungeon", new GenerationInfo(List.of(FORTUNE, SILK_TOUCH), 1.0, 0.25));
    
    // Tier 2 structures
    //MODULE_GENERATION.put("minecraft:chests/nether_bridge", new GenerationInfo(List.of(FLINTSLATE), 1.0, 0.4));
    //MODULE_GENERATION.put("minecraft:chests/nether_bridge", new GenerationInfo(List.of(FLINTSLATE), 1.0, 0.4));

    // Tier 3 structures
    //MODULE_GENERATION.put("minecraft:chests/ancient_city", new GenerationInfo(List.of(SWIFT_SNEAK), 1.0, 0.5));
    //MODULE_GENERATION.put("minecraft:chests/end_city_treasure", new GenerationInfo(List.of(), 0.8, 0.5));
  }
}
