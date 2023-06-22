package bettercombat.mod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLog;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketOnItemUse implements IMessage
{
//	private Integer entityId;
	private int x;
	private int y;
	private int z;
	
	private Integer sideHit;

	public PacketOnItemUse()
	{
		
	}
	
	public PacketOnItemUse(int posX, int posY, int posZ, EnumFacing sideHit )
	{
//		this.entityId = entityId;
		this.x = posX;
		this.y = posY;
		this.z = posZ;
		
		this.sideHit = sideHit.getIndex();
	}

	@Override
	public void fromBytes( ByteBuf buf )
	{
//		if ( buf.readBoolean() )
//		{
//			this.entityId = ByteBufUtils.readVarInt(buf, 4);
//		}
				
		this.x = buf.readBoolean() ? -ByteBufUtils.readVarInt(buf, 4) : ByteBufUtils.readVarInt(buf, 4);
		this.y = buf.readBoolean() ? -ByteBufUtils.readVarInt(buf, 2) : ByteBufUtils.readVarInt(buf, 2);
		this.z = buf.readBoolean() ? -ByteBufUtils.readVarInt(buf, 4) : ByteBufUtils.readVarInt(buf, 4);
		
		this.sideHit = ByteBufUtils.readVarInt(buf, 1);

	}

	@Override
	public void toBytes( ByteBuf buf )
	{
//		buf.writeBoolean(this.entityId != null);
//		
//		if ( this.entityId != null )
//		{
//			ByteBufUtils.writeVarInt(buf, this.entityId, 4);
//		}
				
		if ( this.x < 0 )
		{
			buf.writeBoolean(true);
			ByteBufUtils.writeVarInt(buf, -this.x, 4);
		}
		else
		{
			buf.writeBoolean(false);
			ByteBufUtils.writeVarInt(buf, this.x, 4);
		}
		
		if ( this.y < 0 )
		{
			buf.writeBoolean(true);
			ByteBufUtils.writeVarInt(buf, -this.y, 2);
		}
		else
		{
			buf.writeBoolean(false);
			ByteBufUtils.writeVarInt(buf, this.y, 2);
		}
		
		if ( this.z < 0 )
		{
			buf.writeBoolean(true);
			ByteBufUtils.writeVarInt(buf, -this.z, 4);
		}
		else
		{
			buf.writeBoolean(false);
			ByteBufUtils.writeVarInt(buf, this.z, 4);
		}
		
		ByteBufUtils.writeVarInt(buf, this.sideHit, 1);

	}

	public static class Handler implements IMessageHandler<PacketOnItemUse, IMessage>
	{
		@Override
		public IMessage onMessage( final PacketOnItemUse message, final MessageContext ctx )
		{
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}

		private static void handle( PacketOnItemUse message, MessageContext ctx )
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			BlockPos blockPos = new BlockPos(message.x,message.y,message.z);
			
			EnumActionResult result = player.getHeldItemMainhand().getItem().onItemUse(player, player.world, blockPos, EnumHand.MAIN_HAND, EnumFacing.getFront(message.sideHit), 0.0F, 0.0F, 0.0F);
		
			if ( result.equals(EnumActionResult.SUCCESS) )
			{
				Block block = player.world.getBlockState(blockPos).getBlock();
				
				if ( block == null || block instanceof BlockAir )
				{
					return;
				}
				
				if ( block instanceof BlockLog )
				{
					if ( player.world instanceof WorldServer )
			        {
			            ((WorldServer)player.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, message.x, message.y, message.z, 32, 1.0D, 1.0D, 1.0D, 0.02D, Block.getStateId(block.getDefaultState()));
			        }
				}
				else
				{
			        if ( player.world instanceof WorldServer )
			        {
			            ((WorldServer)player.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, message.x+0.3D, message.y+1.0D, message.z+0.3D, 16, 0.4D, 0.1D, 0.4D, 0.015D, Block.getStateId(block.getDefaultState()));
			        }
				}
			}
			
			// player.getEntityWorld().setBlockState(new BlockPos(message.x,message.y,message.z), Blocks.GRASS_PATH.getDefaultState(), 11);
			
//			ItemStack itemstack = player.getHeldItem(hand);
//
//	        if (!player.canPlayerEdit(pos.offset(facing), facing, itemstack))
//	        {
//	            return EnumActionResult.FAIL;
//	        }
//	        else
//	        {
//	            IBlockState iblockstate = worldIn.getBlockState(pos);
//	            Block block = iblockstate.getBlock();
//
//	            if (facing != EnumFacing.DOWN && worldIn.getBlockState(pos.up()).getMaterial() == Material.AIR && block == Blocks.GRASS)
//	            {
//	                IBlockState iblockstate1 = Blocks.GRASS_PATH.getDefaultState();
//	                worldIn.playSound(player, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
//
//	                if (!worldIn.isRemote)
//	                {
//	                    worldIn.setBlockState(pos, iblockstate1, 11);
//	                    itemstack.damageItem(1, player);
//	                }
//
//	                return EnumActionResult.SUCCESS;
//	            }
//	            else
//	            {
//	                return EnumActionResult.PASS;
//	            }
//	        }
		
		}
	}
}