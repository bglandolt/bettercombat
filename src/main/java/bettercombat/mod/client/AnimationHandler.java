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
	public AnimationHandler()
	{
        this.equippedProgressMainhand = 0.0F;
        this.equippedProgressOffhand = 0.0F;
        
    	this.mainhandEnergy = 0.0F;
    	this.offhandEnergy = 0.0F;
    	
//    	this.mainhandAnimationTicks = 0.0F;
//    	this.offhandAnimationTicks = 0.0F;
    	
    	this.breatheTicks = 0.0F;
    	this.mainhandSprintingTimer = 0;
    	this.offhandSprintingTimer = 0;
    	
    	this.tooClose = false;
    	this.tooCloseTimer = 0.0F;

    	this.blockingTimer = -1;
    	
		this.gc = new GuiCrosshairsBC();
        this.itemRenderer = ClientProxy.EHC_INSTANCE.mc.getRenderItem();
	}
	
	/* (+) Crosshair GUI for attacking */
	public final GuiCrosshairsBC gc;
	
	/* Minecraft item renderer */
	public final RenderItem itemRenderer;

	/* equippedProgressMainhand goes from -1.0 to 0.0, used for raising the weapon up to normal position */
	public float equippedProgressMainhand;
	
	/* equippedProgressOffhand goes from -1.0 to 0.0, used for raising the weapon up to normal position */
    public float equippedProgressOffhand;
	
	/* mainhandEnergy goes from 0.0 to 1.0 over the duration of swingTimer */
	public float mainhandEnergy;
	
	/* mainhandAnimationTicks goes from 0.0 to -0.5 to 0.0 to 1.5 over the duration of swingTimer */
//	public float mainhandAnimationTicks;
	
	/* offhandEnergy goes from 0.0 to 1.0 over the duration of swingTimer */
	public float offhandEnergy;
	
	/* offhandAnimationTicks goes from 0.0 to -0.5 to 0.0 to 1.5 over the duration of swingTimer */
//	public float offhandAnimationTicks;
	
	/* Raises the weapon up and down to simulate breathing */
	public float breatheTicks;
	
	public int mainhandSprintingTimer;
	public int offhandSprintingTimer;

	public boolean tooClose;
	/* Capped at 0.4 */
	public double tooCloseAmount;
	/* 0.0 (not close) to 0.4 (close) */
	public float tooCloseTimer;
	/*
	 * How long the player has been blocking for, up to 10 frames (3.33 ticks)
	 * blockingTimer is -1 if there is no shield in the offhandA
	 */
	public int blockingTimer = 0;
	
	/* 0 to 10 */
	public int parryingAnimationTimer = 0;
	
	public int parriedTimer = 0;
	
	/* float version of PI */
	public static final float PI = (float)Math.PI;
	
