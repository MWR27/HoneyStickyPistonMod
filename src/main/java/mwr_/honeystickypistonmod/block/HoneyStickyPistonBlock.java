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

public class HoneyStickyPistonBlock extends PistonBlock {
   private final boolean isSticky;
   
   public HoneyStickyPistonBlock(boolean sticky, Properties properties) {
      super(sticky, properties);
      this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(EXTENDED, Boolean.valueOf(false)));
      this.isSticky = sticky;
   }

   @Override //Nothing edited, put in for checkForMove()
   public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
      if (!worldIn.isRemote) {
         this.checkForMove(worldIn, pos, state);
      }

   }
   
   @Override //Nothing edited, put in for checkForMove()
   public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      if (!worldIn.isRemote) {
         this.checkForMove(worldIn, pos, state);
      }

   }
   
   @Override //Nothing edited, put in for checkForMove()
   public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
      if (!oldState.isIn(state.getBlock())) {
         if (!worldIn.isRemote && worldIn.getTileEntity(pos) == null) {
            this.checkForMove(worldIn, pos, state);
         }

      }
   }

   @Override //Nothing edited, may not be necessary
   public BlockState getStateForPlacement(BlockItemUseContext context) {
      return this.getDefaultState().with(FACING, context.getNearestLookingDirection().getOpposite()).with(EXTENDED, Boolean.valueOf(false));
   }
   
   private void checkForMove(World worldIn, BlockPos pos, BlockState state) {
      Direction direction = state.get(FACING);
      boolean flag = this.shouldBeExtended(worldIn, pos, direction);
      if (flag && !state.get(EXTENDED)) {
         if ((new PistonBlockStructureHelper(worldIn, pos, direction, true)).canMove()) {
            worldIn.addBlockEvent(pos, this, 0, direction.getIndex());
         }
      } else if (!flag && state.get(EXTENDED)) {
         BlockPos blockpos = pos.offset(direction, 2);
         BlockState blockstate = worldIn.getBlockState(blockpos);
         int i = 1;
         if (blockstate.isIn(Blocks.MOVING_PISTON) && blockstate.get(FACING) == direction) {
            TileEntity tileentity = worldIn.getTileEntity(blockpos);
            if (tileentity instanceof HoneyStickyPistonTileEntity) {
               HoneyStickyPistonTileEntity pistontileentity = (HoneyStickyPistonTileEntity)tileentity;
               if (pistontileentity.isExtending() && (pistontileentity.getProgress(0.0F) < 0.5F || worldIn.getGameTime() == pistontileentity.getLastTicked() || ((ServerWorld)worldIn).isInsideTick())) {
                  i = 2;
               }
            }
         }

         worldIn.addBlockEvent(pos, this, i, direction.getIndex());
      }

   }

   private boolean shouldBeExtended(World worldIn, BlockPos pos, Direction facing) {
      for(Direction direction : Direction.values()) {
         if (direction != facing && worldIn.isSidePowered(pos.offset(direction), direction)) {
            return true;
         }
      }

      if (worldIn.isSidePowered(pos, Direction.DOWN)) {
         return true;
      } else {
         BlockPos blockpos = pos.up();

         for(Direction direction1 : Direction.values()) {
            if (direction1 != Direction.DOWN && worldIn.isSidePowered(blockpos.offset(direction1), direction1)) {
               return true;
            }
         }

         return false;
      }
   }

   @Override @Deprecated
   public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param) {
      Direction direction = state.get(FACING);
      if (!worldIn.isRemote) {
         boolean flag = this.shouldBeExtended(worldIn, pos, direction);
         if (flag && (id == 1 || id == 2)) {
            worldIn.setBlockState(pos, state.with(EXTENDED, Boolean.valueOf(true)), 2);
            return false;
         }

         if (!flag && id == 0) {
            return false;
         }
      }

      if (id == 0) {
         if (net.minecraftforge.event.ForgeEventFactory.onPistonMovePre(worldIn, pos, direction, true)) return false;
         if (!this.doMove(worldIn, pos, direction, true)) {
            return false;
         }

         worldIn.setBlockState(pos, state.with(EXTENDED, Boolean.valueOf(true)), 67);
         worldIn.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.5F, worldIn.rand.nextFloat() * 0.25F + 0.6F);
      } else if (id == 1 || id == 2) {
         if (net.minecraftforge.event.ForgeEventFactory.onPistonMovePre(worldIn, pos, direction, false)) return false;
         TileEntity tileentity1 = worldIn.getTileEntity(pos.offset(direction));
         if (tileentity1 instanceof HoneyStickyPistonTileEntity) {
            ((HoneyStickyPistonTileEntity)tileentity1).clearPistonTileEntity();
         }

         BlockState blockstate = Blocks.MOVING_PISTON.getDefaultState().with(MovingPistonBlock.FACING, direction).with(MovingPistonBlock.TYPE, PistonType.STICKY);
         worldIn.setBlockState(pos, blockstate, 20);
         worldIn.setTileEntity(pos, new HoneyStickyPistonTileEntity(this.getDefaultState().with(FACING, Direction.byIndex(param & 7)), direction, false, true));
         worldIn.func_230547_a_(pos, blockstate.getBlock());
         blockstate.updateNeighbours(worldIn, pos, 2);
         if (this.isSticky) {
            BlockPos blockpos = pos.add(direction.getXOffset() * 2, direction.getYOffset() * 2, direction.getZOffset() * 2);
            BlockState blockstate1 = worldIn.getBlockState(blockpos);
            boolean flag1 = false;
            if (blockstate1.isIn(Blocks.MOVING_PISTON)) {
               TileEntity tileentity = worldIn.getTileEntity(blockpos);
               if (tileentity instanceof HoneyStickyPistonTileEntity) {
                  HoneyStickyPistonTileEntity pistontileentity = (HoneyStickyPistonTileEntity)tileentity;
                  if (pistontileentity.getFacing() == direction && pistontileentity.isExtending()) {
                     pistontileentity.clearPistonTileEntity();
                     flag1 = true;
                  }
               }
            }

            if (!flag1) {
               if (id != 1 || blockstate1.isAir() || !canPush(blockstate1, worldIn, blockpos, direction.getOpposite(), false, direction) || blockstate1.getPushReaction() != PushReaction.NORMAL && !blockstate1.isIn(Blocks.PISTON) && !blockstate1.isIn(Blocks.STICKY_PISTON) && !blockstate1.isIn(ModBlocks.HONEY_STICKY_PISTON.get())) {
                  worldIn.removeBlock(pos.offset(direction), false);
               } else {
                  this.doMove(worldIn, pos, direction, false);
               }
            }
         } else {
            worldIn.removeBlock(pos.offset(direction), false);
         }

         worldIn.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5F, worldIn.rand.nextFloat() * 0.15F + 0.6F);
      }

      net.minecraftforge.event.ForgeEventFactory.onPistonMovePost(worldIn, pos, direction, (id == 0));
      return true;
   }

   public static boolean canPush(BlockState blockStateIn, World worldIn, BlockPos pos, Direction facing, boolean destroyBlocks, Direction direction) {
      if (pos.getY() >= 0 && pos.getY() <= worldIn.getHeight() - 1 && worldIn.getWorldBorder().contains(pos)) {
         if (blockStateIn.isAir()) {
            return true;
         } else if (!blockStateIn.isIn(Blocks.OBSIDIAN) && !blockStateIn.isIn(Blocks.CRYING_OBSIDIAN) && !blockStateIn.isIn(Blocks.RESPAWN_ANCHOR)) {
            if (facing == Direction.DOWN && pos.getY() == 0) {
               return false;
            } else if (facing == Direction.UP && pos.getY() == worldIn.getHeight() - 1) {
               return false;
            } else {
               if (!blockStateIn.isIn(Blocks.PISTON) && !blockStateIn.isIn(Blocks.STICKY_PISTON) && !blockStateIn.isIn(ModBlocks.HONEY_STICKY_PISTON.get())) {
                  if (blockStateIn.getBlockHardness(worldIn, pos) == -1.0F) {
                     return false;
                  }

                  switch(blockStateIn.getPushReaction()) {
                  case BLOCK:
                     return false;
                  case DESTROY:
                     return destroyBlocks;
                  case PUSH_ONLY:
                     return facing == direction;
                  }
               } else if (blockStateIn.get(EXTENDED)) {
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

   private boolean doMove(World worldIn, BlockPos pos, Direction directionIn, boolean extending) {
      BlockPos blockpos = pos.offset(directionIn);
      if (!extending && worldIn.getBlockState(blockpos).isIn(ModBlocks.HONEY_STICKY_PISTON_HEAD.get())) {
         worldIn.setBlockState(blockpos, Blocks.AIR.getDefaultState(), 20);
      }

      PistonBlockStructureHelper pistonblockstructurehelper = new PistonBlockStructureHelper(worldIn, pos, directionIn, extending);
      if (!pistonblockstructurehelper.canMove()) {
         return false;
      } else {
         Map<BlockPos, BlockState> map = Maps.newHashMap();
         List<BlockPos> list = pistonblockstructurehelper.getBlocksToMove();
         List<BlockState> list1 = Lists.newArrayList();

         for(int i = 0; i < list.size(); ++i) {
            BlockPos blockpos1 = list.get(i);
            BlockState blockstate = worldIn.getBlockState(blockpos1);
            list1.add(blockstate);
            map.put(blockpos1, blockstate);
         }

         List<BlockPos> list2 = pistonblockstructurehelper.getBlocksToDestroy();
         BlockState[] ablockstate = new BlockState[list.size() + list2.size()];
         Direction direction = extending ? directionIn : directionIn.getOpposite();
         int j = 0;

         for(int k = list2.size() - 1; k >= 0; --k) {
            BlockPos blockpos2 = list2.get(k);
            BlockState blockstate1 = worldIn.getBlockState(blockpos2);
            TileEntity tileentity = blockstate1.hasTileEntity() ? worldIn.getTileEntity(blockpos2) : null;
            spawnDrops(blockstate1, worldIn, blockpos2, tileentity);
            worldIn.setBlockState(blockpos2, Blocks.AIR.getDefaultState(), 18);
            ablockstate[j++] = blockstate1;
         }

         for(int l = list.size() - 1; l >= 0; --l) {
            BlockPos blockpos3 = list.get(l);
            BlockState blockstate5 = worldIn.getBlockState(blockpos3);
            blockpos3 = blockpos3.offset(direction);
            map.remove(blockpos3);
            worldIn.setBlockState(blockpos3, Blocks.MOVING_PISTON.getDefaultState().with(FACING, directionIn), 68);
            worldIn.setTileEntity(blockpos3, new HoneyStickyPistonTileEntity(list1.get(l), directionIn, extending, false));
            ablockstate[j++] = blockstate5;
         }

         if (extending) {
            PistonType pistontype = PistonType.STICKY;
            BlockState blockstate4 = ModBlocks.HONEY_STICKY_PISTON_HEAD.get().getDefaultState().with(HoneyStickyPistonHeadBlock.FACING, directionIn).with(HoneyStickyPistonHeadBlock.TYPE, pistontype);
            BlockState blockstate6 = Blocks.MOVING_PISTON.getDefaultState().with(MovingPistonBlock.FACING, directionIn).with(MovingPistonBlock.TYPE, PistonType.STICKY);
            map.remove(blockpos);
            worldIn.setBlockState(blockpos, blockstate6, 68);
            worldIn.setTileEntity(blockpos, new HoneyStickyPistonTileEntity(blockstate4, directionIn, true, true));
         }

         BlockState blockstate3 = Blocks.AIR.getDefaultState();

         for(BlockPos blockpos4 : map.keySet()) {
            worldIn.setBlockState(blockpos4, blockstate3, 82);
         }

         for(Entry<BlockPos, BlockState> entry : map.entrySet()) {
            BlockPos blockpos5 = entry.getKey();
            BlockState blockstate2 = entry.getValue();
            blockstate2.updateDiagonalNeighbors(worldIn, blockpos5, 2);
            blockstate3.updateNeighbours(worldIn, blockpos5, 2);
            blockstate3.updateDiagonalNeighbors(worldIn, blockpos5, 2);
         }

         j = 0;

         for(int i1 = list2.size() - 1; i1 >= 0; --i1) {
            BlockState blockstate7 = ablockstate[j++];
            BlockPos blockpos6 = list2.get(i1);
            blockstate7.updateDiagonalNeighbors(worldIn, blockpos6, 2);
            worldIn.notifyNeighborsOfStateChange(blockpos6, blockstate7.getBlock());
         }

         for(int j1 = list.size() - 1; j1 >= 0; --j1) {
            worldIn.notifyNeighborsOfStateChange(list.get(j1), ablockstate[j++].getBlock());
         }

         if (extending) {
            worldIn.notifyNeighborsOfStateChange(blockpos, ModBlocks.HONEY_STICKY_PISTON_HEAD.get());
         }

         return true;
      }
   }

   @Override
   public PushReaction getPushReaction(BlockState state) {
      if (state.get(EXTENDED)) {
         return PushReaction.BLOCK;
      } else {
         return PushReaction.NORMAL;
      }
   }
}
