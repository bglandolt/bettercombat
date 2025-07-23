package bettercombat.mod.client;

import static com.elenai.elenaidodge2.api.FeathersHelper.getFeatherLevel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import bettercombat.mod.network.PacketHandler;
import bettercombat.mod.network.server.PacketBreakBlock;
import bettercombat.mod.network.server.PacketFastEquip;
import bettercombat.mod.network.server.PacketMainhandAttack;
import bettercombat.mod.network.server.PacketOffhandAttack;
import bettercombat.mod.network.server.PacketOnItemUse;
import bettercombat.mod.network.server.PacketParrying;
import bettercombat.mod.network.server.PacketShieldBash;
import bettercombat.mod.network.server.PacketStopActiveHand;
import bettercombat.mod.server.CommonProxy;
import bettercombat.mod.util.ConfigurationHandler;
import bettercombat.mod.util.ConfigurationHandler.ConfigWeapon;
import bettercombat.mod.util.ConfigurationHandler.CustomShield;
import bettercombat.mod.util.ConfigurationHandler.CustomSword;
import bettercombat.mod.util.ConfigurationHandler.WeaponProperty;
import bettercombat.mod.util.Helpers;
import bettercombat.mod.util.Reflections;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockGrassPath;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EventHandlersClient
{
	public EventHandlersClient()
	{
		this.mc = Minecraft.getMinecraft();
	}

	public final Minecraft        mc;

	public ItemStack              itemStackMainhand      = ItemStack.EMPTY;
	public ItemStack              itemStackOffhand       = ItemStack.EMPTY;
	
	public int              	  itemStackMainhandSlot	 = -1;
	public int              	  itemStackOffhandSlot	 = -1;

	public final BetterCombatHand betterCombatMainhand   = new BetterCombatHand();
	public final BetterCombatHand betterCombatOffhand    = new BetterCombatHand();

	/* Ticker for offhand cooldown. If offhand cooldown is 0, the attack is ready */
	public int                    offhandCooldown        = 0;
	
	/* Ticker for mainhand cooldown. If mainhand cooldown is 0, the attack is ready */
	public int                    mainhandCooldown       = 0;

	public static final String    EMPTY                  = "";
	public static final String    BLANK_SPACE            = " ";
	public static final String    SW_DASH                = "- ";

	public static final String    attackSpeedString      = I18n.format("attribute.name.generic.attackSpeed");
	public static final String    attackDamageString     = I18n.format("attribute.name.generic.attackDamage");

	public static final String    ATTACK_SPEED_REGEX     = "(([0-9]+\\.*[0-9]*)( *" + attackSpeedString + "))";
	public static final String    ATTACK_DAMAGE_REGEX    = "(([0-9]+\\.*[0-9]*)( *" + attackDamageString + "))";

	public static final String    mainhandString         = I18n.format("item.modifiers.mainhand");
	
	private double hX = 0.0D;
	private double hZ = 0.0D;
	private int tickCounter = 0;
	public float featherLevelBreathingIntensity = 1.0F;
	
	// nnn vvv
	public boolean overwriteLeftClick(boolean checkBlocks)
	{
		/* If the left click counter is less greater than 0 */
		if ( Reflections.getLeftClickCounter(this.mc) > 0 && !this.mc.playerController.isInCreativeMode() )
		{
			/* Cancel left-click! */
			return true;
		}

		EntityPlayerSP player = this.mc.player;

		/* If the player is not valid, */
		if ( this.invalidPlayer(player) )
		{
			/* Cancel left-click! */
			return true;
		}

		if ( ConfigurationHandler.isBlacklisted(this.itemStackMainhand.getItem()) )
		{
			/* Continue with left-click! */
			return false;
		}
		
		/* Check to see if the items have changed */
		if ( this.checkItemstacksChanged(false) )
		{
			return true;
		}

		/* -------------------------------------------- */
		/* 					SHIELD BASH					*/
		/* -------------------------------------------- */

		/*
		 * If the player does not have an active item such as a bow or shield, and does
		 * not have a twohanded weapon,
		 */
		if ( !player.getActiveItemStack().isEmpty() && this.betterCombatMainhand.getWeaponProperty() != WeaponProperty.TWOHAND )
		{
			/* If the player is blocking, */
			if ( Helpers.isHandActive(player, EnumHand.OFF_HAND) )
			{
				Item shield = player.getActiveItemStack().getItem();

				/* If the shield is on cooldown */
				if ( player.getCooldownTracker().hasCooldown(shield) )
				{
					/* Cancel left-click! */
					return true;
				}

				/* 30 is the default cooldown */
				int bashCooldown = -1;

				for ( CustomShield s : ConfigurationHandler.shields )
				{

					if ( shield.equals(s.shield) )
					{
						bashCooldown = s.cooldown;
						break;
					}

				}

				if ( bashCooldown < 0 )
				{
					/* No shield bash */
				}
				else
				{
					/* offhandCooldown used for crosshair cooldown display */
					this.offhandCooldown = bashCooldown;
					this.betterCombatOffhand.attackCooldown = bashCooldown;

					/* Set the internal shield cooldown */
					player.getCooldownTracker().setCooldown(shield, bashCooldown);

					this.sendStopActiveHandPacket();

					/* animate the shield bash */
					this.betterCombatOffhand.setShieldBashing();

					/* Prevent the player from immediately attacking with a weapon after a shield bash */
					if ( Reflections.getLeftClickCounter(this.mc) < 10 ) Reflections.setLeftClickCounter(this.mc, 10);

					/* Cancel left-click! */
					return true;
				}

			}

			/* Cancel left-click, as the player has an active item and should not attack! */
			return true;
		}

//		/* The player started mining */
//		if ( this.startedMining ) // nnn
//		{
//			/* MINING! Continue with left-click! */
//			return false;
//		}

		/*
		 * If this left-click should check blocks, and there is no entity being targeted
		 */
		if ( checkBlocks )
		{
			RayTraceResult mov = this.mc.objectMouseOver;
			
			if ( mov == null )
			{
				/* Cancel left-click! mov should not be null */
				return true;
			}
			
			if ( this.startedMining )
			{
				if ( this.betterCombatMainhand.getSwingTimer() <= 0 && this.mainhandMouseoverHasEntity() )
				{
					/* Attack! */
				}
				else
				{
					return false;
				}
			}
			else
			{
				/* If a the target is a block, */
				if ( mov.typeOfHit == RayTraceResult.Type.BLOCK && mov.getBlockPos() != null && mov.getBlockPos() != BlockPos.ORIGIN )
				{
					/* If the player has a tool in the MAINHAND */
					if ( this.itemStackMainhand.getItem() instanceof ItemTool )
					{
						/* If the MAINHAND is ready to begin a swing animation, */
						if ( this.betterCombatMainhand.getSwingTimer() <= 0 )
						{
							ItemTool tool = (ItemTool) this.itemStackMainhand.getItem();
							
							/*
							 * WOOD = 2.0F STONE = 4.0F IRON = 6.0F DIAMOND = 8.0F GOLD = 12.0F
							 */
							float efficiency = Reflections.getEfficiency(tool);							
							
							/* DIGGING */
							if ( tool instanceof ItemSpade )
							{
								/* Start the MAINHAND mining animation with a set mining speed */
								this.betterCombatMainhand.startMining(MathHelper.clamp(22 - (int)(efficiency*0.33F), 13, 22));
							}
							/* CHOPPING */
							else if ( tool instanceof ItemAxe )
							{
								/* Start the MAINHAND mining animation with a set mining speed */
								this.betterCombatMainhand.startMining(MathHelper.clamp(15 - (int)(efficiency*0.5F), 6, 15));
							}
							/* MINING */
							else if ( tool instanceof ItemPickaxe )
							{ 
								/* Start the MAINHAND mining animation with a set mining speed */
								this.betterCombatMainhand.startMining(MathHelper.clamp(17 - (int)(efficiency*0.5F), 8, 17));
							}
							else
							{
								/* Start the MAINHAND mining animation with a set mining speed */
								this.betterCombatMainhand.startMining(MathHelper.clamp(15 - (int)(efficiency*0.5F), 6, 15));
							}
	
						}
	
						this.startedMining = true;
	
						/* MINING! Continue with left-click! */
						return false;
					}
	
					/* If the player has a MAINHAND attack ready, or the player does NOT have a custom weapon */
					if ( this.isMainhandAttackReady() || !this.betterCombatMainhand.hasConfigWeapon() ) // ClientProxy.AH_INSTANCE.equippedProgressMainhand >= 0.0F
					{
						/* If the MAINHAND is ready to begin a swing animation, */
						if ( this.betterCombatMainhand.getSwingTimer() <= 0 )
						{
							/* Start the MAINHAND mining animation with a set mining speed */
							this.betterCombatMainhand.startMining(12);
						}
	
						this.startedMining = true;
	
						/* MINING! Continue with left-click! */
						return false;
					}
				}
			}
		}
		
		/* If the MAINHAND attack is not ready, */
		if ( !this.isMainhandAttackReady() )
		{
			/* Cancel left-click! */
			return true;
		}

		/* ----------------------------------------- */
		/*				  SWING WEAPON				 */
		/* ----------------------------------------- */

		this.startedMining = false;
		
		/*
		 * Reset the MAINHAND cooldown so the player cannot attack for a period of time
		 */
		this.resetMainhandCooldown(player);

		/* SWING! Initiate the MAINHAND animation for attacking */
		this.betterCombatMainhand.initiateAnimation(this.mainhandCooldown);
		if ( Reflections.getLeftClickCounter(this.mc) < this.mainhandCooldown ) Reflections.setLeftClickCounter(this.mc, this.mainhandCooldown);

		/* Cancel left-click! */
		return true;
	}
	
	public boolean isHittingBlock()
	{
		return this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK;
	}

	private void sendStopActiveHandPacket()
	{
		this.mc.player.stopActiveHand(); // TODO
		PacketHandler.instance.sendToServer(new PacketStopActiveHand());
	}
	
	public void mainhandAttack()
	{
//		if ( !this.itemStackMainhand.isEmpty() && this.itemStackMainhand.getItem() instanceof ItemTool )
//		{
//			/* Hit Block */
//			if ( this.mainhandHitBlock(this.mc.objectMouseOver) )
//			{
//				return;
//			}
//			
//			/* Hit Entity */
//			if ( this.mainhandHitEntity(this.getMainhandMouseover()) )
//			{
//				return;
//			}
//		}
//		else
//		{
//			/* Hit Entity */
//			if ( this.mainhandHitEntity(this.getMainhandMouseover()) )
//			{
//				return;
//			}
//			
//			/* Hit Block */
//			if ( this.mainhandHitBlock(this.mc.objectMouseOver) )
//			{
//				return;
//			}
//		}
		
		/* Hit Entity */
		if ( this.mainhandHitEntity(this.getMainhandMouseover()) )
		{
			return;
		}
		
		/* Hit Block */
		if ( this.mainhandHitBlock(this.mc.objectMouseOver) )
		{
			return;
		}
		
		/* MISS */
		PacketHandler.instance.sendToServer(new PacketMainhandAttack());
	}
	
	private boolean mainhandHitBlock( RayTraceResult rayTraceResults )
	{
		if ( rayTraceResults != null && rayTraceResults.typeOfHit == Type.BLOCK && rayTraceResults.getBlockPos() != null && rayTraceResults.getBlockPos() != BlockPos.ORIGIN )
		{
			if ( !this.itemStackMainhand.isEmpty() && this.toolCanInteractWithBlock(this.itemStackMainhand.getItem()) && this.itemStackMainhand.getItem().onItemUse(this.mc.player, this.mc.player.world, rayTraceResults.getBlockPos(), EnumHand.MAIN_HAND, rayTraceResults.sideHit, 0.0F, 0.0F, 0.0F) == EnumActionResult.SUCCESS )
			{
				// HIT: Block use success
				PacketHandler.instance.sendToServer(new PacketOnItemUse
				(
					rayTraceResults.getBlockPos().getX(),
					rayTraceResults.getBlockPos().getY(),
					rayTraceResults.getBlockPos().getZ(),
					true,
					rayTraceResults.sideHit
				));
				
				return true;
			}
		}
		
		return false;
	}
	
	private boolean mainhandHitEntity( RayTraceResult rayTraceResults )
	{
		if ( rayTraceResults != null && rayTraceResults.entityHit != null )
		{
			PacketHandler.instance.sendToServer(new PacketMainhandAttack(rayTraceResults.entityHit.getEntityId()));
			return true;
		}
		
		return false;
	}

	private boolean toolCanInteractWithBlock(Item item)
	{
		return (ConfigurationHandler.tillingRequiresAnimation && item instanceof ItemHoe) || (ConfigurationHandler.grassPathingRequiresAnimation && item instanceof ItemSpade) || (ConfigurationHandler.strippingBarkRequiresAnimation && item instanceof ItemAxe);
	}

	/* Returns null if there is no mouseover entity */
	private @Nullable RayTraceResult getMainhandMouseover()
	{
		return this.getMouseOverExtended(this.mc.player, Helpers.getMainhandReach(this.mc.player, this.betterCombatMainhand.getAdditionalReach()), this.getExtraSweepWidth(this.betterCombatMainhand.getSweep()), RayTraceResult.Type.ENTITY);
	}

	private boolean mainhandMouseoverHasEntity()
	{
		return this.getMouseOverExtended(this.mc.player, Helpers.getMainhandReach(this.mc.player, this.betterCombatMainhand.getAdditionalReach()), this.getExtraSweepWidth(this.betterCombatMainhand.getSweep()), RayTraceResult.Type.BLOCK) != null;
	}

	public float getExtraSweepWidth(int sweep)
	{
		return sweep > 0 ? MathHelper.clamp((3.0F + sweep) * 0.1F, 0.0F, 0.8F) : 0.0F;
	}

	/*
	 * =============================================================================
	 * =========================================================================
	 */
	/*
	 * =============================================================================
	 * =========================================================================
	 */
	/* ATTACK - OFF HAND */
	/*
	 * =============================================================================
	 * =========================================================================
	 */
	/*
	 * =============================================================================
	 * =========================================================================
	 */

	public void offhandAttack()
	{
		if ( !this.itemStackOffhand.isEmpty() && this.itemStackOffhand.getItem() instanceof ItemTool )
		{
			/* Hit Block */
			if ( this.offhandHitBlock(this.mc.objectMouseOver) )
			{
				return;
			}
			
			/* Hit Entity */
			if ( this.offhandHitBlock(this.getOffhandMouseover()) )
			{
				return;
			}
		}
		else
		{
			/* Hit Entity */
			if ( this.offhandHitEntity(this.getOffhandMouseover()) )
			{
				return;
			}
			
			/* Hit Block */
			if ( this.offhandHitBlock(this.mc.objectMouseOver) )
			{
				return;
			}
		}
		
		/* MISS */
		PacketHandler.instance.sendToServer(new PacketOffhandAttack());
	}
	
	private boolean offhandHitBlock( RayTraceResult rayTraceResults )
	{
		if ( rayTraceResults != null && rayTraceResults.typeOfHit == Type.BLOCK && rayTraceResults.getBlockPos() != null && rayTraceResults.getBlockPos() != BlockPos.ORIGIN )
		{
			if ( !this.itemStackOffhand.isEmpty() && this.toolCanInteractWithBlock(this.itemStackOffhand.getItem()) && this.itemStackOffhand.getItem().onItemUse(this.mc.player, this.mc.player.world, rayTraceResults.getBlockPos(), EnumHand.OFF_HAND, rayTraceResults.sideHit, 0.0F, 0.0F, 0.0F) == EnumActionResult.SUCCESS )
			{
				// HIT: Block use success
				PacketHandler.instance.sendToServer(new PacketOnItemUse
				(
					rayTraceResults.getBlockPos().getX(),
					rayTraceResults.getBlockPos().getY(),
					rayTraceResults.getBlockPos().getZ(),
					true,
					rayTraceResults.sideHit
				));
				
				return true;
			}
		}
		
		return false;
	}
	
	private boolean offhandHitEntity( RayTraceResult rayTraceResults )
	{
		if ( rayTraceResults != null && rayTraceResults.entityHit != null && ConfigurationHandler.rightClickAttackable(this.mc.player, rayTraceResults.entityHit) )
		{
			if ( this.itemStackOffhand.getItem() instanceof ItemShield )
			{
				/* HIT! Send an shield bash packet with a target! */
				PacketHandler.instance.sendToServer(new PacketShieldBash(rayTraceResults.entityHit.getEntityId()));
				return true;
			}
			else
			{
				/* HIT! Send an attack packet with a target! */
				PacketHandler.instance.sendToServer(new PacketOffhandAttack(rayTraceResults.entityHit.getEntityId()));
				return true;
			}
		}
		
		return false;
	}
	
	/* Returns null if there is no mouseover entity */
	private @Nullable RayTraceResult getOffhandMouseover()
	{
		return this.getMouseOverExtended(this.mc.player, Helpers.getOffhandReach(this.mc.player, this.betterCombatOffhand.getAdditionalReach(), this.itemStackOffhand, this.itemStackMainhand), this.getExtraSweepWidth(this.betterCombatOffhand.getSweep()), RayTraceResult.Type.ENTITY);
	}

	/* Returns true if there is a mouseover entity */
	private boolean offhandMouseoverHasEntity()
	{
		return this.getMouseOverExtended(this.mc.player, Helpers.getOffhandReach(this.mc.player, this.betterCombatOffhand.getAdditionalReach(), this.itemStackOffhand, this.itemStackMainhand), this.getExtraSweepWidth(this.betterCombatOffhand.getSweep()), RayTraceResult.Type.BLOCK) != null;
	}

	/*
	 * =============================================================================
	 * =========================================================================
	 */
	/*
	 * =============================================================================
	 * =========================================================================
	 */
	/* SWING THROUGH GRASS */
	/*
	 * =============================================================================
	 * =========================================================================
	 */
	/*
	 * =============================================================================
	 * =========================================================================
	 */

	private boolean cutGrass(BlockPos pos)
	{
		if (pos == null)
		{
			return false;
		}

		Block block = this.mc.player.world.getBlockState(pos).getBlock();

		/* If the block is a plant or grass, */
		if ( block instanceof IPlantable || block instanceof IShearable )
		{
			PacketHandler.instance.sendToServer(new PacketBreakBlock(pos.getX(), pos.getY(), pos.getZ()));
			this.mc.player.world.setBlockToAir(pos); /* For the Client */

			Helpers.sweepParticlesClient(this.mc.player, pos.getX(), pos.getZ());

			while ( this.mc.player.getEntityWorld().getBlockState(pos).getCollisionBoundingBox(this.mc.player.getEntityWorld(), pos) != Block.NULL_AABB )
			{
				block = this.mc.player.world.getBlockState(pos = pos.up()).getBlock();

				if (pos != null && (block instanceof IPlantable || block instanceof IShearable))
				{
					PacketHandler.instance.sendToServer(new PacketBreakBlock(pos.getX(), pos.getY(), pos.getZ()));
					this.mc.player.world.setBlockToAir(pos); /* For the Client */
				}
				else
				{
					break;
				}

			}

			return true;
		}

		return false;
	}

	/*
	 * Return TRUE to overwrite/cancel the default click Return FALSE to use the
	 * default click
	 * 
	 * MAINHAND has priority for using items and interacting
	 */
	public boolean overwriteRightClick()
	{
		/* If the player is not valid, */
		if (this.invalidPlayer(this.mc.player) ) // || Reflections.getRightClickDelayTimer(this.mc) > 0) // 3/20
		{
			/* Cancel right-click! */
			return true;
		}

		/* Check to see if the ItemStacks have changed */
		this.checkItemstacksChanged(false);

		RayTraceResult mov = this.mc.objectMouseOver;

		if (mov == null)
		{
			/* Cancel right-click! mov should not be null */
			return true;
		}

		if (this.itemStackMainhand.isEmpty() && (this.itemStackOffhand.isEmpty() || ConfigurationHandler.isBlacklisted(this.itemStackOffhand.getItem())))
		{
			/* Continue with left-click! */
			return false;
		}

		/*
		 * If the MAINHAND has a TWOHAND weapon, prevent placing blocks, but use the
		 * item
		 */
		if (this.betterCombatMainhand.getWeaponProperty() == WeaponProperty.TWOHAND)
		{

			if (this.itemStackMainhand.getItemUseAction() == EnumAction.NONE)
			{

				/* Only use the MAINHAND */
				if (this.rightClickInteract(EnumHand.MAIN_HAND))
				{
					/* Cancel right-click! Right click timer is only set on success */
					return true;
				}
				else if (this.canParry(false))
				{
					this.parrying = true;
				}

			}

			Reflections.setRightClickDelayTimer(this.mc, 4);

			/* Cancel right-click! */
			return true;
		}

		/* If the OFFHAND has a TWOHAND or MAINHAND weapon, */
		if (this.betterCombatOffhand.getWeaponProperty() == WeaponProperty.TWOHAND || this.betterCombatOffhand.getWeaponProperty() == WeaponProperty.MAINHAND)
		{

			if (mov.typeOfHit == RayTraceResult.Type.BLOCK)
			{
				BlockPos pos = mov.getBlockPos();

				/* If that position is invalid, */
				if (pos == null || pos == BlockPos.ORIGIN)
				{
					/* Cancel right-click, as there was an error! */
					return true;
				}

				Block block = this.mc.player.world.getBlockState(pos).getBlock();

				if (this.useToolsMainhandOnly(this.mc.player, block))
				{
					/* Cancel right-click! */
					return true;
				}

			}

			if (this.itemStackMainhand.getItemUseAction() == EnumAction.NONE)
			{
				/* Only use the MAINHAND */
				if (this.rightClickInteract(EnumHand.MAIN_HAND))
				{
					/* Cancel right-click, but use still place block! */
					return true; // (complete) Bugfix: Cannot place blocks by holding right click when the offhand has a twohand or mainhand weapon
				}
				else if (this.canParry(false))
				{
					this.parrying = true;
				}

			}
			else // (complete) Bugfix: If you put a "Mainhand" only weapon in the offhand, and try to eat something with your main hand (food), it doesn't work
			{
				/* Do not use the offhand because it contains a TWOHAND or MAINHAND weapon */
				if (this.rightClickInteract(EnumHand.MAIN_HAND))
				{
					/* Cancel right-click, but use the item still! */
					return true;
				}

			}

			Reflections.setRightClickDelayTimer(this.mc, 4);

			/* Cancel right-click! */
			return true;
		}

		/* If the MAINHAND has an action, OR if the OFFHAND has an action, */
		if (this.itemStackMainhand.getItemUseAction() != EnumAction.NONE || this.itemStackOffhand.getItemUseAction() != EnumAction.NONE)
		{

			/* If the OFFHAND is an item that can block */
			if ( this.itemStackOffhand.getItemUseAction() == EnumAction.BLOCK )
			{
				if ( !this.isOffhandAttackReady() || (ConfigurationHandler.disableBlockingWhileAttacking && !this.isMainhandAttackReady()) )
				{
					this.sendStopActiveHandPacket();
				}
			}

			/* Continue with right-click and use item! */
			return false;
		}

		/* If targeting a block, and not targeting any entity */
		if ( mov.typeOfHit == RayTraceResult.Type.BLOCK )
		{
			BlockPos pos = mov.getBlockPos();

			/* If that position is invalid, */
			if (pos == null || pos == BlockPos.ORIGIN)
			{
				/* Cancel right-click! */
				return true;
			}

			IBlockState state = this.mc.player.world.getBlockState(pos);
			Block block = state.getBlock();
			
			if ( !this.offhandMouseoverHasEntity() || (this.mc.player.world.isRemote && block.onBlockActivated(this.mc.player.world, pos, state, this.mc.player, EnumHand.OFF_HAND, this.mc.objectMouseOver.sideHit, this.mc.objectMouseOver.sideHit.getFrontOffsetX(), this.mc.objectMouseOver.sideHit.getFrontOffsetY(), this.mc.objectMouseOver.sideHit.getFrontOffsetZ())) )
			{
				/* If the block is a PLANT and the MAINHAND OR OFFHAND has a HOE, */
				if ( (block instanceof IPlantable || block instanceof IShearable) && (this.betterCombatOffhand.hasConfigWeapon() || (this.itemStackMainhand.getItem() instanceof ItemHoe || this.itemStackOffhand.getItem() instanceof ItemHoe)) )
				{
					if (this.itemStackMainhand.getItem() instanceof ItemHoe || this.itemStackOffhand.getItem() instanceof ItemHoe)
					{
						/* Due to other mods, continue with right-click, using the HOE on the PLANT! */
						return false;
					}
				}
				/*
				 * Otherwise, if the player can interact with any hand,
				 */
				else if ( !this.betterCombatMainhand.hasConfigWeapon() && !this.betterCombatOffhand.hasConfigWeapon() )
				{
					/* Continue with right-click, placing blocks! */
					return false;
				}
				/*
				 * Otherwise, if the player can interact with any hand,
				 */
				else if ( this.rightClickInteract(EnumHand.MAIN_HAND) || this.rightClickInteract(EnumHand.OFF_HAND) ) // XXX
				{
					/* Cancel right-click! */
					return true;
				}
				/*
				 * If hands have the ability to interact, such as tilling, pathing, or stripping
				 * bark,
				 */
				else if ( this.useTools(this.mc.player, block) )
				{
					/* Cancel right-click, use tools! */
					return true;
				}
			}
		}
		/* Otherwise, if there are no equipped weapons */
		else if (!this.betterCombatMainhand.hasConfigWeapon() && !this.betterCombatOffhand.hasConfigWeapon())
		{
			/* Continue with right-click! */
			return false;
		}
		/* Otherwise, if there is a target and it is NOT attackable with the OFFHAND, */
		else if (mov.entityHit != null && !ConfigurationHandler.rightClickAttackable(this.mc.player, mov.entityHit))
		{
			/* Continue with right-click, interact with entity! */
			return false;
		}

		/* Continue if the player is sneaking */
		if (this.mc.player.isSneaking() && ConfigurationHandler.sneakingDisablesOffhandAttack)
		{
			/* Continue with right-click! */
			return false;
		}

		/* If the OFFHAND attack is not ready, */
		if (!this.isOffhandAttackReady())
		{
			/* Cancel right-click! */
			return true;
		}

		/* ----------------------------------------- */
		/* SWING WEAPON */
		/* ----------------------------------------- */

		return this.initiateOffhandAttack(this.mc.player);
	}

	private boolean canParry(boolean checkOffhand)
	{
		return this.betterCombatMainhand.canWeaponParry() && this.betterCombatMainhand.hasConfigWeapon() && (!checkOffhand || this.itemStackOffhand.isEmpty()) && this.isMainhandAttackReady() && !this.mc.player.getCooldownTracker().hasCooldown(this.itemStackMainhand.getItem());
	}

	private boolean useTools(EntityPlayerSP player, Block block)
	{

		if (ConfigurationHandler.grassPathingRequiresAnimation)
		{

			if (block instanceof BlockGrass) // || block == Blocks.DIRT
			{

				if (this.itemStackMainhand.getItem() instanceof ItemSpade)
				{

					if (this.isMainhandAttackReady())
					{
						this.overwriteLeftClick(false);
					}

					return true;
				}
				else if (this.itemStackOffhand.getItem() instanceof ItemSpade)
				{

					if (this.isOffhandAttackReady())
					{
						this.initiateOffhandAttack(player);
					}

					return true;
				}

			}

		}

		if (ConfigurationHandler.tillingRequiresAnimation)
		{

			if (block instanceof BlockDirt || block instanceof BlockGrass || block instanceof BlockGrassPath)
			{

				if (this.itemStackMainhand.getItem() instanceof ItemHoe)
				{

					if (this.isMainhandAttackReady())
					{
						this.overwriteLeftClick(false);
					}

					return true;
				}
				else if (this.itemStackOffhand.getItem() instanceof ItemHoe)
				{

					if (this.isOffhandAttackReady())
					{
						this.initiateOffhandAttack(player);
					}

					return true;
				}

			}

		}

		if (ConfigurationHandler.strippingBarkRequiresAnimation)
		{

			if (block instanceof BlockLog)
			{

				if (this.itemStackMainhand.getItem() instanceof ItemAxe)
				{

					if (this.isMainhandAttackReady())
					{
						this.overwriteLeftClick(false);
					}

					return true;
				}
				else if (this.itemStackOffhand.getItem() instanceof ItemAxe)
				{

					if (this.isOffhandAttackReady())
					{
						this.initiateOffhandAttack(player);
					}

					return true;
				}

			}

		}

		/* Continue with right-click! */
		return false;
	}

	private boolean useToolsMainhandOnly(EntityPlayerSP player, Block block)
	{

		if (ConfigurationHandler.grassPathingRequiresAnimation)
		{

			if (block instanceof BlockGrass) // || block == Blocks.DIRT
			{

				if (this.itemStackMainhand.getItem() instanceof ItemSpade)
				{

					if (this.isMainhandAttackReady())
					{
						this.overwriteLeftClick(false);
					}

					return true;
				}

			}

		}

		if (ConfigurationHandler.tillingRequiresAnimation)
		{

			if (block instanceof BlockDirt || block instanceof BlockGrass || block instanceof BlockGrassPath)
			{

				if (this.itemStackMainhand.getItem() instanceof ItemHoe)
				{

					if (this.isMainhandAttackReady())
					{
						this.overwriteLeftClick(false);
					}

					return true;
				}

			}

		}

		if (ConfigurationHandler.strippingBarkRequiresAnimation)
		{

			if (block instanceof BlockLog)
			{

				if (this.itemStackMainhand.getItem() instanceof ItemAxe)
				{

					if (this.isMainhandAttackReady())
					{
						this.overwriteLeftClick(false);
					}

					return true;
				}

			}

		}

		/* Continue with right-click! */
		return false;
	}

	/* Initiate the right click attack, prepares offhandAttack */
	private boolean initiateOffhandAttack(EntityPlayerSP player)
	{
		if (this.betterCombatOffhand.hasConfigWeapon())
		{
			/*
			 * Reset the OFFHAND cooldown so the player cannot attack for a period of time
			 */
			this.resetOffhandCooldown(player);

			/* SWING! Initiate the OFFHAND animation for attacking */
			this.betterCombatOffhand.initiateAnimation(this.offhandCooldown);

			/* Cancel right-click! */
			return true;
		}
		else if (this.canParry(true))
		{
			this.parrying = true;
		}

		/* Continue with right-click, use item! */
		return false;
	}

	private int    parryingTimer = 0;
	public boolean parrying      = false;

	/* PlayerControllerMP */
	private boolean rightClickInteract(EnumHand enumhand)
	{
		if (Reflections.getRightClickDelayTimer(this.mc) > 0)
		{
			/* Return true and cancel */
			return true;
		}
		
		if (!this.mc.playerController.getIsHittingBlock())
		{
			if (!this.mc.player.isRowingBoat())
			{
				ItemStack itemstack = this.mc.player.getHeldItem(enumhand);

				if (this.mc.objectMouseOver != null)
				{
					switch (this.mc.objectMouseOver.typeOfHit)
					{
						case ENTITY:
						{

							if (this.mc.playerController.interactWithEntity(this.mc.player, this.mc.objectMouseOver.entityHit, this.mc.objectMouseOver, enumhand) == EnumActionResult.SUCCESS)
							{
								Reflections.setRightClickDelayTimer(this.mc, 4);
								return true;
							}

							if (this.mc.playerController.interactWithEntity(this.mc.player, this.mc.objectMouseOver.entityHit, enumhand) == EnumActionResult.SUCCESS)
							{
								Reflections.setRightClickDelayTimer(this.mc, 4);
								return true;
							}

							break;
						}
						case BLOCK:
						{
							BlockPos blockpos = this.mc.objectMouseOver.getBlockPos();

							if (this.mc.world.getBlockState(blockpos).getMaterial() != Material.AIR)
							{
								int i = itemstack.getCount();

								EnumActionResult enumactionresult;

								if (this.toolCanInteractWithBlock(itemstack.getItem()))
								{
									/* Do not cancel! */
									return false;
								}
								else
								{
									enumactionresult = this.mc.playerController.processRightClickBlock(this.mc.player, this.mc.world, blockpos, this.mc.objectMouseOver.sideHit, this.mc.objectMouseOver.hitVec, enumhand);
								}

								if (enumactionresult == EnumActionResult.SUCCESS)
								{
									this.mc.player.swingArm(enumhand);

									if (!itemstack.isEmpty() && (itemstack.getCount() != i || this.mc.playerController.isInCreativeMode()))
									{
										this.mc.entityRenderer.itemRenderer.resetEquippedProgress(enumhand);
									}

									Reflections.setRightClickDelayTimer(this.mc, 4);
									return true;
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

				if (itemstack.isEmpty() && (this.mc.objectMouseOver == null || this.mc.objectMouseOver.typeOfHit == RayTraceResult.Type.MISS))
				{
					net.minecraftforge.common.ForgeHooks.onEmptyClick(this.mc.player, enumhand);
					Reflections.setRightClickDelayTimer(this.mc, 4);
					return true;
				}

				if (!itemstack.isEmpty() && this.mc.playerController.processRightClick(this.mc.player, this.mc.world, enumhand) == EnumActionResult.SUCCESS)
				{
					this.mc.entityRenderer.itemRenderer.resetEquippedProgress(enumhand);
					Reflections.setRightClickDelayTimer(this.mc, 4);
					return true;
				}
			}
		}
		return false;
	}

//	@SubscribeEvent( priority = EventPriority.NORMAL, receiveCanceled = true )
//	public void disableShieldWhileAttackingEvent( LivingEntityUseItemEvent event )
//	{
//		if ( event.getEntityLiving() instanceof EntityPlayerSP )
//		{
//			if ( this.mc.player == event.getEntityLiving() )
//			{
//				if ( !this.isMainhandAttackReady() || !this.isOffhandAttackReady() )
//				{
//					Reflections.activeItemStackUseCount((EntityLivingBase)this.mc.player, this.itemStackOffhand.getMaxItemUseDuration());
//					this.mc.player.stopActiveHand();
//					
//					event.setResult(Result.DENY);
//					event.setCanceled(true);
//				}
//			}
//		}
//	}

	/*
	 * =============================================================================
	 * =========================================================================
	 */
	/* KEYBINDS */
	/*
	 * =============================================================================
	 * =========================================================================
	 */

	/*
	 * Used to check if the player started mining, as it will release
	 * click when a weapon is swapped if they have not started mining,
	 * this is to stop players from cheesing by weapon swapping attacks
	 */
	public boolean startedMining = false;
	
	/* If the player is holding down left click */
	public boolean holdingLeftClick = false;

	// private boolean queuedkeyBindAttack = false;
	
	// vvv
	public void stopBreaking()
	{
//		boolean flag = false;
//		
//		if ( this.mc.gameSettings.keyBindAttack.isPressed() || this.mc.gameSettings.keyBindAttack.isKeyDown() )
//		{
//			KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
//			
//			flag = true;
//		}
		
		KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
		
		this.mc.playerController.resetBlockRemoving();

		this.mc.objectMouseOver = new RayTraceResult
		(
		    RayTraceResult.Type.MISS,
		    this.mc.player.getLookVec(),
		    EnumFacing.UP,
		    BlockPos.ORIGIN
		);
		
		// this.queuedkeyBindAttack = flag;
	}
	
	// vvv
	/* For mouse events only! This event only triggers on mouse clicks */
	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true) // XXX
	public void onMouseEvent(MouseEvent event)
	{
		KeyBinding rightClick = this.mc.gameSettings.keyBindUseItem; /* -1 */

		if ( event.isButtonstate() && event.getButton() == rightClick.getKeyCode() + 100 )
		{
			if ( this.overwriteRightClick() )
			{
				/* Cancel the vanilla right-click! */
				event.setResult(Result.DENY);
				event.setCanceled(true);

				if ( Reflections.getRightClickDelayTimer(this.mc) <= 0 )
				{
					Reflections.setRightClickDelayTimer(this.mc, 4);
				}

				/* Sets this.mc.gameSettings.keyBindUseItem.isKeyDown() to true */
				KeyBinding.setKeyBindState(rightClick.getKeyCode(), true);
			}
		}

		KeyBinding leftClick = this.mc.gameSettings.keyBindAttack; /* 0 */

		if ( event.isButtonstate() && event.getButton() == leftClick.getKeyCode() + 100 )
		{
			if ( this.overwriteLeftClick(true) )
			{
				/* Cancel the vanilla left-click attack! */
				event.setResult(Result.DENY);
				event.setCanceled(true);

				/* Sets this.mc.gameSettings.keyBindAttack.isKeyDown() to true */
				KeyBinding.setKeyBindState(leftClick.getKeyCode(), true);
			}
		}
	}
	
	public void stopClick()
	{
		this.stopBreaking();
		
		if ( Reflections.getLeftClickCounter(this.mc) <= 0 )
		{
			Reflections.setLeftClickCounter(this.mc, 4);
		}
	}

	/*
	 * For keypress events only! This event only triggers when clicks are re-bound
	 * to keys
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
	public void onKeyEvent(KeyInputEvent event)
	{
		if ( ClientProxy.fastEquip.isKeyDown() && this.isMainhandAttackReady() && this.isOffhandAttackReady() )
		{
			PacketHandler.instance.sendToServer(new PacketFastEquip());
		}

		/*
		 * keyBindUseItem isPressed and isKeyDown is only true if set to a keybind, not
		 * a click
		 */
		if ( this.mc.gameSettings.keyBindUseItem.isPressed() || this.mc.gameSettings.keyBindUseItem.isKeyDown() )
		{
			if ( this.mc.gameSettings.keyBindUseItem.getKeyCode() != -99 )
			{
				/*
				 * Reflections.unpressKey(this.mc.gameSettings.keyBindUseItem);
				 * this.mc.gameSettings.keyBindUseItem = new KeyBinding("key.use", -99,
				 * "key.categories.gameplay");
				 * this.mc.gameSettings.keyBindUseItem.setToDefault();
				 */

				/* Cancel the vanilla right-click! */
				if ( this.overwriteRightClick() )
				{
					if (Reflections.getRightClickDelayTimer(this.mc) <= 0)
					{
						Reflections.setRightClickDelayTimer(this.mc, 4);
					}
					
					event.setResult(Result.DENY);
					event.setCanceled(true);
				}
			}
		}

		/*
		 * keyBindAttack isPressed and isKeyDown is only true if set to a keybind, not a
		 * click
		 */
		if ( this.mc.gameSettings.keyBindAttack.isPressed() || this.mc.gameSettings.keyBindAttack.isKeyDown() )
		{

			if ( this.mc.gameSettings.keyBindAttack.getKeyCode() != -100 )
			{
				/*
				 * Reflections.unpressKey(this.mc.gameSettings.keyBindAttack);
				 * this.mc.gameSettings.keyBindAttack = new KeyBinding("key.attack", -100,
				 * "key.categories.gameplay");
				 * this.mc.gameSettings.keyBindAttack.setToDefault();
				 */

				/*
				 * Cancel the vanilla left-click attack! This stops the player from changing
				 * their keybind to bypass the MouseEvent to use a vanilla attack!
				 */
				if ( this.overwriteLeftClick(true) )
				{
					event.setResult(Result.DENY);
					event.setCanceled(true);
				}
			}
		}
	}
	
	/* x multiplier */
	public static float calculateFeatherLevelBreathingIntensity(EntityPlayerSP player)
	{
		if ( ConfigurationHandler.elanaiDodgeEnabled )
		{		
			float featherLevelModifier = 3.0F - getFeatherLevel(player) * 0.1F;
		
			float healthLevelModifier = 3.0F - (player.getHealth() / player.getMaxHealth()) * 2.0F;
		
			return featherLevelModifier > healthLevelModifier ? featherLevelModifier : healthLevelModifier;
		}
		else
		{
			return 3.0F - (player.getHealth() / player.getMaxHealth()) * 2.0F;
		}
	}
	
	/* onUpdate */
	@SubscribeEvent( priority = EventPriority.LOW, receiveCanceled = true ) // XXX
	public void tickEventLow(TickEvent.ClientTickEvent event)
	{
		if ( event.phase == TickEvent.Phase.END && this.mc.player != null )
		{			
			this.checkItemstacksChanged(false);
			
	        if ( this.tickCounter++ > 20 )
	        {
	        	if ( Minecraft.getDebugFPS() < 30 )
	    		{
	    			ClientProxy.AH_INSTANCE.partialTicks = 0.66666666F;
	    		}
	    		else
	    		{
	    			ClientProxy.AH_INSTANCE.partialTicks = (Minecraft.getDebugFPS() <= 0) ? 1.0F : (20.0F / Minecraft.getDebugFPS());
	    		}
	        	
//	        	/* Partial Ticks */
//		        float current = Minecraft.getMinecraft().getRenderPartialTicks();
//	        	
//		        float delta = current - this.lastPartialTick;
//		        
//		        if ( delta < 0.0F )
//		        {
//		            delta += 1.0F;
//		        }
//	
//		        ClientProxy.AH_INSTANCE.partialTicks = (ClientProxy.AH_INSTANCE.partialTicks * 0.9F) + (delta * 0.1F);
//		        
//		        this.lastPartialTick = current;
		        
		        /* Breathing */
	        	if ( this.mc.player != null && !this.mc.player.isAddedToWorld() )
	    		{
	            	this.featherLevelBreathingIntensity = calculateFeatherLevelBreathingIntensity(this.mc.player);
	    		}
	        	
	            this.tickCounter = 0;
	        }
			
			/* Sprinting */
			if ( this.mc.player.isSprinting() )
			{
				ClientProxy.AH_INSTANCE.mainhandSprinting = ClientProxy.AH_INSTANCE.equippedProgressMainhand != -1.0F && !AnimationHandler.isMainhandAttacking();
				ClientProxy.AH_INSTANCE.offhandSprinting = ClientProxy.AH_INSTANCE.equippedProgressOffhand != -1.0F && !AnimationHandler.isOffhandAttacking();
			}
			else
			{
				ClientProxy.AH_INSTANCE.mainhandSprinting = false;
				ClientProxy.AH_INSTANCE.offhandSprinting = false;
			}
			
			/* Blocking */
			if ( this.itemStackOffhand.getItem() instanceof ItemShield && Helpers.isHandActive(this.mc.player, EnumHand.OFF_HAND) && ( !ConfigurationHandler.disableBlockingWhileAttacking || this.isMainhandAttackReady() ))
    		{
	    		ClientProxy.AH_INSTANCE.blocking = true;
    		}
    		else
    		{
	    		ClientProxy.AH_INSTANCE.blocking = false;
    		}

			/* Wall-Aware Positioning */
			if ( this.mc.objectMouseOver != null )
			{
				if ( this.mc.objectMouseOver.hitVec != null )
				{
					this.hX = this.mc.player.posX - this.mc.objectMouseOver.hitVec.x;
					this.hZ = this.mc.player.posZ - this.mc.objectMouseOver.hitVec.z;
					
					this.calculateWallAwarePositioning();
				}
				else if (this.mc.objectMouseOver.entityHit != null)
				{
					this.hX = this.mc.player.posX - (this.mc.objectMouseOver.entityHit.posX + this.mc.objectMouseOver.entityHit.width * 0.5D);
					this.hZ = this.mc.player.posZ - (this.mc.objectMouseOver.entityHit.posZ + this.mc.objectMouseOver.entityHit.width * 0.5D);
					
					this.calculateWallAwarePositioning();
				}
			}
			
			// nnn vvv
			/* Lets the player hold down left-click */
			if ( this.mc.gameSettings.keyBindAttack.isPressed() || this.mc.gameSettings.keyBindAttack.isKeyDown() ) // || this.queuedkeyBindAttack )
			{
				// this.queuedkeyBindAttack = false;
				
				if ( this.overwriteLeftClick(true) )
				{
					//this.stopBreaking();
				}
				
		        this.holdingLeftClick = true;
			}
			else
			{
				if ( this.startedMining || this.holdingLeftClick )
				{
					this.stopClick();
				}
				
		        this.startedMining = false;
		        this.holdingLeftClick = false;
			}

			/* Lets the player hold down right-click */
			if ( this.mc.gameSettings.keyBindUseItem.isPressed() || this.mc.gameSettings.keyBindUseItem.isKeyDown() )
			{
				if ( !this.mc.player.isHandActive() )
				{
					this.overwriteRightClick();
				}

				/* This prevents the player from using right-click with a TWOHAND weapon */
				if ( this.betterCombatMainhand.getWeaponProperty() == WeaponProperty.TWOHAND )
				{
					/* Must be set to a number greater than 1, otherwise this will not work! */
					Reflections.setRightClickDelayTimer(this.mc, 2);
				}

			}
			else
			{

				if ( this.parryingTimer > 0 )
				{
					this.parryingTimer--;
				}

				if ( this.parrying )
				{
					this.parrying = false;
					PacketHandler.instance.sendToServer(new PacketParrying(false));
				}

			}

			if ( this.parrying )
			{

				if ( this.mc.player.onGround )
				{
					this.mc.player.setSprinting(false);
					this.mc.player.motionX *= 0.2;
					this.mc.player.motionZ *= 0.2;
					this.mc.player.velocityChanged = true;
				}

				if ( this.parryingTimer < 10 )
				{

					if ( this.parryingTimer++ == 5 )
					{
						PacketHandler.instance.sendToServer(new PacketParrying(true));
					}

				}

			}

			if ( this.betterCombatMainhand.isSwinging() )
			{
				this.betterCombatMainhand.tick();

				if ( this.betterCombatMainhand.isMining() )
				{

				}
				else
				{
					if (this.betterCombatMainhand.damageReady())
					{
						this.mainhandAttack();
					}
					else if (this.betterCombatMainhand.soundReady())
					{
						this.mainhandSwingSound();
					}
				}

				if ( ConfigurationHandler.disableBlockingWhileAttacking && Helpers.isHandActive(this.mc.player, EnumHand.OFF_HAND) )
				{
					this.sendStopActiveHandPacket();
				}

			}
			else if ( this.betterCombatMainhand.equipSoundTimer > 0 && --this.betterCombatMainhand.equipSoundTimer <= 0 )
			{
				this.mainhandEquipSound();

				/*
				 * Disable the offhand equip sound so they do not play at the same time, as it
				 * sounds off
				 */
				if ( this.betterCombatOffhand.equipSoundTimer == 1 )
				{
					this.betterCombatOffhand.equipSoundTimer = -1;
				}

			}

			if ( this.betterCombatOffhand.getSwingTimer() > 0 )
			{
				this.betterCombatOffhand.tick();

				if ( this.betterCombatOffhand.isMining() )
				{

				}
				else
				{
					if ( this.betterCombatOffhand.damageReady() )
					{
						this.offhandAttack();
					}
					else if ( this.betterCombatOffhand.soundReady() )
					{
						this.offhandSwingSound();
					}

				}

				if ( ConfigurationHandler.disableBlockingWhileShieldBashing && Helpers.isHandActive(this.mc.player, EnumHand.OFF_HAND) )
				{
					this.sendStopActiveHandPacket();
				}

			}
			else if ( this.betterCombatOffhand.equipSoundTimer > 0 && --this.betterCombatOffhand.equipSoundTimer <= 0 )
			{
				if ( this.betterCombatOffhand.getWeaponProperty() != WeaponProperty.TWOHAND && this.betterCombatOffhand.getWeaponProperty() != WeaponProperty.MAINHAND )
				{
					this.offhandEquipSound();
				}
			}

			if ( this.mainhandCooldown > 0 )
			{
				this.mainhandCooldown--;
			}

			if ( this.offhandCooldown > 0 )
			{
				this.offhandCooldown--;
			}
		}
	}
	
	public void calculateWallAwarePositioning()
	{
		this.hX = this.hX * this.hX;
		this.hZ = this.hZ * this.hZ;
		
		if ( this.hX < 1.0D && this.hZ < 1.0D )
		{
			if ( this.hX < this.hZ )
			{
				this.hZ *= 0.1D;
			}
			else
			{
				this.hX *= 0.1D;
			}
			
			ClientProxy.AH_INSTANCE.tooCloseCap = MathHelper.clamp(0.6D - (this.hX + this.hZ) * 3.0D, 0.1D, 0.6D);
		}
		else
		{
			ClientProxy.AH_INSTANCE.tooCloseCap = 0;
		}
	}
	
//	public void calculateWallAwarePositioning()
//	{
//		this.hX = this.hX * this.hX;
//		this.hZ = this.hZ * this.hZ;
//		
//		if ( this.hX < 1.0D && this.hZ < 1.0D )
//		{
//			if ( this.hX < this.hZ )
//			{
//				this.hZ *= 0.1D;
//			}
//			else
//			{
//				this.hX *= 0.1D;
//			}
//			
//			double tooCloseCap = 0.6D - (this.hX + this.hZ) * 3.0D;
//			
//			System.out.println(this.hX); // close to x, 0 is close
//			System.out.println(this.hZ); // close to x, 0 is close
//			System.out.println(tooCloseCap);
//			
//			if ( tooCloseCap > 0.1D )
//			{
//				if ( tooCloseCap > 0.6D )
//				{
//					tooCloseCap = 0.6D;
//				}
//				
//				if ( ClientProxy.AH_INSTANCE.tooCloseTimer < tooCloseCap * 0.8D )
//				{
//					ClientProxy.AH_INSTANCE.tooCloseTicker = 0.03F;
//					ClientProxy.AH_INSTANCE.tooCloseTimer += ClientProxy.AH_INSTANCE.tooCloseTicker;
//				}
//				else if ( ClientProxy.AH_INSTANCE.tooCloseTimer > tooCloseCap * 1.2D )
//				{
//					ClientProxy.AH_INSTANCE.tooCloseTicker = -0.02F;
//					ClientProxy.AH_INSTANCE.tooCloseTimer += ClientProxy.AH_INSTANCE.tooCloseTicker;
//				}
//				else
//				{
//					if ( ClientProxy.AH_INSTANCE.tooCloseTimer < tooCloseCap * 0.9D )
//					{
//						ClientProxy.AH_INSTANCE.tooCloseTicker = 0.015F;
//						ClientProxy.AH_INSTANCE.tooCloseTimer += ClientProxy.AH_INSTANCE.tooCloseTicker;
//					}
//					else if ( ClientProxy.AH_INSTANCE.tooCloseTimer > tooCloseCap * 1.1D )
//					{
//						ClientProxy.AH_INSTANCE.tooCloseTicker = -0.01F;
//						ClientProxy.AH_INSTANCE.tooCloseTimer += ClientProxy.AH_INSTANCE.tooCloseTicker;
//					}
//					else
//					{
//						ClientProxy.AH_INSTANCE.tooCloseTicker = 0;
//					}
//				}
//
//				ClientProxy.AH_INSTANCE.tooClose = true;
//			}
//		}
//		else
//		{
//			if ( ClientProxy.AH_INSTANCE.tooCloseTimer > 0.0F )
//			{
//				ClientProxy.AH_INSTANCE.tooCloseTicker = -0.04F;
//				ClientProxy.AH_INSTANCE.tooCloseTimer += ClientProxy.AH_INSTANCE.tooCloseTicker;
//				
//				if ( ClientProxy.AH_INSTANCE.tooCloseTimer < 0.0F )
//				{
//					ClientProxy.AH_INSTANCE.tooCloseTicker = 0;
//					ClientProxy.AH_INSTANCE.tooCloseTimer = 0;
//				}
//			}
//			
//			ClientProxy.AH_INSTANCE.tooClose = false;
//		}
//	}

//	@SubscribeEvent( priority = EventPriority.HIGH, receiveCanceled = true )
//	public void livingUpdate( TickEvent.ClientTickEvent event )
//	{
//		if ( this.mc.player != null )
//		{
//			ClientProxy.AH_INSTANCE.breatheTicks += ConfigurationHandler.breathingAnimationSpeed;
//			this.checkItemstacksChanged(false);
//		}
//	}

	public static double clamp(double d0, double d1)
	{

		if (d0 > d1)
		{
			return d1;
		}

		return d0;
	}

//	@SubscribeEvent( priority = EventPriority.HIGH, receiveCanceled = true )
//	public void livingUpdate( LivingEvent.LivingUpdateEvent event )
//	{
//		if ( event.getEntityLiving().equals(this.mc.player) )
//		{
//			ClientProxy.AH_INSTANCE.breatheTicks += ConfigurationHandler.breathingAnimationSpeed;			
//			this.checkItemstacksChanged(false);
//		}
//	}

	public boolean checkItemstacksChanged(boolean force)
	{
		if ( this.checkItemstackChangedOffhand(force) )
		{
			Reflections.unpressKey(this.mc.gameSettings.keyBindUseItem);

			if ( this.checkItemstackChangedMainhand(force))
			{
				// if ( !this.startedMining ) ??? why !mining nnn
				{
					Reflections.unpressKey(this.mc.gameSettings.keyBindAttack);
				}
				
				this.resetMainhandCooldown(this.mc.player);
				ClientProxy.AH_INSTANCE.reequipAnimationMainhand();
				
				this.stopClick();
			}

			this.resetOffhandCooldown(this.mc.player);
			ClientProxy.AH_INSTANCE.reequipAnimationOffhand();
			
			return true;
		}

		if ( this.checkItemstackChangedMainhand(force) )
		{
			// if ( !this.startedMining )
			{
				Reflections.unpressKey(this.mc.gameSettings.keyBindAttack);
			}

			this.resetMainhandCooldown(this.mc.player);
			ClientProxy.AH_INSTANCE.reequipAnimationMainhand();
			
			this.stopClick();
			
			return true;
		}

		return false;
	}
	
	public boolean checkItemstackChangedMainhand(boolean force)
	{
		if ( force || this.itemStackMainhandSlot != this.mc.player.inventory.currentItem || !this.areBaseItemsEqual(this.itemStackMainhand, this.mc.player.getHeldItemMainhand()) )
		{
			/* Play sheathe sound */
			if ( !force && this.betterCombatMainhand.equipSoundTimer <= 0 && this.betterCombatOffhand.equipSoundTimer >= 0 && this.betterCombatMainhand.hasConfigWeapon() )
			{
				SoundHandler.playSheatheSoundRight(this.mc.player, this.betterCombatMainhand, this.itemStackMainhand, this.betterCombatMainhand.getAttackCooldown(), Helpers.isMetalItem(this.itemStackMainhand));
			}
			
			/* Previous weapon */
			if ( !this.itemStackMainhand.isEmpty() )
			{
				try
				{
					this.mc.player.getAttributeMap().removeAttributeModifiers(this.itemStackMainhand.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
				}
				catch (Exception e)
				{

				}
				
				if ( this.betterCombatOffhand.hasConfigWeapon() && this.betterCombatMainhand.getWeaponProperty().equals(ConfigurationHandler.WeaponProperty.TWOHAND) )
				{
					this.resetOffhandCooldown(this.mc.player);
			    } 
			}

			/* Previous weapon = Current weapon */
			this.itemStackMainhand = this.mc.player.getHeldItemMainhand();
			this.itemStackMainhandSlot = this.mc.player.inventory.currentItem;

			/* Current weapon */
			if ( !this.itemStackMainhand.isEmpty() )
			{
				try
				{
					this.mc.player.getAttributeMap().applyAttributeModifiers(this.itemStackMainhand.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
				}
				catch (Exception e)
				{

				}
			}

			/* Reset */
			this.betterCombatMainhand.resetBetterCombatWeapon();
			ClientProxy.AH_INSTANCE.resetMiningEnergy();
			this.stopParrying();

			/* Identify and add a new config weapon */
			if ( ConfigurationHandler.isConfigWeapon(this.itemStackMainhand.getItem()) )
			{
				String mainhandString = Helpers.getRegistryNameFromItem(this.itemStackMainhand);

				for ( ConfigWeapon weapon : ConfigurationHandler.weapons )
				{
					if ( mainhandString.contains(weapon.name) )
					{
						/* Config weapon found! */
						this.betterCombatMainhand.setBetterCombatWeapon(this.mc.player, weapon, this.itemStackMainhand, this.itemStackOffhand, true);
						
						if ( this.betterCombatOffhand.hasConfigWeapon() && this.betterCombatMainhand.getWeaponProperty().equals(ConfigurationHandler.WeaponProperty.TWOHAND) )
						{
							this.resetOffhandCooldown(this.mc.player); 
						}
						
						return true;
					}

				}

				/* No config weapon found, but it is a weapon! */
				this.betterCombatMainhand.setBetterCombatWeapon(this.mc.player, ConfigurationHandler.DEFAULT_CUSTOM_WEAPON, this.itemStackMainhand, this.itemStackOffhand, true);

				if ( this.betterCombatOffhand.hasConfigWeapon() && this.betterCombatMainhand.getWeaponProperty().equals(ConfigurationHandler.WeaponProperty.TWOHAND) )
				{
					this.resetOffhandCooldown(this.mc.player);
				}
				
				return true;
			}
			else
			{
				/* If it is a shield */
				if ( this.itemStackMainhand.getItem() instanceof ItemShield )
				{
					this.betterCombatMainhand.equipSoundTimer = 5; /* 10 / 2  = 5 */
				}

				return true;
			}

		}

		return false;
	}
	
	/* Stop parrying on client and server */
	private void stopParrying()
	{
		this.parrying = false;
		PacketHandler.instance.sendToServer(new PacketParrying(false));
	}
	
	/* Ignores NBT, Damage, and Count */
	public boolean areBaseItemsEqual( ItemStack a, ItemStack b )
	{
	    return !a.isEmpty() && !b.isEmpty() && a.getItem() == b.getItem();
	}

	public boolean checkItemstackChangedOffhand( boolean force )
	{
		if ( force || !ItemStack.areItemsEqualIgnoreDurability(this.itemStackOffhand, this.mc.player.getHeldItemOffhand()) )
		{
			/* Play sheathe sound */
			if ( !force && this.betterCombatOffhand.equipSoundTimer <= 0 && this.betterCombatOffhand.hasConfigWeapon() )
			{
				SoundHandler.playSheatheSoundLeft(this.mc.player, this.betterCombatOffhand, this.itemStackOffhand, this.betterCombatOffhand.getAttackCooldown(), Helpers.isMetalItem(this.itemStackOffhand));

				/* Set the offhand equip sound to disabled so it does not play at the same time */
				this.betterCombatOffhand.equipSoundTimer = -1;
			}

			/* Previous weapon = Current weapon */
			this.itemStackOffhand = this.mc.player.getHeldItemOffhand();

			/* Reset */
			this.betterCombatOffhand.resetBetterCombatWeapon();
			
			/* Check if it a weapon */
			if ( ConfigurationHandler.isConfigWeapon(this.itemStackOffhand.getItem()) )
			{
				String offhandString = Helpers.getRegistryNameFromItem(this.itemStackOffhand);

				for ( ConfigWeapon weapon : ConfigurationHandler.weapons )
				{
					if ( offhandString.contains(weapon.name) )
					{
						this.betterCombatOffhand.setBetterCombatWeapon(this.mc.player, weapon, this.itemStackMainhand, this.itemStackOffhand, false);
						return true;
					}
				}

				/* No config weapon found, but it is a weapon! */
				this.betterCombatOffhand.setBetterCombatWeapon(this.mc.player, ConfigurationHandler.DEFAULT_CUSTOM_WEAPON, this.itemStackMainhand, this.itemStackOffhand, false);
				
				return true;
			}
			else
			{
				/* If it is a shield */
				if ( this.itemStackOffhand.getItem() instanceof ItemShield )
				{
					/* Add an equip sound to the shield */
					this.betterCombatOffhand.equipSoundTimer = 5; /* 10 / 2  = 5 */
				}

				return true;
			}

		}

		return false;
	}

	/*
	 * =============================================================================
	 * =========================================================================
	 */
	/* SOUND */
	/*
	 * =============================================================================
	 * =========================================================================
	 */

	public void mainhandSwingSound()
	{
		this.mc.player.swingArm(EnumHand.MAIN_HAND);
		SoundHandler.playSwingSoundRight(this.mc.player, this.betterCombatMainhand, this.itemStackMainhand, this.betterCombatMainhand.getAttackCooldown());
	}

	public void offhandSwingSound()
	{
		this.mc.player.swingArm(EnumHand.OFF_HAND);
		SoundHandler.playSwingSoundLeft(this.mc.player, this.betterCombatOffhand, this.itemStackOffhand, this.betterCombatOffhand.getAttackCooldown());
	}

	private void mainhandEquipSound()
	{
		SoundHandler.playEquipSoundRight(this.mc.player, this.betterCombatMainhand, this.itemStackMainhand, this.betterCombatMainhand.getAttackCooldown());
	}

	private void offhandEquipSound()
	{
		SoundHandler.playEquipSoundLeft(this.mc.player, this.betterCombatOffhand, this.itemStackOffhand, this.betterCombatOffhand.getAttackCooldown());
	}

	/*
	 * =============================================================================
	 * =========================================================================
	 */
	/* COOLDOWNS */
	/*
	 * =============================================================================
	 * =========================================================================
	 */

	public boolean isMainhandAttackReady()
	{
		return this.mainhandCooldown <= 0;
	}

	/* visual only */
	public float getMainhandCooledAttackStrength()
	{
		return 1.0F - MathHelper.clamp((float) (1 + this.mainhandCooldown) / this.betterCombatMainhand.getAttackCooldown(), 0.0F, 1.0F);
	}

	public boolean isOffhandAttackReady()
	{
		return this.offhandCooldown <= 0;
	}

	/* visual only */
	public float getOffhandCooledAttackStrength()
	{
		return 1.0F - MathHelper.clamp((float) (1 + this.offhandCooldown) / this.betterCombatOffhand.getAttackCooldown(), 0.0F, 1.0F);
	}

	public void resetMainhandCooldown(EntityPlayerSP player)
	{
		this.mc.player.resetCooldown();
		this.mainhandCooldown = this.betterCombatMainhand.setAttackCooldown(this.getMainhandCooldown(player, this.itemStackMainhand, this.itemStackOffhand));
	}
	
	public void resetOffhandCooldown(EntityPlayerSP player)
	{
		this.offhandCooldown = this.betterCombatOffhand.setAttackCooldown(this.getOffhandCooldown(player, this.itemStackOffhand, this.itemStackMainhand));
	}
	
	/* ====================================================================================================================================================== */
    /*																		Tooltip 																		  */
    /* ====================================================================================================================================================== */
	
	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
	public void itemTooltipEventHigh(ItemTooltipEvent event)
	{
		if (ConfigurationHandler.isConfigWeapon(event.getItemStack().getItem()))
		{
			for ( ConfigWeapon s : ConfigurationHandler.weapons )
			{
				if ( Helpers.getRegistryNameFromItem(event.getItemStack()).contains(s.name) )
				{
					this.updateBetterCombatTooltipHigh(s, event);
					return;
				}

			}

			this.updateBetterCombatTooltipHigh(ConfigurationHandler.DEFAULT_CUSTOM_WEAPON, event);
			return;
		}

		if ( event.getItemStack().getItem() instanceof ItemShield )
		{

			for ( CustomShield s : ConfigurationHandler.shields )
			{

				if ( event.getItemStack().getItem().equals(s.shield) )
				{
					event.getToolTip().add(EMPTY);
					event.getToolTip().add(I18n.format("bettercombat.info.property.offhand.text"));
					event.getToolTip().add(BLANK_SPACE + I18n.format("bettercombat.info.attribute.color") + s.damage + I18n.format("bettercombat.info.bash.damage.text"));
					event.getToolTip().add(BLANK_SPACE + I18n.format("bettercombat.info.attribute.color") + s.knockback + I18n.format("bettercombat.info.bash.knockback.text"));
					event.getToolTip().add(BLANK_SPACE + I18n.format("bettercombat.info.attribute.color") + s.cooldown + I18n.format("bettercombat.info.bash.cooldown.text"));
					return;
				}

			}

		}

	}

	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
	public void itemTooltipEventLow(ItemTooltipEvent event)
	{
		this.removeDuplicateEmptyLines(event);

//		if ( ConfigurationHandler.isItemClassWhiteList(event.getItemStack().getItem()) )
//		{
//			
//		}
	}

//	public final String twoHandedString = this.getTwoHandedString();
//	
//	private String getTwoHandedString()
//	{
//		String tooltip = I18n.format("tooltip.spartanweaponry:two_handed");
//		
//		try
//		{
//			return (tooltip.substring(0, tooltip.indexOf(BLANK_SPACE)).concat("I"));
//		}
//		catch ( Exception e )
//		{
//			return tooltip;
//		}
//	}
//	
//	public final String sweepString = I18n.format("tooltip.spartanweaponry:sweep_damage");
//	public final String sweepDescString = I18n.format("tooltip.spartanweaponry:sweep_damage.desc");
//	public final String reachString = I18n.format("tooltip.spartanweaponry:reach");
//	public final String reachDescString = I18n.format("tooltip.spartanweaponry:reach.desc");
//
//

	private void removeDuplicateEmptyLines(ItemTooltipEvent event)
	{
		List<String> tooltips = new ArrayList<>();

		boolean previousLineIsEmpty = false;

		for (String tag : event.getToolTip())
		{

			if (tag.isEmpty() || tag.equals(BLANK_SPACE))
			{

				if (!previousLineIsEmpty)
				{
					tooltips.add(tag);
				}

				previousLineIsEmpty = true;
			}
			else
			{

				if (!tag.contains(mainhandString) && !this.isEmptySWDash(tag))
				{
					tooltips.add(tag);
					previousLineIsEmpty = false;
				}

			}

//			try{event.getEntityPlayer().sendMessage(new TextComponentString(tag + tag.length()));}catch(Exception e ){}

		}

		event.getToolTip().clear();
		event.getToolTip().addAll(tooltips);
	}

	private boolean isEmptySWDash(String tag)
	{
		return tag.endsWith(SW_DASH);
	}

//	private void updateBetterCombatTooltipLow(ItemTooltipEvent event)
//	{
//		if ( !ConfigurationHandler.removeRedundantSpartanWeaponryTooltips )
//		{
//			return;
//		}
//		
//		int twoHandedStringIndex = -1;
//		
//		String twoHandedDescString = I18n.format("tooltip.spartanweaponry:two_handed.desc");
//		int twoHandedDescStringIndex = -1;
//
//		
//		int sweepStringIndex = -1;
//		
//		int sweepDescStringIndex = -1;
//
//		
//		int reachStringIndex = -1;
//		
//		int reachDescStringIndex = -1;
//
////			String qualityToolsMainhand = I18n.format(BLANK_SPACE);
////			int qualityToolsMainhandIndex = -1;
//
//		int i = 0;
//
//		for (String tag : event.getToolTip())
//		{
//			try{event.getEntityPlayer().sendMessage(new TextComponentString(tag));}catch(Exception e ){}
//			
//			if (twoHandedStringIndex < 0 && tag.contains(twoHandedString))
//			{
//				twoHandedStringIndex = i;
//			}
//			else if (twoHandedDescStringIndex < 0 && tag.contains(twoHandedDescString))
//			{
//				twoHandedDescStringIndex = i;
//			}
//			else if (reachStringIndex < 0 && tag.contains(reachString))
//			{
//				reachStringIndex = i;
//			}
//			else if (reachDescStringIndex < 0 && tag.contains(reachDescString))
//			{
//				reachDescStringIndex = i;
//			}
//			else if (sweepStringIndex < 0 && tag.contains(sweepString))
//			{
//				sweepStringIndex = i;
//			}
//			else if (sweepDescStringIndex < 0 && tag.contains(sweepDescString))
//			{
//				sweepDescStringIndex = i;
//			}
//
//			i++;
//		}
//
//		i = 0;
//		
////			/* REMOVE QUALITYTOOLS WHEN IN MAIN HAND TAG */
////			if (qualityToolsMainhandIndex >= 0)
////			{
////				event.getToolTip().remove(qualityToolsMainhandIndex - i++);
////			}
//		
//
//		/* REMOVE WEAPONRY TWO-HANDED TAG */
//		if (twoHandedStringIndex >= 0)
//		{
//			event.getToolTip().remove(twoHandedStringIndex - i++);
//		}
//		
//		/* REMOVE WEAPONRY TWO-HANDED DESCRIPTION */
//		if (twoHandedDescStringIndex >= 0)
//		{
//			event.getToolTip().remove(twoHandedDescStringIndex - i++);
//		}
//		
//
//		/* REMOVE CUSTOM REACH TAG */
//		if (reachStringIndex >= 0)
//		{
//			event.getToolTip().remove(reachStringIndex - i++);
//		}
//		
//		/* REMOVE CUSTOM REACH DESCRIPTION */
//		if (reachDescStringIndex >= 0)
//		{
//			event.getToolTip().remove(reachDescStringIndex - i++);
//		}
//		
//
//		/* REMOVE CUSTOM SWEEP TAG */
//		if (sweepStringIndex >= 0)
//		{
//			event.getToolTip().remove(reachStringIndex - i++);
//		}
//
//		/* REMOVE CUSTOM SWEEP DESCRIPTION */
//		if (sweepDescStringIndex >= 0)
//		{
//			event.getToolTip().remove(reachDescStringIndex - i++);
//		}
//	}

	private void updateBetterCombatTooltipHigh(ConfigWeapon s, ItemTooltipEvent event)
	{
		/*
		 * =============================================================================
		 */
		/* Total Tooltips */
		/*
		 * =============================================================================
		 */

		/* Knockback */
		if (ConfigurationHandler.showKnockbackTooltip)
		{

			if (ConfigurationHandler.showKnockbackTooltipAsTotal)
			{
				event.getToolTip().add(BLANK_SPACE + I18n.format("bettercombat.info.attribute.color") + s.knockbackMod + I18n.format("bettercombat.info.knockback.text"));
			}

		}

		/* Reach */
		if (ConfigurationHandler.showReachTooltip)
		{

			if (ConfigurationHandler.showReachTooltipAsTotal)
			{
				event.getToolTip().add(BLANK_SPACE + I18n.format("bettercombat.info.attribute.color") + (s.additionalReachMod + Helpers.getBaseReach(this.mc.player)) + I18n.format("bettercombat.info.reachDistance.text"));
			}

		}

		/* Crit Chance */
		if (ConfigurationHandler.showCritChanceTooltip && ConfigurationHandler.baseCritPercentChance >= 0.0D)
		{

			if (ConfigurationHandler.showCritChanceTooltipAsTotal)
			{
				event.getToolTip().add(BLANK_SPACE + I18n.format("bettercombat.info.attribute.color") + (int) (s.critChanceMod * 100) + "%" + I18n.format("bettercombat.info.critChance.text"));
			}

		}

		/* Crit Damage */
		if (ConfigurationHandler.showCritDamageTooltip)
		{

			if (ConfigurationHandler.showCritDamageTooltipAsTotal)
			{
				event.getToolTip().add(BLANK_SPACE + I18n.format("bettercombat.info.attribute.color") + (int) ((s.additionalCritDamageMod + ConfigurationHandler.baseCritPercentDamage) * 100) + "%" + I18n.format("bettercombat.info.critDamage.text"));
			}

		}

		int mainhandStringIndex = -1;
//		int qualityToolsMainhandIndex = -1;

		boolean formattedAttackSpeed = false;
		boolean formattedAttackDamage = false;

		int i = 0;

		for (String tag : event.getToolTip())
		{

			if (tag.contains(mainhandString))
			{

				if (mainhandStringIndex < 0)
				{
					mainhandStringIndex = i;
				}

//				else if ( qualityToolsMainhandIndex < 0 )
//				{
//					qualityToolsMainhandIndex = i;
//				}
			}
			else if (!formattedAttackSpeed && tag.contains(attackSpeedString))
			{

				if (event.getItemStack().getItem() instanceof ItemSword && !ConfigurationHandler.swords.isEmpty())
				{
					String str = Helpers.getRegistryNameFromItem(event.getItemStack());

					for (CustomSword sword : ConfigurationHandler.swords)
					{

						if (str.contains(sword.name))
						{

							try
							{
								Matcher matcher = Pattern.compile(ATTACK_SPEED_REGEX).matcher(tag);

								if (matcher.find())
								{
									formattedAttackSpeed = true;
									event.getToolTip().set(i, BLANK_SPACE + I18n.format("bettercombat.info.attribute.color") + String.format("%.1f", (sword.attackSpeed + Double.parseDouble(matcher.group(2)))) + BLANK_SPACE + attackSpeedString);
								}

							}
							catch (Exception e)
							{
								formattedAttackSpeed = true;
								event.getToolTip().set(i, I18n.format("bettercombat.info.attribute.color") + tag);
							}

							break;
						}

					}

					if (!formattedAttackSpeed)
					{
						formattedAttackSpeed = true;
						this.reformatAttackString(event, i, ATTACK_SPEED_REGEX, tag, attackSpeedString);
					}

				}
				else
				{
					formattedAttackSpeed = true;
					this.reformatAttackString(event, i, ATTACK_SPEED_REGEX, tag, attackSpeedString);
				}

			}
			else if (!formattedAttackDamage && tag.contains(attackDamageString))
			{
				formattedAttackDamage = true;
				this.reformatAttackString(event, i, ATTACK_DAMAGE_REGEX, tag, attackDamageString);
			}

			i++;
		}

		/* PROPERTY */
		if (mainhandStringIndex >= 0)
		{

			switch (s.property)
			{
				case ONEHAND:
				{
					event.getToolTip().set(mainhandStringIndex, I18n.format("bettercombat.info.property.onehand.text"));
					break;
				}
				case VERSATILE:
				{
					event.getToolTip().set(mainhandStringIndex, I18n.format("bettercombat.info.property.versatile.text"));
					break;
				}
				case MAINHAND:
				{
					event.getToolTip().set(mainhandStringIndex, I18n.format("bettercombat.info.property.mainhand.text"));
					break;
				}
				case TWOHAND:
				{
					event.getToolTip().set(mainhandStringIndex, I18n.format("bettercombat.info.property.twohand.text"));
					break;
				}
				default:
				{
					event.getToolTip().set(mainhandStringIndex, I18n.format("bettercombat.info.property.onehand.text"));
					break;
				}
			}

			/* i = 0; */
//			if ( qualityToolsMainhandIndex >= 0 )
//			{
//				event.getToolTip().remove(qualityToolsMainhandIndex /* - i++ */);
//			}
		}

		/*
		 * =============================================================================
		 */
		/* Addition Tooltips */
		/*
		 * =============================================================================
		 */

		boolean flag = false;

		if (ConfigurationHandler.showKnockbackTooltip)
		{

			if (!ConfigurationHandler.showKnockbackTooltipAsTotal && s.knockbackMod != ConfigurationHandler.baseKnockback)
			{

				if (!flag)
				{
					event.getToolTip().add(EMPTY);
				}

				event.getToolTip().add((s.knockbackMod > ConfigurationHandler.baseKnockback ? I18n.format("bettercombat.info.positive.color") : I18n.format("bettercombat.info.negative.color")) + (s.knockbackMod - ConfigurationHandler.baseKnockback) + I18n.format("bettercombat.info.knockback.text"));
				flag = true;
			}

		}

		if (ConfigurationHandler.showReachTooltip)
		{

			if (!ConfigurationHandler.showReachTooltipAsTotal && s.additionalReachMod != 0.0D)
			{

				if (!flag)
				{
					event.getToolTip().add(EMPTY);
				}

				event.getToolTip().add((s.additionalReachMod > 0.0D ? I18n.format("bettercombat.info.positive.color") : I18n.format("bettercombat.info.negative.color")) + s.additionalReachMod + I18n.format("bettercombat.info.reachDistance.text"));
				flag = true;
			}

		}

		if (ConfigurationHandler.showCritChanceTooltip && ConfigurationHandler.baseCritPercentChance >= 0.0D)
		{

			if (!ConfigurationHandler.showCritChanceTooltipAsTotal && s.critChanceMod != ConfigurationHandler.baseCritPercentChance)
			{

				if (!flag)
				{
					event.getToolTip().add(EMPTY);
				}

				event.getToolTip().add((s.critChanceMod > 0.0F ? I18n.format("bettercombat.info.positive.color") : I18n.format("bettercombat.info.negative.color")) + (int) ((s.critChanceMod - ConfigurationHandler.baseCritPercentChance) * 100) + "%" + I18n.format("bettercombat.info.critChance.text"));
				flag = true;
			}

		}

		if (ConfigurationHandler.showCritDamageTooltip)
		{

			if (!ConfigurationHandler.showCritDamageTooltipAsTotal && s.additionalCritDamageMod != 0)
			{

				if (!flag)
				{
					event.getToolTip().add(EMPTY);
				}

				event.getToolTip().add((s.additionalCritDamageMod > 0.0F ? I18n.format("bettercombat.info.positive.color") : I18n.format("bettercombat.info.negative.color")) + (int) (s.additionalCritDamageMod * 100) + "%" + I18n.format("bettercombat.info.critDamage.text"));
				flag = true;
			}

		}

		if (ConfigurationHandler.showSweepTooltip && s.sweepMod > 0)
		{
			event.getToolTip().add(EMPTY);
			event.getToolTip().add(I18n.format("bettercombat.info.sweep.text") + Helpers.integerToRoman(s.sweepMod));
			flag = true;
		}

		if (ConfigurationHandler.showPotionEffectTooltip && s.configWeaponPotionEffect != null)
		{
			event.getToolTip().add(EMPTY);

			if (s.configWeaponPotionEffect.potionChance > 0.0F)
			{
				event.getToolTip().add((int) (s.configWeaponPotionEffect.potionChance * 100) + "%" + I18n.format("bettercombat.info.potionEffect.chance.text"));
			}
			else
			{
				event.getToolTip().add(I18n.format("bettercombat.info.potionEffect.crit.text"));
			}

			double seconds = Math.round((s.configWeaponPotionEffect.potionDuration * 0.5D)) * 0.1D;

			String str;

			if (seconds % 1 == 0)
			{
				str = (int) seconds + I18n.format("bettercombat.info.potionEffect.second.text") + ((int) seconds == 1 ? EMPTY : "s");
			}
			else
			{
				str = seconds + I18n.format("bettercombat.info.potionEffect.second.text");
			}

			event.getToolTip().add((s.configWeaponPotionEffect.afflict ? I18n.format("bettercombat.info.potionEffect.negative.text") : I18n.format("bettercombat.info.potionEffect.positive.text")) + I18n.format(s.configWeaponPotionEffect.getPotion().getName()) + BLANK_SPACE + Helpers.integerToRoman(s.configWeaponPotionEffect.potionPower) + (s.configWeaponPotionEffect.potionDuration > 0 ? I18n.format("bettercombat.info.potionEffect.for.text") + str : EMPTY));
		}

	}

	private void reformatAttackString(ItemTooltipEvent event, int i, String regex, String tag, String s)
	{
		try
		{
			Matcher matcher = Pattern.compile(regex).matcher(tag);

			if (matcher.find())
			{
				event.getToolTip().set(i, BLANK_SPACE + I18n.format("bettercombat.info.attribute.color") + String.format("%.1f", (Double.parseDouble(matcher.group(2)))) + BLANK_SPACE + s);
			}

		}
		catch (Exception e)
		{
			event.getToolTip().set(i, I18n.format("bettercombat.info.attribute.color") + tag);
		}
	}

	/*
	 * =============================================================================
	 * =========================================================================
	 */
	/* MOV TARGETING */
	/*
	 * =============================================================================
	 * =========================================================================
	 */

	/*
	 * 
	 * 270 x | 180---|---z 0 | 90
	 * 
	 * goes negative, -90 == 270
	 * 
	 */

	public static final float  PI_FLOAT         = (float) Math.PI;
	public static final double SQRT_2_DOUBLE    = (double) MathHelper.SQRT_2;
	public static final double RADIAN_TO_DEGREE = 180.0D / Math.PI;

//	public static boolean isEntityInView(Entity viewer, Entity target) 
//	{
//		double dx = target.posX - viewer.posX;
//		double dz = target.posZ - viewer.posZ;
//
//		// Direction from viewer to target (in degrees)
//		double angleToTarget = (Math.atan2(dz, dx) * RADIAN_TO_DEGREE + 360) % 360;
//
//		// Viewer's head rotation (normalized to 0–360)
//		double viewerYaw = (viewer.getRotationYawHead() + 360) % 360;
//
//		// Difference between them (−180 to +180)
//		double angleDiff = angleToTarget - viewerYaw;
//		if (angleDiff > 180) angleDiff -= 360;
//		if (angleDiff < -180) angleDiff += 360;
//
//		// Check if within ±50° cone in front
//		return Math.abs(angleDiff) <= 50;
//	}
	
	/*
	 * Return true if the target entity is in view of in entity, uses head rotation to calculate
	 * 
	 * @param in The entity that is viewing the target
	 * 
	 * @param target The target entity
	 * 
	 * @param bounds The width of entity view cone, in degrees
	 * 
	 * @return If the target is in view
	 */
	public static boolean isEntityInViewCone(Entity in, Entity target, int bounds)
	{
		bounds = bounds >> 1;
		double rotation = (Math.atan2(target.posZ - in.posZ, target.posX - in.posX) * RADIAN_TO_DEGREE + 360) % 360 - (in.getRotationYawHead() + 450) % 360;
		return (rotation <= bounds && rotation >= -bounds) || rotation >= (360 - bounds) || rotation <= (bounds - 360);
	}
	
	/*
	 * returns true if the target entity is in view of in entity, uses head rotation
	 * to calculate
	 */
	public static boolean isEntityInView(Entity in, Entity target)
	{
		double rotation = (Math.atan2(target.posZ - in.posZ, target.posX - in.posX) * RADIAN_TO_DEGREE + 360) % 360 - (in.getRotationYawHead() + 450) % 360;
		return (rotation <= 50 && rotation >= -50) || rotation >= 310 || rotation <= -310;
	}
	
//	private static double wrapAngleTo180(double angle)
//	{
//	    angle = angle % 360;
//	    if (angle >= 180) angle -= 360;
//	    if (angle < -180) angle += 360;
//	    return angle;
//	}
//
//	public static boolean isEntityInView(Entity in, Entity target)
//	{
//	    double targetAngle = (Math.atan2(target.posZ - in.posZ, target.posX - in.posX) * RADIAN_TO_DEGREE + 360) % 360;
//	    double viewerAngle = (in.getRotationYawHead() + 450) % 360;
//	    double rotation = wrapAngleTo180(targetAngle - viewerAngle);
//	    return Math.abs(rotation) <= 50;
//	}
	
	// vvv
	public RayTraceResult getMouseOverExtended( EntityPlayerSP player, double reach, double sweepWidth, RayTraceResult.Type checkType )
	{
		final Vec3d lookEyes = player.getRidingEntity() == null ? player.getPositionEyes(this.mc.getRenderPartialTicks()) : player.getPositionEyes(this.mc.getRenderPartialTicks()).addVector(0.0D, player.getRidingEntity().getMountedYOffset(), 0.0D);
		final Vec3d lookVec = player.getLook(1.0F);
		final Vec3d lookTarget = lookEyes.add(lookVec.scale(reach));
		
		double reachSq = reach - ConfigurationHandler.extraAttackWidth - sweepWidth; if ( reachSq > 0.0D ) { reachSq *= reachSq; } else { reachSq = 0.0D; }

		RayTraceResult pointedRayTraceResult;

		if ( checkType != RayTraceResult.Type.BLOCK && ConfigurationHandler.cutGrass )
		{
			pointedRayTraceResult = player.world.rayTraceBlocks(lookEyes, lookTarget, false, false, false);
			
			if ( pointedRayTraceResult != null && pointedRayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK )
			{
				this.cutGrass(pointedRayTraceResult.getBlockPos());
			}
			
			pointedRayTraceResult = player.world.rayTraceBlocks(lookEyes, lookTarget, false, false, false);
		}
		else
		{
			/* Ingore blocks without a bounding box, such as grass */
			pointedRayTraceResult = player.world.rayTraceBlocks(lookEyes, lookTarget, false, true, false);
		}
		
		Set<Entity> entities = new HashSet<>();
		
		if ( pointedRayTraceResult != null && pointedRayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK )
		{
			entities.addAll(this.mc.world.getEntitiesWithinAABBExcludingEntity(player, new AxisAlignedBB(pointedRayTraceResult.hitVec, pointedRayTraceResult.hitVec).grow(0.1D)));
		}
		
		double closestLookDistanceSq = reachSq;
		
		/* List of entities within range */
		entities.addAll(this.mc.world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().expand(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach).grow(1.0D)));

		for ( Entity entity : entities )
		{
			if ( entity instanceof MultiPartEntityPart && ((MultiPartEntityPart)entity).parent instanceof Entity )
			{
				entity = (Entity)((MultiPartEntityPart)entity).parent;
			}
			
			if ( this.checkParent(entity) )
			{
				entity = this.getParent(entity);
			}
			
			if ( !entity.isEntityAlive() )
			{
				continue;
			}
			
			/* Do not hit the entity this is riding */
			if ( entity == player.getRidingEntity() )
			{
				continue;
			}

			if ( !entity.canBeAttackedWithItem() )
			{
				continue;
			}
						
			if ( !this.canPVP(entity, this.mc.player) )
			{
				continue;
			}

			/* Checks to see if the target is within the view of an attack, prevents hitting targets off screen */
			if ( !isEntityInView(player, entity) )
			{
				continue;
			}
						
			/* Checks to see if the target can be seen, meaning, no blocks are in the way */
			if ( !player.canEntityBeSeen(entity) ) // TODO XXX

			{
				continue;
			}
						
			if ( checkType == RayTraceResult.Type.BLOCK && pointedRayTraceResult != null && pointedRayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK && player.getDistanceSq(entity) <= reachSq )
			{
				return pointedRayTraceResult;
			}
			
			double width = entity.getCollisionBorderSize() + ConfigurationHandler.extraAttackWidth + sweepWidth;
			
			/* Ray traces to see if the adjusted entity collision border is intersected by the player look */
			RayTraceResult rayTraceResult = entity.getEntityBoundingBox().grow(width, ConfigurationHandler.extraAttackHeight, width).calculateIntercept(lookEyes, lookTarget);
			
			if ( rayTraceResult == null )
			{
				continue;
			}
			
			double lookDistanceSq = lookEyes.squareDistanceTo(rayTraceResult.hitVec);

			if ( lookDistanceSq < closestLookDistanceSq && player.getDistanceSq(entity) <= reachSq )
			{
				pointedRayTraceResult = new RayTraceResult(entity, rayTraceResult.hitVec);
				closestLookDistanceSq = lookDistanceSq;
			}
		}
		
		if ( pointedRayTraceResult != null && pointedRayTraceResult.typeOfHit == RayTraceResult.Type.ENTITY )
		{
			return pointedRayTraceResult;
		}
		
		/* Return the riding entity */
		return this.ridingMouseOverEntity(player);
	}

	public boolean checkParent(Entity entity)
	{
		if ( CommonProxy.partEntityClass != null )
		{
			/* mov.entityHit is an instance of the PartEntity or its subclass */
			return CommonProxy.partEntityClass.isInstance(entity);
		}

		return false;
	}

	public EntityLiving getParent(Object partEntity)
	{
		try
		{
			Field field = CommonProxy.partEntityClass.getDeclaredField("parent");

			field.setAccessible(true);

			Object fieldValue = field.get(partEntity);

			return (EntityLiving) fieldValue;
		}
		catch (Exception e)
		{

		}

		return null;
	}
	
	
	
	

/* BETTER COMBAT */
//	public static RayTraceResult getMouseOverExtended(double dist)
//	{
//		Minecraft mc = Minecraft.getMinecraft();
//		Entity rvEntity = mc.getRenderViewEntity();
//
//		if (rvEntity == null)
//		{
//			return null;
//		}
//
//		AxisAlignedBB viewBB = new AxisAlignedBB(rvEntity.posX - 0.5D, rvEntity.posY - 0.0D, rvEntity.posZ - 0.5D, rvEntity.posX + 0.5D, rvEntity.posY + 1.5D, rvEntity.posZ + 0.5D);
//
//		if (mc.world != null)
//		{
//			RayTraceResult traceResult = rvEntity.rayTrace(dist, 0.0F);
//			final Vec3d lookEyes = rvEntity.getPositionEyes(0.0F).addVector(0.0D, -execNullable(rvEntity.getRidingEntity(), Entity::getMountedYOffset, 0.0D), 0.0D);
//			final Vec3d lookVec = rvEntity.getLook(0.0F);
//			final Vec3d lookTarget = lookEyes.addVector(lookVec.x * dist, lookVec.y * dist, lookVec.z * dist);
//			final float growth = 1.0F;
//			final List<Entity> list = mc.world.getEntitiesWithinAABBExcludingEntity(rvEntity, viewBB.expand(lookVec.x * dist, lookVec.y * dist, lookVec.z * dist).grow(growth, growth, growth));
//			final double calcdist = traceResult != null ? traceResult.hitVec.distanceTo(lookEyes) : dist;
//
//			double newDist = calcdist;
//			Entity pointed = null;
//
//			for (Entity entity : list)
//			{
//
//				if (entity.canBeCollidedWith())
//				{
//					float borderSize = entity.getCollisionBorderSize();
//					AxisAlignedBB aabb;
//
//					if (ConfigurationHandler.extraAttackWidth > 0 )
//					{
//						float w = (float) ConfigurationHandler.extraAttackWidth;
//						aabb = new AxisAlignedBB(entity.posX - entity.width * w, entity.posY, entity.posZ - entity.width * w, entity.posX + entity.width * w, entity.posY + entity.height, entity.posZ + entity.width * w);
//					}
//					else
//					{
//						aabb = new AxisAlignedBB(entity.posX - entity.width / 2.0F, entity.posY, entity.posZ - entity.width / 2.0F, entity.posX + entity.width / 2.0F, entity.posY + entity.height, entity.posZ + entity.width / 2.0F);
//					}
//
//					aabb.grow(borderSize, borderSize, borderSize);
//					RayTraceResult mop0 = aabb.calculateIntercept(lookEyes, lookTarget);
//
//					if (aabb.contains(lookEyes) && entity != rvEntity.getRidingEntity())
//					{
//
//						if (newDist >= -0.000001D)
//						{
//							pointed = entity;
//							newDist = 0.0D;
//						}
//
//					}
//					else if (mop0 != null)
//					{
//						double hitDist = lookEyes.distanceTo(mop0.hitVec);
//
//						if (hitDist < newDist || (newDist >= -0.000001D && newDist <= 0.000001D))
//						{
//							pointed = entity;
//							newDist = hitDist;
//						}
//
//					}
//
//				}
//
//			}
//
//			if (pointed != null && (newDist < calcdist || traceResult == null))
//			{
//				return new RayTraceResult(pointed);
//			}
//
//			return traceResult;
//		}
//
//		return null;
//	}

	/* VANILLA */
//	public void getMouseOver(float partialTicks)
//    {
//        Entity entity = this.mc.getRenderViewEntity();
//
//        if (entity != null)
//        {
//            if (this.mc.world != null)
//            {
//                double d0 = (double)this.mc.playerController.getBlockReachDistance();
//                
//                boolean flag = false;
//                
//                Vec3d eyes = entity.getPositionEyes(partialTicks);
//                Vec3d lookVec = entity.getLook(1.0F);
//                Vec3d lookTarget = eyes.addVector(lookVec.x * d0, lookVec.y * d0, lookVec.z * d0);
//                
//                Vec3d eyes3 = null;
//                
//                float f = 1.0F;
//                List<Entity> list = this.mc.world.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().expand(lookVec.x * d0, lookVec.y * d0, lookVec.z * d0).grow(1.0D, 1.0D, 1.0D), Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>()
//                {
//                    public boolean apply(@Nullable Entity p_apply_1_)
//                    {
//                        return p_apply_1_ != null && p_apply_1_.canBeCollidedWith();
//                    }
//                }));
//                
//                double d2 = d0;
//
//                for (int j = 0; j < list.size(); ++j)
//                {
//                    Entity target = list.get(j);
//                    
//                    AxisAlignedBB axisalignedbb = target.getEntityBoundingBox().grow((double)target.getCollisionBorderSize());
//                    RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(eyes, lookTarget);
//
//                    if (axisalignedbb.contains(eyes))
//                    {
//                        if (d2 >= 0.0D)
//                        {
//                            eyes3 = raytraceresult == null ? eyes : raytraceresult.hitVec;
//                            d2 = 0.0D;
//                        }
//                    }
//                    else if (raytraceresult != null)
//                    {
//                        double d3 = eyes.distanceTo(raytraceresult.hitVec);
//
//                        if (d3 < d2 || d2 == 0.0D)
//                        {
//                            if (target.getLowestRidingEntity() == entity.getLowestRidingEntity() && !target.canRiderInteract())
//                            {
//                                if (d2 == 0.0D)
//                                {
//                                    this.pointedEntity = target;
//                                    eyes3 = raytraceresult.hitVec;
//                                }
//                            }
//                            else
//                            {
//                                this.pointedEntity = target;
//                                eyes3 = raytraceresult.hitVec;
//                                d2 = d3;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

	public RayTraceResult ridingMouseOverEntity(EntityPlayerSP player)
	{
		if ( player.isRiding() && player.getRidingEntity() instanceof IMob )
		{
			return new RayTraceResult(player.getRidingEntity());
		}
		else if ( player.isBeingRidden() )
		{
			for ( Entity passenger : player.getPassengers() )
			{
				return new RayTraceResult(passenger);
			}
		}
		
		return null;
	}

	public boolean invalidPlayer(EntityPlayerSP player)
	{
		return player == null || player.isSpectator() || !player.isEntityAlive() || player.getHealth() <= 0.0F;
	}

	/* Returns true if the entity is a player and PVP is enabled */
	public boolean canPVP(Entity entityHit, EntityPlayer player)
	{
		if ( entityHit instanceof EntityPlayerMP )
		{
			return Helpers.execNullable(entityHit.getServer(), MinecraftServer::isPVPEnabled, false);
		}

		return true;
	}
	
	public int getMainhandCooldown( EntityPlayer player, ItemStack mh, ItemStack oh )
	{
		double speed;
		
		if ( this.betterCombatMainhand.hasConfigWeapon() )
		{
			speed = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();
		}
		else
		{
			speed = ConfigurationHandler.fistAndNonWeaponAttackSpeed;
		}
		
		//System.out.println(speed);

		
		double multiply_base = 1.0D;

		double multiply = 1.0D;

		/* + ALL */
		for ( AttributeModifier attribute : player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getModifiers() )
		{
//			System.out.println(attribute);
//			System.out.println(attribute.getName());
//			System.out.println(attribute.getAmount());
//			System.out.println(attribute.toString());

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
		
//		System.out.println(speed);

		
		/* Adjust sword speed */
		if ( mh.getItem() instanceof ItemSword && !ConfigurationHandler.swords.isEmpty() )
		{
			String s = Helpers.getRegistryNameFromItem(mh);
			
			for ( CustomSword sword : ConfigurationHandler.swords )
			{
				if ( s.contains(sword.name) )
				{
					speed += sword.attackSpeed;
					break;
				}
			}
		}
		
//		System.out.println(speed);

		
		if ( !mh.isEmpty() && !oh.isEmpty() )
		{
			double mhFatigue = this.betterCombatMainhand.getFatigue();
			double ohFatigue = this.betterCombatOffhand.getFatigue();
			
			if ( mhFatigue > 0 || ohFatigue > 0 )
			{
				/* Set the fatigue to the custom weapon fatigue, only versatile weapons have fatigue */
				speed /= 1.0 + mhFatigue + ohFatigue/2.0;
			}
		}
		
//		System.out.println(speed);
		
		return this.calculateAttackSpeedTicks(speed, multiply_base, multiply);
	}
	
	private int calculateAttackSpeedTicks( double speed, double multiply_base, double multiply )
	{
		return (int)( (20.0D/MathHelper.clamp(Helpers.calculateAttribute(speed, multiply_base, multiply), 0.1D, 20.0D)) + ConfigurationHandler.addedSwingTickCooldown );
	}
	
    /* ====================================================================================================================================================== */
    /*																OFFHAND ATTACK SPEED 																	  */
    /* ====================================================================================================================================================== */
	
	public int getOffhandCooldown( EntityPlayer player, ItemStack oh, ItemStack mh )
	{
		double speed = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();

		double multiply_base = 1.0D;

		double multiply = 1.0D;

		/* + ALL */
		for ( AttributeModifier attribute : player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getModifiers() )
		{			
			if ( attribute.getID().equals(Helpers.weaponModifierUUID) )
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
			if ( modifier.getKey().contains(Helpers.ATTACK_SPEED) )
			{
				if ( modifier.getValue().getID().equals(Helpers.weaponModifierUUID) )
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
			if ( modifier.getKey().contains(Helpers.ATTACK_SPEED) )
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
		if ( mh.hasTagCompound() && mh.getTagCompound().hasKey(Helpers.QUALITY, 10) )
		{
			final NBTTagCompound tag = mh.getSubCompound(Helpers.QUALITY);
			final NBTTagList attributeList = tag.getTagList(Helpers.ATTRIBUTE_MODIFIERS, 10);

			for ( int j = 0; j < attributeList.tagCount(); ++j )
			{
				final AttributeModifier modifier = SharedMonsterAttributes.readAttributeModifierFromNBT(attributeList.getCompoundTagAt(j));
				final String attributeName = attributeList.getCompoundTagAt(j).getString(Helpers.ATTRIBUTE_NAME);

				if ( attributeName.contains(Helpers.ATTACK_SPEED) )
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
		if ( oh.hasTagCompound() && oh.getTagCompound().hasKey(Helpers.QUALITY, 10) )
		{
			final NBTTagCompound tag = oh.getSubCompound(Helpers.QUALITY);
			final NBTTagList attributeList = tag.getTagList(Helpers.ATTRIBUTE_MODIFIERS, 10);

			for ( int j = 0; j < attributeList.tagCount(); ++j )
			{
				final AttributeModifier modifier = SharedMonsterAttributes.readAttributeModifierFromNBT(attributeList.getCompoundTagAt(j));
				final String attributeName = attributeList.getCompoundTagAt(j).getString(Helpers.ATTRIBUTE_NAME);

				if ( attributeName.contains(Helpers.ATTACK_SPEED) )
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
		
		/* Adjust sword speed */
		if ( oh.getItem() instanceof ItemSword && !ConfigurationHandler.swords.isEmpty() )
		{
			String s = Helpers.getRegistryNameFromItem(oh);
			
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
			double mhFatigue = this.betterCombatMainhand.getFatigue();
			double ohFatigue = this.betterCombatOffhand.getFatigue();
			
			if ( mhFatigue > 0 || ohFatigue > 0 )
			{
				/* Set the fatigue to the custom weapon fatigue, only versatile weapons have fatigue */
				speed /= 1.0 + ohFatigue + mhFatigue/2.0;
			}
		}

		return this.calculateAttackSpeedTicks(speed, multiply_base, multiply);
	}
	
    /* ====================================================================================================================================================== */
    /*																					 																	  */
    /* ====================================================================================================================================================== */

//	public static void spawnSweep( EntityPlayer e )
//	{
//		double d0 = -Math.sin(e.rotationYaw * 0.017453292F);
//		double d1 = Math.cos(e.rotationYaw * 0.017453292F);
//		
//		ParticleBuilder.create(ParticleType.SWEEP).pos(d0, e.getEyeHeight()-e.rotationPitch*0.016D-0.4D, d1).entity(e).spawn(e.world);
//	}
//	public static final double RADIAN_TO_DEGREE = 180.0D/Math.PI;
//	
//	/* returns true if the target entity is in view of in entity, uses head rotation to calculate */
//	public static boolean isEntityInView( EntityLivingBase in, EntityLivingBase target )
//	{
//        double rotation = (Math.atan2(target.posZ - in.posZ, target.posX - in.posX) * RADIAN_TO_DEGREE + 360) % 360 - (in.rotationYawHead + 450) % 360;
//        return (rotation <= 50 && rotation >= -50) || rotation >= 310 || rotation <= -310;
//	}
//
}