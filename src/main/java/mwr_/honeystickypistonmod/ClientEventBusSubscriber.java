package mwr_.honeystickypistonmod;

import mwr_.honeystickypistonmod.client.renderer.tileentity.HoneyStickyPistonTileEntityRenderer;
import mwr_.honeystickypistonmod.tileentity.ModTileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = HoneyStickyPistonMod.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class ClientEventBusSubscriber {

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		ClientRegistry.bindTileEntityRenderer(ModTileEntityType.HONEY_STICKY_PISTON.get(), HoneyStickyPistonTileEntityRenderer::new);
	}
}
