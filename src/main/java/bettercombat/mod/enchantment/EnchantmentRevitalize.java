package bettercombat.mod.enchantment;

import bettercombat.mod.util.ConfigurationHandler;
import bettercombat.mod.util.Reference;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentRevitalize extends Enchantment
{
	public EnchantmentRevitalize()
	{
		super(Rarity.COMMON, EnumEnchantmentType.WEARABLE, new EntityEquipmentSlot[]
		{
			EntityEquipmentSlot.HEAD,
			EntityEquipmentSlot.CHEST,
			EntityEquipmentSlot.LEGS,
			EntityEquipmentSlot.FEET
		});
		
		String NAME = "revitalize";
		this.setName(NAME);
		this.setRegistryName(Reference.MOD_ID, NAME);
		
		if ( ConfigurationHandler.revitalizeEnchantmentEnabled )
		{
			BetterCombatEnchantments.ENCHANTMENTS.add(this);
		}
	}

	@Override
	public int getMinEnchantability(int enchantmentLevel)
	{
		return 10 + enchantmentLevel * 5;
	}

	@Override
	public int getMaxEnchantability(int enchantmentLevel)
	{
		return this.getMinEnchantability(enchantmentLevel) + 50;
	}

	@Override
	public int getMaxLevel()
	{
		return 5;
	}
}
