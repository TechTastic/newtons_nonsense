package io.github.techtastic.newtons_nonsense.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import io.github.techtastic.newtons_nonsense.physics.Stage;
import io.github.techtastic.newtons_nonsense.physics.pipeline.Backstage;
import io.github.techtastic.newtons_nonsense.registry.physics.materials.PhysicsMaterialRegistry;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.system.MemoryStack;
import physx.common.PxVec3;
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
                            Stage.getOrCreateStage(source.getLevel()).canSimulate(false);
                            source.sendSystemMessage(Component.literal("Physics Simulation Paused!"));
                            return 1;
                        })
                ).then(Commands.literal("resume")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            Stage.getOrCreateStage(source.getLevel()).canSimulate(true);
                            source.sendSystemMessage(Component.literal("Physics Simulation Resumed!"));
                            return 1;
                        })
                ).then(Commands.literal("step")
                        .then(Commands.argument("elapsedTime", FloatArgumentType.floatArg())
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    float time = FloatArgumentType.getFloat(context, "elapsedTime");
                                    Stage.getOrCreateStage(source.getLevel()).step(source.getLevel(), time);
                                    source.sendSystemMessage(Component.literal("Physics Simulation Advanced by " + time));
                                    return 1;
                                })
                        )
                ).then(Commands.literal("materials")
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
                ).then(Commands.literal("actor")
                        .then(Commands.literal("add")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("static")
                                        .executes(context -> {
                                            //  /sim actor add static
                                            CommandSourceStack source = context.getSource();
                                            Vec3 pos = source.getPosition();
                                            Holder.Reference<PxMaterial> material = registry.lookupOrThrow(PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY)
                                                    .getOrThrow(PhysicsMaterialRegistry.DEFAULT_MATERIAL);

                                            PxShape shape = Backstage.createBoxShape(.5f, .5f, .5f, 0, 0, 0, material.value());
                                            PxRigidStatic box = Backstage.createStaticBodyWithShapes((float) pos.x, (float) pos.y, (float) pos.z, shape);
                                            Stage.getOrCreateStage(source.getLevel()).addActor(box);

                                            source.sendSuccess(() -> Component.literal("Added new static actor at " + pos), false);

                                            return 1;
                                        })
                                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                                .executes(context -> {
                                                    //  /sim actor add static <pos>
                                                    CommandSourceStack source = context.getSource();
                                                    Vec3 pos = Vec3Argument.getVec3(context, "pos");
                                                    Holder.Reference<PxMaterial> material = registry.lookupOrThrow(PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY)
                                                            .getOrThrow(PhysicsMaterialRegistry.DEFAULT_MATERIAL);

                                                    PxShape shape = Backstage.createBoxShape(.5f, .5f, .5f, 0, 0, 0, material.value());
                                                    PxRigidStatic box = Backstage.createStaticBodyWithShapes((float) pos.x, (float) pos.y, (float) pos.z, shape);
                                                    Stage.getOrCreateStage(source.getLevel()).addActor(box);

                                                    source.sendSuccess(() -> Component.literal("Added new static actor at " + pos), false);

                                                    return 1;
                                                })
                                                .then(Commands.argument("rot", Vec3Argument.vec3())
                                                        .executes(context -> {
                                                            //  /sim actor add static <pos> <rot>
                                                            try (MemoryStack mem = MemoryStack.stackPush()) {
                                                                CommandSourceStack source = context.getSource();
                                                                Vec3 pos = Vec3Argument.getVec3(context, "pos");
                                                                Vec3 rot = Vec3Argument.getVec3(context, "rot");
                                                                Holder.Reference<PxMaterial> material = registry.lookupOrThrow(PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY)
                                                                        .getOrThrow(PhysicsMaterialRegistry.DEFAULT_MATERIAL);

                                                                PxShape shape = Backstage.createBoxShape(.5f, .5f, .5f, 0, 0, 0, material.value());
                                                                shape.getLocalPose().getQ().rotate(PxVec3.createAt(mem, MemoryStack::nmalloc, (float) rot.x, (float) rot.y, (float) rot.z));
                                                                PxRigidStatic box = Backstage.createStaticBodyWithShapes((float) pos.x, (float) pos.y, (float) pos.z, shape);
                                                                Stage.getOrCreateStage(source.getLevel()).addActor(box);

                                                                source.sendSuccess(() -> Component.literal("Added new static actor at " + pos + " with " + rot + " rotations"), false);

                                                                return 1;
                                                            }
                                                        })
                                                        .then(Commands.argument("material", ResourceLocationArgument.id())
                                                                .executes(context -> {
                                                                    //  /sim actor add static <pos> <rot> <size> <material>
                                                                    try (MemoryStack mem = MemoryStack.stackPush()) {
                                                                        CommandSourceStack source = context.getSource();
                                                                        Vec3 pos = Vec3Argument.getVec3(context, "pos");
                                                                        Vec3 rot = Vec3Argument.getVec3(context, "rot");
                                                                        Holder.Reference<PxMaterial> material = registry.lookupOrThrow(PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY)
                                                                                .getOrThrow(ResourceKey.create(PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY,
                                                                                        ResourceLocationArgument.getId(context, "material")));

                                                                        PxShape shape = Backstage.createBoxShape(.5f, .5f, .5f, 0, 0, 0, material.value());
                                                                        shape.getLocalPose().getQ().rotate(PxVec3.createAt(mem, MemoryStack::nmalloc, (float) rot.x, (float) rot.y, (float) rot.z));
                                                                        PxRigidStatic box = Backstage.createStaticBodyWithShapes((float) pos.x, (float) pos.y, (float) pos.z, shape);
                                                                        Stage.getOrCreateStage(source.getLevel()).addActor(box);

                                                                        source.sendSuccess(() -> Component.literal("Added new static actor at " + pos + " with " + rot + " rotations and " + material.getRegisteredName() + "material"), false);

                                                                        return 1;
                                                                    }
                                                                })
                                                                .then(Commands.argument("size", Vec3Argument.vec3())
                                                                        .executes(context -> {
                                                                            //  /sim actor add static <pos> <rot> <size> <material> <size>
                                                                            try (MemoryStack mem = MemoryStack.stackPush()) {
                                                                                CommandSourceStack source = context.getSource();
                                                                                Vec3 pos = Vec3Argument.getVec3(context, "pos");
                                                                                Vec3 rot = Vec3Argument.getVec3(context, "rot");
                                                                                Holder.Reference<PxMaterial> material = registry.lookupOrThrow(PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY)
                                                                                        .getOrThrow(ResourceKey.create(PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY,
                                                                                                ResourceLocationArgument.getId(context, "material")));
                                                                                Vec3 size = Vec3Argument.getVec3(context, "size");

                                                                                PxShape shape = Backstage.createBoxShape((float) size.x, (float) size.y, (float) size.z, 0, 0, 0, material.value());
                                                                                shape.getLocalPose().getQ().rotate(PxVec3.createAt(mem, MemoryStack::nmalloc, (float) rot.x, (float) rot.y, (float) rot.z));
                                                                                PxRigidStatic box = Backstage.createStaticBodyWithShapes((float) pos.x, (float) pos.y, (float) pos.z, shape);
                                                                                Stage.getOrCreateStage(source.getLevel()).addActor(box);

                                                                                source.sendSuccess(() -> Component.literal("Added new static actor at " + pos + " with " + rot + " rotations, " + material.getRegisteredName() + " material and " + size + "half-lengths"), false);

                                                                                return 1;
                                                                            }
                                                                        })
                                                                )
                                                        )
                                                )
                                        )
                                ).then(Commands.literal("dynamic")
                                        .executes(context -> {
                                            //  /sim actor add dynamic
                                            CommandSourceStack source = context.getSource();
                                            Vec3 pos = source.getPosition();
                                            Holder.Reference<PxMaterial> material = registry.lookupOrThrow(PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY)
                                                    .getOrThrow(PhysicsMaterialRegistry.DEFAULT_MATERIAL);

                                            PxRigidDynamic box = Backstage.createDynamicBox((float) pos.x, (float) pos.y, (float) pos.z, material.value());
                                            Stage.getOrCreateStage(source.getLevel()).addActor(box);

                                            source.sendSuccess(() -> Component.literal("Added new dynamic actor at " + pos), false);

                                            return 1;
                                        })
                                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                                .executes(context -> {
                                                    //  /sim actor add dynamic <pos>
                                                    CommandSourceStack source = context.getSource();
                                                    Vec3 pos = Vec3Argument.getVec3(context, "pos");
                                                    Holder.Reference<PxMaterial> material = registry.lookupOrThrow(PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY)
                                                            .getOrThrow(PhysicsMaterialRegistry.DEFAULT_MATERIAL);

                                                    PxRigidDynamic box = Backstage.createDynamicBox((float) pos.x, (float) pos.y, (float) pos.z, material.value());
                                                    Stage.getOrCreateStage(source.getLevel()).addActor(box);

                                                    source.sendSuccess(() -> Component.literal("Added new dynamic actor at " + pos), false);

                                                    return 1;
                                                })
                                                .then(Commands.argument("rot", Vec3Argument.vec3())
                                                        .executes(context -> {
                                                            //  /sim actor add dynamic <pos> <rot>
                                                            try (MemoryStack mem = MemoryStack.stackPush()) {
                                                                CommandSourceStack source = context.getSource();
                                                                Vec3 pos = Vec3Argument.getVec3(context, "pos");
                                                                Vec3 rot = Vec3Argument.getVec3(context, "rot");
                                                                Holder.Reference<PxMaterial> material = registry.lookupOrThrow(PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY)
                                                                        .getOrThrow(PhysicsMaterialRegistry.DEFAULT_MATERIAL);

                                                                PxRigidDynamic box = Backstage.createDynamicBox((float) pos.x, (float) pos.y, (float) pos.z, material.value());
                                                                box.getGlobalPose().getQ().rotate(PxVec3.createAt(mem, MemoryStack::nmalloc, (float) rot.x, (float) rot.y, (float) rot.z));
                                                                Stage.getOrCreateStage(source.getLevel()).addActor(box);

                                                                source.sendSuccess(() -> Component.literal("Added new dynamic actor at " + pos + " with " + rot + " rotations"), false);

                                                                return 1;
                                                            }
                                                        })
                                                        .then(Commands.argument("material", ResourceLocationArgument.id())
                                                                .executes(context -> {
                                                                    //  /sim actor add dynamic <pos> <rot> <size> <material>
                                                                    try (MemoryStack mem = MemoryStack.stackPush()) {
                                                                        CommandSourceStack source = context.getSource();
                                                                        Vec3 pos = Vec3Argument.getVec3(context, "pos");
                                                                        Vec3 rot = Vec3Argument.getVec3(context, "rot");
                                                                        Holder.Reference<PxMaterial> material = registry.lookupOrThrow(PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY)
                                                                                .getOrThrow(ResourceKey.create(PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY,
                                                                                        ResourceLocationArgument.getId(context, "material")));

                                                                        PxRigidDynamic box = Backstage.createDynamicBox((float) pos.x, (float) pos.y, (float) pos.z, material.value());
                                                                        box.getGlobalPose().getQ().rotate(PxVec3.createAt(mem, MemoryStack::nmalloc, (float) rot.x, (float) rot.y, (float) rot.z));
                                                                        Stage.getOrCreateStage(source.getLevel()).addActor(box);

                                                                        source.sendSuccess(() -> Component.literal("Added new dynamic actor at " + pos + " with " + rot + " rotations and " + material.getRegisteredName() + "material"), false);

                                                                        return 1;
                                                                    }
                                                                })
                                                                .then(Commands.argument("size", Vec3Argument.vec3())
                                                                        .executes(context -> {
                                                                            //  /sim actor add dynamic <pos> <rot> <size> <material> <size>
                                                                            try (MemoryStack mem = MemoryStack.stackPush()) {
                                                                                CommandSourceStack source = context.getSource();
                                                                                Vec3 pos = Vec3Argument.getVec3(context, "pos");
                                                                                Vec3 rot = Vec3Argument.getVec3(context, "rot");
                                                                                Holder.Reference<PxMaterial> material = registry.lookupOrThrow(PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY)
                                                                                        .getOrThrow(ResourceKey.create(PhysicsMaterialRegistry.MATERIAL_REGISTRY_KEY,
                                                                                                ResourceLocationArgument.getId(context, "material")));
                                                                                Vec3 size = Vec3Argument.getVec3(context, "size");

                                                                                PxShape shape = Backstage.createBoxShape((float) size.x, (float) size.y, (float) size.z, 0, 0, 0, material.value());
                                                                                PxRigidStatic box = Backstage.createStaticBodyWithShapes((float) pos.x, (float) pos.y, (float) pos.z, shape);
                                                                                box.getGlobalPose().getQ().rotate(PxVec3.createAt(mem, MemoryStack::nmalloc, (float) rot.x, (float) rot.y, (float) rot.z));
                                                                                Stage.getOrCreateStage(source.getLevel()).addActor(box);

                                                                                source.sendSuccess(() -> Component.literal("Added new dynamic actor at " + pos + " with " + rot + " rotations, " + material.getRegisteredName() + " material and " + size + "half-lengths"), false);

                                                                                return 1;
                                                                            }
                                                                        })
                                                                )
                                                        )
                                                )
                                        )
                                )
                        ).then(Commands.literal("remove")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("all")
                                        .executes(context -> {
                                            CommandSourceStack source = context.getSource();

                                            Stage stage = Stage.getOrCreateStage(source.getLevel());
                                            ArrayList<PxActor> actors = (ArrayList<PxActor>) stage.actors.clone();
                                            actors.forEach(stage::removeAndFreeActor);
                                            actors.clear();

                                            source.sendSystemMessage(Component.literal("Removed all actors from the current level"));

                                            return 1;
                                        })
                                )
                        )
                )
        );
    }
}
