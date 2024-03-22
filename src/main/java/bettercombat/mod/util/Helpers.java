package bettercombat.mod.util;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import bettercombat.mod.client.ClientProxy;
import bettercombat.mod.handler.EventHandlers;
import bettercombat.mod.network.PacketHandler;
import bettercombat.mod.network.PacketParrying;
import bettercombat.mod.util.ConfigurationHandler.Animation;
import bettercombat.mod.util.ConfigurationHandler.CustomAxe;
import bettercombat.mod.util.ConfigurationHandler.CustomShield;
import bettercombat.mod.util.ConfigurationHandler.CustomSword;
import bettercombat.mod.util.ConfigurationHandler.CustomWeapon;
import bettercombat.mod.util.ConfigurationHandler.CustomWeaponPotionEffect;
import bettercombat.mod.util.ConfigurationHandler.SoundType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeEventFactory;

public final class Helpers
{
	private Helpers(){}
	
	public static Random rand = new Random();

	/* ====================================================================================================================================================== */
    /*																		ATTACK 																			  */
    /* ====================================================================================================================================================== */
	
	public static void message(EntityPlayer player, String message)
	{
		player.sendMessage(new TextComponentString(message));
	}
	
	public static boolean playerAttackVictim( EntityPlayer player, Entity victim, boolean mainhandAttack )
	{
		final ItemStack offhand = player.getHeldItemOffhand();
		final ItemStack mainhand = player.getHeldItemMainhand();
				
		boolean attacked = false;
		
		if ( mainhandAttack ) /* MAINHAND */
		{
			player.setHeldItem(EnumHand.OFF_HAND, ItemStack.EMPTY);
			
			attacked = playerAttackVictimWithWeapon(player, victim, mainhand, offhand, true);
			
			player.setHeldItem(EnumHand.OFF_HAND, offhand);

			if ( player.world.isRemote )
			{
				ClientProxy.EHC_INSTANCE.checkItemstacksChanged(true);
			}
			
			if ( !mainhand.isEmpty() && victim instanceof EntityLivingBase )
			{
				ItemStack beforeHitCopy = mainhand.copy();
				
				mainhand.hitEntity((EntityLivingBase) victim, player);
				
				if ( mainhand.isEmpty() )
				{
					player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
					ForgeEventFactory.onPlayerDestroyItem(player, beforeHitCopy, EnumHand.MAIN_HAND);
					
					if ( player.world.isRemote )
					{
						ClientProxy.EHC_INSTANCE.checkItemstacksChanged(true);
					}
				}
			}
		}
		else /* OFFHAND */
		{			
			player.setHeldItem(EnumHand.OFF_HAND, ItemStack.EMPTY);
			player.setHeldItem(EnumHand.MAIN_HAND, offhand);

			if ( offhand.getItem() instanceof ItemShield )
			{
				attacked = playerAttackVictimWithShield(player, victim, (ItemShield)offhand.getItem());
			}
			else
			{
				attacked = playerAttackVictimWithWeapon(player, victim, mainhand, offhand, false);
			}
			
			player.setHeldItem(EnumHand.OFF_HAND, offhand);
			player.setHeldItem(EnumHand.MAIN_HAND, mainhand);
			
			if ( player.world.isRemote )
			{
				ClientProxy.EHC_INSTANCE.checkItemstacksChanged(true);
			}

			if ( !offhand.isEmpty() && victim instanceof EntityLivingBase )
			{
				ItemStack beforeHitCopy = offhand.copy();
				
				offhand.hitEntity((EntityLivingBase) victim, player);
				
				if ( offhand.isEmpty() )
				{
					player.setHeldItem(EnumHand.OFF_HAND, ItemStack.EMPTY);
					ForgeEventFactory.onPlayerDestroyItem(player, beforeHitCopy, EnumHand.OFF_HAND);
					
					if ( player.world.isRemote )
					{
						ClientProxy.EHC_INSTANCE.checkItemstacksChanged(true);
					}
				}
			}
		}
		
		return attacked;
	}