//	float xx,yy,zz;
	
    /* ======================================================================================================================================== */
    /* ======================================================================================================================================== */
    /*																CUSTOM RENDER 																*/
    /* ======================================================================================================================================== */

	@SubscribeEvent
	public void disableVanillaHandRender( RenderSpecificHandEvent event ) // -90, 120, -36 === -110, 116, -36
	{
		
//		ClientProxy.EHC_INSTANCE.mc.player.hurtTime = 0; // event.getPartialTicks(); // TODO
//		ClientProxy.EHC_INSTANCE.mc.player.maxHurtTime = 1000; // TODO
//		ClientProxy.EHC_INSTANCE.mc.player.attackedAtYaw = 0;
//		xx = (float)ClientProxy.EHC_INSTANCE.mc.player.posX;
//		yy = (float)ClientProxy.EHC_INSTANCE.mc.player.posY - 50;
//		zz = (float)ClientProxy.EHC_INSTANCE.mc.player.posZ;
				
		/* ItemRenderer */		
		if ( event.getHand() == EnumHand.MAIN_HAND )
        {
			if ( ClientProxy.EHC_INSTANCE.mc.player.isSprinting() && this.equippedProgressMainhand != -1.0F && !this.isMainhandAttacking() )
			{
				if ( this.mainhandSprintingTimer < 20 )
				{
					this.mainhandSprintingTimer += 2;
				}
			}
			else if ( this.mainhandSprintingTimer > 0 )
			{
				this.mainhandSprintingTimer -= 2;
				
				if ( this.mainhandSprintingTimer > 0 && this.isMainhandAttacking() )
				{
					this.mainhandSprintingTimer -= 2;
				}
			}
			
			if ( this.tooClose )
			{
				if ( this.tooCloseTimer < this.tooCloseAmount * 0.7F )
				{
					this.tooCloseTimer += 0.01F;
				}
				else if ( this.tooCloseTimer > this.tooCloseAmount * 1.3F )
				{
					this.tooCloseTimer -= 0.01F;
				}
			}
			else
			{
				if ( this.tooCloseTimer > 0.0F )
				{
					this.tooCloseTimer -= 0.02F;
				}
				else
				{
					this.tooCloseTimer = 0.0F;
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
			
			if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.hasCustomWeapon() )
			{
				/* If the MAINHAND is active */
	    		if ( Helpers.isHandActive(ClientProxy.EHC_INSTANCE.mc.player, EnumHand.MAIN_HAND) )
	    		{
	    			/* Default rendering, such as bow, eat, or drink! */
					this.positionBreathing();
					return;
	    		}
	    		
	    		this.customMainhandRender(event);
				return;
			}
			else
			{
				/* Default rendering! */
				this.positionBreathing();
				return;
			}
        }
		else if ( event.getHand() == EnumHand.OFF_HAND )
        {
			if ( ClientProxy.EHC_INSTANCE.mc.player.isSprinting() && this.equippedProgressOffhand != -1.0F && !this.isOffhandAttacking() )
			{
				if ( this.offhandSprintingTimer < 20 )
				{
					this.offhandSprintingTimer += 2;
				}
			}
			else if ( this.offhandSprintingTimer > 0 )
			{
				this.offhandSprintingTimer -= 2;
				
				if ( this.offhandSprintingTimer > 0 && this.isOffhandAttacking() )
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
    		else if ( ClientProxy.EHC_INSTANCE.betterCombatOffhand.hasCustomWeapon() )
			{
    			this.customOffhandRender(event);
    			return;
			}
    		else if ( ClientProxy.EHC_INSTANCE.itemStackOffhand.getItem() instanceof ItemShield )
    		{
    	    	if ( Helpers.isHandActive(ClientProxy.EHC_INSTANCE.mc.player, EnumHand.OFF_HAND) && ( !ConfigurationHandler.disableBlockingWhileAttacking || ClientProxy.EHC_INSTANCE.isMainhandAttackReady() ) ) // && !this.isMainhandAttacking() && this.equippedProgressMainhand >= 0.0F )
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
    			
    			return;
    		}
    		else
    		{
    			this.blockingTimer = -1;
    		}

    		/* Default rendering! */
			this.positionBreathing();
			return;
        }
    }
	
    private void positionBreathingShield()
    {
    	GlStateManager.translate(0.0F, MathHelper.sin(this.breatheTicks) * ConfigurationHandler.breathingAnimationIntensity, -this.tooCloseTimer*0.6F);
	}

	/* ======================================================================================================================================== */
    /*																	MAINHAND 																*/
    /* ======================================================================================================================================== */

	private void customMainhandRender( RenderSpecificHandEvent event )
	{
		event.setCanceled(true);
		event.setResult(Result.DENY);
		
		GlStateManager.pushMatrix();

        this.positionMainWeapon();
		this.positionMainhandAwayIfOffhandAttacking();
        this.positionBreathing();
        
		if ( this.isMainhandAttacking() )
		{
			this.mainhandEnergy = 1.0F + (event.getPartialTicks() - ClientProxy.EHC_INSTANCE.betterCombatMainhand.getSwingTimer() - 0.5F) * ClientProxy.EHC_INSTANCE.betterCombatMainhand.getSwingTimerIncrement();
			
			/* Mining */
			if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.isMining() )
    		{
				Item tool = ClientProxy.EHC_INSTANCE.itemStackMainhand.getItem();
				
				if ( tool instanceof ItemSpade )
				{
//					if ( this.mainhandSprintingTimer < 20 )
//					{
//						this.mainhandSprintingTimer += 6;
//					}
//					
//					if ( this.mainhandSprintingTimer > 20 )
//					{
//						this.mainhandSprintingTimer = 20;
//					}
//					
//		        	GlStateManager.rotate(-this.mainhandSprintingTimer*2 ,1.0F,0.0F,0.0F); /* Chopping rotation */
		        	
		        	// this.animationChopMainhand();
		        	// this.animationSweepMainhand();
		        	this.animationDiggingMainhand();
					// this.resetEquippedProgressMainhand();
				}
				else if ( tool instanceof ItemAxe )
				{
					this.animationSweepMainhand();
					//this.resetEquippedProgressMainhand();
				}
				else if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.getAnimation().equals(Animation.STAB) )
				{
	        		this.animationStabMainhand();
					//this.resetEquippedProgressMainhand();
				}
				else /* Sword, Pickaxe, Axe */
				{
					this.animationChopMainhand();
					//this.resetEquippedProgressMainhand();
				}
			}
			else switch ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.getAnimation() )
	        {
	        	case SWEEP:
	        	{
	        		if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.alternateAnimation )
	        		{
	        			this.animationSweepMainhand2();
	        		}
	        		else
	        		{
	        			this.animationSweepMainhand();
	        		}
	        		
        			this.animationSweepCameraMainhand();
        			this.resetEquippedProgressMainhand();
	        		break;
	        	}
	        	case CHOP:
	        	{
            		this.animationChopMainhand();
            		this.animationChopCameraMainhand();
        			this.resetEquippedProgressMainhand();
	        		break;
	        	}
	        	case STAB:
	        	{
	        		this.animationStabMainhand();
	               	this.animationStabCameraMainhand();
	               	/* No re-equip animation */
	            	this.equippedProgressMainhand = 0.0F;
	        		break;
	        	}
	        	default:
	        	{
	        		this.animationChopMainhand();
            		this.animationChopCameraMainhand();
        			this.resetEquippedProgressMainhand();
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
			
			this.mainhandEnergy = 0.0F;			
	        this.positionEquippedProgressMainhand();
		}
		
		this.renderMainWeapon();
	        
		GlStateManager.popMatrix();
	}
    
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
        	GlStateManager.translate(0.0F, -this.tooCloseTimer, 0.0F);
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
//    	/* Position this weapon away when the player shield bashing XXX */
//    	else if ( !this.blockingTimerActive() && this.isOffhandAttacking() )
//    	{
//			float f = MathHelper.sin(this.offhandEnergy*this.offhandEnergy*PI)*0.5F;
//            GlStateManager.translate(f, -f * 0.5F, 0.0F);
//    	}
    }
    
    private void positionMainhandAwayIfOffhandAttacking()
	{
    	if ( this.isOffhandAttacking() )
    	{
    		float f = MathHelper.sin(this.offhandEnergy*PI);
            GlStateManager.translate(f*0.0625F, -f*0.025F, 0.0F);
			/* Move this hand up left */
//	    	switch ( ClientProxy.EHC_INSTANCE.betterCombatOffhand.getAnimation() )
//	        {
//	        	case SWEEP:
//	        	{
//	        		float f = MathHelper.sin(this.offhandEnergy*PI);
//	                GlStateManager.translate(f*0.125F, -f*0.05F, 0.0F);
//	                break;
//	            }
//	        	case CHOP:
//	        	{
//	        		float f = MathHelper.sin(this.offhandEnergy*PI);
//	                GlStateManager.translate(f*0.125F, -f*0.05F, 0.0F);
//	                break;
//	            }
//	        	case STAB:
//	        	{
//	        		float f = MathHelper.sin(this.offhandEnergy*PI);
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
    
    /* ====================================================================================================================================================== */
    /*																		OFFHAND 																		  */
    /* ====================================================================================================================================================== */
	
	public void customOffhandRender( RenderSpecificHandEvent event )
	{
		event.setCanceled(true);
		event.setResult(Result.DENY);
		
		GlStateManager.pushMatrix();

        this.positionOffhandWeapon();
		this.positionOffhandAwayIfMainhandAttacking();
        this.positionBreathing();
        
		if ( this.isOffhandAttacking() )
		{
			this.offhandEnergy = 1.0F + (event.getPartialTicks() - ClientProxy.EHC_INSTANCE.betterCombatOffhand.getSwingTimer()) * ClientProxy.EHC_INSTANCE.betterCombatOffhand.getSwingTimerIncrement();
			// this.offhandAnimationTicks = MathHelper.cos(PI - 1.0F + this.offhandEnergy * 4.0F) + 0.55F;
    		
			switch ( ClientProxy.EHC_INSTANCE.betterCombatOffhand.getAnimation() )
	        {
	        	case SWEEP:
	        	{
	        		this.animationSweepOffhand();
	        		this.animationSweepCameraOffhand();
        			this.resetEquippedProgressOffhand();
	        		break;
	        	}
	        	case CHOP:
	        	{
	        		this.animationChopOffhand();
	        		this.animationChopCameraOffhand();
        			this.resetEquippedProgressOffhand();
	        		break;
	        	}
	        	case STAB:
	        	{
		        	this.animationStabOffhand();
		        	this.animationStabCameraOffhand();
	            	this.equippedProgressOffhand = 0.0F;
	        		break;
	        	}
	        	default:
	        	{
	        		this.animationChopOffhand();
	        		this.animationChopCameraOffhand();
        			this.resetEquippedProgressOffhand();
	        		break;
	        	}
	        }
		}
		else
		{
			this.offhandEnergy = 0.0F;
//			this.offhandAnimationTicks = 0.0F;
	        
	        this.positionEquippedProgressOffhand();
		}
		
		this.renderOffWeapon();
        
    	GlStateManager.popMatrix();
	}
	
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
        	GlStateManager.translate(0.0F, -this.tooCloseTimer, 0.0F);
        }
        else
        {
        	GlStateManager.rotate(-13.0F-this.offhandSprintingTimer,1.0F,0.0F,0.0F); /* Chopping rotation */
        	GlStateManager.rotate(13.0F,0.0F,1.0F,0.0F);
        	GlStateManager.rotate(13.0F,0.0F,0.0F,1.0F);
        }
	}
	
