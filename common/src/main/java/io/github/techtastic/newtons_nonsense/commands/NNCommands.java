package io.github.techtastic.newtons_nonsense.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import io.github.techtastic.newtons_nonsense.physics.pipeline.Orchard;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import physx.physics.*;

import java.util.ArrayList;
import java.util.stream.Stream;

public class NNCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("sim")
                .then(Commands.literal("pause")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            Orchard.getTreeFromLevel(source.getLevel()).togglePause(false);
                            source.sendSystemMessage(Component.literal("Physics Simulation Paused!"));
                            return 1;
                        })
                ).then(Commands.literal("resume")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            Orchard.getTreeFromLevel(source.getLevel()).togglePause(true);
                            source.sendSystemMessage(Component.literal("Physics Simulation Resumed!"));
                            return 1;
                        })
                ).then(Commands.literal("step")
                        .then(Commands.argument("elapsedTime", FloatArgumentType.floatArg())
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    float time = FloatArgumentType.getFloat(context, "elapsedTime");
                                    Orchard.getTreeFromLevel(source.getLevel()).tryAndTick(time);
                                    source.sendSystemMessage(Component.literal("Physics Simulation Advanced by " + time));
                                    return 1;
                                })
                        )
                ).then(Commands.literal("materials")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            Stream<Holder.Reference<PxMaterial>> references = registry.lookupOrThrow(Orchard.MaterialRegistry.MATERIAL_REGISTRY_KEY).listElements();
                            source.sendSuccess(() -> Component.literal("List of Existing Materials:"), false);

                            references.forEach(ref -> source.sendSuccess(() -> Component.literal("\n" + ref.getRegisteredName() + ": {" +
                                    ref.value().getStaticFriction() + ", " + ref.value().getDynamicFriction() + ", " + ref.value().getRestitution() + "}"), false));

                            return 1;
                        })
                )
        );
    }
}