	/*
	 * player.attackTargetEntityWithCurrentItem(victim) vanilla attacks are cancelled and this method is instead called
	 */
	public static boolean playerAttackVictimWithWeapon( EntityPlayer player, Entity entity, ItemStack mainhand, ItemStack offhand, boolean mainhandAttack )
	{
		if ( entity == null || entity == player )
		{
			return false;
		}
		
		boolean isCrit 				= false;
		
		double knockbackMod 		= EnchantmentHelper.getKnockbackModifier(player);
		int fireAspect 				= EnchantmentHelper.getFireAspectModifier(player);
		int sweepAmount 			= 0;
		
		double critChance 			= player.getEntityAttribute(EventHandlers.CRIT_CHANCE).getAttributeValue();
		double critDamage			= player.getEntityAttribute(EventHandlers.CRIT_DAMAGE).getAttributeValue();
		double additionalReach		= 0.0D;
		double enchantmentModifier 	= 0.0D;
		double armor 				= 0.0D;
		double armorToughness 		= 0.0D;
		double damage				= 0.0D;

		SoundType soundType 		= null;
		Animation animation 		= null;

		final ItemStack weapon;
		final boolean isMetal;
		final boolean configWeapon;

		if ( mainhandAttack )
		{
			weapon = mainhand;
			damage = getMainhandAttackDamage(player, mainhand);
		}
		else
		{
			weapon = offhand;
			damage = getOffhandAttackDamage(player, offhand, mainhand);
			damage *= ConfigurationHandler.offHandEfficiency;
		}
				
		CustomWeaponPotionEffect customWeaponPotionEffect = null;
		
		if ( ConfigurationHandler.isItemClassWhiteList(weapon.getItem()) )
		{
			configWeapon = true;

			final String weaponName = getString(weapon);
			
			isMetal = Helpers.isMetal(weaponName);
			
			boolean customWeapon = false;
			
			for ( CustomWeapon s : ConfigurationHandler.weapons )
			{
				if ( weaponName.contains(s.name) )
				{
					customWeapon = true;
					
					sweepAmount += s.sweepMod;
					knockbackMod += s.knockbackMod;
					critChance += s.critChanceMod - player.getEntityAttribute(EventHandlers.CRIT_CHANCE).getBaseValue();
					critDamage += s.additionalCritDamageMod;
					additionalReach = s.additionalReachMod;
					
					soundType = s.soundType;
					animation = s.animation;
					
					customWeaponPotionEffect = s.customWeaponPotionEffect;
					
					break;
				}
			}
			
			if ( !customWeapon )
			{
				sweepAmount += ConfigurationHandler.DEFAULT_CUSTOM_WEAPON.sweepMod;
				knockbackMod += ConfigurationHandler.DEFAULT_CUSTOM_WEAPON.knockbackMod;
				critChance += ConfigurationHandler.DEFAULT_CUSTOM_WEAPON.critChanceMod - player.getEntityAttribute(EventHandlers.CRIT_CHANCE).getBaseValue();
				critDamage += ConfigurationHandler.DEFAULT_CUSTOM_WEAPON.additionalCritDamageMod;
				additionalReach = ConfigurationHandler.DEFAULT_CUSTOM_WEAPON.additionalReachMod;
				
				soundType = ConfigurationHandler.DEFAULT_CUSTOM_WEAPON.soundType;
				animation = ConfigurationHandler.DEFAULT_CUSTOM_WEAPON.animation;
			}
		}
		else
		{
			damage -= ConfigurationHandler.fistAndNonWeaponDamageReduction;
			knockbackMod -= ConfigurationHandler.fistAndNonWeaponKnockbackReduction;
			additionalReach = -ConfigurationHandler.fistAndNonWeaponReachReduction;
			isMetal = false;
			configWeapon = false;
		}
		
		float tgtHealth = 0.0F;
		float tgtMaxHealth = 0.0F;
		float tgtHealthPercent = 0.0F;

		if ( player.isSprinting() )
		{
			knockbackMod += ConfigurationHandler.sprintingKnockbackAmount;
		}
		
		final EntityLivingBase victim;
		
		/* Head, legs, wings */
		MultiPartEntityPart bodyPart = null;
		
		if ( entity instanceof MultiPartEntityPart )
		{
			bodyPart = (MultiPartEntityPart)entity;

			if ( bodyPart.parent instanceof EntityLivingBase )
			{
				victim = (EntityLivingBase)bodyPart.parent;
			}
			else
			{
				/* Backup if the parent is not an EntityLivingBase */

				if ( bodyPart.parent != null )
				{
					SoundHandler.playImpactSound(player, mainhand, soundType, animation, isMetal);
					return bodyPart.parent.attackEntityFromPart(bodyPart, DamageSource.causePlayerDamage(player), (float)damage);
				}
				
				return false;
			}
		}
		else if ( entity instanceof EntityLivingBase )
		{
			victim = (EntityLivingBase)entity;
			
			if ( victim instanceof EntityArmorStand )
			{
				((EntityArmorStand)victim).punchCooldown = victim.world.getTotalWorldTime();
			}
		}
		else /* Minecarts & Boats & Quark's Totem of Holding */
		{
			player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, player.getSoundCategory(), 1.0F, 1.0F);

			entity.hitByEntity(player);

			/* Damage has to be ~2 or higher, otherwise the boats and carts will not break */
			if ( entity.attackEntityFrom(DamageSource.causePlayerDamage(player), damage > 2.0F ? (float)damage : 2.0F ) )
			{
				return true;
			}
			
			return false;
		}
		
		if ( !entity.isEntityAlive() || !entity.canBeAttackedWithItem() )
		{
			return false;
		}
		
		try
		{
			armor = victim.getEntityAttribute(SharedMonsterAttributes.ARMOR).getAttributeValue();
		}
		catch (Exception e)
		{

		}

		try
		{
			armorToughness = victim.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue();
		}
		catch (Exception e)
		{

		}

		for ( PotionEffect effect : player.getActivePotionEffects() )
		{
			switch( effect.getPotion().getRegistryName().toString() )
			{
				case "bettercombat:precision":
				{
					critChance += (1.0D + effect.getAmplifier()) * ConfigurationHandler.critChancePotionAmplifier;
					break;
				}
				case "bettercombat:brutality":
				{
					critDamage += (1.0D + effect.getAmplifier()) * ConfigurationHandler.critDamagePotionAmplifier;
					break;
				}
				case "wards:effect_knockback":
				{
					knockbackMod += (1.0D + effect.getAmplifier());
					break;
				}
				case "wards:effect_sharpness":
				{
					enchantmentModifier += 0.5D + (1 + effect.getAmplifier()) * 0.5D;
					break;
				}
				case "wards:effect_smite":
				{
					if ( victim.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD )
					{
						enchantmentModifier += (1.0D + effect.getAmplifier()) * 2.5D;
					}
					break;
				}
				case "wards:effect_arthropods":
				{
					if ( victim.getCreatureAttribute() == EnumCreatureAttribute.ARTHROPOD )
					{
						enchantmentModifier += (1.0D + effect.getAmplifier()) * 2.5D;
					}
					break;
				}
				case "wards:effect_fire_aspect":
				{
					fireAspect += (1 + effect.getAmplifier());
					break;
				}
				case "wards:effect_sweeping":
				{
					sweepAmount += (1 + effect.getAmplifier());
					break;
				}
				case "wards:effect_unbreaking":
				{
					if ( !weapon.isEmpty() && weapon.isItemDamaged() )
					{
						if ( player.getRNG().nextInt((2 + effect.getAmplifier())) > 0 )
						{
							weapon.setItemDamage(weapon.getItemDamage() - 1);
						}
					}
					break;
				}
				default:
				{
					break;
				}
			}
		}
		
		if ( ConfigurationHandler.autoCritOnSneakAttacks )
		{
			if ( victim instanceof IMob && victim instanceof EntityLiving )
			{
				EntityLiving el = ((EntityLiving) victim);

				if ( el.getAttackTarget() != player && el.getRevengeTarget() != player )
				{
					isCrit = true;
				}
			}
		}

		try
		{
			tgtHealthPercent = tgtHealth / tgtMaxHealth;
		}
		catch ( Exception e )
		{
			tgtHealthPercent = 0.0F;
		}

		if ( fireAspect > 0 )
		{
			victim.setFire(fireAspect * 4);
		}

		damage += EnchantmentHelper.getModifierForCreature(weapon, victim.getCreatureAttribute());

		if ( ConfigurationHandler.baseCritPercentChance < 0.0F )
		{
			/* Vanilla crit */
			isCrit = player.fallDistance > 0.0F && !player.onGround && !player.isOnLadder() && !player.isInWater() && !player.isRiding() && !player.isPotionActive(MobEffects.BLINDNESS);
		}
		else if ( !isCrit )
		{
			if ( (player.fallDistance > 0.0D && !player.onGround) && !player.isOnLadder() && !player.isInWater() && !player.isPotionActive(MobEffects.BLINDNESS) )
			{
				critChance += ConfigurationHandler.jumpCrits;
			}

			critChance += player.getEntityAttribute(SharedMonsterAttributes.LUCK).getAttributeValue() * ConfigurationHandler.luckCritModifier;

			isCrit = critChance > player.getRNG().nextFloat();
		}
		
