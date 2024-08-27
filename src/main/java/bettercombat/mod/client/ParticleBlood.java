package bettercombat.mod.client;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleBlood extends Particle
{
	private final float lavaParticleScale;

	//effect @e bettercombat:bleeding 100 1
	protected ParticleBlood(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn)
	{
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
		
		double d = this.world.getWorldTime() * Math.PI * 0.1D;
		double dirX = Math.cos(d) * 0.1D;
		double dirZ = Math.sin(d) * 0.1D;
		this.motionX = this.motionX * 0.2D + dirX;
		this.motionY = this.rand.nextDouble() * 0.3D;
		this.motionZ = this.motionZ * 0.2D + dirZ;
		
		float f = this.rand.nextFloat() * 0.1F;
		this.particleRed = 0.85F - f;
		this.particleGreen = 0.25F - f;
		this.particleBlue = 0.45F - f;
		this.particleScale = this.rand.nextFloat() + 0.2F;
		this.lavaParticleScale = this.particleScale;
		this.particleMaxAge = this.rand.nextInt(65) + 16;
		this.setParticleTextureIndex(49);
	}

	public int getBrightnessForRender(float p_189214_1_)
	{
		int i = super.getBrightnessForRender(p_189214_1_);
		// int j = 240;
		int k = i >> 16 & 255;
		return 240 | k << 16;
	}

	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
	{
		float f = ((float) this.particleAge + partialTicks) / (float) this.particleMaxAge;
		this.particleScale = this.lavaParticleScale * (1.0F - f * f);
		super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
	}

	public void onUpdate()
	{
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (this.particleAge++ >= this.particleMaxAge)
		{
			this.setExpired();
		}

//        float f = (float)this.particleAge / (float)this.particleMaxAge;

//        if (this.rand.nextFloat() > f)
//        {
//            this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY, this.posZ, this.motionX, this.motionY, this.motionZ);
//        }

		this.motionY -= 0.03D;
		this.move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.99D;
		this.motionY *= 0.99D;
		this.motionZ *= 0.99D;

		if (this.onGround)
		{
			this.motionX *= 0.7D;
			this.motionZ *= 0.7D;
		}

	}

	@SideOnly(Side.CLIENT)
	public static class Factory implements IParticleFactory
	{
		public Particle createParticle(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_)
		{
			return new ParticleBlood(worldIn, xCoordIn, yCoordIn, zCoordIn);
		}
	}
}