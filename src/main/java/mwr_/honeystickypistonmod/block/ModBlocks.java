package mwr_.honeystickypistonmod.block;

import mwr_.honeystickypistonmod.HoneyStickyPistonMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {
   public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HoneyStickyPistonMod.MOD_ID);

   public static final RegistryObject<Block> HONEY_STICKY_PISTON = BLOCKS.register("honey_sticky_piston", () -> honeyStickyPistonBase(true));
   public static final RegistryObject<Block> HONEY_STICKY_PISTON_HEAD = BLOCKS.register("honey_sticky_piston_head", () -> new HoneyStickyPistonHeadBlock(AbstractBlock.Properties.of(Material.PISTON).strength(1.5F).noDrops()));
   
   private static boolean never(BlockState p_235436_0_, IBlockReader p_235436_1_, BlockPos p_235436_2_) {
      return false;
   }

   private static HoneyStickyPistonBlock honeyStickyPistonBase(boolean p_235432_0_) {
      AbstractBlock.IPositionPredicate abstractblock$ipositionpredicate = (p_235440_0_, p_235440_1_, p_235440_2_) -> {
         return !p_235440_0_.getValue(HoneyStickyPistonBlock.EXTENDED);
      };
      return new HoneyStickyPistonBlock(p_235432_0_, AbstractBlock.Properties.of(Material.PISTON).strength(1.5F).harvestTool(ToolType.PICKAXE).isRedstoneConductor(ModBlocks::never).isSuffocating(abstractblock$ipositionpredicate).isViewBlocking(abstractblock$ipositionpredicate));
   }	
}
