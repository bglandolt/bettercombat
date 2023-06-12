package bettercombat.mod.client.handler;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bettercombat.mod.client.BetterCombatHand;
import bettercombat.mod.client.ClientProxy;
import bettercombat.mod.network.PacketBreakBlock;
import bettercombat.mod.network.PacketFastEquip;
import bettercombat.mod.network.PacketFatigue;
import bettercombat.mod.network.PacketHandler;
import bettercombat.mod.network.PacketMainhandAttack;
import bettercombat.mod.network.PacketOffhandAttack;
import bettercombat.mod.network.PacketOnItemUse;
import bettercombat.mod.network.PacketShieldBash;
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
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
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

    public final Minecraft mc;
    
    public ItemStack itemStackMainhand = ItemStack.EMPTY;
	public ItemStack itemStackOffhand = ItemStack.EMPTY;

	public final BetterCombatHand betterCombatMainhand = new BetterCombatHand();
	public final BetterCombatHand betterCombatOffhand = new BetterCombatHand();
	
	/* Ticker for offhand cooldown. If offhand cooldown is 0, the attack is ready */
	public int offhandCooldown = 0;
	
	/* The offhand attack cooldown is the number the offhand cooldown gets set to when making an attack */
	public int offhandAttackCooldown = ConfigurationHandler.minimumAttackSpeedTicks;

	/* Ticker for mainhand cooldown. If minitiateAnimationainhand cooldown is 0, the attack is ready */
	public int mainhandCooldown = 0;
	
	/* The mainhand attack cooldown is the number the mainhand cooldown gets set to when making an attack */
	public int mainhandAttackCooldown = ConfigurationHandler.minimumAttackSpeedTicks;
	
//	private boolean keyBindAttackIsKeyDown = false;
	private boolean keyBindUseItemIsKeyDown = false;
	
    /* ====================================================================================================================================================== */
    /* ====================================================================================================================================================== */
    /*																LEFT CLICK - MAIN HAND																	  */
    /* ====================================================================================================================================================== */
    /* ====================================================================================================================================================== */

	public boolean overwriteLeftClick( boolean checkBlocks )
	{
		EntityPlayerSP player = this.mc.player;
		
		/* If the player is not valid, */
		if ( this.invalidPlayer(player) )
		{
			/* Cancel left-click! */
			return true;
		}
		
		/* Check to see if the items have changed */
		this.checkItemstacksChanged(false);
		
		RayTraceResult mov = mc.objectMouseOver;
		
		if ( mov == null )
		{
			/* Cancel left-click! */
			return true;
		}
		
		/* ----------------------------------------- */
		/*                SHIELD BASH                */
		/* ----------------------------------------- */
		
		/* If the player has an active item such as a bow or shield, */
		if ( !player.getActiveItemStack().isEmpty() )
		{
			/* If the player is blocking, */
			if ( player.isActiveItemStackBlocking() && player.getItemInUseCount() > 0 && player.getActiveHand().equals(EnumHand.OFF_HAND) )
			{
				Item shield = player.getActiveItemStack().getItem();

				/* If the shield is on cooldown */
				if ( player.getCooldownTracker().hasCooldown(shield) )
				{
					/* Cancel left-click! */
					return true;
				}
				
				/* 30 is the default cooldown */
				int bashCooldown = 30;

				for ( CustomShield s : ConfigurationHandler.shields )
				{
					if ( shield.equals(s.shield) )
					{
						bashCooldown = s.cooldown;
						break;
					}
				}
				
				/* offhandCooldown used for crosshair cooldown display */
				this.offhandCooldown = bashCooldown;
				this.offhandAttackCooldown = bashCooldown;
				
				/* Set the internal shield cooldown */
				player.getCooldownTracker().setCooldown(shield, bashCooldown);
				
				player.stopActiveHand();

				/* animate the shield bash */
				ClientProxy.EHC_INSTANCE.betterCombatOffhand.setShieldBashing();
								
				/* Cancel left-click! */
				return true;
			}
			
			/* Cancel left-click, as the player has an active item and should not attack! */
			return true;
		}
				
		/* If this left-click should check blocks, */
		if ( checkBlocks )
		{
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

				/* If the block is a PLANT, */
				if ( block instanceof IPlantable || block instanceof IShearable )
				{
					/* If the MAINHAND is a HOE, */
					if ( this.itemStackMainhand.getItem() instanceof ItemHoe )
					{
						/* Continue with left-click, using the HOE on the PLANT! */
						return false;
					}
					else
					{
						/* MINING! Continue with left-click! */
						return mining();
					}
				}
				else
				{
					/* MINING! Continue with left-click! */
					return mining();
				}
			}
			else if ( this.mc.gameSettings.keyBindAttack.isKeyDown() )
			{
				/* MINING! Continue with left-click! */
				return false;
			}
		}
		
		/* If the MAINHAND attack is not ready, */
		if ( !this.isMainhandAttackReady() )
		{
			/* Cancel left-click! */
			return true;
		}
				
		/* If the MAINHAND and OFFHAND is NOT empty, */
		if ( !this.itemStackMainhand.isEmpty() && !this.itemStackOffhand.isEmpty() )
		{
			/* Set the fatigue to the custom weapon fatigue */
			int fatigue = this.betterCombatMainhand.getFatigue() + this.betterCombatOffhand.getFatigue()/2;
			
			/* Send a fatigue packet if there is fatigue */
			if ( fatigue > 0 )
			{
				PacketHandler.instance.sendToServer(new PacketFatigue(fatigue));
			}
		}
		
		/* ----------------------------------------- */
		/*             	  SWING WEAPON               */
		/* ----------------------------------------- */
		
		/* Reset the MAINHAND cooldown so the player cannot attack for a period of time */
		this.resetMainhandCooldown(player);
		this.mc.player.resetCooldown();

		/* SWING! Initiate the MAINHAND animation for attacking */
		this.betterCombatMainhand.initiateAnimation(this.mainhandCooldown);
				
		/* Set the shield cooldown */
//		if ( ConfigurationHandler.disableShieldAfterAttack && this.itemStackOffhand.getItem() instanceof ItemShield )
//		{
//			player.getCooldownTracker().setCooldown(this.itemStackOffhand.getItem(), this.mainhandCooldown);
//		}
						
		return true;
	}
	
    private boolean mining()
    {
    	/* If the MAINHAND is ready to begin a swing animation, */
		if ( this.betterCombatMainhand.getSwingTimer() <= 0 )
		{
			/* Start the MAINHAND mining animation with a set mining speed XXX */
			this.betterCombatMainhand.startMining();
		}
		
		return false;
	}

