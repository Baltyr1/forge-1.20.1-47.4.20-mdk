package net.baltyr.opmmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.baltyr.opmmod.classes.ModCapabilities;
import net.baltyr.opmmod.classes.OpmClass;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ClassCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("opmclass")
                .requires(src -> src.hasPermission(2)) // OP level 2

                // /opmclass set <joueur> <classe>
                .then(Commands.literal("set")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("class", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            for (OpmClass c : OpmClass.values())
                                                builder.suggest(c.name().toLowerCase());
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            String className = StringArgumentType.getString(ctx, "class");
                                            OpmClass opmClass = OpmClass.fromString(className);

                                            target.getCapability(ModCapabilities.PLAYER_CLASS).ifPresent(cap -> {
                                                cap.setPlayerClass(opmClass);
                                            });

                                            ctx.getSource()
                                                    .sendSuccess(() -> Component.literal("§aClasse de §e"
                                                            + target.getName().getString()
                                                            + "§a définie sur : " + opmClass.getFormattedName()), true);

                                            target.sendSystemMessage(Component.literal(
                                                    "§6Ta classe a été définie sur : " + opmClass.getFormattedName()));

                                            return 1;
                                        }))))

                // /opmclass get <joueur>
                .then(Commands.literal("get")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    target.getCapability(ModCapabilities.PLAYER_CLASS).ifPresent(cap -> {
                                        ctx.getSource().sendSuccess(
                                                () -> Component.literal("§eClasse de §f" + target.getName().getString()
                                                        + "§e : " + cap.getPlayerClass().getFormattedName()),
                                                false);
                                    });
                                    return 1;
                                })))

                // /opmclass list
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            StringBuilder sb = new StringBuilder("§6Classes disponibles :\n");
                            for (OpmClass c : OpmClass.values()) {
                                if (c != OpmClass.NONE)
                                    sb.append("  ").append(c.getFormattedName())
                                            .append(" §7(").append(c.name().toLowerCase()).append(")\n");
                            }
                            ctx.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
                            return 1;
                        })));
    }
}