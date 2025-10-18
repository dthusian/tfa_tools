package dev.wateralt.mc.tfa_tools;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.*;

public class ToolCommand {
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(
      literal("tool")
        .then(literal("add")
          .requires(v -> v.hasPermissionLevel(2))
          .then(argument("module_id", IntegerArgumentType.integer())
            .then(argument("strength", IntegerArgumentType.integer())
              .executes(wrap(ToolCommand::executeAdd)))))
    );
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
  
  public static int executeAdd(CommandContext<ServerCommandSource> ctx) {
    ServerPlayerEntity spe = ctx.getSource().getPlayer();
    if(spe == null) return 0;
    ItemStack stack = spe.getMainHandStack();
    int module = (ctx.getArgument("module_id", Integer.class) << 16) | ctx.getArgument("strength", Integer.class);
    boolean success = ToolManip.addModule(stack, module);
    if(!success) {
      throw new UserError("Failed to add module");
    }
    return 0;
  }
}
