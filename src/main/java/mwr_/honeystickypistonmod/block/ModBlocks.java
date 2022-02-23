package mwr_.honeystickypistonmod.block;

import mwr_.honeystickypistonmod.HoneyStickyPistonMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
			HoneyStickyPistonMod.MOD_ID);
	
	   private static boolean never(BlockState p_50806_, BlockGetter p_50807_, BlockPos p_50808_) {
		   return false;
	   }

	   private static HoneyStickyPistonBaseBlock honeyStickyPistonBase(boolean p_50799_) {
		      BlockBehaviour.StatePredicate blockbehaviour$statepredicate = (p_152641_, p_152642_, p_152643_) -> {
		         return !p_152641_.getValue(HoneyStickyPistonBaseBlock.EXTENDED);
		      };
		      return new HoneyStickyPistonBaseBlock(p_50799_, BlockBehaviour.Properties.of(Material.PISTON).strength(1.5F).isRedstoneConductor(ModBlocks::never).isSuffocating(blockbehaviour$statepredicate).isViewBlocking(blockbehaviour$statepredicate));
		   }
		
		
		public static final RegistryObject<Block> HONEY_STICKY_PISTON = BLOCKS
				.register("honey_sticky_piston",
						() -> honeyStickyPistonBase(true));
		
		public static final RegistryObject<Block> HONEY_STICKY_PISTON_HEAD = BLOCKS
				.register("honey_sticky_piston_head",
						() -> new HoneyStickyPistonHeadBlock(BlockBehaviour.Properties.of(Material.PISTON).strength(1.5F).noDrops()));
		
		public static final RegistryObject<Block> MOVING_HONEY_STICKY_PISTON = BLOCKS
				.register("moving_piston",
						() -> new MovingHoneyStickyPistonBlock(BlockBehaviour.Properties.of(Material.PISTON).strength(-1.0F).dynamicShape().noDrops().noOcclusion().isRedstoneConductor(ModBlocks::never).isSuffocating(ModBlocks::never).isViewBlocking(ModBlocks::never)));

}
