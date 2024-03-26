package bettercombat.mod.potion;

import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber
public class BetterCombatPotions
{
	public static Potion AETHEREALIZED;
	public static Potion PRECISION;
	public static Potion BRUTALITY;
	public static Potion BLEEDING;

	@SubscribeEvent
	public static void registerPotions( RegistryEvent.Register<Potion> event )
	{
		IForgeRegistry<Potion> r = event.getRegistry();
		r.register(AETHEREALIZED = new PotionAetherealized());
		r.register(PRECISION = new PotionPrecision());
		r.register(BRUTALITY = new PotionBrutality());
		r.register(BLEEDING = new PotionBleeding());
	}
}