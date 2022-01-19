package net.arathain.tot.client.entity.renderer.string;

import net.arathain.tot.common.entity.string.StringCollisionEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

/**
 * Renderer for the {@link StringCollisionEntity}.
 * Entities are required to have a renderer. So this is the class that "renders" the entity.
 * Since this entity does not have a texture, it does not need to render anything.
 *
 * @author legoatoom
 */
@Environment(EnvType.CLIENT)
public class StringCollisionEntityRenderer extends EntityRenderer<StringCollisionEntity> {

    public StringCollisionEntityRenderer(EntityRendererFactory.Context dispatcher) {
        super(dispatcher);
    }

    @Override
    public Identifier getTexture(StringCollisionEntity entity) {
        return null;
    }
}
