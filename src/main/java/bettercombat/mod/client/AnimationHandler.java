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
    	this.mainhandSprintingTimer = 0.0F;
    	this.offhandSprintingTimer = 0.0F;
    	
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
	
	public float mainhandSprintingTimer;
	public float offhandSprintingTimer;

	public boolean tooClose;
	/* Capped at 0.4 */
	public double tooCloseAmount;
	/* 0.0 (not close) to 0.4 (close) */
	public float tooCloseTimer;
	/*
	 * How long the player has been blocking for, up to 10 frames (3.33 ticks)
	 * blockingTimer is -1 if there is no shield in the offhandA
	 */
	public int blockingTimer;
	
	/* float version of PI */
	public static final float PI = (float)Math.PI;
	
//	float xx,yy,zz;
	
    /* ======================================================================================================================================== */
    /* ======================================================================================================================================== */
    /*																CUSTOM RENDER 																*/
    /* ======================================================================================================================================== */
    /* ======================================================================================================================================== */

	@SubscribeEvent
	public void disableVanillaHandRender( RenderSpecificHandEvent event ) // -90, 120, -36 === -110, 116, -36
	{
//		xx = (float)ClientProxy.EHC_INSTANCE.mc.player.posX;
//		yy = (float)ClientProxy.EHC_INSTANCE.mc.player.posY - 50;
//		zz = (float)ClientProxy.EHC_INSTANCE.mc.player.posZ;
				
		/* ItemRenderer */		
		if ( event.getHand() == EnumHand.MAIN_HAND )
        {
			if ( ClientProxy.EHC_INSTANCE.mc.player.isSprinting() && this.equippedProgressMainhand != -1.0F && !this.isMainhandAttacking() )
			{
				if ( this.mainhandSprintingTimer < 30 )
				{
					this.mainhandSprintingTimer += 3;
				}
			}
			else if ( this.mainhandSprintingTimer > 0 )
			{
				this.mainhandSprintingTimer -= 3;
				
				if ( this.mainhandSprintingTimer > 0 && this.isMainhandAttacking() )
				{
					this.mainhandSprintingTimer -= 3;
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
				if ( this.offhandSprintingTimer < 30 )
				{
					this.offhandSprintingTimer += 3;
				}
			}
			else if ( this.offhandSprintingTimer > 0 )
			{
				this.offhandSprintingTimer -= 3;
				
				if ( this.offhandSprintingTimer > 0 && this.isOffhandAttacking() )
				{
					this.offhandSprintingTimer -= 3;
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
    	    	if ( Helpers.isHandActive(ClientProxy.EHC_INSTANCE.mc.player, EnumHand.OFF_HAND) )
    			{
					/* Block animation takes 10 frames (3.33 ticks) */
					if ( this.blockingTimer < 10 )
					{
						this.blockingTimer++;
					}
        		}
				else if ( this.blockingTimer > 0 )
    			{
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
    	GlStateManager.translate(0.0F, MathHelper.sin(this.breatheTicks) * ConfigurationHandler.breathingAnimationIntensity, -this.tooCloseTimer);
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
			
			if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.isMining() )
    		{
				Item tool = ClientProxy.EHC_INSTANCE.itemStackMainhand.getItem();
				
				if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.getAnimation().equals(Animation.STAB) )
				{
	        		this.animationStabMainhand();
				}
				else if ( tool instanceof ItemAxe )
				{
					this.animationSweepMainhand();
					this.resetEquippedProgressMainhand();
				}
				else if ( tool instanceof ItemSpade )
				{
					this.animationStabMainhand();
					this.resetEquippedProgressMainhand();
				}
				else /* Sword, Pickaxe, Axe */
				{
					this.animationChopMainhand();
					this.resetEquippedProgressMainhand();
				}
			}
			else switch ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.getAnimation() )
	        {
	        	case SWEEP:
	        	{
        			this.animationSweepMainhand();
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
        
        /* If the weapon is a spear, rotate it accordingly */
    	if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.getAnimation().equals(Animation.STAB) )
        {
        	GlStateManager.rotate(-34.0F-this.mainhandSprintingTimer,1.0F,0.0F,0.0F); /* Chopping rotation */
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

        this.positionOffhand();
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
	
	private void positionOffhand()
	{
    	/* Position the weapon in default position */
		GlStateManager.translate(-0.725F, -0.6F, -1.0F);
        
        /* If the weapon is a spear, rotate it accordingly */
    	if ( ClientProxy.EHC_INSTANCE.betterCombatOffhand.getAnimation().equals(Animation.STAB) )
        {
        	GlStateManager.rotate(-34.0F-this.offhandSprintingTimer,1.0F,0.0F,0.0F); /* Chopping rotation */
        	GlStateManager.translate(0.0F, -this.tooCloseTimer, 0.0F);
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
		float rotateLeft = clamp(this.mainhandEnergy*100.0F,20.0F); /* +left */

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
				/* (HOLD) HIGH | 0.25 -> 0.65 */
				/* ==== */
				
				//moveUp = -0.2F;
				
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
			/* (SWING) LOW | 0.0 -> 0.25 */
			/* === */
			float energy = 1.0F - MathHelper.cos(this.mainhandEnergy*PI*2.5F);
			
			//moveUp = energy * -0.2F;
			
			moveRight = energy * -0.7F;
			
			moveClose = energy * closeCap;
			
			
			rotateUp = energy * -95.0F;
		}

		GlStateManager.translate(
       	/* X */ moveRight,
       	/* Y */ moveUp,
       	/* Z */ moveClose);
       	
		/* Chop */
    	GlStateManager.rotate(rotateUp, 1.0F, 0.0F, 0.0F);
    	
    	GlStateManager.rotate(rotateCounterClockwise, 0.0F, 1.0F, 0.0F);

    	/* Swivel back and forth */
    	GlStateManager.rotate(rotateLeft, 0.0F, 0.0F, 1.0F);
	}
	
	private void animationChopCameraMainhand()
	{
		/* Reduce momentum based off attack speed */
		float f = 13.0F / ClientProxy.EHC_INSTANCE.betterCombatMainhand.getSwingTimerCap();
		
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
		float rotateLeft = clamp(this.offhandEnergy*100.0F,20.0F); /* +left */
		
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
				
				//moveUp = -0.2F;
				
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
			
			//moveUp = energy * -0.2F;
			
			moveRight = energy * -0.7F;
			
			moveClose = energy * closeCap;
			
			
			rotateUp = energy * -95.0F;
		}

		GlStateManager.translate(
       	/* X */ -moveRight,
       	/* Y */ moveUp,
       	/* Z */ moveClose);
       	
		/* Chop */
    	GlStateManager.rotate(rotateUp, 1.0F, 0.0F, 0.0F);
    	
    	GlStateManager.rotate(-rotateCounterClockwise, 0.0F, 1.0F, 0.0F);

    	/* Swivel back and forth */
    	GlStateManager.rotate(-rotateLeft, 0.0F, 0.0F, 1.0F);
	}
	
	private void animationChopCameraOffhand()
	{
		/* Reduce momentum based off attack speed */
		float f = 13.0F / ClientProxy.EHC_INSTANCE.betterCombatOffhand.getSwingTimerCap();
		
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
	    
	    float closeCap = 0.4F - (float)this.tooCloseTimer;
	    
	    rotateUp = -clampMultiplier(this.mainhandEnergy, 6.0F, 140.0F + closeCap * 40.0F); /* Sweep = Up */
	    rotateCounterClockwise = clampMultiplier(this.mainhandEnergy, 12.0F, 150.0F) - clampMultiplier(this.mainhandEnergy, 3.0F, 90.0F) - this.mainhandEnergy * 15.0F; /* Sweep = Left and To */
	    rotateLeft = clampMultiplier(this.mainhandEnergy, 6.0F, 85.0F); /* Sweep = Twist Clockwise -- * */

	    moveRight = clampMultiplier(this.mainhandEnergy, 12.0F, 3.5F) + 0.5F;
	    
		moveUp = clamp(this.mainhandEnergy*10.0F, 0.47F);
		moveClose = -clamp(this.mainhandEnergy*10.0F, closeCap);
	    		
	    if ( this.mainhandEnergy > 0.6F )
	    {
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
	    	moveRight -= clamp(MathHelper.sin(this.mainhandEnergy * PI) * 5.5F, 4.5F + closeCap);
	    }
		
		GlStateManager.translate(
       	/* X */ moveRight,
       	/* Y */ moveUp,
       	/* Z */ moveClose);
       	
    	GlStateManager.rotate(rotateUp, 1.0F, 0.0F, 0.0F);
    	GlStateManager.rotate(rotateCounterClockwise, 0.0F, 1.0F, 0.0F);
    	GlStateManager.rotate(rotateLeft, 0.0F, 0.0F, 1.0F);
	}

	private void animationSweepCameraMainhand()
	{
		float f = 13.0F / ClientProxy.EHC_INSTANCE.betterCombatMainhand.getSwingTimerCap();

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
	    
	    float closeCap = 0.4F - (float)this.tooCloseTimer;
	    
	    rotateUp = -clampMultiplier(this.offhandEnergy, 6.0F, 140.0F + closeCap * 40.0F); /* Sweep = Up */
	    rotateCounterClockwise = clampMultiplier(this.offhandEnergy, 12.0F, 150.0F) - clampMultiplier(this.offhandEnergy, 3.0F, 90.0F) - this.offhandEnergy * 15.0F; /* Sweep = Left and To */
	    rotateLeft = clampMultiplier(this.offhandEnergy, 6.0F, 85.0F); /* Sweep = Twist Clockwise -- * */

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
       	/* X */ -moveRight,
       	/* Y */ moveUp,
       	/* Z */ moveClose);
       	
    	GlStateManager.rotate(rotateUp, 1.0F, 0.0F, 0.0F);
    	GlStateManager.rotate(-rotateCounterClockwise, 0.0F, 1.0F, 0.0F);
    	GlStateManager.rotate(-rotateLeft, 0.0F, 0.0F, 1.0F);
	}

	private void animationSweepCameraOffhand()
	{
		float f = 13.0F / ClientProxy.EHC_INSTANCE.betterCombatOffhand.getSwingTimerCap();

		/* Camera */
    	float rotation = MathHelper.cos(0.5F + this.offhandEnergy * 2.5F * PI);
		ClientProxy.EHC_INSTANCE.mc.player.cameraPitch -= rotation * ConfigurationHandler.cameraPitchSwing * f;
		ClientProxy.EHC_INSTANCE.mc.player.rotationYaw -= rotation * ConfigurationHandler.rotationYawSwing * f;
	}
	
    /* ---------------------------------------------------------------------------------------------------------------------------------------- */
	/*																	STAB ANIMATION															*/
    /* ---------------------------------------------------------------------------------------------------------------------------------------- */
	
	private void animationStabMainhand()
	{
		float moveRight = 0.0F; /* +right */
		float moveUp = 0.0F; /* +up */
		float moveClose = 0.0F; /* +zoom */
		
		float rotateUp = 0.0F; /* +up */
		float rotateCounterClockwise = 0.0F; /* +counter-clockwise */
		float rotateLeft = 0.0F; /* +left */
		
	    float closeCap = (0.4F - (float)this.tooCloseTimer) * 1.25F;
		
		rotateUp = -clamp(this.mainhandEnergy * 300.0F, 60.0F);
		
		rotateCounterClockwise = clamp(this.mainhandEnergy * 88.0F, 40.0F) - this.mainhandEnergy * 10.0F;

		rotateLeft = rotateCounterClockwise - clamp(this.mainhandEnergy * 25.0F, 10.0F);
		
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
					moveClose = -clamp(closeCap, 0.5F) + energy*2.0F;
					
					/* Move +Right */
					moveRight = -0.5F + energy*2.5F;
					
					/* Move -Down */
					moveUp = 0.5F * closeCap*2.3F-energy*2.3F;
					
					//if ( this.mainhandEnergy > 0.9F )
					{
						energy *= energy;
						
						rotateUp += clampMultiplier(energy, 25.0F, 60.0F);
						rotateCounterClockwise -= clampMultiplier(energy, 25.0F, 30.0F);
						rotateLeft -= clampMultiplier(energy, 25.0F, 10.0F);
					}
				}
				else
				{
					/* (HOLD) HIGH | 0.4F -> 0.8F */
					/* ==== */

					/* Stay -Away */
					moveClose = -clamp(closeCap, 0.4F);
					
					/* Stay -Left */
					moveRight = -0.5F;
					
					/* Stay +Up */
					moveUp = 0.5F * closeCap*2.3F;
				}
			}
			else
			{
				/* (THRUST) LOW | 0.2F -> 0.4F */
				/* === */
				
				/* Move -Away */
				moveClose = -clamp((this.mainhandEnergy - 0.2F) * 2.0F, closeCap);
				
				/* Move -Left */
				moveRight = (this.mainhandEnergy - 0.2F) * -2.5F;
				
				/* Move +Up */
				moveUp = -moveRight * closeCap*2.3F;
			}
		}
		/* (READY) LOWEST | 0.0F -> 0.2F */
		
		GlStateManager.translate(
       	/* X */ moveRight,
       	/* Y */ moveUp,
       	/* Z */ moveClose);
       	
    	GlStateManager.rotate(rotateUp, 1.0F, 0.0F, 0.0F);
    	GlStateManager.rotate(rotateCounterClockwise, 0.0F, 1.0F, 0.0F);
    	GlStateManager.rotate(rotateLeft, 0.0F, 0.0F, 1.0F);
	}
	
	private void animationStabCameraMainhand()
	{
		/* Reduce momentum based off attack speed */
		float f = 13.0F / ClientProxy.EHC_INSTANCE.betterCombatMainhand.getSwingTimerCap();
		
		/* Camera */
		float rotation = MathHelper.sin(-0.4F + this.mainhandEnergy * 2.127323F * PI);
		ClientProxy.EHC_INSTANCE.mc.player.cameraPitch += rotation*ConfigurationHandler.cameraPitchSwing * f;
		ClientProxy.EHC_INSTANCE.mc.player.rotationYaw -= rotation*ConfigurationHandler.rotationYawSwing * f;
	}
	
	private void animationStabOffhand()
	{
		float moveRight = 0.0F; /* +right */
		float moveUp = 0.0F; /* +up */
		float moveClose = 0.0F; /* +zoom */
		
		float rotateUp = 0.0F; /* +up */
		float rotateCounterClockwise = 0.0F; /* +counter-clockwise */
		float rotateLeft = 0.0F; /* +left */
		
	    float closeCap = (0.4F - (float)this.tooCloseTimer) * 1.25F;
		
		rotateUp = -clamp(this.offhandEnergy * 300.0F, 60.0F);
		
		rotateCounterClockwise = clamp(this.offhandEnergy * 88.0F, 40.0F) - this.offhandEnergy * 10.0F;

		rotateLeft = rotateCounterClockwise - clamp(this.offhandEnergy * 25.0F, 10.0F);
		
		if ( this.offhandEnergy > 0.2F )
		{
			if ( this.offhandEnergy > 0.4F )
			{
				if ( this.offhandEnergy > 0.8F )
				{
					/* (RETURN) HIGHEST | 0.8F -> 1 */
					/* ======= */
					float energy = (this.offhandEnergy - 0.8F);
					
					/* Move +Close */
					moveClose = -clamp(closeCap, 0.5F) + energy*2.0F;
					
					/* Move +Right */
					moveRight = -0.5F + energy*2.5F;
					
					/* Move -Down */
					moveUp = 0.5F * closeCap*2.3F-energy*2.3F;
					
					//if ( this.offhandEnergy > 0.9F )
					{
						energy *= energy;
						
						rotateUp += clampMultiplier(energy, 25.0F, 60.0F);
						rotateCounterClockwise -= clampMultiplier(energy, 25.0F, 30.0F);
						rotateLeft -= clampMultiplier(energy, 25.0F, 10.0F);
					}
				}
				else
				{
					/* (HOLD) HIGH | 0.4F -> 0.8F */
					/* ==== */

					/* Stay -Away */
					moveClose = -closeCap;
					
					/* Stay -Left */
					moveRight = -0.5F;
					
					/* Stay +Up */
					moveUp = 0.5F * closeCap*2.3F;
				}
			}
			else
			{
				/* (THRUST) LOW | 0.2F -> 0.4F */
				/* === */
				
				/* Move -Away */
				moveClose = -clamp((this.offhandEnergy - 0.2F) * 2.0F, closeCap);
				
				/* Move -Left */
				moveRight = (this.offhandEnergy - 0.2F) * -2.5F;
				
				/* Move +Up */
				moveUp = -moveRight * closeCap*2.3F;
			}
		}
		/* (READY) LOWEST | 0.0F -> 0.2F */
		
		GlStateManager.translate(
       	/* X */ -moveRight,
       	/* Y */ moveUp,
       	/* Z */ moveClose);
       	
    	GlStateManager.rotate(rotateUp, 1.0F, 0.0F, 0.0F);
    	GlStateManager.rotate(-rotateCounterClockwise, 0.0F, 1.0F, 0.0F);
    	GlStateManager.rotate(-rotateLeft, 0.0F, 0.0F, 1.0F);
	}
	
	private void animationStabCameraOffhand()
	{
		/* Reduce momentum based off attack speed */
		float f = 13.0F / ClientProxy.EHC_INSTANCE.betterCombatOffhand.getSwingTimerCap();
		
		/* Camera */
		float rotation = MathHelper.sin(-0.4F + this.offhandEnergy * 2.127323F * PI);
		ClientProxy.EHC_INSTANCE.mc.player.cameraPitch -= rotation*ConfigurationHandler.cameraPitchSwing * f;
		ClientProxy.EHC_INSTANCE.mc.player.rotationYaw -= rotation*ConfigurationHandler.rotationYawSwing * f;
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
    	GlStateManager.translate(0.0F, MathHelper.sin(this.breatheTicks) * ConfigurationHandler.breathingAnimationIntensity, this.tooCloseTimer);
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
				boolean cancelled = event.isCanceled();
				event.setCanceled(true);
				if ( !cancelled )
				{
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

	public static float clampMultiplier(float base, float multiplier, float cap)
	{
		float f = base * multiplier * cap;
		
		if ( f > cap )
		{
			return cap;
		}
		
		return f;
	}

    /* ---------------------------------------------------------------------------------------------------------------------------------------- */
	/*																MINING ANIMATION																*/
    /* ---------------------------------------------------------------------------------------------------------------------------------------- */
	

//	/* ================== SWEEP MAINHAND ================== */
//	private void aanimationSweepMainhand( float partialTicks )
//	{
//		GlStateManager.translate( this.mainhandAnimationTicks > 0.0F ? (0.512F * 5.25F) + this.mainhandAnimationTicks * ClientProxy.EHC_INSTANCE.betterCombatMainhand.xTranslate : this.mainhandEnergy * 5.25F,
//		// this.mainhandEnergy <= 0.9F ? this.mainhandAnimationTicks * ClientProxy.EHC_INSTANCE.betterCombatMainhand.yTranslate : (0.9F - this.mainhandEnergy) * 2.0F + this.mainhandAnimationTicks * ClientProxy.EHC_INSTANCE.betterCombatMainhand.yTranslate,
//		// this.mainhandAnimationTicks < 0.0F ? this.mainhandAnimationTicks * -0.2F : 0.0F,
////		this.mainhandEnergy < 0.2F ? this.mainhandEnergy : 0.25F - this.mainhandEnergy * 0.25F,
//		this.mainhandEnergy < 0.512F ? this.mainhandEnergy : 0.6144 - this.mainhandEnergy * 0.2F,
//
//		Math.sin(PI * this.mainhandEnergy) * ClientProxy.EHC_INSTANCE.betterCombatMainhand.zTranslate );
//		GlStateManager.rotate(-cappedSpeed(this.mainhandEnergy*ClientProxy.EHC_INSTANCE.betterCombatMainhand.xRotate, 80.0F),1.0F,0.0F,0.0F); /* Point forward to enemy */
//		GlStateManager.rotate(cappedSpeed(this.mainhandEnergy*ClientProxy.EHC_INSTANCE.betterCombatMainhand.yRotate, 90.0F),0.0F,1.0F,0.0F); /* Twist */
//		GlStateManager.rotate( this.mainhandAnimationTicks > 0.0F ? -cappedSpeed(this.mainhandAnimationTicks*0.5F,40.0F) : this.mainhandAnimationTicks*80.0F,0.0F,0.0F,1.0F); /* Chop */
//
//		this.animationSweepCameraMainhand( partialTicks );
//	}
	
	/* ------------------ SWEEP MAINHAND ------------------ */
//	GlStateManager.translate
//	(
//   	0.0F,
//   	-(MathHelper.sin(PI * this.mainhandEnergy)+this.mainhandEnergy)*0.33F,
//   	/*																	   slight pull back 		  forward thrust, mAT starts at -0.5, stops at 0.0	 		  return to sender										*/
//   	( this.mainhandAnimationTicks <= 0.0F ? (this.mainhandEnergy <= 0.25F ? this.mainhandEnergy*1.6F : this.mainhandAnimationTicks*-5.0F-2.1F) : (MathHelper.clamp(this.mainhandAnimationTicks*2.4F - 2.6F, -2.1F, 1.0F)) )
//	);
//	
//	GlStateManager.rotate( -45.0F + (this.mainhandAnimationTicks <= 0.0F ? -cappedSpeed(this.mainhandEnergy*2.0F,50.0F) : -50.0F + this.mainhandAnimationTicks*10.0F),1.0F,0.0F,0.0F); /* Chopping rotation */
//	GlStateManager.rotate(MathHelper.sin(PI * this.mainhandEnergy)*20.0F,0.0F,1.0F,1.0F);
//	
//	this.animationStabCameraMainhand();
	
	/* ================== STAB MAINHAND ================== */
	
//	private void animationSwordStabMainhand()
//	{
//		float moveRight = 0.0F; /* +right */
//		float moveUp = 0.0F; /* +up */
//		float moveClose = 0.0F; /* +zoom */
//		
//		float rotateUp = 0.0F; /* +up */
//		float rotateCounterClockwise = 0.0F; /* +counter-clockwise */
//		float rotateLeft = 0.0F; /* +left */
//		
//	    float closeCap = 0.4F - (float)this.tooCloseTimer;
//		
//		float readyDuration = 0.3F; // 0.3
//		float travelDuration = 0.4F; // 0.4
//		float holdDuration = 0.9F; // 0.7
//		
//		rotateUp = -clamp(this.mainhandEnergy * 2200.0F, 110.0F);
//		
//		rotateCounterClockwise = clamp(this.mainhandEnergy * 1600.0F, 40.0F) - this.mainhandEnergy * 20.0F;
//				
//		rotateLeft = clamp(this.mainhandEnergy * 1600.0F, 40.0F) - this.mainhandEnergy * 6.0F;
//				
//		/* Move +Up (Quickly) */
//		moveUp = clamp(this.mainhandEnergy * 20.0F, closeCap);
//		
//		if ( this.mainhandEnergy > readyDuration )
//		{
//			if ( this.mainhandEnergy > travelDuration )
//			{
//				if ( this.mainhandEnergy > holdDuration )
//				{
//					/* HIGHEST | holdDuration -> 1 */
//					/* ======= */
//					
//					/* Stay -left, Move +Right */
//					moveRight = (travelDuration - readyDuration) * -8.15F + (this.mainhandEnergy - holdDuration) * 6.0F;
//
//					/* Stay -Away, Move +Close */
//					moveClose = (travelDuration - readyDuration) * -1.35F + (this.mainhandEnergy - holdDuration) * 8.15F;
//					
//					/* Move -Down */
//					moveUp -= (this.mainhandEnergy - holdDuration) * 4.0F;
//				}
//				else
//				{
//					/* HIGH | travelDuration -> holdDuration */
//					/* ==== */
//
//					/* Stay -Left */
//					moveRight = (travelDuration - readyDuration) * -8.15F;
//
//					/* Stay -Away */
//					moveClose = (travelDuration - readyDuration) * -1.35F;
//				}
//			}
//			else
//			{
//				/* LOW | readyDuration -> travelDuration */
//				/* === */
//				
//				/* Move -Left */
//				moveRight = (this.mainhandEnergy - readyDuration) * -8.15F;
//
//				/* Move -Away */
//				moveClose = this.mainhandEnergy * -1.35F;
//			}
//		}
//		else
//		{
//			/* LOWEST | 0 -> readyDuration */
//			/* ===== */
//
//			/* Move +Right, Move -Left */
//			moveRight = MathHelper.sin(this.mainhandEnergy * (1.0F/readyDuration) * PI) * 2.0F;
//
//			/* Move -Away */
//			moveClose = this.mainhandEnergy * -1.35F;
//		}
//		
//		GlStateManager.translate(
//       	/* X */ moveRight,
//       	/* Y */ moveUp,
//       	/* Z */ moveClose);
//       	
//    	GlStateManager.rotate(rotateUp, 1.0F, 0.0F, 0.0F);
//    	
//    	GlStateManager.rotate(rotateCounterClockwise, 0.0F, 1.0F, 0.0F);
//
//    	GlStateManager.rotate(rotateLeft, 0.0F, 0.0F, 1.0F);
//	}
	
}