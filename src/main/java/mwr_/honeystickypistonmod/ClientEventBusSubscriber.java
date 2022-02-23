package mwr_.honeystickypistonmod;

import mwr_.honeystickypistonmod.client.renderer.tileentity.HoneyStickyPistonHeadRenderer;
import mwr_.honeystickypistonmod.tileentity.ModBlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = HoneyStickyPistonMod.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class ClientEventBusSubscriber {

	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(ModBlockEntityType.HONEY_STICKY_PISTON.get(), HoneyStickyPistonHeadRenderer::new);
	}
}