//	private float miningTimer = 0.0F;
	
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
    	
    	if ( this.isMainhandAttacking() )
    	{
			float f = MathHelper.sin(this.mainhandEnergy*PI);
            GlStateManager.translate(-f*0.125F, -f*0.05F, 0.0F);
    		
			/* Move this hand up left */
//	    	switch ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.getAnimation() )
//	        {
//	        	case SWEEP:
//	        	{
//	        		float f = MathHelper.sin(this.mainhandEnergy*PI);
//	                GlStateManager.translate(-f*0.15F, -f*0.1F, 0.0F);
//	                break;
//	            }
//	        	case CHOP:
//	        	{
//	        		float f = MathHelper.sin(this.mainhandEnergy*PI);
//	                GlStateManager.translate(-f*0.1F, -f*0.1F, f*0.1F);
//	                break;
//	            }
//	        	case STAB:
//	        	{
//	        		float f = MathHelper.sin(this.mainhandEnergy*PI);
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
	
    /* ---------------------------------------------------------------------------------------------------------------------------------------- */
	/*																CHOP ANIMATION																*/
    /* ---------------------------------------------------------------------------------------------------------------------------------------- */
	
	private void animationChopMainhand()
	{
		float moveRight = 0.0F; /* +right */
		float moveUp = 0.0F; /* +up */
		float moveClose = 0.0F; /* +zoom */
		
		float rotateUp = 0.0F;
		float rotateCounterClockwise = clamp(this.mainhandEnergy*300.0F,15.0F) - this.mainhandEnergy*15.0F;
		float rotateLeft = clamp(this.mainhandEnergy*100.0F,30.0F); /* +left */

	    float closeCap = (float)this.tooCloseTimer - 0.4F;
		
		if ( this.mainhandEnergy > 0.2F )
		{
			if ( this.mainhandEnergy > 0.7F )
			{
				/* HIGHEST | holdDuration -> 1 */
				/* ======= */
				float energy = this.mainhandEnergy - 0.7F;

				moveUp = -energy;
				
				moveRight = -0.7F + energy * 2.0F;
								
				moveClose = closeCap + energy * 6.0F;
				
				
				rotateUp = -95.0F;
			}
			else
			{
				/* (HOLD) HIGH | 0.2 -> 0.7 */
				/* ==== */
				
				moveRight = -0.7F;
				
				moveClose = closeCap;
				

				rotateUp = -95.0F;

				if ( this.mainhandEnergy < 0.4F )
				{
					rotateUp += MathHelper.sin((this.mainhandEnergy-0.2F)*PI*5.0F) * 5.0F;
				}
			}
		}
		else
		{
			/* (SWING) LOW | 0.0 -> 0.2 */
			/* === */
			float energy = 1.0F - MathHelper.cos(this.mainhandEnergy*PI*2.5F);
						
			moveRight = energy * -0.7F;
			
			moveClose = energy * closeCap;
			
			
			rotateUp = energy * -95.0F;
		}
		
	    if ( this.mainhandEnergy <= 0.22F )
	    {
			moveUp = MathHelper.sin(this.mainhandEnergy*PI*4.6F) * 0.18F + 0.01F;
	    }

		GlStateManager.translate(
       	/* X */ 1.1F * moveRight * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveRightVariance,
       	/* Y */ (moveUp - (ClientProxy.EHC_INSTANCE.betterCombatMainhand.isMining() ? 0.0F : 0.15F)) * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveUpVariance,
       	/* Z */ moveClose * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveCloseVariance);
       	
		/* Chop */
    	GlStateManager.rotate( (ClientProxy.EHC_INSTANCE.betterCombatMainhand.isMining() ? negativeClamp(rotateUp, -80.0F) : rotateUp) * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateUpVariance, 1.0F, 0.0F, 0.0F);
    	
    	GlStateManager.rotate(rotateCounterClockwise * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateCounterClockwiseVariance, 0.0F, 1.0F, 0.0F);

    	/* Swivel back and forth */
    	GlStateManager.rotate(rotateLeft * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateLeftVariance, 0.0F, 0.0F, 1.0F);
	}
	
	private void animationChopCameraMainhand()
	{
		/* Reduce momentum based off attack speed */
		float f = ClientProxy.EHC_INSTANCE.betterCombatMainhand.getSwingTimerCap() / 12.0F;
		
		/* Camera */
    	float rotation = MathHelper.sin( 0.75F + this.mainhandEnergy * 2.5F * PI);
    	
    	if ( this.mainhandEnergy < 0.75F || rotation < 0.0F )
    	{
    		ClientProxy.EHC_INSTANCE.mc.player.cameraPitch -= rotation * ConfigurationHandler.cameraPitchSwing * 2.0F * f;
    	}
    	
		ClientProxy.EHC_INSTANCE.mc.player.rotationYaw += rotation*ConfigurationHandler.rotationYawSwing * 0.2F * f;
	}
	
	private void animationChopOffhand()
	{
		float moveRight = 0.0F; /* +right */
		float moveUp = 0.0F; /* +up */
		float moveClose = 0.0F; /* +zoom */
		
		float rotateUp = 0.0F;
		float rotateCounterClockwise = clamp(this.offhandEnergy*300.0F,15.0F) - this.offhandEnergy*15.0F;
		float rotateLeft = clamp(this.offhandEnergy*100.0F,30.0F); /* +left */
		
	    float closeCap = (float)this.tooCloseTimer - 0.4F;

		if ( this.offhandEnergy > 0.2F )
		{
			if ( this.offhandEnergy > 0.7F )
			{
				/* HIGHEST | holdDuration -> 1 */
				/* ======= */
				float energy = this.offhandEnergy - 0.7F;

				moveUp = -energy;
				
				moveRight = -0.7F + energy * 2.0F;
								
				moveClose = closeCap + energy * 6.0F;
				
				
				rotateUp = -95.0F;
			}
			else
			{
				/* (HOLD) HIGH | 0.25 -> 0.65 */
				/* ==== */
								
				moveRight = -0.7F;
				
				moveClose = closeCap;
				

				rotateUp = -95.0F;

				if ( this.offhandEnergy < 0.4F )
				{
					rotateUp += MathHelper.sin((this.offhandEnergy-0.2F)*PI*5.0F) * 5.0F;
				}
			}
		}
		else
		{
			/* (SWING) LOW | 0.0 -> 0.25 */
			/* === */
			float energy = 1.0F - MathHelper.cos(this.offhandEnergy*PI*2.5F);
						
			moveRight = energy * -0.7F;
			
			moveClose = energy * closeCap;
			
			
			rotateUp = energy * -95.0F;
		}
		
		if ( this.offhandEnergy <= 0.22F )
	    {
			moveUp = MathHelper.sin(this.offhandEnergy*PI*4.6F) * 0.18F + 0.01F;
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
		/* Reduce momentum based off attack speed */
		float f = ClientProxy.EHC_INSTANCE.betterCombatOffhand.getSwingTimerCap() / 12.0F;
		
		/* Camera */
    	float rotation = MathHelper.sin( 0.75F + this.offhandEnergy * 2.5F * PI);
    	
    	if ( this.offhandEnergy < 0.75F || rotation < 0.0F )
    	{
    		ClientProxy.EHC_INSTANCE.mc.player.cameraPitch += rotation * ConfigurationHandler.cameraPitchSwing * 2.0F * f;
    	}
    	
		ClientProxy.EHC_INSTANCE.mc.player.rotationYaw += rotation*ConfigurationHandler.rotationYawSwing * 0.2F * f;
	}
	
    /* ---------------------------------------------------------------------------------------------------------------------------------------- */
	/*																SWEEP ANIMATION																*/
    /* ---------------------------------------------------------------------------------------------------------------------------------------- */
	
	private void animationSweepMainhand()
	{
		float moveRight = 0.0F;
	    float moveUp = 0.0F;
	    float moveClose = 0.0F;
	    float rotateUp = 0.0F;
	    float rotateCounterClockwise = 0.0F;
	    float rotateLeft = 0.0F;
	    
	    float closeCap = 0.4F - this.tooCloseTimer;
	    
	    rotateUp = -clampMultiplier(this.mainhandEnergy, 6.0F, 140.0F + closeCap * 40.0F); /* Sweep = Up */
	    
		/* If mining, then it should be hitting a block and stopping early */
	    if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.isMining() )
	    {
	    	closeCap -= 0.2F;
	    }
	    
	    rotateCounterClockwise = clampMultiplier(this.mainhandEnergy, 12.0F, 150.0F) - clampMultiplier(this.mainhandEnergy, 3.0F, 50.0F + closeCap * 100.0F) - this.mainhandEnergy * 15.0F; /* Sweep = Left and To */
	    rotateLeft = clampMultiplier(this.mainhandEnergy, 6.0F, 85.0F); /* Sweep = Twist Clockwise -- * */

	    /* Move right very fast at the start */
	    moveRight = clampMultiplier(this.mainhandEnergy, 12.0F, 3.5F) + 0.5F;
		moveUp = clamp(this.mainhandEnergy*10.0F, 0.47F);
		moveClose = -clamp(this.mainhandEnergy*10.0F, closeCap);
	    
	    if ( this.mainhandEnergy > 0.6F )
	    {
		    /* Move left slowly as the animation has reached the center */
		    moveRight -= 4.5F + closeCap - (1.0F - MathHelper.sin(this.mainhandEnergy * PI)) * 0.3F;
		    
		    if ( this.mainhandEnergy > 0.85F )
		    {
				moveUp -= MathHelper.sin(this.mainhandEnergy - 0.85F) * 6.0F;
		        moveClose += MathHelper.sin(this.mainhandEnergy - 0.85F) * 6.0F;
		        moveRight += (this.mainhandEnergy - 0.85F) * 5.5F;
		    }
	    }
	    else
	    {
		    /* Move left fast until 0.6 energy */
	    	moveRight -= clamp(MathHelper.sin(this.mainhandEnergy * PI) * 5.5F, 4.5F + closeCap);
	    }
		
		GlStateManager.translate(
       	/* X */ moveRight * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveRightVariance,
       	/* Y */ moveUp * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveUpVariance,
       	/* Z */ moveClose * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveCloseVariance);
       	
    	GlStateManager.rotate(rotateUp * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateUpVariance, 1.0F, 0.0F, 0.0F);
    	GlStateManager.rotate(rotateCounterClockwise * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateCounterClockwiseVariance, 0.0F, 1.0F, 0.0F);
    	GlStateManager.rotate(rotateLeft * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateLeftVariance, 0.0F, 0.0F, 1.0F);
	}
	
	// XXXAAA
	private void animationSweepMainhand2()
	{
		float moveRight = 0.0F;
	    float moveUp = 0.0F;
	    float moveClose = 0.0F;
	    float rotateUp = 0.0F;
	    float rotateCounterClockwise = 0.0F;
	    float rotateLeft = 0.0F;
	    
	    float f = this.mainhandEnergy;
	    
	    float closeCap = 1.0F - this.tooCloseTimer * 2.5F;
	    
	    if ( f > 0.2F )
		{
			if ( f > 0.6F )
			{
				/* (SWEEP) HIGHEST | 0.6F -> 1 */
				/* ======= */
				float energy = (f - 0.6F);
				
				rotateCounterClockwise = energy * -400.0F;
				
				//rotateUp = 10.0F + energy * 100.0F;
				
				moveUp = 0.2F - energy * 3.0F;
				
				moveRight = -1.55F + energy * 30.0F;
				
				rotateLeft = 80.0F + energy * 25.0F;
				
			    moveClose = (-energy * 3.0F) * closeCap;
			}
			else
			{
				if ( f > 0.4F )
				{
					/* (HOLD) HIGH | 0.4F -> 0.6F */
					/* ==== */
					float energy = (f - 0.4F);

					//rotateUp = energy * 50.0F;
					
					moveUp = 0.2F;
					
				    moveRight = -1.5F - energy * 0.25F;
				    
					rotateLeft = 75.0F + energy * 25.0F;
					
				    moveClose = (0.2F - energy) * closeCap;
				}
				else
				{
					/* (FAST READY) LOW | 0.2F -> 0.4F */
					/* ==== */
					float energy = (f - 0.2F);

					/* Fast > Slow */
					moveUp = -0.2F + MathHelper.sin(energy*PI*2.5F) * 0.4F;
				    moveRight = -1.2F - MathHelper.sin(energy*PI*2.5F) * 0.3F;
					
					rotateLeft = 25.0F + energy * 250.0F;
					
				    moveClose = f * 0.5F * closeCap;
				}
			}
		}
		else
		{
			/* (FAST READY) LOWEST | 0.0F -> 0.2F */
			/* ==== */
			
			/* Slow > Fast | (1.0F - MathHelper.cos(f*PI*2.5F)) */
			moveUp = -f;
		    moveRight = f * -6.0F;

			rotateLeft = f * 125.0F;
			
		    moveClose = f * 0.5F * closeCap;
		}
	    		
		GlStateManager.translate(
       	/* X */ 0.75F * moveRight * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveRightVariance,
       	/* Y */ 0.75F * moveUp * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveUpVariance,
       	/* Z */ moveClose * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveCloseVariance);
       	
//		float xx = (float)ClientProxy.EHC_INSTANCE.mc.player.posX;
//		float yy = (float)ClientProxy.EHC_INSTANCE.mc.player.posY - 50;
//		float zz = (float)ClientProxy.EHC_INSTANCE.mc.player.posZ;
//		
//		rotateUp = xx;
		
//		moveClose = MathHelper.sin(f*PI*1.5F) * 0.1F;
//		rotateCounterClockwise = MathHelper.sin(f*PI) * -9.0F;
		
		GlStateManager.rotate(rotateUp * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateUpVariance, 1.0F, 0.0F, 0.0F);
    	GlStateManager.rotate(rotateCounterClockwise * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateCounterClockwiseVariance, 0.0F, 1.0F, 0.0F);
    	GlStateManager.rotate(rotateLeft * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateLeftVariance, 0.0F, 0.0F, 1.0F);
	}
	
//	private void animationSweepMainhand2()
//	{
//		float moveRight = 0.0F;
//	    float moveUp = 0.0F;
//	    float moveClose = 0.0F;
//	    float rotateUp = 0.0F;
//	    float rotateCounterClockwise = 0.0F;
//	    float rotateLeft = 0.0F;
//	    
//	    float f = this.mainhandEnergy;
//	    
//	    float closeCap = 0.4F - this.tooCloseTimer;
//	    
//	    if ( f > 0.2F )
//		{
//			if ( f > 0.7F )
//			{
//				/* (SWEEP) HIGHEST | 0.7F -> 1 */
//				/* ======= */
//				float energy = (f - 0.7F);
//				
//				moveClose = 0.23F - energy;
//				
//				rotateCounterClockwise = energy * -300.0F;
//				rotateUp = energy * 100.0F;
//				
//				moveUp = 0.34F - energy * 0.6F;
//				moveRight = -1.56F + energy * 15.0F;
//				
//				rotateLeft = 80.0F + energy * 25.0F;
//			}
//			else
//			{
//				if ( f > 0.4F )
//				{
//					/* (HOLD) HIGH | 0.4F -> 0.7F */
//					/* ==== */
//					float energy = (f - 0.4F);
//
//					moveUp = 0.4F - energy * 0.2F;
//				    moveRight = -1.5F - energy * 0.1F;
//				    
//					rotateLeft = 70.0F + energy * 25.0F;
//					
//					moveClose = 0.2F + energy * 0.1F;
//				}
//				else
//				{
//					/* (FAST READY) LOW | 0.2F -> 0.4F */
//					/* ==== */
//					float energy = (f - 0.2F);
//
//					/* Fast > Slow */
//					moveUp = 0.2F + MathHelper.sin(energy*PI*2.5F) * 0.2F;
//				    moveRight = -1.2F - MathHelper.sin(energy*PI*2.5F) * 0.3F;
//					
//					rotateLeft = 10.0F + energy * 300.0F;
//					
//					moveClose = 0.1F + energy * 0.5F;
//				}
//			}
//		}
//		else
//		{
//			/* (FAST READY) LOWEST | 0.0F -> 0.2F */
//			/* ==== */
//			
//			/* Slow > Fast | (1.0F - MathHelper.cos(f*PI*2.5F)) */
//			moveUp = f;
//		    moveRight = f * -6.0F;
//
//		    /* 60.0F */
//			rotateLeft = f * 50.0F;
//			
//			moveClose = f * 0.5F;
//		}
//	    
//		
//		GlStateManager.translate(
//       	/* X */ 1.25F * moveRight * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveRightVariance,
//       	/* Y */ 0.5F * moveUp * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveUpVariance,
//       	/* Z */ moveClose * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveCloseVariance);
//       	
////		float xx = (float)ClientProxy.EHC_INSTANCE.mc.player.posX;
////		float yy = (float)ClientProxy.EHC_INSTANCE.mc.player.posY - 50;
////		float zz = (float)ClientProxy.EHC_INSTANCE.mc.player.posZ;
////		
////		rotateUp = xx;
//		
////		moveClose = MathHelper.sin(f*PI*1.5F) * 0.1F;
////		rotateCounterClockwise = MathHelper.sin(f*PI) * -9.0F;
//		
//		GlStateManager.rotate(rotateUp * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateUpVariance, 1.0F, 0.0F, 0.0F);
//    	GlStateManager.rotate(rotateCounterClockwise * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateCounterClockwiseVariance, 0.0F, 1.0F, 0.0F);
//    	GlStateManager.rotate(rotateLeft * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateLeftVariance, 0.0F, 0.0F, 1.0F);
//	}

	private void animationSweepCameraMainhand()
	{
		float f = ClientProxy.EHC_INSTANCE.betterCombatMainhand.getSwingTimerCap() / 12.0F;

		/* Camera */
    	float rotation = MathHelper.cos(0.5F + this.mainhandEnergy * 2.5F * PI);
		ClientProxy.EHC_INSTANCE.mc.player.cameraPitch -= rotation * ConfigurationHandler.cameraPitchSwing * f;
		ClientProxy.EHC_INSTANCE.mc.player.rotationYaw += rotation * ConfigurationHandler.rotationYawSwing * f;
	}
	
	private void animationSweepOffhand()
	{
		float moveRight = 0.0F;
	    float moveUp = 0.0F;
	    float moveClose = 0.0F;
	    float rotateUp = 0.0F;
	    float rotateCounterClockwise = 0.0F;
	    float rotateLeft = 0.0F;
	    
	    float closeCap = 0.4F - this.tooCloseTimer;
	    
	    rotateUp = -clampMultiplier(this.offhandEnergy, 6.0F, 140.0F + closeCap * 40.0F); /* Sweep = Up */
//	    rotateCounterClockwise = clampMultiplier(this.mainhandEnergy, 12.0F, 150.0F) - clampMultiplier(this.mainhandEnergy, 3.0F, 50.0F + closeCap * 10.0F) - this.mainhandEnergy * 15.0F; /* Sweep = Left and To */
	    rotateCounterClockwise = clampMultiplier(this.offhandEnergy, 12.0F, 150.0F) - clampMultiplier(this.offhandEnergy, 3.0F, 50.0F + closeCap * 100.0F) - this.offhandEnergy * 15.0F; /* Sweep = Left and To */
	    rotateLeft = clampMultiplier(this.offhandEnergy, 6.0F, 85.0F); /* Sweep = Twist Clockwise -- * */

	    /* Move right very fast at the start */
	    moveRight = clampMultiplier(this.offhandEnergy, 12.0F, 3.5F) + 0.5F;
		moveUp = clamp(this.offhandEnergy*10.0F, 0.47F);
		moveClose = -clamp(this.offhandEnergy*10.0F, closeCap);
	    		
	    if ( this.offhandEnergy > 0.6F )
	    {
		    moveRight -= 4.5F + closeCap - (1.0F - MathHelper.sin(this.offhandEnergy * PI)) * 0.3F;
		    
		    if ( this.offhandEnergy > 0.85F )
		    {
				moveUp -= MathHelper.sin(this.offhandEnergy - 0.85F) * 6.0F;
		        moveClose += MathHelper.sin(this.offhandEnergy - 0.85F) * 6.0F;
		        moveRight += (this.offhandEnergy - 0.85F) * 5.5F;
		    }
	    }
	    else
	    {
	    	moveRight -= clamp(MathHelper.sin(this.offhandEnergy * PI) * 5.5F, 4.5F + closeCap);
	    }
		
		GlStateManager.translate(
       	/* X */ -moveRight * ClientProxy.EHC_INSTANCE.betterCombatOffhand.moveRightVariance,
       	/* Y */ moveUp * ClientProxy.EHC_INSTANCE.betterCombatOffhand.moveUpVariance,
       	/* Z */ moveClose * ClientProxy.EHC_INSTANCE.betterCombatOffhand.moveCloseVariance);
       	
    	GlStateManager.rotate(rotateUp * ClientProxy.EHC_INSTANCE.betterCombatOffhand.rotateUpVariance, 1.0F, 0.0F, 0.0F);
    	GlStateManager.rotate(-rotateCounterClockwise * ClientProxy.EHC_INSTANCE.betterCombatOffhand.rotateCounterClockwiseVariance, 0.0F, 1.0F, 0.0F);
    	GlStateManager.rotate(-rotateLeft * ClientProxy.EHC_INSTANCE.betterCombatOffhand.rotateLeftVariance, 0.0F, 0.0F, 1.0F);
	}

	private void animationSweepCameraOffhand()
	{
		/* Reduce momentum based off attack speed */
		float f = ClientProxy.EHC_INSTANCE.betterCombatOffhand.getSwingTimerCap() / 12.0F;

		/* Camera */
    	float rotation = MathHelper.cos(0.5F + this.offhandEnergy * 2.5F * PI);
		ClientProxy.EHC_INSTANCE.mc.player.cameraPitch -= rotation * ConfigurationHandler.cameraPitchSwing * f;
		ClientProxy.EHC_INSTANCE.mc.player.rotationYaw += rotation * ConfigurationHandler.rotationYawSwing * f;
	}
	
	/* ---------------------------------------------------------------------------------------------------------------------------------------- */
	/*																	STAB ANIMATION															*/
    /* ---------------------------------------------------------------------------------------------------------------------------------------- */
	
	private void animationDiggingMainhand()
	{
		float moveRight = 0.0F; /* +right */
		float moveUp = 0.0F; /* +up */
		float moveClose = 0.0F; /* +zoom */
		
		float rotateUp = 0.0F; /* +up */
		float rotateCounterClockwise = 0.0F; /* +counter-clockwise */
		float rotateLeft = 0.0F; /* +left */
		
	    float closeCap = (0.5F - this.tooCloseTimer);
		
		rotateUp = -clamp(this.mainhandEnergy * 240.0F, 60.0F);
		
		rotateCounterClockwise = clamp(this.mainhandEnergy * 200.0F, 80.0F) - this.mainhandEnergy * 20.0F;
		rotateLeft = clamp(this.mainhandEnergy * 100.0F, 40.0F) - this.mainhandEnergy * 20.0F;
		
		if ( this.mainhandEnergy > 0.2F )
		{
			if ( this.mainhandEnergy > 0.4F )
			{
				if ( this.mainhandEnergy > 0.8F )
				{
					/* (RETURN) HIGHEST | 0.8F -> 1 */
					/* ======= */
					float energy = (this.mainhandEnergy - 0.8F);
					
					/* Move +Close */
					moveClose = -closeCap + energy * 2.5F;
					
					/* Move +Right */
					moveRight = moveClose;
					
					/* Move -Down */
					moveUp = -moveClose;
					
					energy *= energy;
					rotateUp += clampMultiplier(energy, 25.0F, 70.0F);
					rotateCounterClockwise -= clampMultiplier(energy, 25.0F, 30.0F);
					rotateLeft -= clampMultiplier(energy, 25.0F, 10.0F);
				}
				else
				{
					/* (HOLD) HIGH | 0.4F -> 0.8F */
					/* ==== */

					/* Stay -Away */
					moveClose = -closeCap * (this.mainhandEnergy * 0.25F + 0.9F);
					
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
				moveClose = (0.2F - this.mainhandEnergy) * closeCap * 5.0F;
				
				/* Move -Left */
				moveRight = moveClose;
				
				/* Move +Up */
				moveUp = -moveClose -0.25F * closeCap;
			}
		}
		else
		{
			/* (READY) LOWEST | 0.0F -> 0.2F */
			moveClose = MathHelper.sin(this.mainhandEnergy*PI*5.0F) * 0.2F;
			
			moveUp = -this.mainhandEnergy * closeCap * 2.5F;
		}
		
		GlStateManager.translate(
       	/* X */ moveRight * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveRightVariance,
       	/* Y */ 1.25F * moveUp * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveUpVariance,
       	/* Z */ 0.5F * moveClose * ClientProxy.EHC_INSTANCE.betterCombatMainhand.moveCloseVariance);
       	
    	GlStateManager.rotate(rotateUp * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateUpVariance, 1.0F, 0.0F, 0.0F);
    	GlStateManager.rotate(rotateCounterClockwise * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateCounterClockwiseVariance, 0.0F, 1.0F, 0.0F);
    	GlStateManager.rotate(rotateLeft * ClientProxy.EHC_INSTANCE.betterCombatMainhand.rotateLeftVariance, 0.0F, 0.0F, 1.0F);
	}
	
	private void animationStabMainhand() // XXXSTAB
	{
		final float f = this.mainhandEnergy;
		final BetterCombatHand hand = ClientProxy.EHC_INSTANCE.betterCombatMainhand;
		
		float moveRight = 0.0F; /* +right */
		float moveUp = 0.0F; /* +up */
		float moveClose = 0.0F; /* +zoom */
		
		float rotateUp = 0.0F; /* +up */
		float rotateCounterClockwise = 0.0F; /* +counter-clockwise */
		float rotateLeft = 0.0F; /* +left */
		
	    float closeCap = (0.6F - this.tooCloseTimer);
		
		rotateUp = -clamp(f * 240.0F, 60.0F);
		
		rotateCounterClockwise = clamp(f * 125.0F, 50.0F) - f * 10.0F;
		rotateLeft = clamp(f * 75.0F, 30.0F) - f * 10.0F;
		
		if ( f > 0.2F )
		{
			if ( f > 0.4F )
			{
				if ( f > 0.8F )
				{
					/* (RETURN) HIGHEST | 0.8F -> 1 */
					/* ======= */
					float energy = (f - 0.8F);
					
					/* Move +Close */
					moveClose = -closeCap + energy * 2.5F;
					
					/* Move +Right */
					moveRight = moveClose;
					
					/* Move -Down */
					moveUp = -moveClose;
					
					energy *= energy;
					rotateUp += clampMultiplier(energy, 25.0F, 70.0F);
					rotateCounterClockwise -= clampMultiplier(energy, 25.0F, 30.0F);
					rotateLeft -= clampMultiplier(energy, 25.0F, 10.0F);
				}
				else
				{
					/* (HOLD) HIGH | 0.4F -> 0.8F */
					/* ==== */

					/* Stay -Away */
					moveClose = -closeCap * (f * 0.25F + 0.9F);
					
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
				moveClose = (0.2F - f) * closeCap * 5.0F;
				
				/* Move -Left */
				moveRight = moveClose;
				
				/* Move +Up */
				moveUp = -moveClose -0.25F * closeCap;
			}
		}
		else
		{
			/* (READY) LOWEST | 0.0F -> 0.2F */
			moveClose = MathHelper.sin(f*PI*5.0F) * 0.2F;
			
			moveUp = -f * closeCap * 2.5F;
		}
		
		this.animationStabGlStateManager(hand, moveRight, moveUp, moveClose, rotateUp, rotateCounterClockwise, rotateLeft);
	}
	
	private void animationStabCameraMainhand()
	{
		/* Reduce momentum based off attack speed */
		float f = ClientProxy.EHC_INSTANCE.betterCombatMainhand.getSwingTimerCap() / 12.0F;
		
		/* Camera */
		float rotation = MathHelper.sin(-0.4F + this.mainhandEnergy * 2.127323F * PI);
		ClientProxy.EHC_INSTANCE.mc.player.cameraPitch += rotation*ConfigurationHandler.cameraPitchSwing * f;
		ClientProxy.EHC_INSTANCE.mc.player.rotationYaw -= rotation*ConfigurationHandler.rotationYawSwing * f;
	}
	
	private void animationStabOffhand() // XXXSTAB
	{
		final float f = this.offhandEnergy;
		final BetterCombatHand hand = ClientProxy.EHC_INSTANCE.betterCombatOffhand;
		
		float moveRight = 0.0F; /* +right */
		float moveUp = 0.0F; /* +up */
		float moveClose = 0.0F; /* +zoom */
		
		float rotateUp = 0.0F; /* +up */
		float rotateCounterClockwise = 0.0F; /* +counter-clockwise */
		float rotateLeft = 0.0F; /* +left */
		
	    float closeCap = (0.6F - this.tooCloseTimer);
		
		rotateUp = -clamp(f * 240.0F, 60.0F);
		
		rotateCounterClockwise = clamp(f * 125.0F, 50.0F) - f * 10.0F;
		rotateLeft = clamp(f * 75.0F, 30.0F) - f * 10.0F;
		
		if ( f > 0.2F )
		{
			if ( f > 0.4F )
			{
				if ( f > 0.8F )
				{
					/* (RETURN) HIGHEST | 0.8F -> 1 */
					/* ======= */
					float energy = (f - 0.8F);
					
					/* Move +Close */
					moveClose = -closeCap + energy * 2.5F;
					
					/* Move +Right */
					moveRight = moveClose;
					
					/* Move -Down */
					moveUp = -moveClose;
					
					energy *= energy;
					rotateUp += clampMultiplier(energy, 25.0F, 70.0F);
					rotateCounterClockwise -= clampMultiplier(energy, 25.0F, 30.0F);
					rotateLeft -= clampMultiplier(energy, 25.0F, 10.0F);
				}
				else
				{
					/* (HOLD) HIGH | 0.4F -> 0.8F */
					/* ==== */

					/* Stay -Away */
					moveClose = -closeCap * (f * 0.25F + 0.9F);
					
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
				moveClose = (0.2F - f) * closeCap * 5.0F;
				
				/* Move -Left */
				moveRight = moveClose;
				
				/* Move +Up */
				moveUp = -moveClose -0.25F * closeCap;
			}
		}
		else
		{
			/* (READY) LOWEST | 0.0F -> 0.2F */
			moveClose = MathHelper.sin(f*PI*5.0F) * 0.2F;
			
			moveUp = -f * closeCap * 2.5F;
		}
		
		this.animationStabGlStateManager(hand, -moveRight, moveUp, moveClose, rotateUp, -rotateCounterClockwise, -rotateLeft);
	}
	
	private void animationStabGlStateManager(BetterCombatHand hand, float moveRight, float moveUp, float moveClose, float rotateUp, float rotateCounterClockwise, float rotateLeft)
	{
		GlStateManager.translate(
       	/* X */ moveRight * hand.moveRightVariance,
       	/* Y */ 1.3F * moveUp * hand.moveUpVariance,
       	/* Z */ moveClose * hand.moveCloseVariance);
       	
    	GlStateManager.rotate(rotateUp * hand.rotateUpVariance, 1.0F, 0.0F, 0.0F);
    	GlStateManager.rotate(rotateCounterClockwise * hand.rotateCounterClockwiseVariance, 0.0F, 1.0F, 0.0F);
    	GlStateManager.rotate(rotateLeft * hand.rotateLeftVariance, 0.0F, 0.0F, 1.0F);
	}
	
	private void animationStabCameraOffhand()
	{
		/* Reduce momentum based off attack speed */
		float f = ClientProxy.EHC_INSTANCE.betterCombatOffhand.getSwingTimerCap() / 12.0F;
		
		/* Camera */
		float rotation = MathHelper.sin(-0.4F + this.offhandEnergy * 2.127323F * PI);
		ClientProxy.EHC_INSTANCE.mc.player.cameraPitch -= rotation*ConfigurationHandler.cameraPitchSwing * f;
		ClientProxy.EHC_INSTANCE.mc.player.rotationYaw += rotation*ConfigurationHandler.rotationYawSwing * f;
	}
	
    /* ====================================================================================================================================================== */
    /*																		SHIELD 																			  */
    /* ====================================================================================================================================================== */
	
	public void customShieldRender( RenderSpecificHandEvent event )
	{
		event.setCanceled(true);
		event.setResult(Result.DENY);
		if ( this.isOffhandAttacking() )
		{
			this.offhandEnergy = 1.0F + (event.getPartialTicks() - ClientProxy.EHC_INSTANCE.betterCombatOffhand.getSwingTimer()) * ClientProxy.EHC_INSTANCE.betterCombatOffhand.getSwingTimerIncrement();
		}
		else
		{
			this.offhandEnergy = 0.0F;
		}
		GlStateManager.pushMatrix();
        this.positionOffShield();
		this.positionBreathingShield();
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
        	// this.blockingTimer = 10; // XXX
        	
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
    		GlStateManager.translate(this.blockingTimer*0.06F, this.blockingTimer*0.008F, this.blockingTimer*0.04F); // XXX 0.04 0.06
    	}
        
        /* Position Shield */
        
        /* Counter-clockwise 30 */
        GlStateManager.rotate(210.0F, 0.0F, 1.0F, 0.0F);
		
		/* Position 2-D shields */
		if ( ConfigurationHandler.isShield2D(Helpers.getString(ClientProxy.EHC_INSTANCE.itemStackOffhand)))
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
    	float rotation = MathHelper.cos( 0.5F + (this.offhandEnergy * 2.0F) * PI);
		ClientProxy.EHC_INSTANCE.mc.player.cameraPitch -= rotation*ConfigurationHandler.cameraPitchSwing;
		ClientProxy.EHC_INSTANCE.mc.player.rotationYaw += rotation*ConfigurationHandler.rotationYawSwing;
	}
	
    /* ====================================================================================================================================================== */
    /*																		RENDER  																			  */
    /* ====================================================================================================================================================== */
	
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
    
    public void resetEquippedProgressMainhand()
    {
    	this.equippedProgressMainhand = -1.0F;
    }
    
    /* ====================================================================================================================================================== */
    /*																		OFF  																			  */
    /* ====================================================================================================================================================== */
    

    
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
    
    public void resetEquippedProgressOffhand()
    {
    	this.equippedProgressOffhand = -1.0F;
    }
    
    protected float cappedSpeed( float speed, float f, float cap )
    {
    	speed *= f;
    	
    	if ( speed >= cap )
    	{
    		return cap;
    	}
    	else
    	{
    		return speed;
    	}
    }
    
    protected float cappedSpeed( float speed, float f )
    {
    	speed *= f;
    	
    	if ( speed >= f )
    	{
    		return f;
    	}
    	else
    	{
    		return speed;
    	}
    }
    
//    private boolean blockingTimerActive()
//    {
//		return this.blockingTimer >= 0;
//	}

	public boolean isMainhandAttacking()
    {
    	return ClientProxy.EHC_INSTANCE.betterCombatMainhand.getSwingTimer() > 0;
    }
    
    public boolean isOffhandAttacking()
    {
    	return ClientProxy.EHC_INSTANCE.betterCombatOffhand.getSwingTimer() > 0;
    }
    
    public boolean isBlocking()
    {
        return this.blockingTimer > 0;
    }
	
    /* ====================================================================================================================================================== */
    /*																	RENDER MAINHAND  																	  */
    /* ====================================================================================================================================================== */

    /* ====================================================================================================================================================== */
    /*																		BREATHING  																		  */
    /* ====================================================================================================================================================== */

    private void positionBreathing()
	{
    	GlStateManager.translate(0.0F, MathHelper.sin(this.breatheTicks) * ConfigurationHandler.breathingAnimationIntensity, this.tooCloseTimer*0.6F);
	}
    
    /* ====================================================================================================================================================== */
    /*																	(+) CROSSHAIR GUI	  																  */
    /* ====================================================================================================================================================== */
    
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
    		GameSettings gamesettings = ClientProxy.EHC_INSTANCE.mc.gameSettings;
    		
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
    			if ( !ClientProxy.EHC_INSTANCE.itemStackOffhand.isEmpty() ) // XXX MAINHAND can only go with an OFFHAND?
    			{
        			if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.getWeaponProperty() == WeaponProperty.TWOHAND || ClientProxy.EHC_INSTANCE.betterCombatOffhand.getWeaponProperty() == WeaponProperty.TWOHAND || ClientProxy.EHC_INSTANCE.betterCombatOffhand.getWeaponProperty() == WeaponProperty.MAINHAND )
        			{
        				this.drawTexturedModalRect((scaledRes.getScaledWidth()-236)/2, scaledRes.getScaledHeight() - 19, 16, 0, 16, 16);
        			}
    			}

    			/* Show debug */
    			if ( gamesettings.showDebugInfo && !gamesettings.hideGUI && !ClientProxy.EHC_INSTANCE.mc.player.hasReducedDebug() && !gamesettings.reducedDebugInfo )
    			{
    				GlStateManager.pushMatrix();
    				GlStateManager.translate(scaledRes.getScaledWidth() / 2.0F, scaledRes.getScaledHeight() / 2.0F, this.zLevel);
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
    				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    				GlStateManager.enableAlpha();
    				drawTexturedModalRect(scaledRes.getScaledWidth() / 2 - 7, scaledRes.getScaledHeight() / 2 - 7, 0, 0, 16, 16);

    				/* ( ) Show attack indicator */
    				if ( ClientProxy.EHC_INSTANCE.mc.gameSettings.attackIndicator == 1 )
    				{
    					/* (+) Dual-weilding */
    					if ( ClientProxy.EHC_INSTANCE.betterCombatOffhand.hasCustomWeapon() )
    					{
        					this.showMainhandCrosshair(scaledRes);
        					this.showOffhandCrosshair(scaledRes);
    					}
    					/* (+) Shield-weilding */
    					else if ( ConfigurationHandler.showShieldCooldownCrosshair && ClientProxy.EHC_INSTANCE.itemStackOffhand.getItem() instanceof ItemShield )
    					{
        					this.showMainhandCrosshair(scaledRes);
        					this.showShieldCrosshair(scaledRes); // XXX
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
    	}

		private void showDefaultCrosshair( ScaledResolution scaledRes )
		{
			if ( ClientProxy.EHC_INSTANCE.mainhandCooldown > 0 )
			{
				int i = scaledRes.getScaledHeight() / 2 - 7;
				int j = scaledRes.getScaledWidth() / 2 - 7;
				int k = (int) (ClientProxy.EHC_INSTANCE.getMainhandCooledAttackStrength() * 18.0F);
				
				drawTexturedModalRect(j - 1, i + 12, 68, 94, 17, 8);
				drawTexturedModalRect(j - 1, i + 12, 68, 102, k, 8);
			}			
		}

		private void showMainhandCrosshair( ScaledResolution scaledRes )
		{
			if ( ClientProxy.EHC_INSTANCE.mainhandCooldown > 0 )
			{
				int i = scaledRes.getScaledHeight() / 2 - 7;
				int j = scaledRes.getScaledWidth() / 2 - 7;
				int k = (int) (ClientProxy.EHC_INSTANCE.getMainhandCooledAttackStrength() * 18.0F);
				
				drawTexturedModalRect(j + 15, i, 51, 94, 8, 17);
				drawTexturedModalRect(j + 15, i + 17 - k, 59, 111 - k, 8, k);
			}			
		}
		
		private void showOffhandCrosshair( ScaledResolution scaledRes )
		{
			if ( ClientProxy.EHC_INSTANCE.offhandCooldown > 0 )
			{
				int i = scaledRes.getScaledHeight() / 2 - 7;
				int j = scaledRes.getScaledWidth() / 2 - 7;
				int k = (int) (ClientProxy.EHC_INSTANCE.getOffhandCooledAttackStrength() * 18.0F);
				drawTexturedModalRect(j - 8, i, 35, 94, 8, 17);
				drawTexturedModalRect(j - 8, i + 17 - k, 43, 111 - k, 8, k);
			}			
		}
		
		private void showShieldCrosshair( ScaledResolution scaledRes )
		{
			float cooldown = 1.0F - ClientProxy.EHC_INSTANCE.mc.player.getCooldownTracker().getCooldown(ClientProxy.EHC_INSTANCE.itemStackOffhand.getItem(), 0.0F);

			if ( cooldown < 1.0F )
			{
				int i = scaledRes.getScaledHeight() / 2 - 7;
				int j = scaledRes.getScaledWidth() / 2 - 7;
				int k = (int) (cooldown * 18.0F);
				drawTexturedModalRect(j - 8, i, 35, 94, 8, 17);
				drawTexturedModalRect(j - 8, i + 17 - k, 43, 111 - k, 8, k);
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
}