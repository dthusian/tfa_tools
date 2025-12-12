package dev.wateralt.mc.tfa_tools;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import static net.minecraft.server.command.CommandManager.*;

public class ToolCommand {
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(
      literal("tool")
        .then(literal("modularize")
          .requires(requirePermissionLevel(MODERATORS_CHECK))
          .executes(wrap(ToolCommand::executeModularize)))
        .then(literal("add")
          .requires(requirePermissionLevel(MODERATORS_CHECK))
          .then(argument("module_id", IntegerArgumentType.integer())
            .then(argument("strength", IntegerArgumentType.integer())
              .executes(wrap(ToolCommand::executeAdd)))))
        .then(literal("giveModule")
          .requires(requirePermissionLevel(MODERATORS_CHECK))
          .then(argument("module_id", IntegerArgumentType.integer())
            .then(argument("strength", IntegerArgumentType.integer())
              .executes(wrap(ToolCommand::executeGiveModule)))))
        .then(literal("clear")
          .executes(wrap(ToolCommand::executeClear)))
    );
  }
  
  static int getModule(CommandContext<ServerCommandSource> ctx) {
    return ToolManip.moduleFromRawParts(ctx.getArgument("module_id", Integer.class), ctx.getArgument("strength", Integer.class));
  }
  
  public static Command<ServerCommandSource> wrap(Command<ServerCommandSource> func) {
    return ctx -> {
      try {
        return func.run(ctx);
      } catch(UserError err) {
        ctx.getSource().sendFeedback(() -> Text.of(err.getMessage()), false);
        return 0;
      } catch(Exception err) {
        err.printStackTrace();
        ctx.getSource().sendFeedback(() -> Text.of("An error occurred. This is a bug."), true);
        return 0;
      }
    };
  }
  
  public static int executeModularize(CommandContext<ServerCommandSource> ctx) {
    ServerPlayerEntity spe = ctx.getSource().getPlayer();
    if(spe == null) return 0;
    ItemStack stack = spe.getMainHandStack();
    boolean success = ToolManip.modularizeItem(stack);
    if(!success) throw new UserError("Failed to modularize");
    return 1;
  }
  
  public static int executeAdd(CommandContext<ServerCommandSource> ctx) {
    ServerPlayerEntity spe = ctx.getSource().getPlayer();
    if(spe == null) return 0;
    ItemStack stack = spe.getMainHandStack();
    int module = getModule(ctx);
    boolean success = ToolManip.addModule(stack, module);
    if(!success) throw new UserError("Failed to add module");
    return 1;
  }
  
  public static int executeClear(CommandContext<ServerCommandSource> ctx) {
    ServerPlayerEntity spe = ctx.getSource().getPlayer();
    if(spe == null) return 0;
    ItemStack stack = spe.getMainHandStack();
    ToolManip.clearModules(stack);
    return 1;
  }
  
  public static int executeGiveModule(CommandContext<ServerCommandSource> ctx) {
    Vec3d pos = ctx.getSource().getPosition();
    World world = ctx.getSource().getWorld();
    world.spawnEntity(new ItemEntity(
      world,
      pos.getX(),
      pos.getY(),
      pos.getZ(),
      ToolManip.createModuleItem(getModule(ctx))
    ));
    return 1;
  }
}
