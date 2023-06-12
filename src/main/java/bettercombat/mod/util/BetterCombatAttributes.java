package bettercombat.mod.util;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;

public class BetterCombatAttributes
{
	public static final IAttribute CRIT_CHANCE;
	public static final IAttribute CRIT_DAMAGE;

	static
	{
		CRIT_CHANCE = (IAttribute) new RangedAttribute((IAttribute) null, Reference.MOD_ID + ".critChance", ConfigurationHandler.baseCritPercentChance, 0, 100);
		CRIT_DAMAGE = (IAttribute) new RangedAttribute((IAttribute) null, Reference.MOD_ID + ".critDamage", ConfigurationHandler.baseCritPercentDamage, 0, 2560);
	}
}