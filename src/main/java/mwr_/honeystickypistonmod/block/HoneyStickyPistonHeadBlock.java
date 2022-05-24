package mwr_.honeystickypistonmod.block;

import java.util.Arrays;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HoneyStickyPistonHeadBlock extends PistonHeadBlock {
   private static final VoxelShape[] SHAPES_SHORT = makeShapes(true);
   private static final VoxelShape[] SHAPES_LONG = makeShapes(false);

   private static VoxelShape[] makeShapes(boolean p_60313_) {
      return Arrays.stream(Direction.values()).map((p_60316_) -> {
         return calculateShape(p_60316_, p_60313_);
      }).toArray((p_60318_) -> {
         return new VoxelShape[p_60318_];
      });
   }

   private static VoxelShape calculateShape(Direction p_60310_, boolean p_60311_) {
      switch(p_60310_) {
      case DOWN:
      default:
         return Shapes.or(DOWN_AABB, p_60311_ ? SHORT_DOWN_ARM_AABB : DOWN_ARM_AABB);
      case UP:
         return Shapes.or(UP_AABB, p_60311_ ? SHORT_UP_ARM_AABB : UP_ARM_AABB);
      case NORTH:
         return Shapes.or(NORTH_AABB, p_60311_ ? SHORT_NORTH_ARM_AABB : NORTH_ARM_AABB);
      case SOUTH:
         return Shapes.or(SOUTH_AABB, p_60311_ ? SHORT_SOUTH_ARM_AABB : SOUTH_ARM_AABB);
      case WEST:
         return Shapes.or(WEST_AABB, p_60311_ ? SHORT_WEST_ARM_AABB : WEST_ARM_AABB);
      case EAST:
         return Shapes.or(EAST_AABB, p_60311_ ? SHORT_EAST_ARM_AABB : EAST_ARM_AABB);
      }
   }

   public HoneyStickyPistonHeadBlock(BlockBehaviour.Properties p_60259_) {
      super(p_60259_);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, PistonType.STICKY).setValue(SHORT, Boolean.valueOf(false))); //sticky?
   }

   public VoxelShape getShape(BlockState p_60320_, BlockGetter p_60321_, BlockPos p_60322_, CollisionContext p_60323_) {
      return (p_60320_.getValue(SHORT) ? SHAPES_SHORT : SHAPES_LONG)[p_60320_.getValue(FACING).ordinal()];
   }
   
   private boolean isFittingBase(BlockState p_60298_, BlockState p_60299_) {
      Block block = ModBlocks.HONEY_STICKY_PISTON.get();
      return p_60299_.is(block) && p_60299_.getValue(HoneyStickyPistonBaseBlock.EXTENDED) && p_60299_.getValue(FACING) == p_60298_.getValue(FACING);
   }
   
   public void playerWillDestroy(Level p_60265_, BlockPos p_60266_, BlockState p_60267_, Player p_60268_) {
      if (!p_60265_.isClientSide && p_60268_.getAbilities().instabuild) {
         BlockPos blockpos = p_60266_.relative(p_60267_.getValue(FACING).getOpposite());
         if (this.isFittingBase(p_60267_, p_60265_.getBlockState(blockpos))) {
            p_60265_.destroyBlock(blockpos, false);
         }
      }

      super.playerWillDestroy(p_60265_, p_60266_, p_60267_, p_60268_);
   }

   public void onRemove(BlockState p_60282_, Level p_60283_, BlockPos p_60284_, BlockState p_60285_, boolean p_60286_) {
      if (!p_60282_.is(p_60285_.getBlock())) {
         super.onRemove(p_60282_, p_60283_, p_60284_, p_60285_, p_60286_);
         BlockPos blockpos = p_60284_.relative(p_60282_.getValue(FACING).getOpposite());
         if (this.isFittingBase(p_60282_, p_60283_.getBlockState(blockpos))) {
            p_60283_.destroyBlock(blockpos, true);
         }

      }
   }

   public boolean canSurvive(BlockState p_60288_, LevelReader p_60289_, BlockPos p_60290_) {
      BlockState blockstate = p_60289_.getBlockState(p_60290_.relative(p_60288_.getValue(FACING).getOpposite()));
      return this.isFittingBase(p_60288_, blockstate) || blockstate.is(ModBlocks.MOVING_HONEY_STICKY_PISTON.get()) && blockstate.getValue(FACING) == p_60288_.getValue(FACING);
   }
   
   public ItemStack getCloneItemStack(BlockGetter worldIn, BlockPos pos, BlockState state) {
      return new ItemStack(ModBlocks.HONEY_STICKY_PISTON.get());
   }
}
