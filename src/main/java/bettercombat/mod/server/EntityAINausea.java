package bettercombat.mod.server;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.init.MobEffects;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

public class EntityAINausea extends EntityAIBase
{
	protected final EntityCreature victim;
	protected double speed;
	protected double randPosX;
	protected double randPosY;
	protected double randPosZ;

	public EntityAINausea( EntityCreature victim )
	{
		this.victim = victim;
		this.speed = 0.75D;
		/**
		 * Sets the mutex bitflags, see getMutexBits. Flag 1 for motion, flag 2 for
		 * look/head movement, flag 4 for
		 * swimming/misc. Flags can be OR'ed.
		 */
		this.setMutexBits(1);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute()
	{
		if ( victim.getNavigator() != null && (this.victim.isPotionActive(MobEffects.NAUSEA) || this.victim.isPotionActive(MobEffects.BLINDNESS)) )
		{
			double speed = 0.75D;

			if ( this.victim.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED) != null )
			{
				speed = MathHelper.clamp(1.25D - (this.victim.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getBaseValue() * 2.0D), 0.35D, 0.75D);
			}

			this.speed = speed * (0.9F + this.victim.world.rand.nextFloat() * 0.1F);

			if ( this.victim.world.rand.nextInt(10) == 0 )
			{
				return this.findRandomPosition(this.victim.getAttackTarget());
			}
			else if ( this.victim.world.rand.nextInt(10) == 0 )
			{
				this.randPosX = this.victim.posX;
				this.randPosY = this.victim.posY;
				this.randPosZ = this.victim.posZ;
				return true;
			}
		}
		return false;
	}

	public void startExecuting()
	{
		this.victim.getNavigator().clearPath();
	}

	public void updateTask()
	{
		this.victim.getNavigator().tryMoveToXYZ(this.randPosX, this.randPosY, this.randPosZ, this.speed);

		if ( this.victim.isPotionActive(MobEffects.BLINDNESS) )
		{
			this.spawnBlindnessParticles(this.victim);
		}
	}

	protected boolean findRandomPosition( EntityLivingBase attacker )
	{
		if ( attacker != null )
		{
			Vec3d enemyPos = attacker.getPositionVector();

			if ( enemyPos != null )
			{
				enemyPos.addVector(this.victim.world.rand.nextInt(7) - 3, 0, this.victim.world.rand.nextInt(7) - 3);
				Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockTowards(this.victim, 6, 4, enemyPos);

				if ( vec3d != null )
				{
					this.randPosX = vec3d.x;
					this.randPosY = vec3d.y;
					this.randPosZ = vec3d.z;
					return true;
				}
			}
		}

		Vec3d vec3d = RandomPositionGenerator.findRandomTarget(this.victim, 6, 4);

		if ( vec3d != null )
		{
			this.randPosX = vec3d.x;
			this.randPosY = vec3d.y;
			this.randPosZ = vec3d.z;
			return true;
		}

		return false;
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	// public boolean shouldContinueExecuting()
	// {
	// return ( this.victim.isPotionActive(MobEffects.NAUSEA) ||
	// this.victim.isPotionActive(MobEffects.BLINDNESS) );
	// }

	// public void spawnConfusionParticle( EntityLiving e )
	// {
	// double d0 = (double)(-MathHelper.sin(e.rotationYaw * 0.017453292F)*0.2F);
	// double d1 = (double)(MathHelper.cos(e.rotationYaw * 0.017453292F)*0.2F);
	// if (e.world instanceof WorldServer)
	// {
	// ((WorldServer)e.world).spawnParticle(EnumParticleTypes.SPELL_MOB,e.posX+d0,e.posY+(double)e.getEyeHeight(),e.posZ+d1,
	// 4, d0, 0.0D, d1, 0.001D);
	// }
	// }
	//
	public void spawnBlindnessParticles( EntityLiving e )
	{
		if ( e.world instanceof WorldServer )
		{
			((WorldServer) e.world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL, e.posX - MathHelper.sin(e.rotationYaw * 0.017453292F) * 0.3D, e.posY + e.getEyeHeight(), e.posZ + MathHelper.cos(e.rotationYaw * 0.017453292F) * 0.3D, 16, 0.0D, 0.0D, 0.0D, 0.0D);
		}
	}
}