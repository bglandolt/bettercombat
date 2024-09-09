package bettercombat.mod.client;

import bettercombat.mod.util.ConfigurationHandler;
import bettercombat.mod.util.ConfigurationHandler.Animation;
import bettercombat.mod.util.ConfigurationHandler.WeaponProperty;
import bettercombat.mod.util.Helpers;
import bettercombat.mod.util.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemSpade;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

	/* ItemRenderer */		
	/* ItemRenderer */		
	/* ItemRenderer */		

@SideOnly(Side.CLIENT)
public class AnimationHandler
{
	/* (+) Crosshair GUI for attacking */
	public final GuiCrosshairsBC gc;
	
	/* Minecraft item renderer */
	public final RenderItem itemRenderer;
	
	public AnimationHandler()
	{
		this.gc = new GuiCrosshairsBC();
        this.itemRenderer = ClientProxy.EHC_INSTANCE.mc.getRenderItem();
	}

	/* equippedProgressMainhand goes from -1.0 to 0.0, used for raising the weapon up to normal position */
	public float equippedProgressMainhand = 0.0F;
	
	/* equippedProgressOffhand goes from -1.0 to 0.0, used for raising the weapon up to normal position */
    public float equippedProgressOffhand = 0.0F;
	
	/* mainhandEnergy goes from 0.0 to 1.0 over the duration of swingTimer */
	public float mainhandEnergy = 0.0F;
	
	/* offhandEnergy goes from 0.0 to 1.0 over the duration of swingTimer */
	public float offhandEnergy = 0.0F;
	
	/* Raises the weapon up and down to simulate breathing */
	public float breatheTicks = 0.0F;
	
	/* Shift the mainhand weapon forward when sprinting, capped at 20 degrees */
	public int mainhandSprintingTimer = 0;
	
	/* Shift the offhand weapon forward when sprinting, capped at 20 degrees */
	public int offhandSprintingTimer = 0;

	/* If the item is too close, set in EventHandlersClient */
	public boolean tooClose = false;
	
	/* Capped at 0.4, set in EventHandlersClient */
	public double tooCloseAmount = 0.0D;
	
	/* 0.0 (not close) to 0.4 (close) */
	public float tooCloseIntensity = 0.0F;
	
	/*
	 * How long the player has been blocking for, up to 10 frames (3.33 ticks)
	 * blockingTimer is -1 if there is no shield in the offhandA
	 */
	public int blockingTimer = 0;
	
	/* 0 to 10 */
	public int parryingAnimationTimer = 0;
	
	/* Animate when an attack is parried, set in PacketParried */
	public int parriedTimer = 0;
	
	/* Float version of PI */
	public static final float PI = (float)Math.PI;
	
	/* Partial tick amount for miningEnergy calculations */
	public static final float partialIncrement = 0.1666667F;
	
	/* Partial tick amount for camera swing calculations */
	public static final float partialCameraSwing = 0.0833333F;
	
	/* Similar to mainhand energy, except only used for mining */
	public float miningEnergy = 0.0F;
	
//	float xx,yy,zz;
	
	/* ========================================================================================================================= */
	/*															Event
	/* ========================================================================================================================= */
	
	boolean breathingApplied = false;
	
	@SubscribeEvent
	public void disableVanillaHandRender( RenderSpecificHandEvent event )
	{
//		ClientProxy.EHC_INSTANCE.mc.player.hurtTime = 0; // event.getPartialTicks();
//		ClientProxy.EHC_INSTANCE.mc.player.maxHurtTime = 1000;
//		ClientProxy.EHC_INSTANCE.mc.player.attackedAtYaw = 0;
//		xx = (float)ClientProxy.EHC_INSTANCE.mc.player.posX;
//		yy = (float)ClientProxy.EHC_INSTANCE.mc.player.posY - 50;
//		zz = (float)ClientProxy.EHC_INSTANCE.mc.player.posZ;
				
		/* ItemRenderer */		
		if ( event.getHand() == EnumHand.MAIN_HAND )
        {
			if ( ClientProxy.EHC_INSTANCE.mc.player.isSprinting() && this.equippedProgressMainhand != -1.0F && !isMainhandAttacking() )
			{
				if ( this.mainhandSprintingTimer < 20 )
				{
					this.mainhandSprintingTimer += 2;
				}
			}
			else if ( this.mainhandSprintingTimer > 0 )
			{
				this.mainhandSprintingTimer -= 2;
				
				if ( this.mainhandSprintingTimer > 0 && isMainhandAttacking() )
				{
					this.mainhandSprintingTimer -= 2;
				}
			}
			
			if ( this.tooClose )
			{
				if ( this.tooCloseIntensity < this.tooCloseAmount * 0.7D )
				{
					this.tooCloseIntensity += 0.015F;
				}
				else if ( this.tooCloseIntensity > this.tooCloseAmount * 1.3D )
				{
					this.tooCloseIntensity -= 0.01F;
				}
			}
			else
			{
				if ( this.tooCloseIntensity > 0.0F )
				{
					this.tooCloseIntensity -= 0.02F;
					
					if ( this.tooCloseIntensity < 0.0F )
					{
						this.tooCloseIntensity = 0.0F;
					}
				}
				else
				{
					this.tooCloseIntensity = 0.0F;
				}
			}
			
			if ( ClientProxy.EHC_INSTANCE.parrying )
			{
				if ( this.parryingAnimationTimer < 10 )
				{
					this.parryingAnimationTimer++;
				}
			}
			else
			{				
				if ( this.parryingAnimationTimer > 0 )
				{
					this.parryingAnimationTimer--;
				}
			}
			
			if ( this.parriedTimer > 0 )
			{
				this.parriedTimer--;
			}
			
			if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.hasConfigWeapon() )
			{
				/* If the MAINHAND is active */
	    		if ( Helpers.isHandActive(ClientProxy.EHC_INSTANCE.mc.player, EnumHand.MAIN_HAND) )
	    		{
	    			/* Default rendering, such as bow, eat, or drink! */
					this.positionBreathingMainhand();
					return;
	    		}
	    		
	    		this.customMainhandRender(event);
				return;
			}
			else
			{
				/* Default rendering! */
				this.breathingApplied = true;
				this.positionBreathingMainhand();
				return;
			}
        }
		else if ( event.getHand() == EnumHand.OFF_HAND )
        {
			if ( ClientProxy.EHC_INSTANCE.mc.player.isSprinting() && this.equippedProgressOffhand != -1.0F && !isOffhandAttacking() )
			{
				if ( this.offhandSprintingTimer < 20 )
				{
					this.offhandSprintingTimer += 2;
				}
			}
			else if ( this.offhandSprintingTimer > 0 )
			{
				this.offhandSprintingTimer -= 2;
				
				if ( this.offhandSprintingTimer > 0 && isOffhandAttacking() )
				{
					this.offhandSprintingTimer -= 2;
				}
			}
			
			/* If the OFFHAND OR MAINHAND has a TWOHAND, or the OFFHAND has a MAINHAND, */
    		if ( ClientProxy.EHC_INSTANCE.betterCombatOffhand.getWeaponProperty() == WeaponProperty.TWOHAND || ClientProxy.EHC_INSTANCE.betterCombatMainhand.getWeaponProperty() == WeaponProperty.TWOHAND || ClientProxy.EHC_INSTANCE.betterCombatOffhand.getWeaponProperty() == WeaponProperty.MAINHAND )
    		{
    			/* Cancel all rendering! */
    			event.setCanceled(true);
    			event.setResult(Result.DENY);
    			return;
    		}
    		else if ( ClientProxy.EHC_INSTANCE.betterCombatOffhand.hasConfigWeapon() )
			{
    			this.customOffhandRender(event);
    			this.breathingApplied = false;
    			return;
			}
    		else if ( ClientProxy.EHC_INSTANCE.itemStackOffhand.getItem() instanceof ItemShield )
    		{
    	    	if ( Helpers.isHandActive(ClientProxy.EHC_INSTANCE.mc.player, EnumHand.OFF_HAND) && ( !ConfigurationHandler.disableBlockingWhileAttacking || ClientProxy.EHC_INSTANCE.isMainhandAttackReady() ) ) // && !isMainhandAttacking() && this.equippedProgressMainhand >= 0.0F )
    			{
					/* Block animation takes 10 frames (3.33 ticks) */
					if ( this.blockingTimer < 10 )
					{
	    				// System.out.println("++");
						this.blockingTimer++;
					}
        		}
				else if ( this.blockingTimer > 0 )
    			{
    				// System.out.println("--");
    				this.blockingTimer--;
    			}

    			/* Shield custom rendering! */
    			this.customShieldRender(event);
    			this.breathingApplied = false;
    			return;
    		}
    		else
    		{
    			this.blockingTimer = -1;
    		}

    		/* Default rendering! */
    		this.positionBreathingOffhand();
    		this.breathingApplied = false;
			return;
        }
    }
	
	/* ========================================================================================================================= */
	/*															Mainhand
	/* ========================================================================================================================= */
	
	public static boolean isMainhandAttacking()
    {
    	return ClientProxy.EHC_INSTANCE.betterCombatMainhand.isSwinging();
    }
	
	/* ------------------------------------------------------------------------------------------------------------------------- */
	/*															 Render
	/* ------------------------------------------------------------------------------------------------------------------------- */
	
	private void customMainhandRender( RenderSpecificHandEvent event )
	{
		event.setCanceled(true);
		event.setResult(Result.DENY);
		
		GlStateManager.pushMatrix();

        this.positionMainWeapon();
		this.positionMainhandAwayIfOffhandAttacking();
        this.positionBreathingMainhand();

		/* Mining */
		if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.isMining() && (isMainhandAttacking() || ClientProxy.EHC_INSTANCE.startedMining || ClientProxy.EHC_INSTANCE.holdingLeftClick || this.miningEnergy > 0.0F) ) // todo
		{
			// !(ClientProxy.EHC_INSTANCE.holdingLeftClick) || !(ClientProxy.EHC_INSTANCE.startedMining)
			
			this.resetMainhandEnergy();
			
			Item tool = ClientProxy.EHC_INSTANCE.itemStackMainhand.getItem();
			
			if ( tool instanceof ItemSpade )
			{
	        	this.animationDiggingMainhand(event);
			}
			else if ( tool instanceof ItemAxe )
			{
				if ( this.calculateMiningEnergy(event) >= 1.0F )
				{
					this.resetMiningEnergy();
				}
				
				this.animationWoodcuttingMainhand(this.miningEnergy);
				this.reequipAnimationMainhand();
			}
			else if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.getAnimation().equals(Animation.STAB) )
			{
				if ( this.calculateMiningEnergy(event) >= 1.0F )
				{
					this.resetMiningEnergy();
				}
				
        		this.animationStabMainhand(this.miningEnergy);
			}
			else /* Sword, Pickaxe, Axe */
			{
				this.animationMiningMainhand(event);
			}
		}
		else if ( isMainhandAttacking() )
		{
			switch ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.getAnimation() )
	        {
	        	case SWEEP:
	        	{
	        		if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.alternateAnimation )
	        		{
	        			this.animationSweepMainhand2(this.calculateMainhandEnergy(event));
	        			// System.out.println(this.mainhandEnergy); // TODO
	        		}
	        		else
	        		{
	        			this.animationSweepMainhand1(this.calculateMainhandEnergy(event));
	        		}
	        		
	    			this.animationSweepCameraMainhand();
	    			this.reequipAnimationMainhand();
	        		break;
	        	}
	        	case CHOP:
	        	{
	        		this.animationChopMainhand(this.calculateMainhandEnergy(event));
	        		this.animationChopCameraMainhand();
	    			this.reequipAnimationMainhand();
	        		break;
	        	}
	        	case STAB:
	        	{
	        		this.animationStabMainhand(this.calculateMainhandEnergy(event));
	               	this.animationStabCameraMainhand();
	               	/* No re-equip animation */
	            	this.noReequipAnimationMainhand();
	        		break;
	        	}
	        	default:
	        	{
	        		this.animationChopMainhand(this.calculateMainhandEnergy(event));
	        		this.animationChopCameraMainhand();
	    			this.reequipAnimationMainhand();
	        		break;
	        	}
			}
		}
		else
		{
			if ( this.parryingAnimationTimer > 0 )
			{	        	
				float x;
				
				if ( this.parryingAnimationTimer < 5 )
				{
					x = (1.0F - MathHelper.cos(this.parryingAnimationTimer*PI*0.1F)) * 0.5F;
				}
				else
				{
					x = (2.0F - MathHelper.sin(this.parryingAnimationTimer*PI*0.1F)) * 0.5F;
				}
				
	        	GlStateManager.rotate(x*40.0F, -15.0F, 45.0F, 75.0F);
	        	GlStateManager.translate(-x*0.35F, x*0.2F, x*0.01F);
			}
			
			if ( this.parriedTimer > 0 )
			{				
	        	GlStateManager.translate(0.0F, -MathHelper.sin(this.parriedTimer*PI/10.0F)*0.2F, 0.0F);
			}
			
			this.resetMainhandEnergy();
			this.resetMiningEnergy();
			
			/* Position the weapon off the screen and return to view */
	        this.positionEquippedProgressMainhand();
		}
		
		this.renderMainWeapon();
	        
		GlStateManager.popMatrix();
	}
	
	/* ------------------------------------------------------------------------------------------------------------------------- */
	/*															Position
	/* ------------------------------------------------------------------------------------------------------------------------- */
	
	/* Position the mainhand weapon in the player hand */
    protected void positionMainWeapon()
    {
    	/* Position the weapon in default position */
        GlStateManager.translate(0.685F, -0.6F, -1.0F);
        
        /* If the weapon is an axe, position it upwards */
    	if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.getAnimation().equals(Animation.CHOP) )
    	{
        	GlStateManager.rotate(-11.0F-this.mainhandSprintingTimer,1.0F,0.0F,0.0F); /* Chopping rotation */
    		/* Position the weapon in default position */
            GlStateManager.translate(0.02F, 0.08F, 0.0F);
        	GlStateManager.rotate(-16.0F,0.0F,1.0F,0.0F);
        	GlStateManager.rotate(-8.0F,0.0F,0.0F,1.0F);
    	}
        /* If the weapon is a spear, rotate it accordingly */
    	else if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.getAnimation().equals(Animation.STAB) )
        {
        	GlStateManager.rotate(-44.0F-this.mainhandSprintingTimer,1.0F,0.0F,0.0F); /* Chopping rotation */
        	GlStateManager.translate(0.0F, -this.tooCloseIntensity, 0.0F);
        }
        else
        {
        	GlStateManager.rotate(-13.0F-this.mainhandSprintingTimer,1.0F,0.0F,0.0F); /* Chopping rotation */
        	GlStateManager.rotate(-13.0F,0.0F,1.0F,0.0F);
        	GlStateManager.rotate(-13.0F,0.0F,0.0F,1.0F);
        }
    	
    	/* Position this weapon away when the player is blocking */
    	if ( this.isBlocking() )
    	{
            GlStateManager.translate(this.blockingTimer*0.016F, -this.blockingTimer*0.012F, 0.0F);
    	}
    	// todo
