package bettercombat.mod.enchantment;

import java.util.ArrayList;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import bettercombat.mod.util.Reference;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber( modid = Reference.MOD_ID )
public class BetterCombatEnchantments
{
	public static final ArrayList<Enchantment> ENCHANTMENTS = new ArrayList<Enchantment>();

	public static final EnumEnchantmentType THROWING_WEAPON = addEnchantment("throwing", item -> item instanceof ItemBow || item.getClass().getSuperclass().getSimpleName().equals("ItemThrowingWeapon"));

	@Nonnull
	public static EnumEnchantmentType addEnchantment( String name, Predicate<Item> condition )
	{
		return EnumHelper.addEnchantmentType(name, condition::test);
	}
	
	public static final Enchantment GOURMAND = new EnchantmentGourmand();
	public static final Enchantment LIGHTNING = new EnchantmentLightning();
	public static final Enchantment REVITALIZE = new EnchantmentRevitalize();
	public static final Enchantment SORCERY = new EnchantmentSorcery();
	public static final Enchantment WEBBING = new EnchantmentWebbing();

	@SubscribeEvent
	public static void registerEnchantments( RegistryEvent.Register<Enchantment> event )
	{
		event.getRegistry().registerAll(ENCHANTMENTS.toArray(new Enchantment[0]));
	}
}