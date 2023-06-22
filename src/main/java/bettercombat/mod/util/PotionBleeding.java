package bettercombat.mod.util;

import bettercombat.mod.client.particle.BloodParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PotionBleeding extends Potion
{
	public ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID + ":textures/gui/bleeding.png");
	public static DamageSource DAMAGE_SOURCE = new DamageSource("bleeding").setDamageBypassesArmor();
	
	protected PotionBleeding()
	{
		super(false, 0xff6066);
		setRegistryName(Reference.MOD_ID, "bleeding");
		setPotionName(Reference.MOD_ID + ".effect.bleeding");
	}

	@SuppressWarnings( "deprecation" )
	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventoryEffect( int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc )
	{
		super.renderInventoryEffect(x, y, effect, mc);
		mc.renderEngine.bindTexture(TEXTURE);
		GlStateManager.enableBlend();
		Gui.drawModalRectWithCustomSizedTexture(x + 6, y + 7, 0, 0, 18, 18, 18, 18);
	}
	
	@SuppressWarnings( "deprecation" )
	@Override
	@SideOnly( Side.CLIENT )
	public void renderHUDEffect( int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha )
	{
		super.renderHUDEffect(x, y, effect, mc, alpha);
		mc.renderEngine.bindTexture(TEXTURE);
		GlStateManager.enableBlend();
		Gui.drawModalRectWithCustomSizedTexture(x + 3, y + 3, 0, 0, 18, 18, 18, 18);
	}
	
	@Override
	public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier)
    {
		if ( entityLivingBaseIn.ticksExisted % 2 == 0 && entityLivingBaseIn.getHealth() > 0.0F )
		{
			final double x;
			final double z;
			final float padding = entityLivingBaseIn.width*0.1F;
			
			if ( entityLivingBaseIn.getRNG().nextBoolean() )
			{
				x = entityLivingBaseIn.getRNG().nextGaussian()*((entityLivingBaseIn.width-padding)*0.5);
				z = entityLivingBaseIn.getRNG().nextBoolean() ? padding - entityLivingBaseIn.width*0.5 : entityLivingBaseIn.width*0.5 - padding;
			}
			else
			{
				x = entityLivingBaseIn.getRNG().nextBoolean() ? padding - entityLivingBaseIn.width*0.5 : entityLivingBaseIn.width*0.5 - padding;
				z = entityLivingBaseIn.getRNG().nextGaussian()*((entityLivingBaseIn.width-padding)*0.5);
			}
			
			Minecraft.getMinecraft().effectRenderer.addEffect(new BloodParticle(entityLivingBaseIn.world, entityLivingBaseIn.posX + x, entityLivingBaseIn.posY + padding + (entityLivingBaseIn.getRNG().nextFloat()*entityLivingBaseIn.height*0.7F), entityLivingBaseIn.posZ + z));
		
			if ( entityLivingBaseIn.ticksExisted % 20 == 0 )
			{
				entityLivingBaseIn.attackEntityFrom(DAMAGE_SOURCE, ConfigurationHandler.bleedingDamagePerTick*amplifier);
			}
		}
    }
	
	@Override
    public boolean isReady(int duration, int amplifier)
    {
        return true;
    }
}