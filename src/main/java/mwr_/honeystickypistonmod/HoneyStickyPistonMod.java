package mwr_.honeystickypistonmod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mwr_.honeystickypistonmod.block.ModBlocks;
import mwr_.honeystickypistonmod.tileentity.ModBlockEntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("honeystickypistonmod")
@Mod.EventBusSubscriber(modid = HoneyStickyPistonMod.MOD_ID, bus = Bus.MOD)
public class HoneyStickyPistonMod {

	public static final Logger LOGGER = LogManager.getLogger();
	public static final String MOD_ID = "honeystickypistonmod";

	public HoneyStickyPistonMod() {

		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		
		ModBlocks.BLOCKS.register(bus);
		ModBlockEntityType.BLOCK_ENTITIES.register(bus);

		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public static void onRegisterItems(final RegistryEvent.Register<Item> event) {
			event.getRegistry().register(new BlockItem(ModBlocks.HONEY_STICKY_PISTON.get(), new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE))
					.setRegistryName(ModBlocks.HONEY_STICKY_PISTON.get().getRegistryName()));
	}
}
