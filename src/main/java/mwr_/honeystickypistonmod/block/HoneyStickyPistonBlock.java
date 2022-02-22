package mwr_.honeystickypistonmod.block;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import mwr_.honeystickypistonmod.tileentity.HoneyStickyPistonTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import net.minecraft.block.AbstractBlock.Properties;

public class HoneyStickyPistonBlock extends PistonBlock {
   private final boolean isSticky;
   
   public HoneyStickyPistonBlock(boolean sticky, Properties properties) {
      super(sticky, properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(EXTENDED, Boolean.valueOf(false)));
      this.isSticky = sticky;
	}

   @Override //Nothing edited, put in for checkIfExtend()
   public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      if (!worldIn.isClientSide) {
         this.checkIfExtend(worldIn, pos, state);
      }

   }
   
   @Override //Nothing edited, put in for checkIfExtend()
   public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      if (!worldIn.isClientSide) {
         this.checkIfExtend(worldIn, pos, state);
      }

   }
   
   @Override //Nothing edited, put in for checkIfExtend()
   public void onPlace(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
      if (!oldState.is(state.getBlock())) {
         if (!worldIn.isClientSide && worldIn.getBlockEntity(pos) == null) {
            this.checkIfExtend(worldIn, pos, state);
         }

      }
   }

   @Override //Nothing edited, may not be necessary
   public BlockState getStateForPlacement(BlockItemUseContext context) {
      return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite()).setValue(EXTENDED, Boolean.valueOf(false));
   }
   
   private void checkIfExtend(World worldIn, BlockPos pos, BlockState state) {
      Direction direction = state.getValue(FACING);
      boolean flag = this.getNeighborSignal(worldIn, pos, direction);
      if (flag && !state.getValue(EXTENDED)) {
         if ((new PistonBlockStructureHelper(worldIn, pos, direction, true)).resolve()) {
            worldIn.blockEvent(pos, this, 0, direction.get3DDataValue());
         }
      } else if (!flag && state.getValue(EXTENDED)) {
         BlockPos blockpos = pos.relative(direction, 2);
         BlockState blockstate = worldIn.getBlockState(blockpos);
         int i = 1;
         if (blockstate.is(Blocks.MOVING_PISTON) && blockstate.getValue(FACING) == direction) {
            TileEntity tileentity = worldIn.getBlockEntity(blockpos);
            if (tileentity instanceof HoneyStickyPistonTileEntity) {
               HoneyStickyPistonTileEntity pistontileentity = (HoneyStickyPistonTileEntity)tileentity;
               if (pistontileentity.isExtending() && (pistontileentity.getProgress(0.0F) < 0.5F || worldIn.getGameTime() == pistontileentity.getLastTicked() || ((ServerWorld)worldIn).isHandlingTick())) {
                  i = 2;
               }
            }
         }

         worldIn.blockEvent(pos, this, i, direction.get3DDataValue());
      }

   }

   private boolean getNeighborSignal(World worldIn, BlockPos pos, Direction facing) {
      for(Direction direction : Direction.values()) {
         if (direction != facing && worldIn.hasSignal(pos.relative(direction), direction)) {
            return true;
         }
      }

      if (worldIn.hasSignal(pos, Direction.DOWN)) {
         return true;
      } else {
         BlockPos blockpos = pos.above();

         for(Direction direction1 : Direction.values()) {
            if (direction1 != Direction.DOWN && worldIn.hasSignal(blockpos.relative(direction1), direction1)) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public boolean triggerEvent(BlockState state, World worldIn, BlockPos pos, int id, int param) {
      Direction direction = state.getValue(FACING);
      if (!worldIn.isClientSide) {
         boolean flag = this.getNeighborSignal(worldIn, pos, direction);
         if (flag && (id == 1 || id == 2)) {
            worldIn.setBlock(pos, state.setValue(EXTENDED, Boolean.valueOf(true)), 2);
            return false;
         }

         if (!flag && id == 0) {
            return false;
         }
      }

      if (id == 0) {
         if (net.minecraftforge.event.ForgeEventFactory.onPistonMovePre(worldIn, pos, direction, true)) return false;
         if (!this.moveBlocks(worldIn, pos, direction, true)) {
            return false;
         }

         worldIn.setBlock(pos, state.setValue(EXTENDED, Boolean.valueOf(true)), 67);
         worldIn.playSound((PlayerEntity)null, pos, SoundEvents.PISTON_EXTEND, SoundCategory.BLOCKS, 0.5F, worldIn.random.nextFloat() * 0.25F + 0.6F);
      } else if (id == 1 || id == 2) {
         if (net.minecraftforge.event.ForgeEventFactory.onPistonMovePre(worldIn, pos, direction, false)) return false;
         TileEntity tileentity1 = worldIn.getBlockEntity(pos.relative(direction));
         if (tileentity1 instanceof HoneyStickyPistonTileEntity) {
            ((HoneyStickyPistonTileEntity)tileentity1).finalTick();
         }

         BlockState blockstate = Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, direction).setValue(MovingPistonBlock.TYPE, PistonType.STICKY);
         worldIn.setBlock(pos, blockstate, 20);
         worldIn.setBlockEntity(pos, new HoneyStickyPistonTileEntity(this.defaultBlockState().setValue(FACING, Direction.from3DDataValue(param & 7)), direction, false, true));
         worldIn.blockUpdated(pos, blockstate.getBlock());
         blockstate.updateNeighbourShapes(worldIn, pos, 2);
         if (this.isSticky) {
            BlockPos blockpos = pos.offset(direction.getStepX() * 2, direction.getStepY() * 2, direction.getStepZ() * 2);
            BlockState blockstate1 = worldIn.getBlockState(blockpos);
            boolean flag1 = false;
            if (blockstate1.is(Blocks.MOVING_PISTON)) {
               TileEntity tileentity = worldIn.getBlockEntity(blockpos);
               if (tileentity instanceof HoneyStickyPistonTileEntity) {
                  HoneyStickyPistonTileEntity pistontileentity = (HoneyStickyPistonTileEntity)tileentity;
                  if (pistontileentity.getDirection() == direction && pistontileentity.isExtending()) {
                     pistontileentity.finalTick();
                     flag1 = true;
                  }
               }
            }

            if (!flag1) {
               if (id != 1 || blockstate1.isAir() || !isPushable(blockstate1, worldIn, blockpos, direction.getOpposite(), false, direction) || blockstate1.getPistonPushReaction() != PushReaction.NORMAL && !blockstate1.is(Blocks.PISTON) && !blockstate1.is(Blocks.STICKY_PISTON) && !blockstate1.is(ModBlocks.HONEY_STICKY_PISTON.get())) {
                  worldIn.removeBlock(pos.relative(direction), false);
               } else {
                  this.moveBlocks(worldIn, pos, direction, false);
               }
            }
         } else {
            worldIn.removeBlock(pos.relative(direction), false);
         }

         worldIn.playSound((PlayerEntity)null, pos, SoundEvents.PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5F, worldIn.random.nextFloat() * 0.15F + 0.6F);
	      }

      net.minecraftforge.event.ForgeEventFactory.onPistonMovePost(worldIn, pos, direction, (id == 0));
      return true;
   }
   //Static method weirdness? might need to remove because it's not polymorphic
   public static boolean isPushable(BlockState blockStateIn, World worldIn, BlockPos pos, Direction facing, boolean destroyBlocks, Direction direction) {
	      if (pos.getY() >= 0 && pos.getY() <= worldIn.getMaxBuildHeight() - 1 && worldIn.getWorldBorder().isWithinBounds(pos)) {
	         if (blockStateIn.isAir()) {
	            return true;
	         } else if (!blockStateIn.is(Blocks.OBSIDIAN) && !blockStateIn.is(Blocks.CRYING_OBSIDIAN) && !blockStateIn.is(Blocks.RESPAWN_ANCHOR)) {
	            if (facing == Direction.DOWN && pos.getY() == 0) {
	               return false;
	            } else if (facing == Direction.UP && pos.getY() == worldIn.getMaxBuildHeight() - 1) {
	               return false;
	            } else {
	               if (!blockStateIn.is(Blocks.PISTON) && !blockStateIn.is(Blocks.STICKY_PISTON) && !blockStateIn.is(ModBlocks.HONEY_STICKY_PISTON.get())) {
	                  if (blockStateIn.getDestroySpeed(worldIn, pos) == -1.0F) {
	                     return false;
	                  }

	                  switch(blockStateIn.getPistonPushReaction()) {
	                  case BLOCK:
	                     return false;
	                  case DESTROY:
	                     return destroyBlocks;
	                  case PUSH_ONLY:
	                     return facing == direction;
	                  }
	               } else if (blockStateIn.getValue(EXTENDED)) {
	                  return false;
	               }

	               return !blockStateIn.hasTileEntity();
	            }
	         } else {
	            return false;
	         }
	      } else {
	         return false;
	      }
	   }
   
   private boolean moveBlocks(World worldIn, BlockPos pos, Direction directionIn, boolean extending) {
      BlockPos blockpos = pos.relative(directionIn);
      if (!extending && worldIn.getBlockState(blockpos).is(ModBlocks.HONEY_STICKY_PISTON_HEAD.get())) {
         worldIn.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 20);
      }

      PistonBlockStructureHelper pistonblockstructurehelper = new PistonBlockStructureHelper(worldIn, pos, directionIn, extending);
      if (!pistonblockstructurehelper.resolve()) {
         return false;
      } else {
         Map<BlockPos, BlockState> map = Maps.newHashMap();
         List<BlockPos> list = pistonblockstructurehelper.getToPush();
         List<BlockState> list1 = Lists.newArrayList();

         for(int i = 0; i < list.size(); ++i) {
            BlockPos blockpos1 = list.get(i);
            BlockState blockstate = worldIn.getBlockState(blockpos1);
            list1.add(blockstate);
            map.put(blockpos1, blockstate);
         }

         List<BlockPos> list2 = pistonblockstructurehelper.getToDestroy();
         BlockState[] ablockstate = new BlockState[list.size() + list2.size()];
         Direction direction = extending ? directionIn : directionIn.getOpposite();
         int j = 0;

         for(int k = list2.size() - 1; k >= 0; --k) {
            BlockPos blockpos2 = list2.get(k);
            BlockState blockstate1 = worldIn.getBlockState(blockpos2);
            TileEntity tileentity = blockstate1.hasTileEntity() ? worldIn.getBlockEntity(blockpos2) : null;
            dropResources(blockstate1, worldIn, blockpos2, tileentity);
            worldIn.setBlock(blockpos2, Blocks.AIR.defaultBlockState(), 18);
            ablockstate[j++] = blockstate1;
         }

         for(int l = list.size() - 1; l >= 0; --l) {
            BlockPos blockpos3 = list.get(l);
            BlockState blockstate5 = worldIn.getBlockState(blockpos3);
            blockpos3 = blockpos3.relative(direction);
            map.remove(blockpos3);
            worldIn.setBlock(blockpos3, Blocks.MOVING_PISTON.defaultBlockState().setValue(FACING, directionIn), 68);
            worldIn.setBlockEntity(blockpos3, new HoneyStickyPistonTileEntity(list1.get(l), directionIn, extending, false));
            ablockstate[j++] = blockstate5;
         }

         if (extending) {
            PistonType pistontype = PistonType.STICKY;
            BlockState blockstate4 = ModBlocks.HONEY_STICKY_PISTON_HEAD.get().defaultBlockState().setValue(HoneyStickyPistonHeadBlock.FACING, directionIn).setValue(HoneyStickyPistonHeadBlock.TYPE, pistontype);
            BlockState blockstate6 = Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, directionIn).setValue(MovingPistonBlock.TYPE, PistonType.STICKY);
            map.remove(blockpos);
            worldIn.setBlock(blockpos, blockstate6, 68);
            worldIn.setBlockEntity(blockpos, new HoneyStickyPistonTileEntity(blockstate4, directionIn, true, true));
         }

         BlockState blockstate3 = Blocks.AIR.defaultBlockState();

         for(BlockPos blockpos4 : map.keySet()) {
            worldIn.setBlock(blockpos4, blockstate3, 82);
         }

         for(Entry<BlockPos, BlockState> entry : map.entrySet()) {
            BlockPos blockpos5 = entry.getKey();
            BlockState blockstate2 = entry.getValue();
            blockstate2.updateIndirectNeighbourShapes(worldIn, blockpos5, 2);
            blockstate3.updateNeighbourShapes(worldIn, blockpos5, 2);
            blockstate3.updateIndirectNeighbourShapes(worldIn, blockpos5, 2);
         }

         j = 0;

         for(int i1 = list2.size() - 1; i1 >= 0; --i1) {
            BlockState blockstate7 = ablockstate[j++];
            BlockPos blockpos6 = list2.get(i1);
            blockstate7.updateIndirectNeighbourShapes(worldIn, blockpos6, 2);
            worldIn.updateNeighborsAt(blockpos6, blockstate7.getBlock());
         }

         for(int j1 = list.size() - 1; j1 >= 0; --j1) {
            worldIn.updateNeighborsAt(list.get(j1), ablockstate[j++].getBlock());
         }

         if (extending) {
            worldIn.updateNeighborsAt(blockpos, ModBlocks.HONEY_STICKY_PISTON_HEAD.get());
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
