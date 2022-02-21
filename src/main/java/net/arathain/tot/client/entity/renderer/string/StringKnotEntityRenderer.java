package net.arathain.tot.client.entity.renderer.string;

import net.arathain.tot.common.entity.string.StringKnotEntity;
import net.arathain.tot.common.util.StringUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.LightType;

import java.util.ArrayList;

/**
 * <p>This class renders the chain you see in game. The block around the fence and the chain.
 * You could use this code to start to understand how this is done.
 * I tried to make it as easy to understand as possible, mainly for myself, since the MobEntityRenderer has a lot of
 * unclear code and shortcuts made.</p>
 *
 *
 * @see net.minecraft.client.render.entity.LeashKnotEntityRenderer
 * @see net.minecraft.client.render.entity.MobEntityRenderer
 * @author legoatoom
 */
@Environment(EnvType.CLIENT)
public class StringKnotEntityRenderer extends EntityRenderer<StringKnotEntity> {
    private static final Identifier KNOT_TEXTURE = StringUtils.identifier("textures/entity/drider/string/string_knot.png");
    private static final Identifier CHAIN_TEXTURE = StringUtils.identifier("textures/entity/drider/string/string.png");;
    private final StringRenderer stringRenderer = new StringRenderer();

    public StringKnotEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(StringKnotEntity entity, Frustum frustum, double x, double y, double z) {
        boolean should = entity.getHoldingEntities().stream().anyMatch(entity1 -> {
            if (entity1 instanceof StringKnotEntity) {
                if (!entity1.shouldRender(x, y, z)) {
                    return false;
                } else if (entity1.ignoreCameraFrustum) {
                    return true;
                } else {
                    Box box = entity1.getVisibilityBoundingBox().expand(entity.distanceTo(entity1) / 2D);
                    if (box.isValid() || box.getAverageSideLength() == 0.0D) {
                        box = new Box(entity1.getX() - 2.0D, entity1.getY() - 2.0D, entity1.getZ() - 2.0D, entity1.getX() + 2.0D, entity1.getY() + 2.0D, entity1.getZ() + 2.0D);
                    }

                    return frustum.isVisible(box);
                }
            } else return entity1 instanceof PlayerEntity;
        });
        return super.shouldRender(entity, frustum, x, y, z) || should;
    }

    public Identifier getTexture(StringKnotEntity StringKnotEntity) {
        return KNOT_TEXTURE;
    }

    @Override
    public void render(StringKnotEntity StringKnotEntity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        Vec3d leashOffset = StringKnotEntity.getLeashPos(tickDelta).subtract(StringKnotEntity.getLerpedPos(tickDelta));
        matrices.translate(leashOffset.x, leashOffset.y + 6.5/16f, leashOffset.z);
        matrices.scale(5/6f, 1, 5/6f);
        matrices.pop();
        ArrayList<Entity> entities = StringKnotEntity.getHoldingEntities();
        for (Entity entity : entities) {
            this.createStringLine(StringKnotEntity, tickDelta, matrices, vertexConsumers, entity);
        }
        super.render(StringKnotEntity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    /**
     * If I am honest I do not really know what is happening here most of the time, most of the code was 'inspired' by
     * the {@link net.minecraft.client.render.entity.LeashKnotEntityRenderer}.
     * Many variables therefore have simple names. I tried my best to comment and explain what everything does.
     *
     * @param fromEntity             The origin Entity
     * @param tickDelta              Delta tick
     * @param matrices               The render matrix stack.
     * @param vertexConsumerProvider The VertexConsumerProvider, whatever it does.
     * @param toEntity               The entity that we connect the chain to, this can be a {@link PlayerEntity} or a {@link StringKnotEntity}.
     */
    private void createStringLine(StringKnotEntity fromEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, Entity toEntity) {
        if (toEntity == null) return; // toEntity can be null, this will return the function if it is null.
        matrices.push();

        // Don't have to lerp knot position as it can't move
        // Also lerping the position of an entity that was just created
        // causes visual bugs because the position is lerped from 0/0/0.
        Vec3d srcPos = fromEntity.getPos().add(fromEntity.getLeashOffset());
        Vec3d dstPos;

        if(toEntity instanceof AbstractDecorationEntity) {
            dstPos = toEntity.getPos().add(toEntity.getLeashOffset());
        } else {
            dstPos = toEntity.getLeashPos(tickDelta);
        }

        // The leash pos offset
        Vec3d leashOffset = fromEntity.getLeashOffset();
        matrices.translate(leashOffset.x, leashOffset.y, leashOffset.z);

        // Some further performance improvements can be made here:
        // Create a rendering layer that:
        // - does not have normals
        // - does not have an overlay
        // - does not have vertex color
        // - uses a tri strip instead of quads
        VertexConsumer buffer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutoutNoCull(CHAIN_TEXTURE));

        Vec3f offset = StringUtils.getStringOffset(srcPos, dstPos);
        matrices.translate(offset.getX(), 0, offset.getZ());

        // Now we gather light information for the chain. Since the chain is lighter if there is more light.
        BlockPos blockPosOfStart = new BlockPos(fromEntity.getCameraPosVec(tickDelta));
        BlockPos blockPosOfEnd = new BlockPos(toEntity.getCameraPosVec(tickDelta));
        blockPosOfStart = new BlockPos(MathHelper.lerp(0.3, blockPosOfStart.getX(), blockPosOfEnd.getX()), MathHelper.lerp(0.3, blockPosOfStart.getY(), blockPosOfEnd.getY()), MathHelper.lerp(0.3, blockPosOfStart.getZ(), blockPosOfEnd.getZ()));
        blockPosOfEnd = new BlockPos(MathHelper.lerp(0.7, blockPosOfStart.getX(), blockPosOfEnd.getX()), MathHelper.lerp(0.7, blockPosOfStart.getY(), blockPosOfEnd.getY()), MathHelper.lerp(0.7, blockPosOfStart.getZ(), blockPosOfEnd.getZ()));
        int blockLightLevelOfStart = fromEntity.world.getLightLevel(LightType.BLOCK, blockPosOfStart);
        int blockLightLevelOfEnd = toEntity.world.getLightLevel(LightType.BLOCK, blockPosOfEnd);
        int skylightLevelOfStart = fromEntity.world.getLightLevel(LightType.SKY, blockPosOfStart);
        int skylightLevelOfEnd = fromEntity.world.getLightLevel(LightType.SKY, blockPosOfEnd);

        Vec3d startPos = srcPos.add(offset.getX(), 0, offset.getZ());
        Vec3d endPos = dstPos.add(-offset.getX(), 0, -offset.getZ());
        Vec3f chainVec = new Vec3f((float) (endPos.x - startPos.x), (float) (endPos.y - startPos.y), (float) (endPos.z - startPos.z));

        float angleY = -(float) Math.atan2(chainVec.getZ(), chainVec.getX());
        matrices.multiply(Quaternion.fromEulerXyz(0, angleY, 0));

        if (toEntity instanceof AbstractDecorationEntity) {
            StringRenderer.BakeKey key = new StringRenderer.BakeKey(fromEntity.getPos(), toEntity.getPos());
            stringRenderer.renderBaked(buffer, matrices, key, chainVec, blockLightLevelOfStart, blockLightLevelOfEnd, skylightLevelOfStart, skylightLevelOfEnd);
        } else {
            stringRenderer.render(buffer, matrices, chainVec, blockLightLevelOfStart, blockLightLevelOfEnd, skylightLevelOfStart, skylightLevelOfEnd);
        }

        matrices.pop();
    }

    public StringRenderer getStringRenderer() {
        return stringRenderer;
    }
}
