package dev.wateralt.mc.tfa_tools.mixin.gui;

import dev.wateralt.mc.tfa_tools.ToolManip;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public class AnvilScreenHandlerMixin {
  @Shadow @Final private Property levelCost;

  @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
  public void updateResult(CallbackInfo ci) {
    AnvilScreenHandler that = (AnvilScreenHandler) (Object) this;
    ForgingScreenHandlerAccessor thatAccess = (ForgingScreenHandlerAccessor) that; 
    ItemStack input1 = thatAccess.getInput().getStack(0);
    ItemStack input2 = thatAccess.getInput().getStack(1);
    
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
        }
      }
    } else if(ToolManip.canBeModularized(input1) && input2.isOf(Items.ECHO_SHARD)) {
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
    }
  }
}
