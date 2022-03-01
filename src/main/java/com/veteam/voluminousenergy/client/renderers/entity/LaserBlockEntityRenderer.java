package com.veteam.voluminousenergy.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.veteam.voluminousenergy.VoluminousEnergy;
import com.veteam.voluminousenergy.blocks.tiles.DimensionalLaserTile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class LaserBlockEntityRenderer implements BlockEntityRenderer<DimensionalLaserTile> {

    public static final ResourceLocation BEAM_RESOURCE_LOCATION =   new ResourceLocation(VoluminousEnergy.MODID, "textures/entity/beacon_beam.png");

    public LaserBlockEntityRenderer(BlockEntityRendererProvider.Context pContext) {
    }

    // If you want to modify things pull it from the DimensionalLaserTile
    @Override
    public void render(DimensionalLaserTile dimensionalLaserTile, float f1, @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int i1, int i2) {
        long gameTime = dimensionalLaserTile.getLevel().getGameTime();

        List<BeaconBlockEntity.BeaconBeamSection> list = new ArrayList<>();
        // If this float is not 1,1,1 expect a black screen. You can modify these if you want special colors
        list.add(new BeaconBlockEntity.BeaconBeamSection(new float[]{1, 1, 1}));
        int totalHeight = 0;

        for (int listPosition = 0; listPosition < list.size(); ++listPosition) {
            BeaconBlockEntity.BeaconBeamSection section = list.get(listPosition);
            //renderBeaconBeam(poseStack, multiBufferSource, f1, gameTime, totalHeight, listPosition == list.size() - 1 ? 1024 : section.getHeight(), section.getColor(),335 - dimensionalLaserTile.getBlockPos().getY());

            // USE FOR TESTING ONLY //TODO set y back to above y (above line of code)
            renderBeaconBeam(poseStack, multiBufferSource, f1, gameTime, totalHeight, listPosition == list.size() - 1 ? 1024 : section.getHeight(), section.getColor(), 50 - dimensionalLaserTile.getBlockPos().getY());

            totalHeight += section.getHeight();
        }
    }

    public static void renderBeaconBeam(PoseStack poseStack, MultiBufferSource multiBufferSource, float f1, long gameTime, int totalHeight, int beaconListSize, float[] beaconColor, int height) {
        renderBeaconBeam(poseStack, multiBufferSource, BEAM_RESOURCE_LOCATION, f1, gameTime, totalHeight, beaconListSize, beaconColor, height);
    }

    public static void renderBeaconBeam(PoseStack poseStack, MultiBufferSource multiBufferSource, ResourceLocation resourceLocation, float p_112188_, long gameTime, int totalHeight, int beaconListSize, float[] beaconColor, int height) {

        // Moved from method parameters to local since they appear static
        float static02F = 0.2F;
        float static025F = 0.25F;
        float static10F = 1.0F;
        float staticRotationNumber = -1.0F;
        //totalHeight + beaconListSize; Old height system
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        float somethingToDoWithTimeAndRotation = (float) Math.floorMod(gameTime, 40) + p_112188_;
        float rotationStuff = beaconListSize < 0 ? somethingToDoWithTimeAndRotation : -somethingToDoWithTimeAndRotation;

        float downwardMovement = Mth.frac(rotationStuff * -0.2F - (float) Mth.floor(rotationStuff * -0.1F)); // Switch negatives to positives to make beam go up
        float beaconColorR = beaconColor[0];
        float beaconColorG = beaconColor[1];
        float beaconColorB = beaconColor[2];
        poseStack.pushPose();
        //poseStack.mulPose(Vector3f.YP.rotationDegrees(somethingToDoWithTimeAndRotation * 2.25F - 45.0F)); // uncomment and comment below to activate rotation
        poseStack.mulPose(Vector3f.YP.rotationDegrees(45.0F));
        float f9 = -static02F;
        float f12 = -static02F;
        float f15 = staticRotationNumber + downwardMovement;
        float f16 = (float) beaconListSize * static10F * (0.5F / static02F) + f15;
        // TODO mess around with RenderTypes energyswirl looks really cool                                                                                                                                                                                                   // playing with f16,f15
        renderPart(poseStack, multiBufferSource.getBuffer(RenderType.beaconBeam(resourceLocation, false)), beaconColorR, beaconColorG, beaconColorB, 1.0F, totalHeight, height, 0.0F, static02F, static02F, 0.0F, f9, 0.0F, 0.0F, f12, f16, f15);
        poseStack.popPose();
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        //Matrix3f matrix3f = pose.normal();

        float portalHeight = 3F;

        int size = 50;

//        renderFace(matrix4f, multiBufferSource.getBuffer(RenderType.endPortal()), 0.0F, 1.0F * size, portalHeight, portalHeight, 0.0F, 0.0F, 1.0F * size, 1.0F + size, Direction.DOWN);
        poseStack.popPose();
    }

    private static void renderPart(PoseStack poseStack, VertexConsumer vertexConsumer, float beaconColorR, float beaconColorG, float beaconColorB, float static0P3F, int totalHeight, int height, float f6, float f7, float nfloat1, float f8, float nfloat2, float nfloat3, float nfloat4, float nfloat5, float f16, float f15) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        renderQuad(matrix4f, matrix3f, vertexConsumer, beaconColorR, beaconColorG, beaconColorB, static0P3F, totalHeight, height, f6, f7, nfloat1, f8, (float) 0.0, (float) 1.0, f16, f15);
        renderQuad(matrix4f, matrix3f, vertexConsumer, beaconColorR, beaconColorG, beaconColorB, static0P3F, totalHeight, height, nfloat4, nfloat5, nfloat2, nfloat3, (float) 0.0, (float) 1.0, f16, f15);
        renderQuad(matrix4f, matrix3f, vertexConsumer, beaconColorR, beaconColorG, beaconColorB, static0P3F, totalHeight, height, nfloat1, f8, nfloat4, nfloat5, (float) 0.0, (float) 1.0, f16, f15);
        renderQuad(matrix4f, matrix3f, vertexConsumer, beaconColorR, beaconColorG, beaconColorB, static0P3F, totalHeight, height, nfloat2, nfloat3, f6, f7, (float) 0.0, (float) 1.0, f16, f15);
    }

    private static void renderQuad(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexConsumer, float beaconColorR, float beaconColorG, float beaconColorB, float staticOP3F, int totalHeight, int height, float point1x, float point1z, float point2x, float point2z, float patternRepeatHorizontalPositive, float patternRepeatHorizontalNegative, float patternLocationYNegative, float patternLocationYNegative2) {
        addVertex(matrix4f, matrix3f, vertexConsumer, beaconColorR, beaconColorG, beaconColorB, staticOP3F, height, point1x, point1z, patternRepeatHorizontalNegative, patternLocationYNegative);
        addVertex(matrix4f, matrix3f, vertexConsumer, beaconColorR, beaconColorG, beaconColorB, staticOP3F, totalHeight, point1x, point1z, patternRepeatHorizontalNegative, patternLocationYNegative2);
        addVertex(matrix4f, matrix3f, vertexConsumer, beaconColorR, beaconColorG, beaconColorB, staticOP3F, totalHeight, point2x, point2z, patternRepeatHorizontalPositive, patternLocationYNegative2);
        addVertex(matrix4f, matrix3f, vertexConsumer, beaconColorR, beaconColorG, beaconColorB, staticOP3F, height, point2x, point2z, patternRepeatHorizontalPositive, patternLocationYNegative);
    }

    private static void addVertex(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexConsumer, float beaconColorR, float beaconColorG, float beaconColorB, float staticOP3F, int heightOrTotalHeight, float xPos, float zPos, float patternRepeat, float patternRepeat2) {
        vertexConsumer.vertex(matrix4f, xPos, (float) heightOrTotalHeight, zPos).color(beaconColorR, beaconColorG, beaconColorB, staticOP3F).uv(patternRepeat, patternRepeat2).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
    }

    private static void renderFace(Matrix4f matrix4f, VertexConsumer vertexConsumer, float xPoint1, float xPoint2, float heightOffset, float heightOffset2, float zPoint1, float zPoint2, float zPoint3, float zPoint4, Direction direction) {
        vertexConsumer.vertex(matrix4f, xPoint1, heightOffset, zPoint1).endVertex();
        vertexConsumer.vertex(matrix4f, xPoint2, heightOffset, zPoint2).endVertex();
        vertexConsumer.vertex(matrix4f, xPoint2, heightOffset2, zPoint3).endVertex();
        vertexConsumer.vertex(matrix4f, xPoint1, heightOffset2, zPoint4).endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(@NonNull DimensionalLaserTile dimensionalLaserTile) {
        return true;
    }

    @Override
    public boolean shouldRender(@NonNull DimensionalLaserTile p_173531_, @Nonnull Vec3 p_173532_) {
        return Vec3.atCenterOf(p_173531_.getBlockPos()).multiply(1.0D, 0.0D, 1.0D).closerThan(p_173532_.multiply(1.0D, 0.0D, 1.0D), this.getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return 256;
    }


}
