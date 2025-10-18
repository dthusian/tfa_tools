package dev.wateralt.mc.tfa_tools.mixin;

import dev.wateralt.mc.tfa_tools.ToolManip;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.screen.GrindstoneScreenHandler$4")
public class GrindstoneScreenHandlerAnonymousMixin {
  @Shadow @Final private GrindstoneScreenHandler field_16780;

  @Inject(method = "onTakeItem", at = @At("HEAD"))
  public void onTakeItem(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
    for(int i = 0; i < 2; i++) {
      ItemStack stack2 = this.field_16780.slots.get(i).getStack();
      if(ToolManip.isModularized(stack2)) {
        ToolManip.getModuleItems(stack2).forEach(v -> {
          ItemEntity itemEntity = new ItemEntity(player.getEntityWorld(), player.getX(), player.getY(), player.getZ(), v);
          player.getEntityWorld().spawnEntity(itemEntity);
        });
      }
    }
  }

  @Inject(method = "getExperience(Lnet/minecraft/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
  public void getExperience(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
    if(ToolManip.isModularized(stack)) {
      cir.setReturnValue(0);
    }
  }
}
