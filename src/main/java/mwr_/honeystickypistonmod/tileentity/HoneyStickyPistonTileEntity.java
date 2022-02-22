package mwr_.honeystickypistonmod.tileentity;

import java.util.Iterator;
import java.util.List;

import mwr_.honeystickypistonmod.block.HoneyStickyPistonBlock;
import mwr_.honeystickypistonmod.block.HoneyStickyPistonHeadBlock;
import mwr_.honeystickypistonmod.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.PistonType;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AabbHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HoneyStickyPistonTileEntity extends TileEntity implements ITickableTileEntity {
   private BlockState movedState;
   private Direction direction;
   private boolean extending;
   private boolean isSourcePiston;
   private static final ThreadLocal<Direction> NOCLIP = ThreadLocal.withInitial(() -> {
      return null;
   });
   private float progress;
   private float progressO;
   private long lastTicked;
   private int deathTicks;

   public HoneyStickyPistonTileEntity() {
      super(ModTileEntityType.HONEY_STICKY_PISTON.get());
   }
   
   public HoneyStickyPistonTileEntity(BlockState movedStateIn, Direction directionIn, boolean extendingIn, boolean isSourcePistonIn) {
      this();
      this.movedState = movedStateIn;
      this.direction = directionIn;
      this.extending = extendingIn;
      this.isSourcePiston = isSourcePistonIn;
   }

   @Override
   public CompoundNBT getUpdateTag() {
      return this.save(new CompoundNBT());
   }

   public boolean isExtending() {
      return this.extending;
   }

   public Direction getDirection() {
      return this.direction;
   }

   public boolean isSourcePiston() {
      return this.isSourcePiston;
   }

   public float getProgress(float ticks) {
      if (ticks > 1.0F) {
         ticks = 1.0F;
      }

      return MathHelper.lerp(ticks, this.progressO, this.progress);
   }

   @OnlyIn(Dist.CLIENT)
   public float getXOff(float ticks) {
      return (float)this.direction.getStepX() * this.getExtendedProgress(this.getProgress(ticks));
   }

   @OnlyIn(Dist.CLIENT)
   public float getYOff(float ticks) {
      return (float)this.direction.getStepY() * this.getExtendedProgress(this.getProgress(ticks));
   }

   @OnlyIn(Dist.CLIENT)
   public float getZOff(float ticks) {
      return (float)this.direction.getStepZ() * this.getExtendedProgress(this.getProgress(ticks));
   }

   private float getExtendedProgress(float p_184320_1_) {
      return this.extending ? p_184320_1_ - 1.0F : 1.0F - p_184320_1_;
   }

   //Any part that refers to vanilla pistons is changed to the honey sticky piston
   private BlockState getCollisionRelatedBlockState() { //TODO
      return !this.isExtending() && this.isSourcePiston() && this.movedState.getBlock() instanceof HoneyStickyPistonBlock ? ModBlocks.HONEY_STICKY_PISTON_HEAD.get().defaultBlockState().setValue(HoneyStickyPistonHeadBlock.SHORT, Boolean.valueOf(this.progress > 0.25F)).setValue(HoneyStickyPistonHeadBlock.TYPE, PistonType.STICKY).setValue(HoneyStickyPistonHeadBlock.FACING, this.movedState.getValue(HoneyStickyPistonBlock.FACING)) : this.movedState;
   }

   private void moveCollidedEntities(float p_184322_1_) {
      Direction direction = this.getMovementDirection();
      double d0 = (double)(p_184322_1_ - this.progress);
      VoxelShape voxelshape = this.getCollisionRelatedBlockState().getCollisionShape(this.level, this.getBlockPos());
      if (!voxelshape.isEmpty()) {
         AxisAlignedBB axisalignedbb = this.moveByPositionAndProgress(voxelshape.bounds());
         List<Entity> list = this.level.getEntities((Entity)null, AabbHelper.getMovementArea(axisalignedbb, direction, d0).minmax(axisalignedbb));
         if (!list.isEmpty()) {
            List<AxisAlignedBB> list1 = voxelshape.toAabbs();
            boolean flag = this.movedState.isSlimeBlock(); //TODO: is this patch really needed the logic of the original seems sound revisit later
            Iterator iterator = list.iterator();

            while(true) {
               Entity entity;
               while(true) {
                  if (!iterator.hasNext()) {
                     return;
                  }

                  entity = (Entity)iterator.next();
                  if (entity.getPistonPushReaction() != PushReaction.IGNORE) {
                     if (!flag) {
                        break;
                     }

                     if (!(entity instanceof ServerPlayerEntity)) {
                        Vector3d vector3d = entity.getDeltaMovement();
                        double d1 = vector3d.x;
                        double d2 = vector3d.y;
                        double d3 = vector3d.z;
                        switch(direction.getAxis()) {
                        case X:
                           d1 = (double)direction.getStepX();
                           break;
                        case Y:
                           d2 = (double)direction.getStepY();
                           break;
                        case Z:
                           d3 = (double)direction.getStepZ();
                        }

                        entity.setDeltaMovement(d1, d2, d3);
                        break;
                     }
                  }
               }

               double d4 = 0.0D;

               for(AxisAlignedBB axisalignedbb2 : list1) {
                  AxisAlignedBB axisalignedbb1 = AabbHelper.getMovementArea(this.moveByPositionAndProgress(axisalignedbb2), direction, d0);
                  AxisAlignedBB axisalignedbb3 = entity.getBoundingBox();
                  if (axisalignedbb1.intersects(axisalignedbb3)) {
                     d4 = Math.max(d4, getMovement(axisalignedbb1, direction, axisalignedbb3));
                     if (d4 >= d0) {
                        break;
                     }
                  }
               }

               if (!(d4 <= 0.0D)) {
                  d4 = Math.min(d4, d0) + 0.01D;
                  moveEntityByPiston(direction, entity, d4, direction);
                  if (!this.extending && this.isSourcePiston) {
                     this.fixEntityWithinPistonBase(entity, direction, d0);
                  }
               }
            }
         }
      }
   }

   private static void moveEntityByPiston(Direction p_227022_0_, Entity p_227022_1_, double p_227022_2_, Direction p_227022_4_) {
      NOCLIP.set(p_227022_0_);
      p_227022_1_.move(MoverType.PISTON, new Vector3d(p_227022_2_ * (double)p_227022_4_.getStepX(), p_227022_2_ * (double)p_227022_4_.getStepY(), p_227022_2_ * (double)p_227022_4_.getStepZ()));
      NOCLIP.set((Direction)null);
   }

   private void moveStuckEntities(float p_227024_1_) {
      if (this.isStickyForEntities()) {
         Direction direction = this.getMovementDirection();
         if (direction.getAxis().isHorizontal()) {
            double d0 = this.movedState.getCollisionShape(this.level, this.worldPosition).max(Direction.Axis.Y);
            AxisAlignedBB axisalignedbb = this.moveByPositionAndProgress(new AxisAlignedBB(0.0D, d0, 0.0D, 1.0D, 1.5000000999999998D, 1.0D));
            double d1 = (double)(p_227024_1_ - this.progress);

            for(Entity entity : this.level.getEntities((Entity)null, axisalignedbb, (p_227023_1_) -> {
               return matchesStickyCritera(axisalignedbb, p_227023_1_);
            })) {
               moveEntityByPiston(direction, entity, d1, direction);
            }

         }
      }
   }

   private static boolean matchesStickyCritera(AxisAlignedBB p_227021_0_, Entity p_227021_1_) {
      return p_227021_1_.getPistonPushReaction() == PushReaction.NORMAL && p_227021_1_.isOnGround() && p_227021_1_.getX() >= p_227021_0_.minX && p_227021_1_.getX() <= p_227021_0_.maxX && p_227021_1_.getZ() >= p_227021_0_.minZ && p_227021_1_.getZ() <= p_227021_0_.maxZ;
   }

   private boolean isStickyForEntities() {
      return this.movedState.is(Blocks.HONEY_BLOCK);
   }

   public Direction getMovementDirection() {
      return this.extending ? this.direction : this.direction.getOpposite();
   }

   private static double getMovement(AxisAlignedBB p_190612_0_, Direction p_190612_1_, AxisAlignedBB facing) {
      switch(p_190612_1_) {
      case EAST:
         return p_190612_0_.maxX - facing.minX;
      case WEST:
         return facing.maxX - p_190612_0_.minX;
      case UP:
      default:
         return p_190612_0_.maxY - facing.minY;
      case DOWN:
         return facing.maxY - p_190612_0_.minY;
      case SOUTH:
         return p_190612_0_.maxZ - facing.minZ;
      case NORTH:
         return facing.maxZ - p_190612_0_.minZ;
      }
   }

   private AxisAlignedBB moveByPositionAndProgress(AxisAlignedBB p_190607_1_) {
      double d0 = (double)this.getExtendedProgress(this.progress);
      return p_190607_1_.move((double)this.worldPosition.getX() + d0 * (double)this.direction.getStepX(), (double)this.worldPosition.getY() + d0 * (double)this.direction.getStepY(), (double)this.worldPosition.getZ() + d0 * (double)this.direction.getStepZ());
   }

   private void fixEntityWithinPistonBase(Entity p_190605_1_, Direction p_190605_2_, double p_190605_3_) {
      AxisAlignedBB axisalignedbb = p_190605_1_.getBoundingBox();
      AxisAlignedBB axisalignedbb1 = VoxelShapes.block().bounds().move(this.worldPosition);
      if (axisalignedbb.intersects(axisalignedbb1)) {
         Direction direction = p_190605_2_.getOpposite();
         double d0 = getMovement(axisalignedbb1, direction, axisalignedbb) + 0.01D;
         double d1 = getMovement(axisalignedbb1, direction, axisalignedbb.intersect(axisalignedbb1)) + 0.01D;
         if (Math.abs(d0 - d1) < 0.01D) {
            d0 = Math.min(d0, p_190605_3_) + 0.01D;
            moveEntityByPiston(p_190605_2_, p_190605_1_, d0, direction);
         }
      }

   }

   public BlockState getMovedState() {
      return this.movedState;
   }

   public void finalTick() {
      if (this.level != null && (this.progressO < 1.0F || this.level.isClientSide)) {
         this.progress = 1.0F;
         this.progressO = this.progress;
         this.level.removeBlockEntity(this.worldPosition);
         this.setRemoved();
         if (this.level.getBlockState(this.worldPosition).is(Blocks.MOVING_PISTON)) {
            BlockState blockstate;
            if (this.isSourcePiston) {
               blockstate = Blocks.AIR.defaultBlockState();
            } else {
               blockstate = Block.updateFromNeighbourShapes(this.movedState, this.level, this.worldPosition);
            }

            this.level.setBlock(this.worldPosition, blockstate, 3);
            this.level.neighborChanged(this.worldPosition, blockstate.getBlock(), this.worldPosition);
         }
      }

   }

   public void tick() {
      this.lastTicked = this.level.getGameTime();
      this.progressO = this.progress;
      if (this.progressO >= 1.0F) {
         if (this.level.isClientSide && this.deathTicks < 5) {
            ++this.deathTicks;
         } else {
            this.level.removeBlockEntity(this.worldPosition);
            this.setRemoved();
            if (this.movedState != null && this.level.getBlockState(this.worldPosition).is(Blocks.MOVING_PISTON)) {
               BlockState blockstate = Block.updateFromNeighbourShapes(this.movedState, this.level, this.worldPosition);
               if (blockstate.isAir()) {
                  this.level.setBlock(this.worldPosition, this.movedState, 84);
                  Block.updateOrDestroy(this.movedState, blockstate, this.level, this.worldPosition, 3);
               } else {
                  if (blockstate.hasProperty(BlockStateProperties.WATERLOGGED) && blockstate.getValue(BlockStateProperties.WATERLOGGED)) {
                     blockstate = blockstate.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false));
                  }

                  this.level.setBlock(this.worldPosition, blockstate, 67);
                  this.level.neighborChanged(this.worldPosition, blockstate.getBlock(), this.worldPosition);
               }
            }

         }
      } else {
         float f = this.progress + 0.5F;
         this.moveCollidedEntities(f);
         this.moveStuckEntities(f);
         this.progress = f;
         if (this.progress >= 1.0F) {
            this.progress = 1.0F;
         }

      }
   }

   @Override
   public void load(BlockState state, CompoundNBT nbt) {
      super.load(state, nbt);
      this.movedState = NBTUtil.readBlockState(nbt.getCompound("blockState"));
      this.direction = Direction.from3DDataValue(nbt.getInt("facing"));
      this.progress = nbt.getFloat("progress");
      this.progressO = this.progress;
      this.extending = nbt.getBoolean("extending");
      this.isSourcePiston = nbt.getBoolean("source");
   }

   @Override
   public CompoundNBT save(CompoundNBT compound) {
      super.save(compound);
      compound.put("blockState", NBTUtil.writeBlockState(this.movedState));
      compound.putInt("facing", this.direction.get3DDataValue());
      compound.putFloat("progress", this.progressO);
      compound.putBoolean("extending", this.extending);
      compound.putBoolean("source", this.isSourcePiston);
      return compound;
   }

   public VoxelShape getCollisionShape(IBlockReader p_195508_1_, BlockPos p_195508_2_) {
      VoxelShape voxelshape;
      if (!this.extending && this.isSourcePiston) {
         voxelshape = this.movedState.setValue(HoneyStickyPistonBlock.EXTENDED, Boolean.valueOf(true)).getCollisionShape(p_195508_1_, p_195508_2_);
      } else {
         voxelshape = VoxelShapes.empty();
      }

      Direction direction = NOCLIP.get();
      if ((double)this.progress < 1.0D && direction == this.getMovementDirection()) {
         return voxelshape;
      } else {
         BlockState blockstate;
         if (this.isSourcePiston()) {
            blockstate = ModBlocks.HONEY_STICKY_PISTON_HEAD.get().defaultBlockState().setValue(HoneyStickyPistonHeadBlock.FACING, this.direction).setValue(HoneyStickyPistonHeadBlock.SHORT, Boolean.valueOf(this.extending != 1.0F - this.progress < 0.25F));
         } else {
            blockstate = this.movedState;
         }

         float f = this.getExtendedProgress(this.progress);
         double d0 = (double)((float)this.direction.getStepX() * f);
         double d1 = (double)((float)this.direction.getStepY() * f);
         double d2 = (double)((float)this.direction.getStepZ() * f);
         return VoxelShapes.or(voxelshape, blockstate.getCollisionShape(p_195508_1_, p_195508_2_).move(d0, d1, d2));
      }
   }

   public long getLastTicked() {
      return this.lastTicked;
   }

   @OnlyIn(Dist.CLIENT) @Override
   public double getViewDistance() {
      return 68.0D;
   }
}
