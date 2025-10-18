package dev.wateralt.mc.tfa_tools.mixin;

import com.mojang.brigadier.CommandDispatcher;
import dev.wateralt.mc.tfa_tools.ToolCommand;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
  @Shadow @Final private CommandDispatcher<ServerCommandSource> dispatcher;

  @Inject(method = "<init>", at = @At("TAIL"))
  public void init(CommandManager.RegistrationEnvironment environment, CommandRegistryAccess commandRegistryAccess, CallbackInfo ci) {
    ToolCommand.register(dispatcher);
  }
}
