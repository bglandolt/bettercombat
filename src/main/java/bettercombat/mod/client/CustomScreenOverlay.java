package bettercombat.mod.client;

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
	private final ResourceLocation SWEEP_150;
	private final ResourceLocation SWEEP_180;

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
	public void onRenderGameOverlay(RenderGameOverlayEvent event)
	{
		if ( event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS )
		{
			if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.getAnimation() == Animation.SWEEP )
			{
				if ( ClientProxy.EHC_INSTANCE.betterCombatMainhand.alternateAnimation )
				{
					if ( ClientProxy.AH_INSTANCE.mainhandEnergy > 0.6F )
					{
						if ( currentFrame < 16 )
						{
							this.renderScreenOverlay150();
							this.currentFrame++;
						}
					}
					else
					{
				        currentFrame = 0;
					}
				}
				else
				{
					if ( ClientProxy.AH_INSTANCE.mainhandEnergy > 0.2F )
					{
						if ( currentFrame < 16 )
						{
							this.renderScreenOverlay180();
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

	public void renderScreenOverlay150()
	{
		ScaledResolution scaledRes = new ScaledResolution(Minecraft.getMinecraft());
		
		float scaleX = 3.5F;
		float scaleY = 2.0F;
		
        int frameWidth = 64; /* width of a single frame */
        int frameHeight = 64; /* height of a single frame */

		int x = (int)(scaledRes.getScaledWidth() / (scaleX+1.0F)) - 128;
		int y = (int)(scaledRes.getScaledHeight() / (scaleY+1.0F)) - 64;
		
        this.mc.getTextureManager().bindTexture(SWEEP_150);

        int startX = (currentFrame % 4) * frameWidth;
        int startY = (int)(Math.ceil((currentFrame + 1) / 4.0D) - 1) * frameHeight;

        GlStateManager.pushMatrix(); // Save the current OpenGL state

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        // Render the current frame of the animation at a specific location
        GlStateManager.scale(scaleX, scaleY, 1.0F);
        // GlStateManager.rotate
        this.mc.ingameGUI.drawTexturedModalRect(x + currentFrame*scaleX, y + currentFrame*scaleY, startX, startY, frameWidth, frameHeight);
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        
        GlStateManager.popMatrix(); // Save the current OpenGL state

    }
	
	public void renderScreenOverlay180()
	{
		ScaledResolution scaledRes = new ScaledResolution(Minecraft.getMinecraft());
		
		float scaleX = 3.5F;
		float scaleY = 2.0F;
		
        int frameWidth = 64; /* width of a single frame */
        int frameHeight = 64; /* height of a single frame */

		int x = (int)(scaledRes.getScaledWidth() / (scaleX+1.0F));
		int y = (int)(scaledRes.getScaledHeight() / (scaleY+1.0F)) - 128;
		
        this.mc.getTextureManager().bindTexture(SWEEP_180);

        int startX = (currentFrame % 4) * frameWidth;
        int startY = (int)(Math.ceil((currentFrame + 1) / 4.0D) - 1) * frameHeight;

        GlStateManager.pushMatrix(); // Save the current OpenGL state

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        // Render the current frame of the animation at a specific location
        GlStateManager.scale(scaleX, scaleY, 1.0F);
        // GlStateManager.rotate
        this.mc.ingameGUI.drawTexturedModalRect(x - currentFrame*scaleX*2, y, startX, startY, frameWidth, frameHeight);
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        
        GlStateManager.popMatrix(); // Save the current OpenGL state

    }
}