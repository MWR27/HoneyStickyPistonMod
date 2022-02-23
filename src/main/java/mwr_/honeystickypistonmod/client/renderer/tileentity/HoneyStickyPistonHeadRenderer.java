package mwr_.honeystickypistonmod.client.renderer.tileentity;

import java.util.Random;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import mwr_.honeystickypistonmod.block.HoneyStickyPistonBaseBlock;
import mwr_.honeystickypistonmod.block.HoneyStickyPistonHeadBlock;
import mwr_.honeystickypistonmod.block.ModBlocks;
import mwr_.honeystickypistonmod.tileentity.HoneyStickyPistonMovingBlockEntity;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HoneyStickyPistonHeadRenderer implements BlockEntityRenderer<HoneyStickyPistonMovingBlockEntity> {
   private BlockRenderDispatcher blockRenderer;

   public HoneyStickyPistonHeadRenderer(BlockEntityRendererProvider.Context p_173623_) {
      this.blockRenderer = p_173623_.getBlockRenderDispatcher();
   }

   public void render(HoneyStickyPistonMovingBlockEntity p_112452_, float p_112453_, PoseStack p_112454_, MultiBufferSource p_112455_, int p_112456_, int p_112457_) {
      Level level = p_112452_.getLevel();
      if (level != null) {
         BlockPos blockpos = p_112452_.getBlockPos().relative(p_112452_.getMovementDirection().getOpposite());
         BlockState blockstate = p_112452_.getMovedState();
         if (!blockstate.isAir()) {
            ModelBlockRenderer.enableCaching();
            p_112454_.pushPose();
            p_112454_.translate((double)p_112452_.getXOff(p_112453_), (double)p_112452_.getYOff(p_112453_), (double)p_112452_.getZOff(p_112453_));
            if (blockstate.is(ModBlocks.HONEY_STICKY_PISTON_HEAD.get()) && p_112452_.getProgress(p_112453_) <= 4.0F) {
               blockstate = blockstate.setValue(HoneyStickyPistonHeadBlock.SHORT, Boolean.valueOf(p_112452_.getProgress(p_112453_) <= 0.5F));
               this.renderBlock(blockpos, blockstate, p_112454_, p_112455_, level, false, p_112457_);
            } else if (p_112452_.isSourcePiston() && !p_112452_.isExtending()) {
               PistonType pistontype = blockstate.is(Blocks.STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT;
               BlockState blockstate1 = ModBlocks.HONEY_STICKY_PISTON_HEAD.get().defaultBlockState().setValue(HoneyStickyPistonHeadBlock.TYPE, pistontype).setValue(HoneyStickyPistonHeadBlock.FACING, blockstate.getValue(HoneyStickyPistonBaseBlock.FACING));
               blockstate1 = blockstate1.setValue(HoneyStickyPistonHeadBlock.SHORT, Boolean.valueOf(p_112452_.getProgress(p_112453_) >= 0.5F));
               this.renderBlock(blockpos, blockstate1, p_112454_, p_112455_, level, false, p_112457_);
               BlockPos blockpos1 = blockpos.relative(p_112452_.getMovementDirection());
               p_112454_.popPose();
               p_112454_.pushPose();
               blockstate = blockstate.setValue(HoneyStickyPistonBaseBlock.EXTENDED, Boolean.valueOf(true));
               this.renderBlock(blockpos1, blockstate, p_112454_, p_112455_, level, true, p_112457_);
            } else {
               this.renderBlock(blockpos, blockstate, p_112454_, p_112455_, level, false, p_112457_);
            }

            p_112454_.popPose();
            ModelBlockRenderer.clearCache();
         }
      }
   }

   private void renderBlock(BlockPos p_112459_, BlockState p_112460_, PoseStack p_112461_, MultiBufferSource p_112462_, Level p_112463_, boolean p_112464_, int p_112465_) {
      net.minecraftforge.client.ForgeHooksClient.renderPistonMovedBlocks(p_112459_, p_112460_, p_112461_, p_112462_, p_112463_, p_112464_, p_112465_, blockRenderer == null ? blockRenderer = net.minecraft.client.Minecraft.getInstance().getBlockRenderer() : blockRenderer);
      if(false) {
      RenderType rendertype = ItemBlockRenderTypes.getMovingBlockRenderType(p_112460_);
      VertexConsumer vertexconsumer = p_112462_.getBuffer(rendertype);
      this.blockRenderer.getModelRenderer().tesselateBlock(p_112463_, this.blockRenderer.getBlockModel(p_112460_), p_112460_, p_112459_, p_112461_, vertexconsumer, p_112464_, new Random(), p_112460_.getSeed(p_112459_), p_112465_);
      }
   }

   public int getViewDistance() {
      return 68;
   }
}
