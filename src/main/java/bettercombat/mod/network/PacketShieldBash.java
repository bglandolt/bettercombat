package bettercombat.mod.network;

import bettercombat.mod.util.ConfigurationHandler;
import bettercombat.mod.util.Helpers;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketShieldBash implements IMessage
{
	private Integer entityId;
	//protected EnumHand hand = EnumHand.OFF_HAND;
	//protected int damage = 0;
	//protected int knockback = 0;
	//protected int cooldown = 0;

	public PacketShieldBash()
	{

	}
	
	public PacketShieldBash( Integer entId ) //, int damage, int knockback )
	{
		//this.hand = enumHand;
		this.entityId = entId;
		//this.damage = damage;
		//this.knockback = knockback;
		//this.cooldown = cooldown;
	}

	public void fromBytes( ByteBuf buf )
	{
		if ( buf.readBoolean() )
		{
			this.entityId = ByteBufUtils.readVarInt(buf, 4);
		}
		//this.hand = EnumHand.values()[ByteBufUtils.readVarInt(buf, 1)];
		//this.damage = ByteBufUtils.readVarInt(buf, 2);
		//this.knockback = ByteBufUtils.readVarInt(buf, 2);
		//this.cooldown = ByteBufUtils.readVarInt(buf, 1);
	}

	public void toBytes( ByteBuf buf )
	{
		buf.writeBoolean(this.entityId != null);
		
		if ( this.entityId != null )
		{
			ByteBufUtils.writeVarInt(buf, this.entityId, 4);
		}
		
		//ByteBufUtils.writeVarInt(buf, this.hand.ordinal(), 1);
		//ByteBufUtils.writeVarInt(buf, this.damage, 2);
		//ByteBufUtils.writeVarInt(buf, this.knockback, 2);
		//ByteBufUtils.writeVarInt(buf, this.cooldown, 1);
	}

	public void handleClientSide( PacketShieldBash message, EntityPlayer player )
	{

	}

	public PacketShieldBash( int f, Integer parEntityId )
	{
		this.entityId = parEntityId;
	}

	public static class Handler implements IMessageHandler<PacketShieldBash, IMessage>
	{
		@Override
		public IMessage onMessage( final PacketShieldBash message, final MessageContext ctx )
		{
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}

		private static void handle( PacketShieldBash message, MessageContext ctx )
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			
			if ( message.entityId != null )
			{
				Entity target = player.world.getEntityByID(message.entityId);
				
				if ( target != null )
				{
					Helpers.playerAttackVictim(player, target, false);
				}
			}
			
			player.stopActiveHand();

			if ( ConfigurationHandler.inertiaOnAttack != 1.0F )
			{
				if ( player.onGround )
				{
					player.motionX *= ConfigurationHandler.inertiaOnAttack;
					player.motionZ *= ConfigurationHandler.inertiaOnAttack;

					player.velocityChanged = true;
				}
			}
			
			
			
//			if ( ConfigurationHandler.momentumOnAttack != 0.0F )
//			{
//				if ( player.onGround )
//				{
//					player.motionY += 0.001D;
//				}
//		
//				player.motionX -= ConfigurationHandler.momentumOnAttack*MathHelper.sin(player.rotationYaw * 0.017453292F);
//				player.motionZ += ConfigurationHandler.momentumOnAttack*MathHelper.cos(player.rotationYaw * 0.017453292F);
//				
//				player.velocityChanged = true;
//			}
			
//			EntityPlayerMP player = ctx.getServerHandler().player;
//
//			if ( message == null || player == null )
//			{
//				return;
//			}
//
//			EnumHand shieldHand = message.hand;
//			ItemStack shield = player.getHeldItem(shieldHand);
//
//			if ( message.entityId != null )
//			{
//				Entity victim = player.world.getEntityByID(message.entityId);
//
//				if ( victim instanceof EntityLivingBase ) // && player.isActiveItemStackBlocking() && !shield.isEmpty() && !player.getCooldownTracker().hasCooldown(shield.getItem()) && shield.getItem() instanceof ItemShield )
//				{
//					int knockLvl = EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, shield);
//
//					int mod = 0;
//
//					try
//					{
//						mod = player.getActivePotionEffect(MobEffects.STRENGTH).getAmplifier() * 3;
//					}
//					catch (Exception e)
//					{
//
//					}
//
//					try
//					{
//						mod -= player.getActivePotionEffect(MobEffects.WEAKNESS).getAmplifier() * 3;
//					}
//					catch (Exception e)
//					{
//
//					}
//					
//					String shieldString = Helpers.getString(shield);
//
//					float damage = (message.damage/100.0F) + mod;
//
//					if ( ConfigurationHandler.shieldSilverDamageMultiplier != 1.0F && ((EntityLivingBase)victim).isEntityUndead() && Helpers.isSilver(shieldString) )
//					{
//						damage *= ConfigurationHandler.shieldSilverDamageMultiplier;
//						EventHandlers.playSilverArmorEffect(victim);
//					}
//
//					if ( damage > 0.0F )
//					{
//						victim.hurtResistantTime = 0;
//						((EntityLivingBase) victim).knockBack((Entity) player, (message.knockback/100.0F)+knockLvl, MathHelper.sin(player.rotationYaw * 0.017453292F), -MathHelper.cos(player.rotationYaw * 0.017453292F));
//						victim.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) player), damage); // XXX
//						shield.damageItem(5, (EntityLivingBase) player);
//						player.onCriticalHit(victim);
//						victim.hurtResistantTime = 0;
//					}
//
//					if ( Helpers.isMetal(shieldString) )
//					{
//						// SoundHandler.playBashMetalShieldSound(player);
//					}
//					else
//					{
//						// SoundHandler.playBashWoodShieldSound(player);
//					}
//				}
//			}
//			
//			player.stopActiveHand();
		}
	}
}
// public class PacketShieldBash extends PacketBase<PacketShieldBash> {
// protected int entityId;
//
// protected boolean attackEntity = false;
//
// protected EnumHand hand;
//
// public PacketShieldBash(EnumHand enumHand, int entId, boolean atkEntity) {
// this.hand = enumHand;
// this.entityId = entId;
// this.attackEntity = atkEntity;
// }
//
// public void fromBytes(ByteBuf buf) {
// this.hand = EnumHand.values()[ByteBufUtils.readVarInt(buf, 1)];
// this.entityId = ByteBufUtils.readVarInt(buf, 4);
// this.attackEntity = buf.readBoolean();
// }
//
// public void toBytes(ByteBuf buf) {
// ByteBufUtils.writeVarInt(buf, this.hand.ordinal(), 1);
// ByteBufUtils.writeVarInt(buf, this.entityId, 4);
// buf.writeBoolean(this.attackEntity);
// }
//
// public void handleClientSide(PacketShieldBash message, EntityPlayer player)
// {}
//
// public void handleServerSide(PacketShieldBash message, EntityPlayerMP player)
// {
// boolean attackEntity = false;
// if (message == null || player == null)
// return;
// EnumHand shieldHand = message.hand;
// int entId = message.entityId;
// attackEntity = message.attackEntity;
// Entity victim = player.world.getEntityByID(entId);
// if (player.isActiveItemStackBlocking()) {
// ItemStack shield = player.getHeldItem(shieldHand);
// if (!shield.isEmpty() &&
// !player.getCooldownTracker().hasCooldown(shield.getItem()) &&
// shield.getItem() instanceof
// com.oblivioussp.spartanshields.item.ItemShieldBase) {
// if (attackEntity && victim != null && victim instanceof EntityLivingBase) {
// int knockLvl = EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK,
// shield);
// victim.hurtResistantTime = 0;
// ((EntityLivingBase)victim).knockBack((Entity)player, 1.0F + knockLvl,
// MathHelper.sin(player.rotationYaw * 0.017453292F),
// -MathHelper.cos(player.rotationYaw * 0.017453292F));
// victim.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)player),
// 1.0F);
// shield.damageItem(5, (EntityLivingBase)player);
// player.world.playSound((EntityPlayer)null, player.posX, player.posY,
// player.posZ, SoundEvents.ITEM_SHIELD_BLOCK, player.getSoundCategory(), 1.0F,
// 1.0F);
// player.onCriticalHit(victim);
// } else {
// player.world.playSound((EntityPlayer)null, player.posX, player.posY,
// player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
// player.getSoundCategory(), 0.5F, 0.01F);
// }
// player.stopActiveHand();
// player.swingArm(shieldHand);
// player.getCooldownTracker().setCooldown(shield.getItem(),
// ConfigHandler.cooldownShieldBash);
// }
// }
// }
//
// public PacketShieldBash() {}
// }
//
//
//
//
