package mwr_.honeystickypistonmod.item;

import mwr_.honeystickypistonmod.HoneyStickyPistonMod;
import mwr_.honeystickypistonmod.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
   public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HoneyStickyPistonMod.MOD_ID);

   public static final RegistryObject<Item> HONEY_STICKY_PISTON = ITEMS.register("honey_sticky_piston", () -> new BlockItem(ModBlocks.HONEY_STICKY_PISTON.get(), (new Item.Properties()).tab(CreativeModeTab.TAB_REDSTONE)));
}
