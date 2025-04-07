package io.github.techtastic.newtons_nonsense.item.custom;

import io.github.techtastic.newtons_nonsense.physics.Stage;
import io.github.techtastic.newtons_nonsense.physics.pipeline.Backstage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import physx.physics.PxRigidBodyFlagEnum;
import physx.physics.PxRigidDynamic;

public class ActorSpawnerItem extends Item {
    public ActorSpawnerItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext useOnContext) {
        // Spawn Actor at Clicked Position

        Level level = useOnContext.getLevel();
        if (!(level instanceof ServerLevel sLevel))
            return InteractionResult.SUCCESS;

        Vec3 pos = useOnContext.getClickedPos().relative(useOnContext.getClickedFace()).getCenter();
        PxRigidDynamic box = Backstage.createDefaultBox((float) pos.x, (float) pos.y, (float) pos.z);
        Stage.getOrCreateStage(sLevel).addActor(box);

        return InteractionResult.SUCCESS;
    }
}
