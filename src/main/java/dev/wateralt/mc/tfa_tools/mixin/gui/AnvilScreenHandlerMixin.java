package dev.wateralt.mc.tfa_tools.mixin.gui;

import dev.wateralt.mc.tfa_tools.ModuleTypes;
import dev.wateralt.mc.tfa_tools.TfaTools;
import dev.wateralt.mc.tfa_tools.ToolManip;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(AnvilScreenHandler.class)
public class AnvilScreenHandlerMixin {
  @Shadow @Final private Property levelCost;

  @Shadow private int repairItemUsage;

  @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
  public void updateResult(CallbackInfo ci) {
    AnvilScreenHandler that = (AnvilScreenHandler) (Object) this;
    ForgingScreenHandlerAccessor thatAccess = (ForgingScreenHandlerAccessor) that; 
    ItemStack input1 = thatAccess.getInput().getStack(0);
    ItemStack input2 = thatAccess.getInput().getStack(1);
    repairItemUsage = 0;
    RegistryEntry<Enchantment> mending = TfaTools.dynamicRegistries.getEntryOrThrow(Enchantments.MENDING);
    RegistryEntry<Enchantment> soulSpeed = TfaTools.dynamicRegistries.getEntryOrThrow(Enchantments.SOUL_SPEED);
    Function<ItemStack, Boolean> checkSoulSpeed = (stack) -> {
      ItemEnchantmentsComponent comp = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
      if(comp != null) {
        return comp.getEnchantments().contains(soulSpeed);
      }
      return false;
    };
    
    if(ToolManip.isModularized(input1) && !input2.isEmpty()) {
      ci.cancel();
      int module = ToolManip.getModuleFromItem(input2);
      if(module != 0) {
        ItemStack output = input1.copy();
        boolean success = ToolManip.addModule(output, module);
        if(!success) {
          thatAccess.getOutput().setStack(0, ItemStack.EMPTY);
          levelCost.set(0);
        } else {
          thatAccess.getOutput().setStack(0, output);
          levelCost.set(1);
          repairItemUsage = 1;
        }
      }
    } else if(ToolManip.canBeModularized(input1) && EnchantmentHelper.getLevel(mending, input1) > 0 && input2.isOf(Items.ECHO_SHARD)) {
      ci.cancel();
      ItemStack output = input1.copy();
      boolean success = ToolManip.modularizeItem(output);
      if(!success) {
        thatAccess.getOutput().setStack(0, ItemStack.EMPTY);
        levelCost.set(0);
      } else {
        thatAccess.getOutput().setStack(0, output);
        levelCost.set(30);
      }
    } else if(input1.isOf(Items.AMETHYST_SHARD) && checkSoulSpeed.apply(input2)) {
      ci.cancel();
      ItemStack output = ToolManip.createModuleItem(ModuleTypes.SOUL_SPEED, 15);
      thatAccess.getOutput().setStack(0, output);
      levelCost.set(5);
    }
  }
}
