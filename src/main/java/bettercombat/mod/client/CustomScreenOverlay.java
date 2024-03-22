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

public class CustomScreenOverlay
{
	private final Minecraft        mc;
	private final ResourceLocation SWEEP_150; /* \ */
	private final ResourceLocation SWEEP_180; /* - */

//	private final int              ANIMATION_FRAMES = 1;                                                                        // Number of frames in your animated texture
//	private final int              FRAME_DURATION   = 1;                                                                        // Duration for each frame (in ticks)
//
	private int                    currentFrame     = 0;
//	private int                    frameCounter     = 0;

	public CustomScreenOverlay()
	{
		this.mc = Minecraft.getMinecraft();
		this.SWEEP_150 = new ResourceLocation(Reference.MOD_ID + ":textures/gui/sweep_150.png");
		this.SWEEP_180 = new ResourceLocation(Reference.MOD_ID + ":textures/gui/sweep_180.png");
	}
	
	@SubscribeEvent
	public void onRenderGameOverlay(RenderGameOverlayEvent.Post event)
	{
		if ( event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS && ConfigurationHandler.attackSweepOverlay )
		{
			if ( !ClientProxy.EHC_INSTANCE.betterCombatMainhand.isMining() )
			{
				if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.getAnimation() == Animation.SWEEP )
				{
					if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.alternateAnimation )
					{
						if ( ClientProxy.AH_INSTANCE.mainhandEnergy > 0.6F )
						{
							if ( this.currentFrame < 16 )
							{
								//Helpers.message(this.mc.player, ""+this.currentFrame);
								this.renderScreenOverlay150(event.getResolution());
								this.currentFrame++;
							}
						}
						else
						{
							this.currentFrame = 0;
						}
					}
					else
					{
						if ( ClientProxy.AH_INSTANCE.mainhandEnergy > 0.25F )
						{
							if ( this.currentFrame < 16 )
							{
								this.renderScreenOverlay180(event.getResolution());
								this.currentFrame++;
							}
						}
						else
						{
					        this.currentFrame = 0;
						}
					}
				}
			}
		}
	}

	/* \ */
	public void renderScreenOverlay150( ScaledResolution scaledRes )
	{
		float scaleX = 3.5F;
		float scaleY = 2.0F;
		
        int frameWidth = 64; /* width of a single frame */
        int frameHeight = 64; /* height of a single frame */

//    	int xx = (int)ClientProxy.EHC_INSTANCE.mc.player.posX;
//    	int yy = (int)ClientProxy.EHC_INSTANCE.mc.player.posY - 50;
//    	int zz = (int)ClientProxy.EHC_INSTANCE.mc.player.posZ;
		
		int x = (scaledRes.getScaledWidth()-410/scaledRes.getScaleFactor())>>1;
		int y = (scaledRes.getScaledHeight()-60/scaledRes.getScaleFactor())>>1;
		
        this.mc.getTextureManager().bindTexture(SWEEP_150);

        int startX = (this.currentFrame % 4) * frameWidth;
        int startY = (int)(Math.ceil((this.currentFrame + 1) / 4.0D) - 1) * frameHeight;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + this.currentFrame * 4.0F, y + this.currentFrame * 0.5F, 0.0F);
        GlStateManager.scale(scaleX/scaledRes.getScaleFactor(), scaleY/scaledRes.getScaleFactor(), 1.0F);
        // x + this.currentFrame*scaleX*2, y + this.currentFrame*scaleY*2,
        this.mc.ingameGUI.drawTexturedModalRect(0, 0, startX, startY, frameWidth, frameHeight);
        GlStateManager.popMatrix();
        
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
	
	/* - */
	public void renderScreenOverlay180( ScaledResolution scaledRes )
	{
		float scaleX = 3.5F;
		float scaleY = 2.0F;
		
        int frameWidth = 64; /* width of a single frame */
        int frameHeight = 64; /* height of a single frame */

//    	int xx = (int)ClientProxy.EHC_INSTANCE.mc.player.posX;
//    	int yy = (int)ClientProxy.EHC_INSTANCE.mc.player.posY - 50;
//    	int zz = (int)ClientProxy.EHC_INSTANCE.mc.player.posZ;
		
        // System.out.println(scaledRes.getScaledWidth() + " " + scaledRes.getScaledHeight() + " " + scaledRes.getScaleFactor() + " " );
		
        int x = (scaledRes.getScaledWidth()+125/scaledRes.getScaleFactor())>>1;
		int y = (scaledRes.getScaledHeight()-50/scaledRes.getScaleFactor())>>1;
		
        this.mc.getTextureManager().bindTexture(SWEEP_180);

        int startX = (this.currentFrame % 4) * frameWidth;
        int startY = (int)(Math.ceil((this.currentFrame + 1) / 4.0D) - 1) * frameHeight;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(x - this.currentFrame * 4.0F, y - this.currentFrame * 0.5F, 0.0F);
        GlStateManager.scale(scaleX/scaledRes.getScaleFactor(), scaleY/scaledRes.getScaleFactor(), 1.0F);
        // x - this.currentFrame*scaleX*2, y - this.currentFrame*scaleY
        this.mc.ingameGUI.drawTexturedModalRect(0, 0, startX, startY, frameWidth, frameHeight);
        GlStateManager.popMatrix();
        
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        
    }
}