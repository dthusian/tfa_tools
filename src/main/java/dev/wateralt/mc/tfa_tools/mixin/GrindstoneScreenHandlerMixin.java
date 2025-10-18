package dev.wateralt.mc.tfa_tools.mixin;

import dev.wateralt.mc.tfa_tools.ToolManip;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrindstoneScreenHandler.class)
public class GrindstoneScreenHandlerMixin {
  @Inject(method = "getOutputStack", at = @At("HEAD"), cancellable = true)
  public void getOutputStack(ItemStack firstInput, ItemStack secondInput, CallbackInfoReturnable<ItemStack> cir) {
    if(ToolManip.isModularized(firstInput) || ToolManip.isModularized(secondInput)) {
      if(secondInput.isEmpty()) {
        ItemStack copied = firstInput.copy();
        ToolManip.clearModules(copied);
        cir.setReturnValue(copied);
      } else {
        cir.setReturnValue(ItemStack.EMPTY);
      }
    }
  }
}