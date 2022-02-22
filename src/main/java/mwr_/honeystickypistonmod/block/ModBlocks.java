package mwr_.honeystickypistonmod.block;

import mwr_.honeystickypistonmod.HoneyStickyPistonMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
			HoneyStickyPistonMod.MOD_ID);
	
	   private static boolean isntSolid(BlockState state, IBlockReader reader, BlockPos pos) {
		      return false;
		   }

	   private static HoneyStickyPistonBlock createHoneyStickyPiston(boolean sticky) {
		      AbstractBlock.IPositionPredicate abstractblock$ipositionpredicate = (state, reader, pos) -> {
		         return !state.get(PistonBlock.EXTENDED);
		      };
		      return new HoneyStickyPistonBlock(sticky, AbstractBlock.Properties.create(Material.PISTON).hardnessAndResistance(1.5F).harvestTool(ToolType.PICKAXE).setOpaque(ModBlocks::isntSolid).setSuffocates(abstractblock$ipositionpredicate).setBlocksVision(abstractblock$ipositionpredicate));
		   }
		
		
		public static final RegistryObject<Block> HONEY_STICKY_PISTON = BLOCKS
				.register("honey_sticky_piston",
						() -> createHoneyStickyPiston(true));
		
		public static final RegistryObject<Block> HONEY_STICKY_PISTON_HEAD = BLOCKS
				.register("honey_sticky_piston_head",
						() -> new HoneyStickyPistonHeadBlock(AbstractBlock.Properties.create(Material.PISTON).hardnessAndResistance(1.5F).noDrops()));
		
}
