package bettercombat.mod.client;

import bettercombat.mod.util.ConfigurationHandler;
import bettercombat.mod.util.ConfigurationHandler.Animation;
import bettercombat.mod.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CustomScreenOverlay
{
	private final Minecraft        mc;
	private final ResourceLocation MAINHAND_SWEEP_1; /* \ */
	private final ResourceLocation MAINHAND_SWEEP_2; /* - */
	
	private final ResourceLocation OFFHAND_SWEEP_1; /* / */
	private final ResourceLocation OFFHAND_SWEEP_2; /* - */
	
    private final int FRAME_SIZE = 64;
    private final int FRAMES = 4;
    
	private int                    currentFrameMainhand = 0;
	private int                    currentFrameOffhand = 0;

	public CustomScreenOverlay()
	{
		this.mc = Minecraft.getMinecraft();
		
		this.MAINHAND_SWEEP_1 = new ResourceLocation(Reference.MOD_ID, "textures/gui/mainhand_sweep_1.png");
		this.MAINHAND_SWEEP_2 = new ResourceLocation(Reference.MOD_ID, "textures/gui/mainhand_sweep_2.png");
		
		this.OFFHAND_SWEEP_1 = new ResourceLocation(Reference.MOD_ID, "textures/gui/offhand_sweep_1.png");
		this.OFFHAND_SWEEP_2 = new ResourceLocation(Reference.MOD_ID, "textures/gui/offhand_sweep_2.png");
	}
	
	@SubscribeEvent
	public void onRenderGameOverlay(RenderGameOverlayEvent.Post event)
	{
		/* Mainhand */
		if ( event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS && ConfigurationHandler.attackSweepOverlay )
		{
			if ( !ClientProxy.EHC_INSTANCE.betterCombatMainhand.isMining() )
			{
				if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.getAnimation() == Animation.SWEEP && AnimationHandler.isMainhandAttacking() )
				{
					if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.alternateAnimation )
					{
						if ( ClientProxy.AH_INSTANCE.mainhandEnergy > 0.625F )
						{
							if ( this.currentFrameMainhand < 16 )
							{
								this.renderScreenOverlayMainhandSweep2(event.getResolution());
								this.currentFrameMainhand++;
							}
						}
						else
						{
							this.currentFrameMainhand = 0;
						}
					}
					else
					{
						if ( ClientProxy.AH_INSTANCE.mainhandEnergy > 0.25F )
						{
							if ( this.currentFrameMainhand < 16 )
							{
								this.renderScreenOverlayMainhandSweep1(event.getResolution());
								this.currentFrameMainhand++;
							}
						}
						else
						{
					        this.currentFrameMainhand = 0;
						}
					}
				}
			}
			
			/* Offhand */
			if ( ClientProxy.EHC_INSTANCE.betterCombatOffhand.getAnimation() == Animation.SWEEP && AnimationHandler.isOffhandAttacking() )
			{
				if ( ClientProxy.EHC_INSTANCE.betterCombatOffhand.alternateAnimation )
				{
					if ( ClientProxy.AH_INSTANCE.offhandEnergy > 0.625F )
					{
						if ( this.currentFrameOffhand < 16 )
						{
							this.renderScreenOverlayOffhandSweep2(event.getResolution());
							this.currentFrameOffhand++;
						}
					}
					else
					{
						this.currentFrameOffhand = 0;
					}
				}
				else
				{
					if ( ClientProxy.AH_INSTANCE.offhandEnergy > 0.25F )
					{
						if ( this.currentFrameOffhand < 16 )
						{
							this.renderScreenOverlayOffhandSweep1(event.getResolution());
							this.currentFrameOffhand++;
						}
					}
					else
					{
				        this.currentFrameOffhand = 0;
					}
				}
			}
		}
	}
	
	/* ========================================================================================================================= */
	/*															Mainhand
	/* ========================================================================================================================= */

	/* - */
	public void renderScreenOverlayMainhandSweep1( ScaledResolution scaledRes )
	{
		float scaleX = scaledRes.getScaledHeight() / 128.0F;
	    float scaleY = scaledRes.getScaledHeight() / 256.0F;
	    
        int x = (scaledRes.getScaledWidth()+75)>>1;
		int y = (scaledRes.getScaledHeight()-50)>>1;
		
        this.mc.getTextureManager().bindTexture(MAINHAND_SWEEP_1);

        int startX = (this.currentFrameMainhand % 4) * FRAME_SIZE;
        int startY = (int)(Math.ceil((this.currentFrameMainhand + 1) / 4.0D) - 1) * FRAME_SIZE;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.translate(x - this.currentFrameMainhand * 4.0F, y - this.currentFrameMainhand * 0.5F, 0.0F);
        GlStateManager.scale(scaleX, scaleY, 1.0F);
        this.mc.ingameGUI.drawTexturedModalRect(0, 0, startX, startY, FRAME_SIZE, FRAME_SIZE);
        
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
	
	/* \ */
	public void renderScreenOverlayMainhandSweep2( ScaledResolution scaledRes )
	{
		float scaleX = scaledRes.getScaledHeight() / 128.0F;
	    float scaleY = scaledRes.getScaledHeight() / 256.0F;
        
		int x = (scaledRes.getScaledWidth()-150)>>1;
		int y = (scaledRes.getScaledHeight()-110)>>1;
		
        this.mc.getTextureManager().bindTexture(MAINHAND_SWEEP_2);

        int startX = (this.currentFrameMainhand % FRAMES) * FRAME_SIZE;
        int startY = (int)(Math.ceil((this.currentFrameMainhand + 1) / 4.0D) - 1) * FRAME_SIZE;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.translate(x + this.currentFrameMainhand * 4.0F, y + this.currentFrameMainhand * 0.5F, 0.0F);
        GlStateManager.scale(scaleX, scaleY, 1.0F);
        GlStateManager.rotate(20.0F, 0.0F, 0.0F, 1.0F);
        this.mc.ingameGUI.drawTexturedModalRect(0, 0, startX, startY, FRAME_SIZE, FRAME_SIZE);
        
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
	
	/* ========================================================================================================================= */
	/*															Offhand
	/* ========================================================================================================================= */
	
	/* - */
	public void renderScreenOverlayOffhandSweep1( ScaledResolution scaledRes )
	{
		float scaleX = scaledRes.getScaledHeight() / 128.0F;
	    float scaleY = scaledRes.getScaledHeight() / 256.0F;
	    
        int x = (scaledRes.getScaledWidth()-520)>>1;
		int y = (scaledRes.getScaledHeight()-55)>>1;
		
        this.mc.getTextureManager().bindTexture(OFFHAND_SWEEP_1);

        int startX = (this.currentFrameOffhand % 4) * FRAME_SIZE;
        int startY = (int)(Math.ceil((this.currentFrameOffhand + 1) / 4.0D) - 1) * FRAME_SIZE;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.translate(x + this.currentFrameOffhand * 4.0F, y - this.currentFrameOffhand * 0.5F, 0.0F);
        GlStateManager.scale(scaleX, scaleY, 1.0F);
        this.mc.ingameGUI.drawTexturedModalRect(0, 0, startX, startY, FRAME_SIZE, FRAME_SIZE);
        
        GlStateManager.disableBlend();        
        GlStateManager.popMatrix();
    }
	
	/* \ */
	public void renderScreenOverlayOffhandSweep2( ScaledResolution scaledRes )
	{
		float scaleX = scaledRes.getScaledHeight() / 128.0F;
	    float scaleY = scaledRes.getScaledHeight() / 256.0F;
        
		int x = (scaledRes.getScaledWidth()-275)>>1;
		int y = (scaledRes.getScaledHeight()-45)>>1;
		
        this.mc.getTextureManager().bindTexture(OFFHAND_SWEEP_2);

        int startX = (this.currentFrameOffhand % FRAMES) * FRAME_SIZE;
        int startY = (int)(Math.ceil((this.currentFrameOffhand + 1) / 4.0D) - 1) * FRAME_SIZE;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.translate(x - this.currentFrameOffhand * 4.0F, y + this.currentFrameOffhand * 0.5F, 0.0F);
        GlStateManager.scale(scaleX, scaleY, 1.0F);
        GlStateManager.rotate(-20.0F, 0.0F, 0.0F, 1.0F);
        this.mc.ingameGUI.drawTexturedModalRect(0, 0, startX, startY, FRAME_SIZE, FRAME_SIZE);
        
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}