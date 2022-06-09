package mwr_.honeystickypistonmod.block;

import java.util.Arrays;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.PistonType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class HoneyStickyPistonHeadBlock extends PistonHeadBlock{
   private static final VoxelShape[] EXTENDED_SHAPES = getShapesForExtension(true);
   private static final VoxelShape[] UNEXTENDED_SHAPES = getShapesForExtension(false);

   private static VoxelShape[] getShapesForExtension(boolean extended) {
      return Arrays.stream(Direction.values()).map((direction) -> {
         return getShapeForDirection(direction, extended);
      }).toArray((id) -> {
         return new VoxelShape[id];
      });
   }

   private static VoxelShape getShapeForDirection(Direction direction, boolean shortArm) {
      switch(direction) {
      case DOWN:
      default:
         return VoxelShapes.or(PISTON_EXTENSION_DOWN_AABB, shortArm ? SHORT_DOWN_ARM_AABB : DOWN_ARM_AABB);
      case UP:
         return VoxelShapes.or(PISTON_EXTENSION_UP_AABB, shortArm ? SHORT_UP_ARM_AABB : UP_ARM_AABB);
      case NORTH:
         return VoxelShapes.or(PISTON_EXTENSION_NORTH_AABB, shortArm ? SHORT_NORTH_ARM_AABB : NORTH_ARM_AABB);
      case SOUTH:
         return VoxelShapes.or(PISTON_EXTENSION_SOUTH_AABB, shortArm ? SHORT_SOUTH_ARM_AABB : SOUTH_ARM_AABB);
      case WEST:
         return VoxelShapes.or(PISTON_EXTENSION_WEST_AABB, shortArm ? SHORT_WEST_ARM_AABB : WEST_ARM_AABB);
      case EAST:
         return VoxelShapes.or(PISTON_EXTENSION_EAST_AABB, shortArm ? SHORT_EAST_ARM_AABB : EAST_ARM_AABB);
      }
   }

   public HoneyStickyPistonHeadBlock(AbstractBlock.Properties properties) {
      super(properties);
      this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(TYPE, PistonType.STICKY).with(SHORT, Boolean.valueOf(false)));
   }

   @Override //Nothing changed, yet necessary
   public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
      return (state.get(SHORT) ? EXTENDED_SHAPES : UNEXTENDED_SHAPES)[state.get(FACING).ordinal()];
   }

   private boolean isExtended(BlockState baseState, BlockState extendedState) { 
      return extendedState.isIn(ModBlocks.HONEY_STICKY_PISTON.get()) && extendedState.get(HoneyStickyPistonBlock.EXTENDED) && extendedState.get(FACING) == baseState.get(FACING);
   }

   @Override //Nothing changed, necessary for piston not to drop in creative mode
   public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
      if (!worldIn.isRemote && player.abilities.isCreativeMode) {
         BlockPos blockpos = pos.offset(state.get(FACING).getOpposite());
         if (this.isExtended(state, worldIn.getBlockState(blockpos))) {
            worldIn.destroyBlock(blockpos, false);
         }
      }

      super.onBlockHarvested(worldIn, pos, state, player);
   }

   @Override //Nothing changed, yet somehow necessary for piston base to break
   public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
      if (!state.isIn(newState.getBlock())) {
         super.onReplaced(state, worldIn, pos, newState, isMoving);
         BlockPos blockpos = pos.offset(state.get(FACING).getOpposite());
         if (this.isExtended(state, worldIn.getBlockState(blockpos))) {
            worldIn.destroyBlock(blockpos, true);
         }

      }
   }

   @Override  //Nothing changed, yet necessary
   public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
      BlockState blockstate = worldIn.getBlockState(pos.offset(state.get(FACING).getOpposite()));
      return this.isExtended(state, blockstate) || blockstate.isIn(Blocks.MOVING_PISTON) && blockstate.get(FACING) == state.get(FACING);
   }

   @Override
   public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
      return new ItemStack(ModBlocks.HONEY_STICKY_PISTON.get());
   }
}