		if ( isCrit )
		{
			net.minecraftforge.event.entity.player.CriticalHitEvent hitResult = net.minecraftforge.common.ForgeHooks.getCriticalHit(player, victim, false, (float)critDamage);
			
			if ( hitResult != null )
			{
				damage *= hitResult.getDamageModifier();
			}
			else
			{
				damage *= critDamage;
			}
		}

		if ( ConfigurationHandler.miningFatigueDamageReduction > 0.0F && player.isPotionActive(MobEffects.MINING_FATIGUE) && damage > ConfigurationHandler.baseAttackDamage )
		{
			damage = MathHelper.clamp(damage - (damage * (ConfigurationHandler.miningFatigueDamageReduction * (player.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()+1.0D))), ConfigurationHandler.baseAttackDamage, damage);
		}
		
		if ( !ConfigurationHandler.moreSprint )
		{
			player.setSprinting(false);
		}
		
		player.addExhaustion(0.1F);
				
		if ( playerAttackedVictim(player, victim, bodyPart, (float)damage) )
		{
			SoundHandler.playImpactSound(player, mainhand, soundType, animation, isMetal);
			
			if ( customWeaponPotionEffect != null && ((customWeaponPotionEffect.potionChance <= 0.0F && isCrit) || customWeaponPotionEffect.potionChance >= player.getRNG().nextFloat()) )
			{
				if ( customWeaponPotionEffect.afflict )
				{
					victim.addPotionEffect(new PotionEffect(customWeaponPotionEffect.getPotion(), customWeaponPotionEffect.potionDuration, customWeaponPotionEffect.potionPower-1, true, false));
				}
				else
				{
					player.addPotionEffect(new PotionEffect(customWeaponPotionEffect.getPotion(), customWeaponPotionEffect.potionDuration, customWeaponPotionEffect.potionPower-1, true, false));
				}
			}
			
			if ( configWeapon && ConfigurationHandler.attackSweepParticles )
			{
				spawnSweepHit(player, victim);
			}
							
			victim.knockBack(player, (float)(0.5D * knockbackMod), MathHelper.sin(player.rotationYaw * 0.017453292F), -MathHelper.cos(player.rotationYaw * 0.017453292F));
			
			NBTTagList nbttaglist = mainhand.getEnchantmentTagList();

			/*
			 * SWEEPING Weapons with this property will inflict half attack damage (for
			 * level I) or full attack damage (for level II) to all targets that get hit by
			 * the sweep attack
			 */
			for ( int i = 0; i < nbttaglist.tagCount(); ++i )
			{
				NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
				Enchantment enchantment = Enchantment.getEnchantmentByID(nbttagcompound.getShort("id"));
				int j = nbttagcompound.getShort("lvl");
				if ( enchantment == Enchantments.SWEEPING )
				{
					if ( j > 0 )
					{
						/* Level 1 Sweeping Edge = 1 (goes up to 3) */
						sweepAmount += j;
					}
				}
			}

			/* Sweep only if sweepAmount is greater than 0, then subtract 1 from sweepAmount */
			if ( sweepAmount-- > 0 )
			{
				boolean swept = false;
				
				/* How far the other entities must be from the main sweep target */
				final double sweepRange = 1.0D + player.width + sweepAmount * 0.25D;
				
				if ( mainhandAttack )
				{
					additionalReach = getMainhandReach(player, additionalReach);
				}
				else
				{
					additionalReach = getOffhandReach(player, additionalReach, offhand, mainhand);
				}
				
				/* Get the entities around the victim */
				List<EntityLivingBase> sweepVictims = player.world.getEntitiesWithinAABB(EntityLivingBase.class, victim.getEntityBoundingBox().grow(sweepRange*MathHelper.SQRT_2, 1.0D, sweepRange*MathHelper.SQRT_2));

				for ( EntityLivingBase sweepVictim : sweepVictims )
				{
					if ( sweepVictim != player && sweepVictim != victim && sweepVictim.isEntityAlive() && player.getDistanceSq(sweepVictim) <= (additionalReach+sweepVictim.width)*(additionalReach+sweepVictim.width) && victim.getDistanceSq(sweepVictim) <= (sweepRange)*(sweepRange) && ConfigurationHandler.rightClickAttackable(player, sweepVictim) && !player.isOnSameTeam(sweepVictim) )
					{							
						swept = true;
						
						double sweepDamage = ConfigurationHandler.baseAttackDamage;

						sweepDamage = MathHelper.clamp(damage * (sweepAmount / 4.0D), ConfigurationHandler.baseAttackDamage, damage);
						
						if ( playerAttackedVictim(player, sweepVictim, null, (float)sweepDamage) )
						{
							sweepVictim.knockBack(player, (float)(0.5D * knockbackMod * MathHelper.clamp((4.0D + sweepAmount) / 8.0D, 0.5D, 1.0D)), MathHelper.sin(player.rotationYaw * 0.017453292F), -MathHelper.cos(player.rotationYaw * 0.017453292F));
							
							if ( ConfigurationHandler.customPotionEffectsWorkOnSweep && customWeaponPotionEffect != null && ( (customWeaponPotionEffect.potionChance <= 0.0F && isCrit) || customWeaponPotionEffect.potionChance >= player.getRNG().nextFloat()))
							{
								if ( customWeaponPotionEffect.afflict )
								{
									sweepVictim.addPotionEffect(new PotionEffect(customWeaponPotionEffect.getPotion(), customWeaponPotionEffect.potionDuration, customWeaponPotionEffect.potionPower-1, true, false));
								}
								else
								{
									player.addPotionEffect(new PotionEffect(customWeaponPotionEffect.getPotion(), customWeaponPotionEffect.potionDuration, customWeaponPotionEffect.potionPower-1, true, false));
								}
							}
						}
					}
				}

				if ( swept )
				{
					player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, player.getSoundCategory(), ConfigurationHandler.weaponHitSoundVolume, randomPitch(player));
				}
			}
			
			if ( isCrit )
			{
				player.onCriticalHit(victim);
				
				player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(), ConfigurationHandler.weaponHitSoundVolume, randomPitch(player));
				
