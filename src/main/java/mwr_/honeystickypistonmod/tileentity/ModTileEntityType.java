package mwr_.honeystickypistonmod.tileentity;

import mwr_.honeystickypistonmod.HoneyStickyPistonMod;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModTileEntityType {
	public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister
			.create(ForgeRegistries.TILE_ENTITIES, HoneyStickyPistonMod.MOD_ID);

	public static final RegistryObject<TileEntityType<HoneyStickyPistonTileEntity>> HONEY_STICKY_PISTON = TILE_ENTITIES
			.register("honey_sticky_piston", () -> TileEntityType.Builder.of(HoneyStickyPistonTileEntity::new, Blocks.MOVING_PISTON).build(null));
}
