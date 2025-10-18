package dev.wateralt.mc.tfa_tools;

import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ModuleTypes {
  public record ModuleType(int id, String name, int levelMin, int levelMax, int capId, int toolClass, Formatting fmt) {
    boolean binary() {
      return levelMax == levelMin && levelMin == 1;
    }
  }
  public static final int NO_CAP = 0;
  public static final int TOOLS = 1;
  public static final int ARMOR = 2;
  public static final int MAX_ID = 3;
  
  public static final ModuleType EFFICIENCY = new ModuleType(1, "Efficiency", 10, 15, NO_CAP, TOOLS, Formatting.GREEN);
  public static final ModuleType SILK_TOUCH = new ModuleType(2, "Silk Touch", 10, 10, NO_CAP, TOOLS, Formatting.YELLOW);
  public static final ModuleType FORTUNE = new ModuleType(3, "Fortune", 10, 15, 1, TOOLS, Formatting.AQUA);

  public static final List<Integer> CAPS = List.of(
    Integer.MAX_VALUE, // No cap
    30, // Fortune
    50, // Sharpness
    20 // Knockback
  );
  
  public static final HashMap<Integer, ModuleType> MODULE_TYPES = new HashMap<>();
  static {
    Arrays.stream(new ModuleType[] {
      EFFICIENCY,
      SILK_TOUCH,
      FORTUNE
    }).forEach(v -> MODULE_TYPES.put(v.id(), v));
  }
}
