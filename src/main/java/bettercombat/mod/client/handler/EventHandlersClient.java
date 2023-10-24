package bettercombat.mod.client.handler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import bettercombat.mod.client.BetterCombatHand;
import bettercombat.mod.client.ClientProxy;
import bettercombat.mod.network.PacketBreakBlock;
import bettercombat.mod.network.PacketFastEquip;
import bettercombat.mod.network.PacketHandler;
import bettercombat.mod.network.PacketMainhandAttack;
import bettercombat.mod.network.PacketOffhandAttack;
import bettercombat.mod.network.PacketOnItemUse;
import bettercombat.mod.network.PacketParrying;
import bettercombat.mod.network.PacketShieldBash;
import bettercombat.mod.network.PacketStopActiveHand;
import bettercombat.mod.util.CommonProxy;
import bettercombat.mod.util.ConfigurationHandler;
import bettercombat.mod.util.ConfigurationHandler.CustomShield;
import bettercombat.mod.util.ConfigurationHandler.CustomSword;
import bettercombat.mod.util.ConfigurationHandler.CustomWeapon;
import bettercombat.mod.util.ConfigurationHandler.WeaponProperty;
import bettercombat.mod.util.Helpers;
import bettercombat.mod.util.Reflections;
import bettercombat.mod.util.SoundHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockGrassPath;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
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

	public final BetterCombatHand betterCombatMainhand   = new BetterCombatHand();
	public final BetterCombatHand betterCombatOffhand    = new BetterCombatHand();

	/* Ticker for offhand cooldown. If offhand cooldown is 0, the attack is ready */
	public int                    offhandCooldown        = 0;

	/*
	 * The offhand attack cooldown is the number the offhand cooldown gets set to
	 * when making an attack
	 */
	public int                    offhandAttackCooldown  = ConfigurationHandler.minimumAttackSpeedTicks;

	/*
	 * Ticker for mainhand cooldown. If minitiateAnimationainhand cooldown is 0, the
	 * attack is ready
	 */
	public int                    mainhandCooldown       = 0;

	/*
	 * The mainhand attack cooldown is the number the mainhand cooldown gets set to
	 * when making an attack
	 */
	public int                    mainhandAttackCooldown = ConfigurationHandler.minimumAttackSpeedTicks;

	public static final String EMPTY = EMPTY;
	public static final String BLANK_SPACE = " ";
	
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
		if (this.invalidPlayer(player))
		{
			/* Cancel left-click! */
			return true;
		}

		/* Check to see if the items have changed */
		this.checkItemstacksChanged(false);

		if (ConfigurationHandler.isBlacklisted(this.itemStackMainhand.getItem()))
		{
			/* Continue with left-click! */
			return false;
		}

		/* -------------------------------------------- */
		/* 					SHIELD BASH 				*/
		/* -------------------------------------------- */

		/* If the player has an active item such as a bow or shield, */
		if (!player.getActiveItemStack().isEmpty())
		{
			/* If the player is blocking, */
			if (Helpers.isHandActive(this.mc.player, EnumHand.OFF_HAND))
			{
				Item shield = player.getActiveItemStack().getItem();

				/* If the shield is on cooldown */
				if (player.getCooldownTracker().hasCooldown(shield))
				{
					/* Cancel left-click! */
					return true;
				}

				/* 30 is the default cooldown */
				int bashCooldown = -1;

				for (CustomShield s : ConfigurationHandler.shields)
				{

					if (shield.equals(s.shield))
					{
						bashCooldown = s.cooldown;
						break;
					}

				}

				if (bashCooldown < 0)
				{
					/* No shield bash */
				}
				else
				{
					/* offhandCooldown used for crosshair cooldown display */
					this.offhandCooldown = bashCooldown;
					this.offhandAttackCooldown = bashCooldown;

					/* Set the internal shield cooldown */
					player.getCooldownTracker().setCooldown(shield, bashCooldown);

					this.sendStopActiveHandPacket();

					/* animate the shield bash */
					ClientProxy.EHC_INSTANCE.betterCombatOffhand.setShieldBashing();

					/* Cancel left-click! */
					return true;
				}

			}

			/* Cancel left-click, as the player has an active item and should not attack! */
			return true;
		}
		
		/* If this left-click should check blocks, and there is no entity being targeted */
		if ( checkBlocks && !this.mainhandMouseoverHasEntity() )
		{
			RayTraceResult mov = this.mc.objectMouseOver;

			if ( mov == null )
			{
				/* Cancel left-click! mov should not be null */
				return true;
			}
						
			/* If a the target is a block, */
			if ( mov.typeOfHit == RayTraceResult.Type.BLOCK )
			{
				BlockPos pos = mov.getBlockPos();

				/* If that position is invalid, */
				if ( pos == null && pos == BlockPos.ORIGIN )
				{
					/* Cancel left-click, as there was an error! */
					return true;
				}

				Block block = player.world.getBlockState(pos).getBlock();

				/* If the block is a PLANT, and the player has weapon in the MAINHAND */
				if ( ( block instanceof IPlantable || block instanceof IShearable ) && this.betterCombatMainhand.hasCustomWeapon() )
				{
					/* Continue... */
				}
				else
				{
					/* MINING! Continue with left-click! */
					return this.mining();
				}
			}
		}

		/* If the MAINHAND attack is not ready, */
		if ( !this.isMainhandAttackReady() )
		{
			/* Cancel left-click! */
			return true;
		}

		/* The player is mining, do not attack if they are holding down left-click and missed a block */
		if ( this.startedMining )
		{
			return true;
		}
		
		/* ----------------------------------------- */
		/* SWING WEAPON */
		/* ----------------------------------------- */
		
		/*
		 * Reset the MAINHAND cooldown so the player cannot attack for a period of time
		 */
		this.mc.player.resetCooldown();
		this.resetMainhandCooldown(player);

		/* SWING! Initiate the MAINHAND animation for attacking */
		this.betterCombatMainhand.initiateAnimation(this.mainhandCooldown);

		return true;
	}

	private void sendStopActiveHandPacket()
	{
		PacketHandler.instance.sendToServer(new PacketStopActiveHand());	
	}

	/* Returns false, do not cancel the left click */
	private boolean mining()
	{
		/* If the MAINHAND is ready to begin a swing animation, */
		if (this.betterCombatMainhand.getSwingTimer() <= 0)
		{
			/* Start the MAINHAND mining animation with a set mining speed */
			this.betterCombatMainhand.startMining();
			
			this.startedMining = true;
		}

		return false;
	}

	public void mainhandAttack()
	{
		if ( this.mc.objectMouseOver != null )
		{
			/* If the MAINHAND item can interact with that block, */
			if ( this.mc.objectMouseOver.typeOfHit == Type.BLOCK && this.mc.objectMouseOver.getBlockPos() != null && this.mc.objectMouseOver.getBlockPos() != BlockPos.ORIGIN )
			{
				if ( this.toolCanInteractWithBlock(this.itemStackMainhand.getItem()) && this.itemStackMainhand.getItem().onItemUse(this.mc.player, this.mc.player.world, this.mc.objectMouseOver.getBlockPos(), EnumHand.MAIN_HAND, this.mc.objectMouseOver.sideHit, 0.0F, 0.0F, 0.0F) == EnumActionResult.SUCCESS )
				{
					/* HIT! Send a packet that uses the item on the block! */
					PacketHandler.instance.sendToServer(new PacketOnItemUse(this.mc.objectMouseOver.getBlockPos().getX(), this.mc.objectMouseOver.getBlockPos().getY(), this.mc.objectMouseOver.getBlockPos().getZ(), true, this.mc.objectMouseOver.sideHit));
					return;
				}
				else
				{
					this.swingThroughGrass(this.mc.objectMouseOver.getBlockPos());
				}
			}
		}
		
		RayTraceResult mov = this.getMainhandMouseover();

		/* If, the MOV is not null AND has an entity AND if it is a player, can it be PVPd */
		if ( mov != null && mov.entityHit != null && this.canPVP(mov.entityHit, this.mc.player) )
		{
			if ( this.checkParent(mov.entityHit) )
			{
				mov.entityHit = this.getParent(mov.entityHit);
			}
			
			/* HIT! Send an attack packet with a target! */
			PacketHandler.instance.sendToServer(new PacketMainhandAttack(mov.entityHit.getEntityId()));
			return;
		}
		else 

		/* MISS! Send an attack packet with NO target! */
		PacketHandler.instance.sendToServer(new PacketMainhandAttack());
		return;
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

            return (EntityLiving)fieldValue;
        }
        catch (Exception e)
        {
        	
        }
        
		return null;
    }

	private boolean toolCanInteractWithBlock(Item item)
	{
		return (ConfigurationHandler.tillingRequiresAnimation && item instanceof ItemHoe) || (ConfigurationHandler.grassPathingRequiresAnimation && item instanceof ItemSpade) || (ConfigurationHandler.strippingBarkRequiresAnimation && item instanceof ItemAxe);
	}

	/* Returns null if there is no mouseover entity */
	private @Nullable RayTraceResult getMainhandMouseover()
	{
		return this.getMouseOverExtended(this.mc.player, Helpers.getMainhandReach(this.mc.player, this.betterCombatMainhand.getAdditionalReach()), this.getExtraSweepWidth(this.betterCombatMainhand.getSweep()));
	}
	
	/* Returns true if there is a mouseover entity */
	private boolean mainhandMouseoverHasEntity()
	{
		return this.getMouseOverExtended(this.mc.player, Helpers.getMainhandReach(this.mc.player, this.betterCombatMainhand.getAdditionalReach()), this.getExtraSweepWidth(this.betterCombatMainhand.getSweep()) + 1.0F) != null;
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
		if ( this.mc.objectMouseOver != null )
		{
			/* If the OFFHAND item can interact with that block, */
			if ( this.mc.objectMouseOver.typeOfHit == Type.BLOCK && this.mc.objectMouseOver.getBlockPos() != null && this.mc.objectMouseOver.getBlockPos() != BlockPos.ORIGIN )
			{
				if ( this.toolCanInteractWithBlock(this.itemStackOffhand.getItem()) && this.itemStackOffhand.getItem().onItemUse(this.mc.player, this.mc.player.world, this.mc.objectMouseOver.getBlockPos(), EnumHand.OFF_HAND, this.mc.objectMouseOver.sideHit, 0.0F, 0.0F, 0.0F) == EnumActionResult.SUCCESS )
				{
					/* HIT! Send a packet that uses the item on the block! */
					PacketHandler.instance.sendToServer(new PacketOnItemUse(this.mc.objectMouseOver.getBlockPos().getX(), this.mc.objectMouseOver.getBlockPos().getY(), this.mc.objectMouseOver.getBlockPos().getZ(), false, this.mc.objectMouseOver.sideHit));
					return;
				}
				else
				{
					this.swingThroughGrass(this.mc.objectMouseOver.getBlockPos());
				}
			}
		}
		
		RayTraceResult mov = this.getOffhandMouseover();
		
		/* If, the MOV is not null AND has an entity AND if it is a player, can it be PVPd */
		if ( mov != null && mov.entityHit != null && this.canPVP(mov.entityHit, this.mc.player) && ConfigurationHandler.rightClickAttackable(this.mc.player, mov.entityHit) )
		{
			if ( this.checkParent(mov.entityHit) )
			{
				mov.entityHit = this.getParent(mov.entityHit);
			}
			
			if ( this.itemStackOffhand.getItem() instanceof ItemShield )
			{
				/* HIT! Send an shield bash packet with a target! */
				PacketHandler.instance.sendToServer(new PacketShieldBash(mov.entityHit.getEntityId()));
				return;
			}
			else
			{
				/* HIT! Send an attack packet with a target! */
				PacketHandler.instance.sendToServer(new PacketOffhandAttack(mov.entityHit.getEntityId()));
				return;
			}
		}
		
		if (this.itemStackOffhand.getItem() instanceof ItemShield)
		{
			PacketHandler.instance.sendToServer(new PacketShieldBash());
			return;
		}
		else
		{
			PacketHandler.instance.sendToServer(new PacketOffhandAttack());
			return;
		}
	}

	/* Returns null if there is no mouseover entity */
	private @Nullable RayTraceResult getOffhandMouseover()
	{
		return getMouseOverExtended(this.mc.player, Helpers.getOffhandReach(this.mc.player, this.betterCombatOffhand.getAdditionalReach(), this.itemStackOffhand, this.itemStackMainhand), this.getExtraSweepWidth(this.betterCombatOffhand.getSweep()));
	}
	
	/* Returns true if there is a mouseover entity */
	private boolean offhandMouseoverHasEntity()
	{
		return getMouseOverExtended(this.mc.player, Helpers.getOffhandReach(this.mc.player, this.betterCombatOffhand.getAdditionalReach(), this.itemStackOffhand, this.itemStackMainhand), this.getExtraSweepWidth(this.betterCombatOffhand.getSweep()) + 1.0F) != null;
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

	private boolean swingThroughGrass(BlockPos pos)
	{
		if (pos == null)
		{
			return false;
		}

		Block block = this.mc.player.world.getBlockState(pos).getBlock();

		/* If the block is a plant or grass, */
		if (block instanceof IPlantable || block instanceof IShearable)
		{
			PacketHandler.instance.sendToServer(new PacketBreakBlock(pos.getX(), pos.getY(), pos.getZ()));
			this.spawnSweepHit(this.mc.player, pos.getX(), pos.getZ());

			do
			{
				pos = pos.up();
				block = this.mc.player.world.getBlockState(pos).getBlock();

				if (pos != null && (block instanceof IPlantable) ) // || block instanceof IShearable))
				{
					PacketHandler.instance.sendToServer(new PacketBreakBlock(pos.getX(), pos.getY(), pos.getZ()));
				}
				else
				{
					break;
				}

			}
			while ( true );

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
		if (this.invalidPlayer(this.mc.player))
		{
			/* Cancel right-click! */
			return true;
		}

		/* Check to see if the ItemStacks have changed */
		this.checkItemstacksChanged(false);

		RayTraceResult mov = this.mc.objectMouseOver;

		if ( mov == null )
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
		 * If the MAINHAND has a TWOHAND weapon, prevent placing blocks, but use the item
		 */
		if ( this.betterCombatMainhand.getWeaponProperty() == WeaponProperty.TWOHAND )
		{
			if ( this.itemStackMainhand.getItemUseAction() == EnumAction.NONE )
			{			
				/* Only use the MAINHAND */
				if ( this.rightClickMouse(EnumHand.MAIN_HAND) )
				{
					
				}
				else if ( this.canParry(false) )
				{
					this.parrying = true;
				}
			}
			
			Reflections.setRightClickDelayTimer(this.mc, 2);
			
			/* Cancel right-click! */
			return true;
		}
		
		/* If the OFFHAND has a TWOHAND or MAINHAND weapon, */
		if (this.betterCombatOffhand.getWeaponProperty() == WeaponProperty.TWOHAND || this.betterCombatOffhand.getWeaponProperty() == WeaponProperty.MAINHAND)
		{
			if ( mov.typeOfHit == RayTraceResult.Type.BLOCK )
			{
				BlockPos pos = mov.getBlockPos();

				/* If that position is invalid, */
				if ( pos == null && pos == BlockPos.ORIGIN )
				{
					/* Cancel right-click, as there was an error! */
					return true;
				}

				Block block = this.mc.player.world.getBlockState(pos).getBlock();

				if ( this.useToolsMainhandOnly(this.mc.player, block) )
				{
					/* Cancel right-click! */
					return true;
				}
			}

			if ( this.itemStackMainhand.getItemUseAction() == EnumAction.NONE )
			{			
				/* Only use the MAINHAND */
				if ( this.rightClickMouse(EnumHand.MAIN_HAND) )
				{
					
				}
				else if ( this.canParry(false) )
				{
					this.parrying = true;
				}
			}
			else
			{
				if ( this.rightClickMouse(EnumHand.MAIN_HAND) )
				{
					/* Cancel right-click, but use the item still! */
					return true;
				}
			}
			
			Reflections.setRightClickDelayTimer(this.mc, 2);
			
			/* Cancel right-click! */
			return true;
		}
		
		/* If the MAINHAND has an action, OR if the OFFHAND has an action, */
		if (this.itemStackMainhand.getItemUseAction() != EnumAction.NONE || this.itemStackOffhand.getItemUseAction() != EnumAction.NONE)
		{
			/* If the OFFHAND can block */
			if (this.itemStackOffhand.getItemUseAction() == EnumAction.BLOCK)
			{
				if (ConfigurationHandler.disableBlockingWhileAttacking && !this.isMainhandAttackReady())
				{
					this.sendStopActiveHandPacket();
				}
			}

			/* Continue with right-click and use item! */
			return false;
		}

		/* If targeting a block, and not targeting any entity */
		if ( mov.typeOfHit == RayTraceResult.Type.BLOCK && !this.offhandMouseoverHasEntity() )
		{
			BlockPos pos = mov.getBlockPos();

			/* If that position is invalid, */
			if ( pos == null && pos == BlockPos.ORIGIN )
			{
				/* Cancel right-click! */
				return true;
			}

			Block block = this.mc.player.world.getBlockState(pos).getBlock();

			/* If the block is a PLANT and the MAINHAND OR OFFHAND has a HOE, */
			if ( (block instanceof IPlantable || block instanceof IShearable) && (this.betterCombatOffhand.hasCustomWeapon() || (this.itemStackMainhand.getItem() instanceof ItemHoe || this.itemStackOffhand.getItem() instanceof ItemHoe) ) )
			{
				if ( this.itemStackMainhand.getItem() instanceof ItemHoe || this.itemStackOffhand.getItem() instanceof ItemHoe )
				{
					/* Due to other mods, continue with right-click, using the HOE on the PLANT! */
					return false;
				}
			}
			/*
			 * Otherwise, if the player can interact with any hand,
			 */
			else if ( !this.betterCombatMainhand.hasCustomWeapon() && !this.betterCombatOffhand.hasCustomWeapon() )
			{
				/* Continue with right-click, placing blocks! */
				return false;
			}
			/*
			 * Otherwise, if the player can interact with any hand,
			 */
			else if ( this.rightClickMouse(EnumHand.MAIN_HAND) || this.rightClickMouse(EnumHand.OFF_HAND) )
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
		/* Otherwise, if there are no equipped weapons */
		else if ( !this.betterCombatMainhand.hasCustomWeapon() && !this.betterCombatOffhand.hasCustomWeapon() )
		{
			/* Continue with right-click! */
			return false;
		}
		/* Otherwise, if there is a target and it is NOT attackable with the OFFHAND, */
		else if ( mov.entityHit != null && !ConfigurationHandler.rightClickAttackable(this.mc.player, mov.entityHit) )
		{
			/* Continue with right-click, interact with entity! */
			return false;
		}

		/* Continue if the player is sneaking */
		if ( this.mc.player.isSneaking() && ConfigurationHandler.sneakingDisablesOffhandAttack  )
		{
			/* Continue with right-click! */
			return false;
		}

		/* If the OFFHAND attack is not ready, */
		if ( !this.isOffhandAttackReady() )
		{
			/* Cancel right-click! */
			return true;
		}

		/* ----------------------------------------- */
		/* SWING WEAPON */
		/* ----------------------------------------- */

		return this.rightClick(this.mc.player);
	}

	private boolean canParry(boolean checkOffhand)
	{
		return this.betterCombatMainhand.canParry() && this.betterCombatMainhand.hasCustomWeapon() && (!checkOffhand || this.itemStackOffhand.isEmpty()) && this.isMainhandAttackReady() && !this.mc.player.getCooldownTracker().hasCooldown(this.itemStackMainhand.getItem());
	}

	private boolean useTools(EntityPlayerSP player, Block block)
	{

		if (ConfigurationHandler.grassPathingRequiresAnimation)
		{

			if (block instanceof BlockGrass ) // || block == Blocks.DIRT
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
						this.rightClick(player);
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
						this.rightClick(player);
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
						this.rightClick(player);
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

			if (block instanceof BlockGrass ) // || block == Blocks.DIRT
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
	private boolean rightClick(EntityPlayerSP player)
	{
		if (this.betterCombatOffhand.hasCustomWeapon())
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

	private boolean rightClickMouse(EnumHand enumhand)
	{
		// System.out.println("rclick 1");

		if ( Reflections.getRightClickDelayTimer(this.mc) > 0 )
		{
			return true;
		}
		
		// System.out.println("rclick 2");

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
								// System.out.println("interactWithEntity");
								Reflections.setRightClickDelayTimer(this.mc, 4);
								return true;
							}

							if (this.mc.playerController.interactWithEntity(this.mc.player, this.mc.objectMouseOver.entityHit, enumhand) == EnumActionResult.SUCCESS)
							{
								// System.out.println("interactWithEntity 2");
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
									// System.out.println("toolCanInteractWithBlock");

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
										// System.out.println("resetEquippedProgress");

										this.mc.entityRenderer.itemRenderer.resetEquippedProgress(enumhand);
									}

									// System.out.println("processRightClickBlock");

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
					
					// System.out.println("onEmptyClick");
					Reflections.setRightClickDelayTimer(this.mc, 4);
					return true;
				}

				if (!itemstack.isEmpty() && this.mc.playerController.processRightClick(this.mc.player, this.mc.world, enumhand) == EnumActionResult.SUCCESS)
				{
					this.mc.entityRenderer.itemRenderer.resetEquippedProgress(enumhand);
					
					// System.out.println("processRightClick");
					Reflections.setRightClickDelayTimer(this.mc, 4);
					return true;
				}

			}

		}

		// System.out.println("false");

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
	
	private boolean startedMining = false;

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

	/* For keypress events only! This event only triggers when clicks are re-bound to keys */
	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
	public void onKeyEvent(KeyInputEvent event)
	{
		if ( ClientProxy.fastEquip.isKeyDown() && this.isMainhandAttackReady() && this.isOffhandAttackReady() )
		{
			PacketHandler.instance.sendToServer(new PacketFastEquip());
		}
		
		/* keyBindUseItem isPressed and isKeyDown is only true if set to a keybind, not a click */
		if ( this.mc.gameSettings.keyBindUseItem.isPressed() || this.mc.gameSettings.keyBindUseItem.isKeyDown() )
		{
			if ( this.mc.gameSettings.keyBindUseItem.getKeyCode() != -99 )
			{
				/*
					Reflections.unpressKey(this.mc.gameSettings.keyBindUseItem);
					this.mc.gameSettings.keyBindUseItem = new KeyBinding("key.use", -99, "key.categories.gameplay");
					this.mc.gameSettings.keyBindUseItem.setToDefault();
				*/
				
				/* Cancel the vanilla right-click! */
				if ( this.overwriteRightClick() )
				{
					if ( Reflections.getRightClickDelayTimer(this.mc) <= 0 )
					{
						Reflections.setRightClickDelayTimer(this.mc, 4);
					}
				}
				
				event.setResult(Result.DENY);
				// Reflections.setRightClickDelayTimer(this.mc, 4); XXX
			}
		}

		/* keyBindAttack isPressed and isKeyDown is only true if set to a keybind, not a click */
		if ( this.mc.gameSettings.keyBindAttack.isPressed() || this.mc.gameSettings.keyBindAttack.isKeyDown() )
		{
			if ( this.mc.gameSettings.keyBindAttack.getKeyCode() != -100 )
			{
				/*
					Reflections.unpressKey(this.mc.gameSettings.keyBindAttack);
					this.mc.gameSettings.keyBindAttack = new KeyBinding("key.attack", -100, "key.categories.gameplay");
					this.mc.gameSettings.keyBindAttack.setToDefault();
				*/
				
				/* Cancel the vanilla left-click attack!
				 * This stops the player from changing their keybind to bypass the MouseEvent to use a vanilla attack! */
				this.overwriteLeftClick(true);
				
				event.setResult(Result.DENY);
			}
		}
	}
	
	private double hX = 0.0D;
	private double hZ = 0.0D;

	@SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true) // XXX
	public void tickEventLow(TickEvent.ClientTickEvent event)
	{
		if ( event.phase == TickEvent.Phase.END && this.mc.player != null )
		{
			ClientProxy.AH_INSTANCE.breatheTicks += ConfigurationHandler.breathingAnimationSpeed;

			this.checkItemstacksChanged(false);

			/* Wall-aware positioning */
			if (this.mc.objectMouseOver != null)
			{

				if (this.mc.objectMouseOver.hitVec != null)
				{
					this.hX = this.mc.player.posX - this.mc.objectMouseOver.hitVec.x;
					this.hZ = this.mc.player.posZ - this.mc.objectMouseOver.hitVec.z;

					if ((this.hX = this.hX * this.hX) < 0.7D && (this.hZ = this.hZ * this.hZ) < 0.7D)
					{
						ClientProxy.AH_INSTANCE.tooCloseAmount = MathHelper.clamp(0.5D - (this.hX + this.hZ) * 0.25D, 0.1D, 0.4D);
						ClientProxy.AH_INSTANCE.tooClose = true;
					}
					else
					{
						ClientProxy.AH_INSTANCE.tooClose = false;
					}

				}
				else if (mc.objectMouseOver.entityHit != null)
				{
					this.hX = this.mc.player.posX - (this.mc.objectMouseOver.entityHit.posX + this.mc.objectMouseOver.entityHit.width * 0.5D);
					this.hZ = this.mc.player.posZ - (this.mc.objectMouseOver.entityHit.posZ + this.mc.objectMouseOver.entityHit.width * 0.5D);

					if ((this.hX = this.hX * this.hX) < 0.7D && (this.hZ = this.hZ * this.hZ) < 0.7D)
					{
						ClientProxy.AH_INSTANCE.tooCloseAmount = MathHelper.clamp(0.5D - (this.hX + this.hZ) * 0.25D, 0.1D, 0.4D);
						ClientProxy.AH_INSTANCE.tooClose = true;
					}
					else
					{
						ClientProxy.AH_INSTANCE.tooClose = false;
					}

				}

			}

			/* Lets the player hold down left-click */
			if ( this.mc.gameSettings.keyBindAttack.isPressed() || this.mc.gameSettings.keyBindAttack.isKeyDown() )
			{
				this.overwriteLeftClick(true);
			}
			else
			{
				/* When the player left-clicks, set startedMining to false */
				this.startedMining = false;
			}

			/* Lets the player hold down right-click */
			if ( this.mc.gameSettings.keyBindUseItem.isPressed() || this.mc.gameSettings.keyBindUseItem.isKeyDown() )
			{				
				if ( !this.mc.player.isHandActive() )
				{
					this.overwriteRightClick();
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

			if (this.betterCombatMainhand.getSwingTimer() > 0)
			{
				this.betterCombatMainhand.tick();

				if (this.betterCombatMainhand.isMining())
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

				if (ConfigurationHandler.disableBlockingWhileAttacking && Helpers.isHandActive(this.mc.player, EnumHand.OFF_HAND))
				{
					this.sendStopActiveHandPacket();
				}

			}
			else if (this.betterCombatMainhand.equipSoundTimer > 0 && --this.betterCombatMainhand.equipSoundTimer <= 0)
			{
				this.mainhandEquipSound();
				
				/* Disable the offhand equip sound so they do not play at the same time, as it sounds off */
				if ( this.betterCombatOffhand.equipSoundTimer == 1 )
				{
					this.betterCombatOffhand.equipSoundTimer = -1;
				}
			}

			if (this.betterCombatOffhand.getSwingTimer() > 0)
			{
				this.betterCombatOffhand.tick();

				if (this.betterCombatOffhand.isMining())
				{

				}
				else
				{

					if (this.betterCombatOffhand.damageReady())
					{
						this.offhandAttack();
					}
					else if (this.betterCombatOffhand.soundReady())
					{
						this.offhandSwingSound();
					}

				}

				if (ConfigurationHandler.disableBlockingWhileShieldBashing && Helpers.isHandActive(this.mc.player, EnumHand.OFF_HAND))
				{
					this.sendStopActiveHandPacket();
				}

			}
			else if (this.betterCombatOffhand.equipSoundTimer > 0 && --this.betterCombatOffhand.equipSoundTimer <= 0)
			{
				if ( this.betterCombatOffhand.getWeaponProperty() != WeaponProperty.TWOHAND && this.betterCombatOffhand.getWeaponProperty() != WeaponProperty.MAINHAND )
				{
					this.offhandEquipSound();
				}
			}

			if (this.mainhandCooldown > 0)
			{
				this.mainhandCooldown--;
			}

			if (this.offhandCooldown > 0)
			{
				this.offhandCooldown--;
			}

		}

	}

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
		boolean flag = false;
		
		if ( this.checkItemstackChangedOffhand(force) )
		{
			Reflections.unpressKey(this.mc.gameSettings.keyBindUseItem);
			
			if ( this.checkItemstackChangedMainhand(force) )
			{
				Reflections.unpressKey(this.mc.gameSettings.keyBindAttack);
			}

			flag = true;
		}

		if ( this.checkItemstackChangedMainhand(force) )
		{
			Reflections.unpressKey(this.mc.gameSettings.keyBindAttack);
			
			flag = true;
		}

		return flag;
	}
	
//	public boolean checkItemstacksChanged(boolean force)
//	{
//		if (this.checkItemstackChangedOffhand(force))
//		{
//			return this.checkItemstackChangedMainhand(force);
//		}
//
//		return this.checkItemstackChangedMainhand(force);
//	}

	public boolean checkItemstackChangedMainhand(boolean force)
	{

		if (force || !ItemStack.areItemsEqualIgnoreDurability(this.itemStackMainhand, this.mc.player.getHeldItemMainhand()) || !ItemStack.areItemStackTagsEqual(this.itemStackMainhand, this.mc.player.getHeldItemMainhand()))
		{

			if (!force && this.betterCombatMainhand.equipSoundTimer <= 0 && this.betterCombatOffhand.equipSoundTimer >= 0 && this.betterCombatMainhand.hasCustomWeapon())
			{
				SoundHandler.playSheatheSoundRight(this.mc.player, this.betterCombatMainhand, this.itemStackMainhand, this.mainhandAttackCooldown, Helpers.isMetal(this.itemStackMainhand));
			}

			/* Previous Weapon */
			if (!this.mc.player.getHeldItemMainhand().isEmpty())
			{

				try
				{
					this.mc.player.getAttributeMap().removeAttributeModifiers(this.mc.player.getHeldItemMainhand().getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
				}
				catch (Exception e)
				{

				}

			}

			/* Swap */
			this.itemStackMainhand = this.mc.player.getHeldItemMainhand();

			/* Current Weapon */
			if (!this.itemStackMainhand.isEmpty())
			{

				try
				{
					this.mc.player.getAttributeMap().applyAttributeModifiers(this.itemStackMainhand.getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
				}
				catch (Exception e)
				{

				}

			}

			this.resetMainhandCooldown(this.mc.player);

			this.parrying = false;
			PacketHandler.instance.sendToServer(new PacketParrying(false));

			this.betterCombatMainhand.resetBetterCombatWeapon();

			if (ConfigurationHandler.isItemClassWhiteList(this.itemStackMainhand.getItem()))
			{
				String mainhandString = Helpers.getString(this.itemStackMainhand);

				for (CustomWeapon weapon : ConfigurationHandler.weapons)
				{

					if (mainhandString.contains(weapon.name))
					{
						/* Config weapon found! */
						this.betterCombatMainhand.setBetterCombatWeapon(weapon, this.itemStackMainhand, this.mainhandAttackCooldown);
						return true;
					}

				}

				/* No config weapon found, but it is a weapon! */
				this.betterCombatMainhand.setBetterCombatWeapon(ConfigurationHandler.DEFAULT_CUSTOM_WEAPON, this.itemStackMainhand, this.mainhandAttackCooldown);
				return true;
			}
			else
			{

				/* Add an equip sound to the shield */
				if (this.itemStackMainhand.getItem() instanceof ItemShield)
				{
					this.betterCombatMainhand.equipSoundTimer = 5; /* 10 / 2 */
				}

				return true;
			}

		}

		return false;
	}

	public boolean checkItemstackChangedOffhand(boolean force)
	{

		if (force || !ItemStack.areItemsEqualIgnoreDurability(this.itemStackOffhand, this.mc.player.getHeldItemOffhand()) || !ItemStack.areItemStackTagsEqual(this.itemStackOffhand, this.mc.player.getHeldItemOffhand()))
		{

			if (!force && this.betterCombatOffhand.equipSoundTimer <= 0 && this.betterCombatOffhand.hasCustomWeapon())
			{
				SoundHandler.playSheatheSoundLeft(this.mc.player, this.betterCombatOffhand, this.itemStackOffhand, this.offhandAttackCooldown, Helpers.isMetal(this.itemStackOffhand));
				
				/* Set the offhand equip sound to disabled so they do not play at the same time, as it sounds off */
				this.betterCombatOffhand.equipSoundTimer = -1;
			}

			/* Swap */
			this.itemStackOffhand = this.mc.player.getHeldItemOffhand();

			/* Current Weapon */

			this.resetOffhandCooldown(this.mc.player);

//			this.parrying = false;
//			PacketHandler.instance.sendToServer(new PacketParrying(false));

			this.betterCombatOffhand.resetBetterCombatWeapon();

			if (ConfigurationHandler.isItemClassWhiteList(this.itemStackOffhand.getItem()))
			{
				String offhandString = Helpers.getString(this.itemStackOffhand);

				for (CustomWeapon weapon : ConfigurationHandler.weapons)
				{

					if (offhandString.contains(weapon.name))
					{
						this.betterCombatOffhand.setBetterCombatWeapon(weapon, this.itemStackOffhand, this.offhandAttackCooldown);
						return true;
					}

				}

				/* No config weapon found, but it is a weapon! */
				this.betterCombatOffhand.setBetterCombatWeapon(ConfigurationHandler.DEFAULT_CUSTOM_WEAPON, this.itemStackOffhand, this.offhandAttackCooldown);
				return true;
			}
			else
			{

				/* Add an equip sound to the shield */
				if (this.itemStackOffhand.getItem() instanceof ItemShield)
				{
					this.betterCombatOffhand.equipSoundTimer = 5; /* 10 / 2 */
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
		SoundHandler.playSwingSoundRight(this.mc.player, this.betterCombatMainhand, this.itemStackMainhand, this.mainhandAttackCooldown);
	}

	public void offhandSwingSound()
	{
		this.mc.player.swingArm(EnumHand.OFF_HAND);
		SoundHandler.playSwingSoundLeft(this.mc.player, this.betterCombatOffhand, this.itemStackOffhand, this.offhandAttackCooldown);
	}

	private void mainhandEquipSound()
	{
		SoundHandler.playEquipSoundRight(this.mc.player, this.betterCombatMainhand, this.itemStackMainhand, this.mainhandAttackCooldown);
	}

	private void offhandEquipSound()
	{
		SoundHandler.playEquipSoundLeft(this.mc.player, this.betterCombatOffhand, this.itemStackOffhand, this.offhandAttackCooldown);
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

	public float getMainhandCooledAttackStrength()
	{
		return 1.0F - MathHelper.clamp((float) (1 + this.mainhandCooldown) / this.mainhandAttackCooldown, 0.0F, 1.0F);
	}

	public boolean isOffhandAttackReady()
	{
		return this.offhandCooldown <= 0;
	}

	public float getOffhandCooledAttackStrength()
	{
		return 1.0F - MathHelper.clamp((float) (1 + this.offhandCooldown) / this.offhandAttackCooldown, 0.0F, 1.0F);
	}

	public void resetMainhandCooldown(EntityPlayerSP player)
	{
		this.mainhandAttackCooldown = Helpers.getMainhandCooldown(player, this.itemStackMainhand, this.itemStackOffhand);

		// this.mc.player.sendChatMessage(EMPTY+this.mainhandAttackCooldown);

		if (this.mainhandAttackCooldown < ConfigurationHandler.minimumAttackSpeedTicks)
		{
			this.mainhandAttackCooldown = ConfigurationHandler.minimumAttackSpeedTicks;
		}

		this.mainhandCooldown = this.mainhandAttackCooldown;

		ClientProxy.AH_INSTANCE.resetEquippedProgressMainhand();
	}

	public void resetOffhandCooldown(EntityPlayerSP player)
	{
		this.offhandAttackCooldown = Helpers.getOffhandCooldown(player, this.itemStackOffhand, this.itemStackMainhand);

		// this.mc.player.sendChatMessage(EMPTY+this.offhandAttackCooldown);

		if (this.offhandAttackCooldown < ConfigurationHandler.minimumAttackSpeedTicks)
		{
			this.offhandAttackCooldown = ConfigurationHandler.minimumAttackSpeedTicks;
		}

		this.offhandCooldown = this.offhandAttackCooldown;

		ClientProxy.AH_INSTANCE.resetEquippedProgressOffhand();
	}

	/*
	 * =============================================================================
	 * =========================================================================
	 */
	/* TOOLTIP */
	/*
	 * =============================================================================
	 * =========================================================================
	 */

	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
	public void itemTooltipEventHigh(ItemTooltipEvent event)
	{

		if (ConfigurationHandler.isItemClassWhiteList(event.getItemStack().getItem()))
		{

			for (CustomWeapon s : ConfigurationHandler.weapons)
			{

				if (Helpers.getString(event.getItemStack()).contains(s.name))
				{
					this.updateBetterCombatTooltipHigh(s, event);
					return;
				}

			}

			this.updateBetterCombatTooltipHigh(ConfigurationHandler.DEFAULT_CUSTOM_WEAPON, event);
			return;
		}

		if (event.getItemStack().getItem() instanceof ItemShield)
		{

			for (CustomShield s : ConfigurationHandler.shields)
			{

				if (event.getItemStack().getItem().equals(s.shield))
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
		this.removeDuplicateBlankTooltips(event);

//		if ( ConfigurationHandler.isItemClassWhiteList(event.getItemStack().getItem()) )
//		{
//			
//		}
	}

	public static final String attackSpeedString   = I18n.format("attribute.name.generic.attackSpeed");
	public static final String attackDamageString  = I18n.format("attribute.name.generic.attackDamage");

	public static final String ATTACK_SPEED_REGEX  = "(([0-9]+\\.*[0-9]*)( *" + attackSpeedString + "))";
	public static final String ATTACK_DAMAGE_REGEX = "(([0-9]+\\.*[0-9]*)( *" + attackDamageString + "))";
	
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
	
	private void removeDuplicateBlankTooltips(ItemTooltipEvent event)
	{
	    List<String> nonBlankTooltips = new ArrayList<>();

	    boolean flag = false;
	    
	    for ( String tag : event.getToolTip() )
	    {
	        if ( tag.isEmpty() )
	        {
	        	if ( !flag )
    	        {
    	            nonBlankTooltips.add(tag);
    	        }
	        	
	        	flag = true;
	        }
	        else
	        {
	            nonBlankTooltips.add(tag);
	        }
	    }

	    event.getToolTip().clear();
	    event.getToolTip().addAll(nonBlankTooltips);
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

	private void updateBetterCombatTooltipHigh(CustomWeapon s, ItemTooltipEvent event)
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
		if (ConfigurationHandler.showCritChanceTooltip && ConfigurationHandler.baseCritPercentChance >= 0.0D )
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

		String mainhandString = I18n.format("item.modifiers.mainhand");
		int mainhandStringIndex = -1;

		boolean formattedAttackSpeed = false;

		boolean formattedAttackDamage = false;

		// int qualityToolsMainhandIndex = -1;

		int i = 0;

		for (String tag : event.getToolTip())
		{

			if (tag.contains(mainhandString))
			{

				if (mainhandStringIndex < 0)
				{
					mainhandStringIndex = i;
				}
				else
				{
					// qualityToolsMainhandIndex = i;
				}

			}
			else if (!formattedAttackSpeed && tag.contains(attackSpeedString))
			{

				if (event.getItemStack().getItem() instanceof ItemSword && !ConfigurationHandler.swords.isEmpty())
				{
					String str = Helpers.getString(event.getItemStack());

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

			if ( !ConfigurationHandler.showReachTooltipAsTotal && s.additionalReachMod != 0.0D )
			{
				if (!flag)
				{
					event.getToolTip().add(EMPTY);
				}
				event.getToolTip().add((s.additionalReachMod > 0.0D ? I18n.format("bettercombat.info.positive.color") : I18n.format("bettercombat.info.negative.color")) + s.additionalReachMod + I18n.format("bettercombat.info.reachDistance.text"));
				flag = true;
			}

		}

		if (ConfigurationHandler.showCritChanceTooltip && ConfigurationHandler.baseCritPercentChance >= 0.0D )
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

		if (ConfigurationHandler.showPotionEffectTooltip && s.customWeaponPotionEffect != null)
		{
			event.getToolTip().add(EMPTY);

			if (s.customWeaponPotionEffect.potionChance > 0.0F)
			{
				event.getToolTip().add((int) (s.customWeaponPotionEffect.potionChance * 100) + "%" + I18n.format("bettercombat.info.potionEffect.chance.text"));
			}
			else
			{
				event.getToolTip().add(I18n.format("bettercombat.info.potionEffect.crit.text"));
			}

			double seconds = Math.round((s.customWeaponPotionEffect.potionDuration * 0.5D)) * 0.1D;

			String str;

			if (seconds % 1 == 0)
			{
				str = (int) seconds + I18n.format("bettercombat.info.potionEffect.second.text") + ((int) seconds == 1 ? EMPTY : "s");
			}
			else
			{
				str = seconds + I18n.format("bettercombat.info.potionEffect.second.text");
			}

			event.getToolTip().add((s.customWeaponPotionEffect.afflict ? I18n.format("bettercombat.info.potionEffect.negative.text") : I18n.format("bettercombat.info.potionEffect.positive.text")) + I18n.format(s.customWeaponPotionEffect.getPotion().getName()) + BLANK_SPACE + Helpers.integerToRoman(s.customWeaponPotionEffect.potionPower) + (s.customWeaponPotionEffect.potionDuration > 0 ? I18n.format("bettercombat.info.potionEffect.for.text") + str : EMPTY));
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

	public static final float 	PI_FLOAT 			= (float)Math.PI;
	public static final double 	SQRT_2_DOUBLE 		= (double)MathHelper.SQRT_2;
	public static final double 	RADIAN_TO_DEGREE 	= 180.0D/Math.PI;
	
	/* returns true if the target entity is in view of in entity, uses head rotation to calculate */
	public static boolean isEntityInView( Entity in, Entity target )
	{
	   double rotation = (Math.atan2(target.posZ - in.posZ, target.posX - in.posX) * RADIAN_TO_DEGREE + 360) % 360 - (in.getRotationYawHead() + 450) % 360;
	   return (rotation <= 50 && rotation >= -50) || rotation >= 310 || rotation <= -310;
	}

	/* EntityRenderer.getMouseOver() */
	public RayTraceResult getMouseOverExtended(EntityPlayerSP player, double reach, float sweepWidth) // TODO /summon ender_dragon ~ ~ ~
	{
		if (this.mc.world == null)
		{
			return null;
		}

		final Vec3d lookEyes = player.getPositionEyes(this.mc.getRenderPartialTicks());
		final Vec3d lookVec = player.getLook(1.0F);
		final Vec3d lookTarget = lookEyes.addVector(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);

		Entity pointed = null;
		RayTraceResult pointedRayTraceResult = null;
				
		if ( this.mc.objectMouseOver != null && this.mc.objectMouseOver.entityHit != null && this.mc.objectMouseOver.hitVec != null )
		{
			double entityDistanceSq = lookEyes.squareDistanceTo(this.mc.objectMouseOver.hitVec);

			if ( entityDistanceSq <= reach*reach )
			{
				pointed = this.mc.objectMouseOver.entityHit;
				pointedRayTraceResult = this.mc.objectMouseOver;
			}
		}
		
		double closestDistance = reach - ConfigurationHandler.extraAttackWidth - sweepWidth; closestDistance *= closestDistance;
		
		if ( pointed == null )
		{
			/* List of entities within range */
			final List<Entity> list = this.mc.world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().expand(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach).grow(1.0D, 1.0D, 1.0D));
			
			for ( Entity entity : list )
			{				
				if ( entity == player.getRidingEntity() || !entity.canBeAttackedWithItem() )
				{
					continue;
				}
				
				if ( !isEntityInView(player, entity) )
				{
					continue;
				}
				
				AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize() + ConfigurationHandler.extraAttackWidth + sweepWidth, ConfigurationHandler.extraAttackHeight, entity.getCollisionBorderSize() + ConfigurationHandler.extraAttackWidth + sweepWidth);
		        RayTraceResult rayTraceResult = aabb.calculateIntercept(lookEyes, lookTarget);

				if ( rayTraceResult != null && rayTraceResult.hitVec != null )
				{
					double entityDistanceSq = lookEyes.squareDistanceTo(rayTraceResult.hitVec);

					if ( entityDistanceSq <= closestDistance )
					{
						pointed = entity;
						pointedRayTraceResult = rayTraceResult;
						closestDistance = entityDistanceSq;
					}
				}

			}
		}

		if ( pointed != null && pointedRayTraceResult != null )
		{
			/* Return the closest entity */
			
			pointedRayTraceResult.entityHit = pointed;
			pointedRayTraceResult.typeOfHit = Type.ENTITY;
			
			return pointedRayTraceResult;
		}
		else
		{
			pointedRayTraceResult = this.ridingMouseOverEntity(player);

			if ( pointedRayTraceResult != null )
			{
				/* Return the riding entity */
				
				pointedRayTraceResult.entityHit = pointed;
				pointedRayTraceResult.typeOfHit = Type.ENTITY;
				
				return pointedRayTraceResult;
			}
			
			/* Return ray trace block for swinging through grass */
			/* return pointedRayTraceResult = player.world.rayTraceBlocks(lookEyes, lookTarget, false, false, true); */
			/* However, grass is handled in EventHandlerClient */
			
			return null;
		}
	}

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

		if (player.isRiding() && player.getRidingEntity() instanceof IMob)
		{
			return new RayTraceResult(player.getRidingEntity());
		}
		else if (player.isBeingRidden())
		{

			for (Entity passenger : player.getPassengers())
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

		if (entityHit instanceof EntityPlayerMP)
		{
			return Helpers.execNullable(entityHit.getServer(), MinecraftServer::isPVPEnabled, false);
		}

		return true;
	}

	/*
	 * =============================================================================
	 * =========================================================================
	 */
	/*																					  																	  */
	/*
	 * =============================================================================
	 * =========================================================================
	 */

//	private void applyAttackInertia( EntityPlayerSP player )
//	{
//		if ( ConfigurationHandler.inertiaOnAttack != 1.0F )
//		{
//			player.motionX *= ConfigurationHandler.inertiaOnAttack;
//			player.motionZ *= ConfigurationHandler.inertiaOnAttack;
//
//			player.velocityChanged = true;
//		}
//		
//		if ( ConfigurationHandler.momentumOnAttack != 0.0F )
//		{
//			if ( player.onGround )
//			{
//				player.motionY += 0.001D;
//			}
//	
//			player.motionX += ConfigurationHandler.momentumOnAttack*MathHelper.sin(player.rotationYaw * 0.017453292F);
//			player.motionZ -= ConfigurationHandler.momentumOnAttack*MathHelper.cos(player.rotationYaw * 0.017453292F);
//			
//			player.velocityChanged = true;
//		}
//	}

	public void spawnSweepHit(EntityPlayer e, int x, int z)
	{
		double d0 = (double) (-MathHelper.sin(e.rotationYaw * 0.017453292F));
		double d1 = (double) MathHelper.cos(e.rotationYaw * 0.017453292F);

		e.world.spawnParticle(EnumParticleTypes.SWEEP_ATTACK, x + d0 * 0.5D, e.posY + e.height * 0.5D, z + d1 * 0.5D, 0.0D, 0.0D, 0.0D);
	}

	// PARTICLE
//	public static void spawnSweep( EntityPlayer e )
//	{
//		double d0 = -Math.sin(e.rotationYaw * 0.017453292F);
//		double d1 = Math.cos(e.rotationYaw * 0.017453292F);
//		
//		ParticleBuilder.create(ParticleType.SWEEP).pos(d0, e.getEyeHeight()-e.rotationPitch*0.016D-0.4D, d1).entity(e).spawn(e.world);
//	}
}