//    	/* Position this weapon away when the player shield bashing */
//    	else if ( !this.blockingTimerActive() && this.isOffhandAttacking() )
//    	{
//			float f = MathHelper.sin(energy*energy*PI)*0.5F;
//            GlStateManager.translate(f, -f * 0.5F, 0.0F);
//    	}
    }

    private void positionMainhandAwayIfOffhandAttacking()
	{
    	if ( isOffhandAttacking() )
    	{
    		float f = MathHelper.sin(this.offhandEnergy*PI);
            GlStateManager.translate(f*0.0625F, -f*0.025F, 0.0F);
			/* Move this hand up left */
//	    	switch ( ClientProxy.EHC_INSTANCE.betterCombatOffhand.getAnimation() )
//	        {
//	        	case SWEEP:
//	        	{
//	        		float f = MathHelper.sin(energy*PI);
//	                GlStateManager.translate(f*0.125F, -f*0.05F, 0.0F);
//	                break;
//	            }
//	        	case CHOP:
//	        	{
//	        		float f = MathHelper.sin(energy*PI);
//	                GlStateManager.translate(f*0.125F, -f*0.05F, 0.0F);
//	                break;
//	            }
//	        	case STAB:
//	        	{
//	        		float f = MathHelper.sin(energy*PI);
//	                GlStateManager.translate(f*0.125F, 0.0F, 0.0F);
//	                break;
//	            }
//	        	default:
//	        	{
//	        		break;
//	        	}
//	        }
    	}
	}
    
	/* Re-equip the main-hand weapon after an attack or item change */
    private void positionEquippedProgressMainhand()
    {    	
        if ( this.equippedProgressMainhand < 0.0F && (this.equippedProgressMainhand += ClientProxy.EHC_INSTANCE.betterCombatMainhand.getEquipTimerIncrement()) < 0.0F )
		{
        	if ( this.equippedProgressMainhand > 0.0F )
			{
				this.equippedProgressMainhand = 0.0F;
			}
        	else
        	{
        		GlStateManager.translate(0.0F,this.equippedProgressMainhand,0.0F);
    			GlStateManager.translate(0.0F,0.0F,this.equippedProgressMainhand*-0.25F);
        	}
		} 
    }

	/* ------------------------------------------------------------------------------------------------------------------------- */
	/*															 Energy
	/* ------------------------------------------------------------------------------------------------------------------------- */
	
	protected float calculateMainhandEnergy(RenderSpecificHandEvent event)
	{
		return this.mainhandEnergy = 1.0F + (event.getPartialTicks() - ClientProxy.EHC_INSTANCE.betterCombatMainhand.getSwingTimer() - 0.5F) * ClientProxy.EHC_INSTANCE.betterCombatMainhand.getSwingTimerIncrement();
	}
	
	protected void resetMainhandEnergy()
	{
		this.mainhandEnergy = 0.0F;
	}
	
	protected float calculateMiningEnergy(RenderSpecificHandEvent event)
	{
		return this.miningEnergy += ClientProxy.EHC_INSTANCE.betterCombatMainhand.getSwingTimerIncrement() * partialIncrement; // TODO
	}
	
	protected void resetMiningEnergy()
	{
		this.miningEnergy = 0.0F;
	}
	
	/* ------------------------------------------------------------------------------------------------------------------------- */
	/*															 Render
	/* ------------------------------------------------------------------------------------------------------------------------- */
	
    private void renderMainWeapon()
    {
    	GlStateManager.scale(1.36F, 1.36F, 1.36F);

        if ( !ClientProxy.EHC_INSTANCE.itemStackMainhand.isEmpty() )
        {
            boolean flag = this.itemRenderer.shouldRenderItemIn3D(ClientProxy.EHC_INSTANCE.itemStackMainhand) && Block.getBlockFromItem(ClientProxy.EHC_INSTANCE.itemStackMainhand.getItem()).getBlockLayer() == BlockRenderLayer.TRANSLUCENT;

            if ( flag )
            {
                GlStateManager.depthMask(false);
            }

            this.itemRenderer.renderItem(ClientProxy.EHC_INSTANCE.itemStackMainhand, ClientProxy.EHC_INSTANCE.mc.player, ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, false);

            if ( flag )
            {
                GlStateManager.depthMask(true);
            }
        }
    }
    
	/* ------------------------------------------------------------------------------------------------------------------------- */
	/*													  Mining Animation
	/* ------------------------------------------------------------------------------------------------------------------------- */
	
	private void animationMiningMainhand(RenderSpecificHandEvent event)
	{
		this.calculateMiningEnergy(event);
		
		if ( ClientProxy.EHC_INSTANCE.holdingLeftClick )
		{
			if ( this.miningEnergy >= 0.7F )
			{
				this.miningEnergy = (1.0F - this.miningEnergy) * 0.33333333F;
			}
		}
		else if ( this.miningEnergy >= 1.0F )
		{
			this.resetMiningEnergy();
		}
		
		float moveRight = 0.0F; /* +right */
		float moveUp = 0.0F; /* +up */
		float moveClose = 0.0F; /* +zoom */
		
		float rotateUp = 0.0F;
		float rotateCounterClockwise = 5.0F;
		float rotateLeft = 25.0F;
		
		float closeCap = this.tooCloseIntensity - 0.5F;
		
		if ( this.miningEnergy > 0.2F )
		{
			if ( this.miningEnergy > 0.4F )
			{
				/* (RETURN) HIGHEST | 0.4 -> 1.0 */
				/* ======= */
				float f = this.miningEnergy - 0.4F;
				float ff = 1.0F - MathHelper.sin(f*PI*0.83333333F); /* 1 . 0 | fast to slow */
				
				moveRight = ff * -0.7F;
				moveClose = ff * closeCap;
				
//				moveUp = MathHelper.sin(f*PI*1.66666667F) * 0.2F;
//				moveUp = MathHelper.sin(f*0.33333333F*PI*5.0F) * 0.2F;
				moveUp = 0.2F - f * 0.33333333F;
				
				rotateUp = ff * -105.0F;
				
				float fff = MathHelper.cos(f*PI*0.83333333F); /* 1 . 0 | slow to fast */
				rotateCounterClockwise = fff * 5.0F;
				rotateLeft = fff * 25.0F;
			}
			else
			{
				/* (HOLD) HIGH | 0.2 -> 0.4 */
				/* ==== */
				float f = MathHelper.sin((this.miningEnergy-0.2F)*PI*5.0F); /* 0 . 1 . 0 */

				moveRight = -0.7F;
				
				moveClose = closeCap + f * 0.01F;
				
				rotateUp = -105.0F + f * 5.0F;
				
				moveUp = 0.2F;
			}
		}
		else
		{
			/* (SWING) LOW | 0.0 -> 0.2 */
			/* === */
			float f = 1.0F - MathHelper.cos(this.miningEnergy*PI*2.5F); /* 0 . 1 | slow to fast */
			moveRight = f * -0.7F;
			moveClose = f * closeCap;
			
//			moveUp = MathHelper.sin(this.miningEnergy*PI*5.0F) * 0.2F; /* 0 . 1 . 0 */
			moveUp = f * 0.2F;
			
			rotateUp = f * -105.0F;
			
			float ff = MathHelper.sin(this.miningEnergy*PI*2.5F); /* 0 . 1 | fast to slow */
			rotateCounterClockwise = ff * 5.0F;
			rotateLeft = ff * 25.0F;
		}
		
		// 0.2 swing, 0.1 hold
		
		/* MathHelper.sin( (0.0 to 0.2) *PI*2.5F) 0 to 1 | slow to fast */
		/* MathHelper.cos( (0.0 to 2.0) *PI*2.5F) 1 to 0 | slow to fast */

	    GlStateManager.translate(
	    1.2F * moveRight,
	    moveUp,
	    moveClose);
       	
		/* Chop */
	    GlStateManager.rotate(rotateUp, 1.0F, 0.0F, 0.0F);
    	
    	GlStateManager.rotate(rotateCounterClockwise, 0.0F, 1.0F, 0.0F);

    	/* Swivel back and forth */
    	GlStateManager.rotate(rotateLeft, 0.0F, 0.0F, 1.0F);
	}
	
