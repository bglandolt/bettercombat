package bettercombat.mod.potion;

import bettercombat.mod.client.ParticleBlood;
import bettercombat.mod.network.PacketBleeding;
import bettercombat.mod.network.PacketHandler;
import bettercombat.mod.util.ConfigurationHandler;
import bettercombat.mod.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PotionBleeding extends Potion
{
	public ResourceLocation    TEXTURE       = new ResourceLocation(Reference.MOD_ID + ":textures/gui/bleeding.png");
	public static DamageSource DAMAGE_SOURCE = new DamageSource("bleeding").setDamageBypassesArmor();

	//effect @e bettercombat:bleeding 100 1
	protected PotionBleeding()
	{
		super(false, 0xff6066);
		setRegistryName(Reference.MOD_ID, "bleeding");
		setPotionName(Reference.MOD_ID + ".effect.bleeding");
	}

	@SuppressWarnings("deprecation")
	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc)
	{
		super.renderInventoryEffect(x, y, effect, mc);
		mc.renderEngine.bindTexture(TEXTURE);
		GlStateManager.enableBlend();
		Gui.drawModalRectWithCustomSizedTexture(x + 6, y + 7, 0, 0, 18, 18, 18, 18);
	}

	@SuppressWarnings("deprecation")
	@Override
	@SideOnly(Side.CLIENT)
	public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha)
	{
		super.renderHUDEffect(x, y, effect, mc, alpha);
		mc.renderEngine.bindTexture(TEXTURE);
		GlStateManager.enableBlend();
		Gui.drawModalRectWithCustomSizedTexture(x + 3, y + 3, 0, 0, 18, 18, 18, 18);
	}
	
	@Override
    public boolean isBadEffect()
    {
        return true;
    }

	@Override
	public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier)
	{
		if ( entityLivingBaseIn.ticksExisted % 20 == 10 )
		{
			if ( ConfigurationHandler.bleedingDurationExtendedIfWet && entityLivingBaseIn.isWet() && !entityLivingBaseIn.canBreatheUnderwater() )
			{
				PotionEffect bleeding = entityLivingBaseIn.getActivePotionEffect(BetterCombatPotions.BLEEDING);
				
				if ( bleeding != null )
				{
					entityLivingBaseIn.removePotionEffect(bleeding.getPotion());
					entityLivingBaseIn.addPotionEffect(new PotionEffect(BetterCombatPotions.BLEEDING, bleeding.getDuration()+10, amplifier, bleeding.getIsAmbient(), bleeding.doesShowParticles()));
				}
			}
			
			if ( ConfigurationHandler.bleedingDamageIncreasedIfSprinting && entityLivingBaseIn.isSprinting() && entityLivingBaseIn.world.rand.nextFloat() < 0.2F )
			{
				PotionEffect bleeding = entityLivingBaseIn.getActivePotionEffect(BetterCombatPotions.BLEEDING);
				
				if ( bleeding != null )
				{
					entityLivingBaseIn.removePotionEffect(bleeding.getPotion());
					entityLivingBaseIn.addPotionEffect(new PotionEffect(BetterCombatPotions.BLEEDING, bleeding.getDuration()+20, amplifier, bleeding.getIsAmbient(), bleeding.doesShowParticles()));
				}
			}
			
			if ( entityLivingBaseIn.getHealth() > 0.0F )
			{
				entityLivingBaseIn.hurtResistantTime = 0;
				entityLivingBaseIn.attackEntityFrom(DAMAGE_SOURCE, ConfigurationHandler.bleedingDamagePerTick * (1 + amplifier));
				entityLivingBaseIn.hurtTime = 0;
				entityLivingBaseIn.maxHurtTime = 0;
				entityLivingBaseIn.hurtResistantTime = 0;
			}
			
			this.bleedParticles(entityLivingBaseIn, MathHelper.clamp(amplifier * 8 + 16, 16, 127));
		}
	}
	
	private void bleedParticles( EntityLivingBase entityLivingBaseIn, int numberOfParticles )
	{
		/* Server */
		if ( !entityLivingBaseIn.world.isRemote )
		{
			PacketHandler.instance.sendToAllAround(new PacketBleeding(entityLivingBaseIn.getEntityId(), numberOfParticles), new NetworkRegistry.TargetPoint(entityLivingBaseIn.world.provider.getDimension(), entityLivingBaseIn.posX, entityLivingBaseIn.posY, entityLivingBaseIn.posZ, 16));
		}
		/* Client */
		else
		{
			while ( --numberOfParticles >= 0 )
			{
				Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBlood.Factory().createParticle(0, entityLivingBaseIn.world, entityLivingBaseIn.posX, entityLivingBaseIn.posY, entityLivingBaseIn.posZ, 0, 0, 0));
			}
		}
	}

	@Override
	public boolean isReady(int duration, int amplifier)
	{
		return true;
	}
}