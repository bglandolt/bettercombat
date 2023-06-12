package bettercombat.mod.enchantment;

import bettercombat.mod.util.ConfigurationHandler;
import bettercombat.mod.util.Reference;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentGourmand extends Enchantment
{	
	public EnchantmentGourmand()
	{
		super(Rarity.RARE, EnumEnchantmentType.ARMOR_CHEST, new EntityEquipmentSlot[]
		{
			EntityEquipmentSlot.CHEST
		});
		
		String NAME = "gourmand";
		this.setName(NAME);
		this.setRegistryName(Reference.MOD_ID, NAME);
		
		if ( ConfigurationHandler.gourmandEnchantmentEnabled )
		{
			BetterCombatEnchantments.ENCHANTMENTS.add(this);
		}
	}

	@Override
	public int getMaxLevel()
	{
		return 1;
	}

	@Override
    public int getMinEnchantability(int enchantmentLevel)
    {
        return 15;
    }

	@Override
    public int getMaxEnchantability(int enchantmentLevel)
    {
        return this.getMinEnchantability(enchantmentLevel) + 50;
    }
	
	public boolean isCurse()
    {
        return true;
    }
}