//	private void animationMiningMainhand(RenderSpecificHandEvent event)
//	{
//		this.calculateMiningEnergy(event);
//		
//		if ( ClientProxy.EHC_INSTANCE.holdingLeftClick )
//		{
//			if ( this.miningEnergy >= 0.6F )
//			{
//				this.miningEnergy = (1.0F - this.miningEnergy) * 0.5F;
//			}
//		}
//		else if ( this.miningEnergy >= 1.0F )
//		{
//			this.resetMiningEnergy();
//		}
//				
//		float moveRight = 0.0F; /* +right */
//		float moveUp = 0.0F; /* +up */
//		float moveClose = 0.0F; /* +zoom */
//		
//		float rotateUp = 0.0F;
//		float rotateCounterClockwise = 15.0F;
//		float rotateLeft = 30.0F;
//		
//		float closeCap = this.tooCloseIntensity - 0.4F;
//		
//		if ( this.miningEnergy > 0.2F )
//		{
//			rotateCounterClockwise -= this.miningEnergy * 15.0F;
//
//			if ( this.miningEnergy > 0.6F )
//			{
//				/* (COMPLETE RETURN) HIGHEST | 0.6 -> 1.0 */
//				/* ======= */
//				float f = this.miningEnergy - 0.6F; /* 0.0 to 0.4 */
//				
//				moveRight = -0.7F + f * 1.75F;
//				moveClose = closeCap - closeCap * f * 2.5F;
//
////				rotateCounterClockwise -= 25.0F * f;
//				rotateLeft -= 75.0F * f;
//				
//				/* Rotate up */
//				rotateUp = -100.0F + f * 250.0F;
//			}
//			else
//			{
//				/* (MINE) LOW | 0.2 -> 0.6 */
//				/* ==== */
//				float f = MathHelper.sin((this.miningEnergy-0.2F)*PI*2.5F); /* 0 . 1 . 0 */
//				f *= f;
//				
//				rotateCounterClockwise -= f * 5.0F;
//				rotateLeft -= f * 15.0F;
//				
//				moveRight = -0.7F;
//				moveClose = closeCap;
//				
//				moveUp = f * 0.25F;
//				/* Rotate up | fast to slow to fast */
//				rotateUp = -100.0F + f * 25.0F;
//			}
//		}
//		else
//		{
//			/* (INITIAL SWING) LOWEST | 0.0 -> 0.2 */
//			/* === */
//			float f = 1.0F - MathHelper.cos(this.miningEnergy*PI*2.5F); /* 0 . 1 */
//			
//			rotateCounterClockwise = this.miningEnergy * 60.0F;
//			rotateLeft = this.miningEnergy * 150.0F;
//			
//			moveRight = f * -0.7F;
//			moveClose = f * closeCap;
//			
//			moveUp = MathHelper.sin(this.miningEnergy*PI*5.0F) * 0.2F; /* 0 . 1 . 0 */
//			/* Rotate down */
//			rotateUp = f * -100.0F;
//		}
//		
//		/* MathHelper.sin( (0.0 to 0.2) *PI*2.5F) 0 to 1 | slow to fast */
//		/* MathHelper.cos( (0.0 to 2.0) *PI*2.5F) 1 to 0 | slow to fast */
//
//	    GlStateManager.translate(
//	    1.1F * moveRight,
//	    moveUp,
//	    moveClose);
//       	
//		/* Chop */
//	    GlStateManager.rotate(rotateUp, 1.0F, 0.0F, 0.0F);
//    	
//    	GlStateManager.rotate(rotateCounterClockwise, 0.0F, 1.0F, 0.0F);
//
//    	/* Swivel back and forth */
//    	GlStateManager.rotate(rotateLeft, 0.0F, 0.0F, 1.0F);
//	}
	
	/* ------------------------------------------------------------------------------------------------------------------------- */
	/*													  Digging Animation
	/* ------------------------------------------------------------------------------------------------------------------------- */
	
	private void animationDiggingMainhand(RenderSpecificHandEvent event)
	{
		this.calculateMiningEnergy(event);
		
		if ( ClientProxy.EHC_INSTANCE.holdingLeftClick )
		{
			if ( this.miningEnergy > 0.75F )
			{
				this.miningEnergy = 1.0F - this.miningEnergy;
			}
		}
		else if ( this.miningEnergy >= 1.0F )
		{
			this.resetMiningEnergy();
		}
		
		float moveRight = 0.0F; /* +right */
		float moveUp = 0.0F; /* +up */
		float moveClose = 0.0F; /* +zoom */
		
		float twist = 0.0F; /* +up   |||   twist */
		float sway = 0.0F; /* +counter-clockwise   \|/   side-to-side */
		float scoop = 0.0F; /* +left   |   scoop */
		
		
//		float xx = (float)ClientProxy.EHC_INSTANCE.mc.player.posX;
//		float yy = (float)ClientProxy.EHC_INSTANCE.mc.player.posY - 50;
//		float zz = (float)ClientProxy.EHC_INSTANCE.mc.player.posZ; // XXX
		
	    float closeCap = (0.5F - this.tooCloseIntensity);
		
		if ( this.miningEnergy > 0.25F )
		{
			if ( this.miningEnergy > 0.45F )
			{
				if ( this.miningEnergy > 0.55F )
				{
					if ( this.miningEnergy > 0.75F )
					{
						/* (RETURN) HIGHEST | 0.75F -> 1.0 */
						/* ======= */
						float f = this.miningEnergy - 0.75F;
						
						/* Move +Close */
				        moveClose = (0.25F - f) * (closeCap * 2.5F);
				        
						/* Move +Right */
				        moveRight = (-0.55F + f * 2.2F);
						
						/* Move +Right */
						moveUp = (0.25F - f);
						
						twist = -110.0F + f * 440.0F;
						sway = 70.0F - f * 280.0F;
						scoop = 80.0F - f * 320.0F;
					}
					else
					{
				        /* =============================================== */

						/* (DOWN) | 0.55F -> 0.75F */
						/* ==== */
						
						float f = this.miningEnergy - 0.35F;
						
						float ff = MathHelper.sin(f*PI*2.5F); /* 0 . 1 . 0 */
						
						ff *= ff;

						/* Move +Right */
				        moveRight = (-0.55F - ff * 0.2F);
						
						/* Move +Right */
						moveUp = (0.25F - ff * 0.2F);
						
						twist = -110.0F - ff * 20.0F;
						sway = 70.0F - ff * 20.0F;
						scoop = 80.0F - ff * 15.0F;
						
						/* Move +Close */
				        moveClose = (0.25F - ff * 0.5F) * (closeCap * 2.5F);
					}
				}
				else
				{
					/* (HOLD) | 0.45F -> 0.55F */
					/* ==== */
					// float f = this.miningEnergy - 0.35F;

					// float ff = MathHelper.sin(f*PI*10.0F); /* 0 . 1 . 0 */
					
					/* Move +Right */
			        moveRight = (-0.75F);
			        
			        moveUp = (0.05F);
					
					twist = -130.0F;
					sway = 50.0F;
					scoop = 65.0F;
					
					/* Move +Close */
			        moveClose = (-0.25F) * (closeCap * 2.5F);
//			        moveClose = (-0.25F + ff * 0.2F) * (closeCap * 3.0F);
				}
			}
			else
			{
				/* (UP) | 0.25F -> 0.45F */
				/* ==== */
				float f = this.miningEnergy - 0.25F;
				
				float ff = MathHelper.sin(f*PI*2.5F); /* 0 . 1 . 0 */

				/* Move +Right */
		        moveRight = (-0.55F - ff * 0.2F);
				
				/* Move +Right */
				moveUp = (0.25F - ff * 0.2F);
				
				twist = -110.0F - ff * 20.0F;
				sway = 70.0F - ff * 20.0F;
				scoop = 80.0F - ff * 15.0F;
				
				ff *= ff;
				
				/* Move +Close */
		        moveClose = (0.25F - ff * 0.5F) * (closeCap * 2.5F);
		        
		        /* =============================================== */
			}
		}
		else
		{
			/* (READY) LOWEST | 0.0F -> 0.25F */
			
			/* Move +Close */
			moveClose = (this.miningEnergy) * (closeCap * 2.5F);
			
			/* Move -Left */
			moveRight = (-this.miningEnergy * 2.2F);

			/* Move -Down */
			// float ff = MathHelper.sin(f*PI*2.5F); /* 0 . 1 . 0 */
			moveUp = (this.miningEnergy);
			
			twist = -this.miningEnergy * 440.0F; /* -110 */
		    sway = this.miningEnergy * 280.0F; /* 70 */
		    scoop = this.miningEnergy * 320.0F; /* 80 */
		}
		
		GlStateManager.translate(
		moveRight,
		0.4F * moveUp,
		moveClose);
		
//		float xx = (float)ClientProxy.EHC_INSTANCE.mc.player.posX;
//		float yy = (float)ClientProxy.EHC_INSTANCE.mc.player.posY - 50;
//		float zz = (float)ClientProxy.EHC_INSTANCE.mc.player.posZ; // XXX
		
//    	GlStateManager.rotate(twist + xx, 1.0F, 0.0F, 0.0F);
//    	GlStateManager.rotate(sway + yy, 0.0F, 1.0F, 0.0F);
//    	GlStateManager.rotate(scoop + zz, 0.0F, 0.0F, 1.0F);
		
    	GlStateManager.rotate(twist, 1.0F, 0.0F, 0.0F);
    	GlStateManager.rotate(sway, 0.0F, 1.0F, 0.0F);
    	GlStateManager.rotate(scoop, 0.0F, 0.0F, 1.0F);
	}
	
	/* ------------------------------------------------------------------------------------------------------------------------- */
	/*													Woodcutting Animation
	/* ------------------------------------------------------------------------------------------------------------------------- */
	
	private void animationWoodcuttingMainhand(float energy)
	{
		float moveRight = 0.0F;
	    float moveUp = 0.0F;
	    float moveClose = 0.0F;
	    float rotateUp = 0.0F;
	    float rotateCounterClockwise = 0.0F;
	    float rotateLeft = 0.0F;
	    
	    float closeCap = 0.2F - this.tooCloseIntensity;
	    
	    rotateUp = -clampMultiplier(energy, 6.0F, 140.0F + closeCap * 50.0F); /* Sweep = Up */
	    
	    rotateCounterClockwise = clampMultiplier(energy, 12.0F, 150.0F) - clampMultiplier(energy, 3.0F, 50.0F + closeCap * 100.0F) - energy * 15.0F; /* Sweep = Left and To */
	    rotateLeft = clampMultiplier(energy, 6.0F, 85.0F); /* Sweep = Twist Clockwise -- * */

	    /* Move right very fast at the start */
	    moveRight = clampMultiplier(energy, 12.0F, 3.5F) + 0.5F;
		moveUp = clamp(energy*10.0F, 0.47F);
		moveClose = -clamp(energy*10.0F, closeCap);
	    
	    if ( energy > 0.6F )
	    {
		    /* Move left slowly as the animation has reached the center */
		    moveRight -= 4.5F + closeCap - (1.0F - MathHelper.sin(energy * PI)) * 0.3F;
		    
		    if ( energy > 0.85F )
		    {
				moveUp -= MathHelper.sin(energy - 0.85F) * 6.0F;
		        moveClose += MathHelper.sin(energy - 0.85F) * 6.0F;
		        moveRight += (energy - 0.85F) * 5.5F;
		    }
	    }
	    else
	    {
		    /* Move left fast until 0.6 energy */
	    	moveRight -= clamp(MathHelper.sin(energy * PI) * 5.5F, 4.5F + closeCap);
	    }
		
		GlStateManager.translate(
       	/* X */ 1.2F * moveRight,
       	/* Y */ 1.1F * moveUp,
       	/* Z */ moveClose);
       	
    	GlStateManager.rotate(rotateUp, 1.0F, 0.0F, 0.0F);
    	GlStateManager.rotate(rotateCounterClockwise, 0.0F, 1.0F, 0.0F);
    	GlStateManager.rotate(rotateLeft, 0.0F, 0.0F, 1.0F);
	}
	
	/* ------------------------------------------------------------------------------------------------------------------------- */
	/*													  Sweep Animation 1
	/* ------------------------------------------------------------------------------------------------------------------------- */
	
	private void animationSweepMainhand1(float energy)
	{
		float moveRight = 0.0F;
	    float moveUp = 0.0F;
	    float moveClose = 0.0F;
	    float rotateUp = 0.0F;
	    float rotateCounterClockwise = 0.0F;
	    float rotateLeft = 0.0F;
	    
	    float closeCap = 0.4F - this.tooCloseIntensity;
	    
	    rotateUp = -clampMultiplier(energy, 6.0F, 140.0F + closeCap * 40.0F); /* Sweep = Up */
	    rotateCounterClockwise = clampMultiplier(energy, 12.0F, 150.0F) - clampMultiplier(energy, 3.0F, 50.0F + closeCap * 100.0F) - energy * 15.0F; /* Sweep = Left and To */
	    rotateLeft = clampMultiplier(energy, 6.0F, 85.0F); /* Sweep = Twist Clockwise -- * */

	    /* Move right very fast at the start */
	    moveRight = clampMultiplier(energy, 12.0F, 3.5F) + 0.5F;
		moveUp = clamp(energy*10.0F, 0.47F);
		moveClose = -clamp(energy*10.0F, closeCap);
	    
	    if ( energy > 0.6F )
	    {
		    /* Move left slowly as the animation has reached the center */
		    moveRight -= 4.5F + closeCap - (1.0F - MathHelper.sin(energy * PI)) * 0.3F;
		    
		    if ( energy > 0.85F )
		    {
		    	float f = energy - 0.85F;
		    	
				moveUp -= MathHelper.sin(f) * 6.0F;
		        moveClose += MathHelper.sin(f) * 6.0F;
		        moveRight += f * 5.5F;
		    }
	    }
	    else
	    {
		    /* Move left fast until 0.6 energy */
	    	moveRight -= clamp(MathHelper.sin(energy * PI) * 5.5F, 4.5F + closeCap);
	    }
		
		GlStateManager.translate(
       	/* X */ 1.2F * moveRight * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveRightVariance,
       	/* Y */ 1.1F * moveUp * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveUpVariance,
       	/* Z */ moveClose * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveCloseVariance);
       	
    	GlStateManager.rotate(rotateUp * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateUpVariance, 1.0F, 0.0F, 0.0F);
    	GlStateManager.rotate(rotateCounterClockwise * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateCounterClockwiseVariance, 0.0F, 1.0F, 0.0F);
    	GlStateManager.rotate(rotateLeft * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateLeftVariance, 0.0F, 0.0F, 1.0F);
	}
	