				if ( enchantmentModifier > 0.0F )
				{
					player.onEnchantmentCritical(victim);
				}
			}

			/* SHIELD */
			ItemStack activeItem = victim.isHandActive() ? victim.getActiveItemStack() : ItemStack.EMPTY;
			
			/* Blocking */
			if ( victim.isActiveItemStackBlocking() && configWeapon )
			{
				Vec3d vec3d = player.getPositionVector();

				if ( vec3d != null )
				{
					Vec3d vec3d1 = victim.getLook(1.0F);
					Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(victim.posX, victim.posY, victim.posZ)).normalize();
					vec3d2 = new Vec3d(vec3d2.x, 0.0D, vec3d2.z);

					/* Blocked */
					if ( vec3d2.dotProduct(vec3d1) < 0.0D )
					{
						int disableDuration = 0;
						
						for ( CustomAxe axe : ConfigurationHandler.axes )
						{
							if ( weapon.getItem().getRegistryName().toString().contains(axe.name) )
							{
								disableDuration += axe.disableDuration;
								break;
							}
						}
						
						if ( isCrit )
						{
							disableDuration *= MathHelper.clamp(damage * ConfigurationHandler.critsDisableShield, 5, 100);
						}
						
						if ( disableDuration > 0 )
						{
							if ( victim instanceof EntityPlayer )
							{
								((EntityPlayer) victim).getCooldownTracker().setCooldown(activeItem.getItem(), disableDuration);
							}
							victim.stopActiveHand();
							player.world.setEntityState(victim, (byte)30); /* Shield break sound */
						}
					}
				}
			}
			else if ( victim.getActiveHand().equals(EnumHand.MAIN_HAND) && victim.getEntityData().hasKey("isParrying") && victim.getEntityData().getBoolean("isParrying") && configWeapon )
			{
				Vec3d vec3d = player.getPositionVector();

				if ( vec3d != null )
				{
					Vec3d vec3d1 = victim.getLook(1.0F);
					Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(victim.posX, victim.posY, victim.posZ)).normalize();
					vec3d2 = new Vec3d(vec3d2.x, 0.0D, vec3d2.z);

					/* Blocked */
					if ( vec3d2.dotProduct(vec3d1) < 0.0D )
					{
						int disableDuration = 0;
						
						for ( CustomAxe axe : ConfigurationHandler.axes )
						{
							if ( weapon.getItem().getRegistryName().toString().contains(axe.name) )
							{
								disableDuration += axe.disableDuration;
								break;
							}
						}
						
						if ( isCrit )
						{
							disableDuration += damage * ConfigurationHandler.critsDisableShield;
						}
						
						if ( disableDuration > 0 )
						{
							if ( victim instanceof EntityPlayer )
							{
								((EntityPlayer) victim).getCooldownTracker().setCooldown(activeItem.getItem(), disableDuration);
							}
							
							PacketHandler.instance.sendToServer(new PacketParrying(false));
						}
					}
				}
			}
			
			EnchantmentHelper.applyThornEnchantments(victim, player);
			EnchantmentHelper.applyArthropodEnchantments(player, victim);

			player.setLastAttackedEntity(victim);

			armor += armorToughness;
			
			if ( armor > 1 && !isCrit && damage <= tgtHealth && player.getRNG().nextFloat() * 2.0F * damage <= (player.getRNG().nextDouble() + tgtHealthPercent) * armor )
			{
				SoundHandler.playImpactArmorMetalSound(player, tgtMaxHealth, tgtHealthPercent);
			}
			
			/* DAMAGE PARTICLES */
			if ( ConfigurationHandler.damageParticles && player.world instanceof WorldServer )
			{
				if ( damage >= 1.0D )
				{
					int k = (int) (damage * 0.5D);
					((WorldServer) player.world).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, victim.posX, victim.posY + victim.height * 0.5F, victim.posZ, k, 0.1D, 0.0D, 0.1D, 0.2D);
				}
			}			
			return true;
		}
		else
		{			
			player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, player.getSoundCategory(), 1.0F, 1.0F);
			return false;
		}
	}

	private static boolean playerAttackedVictim(EntityPlayer player, EntityLivingBase victim, @Nullable MultiPartEntityPart bodyPart, float damage)
	{
		victim.hurtResistantTime = 0;
		
		boolean attacked = false;
		
		if ( bodyPart != null && victim instanceof IEntityMultiPart )
		{
			attacked = ((IEntityMultiPart)victim).attackEntityFromPart(bodyPart, DamageSource.causePlayerDamage(player), damage);
		}
		
		if ( !attacked )
		{
			attacked = victim.attackEntityFrom(DamageSource.causePlayerDamage(player), damage);
		}
		
		if ( victim.hitByEntity(player) )
		{
			attacked = true;
		}
		
		if ( attacked )
		{
			player.setLastAttackedEntity(victim);
			victim.hurtResistantTime = 0;
		}
		
		return attacked;
	}

	private static boolean playerAttackVictimWithShield( EntityPlayer player, Entity entity, ItemShield shield )
	{		
		if ( entity == null || entity == player )
		{
			return false;
		}
		
		double knockbackMod = EnchantmentHelper.getKnockbackModifier(player);
		double damage = ConfigurationHandler.baseAttackDamage;
		
		try
		{
			knockbackMod += player.getActivePotionEffect(MobEffects.STRENGTH).getAmplifier();
		}
		catch (Exception e)
		{

		}

		try
		{
			knockbackMod -= player.getActivePotionEffect(MobEffects.WEAKNESS).getAmplifier();
		}
		catch (Exception e)
		{

		}
		
		for ( CustomShield customShield : ConfigurationHandler.shields )
		{
			if ( shield.equals(customShield.shield) )
			{
				damage = customShield.damage;
				
				knockbackMod += customShield.knockback;

				break;
			}
		}
		
		final EntityLivingBase victim;
		MultiPartEntityPart bodyPart = null;
		
		if ( entity instanceof MultiPartEntityPart )
		{
			bodyPart = (MultiPartEntityPart)entity;

			if ( bodyPart.parent instanceof EntityLivingBase )
			{
				victim = (EntityLivingBase)bodyPart.parent;
			}
			else
			{
				/* Backup if the parent is not an EntityLivingBase */
				
				if ( bodyPart.parent != null )
				{
					if ( isMetal(getString(shield)) )
					{
						SoundHandler.playBashMetalShieldSound(player);
					}
					else
					{
						SoundHandler.playBashWoodShieldSound(player);
					}
					
					return bodyPart.parent.attackEntityFromPart(bodyPart, DamageSource.causePlayerDamage(player), (float)damage);
				}
				
				return false;
			}
		}
		else if ( entity instanceof EntityLivingBase )
		{
			victim = (EntityLivingBase)entity;
			
			if ( victim instanceof EntityArmorStand )
			{
				((EntityArmorStand)victim).punchCooldown = victim.world.getTotalWorldTime();
			}
		}
		else /* Minecarts & Boats & Quark's Totem of Holding */
		{
			player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, player.getSoundCategory(), 1.0F, 1.0F);

			entity.hitByEntity(player);

			/* Damage has to be ~2 or higher, otherwise the boats and carts will not break */
			if ( entity.attackEntityFrom(DamageSource.causePlayerDamage(player), damage > 2.0F ? (float)damage : 2.0F ) )
			{
				return true;
			}
			
			return false;
		}
		
		if ( !entity.isEntityAlive() || !entity.canBeAttackedWithItem() )
		{
			return false;
		}
		
		if ( ConfigurationHandler.shieldSilverDamageMultiplier != 1.0F && victim.isEntityUndead() && isSilver(getString(shield)) )
		{
			damage *= ConfigurationHandler.shieldSilverDamageMultiplier;
			EventHandlers.playSilverArmorEffect(victim);
		}
		
		player.addExhaustion(0.1F);
				
		if ( playerAttackedVictim(player, victim, bodyPart, (float)damage) )
		{
			if ( isMetal(getString(shield)) )
			{
				SoundHandler.playBashMetalShieldSound(player);
			}
			else
			{
				SoundHandler.playBashWoodShieldSound(player);
			}

			victim.knockBack(player, (float)(0.5D * knockbackMod), MathHelper.sin(player.rotationYaw * 0.017453292F), -MathHelper.cos(player.rotationYaw * 0.017453292F));
			
			if ( ConfigurationHandler.shieldBashingTaunts && victim instanceof EntityCreature )
			{
				((EntityCreature)victim).setAttackTarget(player);
				((EntityCreature)victim).setRevengeTarget(player);
			}
			
			player.onCriticalHit(victim);
			
			return true;
		}
		
		return false;
	}

	private static float randomPitch(EntityPlayer player)
	{
		return 0.9F + player.getRNG().nextFloat() * 0.2F;
	}
	
	public static void applySwingInteria(EntityPlayer player)
	{
		if ( ConfigurationHandler.inertiaOnAttack != 1.0F )
		{
			if ( player.onGround )
			{
				player.motionX *= ConfigurationHandler.inertiaOnAttack;
				player.motionZ *= ConfigurationHandler.inertiaOnAttack;
				
				player.velocityChanged = true;
			}
		}
	}

