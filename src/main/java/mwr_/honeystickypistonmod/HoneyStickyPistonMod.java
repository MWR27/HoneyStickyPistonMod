package mwr_.honeystickypistonmod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mwr_.honeystickypistonmod.block.ModBlocks;
import mwr_.honeystickypistonmod.item.ModItems;
import mwr_.honeystickypistonmod.tileentity.ModBlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
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
		ModItems.ITEMS.register(bus);
		ModBlockEntityType.BLOCK_ENTITY_TYPES.register(bus);

		MinecraftForge.EVENT_BUS.register(this);
	}
}
