package io.github.techtastic.newtons_nonsense.physics;

import com.mojang.datafixers.util.Either;
import io.github.techtastic.newtons_nonsense.physics.pipeline.Backstage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import physx.common.PxTransform;
import physx.physics.PxActor;
import physx.physics.PxRigidActor;
import physx.physics.PxRigidDynamic;
import physx.physics.PxRigidStatic;

public class Mimic {
    public final Display.BlockDisplay display;
    public final Stage stage;
    public Either<PxRigidDynamic, PxRigidStatic> actor;

    public Mimic(Display.BlockDisplay display, boolean isStatic) {
        this.display = display;
        this.stage = Stage.getOrCreateStage((ServerLevel) display.level());
        if (isStatic)
            this.actor = Either.left(Backstage.createDefaultBox((float) display.getX(), (float) display.getY(), (float) display.getZ()));
        else
            this.actor = Either.right(Backstage.createStaticBody(Backstage.createDefaultBoxShape((float) display.getX(), (float) display.getY(), (float) display.getZ()), (float) display.getX(), (float) display.getY(), (float) display.getZ()));

        actor.left().ifPresent(this.stage::addActor);
        actor.right().ifPresent(this.stage::addActor);
    }

    public void physTick() {
        if (this.display.isRemoved() || this.actor.right().isPresent())
            return;

        System.out.println("Display Position: " + this.display.position());

        //this.stage.scene.

        PxTransform pose = this.actor.left().get().getGlobalPose();
        System.out.println("Actor Position: " + new Vec3(
                pose.getP().getX(),
                pose.getP().getY(),
                pose.getP().getZ()
        ));

        this.display.setPos(
                pose.getP().getX(),
                pose.getP().getY(),
                pose.getP().getZ()
        );
        /*this.display.forceSetRotation(
                pose.getQ().getBasisVector0().getY(),
                pose.getQ().getBasisVector0().getX()
        );*/
    }

    public void free() {
        this.actor.left().ifPresent(PxActor::release);
        this.actor.right().ifPresent(PxActor::release);
    }
}