//	public static final double RADIAN_TO_DEGREE = 180.0D/Math.PI;
//	
//	/* returns true if the target entity is in view of in entity, uses head rotation to calculate */
//	public static boolean isEntityInView( EntityLivingBase in, EntityLivingBase target )
//	{
//        double rotation = (Math.atan2(target.posZ - in.posZ, target.posX - in.posX) * RADIAN_TO_DEGREE + 360) % 360 - (in.rotationYawHead + 450) % 360;
//        return (rotation <= 50 && rotation >= -50) || rotation >= 310 || rotation <= -310;
//	}
	
	public static double calculateAttribute( double attribute, double additive, double multiplicative )
	{
		return attribute *= additive * multiplicative;
	}
	
	public static double getBaseReach( EntityPlayer player )
	{
		if ( ConfigurationHandler.baseReachDistance > 0.0 )
		{
			return ConfigurationHandler.baseReachDistance;
		}
		
		return player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getBaseValue();
	}
	
	public static double getMainhandReach( EntityPlayer player, double additionalReach )
	{
		double reach = additionalReach + getBaseReach(player);
		
		double additive = 1.0D;
		double multiplicative = 1.0D;
		
		for ( AttributeModifier attribute : player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getModifiers() )
		{
			switch( attribute.getOperation() )
			{
				case 0:
				{
					reach += attribute.getAmount();
					break;
				}
				case 1:
				{
					additive += attribute.getAmount();
					break;
				}
				case 2:
				{
					multiplicative *= (1.0D + attribute.getAmount());
					break;
				}
			}
		}
		
		return calculateAttribute(reach, additive, multiplicative);
	}
	
	public static int getMainhandCooldown( EntityPlayer player, ItemStack mh, ItemStack oh )
	{
		double speed = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();
		
		double multiply_base = 1.0D;

		double multiply = 1.0D;

		/* + ALL */
		for ( AttributeModifier attribute : player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getModifiers() )
		{
			switch( attribute.getOperation() )
			{
				case 0:
				{
					speed += attribute.getAmount();
					break;
				}
				case 1:
				{
					multiply_base += attribute.getAmount();
					break;
				}
				case 2:
				{
					multiply *= (1.0D + attribute.getAmount());
					break;
				}
			}
		}
		
		if ( mh.getItem() instanceof ItemSword && !ConfigurationHandler.swords.isEmpty() )
		{
			String s = Helpers.getString(mh);
			
			for ( CustomSword sword : ConfigurationHandler.swords )
			{
				if ( s.contains(sword.name) )
				{
					speed += sword.attackSpeed;
					break;
				}
			}
		}
		
		if ( !mh.isEmpty() && !oh.isEmpty() )
		{
			/* Set the fatigue to the custom weapon fatigue, only versatile weapons have fatigue */
			speed /= 1.0 + ClientProxy.EHC_INSTANCE.betterCombatMainhand.getFatigue() + ClientProxy.EHC_INSTANCE.betterCombatOffhand.getFatigue()/2.0;
		}
		
		return calculateAttackSpeedTicks(speed, multiply_base, multiply);
	}
	
	private static int calculateAttackSpeedTicks( double speed, double multiply_base, double multiply )
	{
		return (int)( (20.0D/MathHelper.clamp(calculateAttribute(speed, multiply_base, multiply), 0.1D, 20.0D)) + ConfigurationHandler.addedSwingTickCooldown );
	}
		
	/* ====================================================================================================================================================== */
    /*																	OFFHAND REACH 																		  */
    /* ====================================================================================================================================================== */
	
	public static double getOffhandReach( EntityPlayer player, double additionalReach, ItemStack oh, ItemStack mh )
	{
		double reach = additionalReach + getBaseReach(player);
		double additive = 1.0D;
		double multiplicative = 1.0D;

		/* + ALL */
		for ( AttributeModifier attribute : player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getModifiers() )
		{
			switch( attribute.getOperation() )
			{
				case 0:
				{
					reach += attribute.getAmount();
					break;
				}
				case 1:
				{
					additive += attribute.getAmount();
					break;
				}
				case 2:
				{
					multiplicative *= (1.0D + attribute.getAmount());
					break;
				}
			}
		}

		/* - MAINHAND */
		for ( Map.Entry<String, AttributeModifier> modifier : mh.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).entries() )
		{
			if ( modifier.getKey().contains("reachDistance") )
			{
				// Helpers.message("-key mh " + modifier.getValue().getAmount());

				switch( modifier.getValue().getOperation() )
				{
					case 0:
					{
						reach -= modifier.getValue().getAmount();
						break;
					}
					case 1:
					{
						additive -= modifier.getValue().getAmount();
						break;
					}
					case 2:
					{
						multiplicative /= (1.0D + modifier.getValue().getAmount());
						break;
					}
				}
			}
		}

		/* + OFFHAND */
		for ( Map.Entry<String, AttributeModifier> modifier : oh.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).entries() )
		{
			if ( modifier.getKey().contains("reachDistance") )
			{
				// Helpers.message("+key oh" + modifier.getValue().getAmount());

				switch( modifier.getValue().getOperation() )
				{
					case 0:
					{
						reach += modifier.getValue().getAmount();
						break;
					}
					case 1:
					{
						additive += modifier.getValue().getAmount();
						break;
					}
					case 2:
					{
						multiplicative *= (1.0D + modifier.getValue().getAmount());
						break;
					}
				}
			}
		}

		/* - MAINHAND QUALITY TOOLS */
		if ( mh.hasTagCompound() && mh.getTagCompound().hasKey("Quality", 10) )
		{
			final NBTTagCompound tag = mh.getSubCompound("Quality");
			final NBTTagList attributeList = tag.getTagList("AttributeModifiers", 10);

			for ( int j = 0; j < attributeList.tagCount(); ++j )
			{
				final AttributeModifier modifier = SharedMonsterAttributes.readAttributeModifierFromNBT(attributeList.getCompoundTagAt(j));
				final String attributeName = attributeList.getCompoundTagAt(j).getString("AttributeName");

				if ( attributeName.contains("reachDistance") )
				{
					switch( modifier.getOperation() )
					{
						case 0:
						{
							reach -= modifier.getAmount();
							break;
						}
						case 1:
						{
							additive -= modifier.getAmount();
							break;
						}
						case 2:
						{
							multiplicative /= (1.0D + modifier.getAmount());
							break;
						}
					}
				}
			}
		}

		/* + OFFHAND QUALITY TOOLS */
		if ( oh.hasTagCompound() && oh.getTagCompound().hasKey("Quality", 10) )
		{
			final NBTTagCompound tag = oh.getSubCompound("Quality");
			final NBTTagList attributeList = tag.getTagList("AttributeModifiers", 10);

			for ( int j = 0; j < attributeList.tagCount(); ++j )
			{
				final AttributeModifier modifier = SharedMonsterAttributes.readAttributeModifierFromNBT(attributeList.getCompoundTagAt(j));
				final String attributeName = attributeList.getCompoundTagAt(j).getString("AttributeName");

				if ( attributeName.contains("reachDistance") )
				{
					switch( modifier.getOperation() )
					{
						case 0:
						{
							reach += modifier.getAmount();
							break;
						}
						case 1:
						{
							additive += modifier.getAmount();
							break;
						}
						case 2:
						{
							multiplicative *= (1.0D + modifier.getAmount());
							break;
						}
					}
				}
			}
		}

		return calculateAttribute(reach, additive, multiplicative);
	}

    /* ====================================================================================================================================================== */
    /*																OFFHAND ATTACK SPEED 																	  */
    /* ====================================================================================================================================================== */
	private static UUID weaponModifierUUID = UUID.fromString("fa233e1c-4180-4865-b01b-bcce9785aca3");
	
	public static int getOffhandCooldown( EntityPlayer player, ItemStack oh, ItemStack mh )
	{
		double speed = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();

		double multiply_base = 1.0D;

		double multiply = 1.0D;

		/* + ALL */
		for ( AttributeModifier attribute : player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getModifiers() )
		{			
			if ( attribute.getID().equals(weaponModifierUUID) )
			{
				continue;
			}
			
			switch( attribute.getOperation() )
			{
				case 0:
				{
					speed += attribute.getAmount();
					break;
				}
				case 1:
				{
					multiply_base += attribute.getAmount();
					break;
				}
				case 2:
				{
					multiply *= (1.0D + attribute.getAmount());
					break;
				}
			}
		}

		/* - MAINHAND */
		for ( Map.Entry<String, AttributeModifier> modifier : mh.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).entries() )
		{
			if ( modifier.getKey().contains("attackSpeed") )
			{
				if ( modifier.getValue().getID().equals(weaponModifierUUID) )
				{
					continue;
				}
				
				switch( modifier.getValue().getOperation() )
				{
					case 0:
					{
						speed -= modifier.getValue().getAmount();
						break;
					}
					case 1:
					{
						multiply_base -= modifier.getValue().getAmount();
						break;
					}
					case 2:
					{
						multiply /= (1.0D + modifier.getValue().getAmount());
						break;
					}
				}
			}
		}

		/* + OFFHAND */
		for ( Map.Entry<String, AttributeModifier> modifier : oh.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).entries() )
		{
			if ( modifier.getKey().contains("attackSpeed") )
			{
				switch( modifier.getValue().getOperation() )
				{
				case 0:
				{
					speed += modifier.getValue().getAmount();
					break;
				}
				case 1:
				{
					multiply_base += modifier.getValue().getAmount();
					break;
				}
				case 2:
				{
					multiply *= (1.0D + modifier.getValue().getAmount());
					break;
				}
				}
			}
		}

		/* - MAINHAND QUALITY TOOLS */
		if ( mh.hasTagCompound() && mh.getTagCompound().hasKey("Quality", 10) )
		{
			final NBTTagCompound tag = mh.getSubCompound("Quality");
			final NBTTagList attributeList = tag.getTagList("AttributeModifiers", 10);

			for ( int j = 0; j < attributeList.tagCount(); ++j )
			{
				final AttributeModifier modifier = SharedMonsterAttributes.readAttributeModifierFromNBT(attributeList.getCompoundTagAt(j));
				final String attributeName = attributeList.getCompoundTagAt(j).getString("AttributeName");

				if ( attributeName.contains("attackSpeed") )
				{
					switch( modifier.getOperation() )
					{
					case 0:
					{
						speed -= modifier.getAmount();
						break;
					}
					case 1:
					{
						multiply_base -= modifier.getAmount();
						break;
					}
					case 2:
					{
						multiply /= (1.0D + modifier.getAmount());
						break;
					}
					}
				}
			}
		}

		/* + OFFHAND QUALITY TOOLS */
		if ( oh.hasTagCompound() && oh.getTagCompound().hasKey("Quality", 10) )
		{
			final NBTTagCompound tag = oh.getSubCompound("Quality");
			final NBTTagList attributeList = tag.getTagList("AttributeModifiers", 10);

			for ( int j = 0; j < attributeList.tagCount(); ++j )
			{
				final AttributeModifier modifier = SharedMonsterAttributes.readAttributeModifierFromNBT(attributeList.getCompoundTagAt(j));
				final String attributeName = attributeList.getCompoundTagAt(j).getString("AttributeName");

				if ( attributeName.contains("attackSpeed") )
				{
					switch( modifier.getOperation() )
					{
						case 0:
						{
							speed += modifier.getAmount();
							break;
						}
						case 1:
						{
							multiply_base += modifier.getAmount();
							break;
						}
						case 2:
						{
							multiply *= (1.0D + modifier.getAmount());
							break;
						}
					}
				}
			}
		}
		
		if ( oh.getItem() instanceof ItemSword && !ConfigurationHandler.swords.isEmpty() )
		{
			String s = Helpers.getString(oh);
			
			for ( CustomSword sword : ConfigurationHandler.swords )
			{
				if ( s.contains(sword.name) )
				{
					speed += sword.attackSpeed;
					break;
				}
			}
		}
		

		if ( !oh.isEmpty() && !mh.isEmpty() )
		{
			/* Set the fatigue to the custom weapon fatigue, only versatile weapons have fatigue */
			speed /= 1.0 + ClientProxy.EHC_INSTANCE.betterCombatOffhand.getFatigue() + ClientProxy.EHC_INSTANCE.betterCombatMainhand.getFatigue()/2.0;
		}

		return calculateAttackSpeedTicks(speed, multiply_base, multiply);
	}

    /* ====================================================================================================================================================== */
    /*																OFFHAND ATTACK DAMAGE 																	  */
    /* ====================================================================================================================================================== */
	
	public static double getOffhandAttackDamage( EntityPlayer player, ItemStack oh, ItemStack mh )
	{
		double attackDamage = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();

		double multiply_base = 1.0D;

		double multiply = 1.0D;

		/* + ALL */
		for ( AttributeModifier attribute : player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getModifiers() )
		{
			switch( attribute.getOperation() )
			{
				case 0:
				{
					attackDamage += attribute.getAmount();
					break;
				}
				case 1:
				{
					multiply_base += attribute.getAmount();
					break;
				}
				case 2:
				{
					multiply *= (1.0D + attribute.getAmount());
					break;
				}
			}
		}

		/* - MAINHAND */
		for ( Map.Entry<String, AttributeModifier> modifier : mh.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).entries() )
		{
			if ( modifier.getKey().contains("attackDamage") )
			{
				switch( modifier.getValue().getOperation() )
				{
					case 0:
					{
						attackDamage -= modifier.getValue().getAmount();
						break;
					}
					case 1:
					{
						multiply_base -= modifier.getValue().getAmount();
						break;
					}
					case 2:
					{
						multiply /= (1.0D + modifier.getValue().getAmount());
						break;
					}
				}
			}
		}

		/* + OFFHAND */
		for ( Map.Entry<String, AttributeModifier> modifier : oh.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).entries() )
		{
			if ( modifier.getKey().contains("attackDamage") )
			{
				switch( modifier.getValue().getOperation() )
				{
					case 0:
					{
						attackDamage += modifier.getValue().getAmount();
						break;
					}
					case 1:
					{
						multiply_base += modifier.getValue().getAmount();
						break;
					}
					case 2:
					{
						multiply *= (1.0D + modifier.getValue().getAmount());
						break;
					}
				}
			}
		}

		/* - MAINHAND QUALITY TOOLS */
		if ( mh.hasTagCompound() && mh.getTagCompound().hasKey("Quality", 10) )
		{
			final NBTTagCompound tag = mh.getSubCompound("Quality");
			final NBTTagList attributeList = tag.getTagList("AttributeModifiers", 10);

			for ( int j = 0; j < attributeList.tagCount(); ++j )
			{
				final AttributeModifier modifier = SharedMonsterAttributes.readAttributeModifierFromNBT(attributeList.getCompoundTagAt(j));
				final String attributeName = attributeList.getCompoundTagAt(j).getString("AttributeName");

				if ( attributeName.contains("attackDamage") )
				{
					// Helpers.message("tag " + modifier.getAmount());

					switch( modifier.getOperation() )
					{
						case 0:
						{
							attackDamage -= modifier.getAmount();
							break;
						}
						case 1:
						{
							multiply_base -= modifier.getAmount();
							break;
						}
						case 2:
						{
							multiply /= (1.0D + modifier.getAmount());
							break;
						}
					}
				}
			}
		}

		/* + OFFHAND QUALITY TOOLS */
		if ( oh.hasTagCompound() && oh.getTagCompound().hasKey("Quality", 10) )
		{
			final NBTTagCompound tag = oh.getSubCompound("Quality");
			final NBTTagList attributeList = tag.getTagList("AttributeModifiers", 10);

			for ( int j = 0; j < attributeList.tagCount(); ++j )
			{
				final AttributeModifier modifier = SharedMonsterAttributes.readAttributeModifierFromNBT(attributeList.getCompoundTagAt(j));
				final String attributeName = attributeList.getCompoundTagAt(j).getString("AttributeName");

				if ( attributeName.contains("attackDamage") )
				{
					// Helpers.message("tag " + modifier.getAmount());

					switch( modifier.getOperation() )
					{
						case 0:
						{
							attackDamage += modifier.getAmount();
							break;
						}
						case 1:
						{
							multiply_base += modifier.getAmount();
							break;
						}
						case 2:
						{
							multiply *= (1.0D + modifier.getAmount());
							break;
						}
					}
				}
			}
		}

		return attackDamage *= multiply_base *= multiply;
	}

    /* ====================================================================================================================================================== */
    /*																MAINHAND ATTACK DAMAGE 																	  */
    /* ====================================================================================================================================================== */
	
	public static double getMainhandAttackDamage( EntityPlayer player, ItemStack mh )
	{
		double attackDamage = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();

		double multiply_base = 1.0D;

		double multiply = 1.0D;

		for ( AttributeModifier attribute : player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getModifiers() )
		{
			switch( attribute.getOperation() )
			{
				case 0:
				{
					attackDamage += attribute.getAmount();
					break;
				}
				case 1:
				{
					multiply_base += attribute.getAmount();
					break;
				}
				case 2:
				{
					multiply *= (1.0D + attribute.getAmount());
					break;
				}
			}
		}

		return attackDamage *= multiply_base *= multiply;
	}

