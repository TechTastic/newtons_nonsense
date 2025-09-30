package io.github.techtastic.newtons_nonsense.physics.object.box;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualEmbedding;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import io.github.techtastic.newtons_nonsense.physics.client.AbstractPhysicsObjectVisual;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

import java.util.UUID;

public class BoxPhysicsObjectVisual extends AbstractPhysicsObjectVisual<BoxPhysicsObject> {
    private final TransformedInstance instance;
    private final VisualEmbedding embedding;
    private final Vec3i renderOrigin;

    public BoxPhysicsObjectVisual(ClientLevel level, UUID id, VisualizationContext context) {
        super(level, id, context);

        Model model = Models.block(this.getPhysicsObject().getState());
        this.instance = context.instancerProvider().instancer(InstanceTypes.TRANSFORMED, model).createInstance();
        this.renderOrigin = context.renderOrigin();
        this.embedding = context.createEmbedding(this.renderOrigin);
    }

    @Override
    public void beginFrame(Context context) {
        this.update(context.partialTick());
    }

    @Override
    public void update(float v) {
        BoxPhysicsObject box = getPhysicsObject();
        BoxPhysicsObject previousBox = getPreviousPhysicsObject();
        ClientLevel level = getLevel();

        //NewtonsNonsense.LOGGER.info("Object {} Rendered!\nPosition: {}\nRotation: {}", box.getId(), box.getPosition(), box.getRotation());

        Vec3 targetPos = box.getPosition();
        Quaternionfc targetRot = new Quaternionf(box.getRotation());
        if (previousBox != null) {
            targetPos = previousBox.getPosition().lerp(box.getPosition(), v);
            targetRot = new Quaternionf(previousBox.getRotation().slerp(box.getRotation(), v, new Quaterniond()));
        }
        Vec3 offsetPos = targetPos.subtract(Vec3.atLowerCornerOf(this.renderOrigin));

        this.instance
                .setIdentityTransform()
                .translate(offsetPos)
                .center()
                .rotate(targetRot)
                .uncenter()
                .light(level.getLightEngine().getRawBrightness(BlockPos.containing(targetPos), 0))
                .setChanged();
    }

    @Override
    public void delete() {
        this.instance.delete();
        this.embedding.delete();
    }
}
