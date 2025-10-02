package io.github.techtastic.newtons_nonsense;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.github.techtastic.newtons_nonsense.physics.Backend;
import io.github.techtastic.newtons_nonsense.physics.ServerPhysicsWorld;
import io.github.techtastic.newtons_nonsense.physics.object.box.BoxPhysicsObject;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class NNCommands {
    private static LiteralArgumentBuilder<CommandSourceStack> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    private static <T> RequiredArgumentBuilder<CommandSourceStack, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    private static void sendSystemMessage(CommandSourceStack source, Component message, boolean bool) {
        if (source.getPlayer() instanceof ServerPlayer player)
            player.sendSystemMessage(message, bool);
    }

    private static void spawnBox(ServerPhysicsWorld world, Vec3 pos, Vec3 lengths) {
        BoxPhysicsObject boxObject = new BoxPhysicsObject(lengths,
                Blocks.COBBLESTONE.defaultBlockState());
        world.addPhysicsObject(boxObject);
        boxObject.setPosition(pos);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection selection) {
        dispatcher.register(literal("newton")
                .requires(source -> source.hasPermission(2))
                .then(literal("pause")
                        .executes(context -> {
                            if (Backend.getOrCreatePhysicsWorld(context.getSource().getLevel()) instanceof ServerPhysicsWorld world) {
                                world.pause(true);
                                sendSystemMessage(context.getSource(), Component.literal("Simulation Paused!").withStyle(ChatFormatting.RED), true);
                                return 1;
                            }
                            return 0;
                        })
                ).then(literal("unpause")
                        .executes(context -> {
                            if (Backend.getOrCreatePhysicsWorld(context.getSource().getLevel()) instanceof ServerPhysicsWorld world) {
                                world.pause(false);
                                sendSystemMessage(context.getSource(), Component.literal("Simulation Unpaused!").withStyle(ChatFormatting.GREEN), true);
                                return 1;
                            }
                            return 0;
                        })
                ).then(literal("tick")
                        .then(argument("delta", FloatArgumentType.floatArg(0))
                                .executes(context -> {
                                    Float delta = context.getArgument("delta", Float.class);
                                    String message = String.format("Simulation Advanced by %s!", delta);
                                    if (Backend.getOrCreatePhysicsWorld(context.getSource().getLevel()) instanceof ServerPhysicsWorld world) {
                                        world.tick(delta);
                                        sendSystemMessage(context.getSource(), Component.literal(message).withStyle(ChatFormatting.GOLD), true);
                                        return 1;
                                    }
                                    return 0;
                                })
                        )
                ).then(literal("box")
                        .then(argument("lengths", Vec3Argument.vec3())
                                .executes(context -> {
                                    Vec3 lengths = context.getArgument("lengths", WorldCoordinates.class).getPosition(context.getSource());
                                    if (Backend.getOrCreatePhysicsWorld(context.getSource().getLevel()) instanceof ServerPhysicsWorld world) {
                                        spawnBox(world, context.getSource().getPosition(), lengths);
                                        return 1;
                                    }
                                    return 0;
                                })
                                .then(argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> {
                                            Vec3 lengths = context.getArgument("lengths", WorldCoordinates.class).getPosition(context.getSource());
                                            Vec3 pos = context.getArgument("pos", WorldCoordinates.class).getPosition(context.getSource());
                                            if (Backend.getOrCreatePhysicsWorld(context.getSource().getLevel()) instanceof ServerPhysicsWorld world) {
                                                spawnBox(world, pos, lengths);
                                                return 1;
                                            }
                                            return 0;
                                        })
                                )
                        )
                )
        );
    }
}