//	public static final UUID STRENGTH_POTION_UUID = UUID.fromString("648d7064-6a60-4f59-8abe-c2c23a6dd7a9");
//	public static final UUID WEAKNESS_POTION_UUID = UUID.fromString("22653b89-116e-49dc-9b6b-9971489b5be5");

	/* https://algorithms.tutorialhorizon.com/convert-integer-to-roman/ */
	
	static final int[] values =
	{
		1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1
	};
	static final String[] romanLiterals =
	{
		"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"
	};
	
	public static String integerToRoman( int num )
	{
		StringBuilder roman = new StringBuilder();

		for ( int i = 0; i < values.length; i++ )
		{
			while (num >= values[i])
			{
				num -= values[i];
				roman.append(romanLiterals[i]);
			}
		}

		return roman.toString();
	}

	public static void spawnSweepHit( EntityPlayer e, Entity target )
	{
		double d0 = (double) (-MathHelper.sin(e.rotationYaw * 0.017453292F));
		double d1 = (double) MathHelper.cos(e.rotationYaw * 0.017453292F);

		if ( e.world instanceof WorldServer )
		{
			((WorldServer) e.world).spawnParticle(EnumParticleTypes.SWEEP_ATTACK, target.posX + d0 * 0.5D, e.posY + e.height * 0.5D, target.posZ + d1 * 0.5D, 0, d0, 0.0D, d1, 0.0D);
		}
	}
	
	public static <T> void execNullable( @Nullable T obj, Consumer<T> onNonNull )
	{
		if ( obj != null )
		{
			onNonNull.accept(obj);
		}
	}

	public static <T, R> R execNullable( @Nullable T obj, Function<T, R> onNonNull, R orElse )
	{
		if ( obj != null )
		{
			return onNonNull.apply(obj);
		}

		return orElse;
	}

	public static int getOffhandFireAspect( EntityPlayer player )
	{
		NBTTagList tagList = player.getHeldItemOffhand().getEnchantmentTagList();

		for ( int i = 0; i < tagList.tagCount(); i++ )
		{
			NBTTagCompound tag = tagList.getCompoundTagAt(i);

			if ( tag.getInteger("id") == Enchantment.getEnchantmentID(Enchantments.FIRE_ASPECT) )
			{
				return tag.getInteger("lvl");
			}
		}

		return 0;
	}

	public static int getOffhandKnockback( EntityPlayer player )
	{
		NBTTagList tagList = player.getHeldItemOffhand().getEnchantmentTagList();

		for ( int i = 0; i < tagList.tagCount(); i++ )
		{
			NBTTagCompound tag = tagList.getCompoundTagAt(i);

			if ( tag.getInteger("id") == Enchantment.getEnchantmentID(Enchantments.KNOCKBACK) )
			{
				return tag.getInteger("lvl");
			}
		}

		return 0;
	}

