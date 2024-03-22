package bettercombat.mod.handler;

import java.lang.reflect.Field;
import java.util.List;

import javax.annotation.Nullable;

import bettercombat.mod.client.ClientProxy;
import bettercombat.mod.enchantment.BetterCombatEnchantments;
import bettercombat.mod.enchantment.EnchantmentLightning;
import bettercombat.mod.enchantment.EnchantmentWebbing;
import bettercombat.mod.network.PacketHandler;
import bettercombat.mod.network.PacketParried;
import bettercombat.mod.network.PacketParrying;
import bettercombat.mod.util.BetterCombatPotions;
import bettercombat.mod.util.ConfigurationHandler;
import bettercombat.mod.util.ConfigurationHandler.CustomBow;
import bettercombat.mod.util.Helpers;
import bettercombat.mod.util.PotionAetherealized;
import bettercombat.mod.util.Reference;
import bettercombat.mod.util.SoundHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockGrassPath;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionAddedEvent;
import net.minecraftforge.event.entity.living.PotionEvent.PotionApplicableEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event.HasResult;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandlers
{
	public static final IAttribute CRIT_CHANCE = (new RangedAttribute(null, Reference.MOD_ID + ".critChance", ConfigurationHandler.baseCritPercentChance, 0.0D, 1.0D)).setDescription("Critical strike chance").setShouldWatch(true);
	public static final IAttribute CRIT_DAMAGE = (new RangedAttribute(null, Reference.MOD_ID + ".critDamage", ConfigurationHandler.baseCritPercentDamage, 0.0D, Double.MAX_VALUE)).setDescription("Critical strike damage").setShouldWatch(true);

	public EventHandlers()
	{

	}
	
	/*
	    EntityPlayer.attackEntityFrom(DamageSource, float) > ForgeHooks.onPlayerAttack(this, source, amount) > LivingAttackEvent (damage)
	    
	    EntityPlayer.attackTargetEntityWithCurrentItem(Entity ) > ForgeHooks.onPlayerAttackTarget(this, targetEntity) > AttackEntityEvent (attack)
	    
	    
		public static boolean onPlayerAttackTarget(EntityPlayer player, Entity target)
	    {
	        if (MinecraftForge.EVENT_BUS.post(new AttackEntityEvent(player, target))) return false;
	        ItemStack stack = player.getHeldItemMainhand();
	        return stack.isEmpty() || !stack.getItem().onLeftClickEntity(stack, player, target);
	    }
	    
	    (Item)
	    
	    Called when the player Left Clicks (attacks) an entity.
	    Processed before damage is done, if the return value is TRUE, further processing is canceled and the entity is NOT attacked!
	     
	    @param stack The Item being used
	    @param player The player that is attacking
	    @param entity The entity being attacked
	    @return True to cancel the rest of the interaction.
     
	    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity)
	    {
	        return false;
	    }
	/* 
    
	/* AttackEntityEvent is responsible for calling LivingHurtEvent */
	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
	public void cancelAttackEntityEvent(AttackEntityEvent event)
	{
		/* If both hands are empty, */
		if ( (event.getEntityPlayer().getHeldItemMainhand().isEmpty() && event.getEntityPlayer().getHeldItemOffhand().isEmpty()) || !ConfigurationHandler.isBlacklisted(event.getEntityPlayer().getHeldItemMainhand().getItem()) )
		{
			/* Set the hurtResistantTime to cancel the damage, rather than completely cancelling the attack! */
			if ( event.getTarget() instanceof EntityLivingBase )
			{
				event.getTarget().hurtResistantTime = ((EntityLivingBase)event.getTarget()).maxHurtResistantTime;
			}
			
			/* Count the attack as a "hit" for the CarryOn mod! */
			event.getEntityLiving().hitByEntity(event.getEntityPlayer());
			
			return;
		}
				
		/* Cancel the event, this attack deals no damage and does not count as a hit! */
		// event.setCanceled(true);
		
		return;
	}

	@SubscribeEvent
	public void entityJoinWorldEvent(EntityJoinWorldEvent event) /* ArrowLooseEvent */
	{
		if ( event.getEntity() instanceof EntityArrow )
		{
			EntityArrow arrow = ((EntityArrow) event.getEntity());

			if ( arrow.shootingEntity instanceof EntityPlayer )
			{
				EntityPlayer p = (EntityPlayer) arrow.shootingEntity;

				if ( p.isHandActive() )
				{
					ItemStack stack = p.getActiveHand() == EnumHand.MAIN_HAND ? p.getHeldItemMainhand() : p.getHeldItemOffhand();

					if ( !stack.isEmpty() )
					{
						NBTTagList nbttaglist = stack.getEnchantmentTagList();

						for (int i = 0; i < nbttaglist.tagCount(); ++i)
						{
							int id = nbttaglist.getCompoundTagAt(i).getShort("id");
							int lvl = nbttaglist.getCompoundTagAt(i).getShort("lvl");

							if ( ConfigurationHandler.lightningEnchantmentEnabled )
							{
								if (Enchantment.getEnchantmentID(BetterCombatEnchantments.LIGHTNING) == id)
								{
									arrow.addTag("lightning~" + lvl);
									arrow.setGlowing(true);
								}
							}

							if ( ConfigurationHandler.webbingEnchantmentEnabled )
							{
								if (Enchantment.getEnchantmentID(BetterCombatEnchantments.WEBBING) == id)
								{
									arrow.addTag("webbing");
								}
							}
						}

					}

				}

			}

		}
		else if ( event.getEntity() instanceof EntityPlayer )
		{
			EntityPlayer player = (EntityPlayer) event.getEntity();
			
			player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ConfigurationHandler.baseAttackDamage);
			player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).setBaseValue(ConfigurationHandler.baseAttackSpeed);
			
			this.registerCritAttributes(player);

			if ( player.world.isRemote && player == ClientProxy.EHC_INSTANCE.mc.player )
			{
				ClientProxy.EHC_INSTANCE.checkItemstacksChanged(true);
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerClone( PlayerEvent.Clone event )
	{
		this.registerCritAttributes(event.getEntityPlayer());
	}

	@SubscribeEvent
	public void onEntityConstructing( final PlayerEvent.EntityConstructing event )
	{
		if ( event.getEntity() instanceof EntityPlayer )
		{
			EntityPlayer player = (EntityPlayer) event.getEntity();
			this.registerCritAttributes(player);
		}
	}
	
	/* For whatever reason I cannot get this to work correctly on player respawn/ join world/ whatever so
	 * I am just going to add it to all these events and wrap this ***** in a try catch and say **** it */
	public void registerCritAttributes( EntityPlayer player )
	{
		try
		{
			player.getAttributeMap().registerAttribute(CRIT_CHANCE).setBaseValue(ConfigurationHandler.baseCritPercentChance);
		  	player.getAttributeMap().registerAttribute(CRIT_DAMAGE).setBaseValue(ConfigurationHandler.baseCritPercentDamage);
		}
		catch ( Exception e )
		{
			
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true) /* EntityLivingBase */
	public void knockBack( LivingKnockBackEvent event )
	{
		if ( ConfigurationHandler.betterKnockback )
		{
			if ( !event.isCanceled() && event.getEntityLiving() != null )
			{
				double strength = event.getStrength();
				double xRatio = event.getRatioX();
				double zRatio = event.getRatioZ();
				EntityLivingBase entityLivingBase = event.getEntityLiving();
				
				double knockbackResistance = entityLivingBase.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue();
				
				if ( knockbackResistance < 1.0D )
				{
					strength *= (1.0D - knockbackResistance) * ConfigurationHandler.knockbackStrengthMultiplier;

					entityLivingBase.isAirBorne = true;
					
		            entityLivingBase.motionX *= 0.5D;
		            entityLivingBase.motionZ *= 0.5D;
		            
		            double d = Math.sqrt(xRatio * xRatio + zRatio * zRatio);

		            if ( d > 0.0D )
		            {
			            entityLivingBase.motionX -= xRatio / d * strength;
			            entityLivingBase.motionZ -= zRatio / d * strength;
		            }
		            
		            if ( entityLivingBase.onGround )
		            {
		            	entityLivingBase.motionY *= 0.5D;
		            	entityLivingBase.motionY += strength * ConfigurationHandler.knockUpStrengthMultiplier;

		                if (entityLivingBase.motionY > 0.4D)
		                {
		                	entityLivingBase.motionY = 0.4D;
		                }
		            }
		            
					entityLivingBase.velocityChanged = true;
				}
				
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
	public void potionApplicable(PotionApplicableEvent event)
	{
		if ( ConfigurationHandler.cancelSpartanWeaponryFatigue && event.getEntityLiving() instanceof EntityPlayer )
		{
			/* entity.func_70690_d(new PotionEffect(MobEffects.field_76419_f, 20, mfLevel, false, false)); */
						
			if ( event.getPotionEffect().getDuration() == 20 && event.getPotionEffect().getPotion().equals(MobEffects.MINING_FATIGUE) )
			{
				event.setResult(Result.DENY);
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
	public void potionAdded(PotionAddedEvent event)
	{
		if ( event.getEntityLiving() instanceof EntityCreature && ConfigurationHandler.nauseaAffectsMobs > 0.0F )
		{
			EntityCreature creature = (EntityCreature) (event.getEntityLiving());

			if (creature.height <= ConfigurationHandler.nauseaAffectsMobs )
			{
				if (creature.isPotionActive(MobEffects.NAUSEA) || creature.isPotionActive(MobEffects.BLINDNESS))
				{
					boolean flag = false;

					for (EntityAITaskEntry task : creature.tasks.taskEntries)
					{
						if (task.getClass().equals(EntityAINausea.class))
						{
							flag = true;
							break;
						}
					}

					if (!flag)
					{
						creature.tasks.addTask(0, new EntityAINausea(creature));
					}
					else if (creature.world.rand.nextBoolean())
					{
						creature.setAttackTarget(null);
					}
				}
			}

//			if ( ConfigurationHandler.stackableBleeds && event.getPotionEffect().getPotion().equals(BetterCombatPotions.BLEEDING) && creature.isPotionActive(BetterCombatPotions.BLEEDING) )
//			{
//				// event.getPotionEffect().getPotion().equals(BetterCombatPotions.BLEEDING)
//				// creature.isPotionActive(BetterCombatPotions.BLEEDING
//
//				PotionEffect p0 = event.getPotionEffect();
//				PotionEffect p1 = creature.getActivePotionEffect(BetterCombatPotions.BLEEDING);
//				
//				p0.combine(p1);
//				
//		        if ( p0.getAmplifier() > p1.getAmplifier() )
//		        {
//		            p0.amplifier = p1.amplifier;
//		        }
//		        else if (other.amplifier == this.amplifier && this.duration < other.duration)
//		        {
//		            this.duration = other.duration;
//		        }
//		        else if (!other.isAmbient && this.isAmbient)
//		        {
//		            this.isAmbient = other.isAmbient;
//		        }
//
//		        this.showParticles = other.showParticles;
//		    }
		}
	}
	
	

// ROTM
//	@SubscribeEvent( priority = EventPriority.LOW )
//	public static void applyMobOffenseEffect( LivingHurtEvent event )
//	{
//
//		if ( !event.getEntityLiving().world.isRemote && event.getAmount() > 0 )
//		{
//			EntityLivingBase entity = event.getEntityLiving();
//			DamageSource source = event.getSource();
//
//			if ( source.getTrueSource() instanceof EntityLivingBase )
//			{
//				EntityLivingBase attacker = (EntityLivingBase) event.getSource().getTrueSource();
//
//				if ( entity.isActiveItemStackBlocking() )
//				{
//					Vec3d vec3d = attacker.getPositionVector();
//
//					if ( vec3d != null )
//					{
//						Vec3d vec3d1 = entity.getLook(1.0F);
//						Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(entity.posX, entity.posY, entity.posZ)).normalize();
//						vec3d2 = new Vec3d(vec3d2.x, 0.0D, vec3d2.z);
//
//						if ( vec3d2.dotProduct(vec3d1) < 0.0D )
//						{
//							return;
//						}
//
//					}
//				}
//				
//				Collection<MobOffenseType> offenseTypes = ModConfigs.entityConfigs.mobOffense.get(Utils.getEntityName(attacker));
//				
//				for ( MobOffenseType type : offenseTypes )
//				{
//					if ( type.canApplyToEntity(attacker) && (type.damageType.isEmpty() || type.damageType.equalsIgnoreCase(source.getDamageType())) )
//					{
//						PotionEffect effect = new PotionEffect(ForgeRegistries.POTIONS.getValue(type.potion), type.duration, type.level);
//						entity.addPotionEffect(effect);
//
//						if ( type.sound != null )
//						{
//							entity.world.playSound(null, attacker.getPosition(), ForgeRegistries.SOUND_EVENTS.getValue(type.sound), SoundCategory.HOSTILE, 1, 1);
//						}
//
//					}
//
//				}
//
//			}
//
//		}
//
//	}
	
	/* HEAL EVENT */
	@SubscribeEvent(priority=EventPriority.HIGH, receiveCanceled=true)
	public void onHeal(LivingHealEvent event)
	{
		if ( event.getEntityLiving() != null )
		{
			if ( ConfigurationHandler.revitalizeEnchantmentEnabled )
			{
				int level = EnchantmentHelper.getMaxEnchantmentLevel(BetterCombatEnchantments.REVITALIZE, event.getEntityLiving());
		        
		        if ( level > 0 )
		        {
		        	event.setAmount(event.getAmount()*(1.0F+ConfigurationHandler.revitalizePercentPerLevel*level));
		        }
			}
		}
	}
	
	/* HURT EVENT -> BEFORE CALCULATIONS */
	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
	public void livingHurtHigh(LivingHurtEvent event)
	{
		if (event.getEntityLiving() == null)
		{
			return;
		}

		if (event.getSource() == null)
		{
			return;
		}

		if (!(event.getSource().getTrueSource() instanceof EntityLivingBase))
		{
			return;
		}

		EntityLivingBase attacker = (EntityLivingBase) event.getSource().getTrueSource();

		if (attacker.isPotionActive(BetterCombatPotions.AETHEREALIZED) && !event.getSource().isMagicDamage() && !event.getSource().isFireDamage() && !event.getSource().isExplosion())
		{
			PotionEffect potionEffect = attacker.getActivePotionEffect(BetterCombatPotions.AETHEREALIZED);

			if (potionEffect != null)
			{
				float magicDamage = event.getAmount() * potionEffect.getAmplifier() * ConfigurationHandler.alchemizedAmplifier;
				event.setAmount(event.getAmount() - magicDamage);
				event.getEntityLiving().hurtResistantTime = 0;
				event.getEntityLiving().attackEntityFrom(DamageSource.causeIndirectMagicDamage(event.getEntityLiving().getRevengeTarget(),event.getEntityLiving().getRevengeTarget()),magicDamage);
				event.getEntityLiving().hurtResistantTime = 0;
				PotionAetherealized.playAetherealizedEffect(event.getEntityLiving(), potionEffect.getAmplifier());
			}
		}

		if (event.getSource().damageType.equals("arrow"))
		{
			event.getSource().setProjectile();
		}

		if ( ConfigurationHandler.sorceryEnchantmentEnabled )
		{
			if ( event.getSource().isMagicDamage() )
		  	{
			  	int level = EnchantmentHelper.getMaxEnchantmentLevel(BetterCombatEnchantments.SORCERY, attacker);

			  	if ( level > 0 )
			  	{
				  	event.setAmount(event.getAmount()*(1.0F+ConfigurationHandler.sorceryPercentPerLevel*level));
			  	}
		  	}
		}

		if (ConfigurationHandler.silverArmorDamagesUndeadAttackers > 0.0F)
		{
			if (attacker.isEntityUndead() && !(event.getEntityLiving().isEntityUndead()) && !event.getSource().isProjectile() && attacker.getDistance(event.getEntityLiving()) < 4.0D)
			{
				int armorPieces = 0;

				for (ItemStack piece : event.getEntityLiving().getEquipmentAndArmor())
				{
					if (!piece.isEmpty() && piece.getItem().getRegistryName().toString().contains("silver_metal"))
					{
						armorPieces++;
					}
				}

				if (armorPieces > 0)
				{
					attacker.hurtResistantTime = 0;
					attacker.attackEntityFrom(DamageSource.causeIndirectMagicDamage(event.getEntityLiving(), event.getEntityLiving()),ConfigurationHandler.silverArmorDamagesUndeadAttackers * armorPieces);
					attacker.hurtResistantTime = 0;
					playSilverArmorEffect(attacker);
				}
			}
		}
	}
	
	@SubscribeEvent( priority = EventPriority.HIGHEST, receiveCanceled = true )
    public void onRightClickBlockEvent(PlayerInteractEvent.RightClickBlock event)
    {
		if ( this.cancelTools(event.getEntityPlayer(), event.getEntityPlayer().getEntityWorld().getBlockState(event.getPos()).getBlock()) )
		{
			event.setCanceled(true);
		}
    }
	

	private boolean cancelTools( EntityPlayer entityPlayer, Block block )
	{
		if ( ConfigurationHandler.grassPathingRequiresAnimation )
		{
			if ( block instanceof BlockGrass )
			{
				if ( entityPlayer.getHeldItemMainhand().getItem() instanceof ItemSpade )
				{
					return true;
				}
				else if ( entityPlayer.getHeldItemOffhand().getItem() instanceof ItemSpade )
				{
					return true;
				}
			}
		}
		
		if ( ConfigurationHandler.tillingRequiresAnimation )
		{
			if ( block instanceof BlockDirt || block instanceof BlockGrass || block instanceof BlockGrassPath )
			{
				if ( entityPlayer.getHeldItemMainhand().getItem() instanceof ItemHoe )
				{
					return true;
				}
				else if ( entityPlayer.getHeldItemOffhand().getItem() instanceof ItemHoe )
				{
					return true;
				}
			}
		}
		
		if ( ConfigurationHandler.strippingBarkRequiresAnimation )
		{
			if ( block instanceof BlockLog )
			{
				if ( entityPlayer.getHeldItemMainhand().getItem() instanceof ItemAxe )
				{
					return true;
				}
				else if ( entityPlayer.getHeldItemOffhand().getItem() instanceof ItemAxe )
				{
					return true;
				}
			}
		}
		
		/* Continue with right-click! */
		return false;
	}

	/**
	 * LivingHurtEvent is fired when an Entity is set to be hurt. <br>
	 * This event is fired whenever an Entity is hurt in 
	 * {@link EntityLivingBase#damageEntity(DamageSource, float)} and
	 * {@link EntityPlayer#damageEntity(DamageSource, float)}.<br>
	 * <br>
	 * This event is fired via the {@link ForgeHooks#onLivingHurt(EntityLivingBase, DamageSource, float)}.<br>
	 * <br>
	 * {@link #source} contains the DamageSource that caused this Entity to be hurt. <br>
	 * {@link #amount} contains the amount of damage dealt to the Entity that was hurt. <br>
	 * <br>
	 * This event is {@link Cancelable}.<br>
	 * If this event is canceled, the Entity is not hurt.<br>
	 * <br>
	 * This event does not have a result. {@link HasResult}<br>
	 * <br>
	 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
	 * @see LivingDamageEvent
	 **/
	/* HURT EVENT -> BEFORE CALCULATIONS */
	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
	public void livingHurtLow(LivingHurtEvent event)
	{
		if ( ConfigurationHandler.addDamageSourceToTippedArrows )
		{
			if (event.getEntityLiving() == null)
			{
				return;
			}

			if (event.getSource() == null)
			{
				return;
			}

			if (!(event.getSource().getTrueSource() instanceof EntityLivingBase))
			{
				return;
			}
			
			/* Add a damage source to tipped arrows */
			if (event.getSource().getTrueSource() == null && event.getSource().isMagicDamage() && event.getEntityLiving().getRevengeTarget() instanceof EntityPlayer)
			{
				event.getEntityLiving().hurtResistantTime = 0;
				event.getEntityLiving().attackEntityFrom(DamageSource.causeIndirectMagicDamage(event.getEntityLiving().getRevengeTarget(),
				event.getEntityLiving().getRevengeTarget()), event.getAmount());
				event.getEntityLiving().hurtResistantTime = 0;
				event.setAmount(0.0F);
				event.setCanceled(true);
				return;
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
	public void livingAttackEvent( LivingAttackEvent event )
	{		
		if ( event.getEntityLiving() == null || event.getSource() == null )
		{
			return;
		}
		
		if ( event.getAmount() > 0.0F )
        {
			if ( this.canParryDamageSource(event.getSource(),event.getEntityLiving()) )
			{
				/* Player is being attacked by an EntityLivingBase */
				if ( event.getEntityLiving() instanceof EntityPlayerMP && event.getSource().getTrueSource() instanceof EntityLivingBase )
				{
					EntityPlayerMP player = (EntityPlayerMP)event.getEntityLiving();
					
					if ( player.getActiveHand().equals(EnumHand.MAIN_HAND) && player.getEntityData().hasKey("isParrying") && player.getEntityData().getBoolean("isParrying") && ConfigurationHandler.isItemClassWhiteList(player.getHeldItemMainhand().getItem()) )
					{
						PacketHandler.instance.sendTo(new PacketParried(), player);
						
						Helpers.applySwingInteria(player);
						
			            player.swingArm(EnumHand.MAIN_HAND);
						double attackDamage = 1.0D + Helpers.getMainhandAttackDamage(player, player.getHeldItemMainhand());
						
						/* Not Parried */
						if ( attackDamage >= 0.0D && event.getAmount() > player.world.rand.nextInt((int)(attackDamage*ConfigurationHandler.parryChanceEffectivness)) )
						{
							player.getCooldownTracker().setCooldown(player.getHeldItemMainhand().getItem(), (int)(event.getAmount()*ConfigurationHandler.critsDisableShield));							
							PacketHandler.instance.sendTo(new PacketParrying(false), player);
						}
						/* Parried */
						else
						{
			                event.setCanceled(true);
			                
				            SoundHandler.playImpactArmorMetalSound(event.getEntityLiving(), SoundHandler.getRandomShieldBlockVolume(), SoundHandler.getRandomImpactPitch());

							player.knockBack(event.getSource().getTrueSource(), ConfigurationHandler.parryKnockbackAmount, (double)-MathHelper.sin(player.rotationYaw * 0.017453292F), (double)(MathHelper.cos(player.rotationYaw * 0.017453292F)));
							
							if ( player.world instanceof WorldServer )
							{
								if ( event.getAmount() >= 1.0D )
								{
									int k = 3 + (int)(event.getAmount() * 0.5F);
									((WorldServer) player.world).spawnParticle(EnumParticleTypes.CRIT, player.posX + (double)-MathHelper.sin(player.rotationYaw * 0.017453292F), player.posY + player.height * 0.6F, player.posZ + (double)(MathHelper.cos(player.rotationYaw * 0.017453292F)), k, 0.2D, 0.1D, 0.2D, 0.25D);
								}
							}
							
							player.getHeldItemMainhand().damageItem(1, player);
						}
						
						/* Knockback the attacking entity, from the player */
						((EntityLivingBase)event.getSource().getTrueSource()).knockBack(player, ConfigurationHandler.parryKnockbackAmount, (double)MathHelper.sin(player.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(player.rotationYaw * 0.017453292F)));
						
			            return;
		            }
				}
				
//				else if ( victim.getActiveHand().equals(EnumHand.MAIN_HAND) && victim.getEntityData().hasKey("isParrying") && victim.getEntityData().getBoolean("isParrying") && configWeapon )
//					{
//						Vec3d vec3d = player.getPositionVector();
		//
//						if ( vec3d != null )
//						{
//							Vec3d vec3d1 = victim.getLook(1.0F);
//							Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(victim.posX, victim.posY, victim.posZ)).normalize();
//							vec3d2 = new Vec3d(vec3d2.x, 0.0D, vec3d2.z);
		//
//							/* Blocked */
//							if ( vec3d2.dotProduct(vec3d1) < 0.0D )
//							{
//								int disableDuration = 0;
//								
//								for ( CustomAxe axe : ConfigurationHandler.axes )
//								{
//									if ( weapon.getItem().getRegistryName().toString().contains(axe.name) )
//									{
//										disableDuration += axe.disableDuration;
//										break;
//									}
//								}
//								
//								if ( isCrit )
//								{
//									disableDuration += damage * ConfigurationHandler.critsDisableShield;
//								}
//								
//								if ( disableDuration > 0 )
//								{
//									if ( victim instanceof EntityPlayer )
//									{
//										((EntityPlayer) victim).getCooldownTracker().setCooldown(activeItem.getItem(), disableDuration);
//									}
//									
//									PacketHandler.instance.sendToServer(new PacketParrying(false));
//								}
//							}
//						}
//					}
			}
			/* Add sounds for blocking - ALL */
			else if ( this.canBlockDamageSource(event.getSource(), event.getEntityLiving()) )
			{
				float f = event.getEntityLiving().getMaxHealth() * 0.25F;
				
				if ( f > 0.0F && event.getAmount() / f > event.getEntityLiving().world.rand.nextFloat() )
				{
		            SoundHandler.blockMetalHeavy(event.getEntityLiving(), SoundHandler.getRandomShieldBlockVolume(), SoundHandler.getRandomImpactPitch());
				}
				else
				{
		            SoundHandler.blockMetalLight(event.getEntityLiving(), SoundHandler.getRandomShieldBlockVolume(), SoundHandler.getRandomImpactPitch());
				}
			}
        }
	}
	
	private boolean canBlockDamageSource(DamageSource damageSourceIn, EntityLivingBase elb)
    {
        if (!damageSourceIn.isUnblockable() && elb.isActiveItemStackBlocking())
        {
            Vec3d vec3d = damageSourceIn.getDamageLocation();

            if (vec3d != null)
            {
                Vec3d vec3d1 = elb.getLook(1.0F);
                Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(elb.posX, elb.posY, elb.posZ)).normalize();
                vec3d2 = new Vec3d(vec3d2.x, 0.0D, vec3d2.z);

                if (vec3d2.dotProduct(vec3d1) < 0.0D)
                {
                    return true;
                }
            }
        }

        return false;
    }
	
	private boolean canParryDamageSource(DamageSource damageSourceIn, EntityLivingBase elb)
    {
		/* getTrueSource() must be null checked before getDamageLocation() is called, as it uses damageSourceEntity which can be null */
    	/* new Vec3d(this.damageSourceEntity.posX, this.damageSourceEntity.posY, this.damageSourceEntity.posZ) */
		
        if ( !damageSourceIn.isUnblockable() && !damageSourceIn.isProjectile() && !damageSourceIn.isMagicDamage() && damageSourceIn.getTrueSource() != null )
        {
            Vec3d vec3d = damageSourceIn.getDamageLocation();

            if ( vec3d != null )
            {
                Vec3d vec3d1 = elb.getLook(1.0F);
                Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(elb.posX, elb.posY, elb.posZ)).normalize();
                vec3d2 = new Vec3d(vec3d2.x, 0.0D, vec3d2.z);

                if (vec3d2.dotProduct(vec3d1) < 0.0D)
                {
                    return true;
                }
            }
        }

        return false;
    }
	
	/**
	 * LivingDamageEvent is fired just before damage is applied to entity.<br>
	 * At this point armor, potion and absorption modifiers have already been applied to damage - this is FINAL value.<br>
	 * Also note that appropriate resources (like armor durability and absorption extra hearths) have already been consumed.<br>
	 * This event is fired whenever an Entity is damaged in
	 * {@link EntityLivingBase#damageEntity(DamageSource, float)} and
	 * {@link EntityPlayer#damageEntity(DamageSource, float)}.<br>
	 * <br>
	 * This event is fired via the {@link ForgeHooks#onLivingDamage(EntityLivingBase, DamageSource, float)}.<br>
	 * <br>
	 * {@link #source} contains the DamageSource that caused this Entity to be hurt. <br>
	 * {@link #amount} contains the final amount of damage that will be dealt to entity. <br>
	 * <br>
	 * This event is {@link Cancelable}.<br>
	 * If this event is canceled, the Entity is not hurt. Used resources WILL NOT be restored.<br>
	 * <br>
	 * This event does not have a result. {@link HasResult}<br>
	 * @see LivingHurtEvent
	 **/
	/* DAMAGE EVENT -> AFTER CALCULATIONS */
	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
	public void livingHurtLow(LivingDamageEvent event)
	{
		if ( ConfigurationHandler.gourmandEnchantmentEnabled )
		{
			if (event.getEntityLiving() instanceof EntityPlayer)
			{
				EntityPlayer player = (EntityPlayer) event.getEntityLiving();
		
				if (EnchantmentHelper.getMaxEnchantmentLevel(BetterCombatEnchantments.GOURMAND, player) > 0)
				{
					int   hunger = player.getFoodStats().getFoodLevel();
					float saturation = player.getFoodStats().getSaturationLevel();
					
					float damage = event.getAmount();
					
					if ( damage > hunger + saturation )
					{
						event.setAmount(damage - hunger - saturation);
						player.getFoodStats().setFoodLevel(0);
						player.getFoodStats().setFoodSaturationLevel(0.0F);
					}
					else
					{
						int   intDamage = (int)damage;
						float floatDamage = damage - intDamage;

						hunger -= intDamage;
						saturation -= floatDamage;
						
						if ( saturation < 0.0F )
						{
							hunger--;
							saturation++;
						}

						event.setAmount(0.0F);
						player.getFoodStats().setFoodLevel(hunger);
						player.getFoodStats().setFoodSaturationLevel(saturation);
					}
				}
			}
		}
	}

	@Nullable
	private BlockPos getRandPos(World worldIn, Entity entityIn, int horizontalRange, int verticalRange)
	{
		BlockPos blockpos = new BlockPos(entityIn);
		int i = blockpos.getX();
		int j = blockpos.getY();
		int k = blockpos.getZ();
		float f = (float) (horizontalRange * horizontalRange * verticalRange * 2);
		BlockPos blockpos1 = null;
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

		for (int l = i - horizontalRange; l <= i + horizontalRange; ++l)
		{

			for (int i1 = j - verticalRange; i1 <= j + verticalRange; ++i1)
			{

				for (int j1 = k - horizontalRange; j1 <= k + horizontalRange; ++j1)
				{
					blockpos$mutableblockpos.setPos(l, i1, j1);
					IBlockState iblockstate = worldIn.getBlockState(blockpos$mutableblockpos);

					if (iblockstate.getMaterial() == Material.WATER)
					{
						float f1 = (float) ((l - i) * (l - i) + (i1 - j) * (i1 - j) + (j1 - k) * (j1 - k));

						if (f1 < f)
						{
							f = f1;
							blockpos1 = new BlockPos(blockpos$mutableblockpos);
						}

					}

				}

			}

		}

		return blockpos1;
	}
	
	@SubscribeEvent
	public void adjustBowTicksStart(LivingEntityUseItemEvent.Start event)
	{
		if (event.getEntityLiving() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			ItemStack heldItem = player.getHeldItemMainhand();

			if (!(heldItem.getItem() instanceof ItemBow))
			{
				heldItem = player.getHeldItemOffhand();
			}

			if (heldItem.getItem() instanceof ItemBow)
			{

				for (CustomBow CustomBow : ConfigurationHandler.bows)
				{

					if (CustomBow.bow == heldItem.getItem())
					{

						if (CustomBow.additionalDrawSpeed > 0)
						{
							event.setDuration(event.getDuration() + CustomBow.additionalDrawSpeed);
						}
						else if (CustomBow.additionalDrawSpeed < -6)
						{
							event.setDuration(event.getDuration() + 6 + CustomBow.additionalDrawSpeed);
							// int i = -CustomBow.additionalDrawSpeed - 6;
							//
							// while ( i-- > 0 )
							// {
							// Reflections.tickHeldBow(event.getEntityLiving());
							// }
						}

					}

				}

			}

		}

	}

	@SubscribeEvent
	public void adjustBowTicks(LivingEntityUseItemEvent event)
	{

		if (event.getEntityLiving() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			ItemStack activeItem = player.getActiveItemStack();

			if (activeItem.getItem() instanceof ItemBow)
			{

				for (CustomBow CustomBow : ConfigurationHandler.bows)
				{

					if (CustomBow.bow == activeItem.getItem())
					{

						if (CustomBow.additionalDrawSpeed < 0)
						{
							int mod;

							switch (CustomBow.additionalDrawSpeed)
							{
							case -1:
							{
								mod = 15;
								break;
							}
							case -2:
							{
								mod = 10;
								break;
							}
							case -3:
							{
								mod = 6;
								break;
							}
							case -4:
							{
								mod = 5;
								break;
							}
							case -5:
							{
								mod = 4;
								break;
							}
							default:
							{
								mod = 3;
								break;
							}
							}

							if ((activeItem.getMaxItemUseDuration() - event.getDuration()) % mod == 0)
							{
								event.setDuration(event.getDuration() - 1);
								// Reflections.tickHeldBow(event.getEntityLiving());
							}

						}

					}

				}

			}

		}

	}
	//
	// private int partialTick = 0;
	// public static int onItemUseTick(EntityLivingBase entity, ItemStack item, int
	// duration)
	// {
	// LivingEntityUseItemEvent event = new LivingEntityUseItemEvent.Tick(entity,
	// item, duration);
	// return MinecraftForge.EVENT_BUS.post(event) ? -1 : event.getDuration();
	// }

//	@SubscribeEvent( receiveCanceled = true )
//	public void onLivingUpdate( LivingEvent.LivingUpdateEvent event )
//	{
//		if ( event.getEntityLiving() instanceof EntityPlayer )
//		{
//			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
//			IOffHandAttack oha = player.getCapability(OFFHAND_CAP, null);
//			CapabilityOffhandCooldown cof = player.getCapability(TUTO_CAP, null);
//			Helpers.execNullable(oha, IOffHandAttack::tick);
//
//			if ( cof != null )
//			{
//				cof.tick();
//
//				if ( offhandCooldown > 0 )
//				{
//					cof.setOffhandCooldown(offhandCooldown);
//
//					if ( !player.world.isRemote )
//					{
//						cof.sync();
//					}
//
//					offhandCooldown = 0;
//				}
//			}
//		}
//	}

//	@SuppressWarnings( "rawtypes" )
//	@SubscribeEvent
//	public void onEntityConstruct( AttachCapabilitiesEvent event )
//	{
//
//		if ( event.getGenericType() != Entity.class )
//		{
//			return;
//		}
//
//		if ( event.getObject() instanceof EntityPlayer )
//		{
//			event.addCapability(new ResourceLocation(Reference.MOD_ID, "TUTO_CAP"), new CapabilityOffhandCooldown((EntityPlayer) event.getObject()));
//			event.addCapability(new ResourceLocation(Reference.MOD_ID, "IOffHandAttack"), new ICapabilitySerializable()
//			{
//				IOffHandAttack inst = EventHandlers.OFFHAND_CAP.getDefaultInstance();
//
//
//				@Override
//				public boolean hasCapability( Capability<?> capability, EnumFacing facing )
//				{
//					return capability == EventHandlers.OFFHAND_CAP;
//				}
//
//
//				@Override
//				public <T> T getCapability( Capability<T> capability, EnumFacing facing )
//				{
//					return capability == EventHandlers.OFFHAND_CAP ? EventHandlers.OFFHAND_CAP.cast(this.inst) : null;
//				}
//
//
//				@Override
//				public NBTPrimitive serializeNBT()
//				{
//					return (NBTPrimitive) EventHandlers.OFFHAND_CAP.getStorage().writeNBT(EventHandlers.OFFHAND_CAP, this.inst, null);
//				}
//
//
//				@Override
//				public void deserializeNBT( NBTBase nbt )
//				{
//					EventHandlers.OFFHAND_CAP.getStorage().readNBT(EventHandlers.OFFHAND_CAP, this.inst, null, nbt);
//				}
//			});
//		}
//
//	}

	@SubscribeEvent
	public void onEntitySpawn(EntityJoinWorldEvent event)
	{
		Entity entity = event.getEntity();
		World world = event.getWorld();

		if (world == null || entity == null)
		{
			return;
		}

		if (entity instanceof EntityArrow)
		{
			EntityArrow arrow = (EntityArrow) entity;

			if (arrow.shootingEntity instanceof EntityLivingBase)
			{
				EntityLivingBase p = (EntityLivingBase) arrow.shootingEntity;

				try
				{
					Item item = p.getHeldItem(p.getActiveHand()).getItem();

					/* BOWS */
					// if ( item instanceof ItemBow || item.getClass().getSimpleName().equals("ItemCrossbow") )
					{
						String name = item.getRegistryName().toString();

						for (CustomBow CustomBow : ConfigurationHandler.bows)
						{
							if (CustomBow.bow == item)
							{

								if (CustomBow.velocity != 1.0D)
								{
									arrow.motionX *= CustomBow.velocity;
									arrow.motionY *= CustomBow.velocity;
									arrow.motionZ *= CustomBow.velocity;
									arrow.velocityChanged = true;
								}

								if (CustomBow.damage != 1.0D)
								{
									double damage = arrow.getDamage() * CustomBow.damage;

									if (damage < 0.0D)
									{
										damage = 0.0D;
									}

									arrow.setDamage(damage);
								}

								/* If the bow firing the arrow contains silver, such as modid:bow_silver */
								if (name.contains("silver"))
								{
									arrow.addTag("silver");
								}

								return;
							}
						}
					}
				}
				catch (Exception e)
				{
					
				}
			}
		}
//		else if ( isBolt(entity.getClass()) )
//		{
//			
//		}
	}
	
//	private boolean isBolt(Class<?> clazz)
//	{
//	    while (clazz != null)
//	    {
//	        if (clazz.getSimpleName().equals("ItemBolt"))
//	        {
//	            return true;
//	        }
//	        
//	        clazz = clazz.getSuperclass();
//	        
//	        if ( clazz != null )
//	        {
//	        	if (clazz.getSimpleName().equals("ItemBolt"))
//		        {
//	        		Field field = ObfuscationReflectionHelper.findField(clazz, "baseDamage");
//					Float baseDamage = (Float) field.get(event.getEntity());
//		        }
//	        }
//	    }
//	    return false;
//	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void arrowImpact(ProjectileImpactEvent event)
	{
		/* NO FIRE WHEN BLOCKING PROJECTILES */
		if (ConfigurationHandler.blockFireProjectiles && event.getEntity() != null && event.getEntity().isBurning() && event.getRayTraceResult().entityHit instanceof EntityLivingBase)
		{
			EntityLivingBase victim = (EntityLivingBase) (event.getRayTraceResult().entityHit);

			if (victim.isActiveItemStackBlocking())
			{
				Vec3d vec3d = event.getEntity().getPositionVector();

				if (vec3d != null)
				{
					Vec3d vec3d1 = victim.getLook(1.0F);
					Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(victim.posX, victim.posY, victim.posZ)).normalize();
					vec3d2 = new Vec3d(vec3d2.x, 0.0D, vec3d2.z);

					if (vec3d2.dotProduct(vec3d1) < 0.0D)
					{
						event.getEntity().extinguish();
					}
				}
			}
		}

		if (event.getEntity() instanceof EntityArrow)
		{
			EntityArrow arrow = (EntityArrow) (event.getEntity());

			if (event.getRayTraceResult().entityHit instanceof EntityLivingBase)
			{
				EntityLivingBase victim = (EntityLivingBase) (event.getRayTraceResult().entityHit);

				/* Ignore effects when blocked */
				if ( victim.isActiveItemStackBlocking() )
				{
					Vec3d vec3d = event.getEntity().getPositionVector();

					if (vec3d != null)
					{
						Vec3d vec3d1 = victim.getLook(1.0F);
						Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(victim.posX, victim.posY, victim.posZ)).normalize();
						vec3d2 = new Vec3d(vec3d2.x, 0.0D, vec3d2.z);

						if (vec3d2.dotProduct(vec3d1) < 0.0D)
						{
							return;
						}
					}
				}
				
				for (String t : arrow.getTags())
				{

					if (t.equals("silver"))
					{

						if (ConfigurationHandler.rangedSilverDamageMultiplier != 1.0F && victim.isEntityUndead())
						{
							arrow.setDamage(arrow.getDamage() * ConfigurationHandler.rangedSilverDamageMultiplier);
							this.playSilverArrowEffect(arrow);
						}

					}
					else if (t.contains("lightning~"))
					{
						try
						{
							/* Integer i = Integer.valueOf(t.substring(t.indexOf('~')+1)); */
							Integer i = Integer.valueOf(t.substring(10));
							EnchantmentLightning.doLightning(arrow.shootingEntity, victim, i);
							arrow.setDead();
						}
						catch (Exception error)
						{

						}
					}
					else if (t.equals("webbing"))
					{
						try
						{
							// Integer i = Integer.valueOf(t.substring(8));
							arrow.setKnockbackStrength(0);
							EnchantmentWebbing.doWebbing(arrow, arrow.shootingEntity, victim);
							arrow.setDead();
						}
						catch (Exception error)
						{

						}
					}
					else if (t.contains("instant_harming~"))
					{
						try
						{
							Integer i = Integer.valueOf(t.substring(16));

							if (victim.isEntityUndead())
							{

								if (ConfigurationHandler.healingArrowNoDamage)
								{
									arrow.setDamage(0.0D);
								}

								float healAmount = i * ConfigurationHandler.healingArrowAmplifier;
								victim.heal(healAmount);
								playHealEffect(victim, Math.round(healAmount));
							}
							else
							{
								if (arrow.shootingEntity == null)
								{
									victim.hurtResistantTime = 0;
									victim.attackEntityFrom(DamageSource.MAGIC,
									i * ConfigurationHandler.harmingArrowAmplifier);
									victim.hurtResistantTime = 0;
								}
								else
								{
									victim.hurtResistantTime = 0;
									victim.attackEntityFrom(
									DamageSource.causeIndirectMagicDamage(arrow.shootingEntity,
									arrow.shootingEntity),
									i * ConfigurationHandler.harmingArrowAmplifier);
									victim.hurtResistantTime = 0;
								}

							}

						}
						catch (Exception error)
						{

						}
					}
					else if (t.contains("instant_healing~"))
					{
						try
						{
							Integer i = Integer.valueOf(t.substring(16));

							if (victim.isEntityUndead())
							{
								if (arrow.shootingEntity == null)
								{
									victim.hurtResistantTime = 0;
									victim.attackEntityFrom(DamageSource.MAGIC,
									i * ConfigurationHandler.harmingArrowAmplifier);
									victim.hurtResistantTime = 0;
								}
								else
								{
									victim.hurtResistantTime = 0;
									victim.attackEntityFrom(
									DamageSource.causeIndirectMagicDamage(arrow.shootingEntity,
									arrow.shootingEntity),
									i * ConfigurationHandler.harmingArrowAmplifier);
									victim.hurtResistantTime = 0;
								}
							}
							else
							{
								if (ConfigurationHandler.healingArrowNoDamage)
								{
									arrow.setDamage(0.0D);
								}

								float healAmount = i * ConfigurationHandler.healingArrowAmplifier;
								victim.heal(healAmount);
								playHealEffect(victim, Math.round(healAmount));
							}

						}
						catch (Exception error)
						{
						}

					}

				}

				/* TIPPED ARROWS */
				if (arrow instanceof EntityTippedArrow && ConfigurationHandler.tippedArrowFix)
				{
					EntityTippedArrow tippedArrow = (EntityTippedArrow) arrow;

					try
					{
						Field field = ObfuscationReflectionHelper.findField(tippedArrow.getClass(), "potion");
						PotionType potion = (PotionType) field.get(tippedArrow);

						if (potion == PotionTypes.HEALING || potion == PotionTypes.STRONG_HEALING)
						{
							try
							{
								Integer i = Integer.valueOf(potion.getEffects().get(0).getAmplifier()); // Integer i =
																										// Integer.valueOf(t.substring(t.indexOf('~')+1));
								field.set(tippedArrow, PotionTypes.EMPTY);

								if (victim.isEntityUndead())
								{
									if (arrow.shootingEntity == null)
									{
										victim.hurtResistantTime = 0;
										victim.attackEntityFrom(DamageSource.MAGIC,
										i * ConfigurationHandler.harmingArrowAmplifier);
										victim.hurtResistantTime = 0;
									}
									else
									{
										victim.hurtResistantTime = 0;
										victim.attackEntityFrom(
										DamageSource.causeIndirectMagicDamage(arrow.shootingEntity,
										arrow.shootingEntity),
										i * ConfigurationHandler.harmingArrowAmplifier);
										victim.hurtResistantTime = 0;
									}

								}
								else
								{

									if (ConfigurationHandler.healingArrowNoDamage)
									{
										arrow.setDamage(0.0D);
									}

									float healAmount = i * ConfigurationHandler.healingArrowAmplifier;
									victim.heal(healAmount);
									playHealEffect(victim, Math.round(healAmount));
								}

							}
							catch (Exception error)
							{
							}

						}
						else if (potion == PotionTypes.HARMING || potion == PotionTypes.STRONG_HARMING)
						{
							try
							{
								Integer i = Integer.valueOf(potion.getEffects().get(0).getAmplifier()); // Integer i =
																										// Integer.valueOf(t.substring(t.indexOf('~')+1));
								field.set(tippedArrow, PotionTypes.EMPTY);

								if (victim.isEntityUndead())
								{
									if (arrow.shootingEntity == null)
									{
										victim.hurtResistantTime = 0;
										victim.attackEntityFrom(DamageSource.MAGIC,
										i * ConfigurationHandler.healingArrowAmplifier);
										victim.hurtResistantTime = 0;
									}
									else
									{
										victim.hurtResistantTime = 0;
										victim.attackEntityFrom(
										DamageSource.causeIndirectMagicDamage(arrow.shootingEntity,
										arrow.shootingEntity),
										i * ConfigurationHandler.healingArrowAmplifier);
										victim.hurtResistantTime = 0;
									}

								}
								else
								{

									if (ConfigurationHandler.healingArrowNoDamage)
									{
										arrow.setDamage(0.0D);
									}

									float healAmount = i * ConfigurationHandler.healingArrowAmplifier;
									victim.heal(healAmount);
									playHealEffect(victim, Math.round(healAmount));
								}

							}
							catch (Exception error)
							{
							}

						}
						else if (potion == PotionTypes.REGENERATION || potion == PotionTypes.LONG_REGENERATION
						|| potion == PotionTypes.STRONG_REGENERATION)
						{
							try
							{
								if (victim.isEntityUndead())
								{
								}
								else
								{
									if (ConfigurationHandler.healingArrowNoDamage)
									{
										arrow.setDamage(0.0D);
									}
								}

							}
							catch (Exception error)
							{
							}
						}

					}
					catch (Exception e)
					{
					}
				}

				if (ConfigurationHandler.dragonboneBowWitherDamage > 0.0F)
				{
					if (arrow.getClass().getSimpleName().equals("EntityDragonArrow"))
					{
						victim.hurtResistantTime = 0;
						victim.attackEntityFrom(DamageSource.WITHER, ConfigurationHandler.dragonboneBowWitherDamage);
						victim.hurtResistantTime = 0;
						this.playDragonEffect(arrow);
						arrow.setDead();
					}
				}

				if (ConfigurationHandler.playArrowHitSound)
				{
					if (arrow.shootingEntity instanceof EntityPlayer)
					{
						Entity player = (EntityPlayer) arrow.shootingEntity;

						if (player == victim && arrow.getClass().toString().equals("EntityBoomerang"))
						{

						}
						else
						{
//							if (arrow.getIsCritical())
//							{
//								player.world.playSound(null, player.posX, player.posY, player.posZ,
//								SoundHandler.IMPACT_RANGED_0, player.getSoundCategory(), 0.5F * ConfigurationHandler.bowThudSoundVolume, 0.9F + player.world.rand.nextFloat() * 0.2F);
//							}
							
							if ( !arrow.world.isRemote ) SoundHandler.playSound(player, SoundHandler.IMPACT_RANGED_0, 0.5F * ConfigurationHandler.bowStrikeSoundVolume, 0.9F + player.world.rand.nextFloat() * 0.2F);
						}

					}

				}

			}
			else
			{
				for (String t : arrow.getTags())
				{
					if (t.contains("lightning~"))
					{
						try
						{
							Integer i = Integer.valueOf(t.substring(10));
							EnchantmentLightning.doLightning(arrow.shootingEntity, arrow, i);
							arrow.setDead();
						}
						catch (Exception error)
						{
						}
					}
				}
			}
		}
		else if ( event.getEntity() instanceof EntityPotion )
		{
			EntityPotion entityPotion = (EntityPotion) event.getEntity();

			BlockPos pos = event.getRayTraceResult().getBlockPos();

			if ( pos == null )
			{
				pos = event.getRayTraceResult().entityHit.getPosition();
			}
			
			if ( pos == null )
			{
				return;
			}

			if ( ConfigurationHandler.extraSplashPotionWidth > 0.0D || ConfigurationHandler.extraSplashPotionHeight > 0.0D )
			{
				AxisAlignedBB axisalignedbb0 = entityPotion.getEntityBoundingBox().grow(4.0D, 2.0D, 4.0D);
				AxisAlignedBB axisalignedbb1 = entityPotion.getEntityBoundingBox().grow(4.0D+ConfigurationHandler.extraSplashPotionWidth, 2.0D+ConfigurationHandler.extraSplashPotionHeight, 4.0D+ConfigurationHandler.extraSplashPotionWidth);
				
		        List<EntityLivingBase> list0 = entityPotion.world.<EntityLivingBase>getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb0);
		        List<EntityLivingBase> list1 = entityPotion.world.<EntityLivingBase>getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb1);
	
				list1.removeAll(list0);
	
		        if ( !list1.isEmpty() )
		        {
		            for ( EntityLivingBase entitylivingbase : list1 )
		            {
		                if ( entitylivingbase.canBeHitWithPotion() )
		                {
		                    double d0 = entityPotion.getDistanceSq(entitylivingbase);
		                    double r = 4.0D + ConfigurationHandler.extraSplashPotionWidth + ConfigurationHandler.extraSplashPotionHeight; r *= r;
		                    
		                    if ( d0 < r )
		                    {
		                        double d1 = 1.0D - Math.sqrt(d0) / 4.0D;
	
		                        if ( entitylivingbase == event.getRayTraceResult().entityHit )
		                        {
		                            d1 = 1.0D;
		                        }
	
		                        for ( PotionEffect potioneffect : PotionUtils.getEffectsFromStack(entityPotion.getPotion()) )
		                        {
		                            Potion potion = potioneffect.getPotion();
	
		                            if ( potion.isInstant() )
		                            {
		                                potion.affectEntity(entityPotion, entityPotion.getThrower(), entitylivingbase, potioneffect.getAmplifier(), d1);
		                            }
		                            else
		                            {
		                                int i = (int)(d1 * (double)potioneffect.getDuration() + 0.5D);
	
		                                if (i > 20)
		                                {
		                                    entitylivingbase.addPotionEffect(new PotionEffect(potion, i, potioneffect.getAmplifier(), potioneffect.getIsAmbient(), potioneffect.doesShowParticles()));
		                                }
		                            }
		                        }
		                    }
		                }
		            }
		        }
			}
		}
	}
	
//	private boolean canPotionHit(EntityLivingBase elb, EntityPotion potion)
//	{
//        return ( elb.world.rayTraceBlocks(new Vec3d(elb.posX, elb.posY, elb.posZ), new Vec3d(potion.posX, potion.posY+potion.getEyeHeight(), potion.posZ), false, true, false) == null ||
//        		 elb.world.rayTraceBlocks(new Vec3d(elb.posX, elb.posY+elb.height, elb.posZ), new Vec3d(potion.posX, potion.posY+potion.getEyeHeight(), potion.posZ), false, true, false) == null );
//	}

	public void playDragonEffect(Entity e)
	{

		if (e.world instanceof WorldServer)
		{

			for (int i = 12; i > 0; i--)
			{
				((WorldServer) e.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
				e.posX + e.world.rand.nextGaussian() * 0.12D, e.posY + e.world.rand.nextGaussian() * 0.18D,
				e.posZ + e.world.rand.nextGaussian() * 0.12D, 1, e.world.rand.nextGaussian() * 0.06D,
				e.world.rand.nextGaussian() * 0.06D, e.world.rand.nextGaussian() * 0.06D,
				e.world.rand.nextDouble() * 0.08D, new int[0]);
			}

			for (int i = 4; i > 0; i--)
			{
				((WorldServer) e.world).spawnParticle(EnumParticleTypes.SMOKE_LARGE,
				e.posX + e.world.rand.nextGaussian() * 0.12D, e.posY + e.world.rand.nextGaussian() * 0.18D,
				e.posZ + e.world.rand.nextGaussian() * 0.12D, 1, e.world.rand.nextGaussian() * 0.06D,
				e.world.rand.nextGaussian() * 0.06D, e.world.rand.nextGaussian() * 0.06D,
				e.world.rand.nextDouble() * 0.08D, new int[0]);
			}

		}

	}

	public void playSilverArrowEffect(Entity e)
	{

		if (e.world instanceof WorldServer)
		{

			for (int i = 12; i > 0; i--)
			{
				((WorldServer) e.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC,
				e.posX + e.world.rand.nextGaussian() * e.width, e.posY + e.world.rand.nextGaussian() * e.height,
				e.posZ + e.world.rand.nextGaussian() * e.width, 1, e.world.rand.nextGaussian() * 0.01D,
				e.world.rand.nextGaussian() * 0.01D, e.world.rand.nextGaussian() * 0.01D,
				e.world.rand.nextDouble() * 0.02D, new int[0]);
			}

		}

	}

	public static void playSilverArmorEffect(Entity e)
	{

		if (e.world instanceof WorldServer)
		{

			for (int i = 12; i > 0; i--)
			{
				((WorldServer) e.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC,
				e.posX + e.world.rand.nextGaussian() * e.width, e.posY + e.world.rand.nextGaussian() * e.height,
				e.posZ + e.world.rand.nextGaussian() * e.width, 1, e.world.rand.nextGaussian() * 0.06D,
				e.world.rand.nextGaussian() * 0.06D, e.world.rand.nextGaussian() * 0.06D,
				e.world.rand.nextDouble() * 0.06D, new int[0]);
			}

		}

	}

	public static void playHealEffect(Entity e, int amount)
	{
		if (e.world instanceof WorldServer)
		{
			for (int i = 0; i < amount; i++)
			{
				double d0 = e.world.rand.nextGaussian() * 0.02D;
				double d1 = e.world.rand.nextGaussian() * 0.02D;
				double d2 = e.world.rand.nextGaussian() * 0.02D;
				((WorldServer) e.world).spawnParticle(EnumParticleTypes.HEART,
				e.posX + (double) (e.world.rand.nextFloat() * e.width * 2.0F) - (double) e.width,
				e.posY + 0.5D + (double) (e.world.rand.nextFloat() * e.height),
				e.posZ + (double) (e.world.rand.nextFloat() * e.width * 2.0F) - (double) e.width, d0, d1, d2);
			}
		}
	}
}