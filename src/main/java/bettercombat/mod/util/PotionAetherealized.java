package bettercombat.mod.util;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PotionAetherealized extends Potion
{
	public ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID + ":textures/gui/aetherealized.png");

	protected PotionAetherealized()
	{
		super(false, 0x79f6f0);
		setRegistryName(Reference.MOD_ID, "aetherealized");
		setPotionName(Reference.MOD_ID + ".effect.aetherealized");
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
	
	public static void playAetherealizedEffect(Entity e, int amount)
	{
		if (ConfigurationHandler.aetherealizedDamageParticles && e.world instanceof WorldServer)
		{
			double h = e.height / 2.0;

			if (amount > 1)
			{
				((WorldServer) e.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, e.posX, e.posY + h, e.posZ + 1.5,
				amount, 0.0D, 0.0D, 0.01D, 0.0D);
				((WorldServer) e.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, e.posX + 0.75, e.posY + h,
				e.posZ + 1.28, amount, 0.0D, 0.0D, 0.01D, 0.0D);
				((WorldServer) e.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, e.posX + 1.28, e.posY + h,
				e.posZ + 0.75, amount, 0.0D, 0.0D, 0.01D, 0.0D);
				((WorldServer) e.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, e.posX + 1.4, e.posY + h,
				e.posZ + 1.4, amount, 0.0D, 0.0D, 0.0D, 0.0D);
				((WorldServer) e.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, e.posX + 1.5, e.posY + h, e.posZ,
				amount, 0.0D, 0.0D, 0.01D, 0.0D);
				((WorldServer) e.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, e.posX + 1.28, e.posY + h,
				e.posZ - 0.75, amount, 0.0D, 0.0D, 0.01D, 0.0D);
				((WorldServer) e.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, e.posX + 0.75, e.posY + h,
				e.posZ - 1.28, amount, 0.0D, 0.0D, 0.01D, 0.0D);
				((WorldServer) e.world).spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, e.posX + 1.4, e.posY + h,
				e.posZ - 1.4, amount, 0.0D, 0.0D, 0.0D, 0.0D);
				((WorldServer) e.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, e.posX, e.posY + h, e.posZ - 1.5,
				amount, 0.0D, 0.0D, 0.01D, 0.0D);
				((WorldServer) e.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, e.posX - 0.75, e.posY + h,
				e.posZ - 1.28, amount, 0.0D, 0.0D, 0.01D, 0.0D);
				((WorldServer) e.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, e.posX - 1.28, e.posY + h,
				e.posZ - 0.75, amount, 0.0D, 0.0D, 0.01D, 0.0D);
				((WorldServer) e.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, e.posX - 1.5, e.posY + h, e.posZ,
				amount, 0.0D, 0.0D, 0.01D, 0.0D);
				((WorldServer) e.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, e.posX - 1.28, e.posY + h,
				e.posZ + 0.75, amount, 0.0D, 0.0D, 0.01D, 0.0D);
				((WorldServer) e.world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, e.posX - 0.75, e.posY + h,
				e.posZ + 1.28, amount, 0.0D, 0.0D, 0.01D, 0.0D);
			}
		}
	}
}