//	public static void message( Object o )
//	{
//		try
//		{
//			Minecraft mc = Minecraft.getMinecraft();
//			EntityPlayer player = mc.player;
//			player.sendMessage(new TextComponentString("" + o));
//		}
//		catch (Exception e)
//		{
//
//		}
//	}
	

	public static String getString(ItemStack itemStack)
	{
		return itemStack.getItem().getRegistryName().getResourcePath();
	}
	
	public static String getString(Item item)
	{
		return item.getRegistryName().getResourcePath();
	}
	
	public static boolean isSilver(String string)
	{
		return string.contains("silver");
	}
	
	public static boolean isSilver(ItemStack itemStack)
	{
		return getString(itemStack).contains("silver");
	}
	
	public static boolean isMetal(String string)
	{		
		for ( String s : ConfigurationHandler.nonMetalList )
		{
			if ( string.contains(s) )
			{
				return false;
			}
		}
		return true;
	}
	
	public static boolean isMetal(ItemStack itemStack)
	{
		String string = getString(itemStack);

		for ( String s : ConfigurationHandler.nonMetalList )
		{
			if ( string.contains(s) )
			{
				return false;
			}
		}
		return true;
	}
	
	public static boolean isHandActive( EntityPlayer player, EnumHand hand )
	{
		return player.isHandActive() && player.getItemInUseCount() > 0 && player.getActiveHand().equals(hand);
	}
	

	/* CombatRules */
