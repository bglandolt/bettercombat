package bettercombat.mod.enchantment;

import java.util.List;

import bettercombat.mod.util.ConfigurationHandler;
import bettercombat.mod.util.Reference;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;

public class EnchantmentLightning extends Enchantment
{
	public EnchantmentLightning()
	{
		super(Rarity.VERY_RARE, BetterCombatEnchantments.THROWING_WEAPON, new EntityEquipmentSlot[]
		{
			EntityEquipmentSlot.MAINHAND, EntityEquipmentSlot.OFFHAND
		});
		String NAME = "lightning";
		this.setName(NAME);
		this.setRegistryName(Reference.MOD_ID, NAME);
		
		if ( ConfigurationHandler.lightningEnchantmentEnabled )
		{
			BetterCombatEnchantments.ENCHANTMENTS.add(this);
		}
	}
	
	@Override
	public int getMaxLevel()
	{
		return 5;
	}

	@Override
    public int getMinEnchantability(int enchantmentLevel)
    {
        return 20 + enchantmentLevel * 2;
    }

	@Override
    public int getMaxEnchantability(int enchantmentLevel)
    {
        return this.getMinEnchantability(enchantmentLevel) + 50;
    }

	public static boolean canActuallySeeSky( Entity victim )
	{
		try
		{
			for ( int i = 0; i < 127; i++ )
			{
				if ( i > 16 )
				{
					i += 16;
				}

				IBlockState iblockstate = victim.world.getBlockState(victim.getPosition().up(i));

				boolean flag = false;

				if ( iblockstate.getBlock() instanceof BlockAir || iblockstate.getBlock() instanceof BlockLiquid )
				{
					flag = true;
				}
				else
				{
					if ( flag )
					{
						return false;
					}
				}
			}
		}
		catch (Exception e)
		{
			return false;
		}

		return true;
	}

	public static void doLightning( Entity attacker, Entity victim, int lvl )
	{
		if ( lvl > 0 )
		{
			if ( canActuallySeeSky(victim) )
			{
				for ( int i = 0; i < lvl; i++ )
				{
					EntityLightningBolt bolt = new EntityLightningBolt(victim.world, victim.posX + (victim.world.rand.nextBoolean() ? victim.world.rand.nextInt(lvl) * 0.5F : -victim.world.rand.nextInt(lvl) * 0.5F), victim.posY, victim.posZ + (victim.world.rand.nextBoolean() ? victim.world.rand.nextInt(lvl) * 0.5F : -victim.world.rand.nextInt(lvl) * 0.5F), true);

					bolt.lightningState = 1;
					
					victim.world.addWeatherEffect(bolt);
				}

				victim.setFire(1);

				float a = 0.8F + victim.world.rand.nextFloat() * 0.2F;
				float b = 0.8F + victim.world.rand.nextFloat() * 0.2F;

				victim.world.playSound((EntityPlayer) attacker, victim.posX, victim.posY, victim.posZ, SoundEvents.ENTITY_LIGHTNING_IMPACT, SoundCategory.HOSTILE, 1.0F, a);
				victim.world.playSound((EntityPlayer) attacker, victim.posX, victim.posY, victim.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.HOSTILE, 1.0F, b);

				if ( attacker instanceof EntityPlayer )
				{
					attacker.world.playSound(null, attacker.posX, attacker.posY, attacker.posZ, SoundEvents.ENTITY_LIGHTNING_IMPACT, SoundCategory.HOSTILE, 1.0F, a);
					attacker.world.playSound(null, attacker.posX, attacker.posY, attacker.posZ, SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.HOSTILE, 1.0F, b);
				}

				float r = 0.4F + lvl * 0.4F;

				List<EntityLivingBase> livingList = victim.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(victim.getPosition()).grow(r, r, r));

				for ( EntityLivingBase elb : livingList )
				{
					if ( victim != elb )
					{
						elb.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 13, 1, true, false));
						elb.hurtResistantTime = 0;
						elb.attackEntityFrom(DamageSource.causeIndirectMagicDamage(attacker, attacker), victim.isWet() ? (ConfigurationHandler.lightningEnchantmentBaseDamage + lvl * ConfigurationHandler.lightningEnchantmentDamagePerLevel) * ConfigurationHandler.lightningEnchantmentWetModifier : (ConfigurationHandler.lightningEnchantmentBaseDamage + lvl * ConfigurationHandler.lightningEnchantmentDamagePerLevel));
						elb.hurtResistantTime = 0;
					}
				}

				if ( victim instanceof EntityLivingBase )
				{
					victim.hurtResistantTime = 0;
					((EntityLivingBase) victim).addPotionEffect(new PotionEffect(MobEffects.GLOWING, 13, 1, true, false));
					victim.hurtResistantTime = 0;
				}
				
				if ( attacker != null )
				{
					victim.hurtResistantTime = 0;
					victim.attackEntityFrom(DamageSource.causeIndirectMagicDamage(attacker, attacker), victim.isWet() ? (ConfigurationHandler.lightningEnchantmentBaseDamage + lvl * ConfigurationHandler.lightningEnchantmentDamagePerLevel) * ConfigurationHandler.lightningEnchantmentWetModifier : (ConfigurationHandler.lightningEnchantmentBaseDamage + lvl * ConfigurationHandler.lightningEnchantmentDamagePerLevel));
					victim.hurtResistantTime = 0;
				}
				else
				{
					victim.hurtResistantTime = 0;
					victim.attackEntityFrom(DamageSource.LIGHTNING_BOLT.setMagicDamage(), victim.isWet() ? (ConfigurationHandler.lightningEnchantmentBaseDamage + lvl * ConfigurationHandler.lightningEnchantmentDamagePerLevel) * ConfigurationHandler.lightningEnchantmentWetModifier : (ConfigurationHandler.lightningEnchantmentBaseDamage + lvl * ConfigurationHandler.lightningEnchantmentDamagePerLevel));
					victim.hurtResistantTime = 0;
				}
			}
		}
	}

}