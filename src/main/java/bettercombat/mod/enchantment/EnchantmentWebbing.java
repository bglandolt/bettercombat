package bettercombat.mod.enchantment;

import bettercombat.mod.util.ConfigurationHandler;
import bettercombat.mod.util.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentArrowKnockback;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.IShearable;

public class EnchantmentWebbing extends Enchantment
{
	public EnchantmentWebbing()
	{
		super(Rarity.VERY_RARE, BetterCombatEnchantments.THROWING_WEAPON, new EntityEquipmentSlot[]
		{
			EntityEquipmentSlot.MAINHAND,
			EntityEquipmentSlot.OFFHAND
		});
		String NAME = "webbing";
		this.setName(NAME);
		this.setRegistryName(Reference.MOD_ID, NAME);
		
		// TODO add tileentity to remove webs
		
		if ( ConfigurationHandler.webbingEnchantmentEnabled )
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
    public boolean canApplyTogether(Enchantment ench)
    {
        if (ench instanceof EnchantmentArrowKnockback)
        {
            return false;
        }
        else
        {
            return super.canApplyTogether(ench);
        }
    }

	@Override
	public int getMinEnchantability( int level )
	{
		return 25;
	}

	public static void doWebbing( Entity attacker, Entity victim )
	{
		BlockPos pos = victim.getPosition();
		
		if ( pos == null )
		{
			return;
		}
		
		Block block = victim.world.getBlockState(pos).getBlock();
		
		if ( block instanceof IPlantable || block instanceof IShearable )
		{
			victim.world.setBlockToAir(pos);

			do
			{
				pos = pos.up();
				block = attacker.world.getBlockState(pos).getBlock();
				
				if ( pos != null && block instanceof IPlantable || block instanceof IShearable )
				{
					victim.world.setBlockToAir(pos);
				}
				else
				{
					break;
				}
			}
			while ( true );
			
			victim.world.setBlockToAir(pos);
		}
		
		if ( block instanceof BlockAir )
		{
			victim.world.setBlockState(pos, Blocks.WEB.getDefaultState());
		}
	}

}