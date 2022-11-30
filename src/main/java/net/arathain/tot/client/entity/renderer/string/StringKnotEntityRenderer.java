package net.arathain.tot.client.entity.renderer.string;

import com.github.legoatoom.connectiblechains.ConnectibleChains;
import com.github.legoatoom.connectiblechains.util.Helper;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.arathain.tot.common.entity.string.StringKnotEntity;
import net.arathain.tot.common.entity.string.StringLink;
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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.LightType;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>This class renders the chain you see in game. The block around the fence and the chain.
 * You could use this code to start to understand how this is done.
 * I tried to make it as easy to understand as possible, mainly for myself, since the MobEntityRenderer has a lot of
 * unclear code and shortcuts made.</p>
 *
 *
 * @see net.minecraft.client.render.entity.LeashKnotEntityRenderer
 * @see net.minecraft.client.render.entity.MobEntityRenderer
 * @author legoatoom, Qendolin
 */
@Environment(EnvType.CLIENT)
public class StringKnotEntityRenderer extends EntityRenderer<StringKnotEntity> {
    private static final Identifier KNOT_TEXTURE = StringUtils.identifier("textures/entity/drider/string/string_knot.png");
    private static final Identifier STRING_TEXTURE = StringUtils.identifier("textures/entity/drider/string/string.png");;
    private final StringRenderer stringRenderer = new StringRenderer();

    public StringKnotEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(StringKnotEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    public Identifier getTexture(StringKnotEntity StringKnotEntity) {
        return KNOT_TEXTURE;
    }

    @Override
    public void render(StringKnotEntity stringKnotEntity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        // Render the links
        List<StringLink> links = stringKnotEntity.getLinks();
        for (StringLink link : links) {
            if (link.primary != stringKnotEntity || link.isDead()) continue;
            this.renderStringLink(link, tickDelta, matrices, vertexConsumers);
            if (ConnectibleChains.runtimeConfig.doDebugDraw()) {
                this.drawDebugVector(matrices, stringKnotEntity, link.secondary, vertexConsumers.getBuffer(RenderLayer.LINES));
            }
        }
        if (ConnectibleChains.runtimeConfig.doDebugDraw()) {
            matrices.push();
            // F stands for "from", T for "to"
            Text holdingCount = Text.literal("F: " + stringKnotEntity.getLinks().stream()
                    .filter(l -> l.primary == stringKnotEntity).count());
            Text heldCount = Text.literal("T: " + stringKnotEntity.getLinks().stream()
                    .filter(l -> l.secondary == stringKnotEntity).count());
            matrices.translate(0, 0.25, 0);
            this.renderLabelIfPresent(stringKnotEntity, holdingCount, matrices, vertexConsumers, light);
            matrices.translate(0, 0.25, 0);
            this.renderLabelIfPresent(stringKnotEntity, heldCount, matrices, vertexConsumers, light);
            matrices.pop();
        }

        super.render(stringKnotEntity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    /**
     * If I am honest I do not really know what is happening here most of the time, most of the code was 'inspired' by
     * the {@link net.minecraft.client.render.entity.LeashKnotEntityRenderer}.
     * Many variables therefore have simple names. I tried my best to comment and explain what everything does.
     *
     * @param link                   A link that provides the positions and type
     * @param tickDelta              Delta tick
     * @param matrices               The render matrix stack.
     * @param vertexConsumerProvider The VertexConsumerProvider, whatever it does.
     */
    private void renderStringLink(StringLink link, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider) {
        StringKnotEntity fromEntity = link.primary;
        Entity toEntity = link.secondary;
        matrices.push();

        // Don't have to lerp knot position as it can't move
        // Also lerping the position of an entity that was just created
        // causes visual bugs because the position is lerped from 0/0/0.
        Vec3d srcPos = fromEntity.getPos().add(fromEntity.getLeashOffset());
        Vec3d dstPos;

        if (toEntity instanceof AbstractDecorationEntity) {
            dstPos = toEntity.getPos().add(toEntity.getLeashOffset());
        } else {
            dstPos = toEntity.getLerpedPos(tickDelta);
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
        VertexConsumer buffer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutoutNoCull(STRING_TEXTURE));
        if (ConnectibleChains.runtimeConfig.doDebugDraw()) {
            buffer = vertexConsumerProvider.getBuffer(RenderLayer.getLines());
        }

        Vec3f offset = Helper.getChainOffset(srcPos, dstPos);
        matrices.translate(offset.getX(), 0, offset.getZ());

        // Now we gather light information for the string. This is janky, but it somewhat works.
        BlockPos blockPosOfStart = new BlockPos(MathHelper.lerp(0.3, fromEntity.getCameraPosVec(tickDelta).x, toEntity.getCameraPosVec(tickDelta).x), MathHelper.lerp(0.3, fromEntity.getCameraPosVec(tickDelta).y, toEntity.getCameraPosVec(tickDelta).y), MathHelper.lerp(0.3, fromEntity.getCameraPosVec(tickDelta).z, toEntity.getCameraPosVec(tickDelta).z));
        BlockPos blockPosOfEnd = new BlockPos(MathHelper.lerp(0.7, fromEntity.getCameraPosVec(tickDelta).x, toEntity.getCameraPosVec(tickDelta).x), MathHelper.lerp(0.7, fromEntity.getCameraPosVec(tickDelta).y, toEntity.getCameraPosVec(tickDelta).y), MathHelper.lerp(0.7, fromEntity.getCameraPosVec(tickDelta).z, toEntity.getCameraPosVec(tickDelta).z));
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

    /**
     * Draws a line fromEntity - toEntity, from green to red.
     */
    private void drawDebugVector(MatrixStack matrices, Entity fromEntity, Entity toEntity, VertexConsumer buffer) {
        if (toEntity == null) return;
        Matrix4f modelMat = matrices.peek().getModel();
        Vec3d vec = toEntity.getPos().subtract(fromEntity.getPos());
        Vec3d normal = vec.normalize();
        buffer.vertex(modelMat, 0, 0, 0)
                .color(0, 255, 0, 255)
                .normal((float) normal.x, (float) normal.y, (float) normal.z).next();
        buffer.vertex(modelMat, (float) vec.x, (float) vec.y, (float) vec.z)
                .color(255, 0, 0, 255)
                .normal((float) normal.x, (float) normal.y, (float) normal.z).next();
    }

    public StringRenderer getStringRenderer() {
        return stringRenderer;
    }
}