//	public class MyClass
//	{
//		  public static void main(String args[])
//		  {
//		    int i = 0;
//		    double a = 0.0D;
//		    
//		    while ( ++i < 12 )
//		    {
//		        double aa = Math.cos(0.4 + (i/12.0D) * 2.18181818 * Math.PI);
//		        a += aa;
//		        System.out.println(aa);
//		    }
//
//		    System.out.println(a+ 10000);
//		  }
//	}
	
	private void animationSweepCameraMainhand()
	{
		/* Adjust speed of the camera based on the speed of the attack swing */
		float f = ClientProxy.EHC_INSTANCE.betterCombatMainhand.getSwingTimerCap() * partialCameraSwing;

		/* Camera */
    	float rotation = MathHelper.cos(-1.0F + this.mainhandEnergy * 1.9F * PI);
    	
    	// System.out.println(rotation);
    	
		ClientProxy.EHC_INSTANCE.mc.player.cameraPitch -= rotation * ConfigurationHandler.cameraPitchSwing * f; /* +right */
		ClientProxy.EHC_INSTANCE.mc.player.rotationYaw -= rotation * ConfigurationHandler.rotationYawSwing * f; /* +down */
	}
	
	/* ------------------------------------------------------------------------------------------------------------------------- */
	/*													  Sweep Animation 2
	/* ------------------------------------------------------------------------------------------------------------------------- */
	
	private void animationSweepMainhand2(float energy)
	{
		float moveRight = 0.0F;
	    float moveUp = 0.0F;
	    float moveClose = 0.0F;
	    float rotateUp = 0.0F;
	    float rotateCounterClockwise = 0.0F;
	    float rotateLeft = 0.0F;
	    	    
	    float closeCap = 1.0F - this.tooCloseIntensity * 2.5F;
	    
	    if ( energy > 0.2F )
		{
			if ( energy > 0.6F )
			{
				/* (SWEEP) HIGHEST | 0.6F -> 1 */
				/* ======= */
				float f = (energy - 0.6F);
				
				rotateCounterClockwise = f * -400.0F;
				
				//rotateUp = 10.0F + energy * 100.0F;
				
				moveUp = 0.2F - f * 0.5F;
				// moveUp = 0.2F - energy * 3.0F;
				
				moveRight = -1.55F + f * 30.0F;
				
				rotateLeft = 83.0F + f * 12.5F;
				
			    moveClose = (-f * 3.0F) * closeCap;
			}
			else
			{
				if ( energy > 0.4F )
				{
					/* (HOLD) HIGH | 0.4F -> 0.6F */
					/* ==== */
					float f = (energy - 0.4F);

					//rotateUp = f * 50.0F;
					
					moveUp = 0.2F;
					
				    moveRight = -1.5F - f * 0.25F;
				    
					rotateLeft = 75.0F + f * 40.0F;
					
				    moveClose = (0.2F - f) * closeCap;
				}
				else
				{
					/* (FAST READY) LOW | 0.2F -> 0.4F */
					/* ==== */
					float f = (energy - 0.2F);

					/* Fast > Slow */
					moveUp = -0.2F + MathHelper.sin(f*PI*2.5F) * 0.4F;
				    moveRight = -1.2F - MathHelper.sin(f*PI*2.5F) * 0.3F;
					
					rotateLeft = 25.0F + f * 250.0F;
					
				    moveClose = energy * 0.5F * closeCap;
				}
			}
		}
		else
		{
			/* (FAST READY) LOWEST | 0.0F -> 0.2F */
			/* ==== */
			
			/* Slow > Fast | (1.0F - MathHelper.cos(f*PI*2.5F)) */
			moveUp = -energy;
		    moveRight = energy * -6.0F;

			rotateLeft = energy * 125.0F;
			
		    moveClose = energy * 0.5F * closeCap;
		}
	    		
		GlStateManager.translate(
       	/* X */ 0.75F * moveRight * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveRightVariance,
       	/* Y */ 1.15F * moveUp * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveUpVariance,
       	/* Z */ moveClose * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveCloseVariance);
		
		GlStateManager.rotate(rotateUp * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateUpVariance, 1.0F, 0.0F, 0.0F);
    	GlStateManager.rotate(rotateCounterClockwise * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateCounterClockwiseVariance, 0.0F, 1.0F, 0.0F);
    	GlStateManager.rotate(rotateLeft * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateLeftVariance, 0.0F, 0.0F, 1.0F);
	}
	
	/* ------------------------------------------------------------------------------------------------------------------------- */
	/*														Chop Animation
	/* ------------------------------------------------------------------------------------------------------------------------- */
	
	private void animationChopMainhand(float energy)
	{
		float moveRight = 0.0F; /* +right */
		float moveUp = 0.0F; /* +up */
		float moveClose = 0.0F; /* +zoom */
		
		float rotateUp = 0.0F;
		float rotateCounterClockwise = clamp(energy*300.0F,15.0F) - energy*15.0F;
		float rotateLeft = clamp(energy*100.0F,30.0F); /* +left */

	    float closeCap = this.tooCloseIntensity - 0.4F;
		
		if ( energy > 0.2F )
		{
			if ( energy > 0.7F )
			{
				/* HIGHEST | 0.7 -> 1.0 */
				/* ======= */
				float f = energy - 0.7F;

				moveUp = -f;
				
				moveRight = -0.7F + f * 2.0F;
								
				moveClose = closeCap + f * 6.0F;
				
				
				rotateUp = -95.0F;
			}
			else
			{
				/* (HOLD) HIGH | 0.2 -> 0.7 */
				/* ==== */
				
				moveRight = -0.7F;
				
				moveClose = closeCap;
				

				rotateUp = -95.0F;

				if ( energy < 0.4F )
				{
					rotateUp += MathHelper.sin((energy-0.2F)*PI*5.0F) * 5.0F;
				}
			}
		}
		else
		{
			/* (SWING) LOW | 0.0 -> 0.2 */
			/* === */
			float f = 1.0F - MathHelper.cos(energy*PI*2.5F);
						
			moveRight = f * -0.7F;
			
			moveClose = f * closeCap;
			
			
			rotateUp = f * -95.0F;
		}
		
	    if ( energy <= 0.22F )
	    {
			moveUp = MathHelper.sin(energy*PI*4.6F) * 0.2F;
	    }
	    
	    GlStateManager.translate(
	    		1.1F * moveRight * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveRightVariance,
	    		moveUp * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveUpVariance,
	    		moveClose * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveCloseVariance);
       	
		/* Chop */
	    GlStateManager.rotate(rotateUp * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateUpVariance, 1.0F, 0.0F, 0.0F);
    	//GlStateManager.rotate( (ClientProxy.EHC_INSTANCE.betterCombatMainhand.isMining() ? negativeClamp(rotateUp, -80.0F) : rotateUp) * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateUpVariance, 1.0F, 0.0F, 0.0F);
    	
    	GlStateManager.rotate(rotateCounterClockwise * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateCounterClockwiseVariance, 0.0F, 1.0F, 0.0F);

    	/* Swivel back and forth */
    	GlStateManager.rotate(rotateLeft * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateLeftVariance, 0.0F, 0.0F, 1.0F);
	}
	
	private void animationChopCameraMainhand()
	{
		float f = ClientProxy.EHC_INSTANCE.betterCombatMainhand.getSwingTimerCap() * partialCameraSwing;
		
		/* Camera */
//    	float rotation = MathHelper.sin(-1.8F + this.mainhandEnergy * 2.0F * PI);
//		ClientProxy.EHC_INSTANCE.mc.player.cameraPitch -= rotation * ConfigurationHandler.cameraPitchSwing * 2.0F * f;
//		ClientProxy.EHC_INSTANCE.mc.player.rotationYaw += rotation * ConfigurationHandler.rotationYawSwing * 0.2F * f;

    	float rotation = MathHelper.cos(1.0F + this.mainhandEnergy * 2.2F * PI);

    	if ( rotation > 0.0F )
    	{
    		ClientProxy.EHC_INSTANCE.mc.player.cameraPitch -= rotation * ConfigurationHandler.cameraPitchSwing * f;
    	}
    	else
    	{
    		ClientProxy.EHC_INSTANCE.mc.player.cameraPitch -= rotation * ConfigurationHandler.cameraPitchSwing * 2.0F * f;
    	}
    	
		ClientProxy.EHC_INSTANCE.mc.player.rotationYaw += rotation*ConfigurationHandler.rotationYawSwing * 0.2F * f;
	}
	
	/* ------------------------------------------------------------------------------------------------------------------------- */
	/*														Stab Animation
	/* ------------------------------------------------------------------------------------------------------------------------- */
		
	private void animationStabMainhand(float energy)
	{		
		float moveRight = 0.0F; /* +right */
		float moveUp = 0.0F; /* +up */
		float moveClose = 0.0F; /* +zoom */
		
		float rotateUp = 0.0F; /* +up */
		float rotateCounterClockwise = 0.0F; /* +counter-clockwise */
		float rotateLeft = 0.0F; /* +left */
		
	    float closeCap = (0.6F - this.tooCloseIntensity);
		
		rotateUp = -clamp(energy * 240.0F, 60.0F);
		
		rotateCounterClockwise = clamp(energy * 125.0F, 50.0F) - energy * 10.0F;
		rotateLeft = clamp(energy * 75.0F, 30.0F) - energy * 10.0F;
		
		if ( energy > 0.2F )
		{
			if ( energy > 0.4F )
			{
				if ( energy > 0.8F )
				{
					/* (RETURN) HIGHEST | 0.8F -> 1 */
					/* ======= */
					float f = (energy - 0.8F);
					
					/* Move +Close */
					moveClose = -closeCap + f * 2.5F;
					
					/* Move +Right */
					moveRight = moveClose;
					
					/* Move -Down */
					moveUp = -moveClose;
					
					f *= f;
					rotateUp += clampMultiplier(f, 25.0F, 70.0F);
					rotateCounterClockwise -= clampMultiplier(f, 25.0F, 30.0F);
					rotateLeft -= clampMultiplier(f, 25.0F, 10.0F);
				}
				else
				{
					/* (HOLD) HIGH | 0.4F -> 0.8F */
					/* ==== */

					/* Stay -Away */
					moveClose = -closeCap * (energy * 0.25F + 0.9F);
					
					/* Stay -Left */
					moveRight = moveClose;
					
					/* Stay +Up */
					moveUp = -moveClose;
				}
			}
			else
			{
				/* (THRUST) LOW | 0.2F -> 0.4F */
				/* === */
				
				/* Move -Away */
				moveClose = (0.2F - energy) * closeCap * 5.0F;
				
				/* Move -Left */
				moveRight = moveClose;
				
				/* Move +Up */
				moveUp = -moveClose -0.25F * closeCap;
			}
		}
		else
		{
			/* (READY) LOWEST | 0.0F -> 0.2F */
			moveClose = MathHelper.sin(energy*PI*5.0F) * 0.2F;
			
			moveUp = -energy * closeCap * 2.5F;
		}
		
		GlStateManager.translate(
	       	/* X */ moveRight * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveRightVariance,
	       	/* Y */ 1.3F * moveUp * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveUpVariance,
	       	/* Z */ moveClose * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveCloseVariance);
			       	
    	GlStateManager.rotate(rotateUp * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateUpVariance, 1.0F, 0.0F, 0.0F);
    	GlStateManager.rotate(rotateCounterClockwise * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateCounterClockwiseVariance, 0.0F, 1.0F, 0.0F);
    	GlStateManager.rotate(rotateLeft * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateLeftVariance, 0.0F, 0.0F, 1.0F);
	}
	
	private void animationStabCameraMainhand()
	{
		/* Reduce momentum based off attack speed */
		float f = ClientProxy.EHC_INSTANCE.betterCombatMainhand.getSwingTimerCap() * partialCameraSwing;
		
		/* Camera */
//		float rotation = MathHelper.sin(-0.4F + this.mainhandEnergy * 2.0F * PI);
//		ClientProxy.EHC_INSTANCE.mc.player.cameraPitch += rotation*ConfigurationHandler.cameraPitchSwing * f;
		
		float rotation = MathHelper.sin(-1.8F + this.mainhandEnergy * PI);
		ClientProxy.EHC_INSTANCE.mc.player.cameraPitch += rotation * ConfigurationHandler.cameraPitchSwing * f;
		ClientProxy.EHC_INSTANCE.mc.player.rotationYaw -= rotation * ConfigurationHandler.rotationYawSwing * f;
	}
	
    /* ====================================================================================================================================================== */
    /*																		SHIELD 																			  */
    /* ====================================================================================================================================================== */
	
	public void customShieldRender( RenderSpecificHandEvent event )
	{
		event.setCanceled(true);
		event.setResult(Result.DENY);
		
		if ( isOffhandAttacking() )
		{
			this.calculateOffhandEnergy(event);
		}
		else
		{
			this.resetOffhandEnergy();
		}
		
		GlStateManager.pushMatrix();
		
		/* Animate the shield bash */
        this.positionOffShield();

        this.positionBreathingOffhand();
        this.positionEquippedProgressOffhand();
        this.renderOffShield();
        GlStateManager.popMatrix();
	}
	
	private void positionOffShield()
	{
		this.positionOffhandAwayIfMainhandAttacking();
		
		/* If the offhandEnergy is greater than 0, the player is shield bashing */
        if ( this.offhandEnergy > 0.0F )
        {
        	// this.blockingTimer = 10; // todo
        	
        	float shieldBashProgress = MathHelper.sin(this.offhandEnergy*PI)*1.25F;
        	
        	if ( shieldBashProgress > 1.0F )
        	{
        		shieldBashProgress = 1.0F;
        	}
        	
        	/* Counter-clockwise */
        	GlStateManager.rotate(shieldBashProgress*40.0F,	0.0F, 1.0F, 0.0F);
        	
        	/* Rotate down */
        	GlStateManager.rotate(-shieldBashProgress*5.0F, 1.0F, 0.0F, 0.0F);
        	
        	/* Rotate left */
        	GlStateManager.rotate(shieldBashProgress*1.5F, 	0.0F, 0.0F, 1.0F);
        	
        	/* Position right, Position Up, Zoom out */
    		GlStateManager.translate(0.0F, shieldBashProgress*0.05F, -shieldBashProgress*1.25F);
    		
    		/* Adjust camera angle while shield bashing */
    		this.animationShieldBashCameraOffhand();
        }
        
        /* Blocking Animation */
        if ( this.blockingTimer > 0 )
        {
        	/* Clockwise */
        	GlStateManager.rotate(-this.blockingTimer*0.75F, 0.0F, 1.0F, 0.0F);
        	
        	/* Rotate up */
        	GlStateManager.rotate(this.blockingTimer*1.25F, 1.0F, 0.0F, 0.0F);
        	
        	/* Rotate right */
        	GlStateManager.rotate(-this.blockingTimer*1.5F, 0.0F, 0.0F, 1.0F);
        	
        	/* Position right, Position up, Zoom in */
    		GlStateManager.translate(this.blockingTimer*0.06F, this.blockingTimer*0.008F, this.blockingTimer*0.04F); // todo 0.04 0.06
    	}
        
        /* Position Shield */
        
        /* Counter-clockwise 30 */
        GlStateManager.rotate(210.0F, 0.0F, 1.0F, 0.0F);
		
		/* Position 2-D shields */
		if ( ConfigurationHandler.isShield2D(Helpers.getRegistryNameFromItem(ClientProxy.EHC_INSTANCE.itemStackOffhand)))
		{
			/* Position left, Position down, Position away */
//			GlStateManager.translate(xx/10.0F, yy/10.0F, zz/10.0F);
			GlStateManager.translate(0.27F, -0.9F, 1.3F);
		}
		else
		{
			/* Position left, Position down, Position away */
			GlStateManager.translate(1.0F, -0.52F, 1.8F);
		}
	}
	
	private void animationShieldBashCameraOffhand()
	{
		/* Camera */
    	float rotation = MathHelper.cos(-0.4F + this.offhandEnergy * 2.0F * PI);
		ClientProxy.EHC_INSTANCE.mc.player.cameraPitch -= rotation*ConfigurationHandler.cameraPitchSwing;
		ClientProxy.EHC_INSTANCE.mc.player.rotationYaw += rotation*ConfigurationHandler.rotationYawSwing;
	}
	
    
    public void reequipAnimationMainhand()
    {
    	this.equippedProgressMainhand = -1.0F;
    }
    
    public void noReequipAnimationMainhand()
    {
    	this.equippedProgressMainhand = 0.0F;
    }
    
    public void reequipAnimationOffhand()
    {
    	this.equippedProgressOffhand = -1.0F;
    }
    
    public void noReequipAnimationOffhand()
    {
    	this.equippedProgressOffhand = 0.0F;
    }
    
    public boolean isBlocking()
    {
        return this.blockingTimer > 0;
    }
	
    private void positionBreathingMainhand()
	{
    	GlStateManager.translate(0.0F, MathHelper.sin(this.breatheTicks) * ConfigurationHandler.breathingAnimationIntensity, this.tooCloseIntensity*0.6F);

    	// TODO - more breathing if out of feathers
//    	if ( true )
//    	{
//    		float f = 1.0F + MathHelper.clamp((10 - getFeatherLevel(ClientProxy.EHC_INSTANCE.mc.player)),0,10) * 0.5F;
//
//        	GlStateManager.translate(0.0F, MathHelper.sin(this.breatheTicks) * ConfigurationHandler.breathingAnimationIntensity * f, this.tooCloseIntensity*0.6F);
//    	}
	}
    
    private void positionBreathingOffhand()
	{
    	if ( !this.breathingApplied )
    	{
    		this.positionBreathingMainhand();
    	}
	}
    
