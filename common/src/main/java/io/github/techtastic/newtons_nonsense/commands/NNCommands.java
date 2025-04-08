package io.github.techtastic.newtons_nonsense.commands;

import com.mojang.brigadier.CommandDispatcher;
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
import net.minecraft.world.phys.Vec3;
import physx.physics.PxActor;
import physx.physics.PxMaterial;
import physx.physics.PxRigidDynamic;

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
                            PxRigidDynamic box = Backstage.createDynamicBox((float) pos.x, (float) pos.y, (float) pos.z);
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
    }
}
