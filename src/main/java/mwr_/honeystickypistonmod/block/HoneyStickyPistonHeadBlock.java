package mwr_.honeystickypistonmod.block;

import java.util.Arrays;
import net.minecraft.block.BlockState;
import net.minecraft.block.AbstractBlock;
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

public class HoneyStickyPistonHeadBlock extends PistonHeadBlock {
   private static final VoxelShape[] SHAPES_SHORT = makeShapes(true);
   private static final VoxelShape[] SHAPES_LONG = makeShapes(false);

   private static VoxelShape[] makeShapes(boolean p_242694_0_) {
      return Arrays.stream(Direction.values()).map((p_242695_1_) -> {
         return calculateShape(p_242695_1_, p_242694_0_);
      }).toArray((p_242696_0_) -> {
         return new VoxelShape[p_242696_0_];
      });
   }

   private static VoxelShape calculateShape(Direction p_242693_0_, boolean p_242693_1_) {
      switch(p_242693_0_) {
      case DOWN:
      default:
         return VoxelShapes.or(DOWN_AABB, p_242693_1_ ? SHORT_DOWN_ARM_AABB : DOWN_ARM_AABB);
      case UP:
         return VoxelShapes.or(UP_AABB, p_242693_1_ ? SHORT_UP_ARM_AABB : UP_ARM_AABB);
      case NORTH:
         return VoxelShapes.or(NORTH_AABB, p_242693_1_ ? SHORT_NORTH_ARM_AABB : NORTH_ARM_AABB);
      case SOUTH:
         return VoxelShapes.or(SOUTH_AABB, p_242693_1_ ? SHORT_SOUTH_ARM_AABB : SOUTH_ARM_AABB);
      case WEST:
         return VoxelShapes.or(WEST_AABB, p_242693_1_ ? SHORT_WEST_ARM_AABB : WEST_ARM_AABB);
      case EAST:
         return VoxelShapes.or(EAST_AABB, p_242693_1_ ? SHORT_EAST_ARM_AABB : EAST_ARM_AABB);
      }
   }

   public HoneyStickyPistonHeadBlock(AbstractBlock.Properties p_i48280_1_) {
      super(p_i48280_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, PistonType.STICKY).setValue(SHORT, Boolean.valueOf(false)));
   }

   @Override //Nothing changed, yet necessary
   public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
      return (p_220053_1_.getValue(SHORT) ? SHAPES_SHORT : SHAPES_LONG)[p_220053_1_.getValue(FACING).ordinal()];
   }

   private boolean isFittingBase(BlockState p_235682_1_, BlockState p_235682_2_) { 
      return p_235682_2_.is(ModBlocks.HONEY_STICKY_PISTON.get()) && p_235682_2_.getValue(HoneyStickyPistonBlock.EXTENDED) && p_235682_2_.getValue(FACING) == p_235682_1_.getValue(FACING);
   }

   @Override //Nothing changed, necessary for piston not to drop in creative mode
   public void playerWillDestroy(World p_176208_1_, BlockPos p_176208_2_, BlockState p_176208_3_, PlayerEntity p_176208_4_) {
      if (!p_176208_1_.isClientSide && p_176208_4_.abilities.instabuild) {
         BlockPos blockpos = p_176208_2_.relative(p_176208_3_.getValue(FACING).getOpposite());
         if (this.isFittingBase(p_176208_3_, p_176208_1_.getBlockState(blockpos))) {
            p_176208_1_.destroyBlock(blockpos, false);
         }
      }

      super.playerWillDestroy(p_176208_1_, p_176208_2_, p_176208_3_, p_176208_4_);
   }

   @Override //Nothing changed, yet somehow necessary for piston base to break
   public void onRemove(BlockState p_196243_1_, World p_196243_2_, BlockPos p_196243_3_, BlockState p_196243_4_, boolean p_196243_5_) {
      if (!p_196243_1_.is(p_196243_4_.getBlock())) {
         super.onRemove(p_196243_1_, p_196243_2_, p_196243_3_, p_196243_4_, p_196243_5_);
         BlockPos blockpos = p_196243_3_.relative(p_196243_1_.getValue(FACING).getOpposite());
         if (this.isFittingBase(p_196243_1_, p_196243_2_.getBlockState(blockpos))) {
            p_196243_2_.destroyBlock(blockpos, true);
         }

      }
   }

   @Override  //Nothing changed, yet necessary
   public boolean canSurvive(BlockState p_196260_1_, IWorldReader p_196260_2_, BlockPos p_196260_3_) {
      BlockState blockstate = p_196260_2_.getBlockState(p_196260_3_.relative(p_196260_1_.getValue(FACING).getOpposite()));
      return this.isFittingBase(p_196260_1_, blockstate) || blockstate.is(Blocks.MOVING_PISTON) && blockstate.getValue(FACING) == p_196260_1_.getValue(FACING);
   }

   @Override
   public ItemStack getCloneItemStack(IBlockReader worldIn, BlockPos pos, BlockState state) {
      return new ItemStack(ModBlocks.HONEY_STICKY_PISTON.get());
   }
}
