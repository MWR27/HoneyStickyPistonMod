package mwr_.honeystickypistonmod.tileentity;

import mwr_.honeystickypistonmod.HoneyStickyPistonMod;
import mwr_.honeystickypistonmod.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntityType {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
			.create(ForgeRegistries.BLOCK_ENTITIES, HoneyStickyPistonMod.MOD_ID);

	public static final RegistryObject<BlockEntityType<HoneyStickyPistonMovingBlockEntity>> HONEY_STICKY_PISTON = BLOCK_ENTITIES
			.register("honey_sticky_piston", () -> BlockEntityType.Builder.of(HoneyStickyPistonMovingBlockEntity::new, ModBlocks.MOVING_HONEY_STICKY_PISTON.get()).build(null));
}