//	private void leftClickMouse()
//    {
//        //if (this.mc.leftClickCounter <= 0)
//        {
//            if (this.mc.objectMouseOver == null)
//            {
////                LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
////
////                if (this.mc.playerController.isNotCreative())
////                {
////                    this.mc.leftClickCounter = 10;
////                }
//            }
//            else if (!this.mc.player.isRowingBoat())
//            {
//                switch (this.mc.objectMouseOver.typeOfHit)
//                {
//                    case ENTITY:
//                        this.mc.playerController.attackEntity(this.mc.player, this.mc.objectMouseOver.entityHit);
//                        break;
//                    case BLOCK:
//                        BlockPos blockpos = this.mc.objectMouseOver.getBlockPos();
//
//                        if (!this.mc.world.isAirBlock(blockpos))
//                        {
//                            this.mc.playerController.clickBlock(blockpos, this.mc.objectMouseOver.sideHit);
//                            break;
//                        }
//
//                    case MISS:
//
////                        if (this.mc.playerController.isNotCreative())
////                        {
////                            this.mc.leftClickCounter = 10;
////                        }
//
//                        this.mc.player.resetCooldown();
//                        net.minecraftforge.common.ForgeHooks.onEmptyLeftClick(this.mc.player);
//                }
//
//                this.mc.player.swingArm(EnumHand.MAIN_HAND);
//            }
//        }
//    }

	/* ====================================================================================================================================================== */
    /* ====================================================================================================================================================== */
    /*																ATTACK - MAIN HAND 																		  */
    /* ====================================================================================================================================================== */
    /* ====================================================================================================================================================== */

	public void mainhandAttack()
	{
		RayTraceResult mov = mc.objectMouseOver;
		
		/* If the vanilla mouseover does not exist OR if there is no entity hit, */
		if ( mov == null || mc.objectMouseOver.entityHit == null )
		{
			/* Set the MOV to bettercombat mouseover */
			mov = this.getMainhandMouseover();
		}
		
		/* If, the MOV is valid, */
		if ( mov != null )
		{
			/* If the MOV is targeting a block, */
			if ( mov.typeOfHit == Type.BLOCK && mov.getBlockPos() != null && mov.getBlockPos() != BlockPos.ORIGIN )
			{
				/* If the MAINHAND item can interact with that block, */
				if ( this.itemCanInteractWithBlock(this.itemStackMainhand.getItem()) )
				{
					if ( this.itemStackMainhand.getItem().onItemUse(this.mc.player, this.mc.player.world, mov.getBlockPos(), EnumHand.MAIN_HAND, mov.sideHit, 0.0F, 0.0F, 0.0F) == EnumActionResult.SUCCESS )
					{
						/* HIT! Send a packet that uses the item on the block! */
						PacketHandler.instance.sendToServer(new PacketOnItemUse(mov.getBlockPos().getX(), mov.getBlockPos().getY(), mov.getBlockPos().getZ(), mov.sideHit));
						return;
					}
				}
				
				if ( this.swingThroughGrass(mov.getBlockPos()) )
				{
					/* Get a new MOV after the grass or plant has been broken */
					mov = this.getMainhandMouseover();
				}
			}
			
			/* If the MOV is targeting an entity AND if it is a player, can it be PVPd  */
			if ( mov.entityHit != null && this.canPVP(mov.entityHit, this.mc.player) )
			{				
				/* HIT! Send an attack packet with a target! */
				PacketHandler.instance.sendToServer(new PacketMainhandAttack(mov.entityHit.getEntityId()));
				return;
			}
		}
		
		/* MISS! Send an attack packet with NO target! */
		PacketHandler.instance.sendToServer(new PacketMainhandAttack());
	}
	
    private boolean swingThroughGrass( BlockPos pos )
    {
		if ( pos == null )
		{
			return false;
		}
		
		Block block = this.mc.player.world.getBlockState(pos).getBlock();
		
		/* If the block is a plant or grass, */
		if ( block instanceof IPlantable || block instanceof IShearable )
		{
			PacketHandler.instance.sendToServer(new PacketBreakBlock(pos.getX(), pos.getY(), pos.getZ()));
			this.spawnSweepHit(this.mc.player, pos.getX(), pos.getZ());					

			do
			{
				pos = pos.up();
				block = this.mc.player.world.getBlockState(pos).getBlock();
				
				if ( pos != null && (block instanceof IPlantable || block instanceof IShearable) )
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

	private boolean itemCanInteractWithBlock( Item item )
    {
    	return item instanceof ItemTool || item instanceof ItemHoe;
	}

	private RayTraceResult getMainhandMouseover()
    {
    	return getMouseOverExtended(this.mc.player, Helpers.getMainhandReach(this.mc.player, this.betterCombatMainhand.getAdditionalReach()), this.getExtraSweepWidth(this.betterCombatMainhand.getSweep()));
	}

	/* ====================================================================================================================================================== */
    /* ====================================================================================================================================================== */
    /*																	ATTACK - OFF HAND																	  */
    /* ====================================================================================================================================================== */
    /* ====================================================================================================================================================== */
	
	public void offhandAttack()
	{
		RayTraceResult mov = mc.objectMouseOver;
				
		if ( mov == null || mc.objectMouseOver.entityHit == null )
		{
			mov = this.getOffhandMouseover();
		}
		
		if ( mov != null )
		{
			if ( mov.typeOfHit == Type.BLOCK && mov.getBlockPos() != null && mov.getBlockPos() != BlockPos.ORIGIN )
			{
				if ( this.itemStackOffhand.getItem() instanceof ItemTool || this.itemStackOffhand.getItem() instanceof ItemHoe )
				{
					PacketHandler.instance.sendToServer(new PacketOnItemUse(mov.getBlockPos().getX(), mov.getBlockPos().getY(), mov.getBlockPos().getZ(), mov.sideHit));
					
					if ( this.itemStackOffhand.getItem().onItemUse(this.mc.player, this.mc.player.world, mov.getBlockPos(), EnumHand.OFF_HAND, mov.sideHit, 0.0F, 0.0F, 0.0F) == EnumActionResult.SUCCESS )
					{
						return;
					}
				}
				
				if ( this.swingThroughGrass(mov.getBlockPos()) )
				{
					/* Get a new MOV after the grass or plant has been broken */
					mov = this.getOffhandMouseover();
				}
			}
			
			if ( mov != null && mov.entityHit != null && this.canPVP(mov.entityHit, this.mc.player) && ConfigurationHandler.rightClickAttackable(mov.entityHit) )
			{
				if ( this.itemStackOffhand.getItem() instanceof ItemShield )
				{
					PacketHandler.instance.sendToServer(new PacketShieldBash(mov.entityHit.getEntityId()));
				}
				else
				{
					PacketHandler.instance.sendToServer(new PacketOffhandAttack(mov.entityHit.getEntityId()));
				}
				return;
			}
		}
		
		if ( this.itemStackOffhand.getItem() instanceof ItemShield )
		{
			PacketHandler.instance.sendToServer(new PacketShieldBash());
		}
		else
		{
			PacketHandler.instance.sendToServer(new PacketOffhandAttack());
		}
	}
	
	private RayTraceResult getOffhandMouseover()
    {
    	return getMouseOverExtended(this.mc.player, Helpers.getMainhandReach(this.mc.player, this.betterCombatOffhand.getAdditionalReach()), this.getExtraSweepWidth(this.betterCombatOffhand.getSweep()));
	}
	
    /* ====================================================================================================================================================== */
    /* ====================================================================================================================================================== */
    /*																	RIGHT CLICK - MAIN HAND 	 														  */
    /* ====================================================================================================================================================== */
    /* ====================================================================================================================================================== */
	
	/* Return TRUE to overwrite/cancel the default click
	 * Return FALSE to use the default click
	 * 
	 * MAINHAND has priority for using items and interacting 
	 */
	public boolean overwriteRightClick()
	{
		EntityPlayerSP player = this.mc.player;
		
		/* If the player is not valid, */
		if ( this.invalidPlayer(player) )
		{
			/* Cancel right-click! */
			return true;
		}
		
		/* Check to see if the ItemStacks have changed */
		this.checkItemstacksChanged(false);
		
		RayTraceResult mov = mc.objectMouseOver;
		
		if ( mov == null )
		{
			/* Cancel right-click! */
			return true;
		}

		if ( this.itemStackMainhand.isEmpty() && this.itemStackOffhand.isEmpty() )
		{
			/* Continue with right-click! */
			return false;
		}
		
		/* If the MAINHAND has a TWOHAND weapon, */
		if ( this.betterCombatMainhand.getWeaponProperty() == WeaponProperty.TWOHAND )
		{
			this.rightClickMouse(EnumHand.MAIN_HAND);

			/* Cancel right-click! */
			return true;
		}

		/* If the OFFHAND has a TWOHAND or MAINHAND weapon, */
		if ( this.betterCombatOffhand.getWeaponProperty() == WeaponProperty.TWOHAND || this.betterCombatOffhand.getWeaponProperty() == WeaponProperty.MAINHAND )
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
				
				Block block = player.world.getBlockState(pos).getBlock();
				
				if ( this.useTools(player, block) )
				{
					/* Cancel right-click! */
					return true;
				}
			}
			
			this.rightClickMouse(EnumHand.MAIN_HAND);
			/* Cancel right-click! */
			return true;
		}
		
		/* If the MAINHAND has an action, OR if the OFFHAND has an action, */
		if ( this.itemStackMainhand.getItemUseAction() != EnumAction.NONE || this.itemStackOffhand.getItemUseAction() != EnumAction.NONE )
		{
			/* If the MAINHAND has a BLOCK action, */
			if ( this.itemStackMainhand.getItemUseAction() == EnumAction.BLOCK )
			{
				/* Cancel right-click! */
				return true;
			}
			
			/* !this.isMainhandAttackReady() */
			if ( this.itemStackOffhand.getItemUseAction() == EnumAction.BLOCK )
			{
//				if ( player.getCooldownTracker().hasCooldown(this.itemStackOffhand.getItem()) )
//				{
//					player.stopActiveHand();
//					return true;
//				}
//				else if ( ConfigurationHandler.disableBlockingWhileAttacking && !this.isMainhandAttackReady() )
//				{
//					player.stopActiveHand();
//					return true;
//				}
			}
			
			/* Continue with right-click and use item! */
			return false;
		}
				
		/* If targeting a block, */
		if ( mov.typeOfHit == RayTraceResult.Type.BLOCK )
		{
			BlockPos pos = mov.getBlockPos();
	
			/* If that position is invalid, */
			if ( pos == null && pos == BlockPos.ORIGIN )
			{
				/* Cancel right-click, as there was an error! */
				return true;
			}
			
			Block block = player.world.getBlockState(pos).getBlock();
			
			// !block.hasTileEntity(block.getDefaultState())
			
			/* If the block is a PLANT and the MAINHAND OR OFFHAND has a HOE, */
			if ( (block instanceof IPlantable || block instanceof IShearable) && (this.itemStackMainhand.getItem() instanceof ItemHoe || this.itemStackOffhand.getItem() instanceof ItemHoe) )
			{
				/* Continue with right-click, using the HOE on the PLANT! */
				return false;
			}
			/* 
			 * Otherwise, if the block is NOT a Tile Entity, such as a chest or bed,
			 * AND the MAINHAND has the ability to interact, such as tilling or stripping bark,
			 */
			else if ( this.itemStackMainhand.getItem() instanceof ItemTool || this.itemStackOffhand.getItem() instanceof ItemTool )
			{
				return this.useTools(player, block);
			}
			else
			{
				/* Continue with right-click! */
				return false;
			}
		}
		/* Otherwise, if there is no entity hit and the mainhand does not have a weapon, continue with the right click and do not attack, instead use the mainhand item */
		else if ( mov.entityHit == null && !this.betterCombatMainhand.hasCustomWeapon() )
		{
			/* Continue with right-click! */
			return false;
		}
		/* Otherwise, if the keybind is held down, */
		else if ( this.keyBindUseItemIsKeyDown ) // XXX
		{
			/* Continue with right-click! */
			return false;
		}
		/* Otherwise, if there is a target and it is NOT attackable with the OFFHAND, */
		else if ( mov.entityHit != null && !ConfigurationHandler.rightClickAttackable(mov.entityHit) )
		{
			/* Continue with right-click, interact with entity! */
			return false;
		}
		
		if ( player.isSneaking() && ConfigurationHandler.sneakingDisablesOffhandAttack )
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
		/*             	  SWING WEAPON               */
		/* ----------------------------------------- */		
		
		return this.rightClick(player);
	}
	
	private boolean useTools( EntityPlayerSP player, Block block )
	{
		if ( ConfigurationHandler.grassPathingRequiresAnimation )
		{
			if ( block instanceof BlockGrass )
			{
				if ( this.itemStackMainhand.getItem() instanceof ItemSpade )
				{
					if ( this.isMainhandAttackReady() )
					{
						this.overwriteLeftClick(false);
					}
					return true;
				}
				else if ( this.itemStackOffhand.getItem() instanceof ItemSpade )
				{
					if ( this.isOffhandAttackReady() )
					{
						this.rightClick(player);
					}
					return true;
				}
			}
		}
		
		if ( ConfigurationHandler.tillingRequiresAnimation )
		{
			if ( block instanceof BlockDirt || block instanceof BlockGrass ||  block instanceof BlockGrassPath )
			{
				if ( this.itemStackMainhand.getItem() instanceof ItemHoe )
				{
					if ( this.isMainhandAttackReady() )
					{
						this.overwriteLeftClick(false);
					}
					return true;
				}
				else if ( this.itemStackOffhand.getItem() instanceof ItemHoe )
				{
					if ( this.isOffhandAttackReady() )
					{
						this.rightClick(player);
					}
					return true;
				}
			}
		}
		else if ( ConfigurationHandler.strippingBarkRequiresAnimation )
		{
			if ( block instanceof BlockLog )
			{
				if ( this.itemStackMainhand.getItem() instanceof ItemAxe )
				{
					if ( this.isMainhandAttackReady() )
					{
						this.overwriteLeftClick(false);
					}
					return true;
				}
				else if ( this.itemStackOffhand.getItem() instanceof ItemAxe )
				{
					if ( this.isOffhandAttackReady() )
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

	private boolean rightClick( EntityPlayerSP player )
	{
		if ( this.betterCombatOffhand.hasCustomWeapon() )
		{
			/* If both the MAINHAND AND OFFHAND have items, */
			if ( !this.itemStackMainhand.isEmpty() && !this.itemStackOffhand.isEmpty() )
			{
				/* Set the fatigue to the custom weapon fatigue */
				int fatigue = this.betterCombatOffhand.getFatigue() + this.betterCombatMainhand.getFatigue()/2;
				
				/* Send a fatigue packet if there is fatigue */
				if ( fatigue > 0 )
				{
					PacketHandler.instance.sendToServer(new PacketFatigue(fatigue));
				}
			}
			
			/* Reset the OFFHAND cooldown so the player cannot attack for a period of time */
			this.resetOffhandCooldown(player);
			
			/* SWING! Initiate the OFFHAND animation for attacking */
			this.betterCombatOffhand.initiateAnimation(this.offhandCooldown);
			
			/* Cancel right-click! */
			return true;
		}
		
		/* Continue with right-click, use item! */
		return false;
	}
	
	@SuppressWarnings("incomplete-switch")
    private void rightClickMouse( EnumHand enumhand )
    {
        if ( !this.mc.playerController.getIsHittingBlock() )
        {
            //this.mc.rightClickDelayTimer = 4;

            if ( !this.mc.player.isRowingBoat() )
            {
                if ( this.mc.objectMouseOver == null )
                {
                    return;
                }
                
                ItemStack itemstack = this.mc.player.getHeldItem(enumhand);

                if (this.mc.objectMouseOver != null)
                {
                    switch (this.mc.objectMouseOver.typeOfHit)
                    {
                        case ENTITY:
                        {
                            if (this.mc.playerController.interactWithEntity(this.mc.player, this.mc.objectMouseOver.entityHit, this.mc.objectMouseOver, enumhand) == EnumActionResult.SUCCESS)
                            {
                                return;
                            }

                            if (this.mc.playerController.interactWithEntity(this.mc.player, this.mc.objectMouseOver.entityHit, enumhand) == EnumActionResult.SUCCESS)
                            {
                                return;
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

                    			if ( this.itemStackOffhand.getItem() instanceof ItemHoe || this.itemStackOffhand.getItem() instanceof ItemSpade )
                    			{
                    				this.mc.player.inventory.offHandInventory.set(0, new ItemStack(Items.AIR));
                    				enumactionresult = this.mc.playerController.processRightClickBlock(this.mc.player, this.mc.world, blockpos, this.mc.objectMouseOver.sideHit, this.mc.objectMouseOver.hitVec, enumhand);
                    				this.mc.player.inventory.offHandInventory.set(0, this.itemStackOffhand);
                    			}
                    			else
                    			{
                    				enumactionresult = this.mc.playerController.processRightClickBlock(this.mc.player, this.mc.world, blockpos, this.mc.objectMouseOver.sideHit, this.mc.objectMouseOver.hitVec, enumhand);
                    			}

                                if ( enumactionresult == EnumActionResult.SUCCESS )
                                {
                                    this.mc.player.swingArm(enumhand);

                                    if (!itemstack.isEmpty() && (itemstack.getCount() != i || this.mc.playerController.isInCreativeMode()))
                                    {
                                        this.mc.entityRenderer.itemRenderer.resetEquippedProgress(enumhand);
                                    }

                                    return;
                                }
                            }
                        }
                    }
                }

                if (itemstack.isEmpty() && (this.mc.objectMouseOver == null || this.mc.objectMouseOver.typeOfHit == RayTraceResult.Type.MISS)) net.minecraftforge.common.ForgeHooks.onEmptyClick(this.mc.player, enumhand);
                if (!itemstack.isEmpty() && this.mc.playerController.processRightClick(this.mc.player, this.mc.world, enumhand) == EnumActionResult.SUCCESS)
                {
                    this.mc.entityRenderer.itemRenderer.resetEquippedProgress(enumhand);
                    return;
                }
            }
        }
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

	/* ====================================================================================================================================================== */
    /*																		KEYBINDS																		  */
    /* ====================================================================================================================================================== */

	@SubscribeEvent( priority = EventPriority.HIGHEST, receiveCanceled = true )
	public void onMouseEvent( MouseEvent event )
	{
		KeyBinding rightClick = mc.gameSettings.keyBindUseItem; /* -1 */

		if ( event.isButtonstate() && event.getButton() == rightClick.getKeyCode() + 100 )
		{
			this.keyBindUseItemIsKeyDown = false;
			
			if ( this.overwriteRightClick() )
			{
				event.setResult(Result.DENY);
				event.setCanceled(true);
			}
		}

		KeyBinding leftClick = mc.gameSettings.keyBindAttack; /* 0 */

		if ( event.isButtonstate() && event.getButton() == leftClick.getKeyCode() + 100 )
		{
			if ( this.overwriteLeftClick(true) )
			{
				event.setResult(Result.DENY);
				event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent( priority = EventPriority.HIGHEST, receiveCanceled = true )
	public void onKeyEvent( KeyInputEvent event )
	{
		if ( ClientProxy.fastEquip.isPressed() && this.isMainhandAttackReady() && this.isOffhandAttackReady() )
		{
			PacketHandler.instance.sendToServer(new PacketFastEquip());
		}

		if ( this.mc.gameSettings.keyBindUseItem.getKeyCode() != -99 && this.mc.gameSettings.keyBindUseItem.isPressed() )
		{
			Reflections.unpressKey(this.mc.gameSettings.keyBindUseItem);
			this.mc.gameSettings.keyBindUseItem = new KeyBinding("key.use", -99, "key.categories.gameplay");
			this.mc.gameSettings.keyBindUseItem.setToDefault();
		}
		
		if ( this.mc.gameSettings.keyBindAttack.getKeyCode() != -100 && this.mc.gameSettings.keyBindAttack.isPressed() )
		{
			Reflections.unpressKey(this.mc.gameSettings.keyBindAttack);
			this.mc.gameSettings.keyBindAttack = new KeyBinding("key.attack", -100, "key.categories.gameplay");
			this.mc.gameSettings.keyBindAttack.setToDefault();
		}
	}
	
    /* ====================================================================================================================================================== */
    /*																	TICK UDPATE																			  */
    /* ====================================================================================================================================================== */
	
	@SubscribeEvent( priority = EventPriority.LOW, receiveCanceled = true )
	public void tickEventLow( TickEvent.ClientTickEvent event )
	{		
		if ( event.phase == TickEvent.Phase.END && this.mc.player != null )
		{
			/* Too close animation */
			if ( this.mc.objectMouseOver != null )
			{
				double x;
				double z;
				
				if ( this.mc.objectMouseOver.hitVec != null )
				{
					x = this.mc.player.posX - this.mc.objectMouseOver.hitVec.x;
					z = this.mc.player.posZ - this.mc.objectMouseOver.hitVec.z;
					
//					if ( xx < 0.6D && zz < 0.6D )
//					{
//						ClientProxy.AH_INSTANCE.tooCloseAmount = MathHelper.clamp(1.2D - (xx + zz) * 2.0F, 0.0D, 0.3D);
//						ClientProxy.AH_INSTANCE.tooClose = true;
//					}
					
					if ( (x = x*x) < 0.7D && (z = z*z) < 0.7D )
					{
						ClientProxy.AH_INSTANCE.tooCloseAmount = MathHelper.clamp(0.5D - (x + z) * 0.25D, 0.1D, 0.4D);
						ClientProxy.AH_INSTANCE.tooClose = true;
					}
					else
					{
						ClientProxy.AH_INSTANCE.tooClose = false;
					}
				}
				else if ( mc.objectMouseOver.entityHit != null )
				{
					x = this.mc.player.posX - (this.mc.objectMouseOver.entityHit.posX + this.mc.objectMouseOver.entityHit.width * 0.5D);
					z = this.mc.player.posZ - (this.mc.objectMouseOver.entityHit.posZ + this.mc.objectMouseOver.entityHit.width * 0.5D);
					
					if ( (x = x*x) < 0.7D && (z = z*z) < 0.7D )
					{
						ClientProxy.AH_INSTANCE.tooCloseAmount = MathHelper.clamp(0.5D - (x + z) * 0.25D, 0.1D, 0.4D);
						ClientProxy.AH_INSTANCE.tooClose = true;
					}
					else
					{
						ClientProxy.AH_INSTANCE.tooClose = false;
					}
				}
			}
			
			if ( this.mc.gameSettings.keyBindAttack.isKeyDown() )
			{
				this.overwriteLeftClick(true);
			}
			 
//			if ( this.mc.gameSettings.keyBindUseItem.isKeyDown() && this.overwriteRightClick() )
//			{
//				this.keyBindUseItemIsKeyDown = true;
//			}
//			else
//			{
//				this.keyBindUseItemIsKeyDown = false;
//			}

//			this.mc.player.sendChatMessage( "d " + this.mc.gameSettings.keyBindUseItem.isKeyDown() + "p " + this.mc.gameSettings.keyBindUseItem.isPressed() );
//			Reflections.unpressKey(this.mc.gameSettings.keyBindUseItem);
//			this.mc.player.sendChatMessage( "d " + this.mc.gameSettings.keyBindUseItem.isKeyDown() + "p " + this.mc.gameSettings.keyBindUseItem.isPressed() );
			
			if ( this.betterCombatMainhand.getSwingTimer() > 0 )
			{
				this.betterCombatMainhand.tick();
				
				if ( this.betterCombatMainhand.isMining() )
				{
					
				}
				else
				{
					if ( this.betterCombatMainhand.damageReady() )
					{
						this.mainhandAttack();
					}
					else if ( this.betterCombatMainhand.soundReady() )
					{
						this.mainhandSwingSound();
					}
				}
				
				if ( ConfigurationHandler.disableBlockingWhileAttacking && this.mc.player.isHandActive() && this.mc.player.getActiveHand().equals(EnumHand.OFF_HAND) )
				{
					this.mc.player.resetActiveHand();
				}
			}
			else if ( this.betterCombatMainhand.equipSoundTimer > 0 && --this.betterCombatMainhand.equipSoundTimer <= 0 )
			{
				this.mainhandEquipSound();
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
				
				if ( ConfigurationHandler.disableBlockingWhileShieldBashing && this.mc.player.isHandActive() && this.mc.player.getActiveHand().equals(EnumHand.OFF_HAND) )
				{
					this.mc.player.resetActiveHand();
				}
			}
			else if ( this.betterCombatOffhand.equipSoundTimer > 0 && --this.betterCombatOffhand.equipSoundTimer <= 0 )
			{
				this.offhandEquipSound();
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
	
	public static double clamp(double d0, double d1)
	{
		if ( d0 > d1 )
		{
			return d1;
		}
		
		return d0;
	}

	@SubscribeEvent( priority = EventPriority.HIGH, receiveCanceled = true )
	public void livingUpdate( TickEvent.ClientTickEvent event )
	{
		if ( this.mc.player != null )
		{
			ClientProxy.AH_INSTANCE.breatheTicks += ConfigurationHandler.breathingAnimationSpeed;
			this.checkItemstacksChanged(false);
		}
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
	
	public void checkItemstacksChanged( boolean force )
	{
		this.checkItemstackChangedOffhand(force);
		this.checkItemstackChangedMainhand(force);
	}
	
    public void checkItemstackChangedMainhand( boolean force )
    {
        if ( !ItemStack.areItemsEqualIgnoreDurability(this.itemStackMainhand, this.mc.player.getHeldItemMainhand()) || force )
        {
        	if ( !force && this.betterCombatMainhand.equipSoundTimer <= 0 && this.betterCombatMainhand.hasCustomWeapon() )
        	{
        		SoundHandler.playSheatheSoundRight(this.mc.player, this.betterCombatMainhand, this.itemStackMainhand, this.mainhandAttackCooldown, Helpers.isMetal(this.itemStackMainhand));
        	}
        	            
        	/* Previous Weapon */
            if ( !this.mc.player.getHeldItemMainhand().isEmpty() )
            {
            	try
            	{
            		this.mc.player.getAttributeMap().removeAttributeModifiers(this.mc.player.getHeldItemMainhand().getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
            	}
            	catch ( Exception e )
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
            	catch ( Exception e )
            	{

            	}
            }
            
    		this.resetMainhandCooldown(this.mc.player);
            
            if ( ConfigurationHandler.isItemWhiteList(this.itemStackMainhand.getItem()) )
            {
	            String mainhandString = Helpers.getString(this.itemStackMainhand);
	            	            
	            for ( CustomWeapon weapon : ConfigurationHandler.weapons )
				{
					if ( mainhandString.contains(weapon.name) )
					{
						/* Config weapon found! */
		    			this.betterCombatMainhand.setBetterCombatWeapon(weapon, this.itemStackMainhand, this.mainhandAttackCooldown);
						return;
					}
				}
	            
	            /* No config weapon found, but it is a weapon! */
    			this.betterCombatMainhand.setBetterCombatWeapon(ConfigurationHandler.DEFAULT_CUSTOM_WEAPON, this.itemStackMainhand, this.mainhandAttackCooldown);
    			return;
            }
            else
            {
                /* No valid weapon found! */
                this.betterCombatMainhand.resetBetterCombatWeapon();
                
                /* Add an equip sound to the shield */
                if ( this.itemStackMainhand.getItem() instanceof ItemShield )
	            {
		            this.betterCombatMainhand.equipSoundTimer = 5; // 10 / 2
	            }
                
                return;
            }
        }
    }
    
    public void checkItemstackChangedOffhand( boolean force )
    {
        if ( !ItemStack.areItemsEqualIgnoreDurability(this.itemStackOffhand, this.mc.player.getHeldItemOffhand()) || force )
        {
        	if ( !force && this.betterCombatOffhand.equipSoundTimer <= 0 && this.betterCombatOffhand.hasCustomWeapon() )
        	{
        		SoundHandler.playSheatheSoundLeft(this.mc.player, this.betterCombatOffhand, this.itemStackOffhand, this.offhandAttackCooldown, Helpers.isMetal(this.itemStackOffhand));
        	}
        	
        	/* Previous Weapon */
        	try
        	{
        		this.mc.player.getAttributeMap().removeAttributeModifiers(this.mc.player.getHeldItemOffhand().getAttributeModifiers(EntityEquipmentSlot.MAINHAND));
        	}
        	catch ( Exception e )
        	{

        	}
  
            /* Swap */
            this.itemStackOffhand = this.mc.player.getHeldItemOffhand();

            /* Current Weapon */
            
    		this.resetOffhandCooldown(this.mc.player);
            
            if ( ConfigurationHandler.isItemWhiteList(this.itemStackOffhand.getItem()) )
            {
	            String offhandString = Helpers.getString(this.itemStackOffhand);
	            
	            for ( CustomWeapon weapon : ConfigurationHandler.weapons )
				{
	            	if ( offhandString.contains(weapon.name) )
					{
		    			this.betterCombatOffhand.setBetterCombatWeapon(weapon, this.itemStackOffhand, this.offhandAttackCooldown);
		    			return;
					}
				}
	            
	            /* No config weapon found, but it is a weapon! */
    			this.betterCombatOffhand.setBetterCombatWeapon(ConfigurationHandler.DEFAULT_CUSTOM_WEAPON, this.itemStackOffhand, this.offhandAttackCooldown);
    			return;
            }
            else
            {
	            /* No valid weapon found! */
	            this.betterCombatOffhand.resetBetterCombatWeapon();
	            
                /* Add an equip sound to the shield */
	            if ( this.itemStackOffhand.getItem() instanceof ItemShield )
	            {
		            this.betterCombatOffhand.equipSoundTimer = 5; // 10 / 2
	            }
	            
	            return;
            }
        }
    }
    
    /* ====================================================================================================================================================== */
    /*																	SOUND  																				  */
    /* ====================================================================================================================================================== */

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
	
    /* ====================================================================================================================================================== */
    /*																	COOLDOWNS																			  */
    /* ====================================================================================================================================================== */

	public boolean isMainhandAttackReady()
	{
		return this.mainhandCooldown <= 0;
	}
	
	public float getMainhandCooledAttackStrength()
	{
		return 1.0F - MathHelper.clamp((float)(1 + this.mainhandCooldown) / this.mainhandAttackCooldown, 0.0F, 1.0F);
	}
	
	public boolean isOffhandAttackReady()
	{
		return this.offhandCooldown <= 0;
	}
	
	public float getOffhandCooledAttackStrength()
	{
		return 1.0F - MathHelper.clamp((float)(1 + this.offhandCooldown) / this.offhandAttackCooldown, 0.0F, 1.0F);
	}
	
	public void resetMainhandCooldown( EntityPlayerSP player )
	{
		this.mainhandAttackCooldown = Helpers.getMainhandCooldown(player, this.itemStackMainhand);
		
		if ( this.mainhandAttackCooldown < ConfigurationHandler.minimumAttackSpeedTicks )
		{
			this.mainhandAttackCooldown = ConfigurationHandler.minimumAttackSpeedTicks;
		}
		
		this.mainhandCooldown = this.mainhandAttackCooldown;
		
		ClientProxy.AH_INSTANCE.resetEquippedProgressMainhand();
	}
	
	public void resetOffhandCooldown( EntityPlayerSP player )
	{
		this.offhandAttackCooldown = Helpers.getOffhandCooldown(player, this.itemStackOffhand, this.itemStackMainhand);
		
		if ( this.offhandAttackCooldown < ConfigurationHandler.minimumAttackSpeedTicks )
		{
			this.offhandAttackCooldown = ConfigurationHandler.minimumAttackSpeedTicks;
		}
		
		this.offhandCooldown = this.offhandAttackCooldown;
		
		ClientProxy.AH_INSTANCE.resetEquippedProgressOffhand();
	}
	
    /* ====================================================================================================================================================== */
    /*																	TOOLTIP																				  */
    /* ====================================================================================================================================================== */

	@SubscribeEvent( priority = EventPriority.HIGHEST, receiveCanceled = true )
	public void itemTooltipEventHigh( ItemTooltipEvent event )
	{		
		if ( ConfigurationHandler.isItemWhiteList(event.getItemStack().getItem()) )
		{
			for ( CustomWeapon s : ConfigurationHandler.weapons )
			{
				if ( Helpers.getString(event.getItemStack()).contains(s.name) )
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
				if ( event.getItemStack().getItem().equals(s.shield) ) // XXX just use strings
				{
					event.getToolTip().add("");
					event.getToolTip().add(I18n.format("bettercombat.info.property.offhand.text"));
					event.getToolTip().add(" " + I18n.format("bettercombat.info.attribute.color") + s.damage + I18n.format("bettercombat.info.bash.damage.text"));
					event.getToolTip().add(" " + I18n.format("bettercombat.info.attribute.color") + s.knockback + I18n.format("bettercombat.info.bash.knockback.text"));
					event.getToolTip().add(" " + I18n.format("bettercombat.info.attribute.color") + s.cooldown + I18n.format("bettercombat.info.bash.cooldown.text"));
					return;
				}
			}
		}
	}
	
	@SubscribeEvent( priority = EventPriority.LOWEST, receiveCanceled = true )
	public void itemTooltipEventLow( ItemTooltipEvent event )
	{		
		if ( ConfigurationHandler.isItemWhiteList(event.getItemStack().getItem()) )
		{
			for ( CustomWeapon s : ConfigurationHandler.weapons )
			{
				if ( Helpers.getString(event.getItemStack()).contains(s.name) )
				{
					this.updateBetterCombatTooltipLow(s, event);
					return;
				}
			}
			
			this.updateBetterCombatTooltipLow(ConfigurationHandler.DEFAULT_CUSTOM_WEAPON, event);
			return;
		}
	}
	
	public final String attackSpeedString = I18n.format("attribute.name.generic.attackSpeed");
	public final String attackDamageString = I18n.format("attribute.name.generic.attackDamage");
	
	public final String ATTACK_SPEED_REGEX = "(([0-9]+\\.*[0-9]*)( *" + attackSpeedString + "))";
	public final String ATTACK_DAMAGE_REGEX = "(([0-9]+\\.*[0-9]*)( *" + attackDamageString + "))";
	

	private void updateBetterCombatTooltipLow( CustomWeapon s, ItemTooltipEvent event )
	{
		String twoHandedString = I18n.format("tooltip.Customweaponry:two_handed");
		int twoHandedStringIndex = -1;
						
		String sweepString = I18n.format("tooltip.Customweaponry:sweep_damage");
		int sweepStringIndex = -1;
		
		String reachString = I18n.format("tooltip.Customweaponry:reach");
		int reachStringIndex = -1;
				
		int qualityToolsMainhandIndex = -1;

		int i = 0;
		
		for ( String tag : event.getToolTip() )
		{
			if ( twoHandedStringIndex < 0 && tag.contains(twoHandedString) )
			{
				twoHandedStringIndex = i;
			}
			else if ( reachStringIndex < 0 && tag.contains(reachString) )
			{
				reachStringIndex = i;
			}
			else if ( sweepStringIndex < 0 && tag.contains(sweepString) )
			{
				sweepStringIndex = i;
			}
			
			i++;
		}
		
		i = 0;
		
		/* REMOVE WEAPONRY FATIGUE TAG */
		if ( twoHandedStringIndex >= 0 )
		{
			event.getToolTip().remove(twoHandedStringIndex - i++);
		}
		
		/* REMOVE QUALITYTOOLS WHEN IN MAIN HAND TAG */
		if ( qualityToolsMainhandIndex >= 0 )
		{
			event.getToolTip().remove(qualityToolsMainhandIndex - i++);
		}
						
		/* REMOVE Custom REACH */
		if ( reachStringIndex >= 0 )
		{
			event.getToolTip().remove(reachStringIndex - i++);
		}

		/* REMOVE Custom SWEEP */
		if ( sweepStringIndex >= 0 )
		{
			event.getToolTip().remove(reachStringIndex - i++);
		}
	}
	
	private void updateBetterCombatTooltipHigh( CustomWeapon s, ItemTooltipEvent event )
	{
		/* ============================================================================= */
		/* 									Total Tooltips								 */
		/* ============================================================================= */
		
		/* Knockback */
		if ( ConfigurationHandler.showKnockbackTooltip )
		{
			if ( ConfigurationHandler.showKnockbackTooltipAsTotal )
			{
				event.getToolTip().add(" " + I18n.format("bettercombat.info.attribute.color") + s.knockbackMod + I18n.format("bettercombat.info.knockback.text"));
			}
		}
		
		/* Reach */
		if ( ConfigurationHandler.showReachTooltip )
		{
			if ( ConfigurationHandler.showReachTooltipAsTotal )
			{
				event.getToolTip().add(" " + I18n.format("bettercombat.info.attribute.color") + (s.additionalReachMod+event.getEntityPlayer().getEntityAttribute(EntityPlayer.REACH_DISTANCE).getBaseValue()) + I18n.format("bettercombat.info.reachDistance.text"));
			}
		}
		
		/* Crit Chance */
		if ( ConfigurationHandler.showCritChanceTooltip )
		{
			if ( ConfigurationHandler.showCritChanceTooltipAsTotal )
			{
				event.getToolTip().add(" " + I18n.format("bettercombat.info.attribute.color") + (int)(s.critChanceMod*100) + "%" + I18n.format("bettercombat.info.critChance.text"));
			}
		}
		
		/* Crit Damage */
		if ( ConfigurationHandler.showCritDamageTooltip )
		{
			if ( ConfigurationHandler.showCritDamageTooltipAsTotal )
			{
				event.getToolTip().add(" " + I18n.format("bettercombat.info.attribute.color") + (int)((s.additionalCritDamageMod+ConfigurationHandler.baseCritPercentDamage)*100) + "%" + I18n.format("bettercombat.info.critDamage.text"));
			}
		}
		
		String mainhandString = I18n.format("item.modifiers.mainhand");
		int mainhandStringIndex = -1;
		
		boolean formattedAttackSpeed = false;
		
		boolean formattedAttackDamage = false;

		// int qualityToolsMainhandIndex = -1;

		int i = 0;
		
		for ( String tag : event.getToolTip() )
		{
			if ( tag.contains(mainhandString) )
			{
				if ( mainhandStringIndex < 0 )
				{
					mainhandStringIndex = i;
				}
				else
				{
					// qualityToolsMainhandIndex = i;
				}
			}
			else if ( !formattedAttackSpeed && tag.contains(attackSpeedString) )
			{				
				if ( event.getItemStack().getItem() instanceof ItemSword && !ConfigurationHandler.swords.isEmpty() )
				{
					String str = Helpers.getString(event.getItemStack());
										
					for ( CustomSword sword : ConfigurationHandler.swords )
					{
						if ( str.contains(sword.name) )
						{
							try
							{
								Matcher matcher = Pattern.compile(ATTACK_SPEED_REGEX).matcher(tag);
								
								if ( matcher.find() )
								{
									formattedAttackSpeed = true;
								    event.getToolTip().set(i, " " + I18n.format("bettercombat.info.attribute.color") + String.format("%.1f", (sword.attackSpeed + Double.parseDouble(matcher.group(2)))) + " " + attackSpeedString);
								}
							}
							catch ( Exception e )
							{
								formattedAttackSpeed = true;
								event.getToolTip().set(i, I18n.format("bettercombat.info.attribute.color") + tag);
							}
							
							break;
						}
					}
					
					if ( !formattedAttackSpeed )
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
			else if ( !formattedAttackDamage && tag.contains(attackDamageString) )
			{
				formattedAttackDamage = true;
				this.reformatAttackString(event, i, ATTACK_DAMAGE_REGEX, tag, attackDamageString);
			}
			
			i++;
		}
		
		/* PROPERTY */
		if ( mainhandStringIndex >= 0 )
		{
			switch ( s.property )
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
				default :
				{
					event.getToolTip().set(mainhandStringIndex, I18n.format("bettercombat.info.property.onehand.text"));
					break;
				}
			}
		}
		
		/* ============================================================================= */
		/* 								Addition Tooltips								 */
		/* ============================================================================= */

		boolean flag = false;
		
		if ( ConfigurationHandler.showKnockbackTooltip )
		{
			if ( !ConfigurationHandler.showKnockbackTooltipAsTotal && s.knockbackMod != ConfigurationHandler.baseKnockback )
			{
				if ( !flag ) event.getToolTip().add("");
				event.getToolTip().add((s.knockbackMod > ConfigurationHandler.baseKnockback ? I18n.format("bettercombat.info.positive.color") : I18n.format("bettercombat.info.negative.color")) + (s.knockbackMod-ConfigurationHandler.baseKnockback) + I18n.format("bettercombat.info.knockback.text"));
				flag = true;
			}
		}
		
		if ( ConfigurationHandler.showReachTooltip )
		{
			if ( !ConfigurationHandler.showReachTooltipAsTotal && s.additionalReachMod != 0.0D )
			{
				if ( !flag ) event.getToolTip().add("");
				event.getToolTip().add((s.additionalReachMod > 0.0D ? I18n.format("bettercombat.info.positive.color") : I18n.format("bettercombat.info.negative.color")) + s.additionalReachMod + I18n.format("bettercombat.info.reachDistance.text"));
				flag = true;
			}
		}
		
		if ( ConfigurationHandler.showCritChanceTooltip )
		{
			if ( !ConfigurationHandler.showCritChanceTooltipAsTotal && s.critChanceMod != ConfigurationHandler.baseCritPercentChance )
			{
				if ( !flag ) event.getToolTip().add("");
				event.getToolTip().add((s.critChanceMod > 0.0F ? I18n.format("bettercombat.info.positive.color") : I18n.format("bettercombat.info.negative.color")) + (int)((s.critChanceMod-ConfigurationHandler.baseCritPercentChance)*100) + "%" + I18n.format("bettercombat.info.critChance.text"));
				flag = true;
			}
		}
		
		if ( ConfigurationHandler.showCritDamageTooltip )
		{
			if ( !ConfigurationHandler.showCritDamageTooltipAsTotal && s.additionalCritDamageMod != 0 )
			{
				if ( !flag ) event.getToolTip().add("");
				event.getToolTip().add((s.additionalCritDamageMod > 0.0F ? I18n.format("bettercombat.info.positive.color") : I18n.format("bettercombat.info.negative.color")) + (int)(s.additionalCritDamageMod*100)+ "%" + I18n.format("bettercombat.info.critDamage.text"));
				flag = true;
			}
		}

		if ( ConfigurationHandler.showSweepTooltip && s.sweepMod > 0 )
		{
			event.getToolTip().add("");
			event.getToolTip().add(I18n.format("bettercombat.info.sweep.text") + Helpers.integerToRoman(s.sweepMod));
			flag = true;
		}
		
		if ( ConfigurationHandler.showPotionEffectTooltip && s.customWeaponPotionEffect != null )
		{
			event.getToolTip().add("");
			event.getToolTip().add((int)(s.customWeaponPotionEffect.potionChance*100) + "%" + I18n.format("bettercombat.info.potionEffect.chance.text"));
			
			double seconds = Math.round((s.customWeaponPotionEffect.potionDuration*0.5D))*0.10D;
			
			String str;
			
			if ( seconds % 1 == 0 )
			{
				str = (int)seconds + I18n.format("bettercombat.info.potionEffect.second.text") + ((int)seconds == 1 ? "" : "s");
			}
			else
			{
				str = seconds + I18n.format("bettercombat.info.potionEffect.second.text");
			}
			
			event.getToolTip().add((s.customWeaponPotionEffect.afflict ? I18n.format("bettercombat.info.potionEffect.negative.text") : I18n.format("bettercombat.info.potionEffect.positive.text")) + I18n.format(s.customWeaponPotionEffect.potionEffect.getName()) + " " + Helpers.integerToRoman(s.customWeaponPotionEffect.potionPower) + (s.customWeaponPotionEffect.potionDuration > 0 ? I18n.format("bettercombat.info.potionEffect.for.text") + str : ""));
		}
	}
	
    private void reformatAttackString(ItemTooltipEvent event, int i, String regex, String tag, String s)
    {
    	try
		{
			Matcher matcher = Pattern.compile(regex).matcher(tag);
			
			if ( matcher.find() )
			{
			    event.getToolTip().set(i, " " + I18n.format("bettercombat.info.attribute.color") + String.format("%.1f", (Double.parseDouble(matcher.group(2)))) + " " + s);
			}
		}
		catch ( Exception e )
		{
			event.getToolTip().set(i, I18n.format("bettercombat.info.attribute.color") + tag);
		}
	}

	/* ====================================================================================================================================================== */
    /*																MOV TARGETING		  																	  */
    /* ====================================================================================================================================================== */
	
	public RayTraceResult getMouseOverExtended( EntityPlayerSP player, double dist, float sweepWidth )
	{		
		if ( mc.world == null )
		{
			return null;
		}
		
		double playerWidth = player.width / 2.0D;
		
		final Vec3d pos = player.getPositionEyes(0.0F).addVector(0.0D, -Helpers.execNullable(player.getRidingEntity(), Entity::getMountedYOffset, 0.0D), 0.0D);
		final Vec3d lookVec = player.getLook(0.0F);
		final Vec3d lookTarget = pos.addVector(lookVec.x * dist, lookVec.y * dist, lookVec.z * dist);
		
		final List<Entity> list = mc.world.getEntitiesWithinAABBExcludingEntity(player, new AxisAlignedBB(player.posX - playerWidth, player.posY, player.posZ - playerWidth, player.posX + playerWidth, player.posY + player.height, player.posZ + playerWidth).expand(lookVec.x * dist, lookVec.y * dist, lookVec.z * dist).grow(1.0F, 1.0F, 1.0F));
		
		double distance = dist;

		Entity pointed = null;

		for ( Entity entity : list )
		{
			if ( entity == player.getRidingEntity() )
			{
				continue;
			}
			
			double borderSize = entity.getCollisionBorderSize() + ConfigurationHandler.extraAttackWidth + sweepWidth;
			
			double entityWidth = entity.width / 2.0D;

			AxisAlignedBB aabb = new AxisAlignedBB(entity.posX - entityWidth, entity.posY, entity.posZ - entityWidth, entity.posX + entityWidth, entity.posY + entity.height + ConfigurationHandler.extraAttackHeight, entity.posZ + entityWidth).grow(borderSize, 0.0F, borderSize);
							
			if ( aabb.contains(pos) )
			{
				if ( distance >= -0.000001D )
				{
					pointed = entity;
					distance = 0.0D;
				}
			}
			else
			{
				RayTraceResult target = aabb.calculateIntercept(pos, lookTarget);

				if ( target != null )
				{
					double hitDist = pos.distanceTo(target.hitVec);
					
					if ( hitDist < distance || (distance >= -0.000001D && distance <= 0.000001D) )
					{
						pointed = entity;
						distance = hitDist;
					}
				}
			}
		}

		if ( pointed != null )
		{
			return new RayTraceResult(pointed);
		}

		RayTraceResult traceResult = this.ridingMouseOverEntity(player);

		if ( traceResult != null )
		{
			return traceResult;
		}
		
		return mc.objectMouseOver;
	}
	
	public RayTraceResult ridingMouseOverEntity( EntityPlayerSP player )
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
	
	public boolean invalidPlayer( EntityPlayerSP player )
	{
		return player == null || player.isSpectator() || !player.isEntityAlive() || player.getHealth() <= 0.0F;
	}
	
	/* Returns true if the entity is a player and PVP is enabled */
	public boolean canPVP( Entity entityHit, EntityPlayer player )
	{
		if ( entityHit instanceof EntityPlayerMP )
		{
			return Helpers.execNullable(entityHit.getServer(), MinecraftServer::isPVPEnabled, false);
		}

		return true;
	}
	
    /* ====================================================================================================================================================== */
    /*																					  																	  */
    /* ====================================================================================================================================================== */
	
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
	
	public float getExtraSweepWidth( int sweep )
	{
		return sweep >= 0 ? (4.0F + sweep) / 8.0F : 0.0F;
	}
	
	public void spawnSweepHit( EntityPlayer e, int x, int z )
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