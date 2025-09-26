package io.github.techtastic.newtons_nonsense.physics.client;

import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.Effect;
import dev.engine_room.flywheel.api.visual.EffectVisual;
import dev.engine_room.flywheel.api.visualization.VisualEmbedding;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.task.RunnablePlan;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.NotNull;

public class ClientAppleVisual implements DynamicVisual, EffectVisual<ClientAppleVisual.AppleEffect> {
    private final ClientApple apple;
    private final Vec3i prevRenderOrigin;
    private final VisualEmbedding embedding;

    public ClientAppleVisual(VisualizationContext context, ClientApple apple, float v) {
        this.apple = apple;
        this.prevRenderOrigin = context.renderOrigin();
        this.embedding = context.createEmbedding(this.prevRenderOrigin);
    }

    @Override
    public @NotNull Plan<Context> planFrame() {
        return RunnablePlan.of(context -> this.apple.render(context, this.prevRenderOrigin));
    }

    @Override
    public void update(float v) {}

    @Override
    public void delete() {
        embedding.delete();
    }

    static class AppleEffect implements Effect {
        private final LevelAccessor accessor;
        private final ClientApple apple;

        public AppleEffect(LevelAccessor accessor, ClientApple apple) {
            this.accessor = accessor;
            this.apple = apple;
        }

        @Override
        public @NotNull LevelAccessor level() {
            return this.accessor;
        }

        @Override
        public @NotNull EffectVisual<?> visualize(VisualizationContext ctx, float v) {
            return new ClientAppleVisual(ctx, this.apple, v);
        }
    }
}