//	if ( (armor > 0.0D || armorToughness > 0.0D) && ConfigurationHandler.warhammerArmorPiercingAdjustments && weaponName.contains("warhammer_") )
//	{
//		double damageAfterArmor = damage * (1.0F - (MathHelper.clamp(armor - damage / (2.0F + armorToughness / 4.0F), armor * 0.2F, 20.0F)) / 25.0F);
//
//		if ( damageAfterArmor < damage )
//		{
//			damage += damage - (damageAfterArmor * 0.8F);
//		}
//	}
	
	
//	public void disableShield(boolean p_190777_1_)
//    {
//        float f = 0.25F + (float)EnchantmentHelper.getEfficiencyModifier(this) * 0.05F;
//
//        if (p_190777_1_)
//        {
//            f += 0.75F;
//        }
//
//        if (this.rand.nextFloat() < f)
//        {
//            this.getCooldownTracker().setCooldown(this.getActiveItemStack().getItem(), 100);
//            this.resetActiveHand();
//            this.world.setEntityState(this, (byte)30);
//        }
//    }

//	else if (id == 30)
//    {
//        this.playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8F, 0.8F + this.world.rand.nextFloat() * 0.4F);
//    }


/* TESTING! remove XXX */
//	if ( player.getPositionVector() != null )
//	{
//		Vec3d vec3d = player.getPositionVector();
//
//		Vec3d vec3d1 = victim.getLook(1.0F);
//		Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(victim.posX, victim.posY, victim.posZ)).normalize();
//		vec3d2 = new Vec3d(vec3d2.x, 0.0D, vec3d2.z);
//
//		if ( vec3d2.dotProduct(vec3d1) < 0.0D )
//		{
//		}
//
//	}
/* TESTING! remove XXX */
}
