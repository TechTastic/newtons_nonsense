package io.github.techtastic.newtons_nonsense.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import io.github.techtastic.newtons_nonsense.physics.Stage;
import io.github.techtastic.newtons_nonsense.physics.pipeline.Backstage;
import io.github.techtastic.newtons_nonsense.registry.physics.materials.PhysicsMaterialRegistry;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import physx.physics.*;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class NNCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("add_actor")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("pos", Vec3Argument.vec3())
                        .executes(context -> {
                            Vec3 pos = Vec3Argument.getVec3(context, "pos");
                            PxRigidDynamic box = Backstage.createDynamicBox((float) pos.x, (float) pos.y, (float) pos.z, registry.lookupOrThrow(PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY).getOrThrow(PhysicsMaterialRegistry.DEFAULT_MATERIAL).value());
                            Stage.getOrCreateStage(context.getSource().getLevel()).addActor(box);

                            context.getSource().sendSuccess(() -> Component.literal("Added new dynamic actor at " + pos), false);

                            return 1;
                        })
                )
        );

        dispatcher.register(Commands.literal("remove_all_actors")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    Stage stage = Stage.getOrCreateStage(context.getSource().getLevel());
                    ArrayList<PxActor> actors = (ArrayList<PxActor>) stage.actors.clone();
                    actors.forEach(stage::removeAndFreeActor);
                    actors.clear();

                    context.getSource().sendSuccess(() -> Component.literal("Removed all dynamic actors from the current level"), false);

                    return 1;
                })
        );

        dispatcher.register(Commands.literal("list_materials")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    Stream<Holder.Reference<PxMaterial>> references = registry.lookupOrThrow(PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY).listElements();
                    source.sendSuccess(() -> Component.literal("List of Existing Materials:"), false);

                    references.forEach(ref ->
                            source.sendSuccess(() -> Component.literal(
                                    ref.getRegisteredName() + ": {" +
                                            ref.value().getStaticFriction() + ", " +
                                            ref.value().getDynamicFriction() + ", " +
                                            ref.value().getRestitution() + "}"
                            ), false)
                    );

                    return 1;
                })
        );

        dispatcher.register(Commands.literal("add_ground_actor")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("firstCorner", Vec3Argument.vec3())
                        .then(Commands.argument("secondCorner", Vec3Argument.vec3())
                        .executes(context -> {
                            Vec3 firstCorner = Vec3Argument.getVec3(context, "firstCorner");
                            Vec3 secondCorner = Vec3Argument.getVec3(context, "secondCorner");
                            AABB aabb = new AABB(firstCorner, secondCorner);
                            PxShape groundShape = Backstage.createBoxShape(
                                    (float) aabb.getXsize() / 2,
                                    (float) aabb.getYsize() / 2,
                                    (float) aabb.getZsize() / 2,
                                    0, 0, 0,
                                    registry.lookupOrThrow(PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY).getOrThrow(PhysicsMaterialRegistry.DEFAULT_MATERIAL).value()
                            );
                            PxRigidStatic ground = Backstage.createStaticBodyWithShapes((float) aabb.getCenter().x, (float) aabb.getCenter().y, (float) aabb.getCenter().z, groundShape);
                            Stage.getOrCreateStage(context.getSource().getLevel()).addActor(ground);

                            context.getSource().sendSuccess(() -> Component.literal("Added new ground actor from " + firstCorner + " to " + secondCorner), false);

                            return 1;
                        })
                ))
        );

        dispatcher.register(Commands.literal("pause_simulation")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    Stage.getOrCreateStage(context.getSource().getLevel()).canSimulate(false);
                    context.getSource().sendSystemMessage(Component.literal("Physics Simulation Paused!"));
                    return 1;
                })
        );

        dispatcher.register(Commands.literal("resume_simulation")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    Stage.getOrCreateStage(context.getSource().getLevel()).canSimulate(true);
                    context.getSource().sendSystemMessage(Component.literal("Physics Simulation Resumed!"));
                    return 1;
                })
        );

        dispatcher.register(Commands.literal("step_simulation")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("step", FloatArgumentType.floatArg(0, 100))
                        .executes(context -> {
                            float step = FloatArgumentType.getFloat(context, "step");
                            Stage.getOrCreateStage(context.getSource().getLevel()).step(context.getSource().getLevel(), step);
                            context.getSource().sendSystemMessage(Component.literal("Physics Simulation Advanced by " + step));
                            return 1;
                        })
                )
        );
    }
}