//    private void positionBreathingShield()
//    {
//    	GlStateManager.translate(0.0F, MathHelper.sin(this.breatheTicks) * ConfigurationHandler.breathingAnimationIntensity, -this.tooCloseIntensity*0.6F);
//	}

	/* ========================================================================================================================= */
	/*															 Offhand
	/* ========================================================================================================================= */

    public static boolean isOffhandAttacking()
    {
    	return ClientProxy.EHC_INSTANCE.betterCombatOffhand.isSwinging();
    }
    
	/* ------------------------------------------------------------------------------------------------------------------------- */
	/*															 Render
	/* ------------------------------------------------------------------------------------------------------------------------- */
    
	public void customOffhandRender( RenderSpecificHandEvent event )
	{
		event.setCanceled(true);
		event.setResult(Result.DENY);
		
		GlStateManager.pushMatrix();

        this.positionOffhandWeapon();
        
        if ( !ClientProxy.EHC_INSTANCE.betterCombatMainhand.isMining() )
        {
        	this.positionOffhandAwayIfMainhandAttacking();
        }
        
        this.positionBreathingOffhand();
        
		if ( isOffhandAttacking() )
		{    		
			switch ( ClientProxy.EHC_INSTANCE.betterCombatOffhand.getAnimation() )
	        {
	        	case SWEEP:
	        	{
	        		if ( ClientProxy.EHC_INSTANCE.betterCombatOffhand.alternateAnimation )
	        		{
		        		this.animationSweepOffhand2(this.calculateOffhandEnergy(event));
	        		}
	        		else
	        		{
		        		this.animationSweepOffhand1(this.calculateOffhandEnergy(event));
	        		}
	        		
	        		this.animationSweepCameraOffhand();
        			this.reequipAnimationOffhand();
	        		break;
	        	}
	        	case CHOP:
	        	{
	        		this.animationChopOffhand(this.calculateOffhandEnergy(event));
	        		this.animationChopCameraOffhand();
        			this.reequipAnimationOffhand();
	        		break;
	        	}
	        	case STAB:
	        	{
		        	this.animationStabOffhand(this.calculateOffhandEnergy(event));
		        	this.animationStabCameraOffhand();
	            	this.noReequipAnimationOffhand();
	        		break;
	        	}
	        	default:
	        	{
	        		this.animationChopOffhand(this.calculateOffhandEnergy(event));
	        		this.animationChopCameraOffhand();
        			this.reequipAnimationOffhand();
	        		break;
	        	}
	        }
		}
		else
		{
			this.resetOffhandEnergy();
	        this.positionEquippedProgressOffhand();
		}
		
		this.renderOffWeapon();
        
    	GlStateManager.popMatrix();
	}
	
    private void renderOffWeapon()
    {
    	GlStateManager.scale(1.36F, 1.36F, 1.36F);

        if ( !ClientProxy.EHC_INSTANCE.itemStackOffhand.isEmpty() )
        {
            boolean flag = this.itemRenderer.shouldRenderItemIn3D(ClientProxy.EHC_INSTANCE.itemStackOffhand) && Block.getBlockFromItem(ClientProxy.EHC_INSTANCE.itemStackOffhand.getItem()).getBlockLayer() == BlockRenderLayer.TRANSLUCENT;

            if ( flag )
            {
                GlStateManager.depthMask(false);
            }

            this.itemRenderer.renderItem(ClientProxy.EHC_INSTANCE.itemStackOffhand, ClientProxy.EHC_INSTANCE.mc.player, ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, true);

            if ( flag )
            {
                GlStateManager.depthMask(true);
            }
        }
    }
    
    private void renderOffShield()
    {
    	GlStateManager.scale(1.36F, 1.36F, 1.36F);

        this.itemRenderer.renderItem(ClientProxy.EHC_INSTANCE.itemStackOffhand, ClientProxy.EHC_INSTANCE.mc.player, ItemCameraTransforms.TransformType.NONE, true);
    }
	
	/* ------------------------------------------------------------------------------------------------------------------------- */
	/*															Position
	/* ------------------------------------------------------------------------------------------------------------------------- */
	
	private void positionOffhandWeapon()
	{
    	/* Position the weapon in default position */
		GlStateManager.translate(-0.7F, -0.6F, -1.0F);
        
		/* If the weapon is an axe, position it upwards */
    	if ( ClientProxy.EHC_INSTANCE.betterCombatOffhand.getAnimation().equals(Animation.CHOP) )
    	{
        	GlStateManager.rotate(-11.0F-this.offhandSprintingTimer,1.0F,0.0F,0.0F); /* Chopping rotation */
    		/* Position the weapon in default position */
            GlStateManager.translate(-0.02F, 0.08F, 0.0F);
        	GlStateManager.rotate(16.0F,0.0F,1.0F,0.0F);
        	GlStateManager.rotate(8.0F,0.0F,0.0F,1.0F);
    	}
        /* If the weapon is a spear, rotate it accordingly */
    	else if ( ClientProxy.EHC_INSTANCE.betterCombatOffhand.getAnimation().equals(Animation.STAB) )
        {
        	GlStateManager.rotate(-44.0F-this.offhandSprintingTimer,1.0F,0.0F,0.0F); /* Chopping rotation */
        	GlStateManager.translate(0.0F, -this.tooCloseIntensity, 0.0F);
        }
        else
        {
        	GlStateManager.rotate(-13.0F-this.offhandSprintingTimer,1.0F,0.0F,0.0F); /* Chopping rotation */
        	GlStateManager.rotate(13.0F,0.0F,1.0F,0.0F);
        	GlStateManager.rotate(13.0F,0.0F,0.0F,1.0F);
        }
	}
	
	private void positionOffhandAwayIfMainhandAttacking()
	{
//    	if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.isMining() )
//		{
//			if ( (this.miningTimer += 0.1F) > 1.0F )
//			{
//				this.miningTimer = 1.0F;
//			}
//			
//			if ( this.miningTimer > 0.0F )
//			{
//		        GlStateManager.translate(-this.miningTimer*0.125F, -this.miningTimer*0.05F, 0.0F);
//			}
//		}
//    	else if ( this.miningTimer > 0.0F )
//		{
//	        GlStateManager.translate(-this.miningTimer*0.125F, -this.miningTimer*0.05F, 0.0F);
//	        
//    		this.miningTimer -= 0.1F;
//		}
    	
    	if ( isMainhandAttacking() )
    	{
			float f = MathHelper.sin(this.mainhandEnergy*PI);
            GlStateManager.translate(-f*0.125F, -f*0.05F, 0.0F);
    		
			/* Move this hand up left */
//	    	switch ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.getAnimation() )
//	        {
//	        	case SWEEP:
//	        	{
//	        		float f = MathHelper.sin(energy*PI);
//	                GlStateManager.translate(-f*0.15F, -f*0.1F, 0.0F);
//	                break;
//	            }
//	        	case CHOP:
//	        	{
//	        		float f = MathHelper.sin(energy*PI);
//	                GlStateManager.translate(-f*0.1F, -f*0.1F, f*0.1F);
//	                break;
//	            }
//	        	case STAB:
//	        	{
//	        		float f = MathHelper.sin(energy*PI);
//	                GlStateManager.translate(-f*0.1F, 0.0F, f*0.1F);
//	                break;
//	            }
//	        	default:
//	        	{
//	        		break;
//	        	}
//	        }
    	}
	}

	/* Re-equip the Off-hand weapon after an attack or item change */
    private void positionEquippedProgressOffhand()
    {
        if ( this.equippedProgressOffhand < 0.0F && (this.equippedProgressOffhand += ClientProxy.EHC_INSTANCE.betterCombatOffhand.getEquipTimerIncrement()) < 0.0F )
		{
        	if ( this.equippedProgressOffhand > 0.0F )
			{
				this.equippedProgressOffhand = 0.0F;
			}
        	else
        	{
				GlStateManager.translate(0.0F,this.equippedProgressOffhand,0.0F);
				GlStateManager.translate(0.0F,0.0F,this.equippedProgressOffhand*-0.25F);
        	}
		}
    }
	
	/* ------------------------------------------------------------------------------------------------------------------------- */
	/*															 Energy
	/* ------------------------------------------------------------------------------------------------------------------------- */
	
	protected float calculateOffhandEnergy(RenderSpecificHandEvent event)
	{
		return this.offhandEnergy = 1.0F + (event.getPartialTicks() - ClientProxy.EHC_INSTANCE.betterCombatOffhand.getSwingTimer() - 0.5F) * ClientProxy.EHC_INSTANCE.betterCombatOffhand.getSwingTimerIncrement();
	}
	
	protected void resetOffhandEnergy()
	{
		this.offhandEnergy = 0.0F;
	}
	
	/* ------------------------------------------------------------------------------------------------------------------------- */
	/*														Chop Animation
	/* ------------------------------------------------------------------------------------------------------------------------- */
	
	private void animationChopOffhand(float energy)
	{
		float moveRight = 0.0F; /* +right */
		float moveUp = 0.0F; /* +up */
		float moveClose = 0.0F; /* +zoom */
		
		float rotateUp = 0.0F;
		float rotateCounterClockwise = clamp(energy*300.0F,15.0F) - energy*15.0F;
		float rotateLeft = clamp(energy*100.0F,30.0F); /* +left */
		
	    float closeCap = this.tooCloseIntensity - 0.4F;

		if ( energy > 0.2F )
		{
			if ( energy > 0.7F )
			{
				/* HIGHEST | holdDuration -> 1 */
				/* ======= */
				float f = energy - 0.7F;

				moveUp = -f;
				
				moveRight = -0.7F + f * 2.0F;
								
				moveClose = closeCap + f * 6.0F;
				
				
				rotateUp = -95.0F;
			}
			else
			{
				/* (HOLD) HIGH | 0.25 -> 0.65 */
				/* ==== */
								
				moveRight = -0.7F;
				
				moveClose = closeCap;
				

				rotateUp = -95.0F;

				if ( energy < 0.4F )
				{
					rotateUp += MathHelper.sin((energy-0.2F)*PI*5.0F) * 5.0F;
				}
			}
		}
		else
		{
			/* (SWING) LOW | 0.0 -> 0.25 */
			/* === */
			float f = 1.0F - MathHelper.cos(energy*PI*2.5F);
						
			moveRight = f * -0.7F;
			
			moveClose = f * closeCap;
			
			
			rotateUp = f * -95.0F;
		}
		
		if ( energy <= 0.22F )
	    {
			moveUp = MathHelper.sin(energy*PI*4.6F) * 0.2F; // 0.19F + 0.01F;
	    }

		GlStateManager.translate(
       	/* X */ -1.1F * moveRight * ClientProxy.EHC_INSTANCE.betterCombatOffhand.moveRightVariance,
       	/* Y */ moveUp * ClientProxy.EHC_INSTANCE.betterCombatOffhand.moveUpVariance,
       	/* Z */ moveClose * ClientProxy.EHC_INSTANCE.betterCombatOffhand.moveCloseVariance);
       	
		/* Chop */
    	GlStateManager.rotate(rotateUp * ClientProxy.EHC_INSTANCE.betterCombatOffhand.rotateUpVariance, 1.0F, 0.0F, 0.0F);
    	
    	GlStateManager.rotate(-rotateCounterClockwise * ClientProxy.EHC_INSTANCE.betterCombatOffhand.rotateCounterClockwiseVariance, 0.0F, 1.0F, 0.0F);

    	/* Swivel back and forth */
    	GlStateManager.rotate(-rotateLeft * ClientProxy.EHC_INSTANCE.betterCombatOffhand.rotateLeftVariance, 0.0F, 0.0F, 1.0F);
	}
	
	private void animationChopCameraOffhand()
	{
		float f = ClientProxy.EHC_INSTANCE.betterCombatOffhand.getSwingTimerCap() * partialCameraSwing; // todo
		
		/* Camera */
//    	float rotation = MathHelper.sin(-1.8F + this.offhandEnergy * 2.0F * PI);
//		ClientProxy.EHC_INSTANCE.mc.player.cameraPitch -= rotation * ConfigurationHandler.cameraPitchSwing * 2.0F * f;
//		ClientProxy.EHC_INSTANCE.mc.player.rotationYaw -= rotation * ConfigurationHandler.rotationYawSwing * 0.2F * f;

    	float rotation = MathHelper.cos(1.0F + this.offhandEnergy * 2.2F * PI);
    	
    	if ( rotation > 0.0F )
    	{
    		ClientProxy.EHC_INSTANCE.mc.player.cameraPitch -= rotation * ConfigurationHandler.cameraPitchSwing * f;
    	}
    	else
    	{
    		ClientProxy.EHC_INSTANCE.mc.player.cameraPitch -= rotation * ConfigurationHandler.cameraPitchSwing * 2.0F * f;
    	}
    	
		ClientProxy.EHC_INSTANCE.mc.player.rotationYaw -= rotation*ConfigurationHandler.rotationYawSwing * 0.2F * f;
	}
	
	/* ------------------------------------------------------------------------------------------------------------------------- */
	/*													  Sweep Animation 1
	/* ------------------------------------------------------------------------------------------------------------------------- */
	
	private void animationSweepOffhand1(float energy)
	{
		float moveRight = 0.0F;
	    float moveUp = 0.0F;
	    float moveClose = 0.0F;
	    float rotateUp = 0.0F;
	    float rotateCounterClockwise = 0.0F;
	    float rotateLeft = 0.0F;
	    
	    float closeCap = 0.4F - this.tooCloseIntensity;
	    
	    rotateUp = -clampMultiplier(energy, 6.0F, 140.0F + closeCap * 40.0F); /* Sweep = Up */
	    rotateCounterClockwise = clampMultiplier(energy, 12.0F, 150.0F) - clampMultiplier(energy, 3.0F, 50.0F + closeCap * 100.0F) - energy * 15.0F; /* Sweep = Left and To */
	    rotateLeft = clampMultiplier(energy, 6.0F, 85.0F); /* Sweep = Twist Clockwise -- * */

	    /* Move right very fast at the start */
	    moveRight = clampMultiplier(energy, 12.0F, 3.5F) + 0.5F;
		moveUp = clamp(energy*10.0F, 0.47F);
		moveClose = -clamp(energy*10.0F, closeCap);
	    		
	    if ( energy > 0.6F )
	    {
		    moveRight -= 4.5F + closeCap - (1.0F - MathHelper.sin(energy * PI)) * 0.3F;
		    
		    if ( energy > 0.85F )
		    {
		    	float f = energy - 0.85F;

				moveUp -= MathHelper.sin(f) * 6.0F;
		        moveClose += MathHelper.sin(f) * 6.0F;
		        moveRight += f * 5.5F;
		    }
	    }
	    else
	    {
	    	moveRight -= clamp(MathHelper.sin(energy * PI) * 5.5F, 4.5F + closeCap);
	    }
		
		GlStateManager.translate(
       	/* X */ 1.2F * -moveRight * ClientProxy.EHC_INSTANCE.betterCombatOffhand.moveRightVariance,
       	/* Y */ 1.1F * moveUp * ClientProxy.EHC_INSTANCE.betterCombatOffhand.moveUpVariance,
       	/* Z */ moveClose * ClientProxy.EHC_INSTANCE.betterCombatOffhand.moveCloseVariance);
       	
    	GlStateManager.rotate(rotateUp * ClientProxy.EHC_INSTANCE.betterCombatOffhand.rotateUpVariance, 1.0F, 0.0F, 0.0F);
    	GlStateManager.rotate(-rotateCounterClockwise * ClientProxy.EHC_INSTANCE.betterCombatOffhand.rotateCounterClockwiseVariance, 0.0F, 1.0F, 0.0F);
    	GlStateManager.rotate(-rotateLeft * ClientProxy.EHC_INSTANCE.betterCombatOffhand.rotateLeftVariance, 0.0F, 0.0F, 1.0F);
	}

	private void animationSweepCameraOffhand()
	{
		float f = ClientProxy.EHC_INSTANCE.betterCombatOffhand.getSwingTimerCap() * partialCameraSwing;

		/* Camera */
    	float rotation = MathHelper.cos(-0.4F + this.offhandEnergy * 1.9F * PI);
    	
		ClientProxy.EHC_INSTANCE.mc.player.cameraPitch -= rotation * ConfigurationHandler.cameraPitchSwing * f;
		ClientProxy.EHC_INSTANCE.mc.player.rotationYaw -= rotation * ConfigurationHandler.rotationYawSwing * f;
	}
	
	/* ------------------------------------------------------------------------------------------------------------------------- */
	/*													  Sweep Animation 2
	/* ------------------------------------------------------------------------------------------------------------------------- */
	
	private void animationSweepOffhand2(float energy)
	{
		float moveRight = 0.0F;
	    float moveUp = 0.0F;
	    float moveClose = 0.0F;
	    float rotateUp = 0.0F;
	    float rotateCounterClockwise = 0.0F;
	    float rotateLeft = 0.0F;
	    	    
	    float closeCap = 1.0F - this.tooCloseIntensity * 2.5F;
	    
	    if ( energy > 0.2F )
		{
			if ( energy > 0.6F )
			{
				/* (SWEEP) HIGHEST | 0.6F -> 1 */
				/* ======= */
				float f = (energy - 0.6F);
				
				rotateCounterClockwise = f * -400.0F;
				
				//rotateUp = 10.0F + energy * 100.0F;
				
				moveUp = 0.2F - f * 0.5F;
				// moveUp = 0.2F - energy * 3.0F;
				
				moveRight = -1.55F + f * 30.0F;
				
				rotateLeft = 83.0F + f * 12.5F;
				
			    moveClose = (-f * 3.0F) * closeCap;
			}
			else
			{
				if ( energy > 0.4F )
				{
					/* (HOLD) HIGH | 0.4F -> 0.6F */
					/* ==== */
					float f = (energy - 0.4F);

					//rotateUp = f * 50.0F;
					
					moveUp = 0.2F;
					
				    moveRight = -1.5F - f * 0.25F;
				    
					rotateLeft = 75.0F + f * 40.0F;
					
				    moveClose = (0.2F - f) * closeCap;
				}
				else
				{
					/* (FAST READY) LOW | 0.2F -> 0.4F */
					/* ==== */
					float f = (energy - 0.2F);

					/* Fast > Slow */
					moveUp = -0.2F + MathHelper.sin(f*PI*2.5F) * 0.4F;
				    moveRight = -1.2F - MathHelper.sin(f*PI*2.5F) * 0.3F;
					
					rotateLeft = 25.0F + f * 250.0F;
					
				    moveClose = energy * 0.5F * closeCap;
				}
			}
		}
		else
		{
			/* (FAST READY) LOWEST | 0.0F -> 0.2F */
			/* ==== */
			
			/* Slow > Fast | (1.0F - MathHelper.cos(f*PI*2.5F)) */
			moveUp = -energy;
		    moveRight = energy * -6.0F;

			rotateLeft = energy * 125.0F;
			
		    moveClose = energy * 0.5F * closeCap;
		}
	    		
		GlStateManager.translate(
       	/* X */ 0.75F * -moveRight * ClientProxy.EHC_INSTANCE.betterCombatOffhand.moveRightVariance,
       	/* Y */ 1.15F * moveUp * ClientProxy.EHC_INSTANCE.betterCombatOffhand.moveUpVariance,
       	/* Z */ moveClose * ClientProxy.EHC_INSTANCE.betterCombatOffhand.moveCloseVariance);
       	
//		float xx = (float)ClientProxy.EHC_INSTANCE.mc.player.posX;
//		float yy = (float)ClientProxy.EHC_INSTANCE.mc.player.posY - 50;
//		float zz = (float)ClientProxy.EHC_INSTANCE.mc.player.posZ;
		
		GlStateManager.rotate(rotateUp * ClientProxy.EHC_INSTANCE.betterCombatOffhand.rotateUpVariance, 1.0F, 0.0F, 0.0F);
    	GlStateManager.rotate(-rotateCounterClockwise * ClientProxy.EHC_INSTANCE.betterCombatOffhand.rotateCounterClockwiseVariance, 0.0F, 1.0F, 0.0F);
    	GlStateManager.rotate(-rotateLeft * ClientProxy.EHC_INSTANCE.betterCombatOffhand.rotateLeftVariance, 0.0F, 0.0F, 1.0F);
	}
	
	/* ------------------------------------------------------------------------------------------------------------------------- */
	/*														Stab Animation
	/* ------------------------------------------------------------------------------------------------------------------------- */
	
	private void animationStabOffhand(float energy)
	{		
		float moveRight = 0.0F; /* +right */
		float moveUp = 0.0F; /* +up */
		float moveClose = 0.0F; /* +zoom */
		
		float rotateUp = 0.0F; /* +up */
		float rotateCounterClockwise = 0.0F; /* +counter-clockwise */
		float rotateLeft = 0.0F; /* +left */
		
	    float closeCap = (0.6F - this.tooCloseIntensity);
		
		rotateUp = -clamp(energy * 240.0F, 60.0F);
		
		rotateCounterClockwise = clamp(energy * 125.0F, 50.0F) - energy * 10.0F;
		rotateLeft = clamp(energy * 75.0F, 30.0F) - energy * 10.0F;
		
		if ( energy > 0.2F )
		{
			if ( energy > 0.4F )
			{
				if ( energy > 0.8F )
				{
					/* (RETURN) HIGHEST | 0.8F -> 1 */
					/* ======= */
					float f = (energy - 0.8F);
					
					/* Move +Close */
					moveClose = -closeCap + f * 2.5F;
					
					/* Move +Right */
					moveRight = moveClose;
					
					/* Move -Down */
					moveUp = -moveClose;
					
					f *= f;
					rotateUp += clampMultiplier(f, 25.0F, 70.0F);
					rotateCounterClockwise -= clampMultiplier(f, 25.0F, 30.0F);
					rotateLeft -= clampMultiplier(f, 25.0F, 10.0F);
				}
				else
				{
					/* (HOLD) HIGH | 0.4F -> 0.8F */
					/* ==== */

					/* Stay -Away */
					moveClose = -closeCap * (energy * 0.25F + 0.9F);
					
					/* Stay -Left */
					moveRight = moveClose;
					
					/* Stay +Up */
					moveUp = -moveClose;
				}
			}
			else
			{
				/* (THRUST) LOW | 0.2F -> 0.4F */
				/* === */
				
				/* Move -Away */
				moveClose = (0.2F - energy) * closeCap * 5.0F;
				
				/* Move -Left */
				moveRight = moveClose;
				
				/* Move +Up */
				moveUp = -moveClose -0.25F * closeCap;
			}
		}
		else
		{
			/* (READY) LOWEST | 0.0F -> 0.2F */
			moveClose = MathHelper.sin(energy*PI*5.0F) * 0.2F;
			
			moveUp = -energy * closeCap * 2.5F;
		}
		
		GlStateManager.translate(
	       	/* X */ -moveRight * ClientProxy.EHC_INSTANCE.betterCombatOffhand.moveRightVariance,
	       	/* Y */ 1.3F * moveUp * ClientProxy.EHC_INSTANCE.betterCombatOffhand.moveUpVariance,
	       	/* Z */ moveClose * ClientProxy.EHC_INSTANCE.betterCombatOffhand.moveCloseVariance);
		       	
    	GlStateManager.rotate(rotateUp * ClientProxy.EHC_INSTANCE.betterCombatOffhand.rotateUpVariance, 1.0F, 0.0F, 0.0F);
    	GlStateManager.rotate(-rotateCounterClockwise * ClientProxy.EHC_INSTANCE.betterCombatOffhand.rotateCounterClockwiseVariance, 0.0F, 1.0F, 0.0F);
    	GlStateManager.rotate(-rotateLeft * ClientProxy.EHC_INSTANCE.betterCombatOffhand.rotateLeftVariance, 0.0F, 0.0F, 1.0F);
	}
	
	private void animationStabCameraOffhand()
	{
		float f = ClientProxy.EHC_INSTANCE.betterCombatOffhand.getSwingTimerCap() * partialCameraSwing; // todo check camera swing
		
		/* Camera */
//		float rotation = MathHelper.sin(-0.4F + this.offhandEnergy * 2.0F * PI);
		
		float rotation = MathHelper.sin(-1.8F + this.mainhandEnergy * PI);
		ClientProxy.EHC_INSTANCE.mc.player.cameraPitch -= rotation*ConfigurationHandler.cameraPitchSwing * f;
		ClientProxy.EHC_INSTANCE.mc.player.rotationYaw += rotation*ConfigurationHandler.rotationYawSwing * f;
	}
    
	/* ========================================================================================================================= */
	/*															 Math
	/* ========================================================================================================================= */
    
	public static float clamp(float f0, float f1)
	{
		if ( f0 > f1 )
		{
			return f1;
		}
		
		return f0;
	}
	
	public static float negativeClamp(float f0, float f1)
	{
		if ( f0 < f1 )
		{
			return f1;
		}
		
		return f0;
	}

	public static float clampMultiplier(float base, float multiplier, float cap)
	{
		float f = base * multiplier * cap;
		
		if ( f > cap )
		{
			return cap;
		}
		
		return f;
	}
    
	/* ========================================================================================================================= */
	/*															 (+) GUI
	/* ========================================================================================================================= */
	
	private int currentFrameMainhand = 0;
	private int currentFrameOffhand = 0;
	
	private final ResourceLocation MAINHAND_SWEEP_1 = new ResourceLocation(Reference.MOD_ID, "textures/gui/mainhand_sweep_1.png");
	private final ResourceLocation MAINHAND_SWEEP_2 = new ResourceLocation(Reference.MOD_ID, "textures/gui/mainhand_sweep_2.png");
	
	private final ResourceLocation OFFHAND_SWEEP_1 = new ResourceLocation(Reference.MOD_ID, "textures/gui/offhand_sweep_1.png");
	private final ResourceLocation OFFHAND_SWEEP_2 = new ResourceLocation(Reference.MOD_ID, "textures/gui/offhand_sweep_2.png");
	
    private final int FRAME_SIZE_X = 64;
    private final int FRAME_SIZE_Y = 32;
    private final int FRAMES = 4;
    
    @SideOnly( Side.CLIENT )
    public class GuiCrosshairsBC extends Gui
    {
    	public final ResourceLocation ICONS = new ResourceLocation(Reference.MOD_ID + ":textures/gui/icons.png");

    	public void renderAttackIndicator( float partTicks, ScaledResolution scaledRes )
    	{
    		/* This GUI displays above vanilla GUI */
			this.zLevel = 200;

    		ClientProxy.EHC_INSTANCE.mc.getTextureManager().bindTexture(ICONS);
    		GlStateManager.enableBlend();
			
    		// if ( gamesettings.thirdPersonView == 0 )
    		{
    			if ( ClientProxy.EHC_INSTANCE.mc.playerController.isSpectator() && ClientProxy.EHC_INSTANCE.mc.pointedEntity == null )
    			{
    				RayTraceResult rtRes = ClientProxy.EHC_INSTANCE.mc.objectMouseOver;
    				if ( rtRes == null || rtRes.typeOfHit != net.minecraft.util.math.RayTraceResult.Type.BLOCK )
    				{
    					return;
    				}

    				BlockPos blockpos = rtRes.getBlockPos();
    				IBlockState state = ClientProxy.EHC_INSTANCE.mc.world.getBlockState(blockpos);
    				if ( !state.getBlock().hasTileEntity(state) || !(ClientProxy.EHC_INSTANCE.mc.world.getTileEntity(blockpos) instanceof IInventory) )
    				{
    					return;
    				}
    			}
    			
    			/* (/) Disable rendering TWOHAND GUI */
    			if ( !ClientProxy.EHC_INSTANCE.itemStackOffhand.isEmpty() ) // todo MAINHAND can only go with an OFFHAND?
    			{
        			if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.getWeaponProperty() == WeaponProperty.TWOHAND || ClientProxy.EHC_INSTANCE.betterCombatOffhand.getWeaponProperty() == WeaponProperty.TWOHAND || ClientProxy.EHC_INSTANCE.betterCombatOffhand.getWeaponProperty() == WeaponProperty.MAINHAND )
        			{
        				this.drawTexturedModalRect((scaledRes.getScaledWidth()-236)>>1, scaledRes.getScaledHeight() - 19, 16, 0, 16, 16);
        			}
    			}

    			/* Show debug */
    			if ( ClientProxy.EHC_INSTANCE.mc.gameSettings.showDebugInfo && !ClientProxy.EHC_INSTANCE.mc.gameSettings.hideGUI && !ClientProxy.EHC_INSTANCE.mc.player.hasReducedDebug() && !ClientProxy.EHC_INSTANCE.mc.gameSettings.reducedDebugInfo )
    			{
    				GlStateManager.pushMatrix();
    				GlStateManager.translate((scaledRes.getScaledWidth()>>1), (scaledRes.getScaledHeight()>>1), this.zLevel);
    				Entity entity = ClientProxy.EHC_INSTANCE.mc.getRenderViewEntity();
    				
    				if ( entity == null )
    				{
    					return;
    				}
    				
    				GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partTicks, -1.0F, 0.0F, 0.0F);
    				GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partTicks, 0.0F, 1.0F, 0.0F);
    				GlStateManager.scale(-1.0F, -1.0F, -1.0F);
    				net.minecraft.client.renderer.OpenGlHelper.renderDirections(10);
    				GlStateManager.popMatrix();
    			}
    			else
    			{
        			/* + Show crosshair */
    				this.showPlusCrosshair(scaledRes);

    				/* ( ) Show attack indicator */
    				if ( ClientProxy.EHC_INSTANCE.mc.gameSettings.attackIndicator == 1 )
    				{
    					/* (+) Dual-weilding */
    					if ( ClientProxy.EHC_INSTANCE.betterCombatOffhand.hasConfigWeapon() )
    					{
        					this.showMainhandCrosshair(scaledRes);
        					this.showOffhandCrosshair(scaledRes);
    					}
    					/* (+) Shield */
    					else if ( ConfigurationHandler.showShieldCooldownCrosshair && ClientProxy.EHC_INSTANCE.itemStackOffhand.getItem() instanceof ItemShield )
    					{
        					this.showMainhandCrosshair(scaledRes);
        					this.showShieldCrosshair(scaledRes); // todo
    					}
    					/* + Default */
    					else
    					{        					
    						this.showDefaultCrosshair(scaledRes);
    					}
    				}    				
    			}
    		}
    		
    		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    		
    		this.renderSweepOverlay(scaledRes);
    	}
    	
    	private void renderSweepOverlay(ScaledResolution scaledRes)
    	{			
			if ( !ClientProxy.EHC_INSTANCE.betterCombatMainhand.isMining() )
			{
				if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.getAnimation() == Animation.SWEEP && AnimationHandler.isMainhandAttacking() )
				{					
					if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.alternateAnimation )
					{
						if ( ClientProxy.AH_INSTANCE.mainhandEnergy > 0.625F )
						{
							if ( currentFrameMainhand < 16 )
							{
								ClientProxy.EHC_INSTANCE.mc.getTextureManager().bindTexture(MAINHAND_SWEEP_2);
	        		    		
	        		    		float factor = ClientProxy.EHC_INSTANCE.mc.displayHeight / 128.0F;
	        		    		
	        		    		System.out.println(ClientProxy.EHC_INSTANCE.mc.displayHeight + " | " + factor);
	        		    		
	        		    		float scale = factor / scaledRes.getScaleFactor();
	        		    		
	        		    		GlStateManager.scale(scale, scale, 1);
	        		            GlStateManager.rotate(15.0F, 0.0F, 0.0F, 1.0F);

	        		    		scale = 2 * factor / scaledRes.getScaleFactor();
	        		    		
	        		    		int startX = (currentFrameMainhand % 4) * FRAME_SIZE_X;
		        		        int startY = (int)(Math.ceil((currentFrameMainhand + 1) / 4.0D) - 1) * FRAME_SIZE_Y;

		        		        this.drawTexturedModalRect((scaledRes.getScaledWidth()-40) / scale, (scaledRes.getScaledHeight()-192) / scale, startX, startY, FRAME_SIZE_X, FRAME_SIZE_Y);
		        				currentFrameMainhand++;
							}
						}
						else
						{
							currentFrameMainhand = 0;
						}
					}
					else
					{
						if ( ClientProxy.AH_INSTANCE.mainhandEnergy > 0.25F )
						{
							if ( currentFrameMainhand < 16 )
							{
	        		    		ClientProxy.EHC_INSTANCE.mc.getTextureManager().bindTexture(MAINHAND_SWEEP_1);
	        		    		
	        		    		float factor = ClientProxy.EHC_INSTANCE.mc.displayHeight / 128.0F;
	        		    		
	        		    		System.out.println(ClientProxy.EHC_INSTANCE.mc.displayHeight + " | " + factor);
	        		    		
	        		    		float scale = factor / scaledRes.getScaleFactor();
	        		    		
	        		    		GlStateManager.scale(scale, scale, 1);
	        		    		
	        		    		scale = 2 * factor / scaledRes.getScaleFactor();
	        		    		
	        		    		int startX = (currentFrameMainhand % 4) * FRAME_SIZE_X;
		        		        int startY = (int)(Math.ceil((currentFrameMainhand + 1) / 4.0D) - 1) * FRAME_SIZE_Y;

		        		        this.drawTexturedModalRect((scaledRes.getScaledWidth()) / scale, (scaledRes.getScaledHeight()-32) / scale, startX, startY, FRAME_SIZE_X, FRAME_SIZE_Y);
		        				currentFrameMainhand++;
							}
						}
						else
						{
					        currentFrameMainhand = 0;
						}
					}
				}
			}
    	}
    	
		private void showPlusCrosshair( ScaledResolution scaledRes )
		{
	    	GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.enableAlpha();
			this.drawTexturedModalRect((scaledRes.getScaledWidth()>>1) - 7, (scaledRes.getScaledHeight()>>1) - 7, 0, 0, 16, 16);
		}
		
		private void showDefaultCrosshair( ScaledResolution scaledRes )
		{
			if ( ClientProxy.EHC_INSTANCE.mainhandCooldown > 0 )
			{
				int i = (scaledRes.getScaledHeight()>>1) - 7;
				int j = (scaledRes.getScaledWidth()>>1) - 7;
				int k = (int) (ClientProxy.EHC_INSTANCE.getMainhandCooledAttackStrength() * 18.0F);
				
				this.drawTexturedModalRect(j - 1, i + 12, 68, 94, 17, 8);
				this.drawTexturedModalRect(j - 1, i + 12, 68, 102, k, 8);
			}			
		}

		private void showMainhandCrosshair( ScaledResolution scaledRes )
		{
			if ( ClientProxy.EHC_INSTANCE.mainhandCooldown > 0 )
			{
				int i = (scaledRes.getScaledHeight()>>1) - 7;
				int j = (scaledRes.getScaledWidth()>>1) - 7;
				int k = (int) (ClientProxy.EHC_INSTANCE.getMainhandCooledAttackStrength() * 18.0F);
				
				this.drawTexturedModalRect(j + 15, i, 51, 94, 8, 17);
				this.drawTexturedModalRect(j + 15, i + 17 - k, 59, 111 - k, 8, k);
			}			
		}
		
		private void showOffhandCrosshair( ScaledResolution scaledRes )
		{
			if ( ClientProxy.EHC_INSTANCE.offhandCooldown > 0 )
			{
				int i = (scaledRes.getScaledHeight()>>1) - 7;
				int j = (scaledRes.getScaledWidth()>>1) - 7;
				int k = (int) (ClientProxy.EHC_INSTANCE.getOffhandCooledAttackStrength() * 18.0F);
				this.drawTexturedModalRect(j - 8, i, 35, 94, 8, 17);
				this.drawTexturedModalRect(j - 8, i + 17 - k, 43, 111 - k, 8, k);
			}			
		}
		
		private void showShieldCrosshair( ScaledResolution scaledRes )
		{
			float cooldown = 1.0F - ClientProxy.EHC_INSTANCE.mc.player.getCooldownTracker().getCooldown(ClientProxy.EHC_INSTANCE.itemStackOffhand.getItem(), 0.0F);

			if ( cooldown < 1.0F )
			{
				int i = (scaledRes.getScaledHeight()>>1) - 7;
				int j = (scaledRes.getScaledWidth()>>1) - 7;
				int k = (int) (cooldown * 18.0F);
				this.drawTexturedModalRect(j - 8, i, 35, 94, 8, 17);
				this.drawTexturedModalRect(j - 8, i + 17 - k, 43, 111 - k, 8, k);
			}			
		}
    }
	 
	@SubscribeEvent( priority = EventPriority.LOWEST, receiveCanceled = true )
	public void onRenderGameOverlay( RenderGameOverlayEvent.Pre event )
	{
		switch( event.getType() )
		{
			case CROSSHAIRS:
			{
				if ( !event.isCanceled() && !ConfigurationHandler.showDefaultCrosshair )
				{
					event.setCanceled(true);
					this.gc.renderAttackIndicator(0.5F, new ScaledResolution(Minecraft.getMinecraft()));
					MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(event, event.getType()));
				}
				
				break;
			}
			default:
			{
				break;
			}
		}
	}
}