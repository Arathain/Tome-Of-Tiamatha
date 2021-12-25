package net.arathain.tot.mixin;

import net.arathain.tot.common.entity.spider.IClimberEntity;
import net.arathain.tot.common.entity.spider.Orientation;
import net.arathain.tot.common.entity.spider.PathingTarget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.SpiderEntityRenderer;
import net.minecraft.client.render.entity.model.SpiderEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.util.math.*;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(SpiderEntityRenderer.class)
public abstract class BetterSpiderEntityRendererMixin<T extends SpiderEntity> extends MobEntityRenderer<T, SpiderEntityModel<T>> {
    public BetterSpiderEntityRendererMixin(EntityRendererFactory.Context context, SpiderEntityModel<T> climberModel, float f) {
        super(context, climberModel, f);
    }

    @Override
    public void render(T mobEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        IClimberEntity climber = (IClimberEntity) mobEntity;


        Orientation orientation = climber.getOrientation();
        Orientation renderOrientation = climber.calculateOrientation(g);
        climber.setRenderOrientation(renderOrientation);

        float verticalOffset = climber.getVerticalOffset(g);

        float x = climber.getAttachmentOffset(Direction.Axis.X, g) - (float) renderOrientation.normal.x * verticalOffset;
        float y = climber.getAttachmentOffset(Direction.Axis.Y, g) - (float) renderOrientation.normal.y * verticalOffset;
        float z = climber.getAttachmentOffset(Direction.Axis.Z, g) - (float) renderOrientation.normal.z * verticalOffset;

        matrixStack.translate(x, y, z);

        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(renderOrientation.yaw));
        matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(renderOrientation.pitch));
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((float) Math.signum(0.5f - orientation.componentY - orientation.componentZ - orientation.componentX) * renderOrientation.yaw));
        super.render(mobEntity, f, g, matrixStack, vertexConsumerProvider, i);

        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-(float) Math.signum(0.5f - orientation.componentY - orientation.componentZ - orientation.componentX) * renderOrientation.yaw));
        matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-renderOrientation.pitch));
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-renderOrientation.yaw));

        if(MinecraftClient.getInstance().getEntityRenderDispatcher().shouldRenderHitboxes()) {
            WorldRenderer.drawBox(matrixStack, vertexConsumerProvider.getBuffer(RenderLayer.LINES), new Box(0, 0, 0, 0, 0, 0).expand(0.2f), 1.0f, 1.0f, 1.0f, 1.0f);

            double rx = mobEntity.prevX + (mobEntity.getX() - mobEntity.prevX) * g;
            double ry = mobEntity.prevY + (mobEntity.getY() - mobEntity.prevY) * g;
            double rz = mobEntity.prevZ + (mobEntity.getZ() - mobEntity.prevZ) * g;

            Vec3d movementTarget = climber.getTrackedMovementTarget();

            if(movementTarget != null) {
                WorldRenderer.drawBox(matrixStack, vertexConsumerProvider.getBuffer(RenderLayer.LINES), new Box(movementTarget.getX() - 0.25f, movementTarget.getY() - 0.25f, movementTarget.getZ() - 0.25f, movementTarget.getX() + 0.25f, movementTarget.getY() + 0.25f, movementTarget.getZ() + 0.25f).offset(-rx - x, -ry - y, -rz - z), 0.0f, 1.0f, 1.0f, 1.0f);
            }

            List<PathingTarget> pathingTargets = climber.getTrackedPathingTargets();

            if(pathingTargets != null) {
                i = 0;

                for(PathingTarget pathingTarget : pathingTargets) {
                    BlockPos pos = pathingTarget.pos;

                    WorldRenderer.drawBox(matrixStack, vertexConsumerProvider.getBuffer(RenderLayer.LINES), new Box(pos).offset(-rx - x, -ry - y, -rz - z), 1.0f, i / (float) (pathingTargets.size() - 1), 0.0f, 0.15f);

                    matrixStack.push();
                    matrixStack.translate(pos.getX() + 0.5D - rx - x, pos.getY() + 0.5D - ry - y, pos.getZ() + 0.5D - rz - z);

                    matrixStack.multiply(pathingTarget.side.getOpposite().getRotationQuaternion());

                    WorldRenderer.drawBox(matrixStack, vertexConsumerProvider.getBuffer(RenderLayer.LINES), new Box(-0.501D, -0.501D, -0.501D, 0.501D, -0.45D, 0.501D), 1.0f, i / (float) (pathingTargets.size() - 1), 0.0f, 1.0f);

                    Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
                    VertexConsumer builder = vertexConsumerProvider.getBuffer(RenderLayer.LINES);

                    builder.vertex(matrix4f, -0.501f, -0.45f, -0.501f).color(1.0f, i / (float) (pathingTargets.size() - 1), 0.0f, 1.0f).next();
                    builder.vertex(matrix4f, 0.501f, -0.45f, 0.501f).color(1.0f, i / (float) (pathingTargets.size() - 1), 0.0f, 1.0f).next();
                    builder.vertex(matrix4f, -0.501f, -0.45f, 0.501f).color(1.0f, i / (float) (pathingTargets.size() - 1), 0.0f, 1.0f).next();
                    builder.vertex(matrix4f, 0.501f, -0.45f, -0.501f).color(1.0f, i / (float) (pathingTargets.size() - 1), 0.0f, 1.0f).next();

                    matrixStack.pop();

                    i++;
                }
            }

            Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
            VertexConsumer builder = vertexConsumerProvider.getBuffer(RenderLayer.LINES);

            builder.vertex(matrix4f, 0, 0, 0).color(0, 1, 1, 1).next();
            builder.vertex(matrix4f, (float) orientation.normal.x * 2, (float) orientation.normal.y * 2, (float) orientation.normal.z * 2).color(1.0f, 0.0f, 1.0f, 1.0f).next();

            WorldRenderer.drawBox(matrixStack, vertexConsumerProvider.getBuffer(RenderLayer.LINES), new Box(0, 0, 0, 0, 0, 0).offset((float) orientation.normal.x * 2, (float) orientation.normal.y * 2, (float) orientation.normal.z * 2).expand(0.025f), 1.0f, 0.0f, 1.0f, 1.0f);

            matrixStack.push();

            matrixStack.translate(-x, -y, -z);

            matrix4f = matrixStack.peek().getPositionMatrix();

            builder.vertex(matrix4f, 0, mobEntity.getHeight() * 0.5f, 0).color(0, 1, 1, 1).next();
            builder.vertex(matrix4f, (float) orientation.localX.x, mobEntity.getHeight() * 0.5f + (float) orientation.localX.y, (float) orientation.localX.z).color(1.0f, 0.0f, 0.0f, 1.0f).next();

            WorldRenderer.drawBox(matrixStack, vertexConsumerProvider.getBuffer(RenderLayer.LINES), new Box(0, 0, 0, 0, 0, 0).offset((float) orientation.localX.x, mobEntity.getHeight() * 0.5f + (float) orientation.localX.y, (float) orientation.localX.z).expand(0.025f), 1.0f, 0.0f, 0.0f, 1.0f);

            builder.vertex(matrix4f, 0, mobEntity.getHeight() * 0.5f, 0).color(0, 1, 1, 1).next();
            builder.vertex(matrix4f, (float) orientation.localY.x, mobEntity.getHeight() * 0.5f + (float) orientation.localY.y, (float) orientation.localY.z).color(0.0f, 1.0f, 0.0f, 1.0f).next();

            WorldRenderer.drawBox(matrixStack, vertexConsumerProvider.getBuffer(RenderLayer.LINES), new Box(0, 0, 0, 0, 0, 0).offset((float) orientation.localY.x, mobEntity.getHeight() * 0.5f + (float) orientation.localY.y, (float) orientation.localY.z).expand(0.025f), 0.0f, 1.0f, 0.0f, 1.0f);

            builder.vertex(matrix4f, 0, mobEntity.getHeight() * 0.5f, 0).color(0, 1, 1, 1).next();
            builder.vertex(matrix4f, (float) orientation.localZ.x, mobEntity.getHeight() * 0.5f + (float) orientation.localZ.y, (float) orientation.localZ.z).color(0.0f, 0.0f, 1.0f, 1.0f).next();

            WorldRenderer.drawBox(matrixStack, vertexConsumerProvider.getBuffer(RenderLayer.LINES), new Box(0, 0, 0, 0, 0, 0).offset((float) orientation.localZ.x, mobEntity.getHeight() * 0.5f + (float) orientation.localZ.y, (float) orientation.localZ.z).expand(0.025f), 0.0f, 0.0f, 1.0f, 1.0f);

            matrixStack.pop();
        }

        matrixStack.translate(-x, -y, -z);
    }
}
