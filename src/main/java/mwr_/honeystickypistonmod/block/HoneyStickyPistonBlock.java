package mwr_.honeystickypistonmod.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.block.BlockState;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MovingPistonBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PistonBlockStructureHelper;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.PistonType;
import mwr_.honeystickypistonmod.tileentity.HoneyStickyPistonTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class HoneyStickyPistonBlock extends PistonBlock {
   private final boolean isSticky;
   
   public HoneyStickyPistonBlock(boolean p_i48281_1_, AbstractBlock.Properties p_i48281_2_) {
      super(p_i48281_1_, p_i48281_2_);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(EXTENDED, Boolean.valueOf(false)));
      this.isSticky = p_i48281_1_;
   }

   @Override //Nothing edited, put in for checkIfExtend()
   public void setPlacedBy(World p_180633_1_, BlockPos p_180633_2_, BlockState p_180633_3_, LivingEntity p_180633_4_, ItemStack p_180633_5_) {
      if (!p_180633_1_.isClientSide) {
         this.checkIfExtend(p_180633_1_, p_180633_2_, p_180633_3_);
      }

   }
   
   @Override //Nothing edited, put in for checkIfExtend()
   public void neighborChanged(BlockState p_220069_1_, World p_220069_2_, BlockPos p_220069_3_, Block p_220069_4_, BlockPos p_220069_5_, boolean p_220069_6_) {
      if (!p_220069_2_.isClientSide) {
         this.checkIfExtend(p_220069_2_, p_220069_3_, p_220069_1_);
      }

   }
   
   @Override //Nothing edited, put in for checkIfExtend()
   public void onPlace(BlockState p_220082_1_, World p_220082_2_, BlockPos p_220082_3_, BlockState p_220082_4_, boolean p_220082_5_) {
      if (!p_220082_4_.is(p_220082_1_.getBlock())) {
         if (!p_220082_2_.isClientSide && p_220082_2_.getBlockEntity(p_220082_3_) == null) {
            this.checkIfExtend(p_220082_2_, p_220082_3_, p_220082_1_);
         }

      }
   }

   @Override //Nothing edited, may not be necessary
   public BlockState getStateForPlacement(BlockItemUseContext p_196258_1_) {
      return this.defaultBlockState().setValue(FACING, p_196258_1_.getNearestLookingDirection().getOpposite()).setValue(EXTENDED, Boolean.valueOf(false));
   }
   
   private void checkIfExtend(World p_176316_1_, BlockPos p_176316_2_, BlockState p_176316_3_) {
      Direction direction = p_176316_3_.getValue(FACING);
      boolean flag = this.getNeighborSignal(p_176316_1_, p_176316_2_, direction);
      if (flag && !p_176316_3_.getValue(EXTENDED)) {
         if ((new PistonBlockStructureHelper(p_176316_1_, p_176316_2_, direction, true)).resolve()) {
            p_176316_1_.blockEvent(p_176316_2_, this, 0, direction.get3DDataValue());
         }
      } else if (!flag && p_176316_3_.getValue(EXTENDED)) {
         BlockPos blockpos = p_176316_2_.relative(direction, 2);
         BlockState blockstate = p_176316_1_.getBlockState(blockpos);
         int i = 1;
         if (blockstate.is(Blocks.MOVING_PISTON) && blockstate.getValue(FACING) == direction) {
            TileEntity tileentity = p_176316_1_.getBlockEntity(blockpos);
            if (tileentity instanceof HoneyStickyPistonTileEntity) {
               HoneyStickyPistonTileEntity pistontileentity = (HoneyStickyPistonTileEntity)tileentity;
               if (pistontileentity.isExtending() && (pistontileentity.getProgress(0.0F) < 0.5F || p_176316_1_.getGameTime() == pistontileentity.getLastTicked() || ((ServerWorld)p_176316_1_).isHandlingTick())) {
                  i = 2;
               }
            }
         }

         p_176316_1_.blockEvent(p_176316_2_, this, i, direction.get3DDataValue());
      }

   }

   private boolean getNeighborSignal(World p_176318_1_, BlockPos p_176318_2_, Direction p_176318_3_) {
      for(Direction direction : Direction.values()) {
         if (direction != p_176318_3_ && p_176318_1_.hasSignal(p_176318_2_.relative(direction), direction)) {
            return true;
         }
      }

      if (p_176318_1_.hasSignal(p_176318_2_, Direction.DOWN)) {
         return true;
      } else {
         BlockPos blockpos = p_176318_2_.above();

         for(Direction direction1 : Direction.values()) {
            if (direction1 != Direction.DOWN && p_176318_1_.hasSignal(blockpos.relative(direction1), direction1)) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public boolean triggerEvent(BlockState p_176318_3_, World p_189539_2_, BlockPos p_189539_3_, int p_189539_4_, int p_189539_5_) {
      Direction direction = p_176318_3_.getValue(FACING);
      if (!p_189539_2_.isClientSide) {
         boolean flag = this.getNeighborSignal(p_189539_2_, p_189539_3_, direction);
         if (flag && (p_189539_4_ == 1 || p_189539_4_ == 2)) {
            p_189539_2_.setBlock(p_189539_3_, p_176318_3_.setValue(EXTENDED, Boolean.valueOf(true)), 2);
            return false;
         }

         if (!flag && p_189539_4_ == 0) {
            return false;
         }
      }

      if (p_189539_4_ == 0) {
         if (net.minecraftforge.event.ForgeEventFactory.onPistonMovePre(p_189539_2_, p_189539_3_, direction, true)) return false;
         if (!this.moveBlocks(p_189539_2_, p_189539_3_, direction, true)) {
            return false;
         }

         p_189539_2_.setBlock(p_189539_3_, p_176318_3_.setValue(EXTENDED, Boolean.valueOf(true)), 67);
         p_189539_2_.playSound((PlayerEntity)null, p_189539_3_, SoundEvents.PISTON_EXTEND, SoundCategory.BLOCKS, 0.5F, p_189539_2_.random.nextFloat() * 0.25F + 0.6F);
      } else if (p_189539_4_ == 1 || p_189539_4_ == 2) {
         if (net.minecraftforge.event.ForgeEventFactory.onPistonMovePre(p_189539_2_, p_189539_3_, direction, false)) return false;
         TileEntity tileentity1 = p_189539_2_.getBlockEntity(p_189539_3_.relative(direction));
         if (tileentity1 instanceof HoneyStickyPistonTileEntity) {
            ((HoneyStickyPistonTileEntity)tileentity1).finalTick();
         }

         BlockState blockstate = Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, direction).setValue(MovingPistonBlock.TYPE, PistonType.STICKY);
         p_189539_2_.setBlock(p_189539_3_, blockstate, 20);
         p_189539_2_.setBlockEntity(p_189539_3_, new HoneyStickyPistonTileEntity(this.defaultBlockState().setValue(FACING, Direction.from3DDataValue(p_189539_5_ & 7)), direction, false, true));
         p_189539_2_.blockUpdated(p_189539_3_, blockstate.getBlock());
         blockstate.updateNeighbourShapes(p_189539_2_, p_189539_3_, 2);
         if (this.isSticky) {
            BlockPos blockpos = p_189539_3_.offset(direction.getStepX() * 2, direction.getStepY() * 2, direction.getStepZ() * 2);
            BlockState blockstate1 = p_189539_2_.getBlockState(blockpos);
            boolean flag1 = false;
            if (blockstate1.is(Blocks.MOVING_PISTON)) {
               TileEntity tileentity = p_189539_2_.getBlockEntity(blockpos);
               if (tileentity instanceof HoneyStickyPistonTileEntity) {
                  HoneyStickyPistonTileEntity pistontileentity = (HoneyStickyPistonTileEntity)tileentity;
                  if (pistontileentity.getDirection() == direction && pistontileentity.isExtending()) {
                     pistontileentity.finalTick();
                     flag1 = true;
                  }
               }
            }

            if (!flag1) {
               if (p_189539_4_ != 1 || blockstate1.isAir() || !isPushable(blockstate1, p_189539_2_, blockpos, direction.getOpposite(), false, direction) || blockstate1.getPistonPushReaction() != PushReaction.NORMAL && !blockstate1.is(Blocks.PISTON) && !blockstate1.is(Blocks.STICKY_PISTON) && !blockstate1.is(ModBlocks.HONEY_STICKY_PISTON.get())) {
                  p_189539_2_.removeBlock(p_189539_3_.relative(direction), false);
               } else {
                  this.moveBlocks(p_189539_2_, p_189539_3_, direction, false);
               }
            }
         } else {
            p_189539_2_.removeBlock(p_189539_3_.relative(direction), false);
         }

         p_189539_2_.playSound((PlayerEntity)null, p_189539_3_, SoundEvents.PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5F, p_189539_2_.random.nextFloat() * 0.15F + 0.6F);
      }

      net.minecraftforge.event.ForgeEventFactory.onPistonMovePost(p_189539_2_, p_189539_3_, direction, (p_189539_4_ == 0));
      return true;
   }

   public static boolean isPushable(BlockState p_185646_0_, World p_185646_1_, BlockPos p_185646_2_, Direction p_185646_3_, boolean p_185646_4_, Direction p_185646_5_) {
      if (p_185646_2_.getY() >= 0 && p_185646_2_.getY() <= p_185646_1_.getMaxBuildHeight() - 1 && p_185646_1_.getWorldBorder().isWithinBounds(p_185646_2_)) {
         if (p_185646_0_.isAir()) {
            return true;
         } else if (!p_185646_0_.is(Blocks.OBSIDIAN) && !p_185646_0_.is(Blocks.CRYING_OBSIDIAN) && !p_185646_0_.is(Blocks.RESPAWN_ANCHOR)) {
            if (p_185646_3_ == Direction.DOWN && p_185646_2_.getY() == 0) {
               return false;
            } else if (p_185646_3_ == Direction.UP && p_185646_2_.getY() == p_185646_1_.getMaxBuildHeight() - 1) {
               return false;
            } else {
               if (!p_185646_0_.is(Blocks.PISTON) && !p_185646_0_.is(Blocks.STICKY_PISTON) && !p_185646_0_.is(ModBlocks.HONEY_STICKY_PISTON.get())) {
                  if (p_185646_0_.getDestroySpeed(p_185646_1_, p_185646_2_) == -1.0F) {
                     return false;
                  }

                  switch(p_185646_0_.getPistonPushReaction()) {
                  case BLOCK:
                     return false;
                  case DESTROY:
                     return p_185646_4_;
                  case PUSH_ONLY:
                     return p_185646_3_ == p_185646_5_;
                  }
               } else if (p_185646_0_.getValue(EXTENDED)) {
                  return false;
               }

               return !p_185646_0_.hasTileEntity();
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
   
   private boolean moveBlocks(World p_176319_1_, BlockPos p_176319_2_, Direction p_176319_3_, boolean p_176319_4_) {
      BlockPos blockpos = p_176319_2_.relative(p_176319_3_);
      if (!p_176319_4_ && p_176319_1_.getBlockState(blockpos).is(ModBlocks.HONEY_STICKY_PISTON_HEAD.get())) {
         p_176319_1_.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 20);
      }

      PistonBlockStructureHelper pistonblockstructurehelper = new PistonBlockStructureHelper(p_176319_1_, p_176319_2_, p_176319_3_, p_176319_4_);
      if (!pistonblockstructurehelper.resolve()) {
         return false;
      } else {
         Map<BlockPos, BlockState> map = Maps.newHashMap();
         List<BlockPos> list = pistonblockstructurehelper.getToPush();
         List<BlockState> list1 = Lists.newArrayList();

         for(int i = 0; i < list.size(); ++i) {
            BlockPos blockpos1 = list.get(i);
            BlockState blockstate = p_176319_1_.getBlockState(blockpos1);
            list1.add(blockstate);
            map.put(blockpos1, blockstate);
         }

         List<BlockPos> list2 = pistonblockstructurehelper.getToDestroy();
         BlockState[] ablockstate = new BlockState[list.size() + list2.size()];
         Direction direction = p_176319_4_ ? p_176319_3_ : p_176319_3_.getOpposite();
         int j = 0;

         for(int k = list2.size() - 1; k >= 0; --k) {
            BlockPos blockpos2 = list2.get(k);
            BlockState blockstate1 = p_176319_1_.getBlockState(blockpos2);
            TileEntity tileentity = blockstate1.hasTileEntity() ? p_176319_1_.getBlockEntity(blockpos2) : null;
            dropResources(blockstate1, p_176319_1_, blockpos2, tileentity);
            p_176319_1_.setBlock(blockpos2, Blocks.AIR.defaultBlockState(), 18);
            ablockstate[j++] = blockstate1;
         }

         for(int l = list.size() - 1; l >= 0; --l) {
            BlockPos blockpos3 = list.get(l);
            BlockState blockstate5 = p_176319_1_.getBlockState(blockpos3);
            blockpos3 = blockpos3.relative(direction);
            map.remove(blockpos3);
            p_176319_1_.setBlock(blockpos3, Blocks.MOVING_PISTON.defaultBlockState().setValue(FACING, p_176319_3_), 68);
            p_176319_1_.setBlockEntity(blockpos3, new HoneyStickyPistonTileEntity(list1.get(l), p_176319_3_, p_176319_4_, false));
            ablockstate[j++] = blockstate5;
         }

         if (p_176319_4_) {
            PistonType pistontype = PistonType.STICKY;
            BlockState blockstate4 = ModBlocks.HONEY_STICKY_PISTON_HEAD.get().defaultBlockState().setValue(HoneyStickyPistonHeadBlock.FACING, p_176319_3_).setValue(HoneyStickyPistonHeadBlock.TYPE, pistontype);
            BlockState blockstate6 = Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, p_176319_3_).setValue(MovingPistonBlock.TYPE, PistonType.STICKY);
            map.remove(blockpos);
            p_176319_1_.setBlock(blockpos, blockstate6, 68);
            p_176319_1_.setBlockEntity(blockpos, new HoneyStickyPistonTileEntity(blockstate4, p_176319_3_, true, true));
         }

         BlockState blockstate3 = Blocks.AIR.defaultBlockState();

         for(BlockPos blockpos4 : map.keySet()) {
            p_176319_1_.setBlock(blockpos4, blockstate3, 82);
         }

         for(Entry<BlockPos, BlockState> entry : map.entrySet()) {
            BlockPos blockpos5 = entry.getKey();
            BlockState blockstate2 = entry.getValue();
            blockstate2.updateIndirectNeighbourShapes(p_176319_1_, blockpos5, 2);
            blockstate3.updateNeighbourShapes(p_176319_1_, blockpos5, 2);
            blockstate3.updateIndirectNeighbourShapes(p_176319_1_, blockpos5, 2);
         }

         j = 0;

         for(int i1 = list2.size() - 1; i1 >= 0; --i1) {
            BlockState blockstate7 = ablockstate[j++];
            BlockPos blockpos6 = list2.get(i1);
            blockstate7.updateIndirectNeighbourShapes(p_176319_1_, blockpos6, 2);
            p_176319_1_.updateNeighborsAt(blockpos6, blockstate7.getBlock());
         }

         for(int j1 = list.size() - 1; j1 >= 0; --j1) {
            p_176319_1_.updateNeighborsAt(list.get(j1), ablockstate[j++].getBlock());
         }

         if (p_176319_4_) {
            p_176319_1_.updateNeighborsAt(blockpos, ModBlocks.HONEY_STICKY_PISTON_HEAD.get());
         }

         return true;
      }
   }
   
   @Override
   public PushReaction getPistonPushReaction(BlockState state) {
      if (state.getValue(EXTENDED)) {
         return PushReaction.BLOCK;
      } else {
         return PushReaction.NORMAL;
      }
   }
}
