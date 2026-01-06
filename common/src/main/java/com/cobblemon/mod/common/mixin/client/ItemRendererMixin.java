/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.ModAPI;
import com.cobblemon.mod.common.client.render.bedrock.SimpleBedrockRenderer;
import com.cobblemon.mod.common.client.render.bedrock.SimplePosableState;
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState;
import com.cobblemon.mod.common.item.PokeBallItem;
import com.cobblemon.mod.common.item.PokedexItem;
import com.cobblemon.mod.common.item.WearableItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
    I think it would be nice to maybe use the existing mixin we have for custom item rendering [BuiltinModelItemRendererMixin]
    Obviously that would require changes. Potentially each item renderer defines what modes it overrides for?
 */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    // ignore the hint saying implmentation is never null, if something crashes then it is null and causes red herrings
    @Unique private final String MODEL_PATH = (Cobblemon.implementation != null && Cobblemon.implementation.getModAPI() == ModAPI.FABRIC) ? "fabric_resource" : "standalone";

    @Shadow @Final private ItemModelShaper itemModelShaper;

    @Unique SimplePosableState deltaclient$simplePosableState = new SimplePosableState();

    @Shadow public abstract void render(ItemStack stack, ItemDisplayContext renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model);

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cobblemon$renderCosmetic(ItemStack itemStack, ItemDisplayContext displayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay, BakedModel model, CallbackInfo ci) {
        if (itemStack.isEmpty()) return;

        if (!itemStack.has(DataComponents.CUSTOM_DATA)) return;

        CustomData data = itemStack.get(DataComponents.CUSTOM_DATA);

        if (data.contains("BedrockModelData")) {
            cobblemon$renderBedrockModelFromItemStack(data, displayContext, poseStack, bufferSource, combinedLight, ci);
        }
    }

    @Inject(
        method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void cobblemon$overrideItemModel(ItemStack stack, ItemDisplayContext renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource multiBufferSource, int light, int overlay, BakedModel model, CallbackInfo ci) {
        boolean shouldBe2d = renderMode == ItemDisplayContext.GUI || renderMode == ItemDisplayContext.FIXED;
        ResourceLocation resourceLocation = null;
        if (shouldBe2d) {
            if (stack.getItem() instanceof PokeBallItem pokeBallItem) resourceLocation = pokeBallItem.getPokeBall().getModel2d();
            else if (stack.getItem() instanceof PokedexItem pokedexItem) resourceLocation = pokedexItem.getType().getItemSpritePath();
        }
        if (renderMode != ItemDisplayContext.HEAD && stack.getItem() instanceof WearableItem wearableItem) resourceLocation = wearableItem.getModel2d();

        if (resourceLocation != null) {
            BakedModel replacementModel = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation(resourceLocation, "inventory"));
            if (!cobblemon$isSameModel(model, replacementModel)) {
                ci.cancel();
                render(stack, renderMode, leftHanded, matrices, multiBufferSource, light, overlay, replacementModel);
            }
        }
    }

    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    private void cobblemon$getItemModel(ItemStack stack, Level world, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        ResourceLocation resourceLocation = null;
        if (stack.getItem() instanceof PokeBallItem pokeBallItem) resourceLocation = pokeBallItem.getPokeBall().getModel3d();
        else if (stack.getItem() instanceof PokedexItem pokedexItem) {
            boolean isScanModel = entity instanceof Player && entity.getUseItem() == stack && entity.isUsingItem();
            boolean canOpenScreen = entity != null && ((entity.getOffhandItem() == stack && !(entity.getMainHandItem().getItem() instanceof PokedexItem)) || entity.getMainHandItem() == stack);
            resourceLocation = pokedexItem.getType().getItemModelPath(isScanModel ? "scanning" : (canOpenScreen ? null : "off"));
        }
        else if (stack.getItem() instanceof WearableItem wearableItem) resourceLocation = wearableItem.getModel3d();

        if (resourceLocation != null) {
            BakedModel model = this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation(resourceLocation, MODEL_PATH));
            ClientLevel clientWorld = world instanceof ClientLevel ? (ClientLevel) world : null;
            BakedModel overriddenModel = model.getOverrides().resolve(model, stack, clientWorld, entity, seed);
            cir.setReturnValue(overriddenModel == null ? this.itemModelShaper.getModelManager().getMissingModel() : overriddenModel);
        }
    }

    // this method aims to cover an issue with modernfixes and it's "dynamic resource" causing the usual .equals check to always fail, resulting in a StackOverflow
    // this falls back to comparing the model contents (e.g. location of the texture, width and height)
    @Unique
    private static boolean cobblemon$isSameModel(BakedModel original, BakedModel replacement) {
        return original.equals(replacement) ||
                       (original.getParticleIcon().contents().name().equals(replacement.getParticleIcon().contents().name()) &&
                                original.getParticleIcon().contents().width() == replacement.getParticleIcon().contents().width() &&
                                original.getParticleIcon().contents().height() == replacement.getParticleIcon().contents().height());
    }

    @Unique
    private void cobblemon$renderBedrockModelFromItemStack(CustomData data, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, CallbackInfo ci) {
        var bedrockModelData = data.getUnsafe().get("BedrockModelData");
        if (!(bedrockModelData instanceof CompoundTag nbt)) return;

        var model = nbt.getString("Model");
        var texture = nbt.getString("Texture");
        var state = deltaclient$simplePosableState;
        var animation = nbt.contains("Animation", 8) ? nbt.getString("Animation") : null;

        var translation = cobblemon$getNullableArrayFromCompound(nbt, "Translation", new float[]{0f, 0f, 0f});
        var rotation = cobblemon$getNullableArrayFromCompound(nbt, "Rotation", new float[]{0f, 0f, 0f});
        var scale = cobblemon$getNullableArrayFromCompound(nbt, "Scale", new float[]{1f, 1f, 1f});

        if (model.isEmpty() || texture.isEmpty()) return;

        cobblemon$renderBedrockModel(
                displayContext,
                poseStack,
                bufferSource,
                combinedLight,
                ResourceLocation.parse(model),
                ResourceLocation.parse(texture),
                animation != null ? ResourceLocation.parse(animation) : null,
                state,
                translation,
                rotation,
                scale,
                ci
        );
    }

    @Unique
    private float[] cobblemon$getNullableArrayFromCompound(CompoundTag nbt, String tag, float[] fallback) {
        if (!nbt.contains(tag, 10)) return fallback;
        return new float[]{
                nbt.getCompound(tag).getFloat("X"),
                nbt.getCompound(tag).getFloat("Y"),
                nbt.getCompound(tag).getFloat("Z")
        };
    }

    @Unique
    private void cobblemon$renderBedrockModel(ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, ResourceLocation model, ResourceLocation texture, ResourceLocation animation, PosableState state, float[] translation, float[] rotation, float[] scale, CallbackInfo ci) {
        poseStack.pushPose();


        if (displayContext != ItemDisplayContext.GROUND) {
            poseStack.translate(0f, -0.5f, 0f);
        }

        if (translation != null) {
            poseStack.translate(translation[0], translation[1], translation[2]);
        }

        poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));

        if (rotation != null) {
            poseStack.mulPose(Axis.XP.rotationDegrees(rotation[0]));
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation[1]));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation[2]));
        }

        if (scale != null) {
            poseStack.scale(scale[0], scale[1], scale[2]);
        }

        var player = Minecraft.getInstance().player;
        if (player == null) {
            poseStack.popPose();
            return;
        }

        SimpleBedrockRenderer.INSTANCE.render(
                model,
                deltaclient$simplePosableState,
                texture,
                poseStack,
                bufferSource,
                combinedLight,
                player.tickCount,
                animation,
                player.tickCount
        );

        poseStack.popPose();
        ci.cancel();
    }

}
