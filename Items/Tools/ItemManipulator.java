/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.Items.Tools;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.IScribeTools;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.nodes.INode;
import Reika.ChromatiCraft.ChromatiCraft;
import Reika.ChromatiCraft.Auxiliary.ChromaAux;
import Reika.ChromatiCraft.Auxiliary.ChromaStructures;
import Reika.ChromatiCraft.Auxiliary.ProgressionManager.ProgressStage;
import Reika.ChromatiCraft.Auxiliary.Ability.AbilityHelper;
import Reika.ChromatiCraft.Auxiliary.Interfaces.SneakPop;
import Reika.ChromatiCraft.Base.ItemChromaTool;
import Reika.ChromatiCraft.Block.BlockCrystalFence.CrystalFenceAuxTile;
import Reika.ChromatiCraft.Block.BlockDummyAux.TileEntityDummyAux;
import Reika.ChromatiCraft.Block.Crystal.BlockCrystalGlow.TileEntityCrystalGlow;
import Reika.ChromatiCraft.Block.Crystal.BlockPowerTree.TileEntityPowerTreeAux;
import Reika.ChromatiCraft.Magic.PlayerElementBuffer;
import Reika.ChromatiCraft.Magic.Interfaces.ChargingPoint;
import Reika.ChromatiCraft.Magic.Interfaces.CrystalNetworkTile;
import Reika.ChromatiCraft.ModInterface.Bees.CrystalBees;
import Reika.ChromatiCraft.ModInterface.ThaumCraft.NodeRecharger;
import Reika.ChromatiCraft.Registry.ChromaBlocks;
import Reika.ChromatiCraft.Registry.ChromaSounds;
import Reika.ChromatiCraft.Registry.ChromaTiles;
import Reika.ChromatiCraft.Registry.Chromabilities;
import Reika.ChromatiCraft.Registry.CrystalElement;
import Reika.ChromatiCraft.Render.Particle.EntityRuneFX;
import Reika.ChromatiCraft.TileEntity.TileEntityBiomePainter;
import Reika.ChromatiCraft.TileEntity.TileEntityCrystalConsole;
import Reika.ChromatiCraft.TileEntity.TileEntityDataNode;
import Reika.ChromatiCraft.TileEntity.TileEntityLumenWire;
import Reika.ChromatiCraft.TileEntity.AOE.TileEntityAreaBreaker;
import Reika.ChromatiCraft.TileEntity.AOE.TileEntityAuraPoint;
import Reika.ChromatiCraft.TileEntity.AOE.TileEntityItemInserter;
import Reika.ChromatiCraft.TileEntity.AOE.TileEntityMultiBuilder;
import Reika.ChromatiCraft.TileEntity.AOE.Defence.TileEntityCrystalFence;
import Reika.ChromatiCraft.TileEntity.Acquisition.TileEntityMiner;
import Reika.ChromatiCraft.TileEntity.Auxiliary.TileEntityPylonTurboCharger;
import Reika.ChromatiCraft.TileEntity.Networking.TileEntityCrystalPylon;
import Reika.ChromatiCraft.TileEntity.Networking.TileEntityCrystalRepeater;
import Reika.ChromatiCraft.TileEntity.Processing.TileEntityGlowFire;
import Reika.ChromatiCraft.TileEntity.Recipe.TileEntityCastingTable;
import Reika.ChromatiCraft.TileEntity.Recipe.TileEntityRitualTable;
import Reika.ChromatiCraft.TileEntity.Technical.TileEntityStructControl;
import Reika.ChromatiCraft.TileEntity.Transport.TileEntityItemRift;
import Reika.ChromatiCraft.TileEntity.Transport.TileEntityRift;
import Reika.ChromatiCraft.TileEntity.Transport.TileEntityRouterHub;
import Reika.ChromatiCraft.TileEntity.Transport.TileEntityTeleportGate;
import Reika.ChromatiCraft.TileEntity.Transport.TileEntityTransportWindow;
import Reika.DragonAPI.APIPacketHandler.PacketIDs;
import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.DragonAPIInit;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.APIStripper.Strippable;
import Reika.DragonAPI.Libraries.ReikaPlayerAPI;
import Reika.DragonAPI.Libraries.IO.ReikaColorAPI;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.ModInteract.DeepInteract.ReikaThaumHelper;
import Reika.DragonAPI.ModRegistry.InterfaceCache;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import forestry.api.apiculture.IBeeHousing;

@Strippable("thaumcraft.api.IScribeTools")
public class ItemManipulator extends ItemChromaTool implements IScribeTools {

	public ItemManipulator(int index) {
		super(index);
	}

	@Override
	public boolean onItemUse(ItemStack is, EntityPlayer ep, World world, int x, int y, int z, int s, float a, float b, float c) {
		if (ReikaPlayerAPI.isFakeOrNotInteractable(ep, x+0.5, y+0.5, z+0.5, 8))
			return false;
		ChromaTiles t = ChromaTiles.getTile(world, x, y, z);
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof SneakPop && ep.isSneaking()) {
			if (((SneakPop)tile).canDrop(ep)) {
				((SneakPop)tile).drop();
				ChromaSounds.RIFT.playSoundAtBlock(tile);
				return true;
			}
		}

		if (t == null && tile instanceof TileEntityDummyAux) {
			return ((TileEntityDummyAux)tile).relayManipulatorClick(is, ep, s, a, b, c);
		}

		if (t == ChromaTiles.RIFT) {
			TileEntityRift te = (TileEntityRift)tile;
			te.setDirection(ForgeDirection.VALID_DIRECTIONS[s]);
			return true;
		}
		if (t == ChromaTiles.TABLE) {
			boolean flag = ((TileEntityCastingTable)tile).triggerCrafting(ep);
			return flag;
		}
		if (t == ChromaTiles.RITUAL) {
			boolean flag = ((TileEntityRitualTable)tile).triggerRitual(ep);
			return flag;
		}
		if (t == ChromaTiles.MINER) {
			((TileEntityMiner)tile).triggerDigging();
			return true;
		}
		if (t == ChromaTiles.ITEMRIFT) {
			TileEntityItemRift ir = (TileEntityItemRift)tile;
			ir.isEmitting = !ir.isEmitting;
			return true;
		}
		if (t == ChromaTiles.CONSOLE) {
			TileEntityCrystalConsole tc = (TileEntityCrystalConsole)tile;
			tc.setFacing(ForgeDirection.VALID_DIRECTIONS[s]);
			return true;
		}
		if (t == ChromaTiles.AURAPOINT) {
			TileEntityAuraPoint tp = (TileEntityAuraPoint)tile;
			tp.doPVP = !tp.doPVP;
			return true;
		}
		if (t == ChromaTiles.BIOMEPAINTER) {
			TileEntityBiomePainter tb = (TileEntityBiomePainter)tile;
			tb.safeMode = !tb.safeMode;
			return true;
		}
		if (t == ChromaTiles.PYLONTURBO) {
			TileEntityPylonTurboCharger te = (TileEntityPylonTurboCharger)tile;
			if (te.trigger(ep)) {

			}
			return true;
		}
		if (t == ChromaTiles.GLOWFIRE) {
			TileEntityGlowFire te = (TileEntityGlowFire)tile;
			if (ep.isSneaking()) {
				te.empty();
			}
			else {
				if (te.craft()) {
					ChromaSounds.CAST.playSoundAtBlock(te);
				}
				else {
					ChromaSounds.ERROR.playSoundAtBlock(te);
				}
			}
		}

		if (t == ChromaTiles.DATANODE) {
			TileEntityDataNode te = (TileEntityDataNode)tile;
			te.scan(ep);
			return true;
		}

		if (t == ChromaTiles.MULTIBUILDER) {
			TileEntityMultiBuilder te = (TileEntityMultiBuilder)tile;
			ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[s].getOpposite();
			if (dir.offsetY == 0) {
				if (ep.isSneaking()) {
					te.contract(dir);
				}
				else {
					te.expand(dir);
				}
				return true;
			}
		}

		if (t == ChromaTiles.WINDOW) {
			TileEntityTransportWindow ir = (TileEntityTransportWindow)tile;
			if (!ir.isUnMineable()) {
				if (ep.isSneaking()) {
					ir.renderBackPane = !ir.renderBackPane;
				}
				else {
					ir.renderTexture = !ir.renderTexture;
				}
			}
			return true;
		}

		if (t == ChromaTiles.LUMENWIRE) {
			TileEntityLumenWire ir = (TileEntityLumenWire)tile;
			ir.cycleMode();
			return true;
		}

		if (t == ChromaTiles.INSERTER) {
			TileEntityItemInserter ir = (TileEntityItemInserter)tile;
			ir.omniMode = !ir.omniMode;
			ChromaSounds.USE.playSoundAtBlock(ir);
			return true;
		}

		if (t == ChromaTiles.ROUTERHUB) {
			TileEntityRouterHub rh = (TileEntityRouterHub)tile;
			rh.scanAndLink(world, x, y, z, 32);
			ChromaSounds.USE.playSoundAtBlock(rh);
			return true;
		}

		if (t == ChromaTiles.AREABREAKER) {
			TileEntityAreaBreaker ab = (TileEntityAreaBreaker)tile;
			if (ep.isSneaking()) {
				ab.incRange();
			}
			else {
				ab.cycleShape();
			}
			return true;
		}
		/*
		if (t == ChromaTiles.HOVERPAD) {
			TileEntityHoverPad ir = (TileEntityHoverPad)tile;
			ir.toggleMode();
			return true;
		}
		 */

		if (t == ChromaTiles.FENCE) {
			TileEntityCrystalFence te = (TileEntityCrystalFence)tile;
			if (ep.isSneaking()) {
				te.setFacing(ForgeDirection.VALID_DIRECTIONS[s]);
			}
			else {
				te.calcFence();
				if (te.isValid()) {
					ChromaSounds.CAST.playSoundAtBlock(te);
				}
				else {
					ChromaSounds.ERROR.playSoundAtBlock(te);
				}
			}
			return true;
		}

		if (tile instanceof CrystalFenceAuxTile) {
			CrystalFenceAuxTile te = (CrystalFenceAuxTile)tile;
			ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[s];
			if (ep.isSneaking()) {
				te.setOutput(dir);
			}
			else {
				te.setInput(dir);
			}
		}

		//if (t == ChromaTiles.FIBERSINK) {
		//	TileEntityFiberTransmitter ft = (TileEntityFiberTransmitter)tile;
		//	ft.setFacing(ForgeDirection.VALID_DIRECTIONS[s]);
		//	return true;
		//}

		if (t == ChromaTiles.PYLON && ep.capabilities.isCreativeMode && DragonAPICore.debugtest) {
			TileEntityCrystalPylon cp = (TileEntityCrystalPylon)tile;
			if (ep.isSneaking()) {
				if (cp.isEnhanced())
					cp.disenhance();
				else
					cp.enhance();
			}
			else {
				ChromaAux.changePylonColor(world, cp, CrystalElement.elements[(cp.getColor().ordinal()+1)%16]);
			}
			return true;
		}

		if (t == ChromaTiles.STRUCTCONTROL && ep.capabilities.isCreativeMode && DragonAPICore.debugtest) {
			TileEntityStructControl te = (TileEntityStructControl)tile;
			if (ep.isSneaking()) {
				te.setMonument();
			}
			else {

			}
			return true;
		}

		if (t == ChromaTiles.STRUCTCONTROL) {
			//if (!world.isRemote) {
			ChromatiCraft.logger.debug("Right clicked struct control. Side="+FMLCommonHandler.instance().getEffectiveSide());
			if (ProgressStage.CTM.playerHasPrerequisites(ep)) {
				ChromatiCraft.logger.debug("Player has CTM prereqs. Side="+FMLCommonHandler.instance().getEffectiveSide());
				TileEntityStructControl te = (TileEntityStructControl)tile;
				if (te.isMonument()) {
					ChromatiCraft.logger.debug("Tile is a monument. Side="+FMLCommonHandler.instance().getEffectiveSide());
					if (te.triggerMonument(ep)) {
						ChromatiCraft.logger.debug("Monument triggered. Side="+FMLCommonHandler.instance().getEffectiveSide());
						ChromaSounds.USE.playSoundAtBlockNoAttenuation(te, 1, 1, 128);
						return true;
					}
				}
			}
			ChromaSounds.ERROR.playSoundAtBlock(tile);
			//}
			return true;
		}

		if (t == ChromaTiles.TELEPORT) {
			TileEntityTeleportGate te = (TileEntityTeleportGate)tile;
			if (DragonAPICore.debugtest) {
				ChromaStructures.getGateStructure(world, x, y, z).place();
				te.validateStructure();
			}
			else {
				if (te.isOwnedByPlayer(ep)) {
					if (ep.isSneaking()) {
						te.incrementDirection();
					}
					else {
						te.publicMode = !te.publicMode;
					}
					ChromaSounds.USE.playSoundAtBlock(te);
				}
				else {
					ChromaSounds.ERROR.playSoundAtBlock(te);
				}
			}
		}

		if (t != null && t.isRepeater() && t != ChromaTiles.SKYPEATER) {
			TileEntityCrystalRepeater te = (TileEntityCrystalRepeater)tile;
			te.triggerConnectionRender();
			if (ep.isSneaking()) {
				if (te.isPlacer(ep)) {
					//world.setBlock(x, y, z, Blocks.air);
					//ReikaItemHelper.dropItem(world, x+0.5, y+0.5, z+0.5, ChromaTiles.REPEATER.getCraftedProduct());
					ReikaSoundHelper.playSoundAtBlock(world, x, y, z, Block.soundTypeStone.getStepResourcePath(), 2, 0.5F);
					te.redirect(s);
				}
			}
			else if (!world.isRemote) {
				if (te.checkConnectivity()) {
					CrystalElement e = te.getActiveColor();
					ChromaSounds.CAST.playSoundAtBlock(world, x, y, z);
					int rd = e.getRed();
					int gn = e.getGreen();
					int bl = e.getBlue();
					ReikaPacketHelper.sendDataPacket(DragonAPIInit.packetChannel, PacketIDs.COLOREDPARTICLE.ordinal(), te, rd, gn, bl, 32, 8);
					ReikaPacketHelper.sendDataPacket(DragonAPIInit.packetChannel, PacketIDs.NUMBERPARTICLE.ordinal(), te, te.getSignalDepth(e));
				}
				else {
					ChromaSounds.ERROR.playSoundAtBlock(world, x, y, z);
				}
			}
			return true;
		}

		if (tile instanceof TileEntityCrystalGlow) {
			if (ep.isSneaking())
				((TileEntityCrystalGlow)tile).toggle();
			else
				((TileEntityCrystalGlow)tile).rotate();
			ReikaSoundHelper.playBreakSound(world, x, y, z, Blocks.stone, 0.35F, 0.05F);
		}

		if (ModList.THAUMCRAFT.isLoaded() && !(tile instanceof CrystalNetworkTile) && InterfaceCache.NODE.instanceOf(tile)) {
			if (ProgressStage.CTM.isPlayerAtStage(ep) && ReikaThaumHelper.isResearchComplete(ep, "NODESTABILIZERADV")) { //CC and TC progression
				if (!world.isRemote) {
					NodeRecharger.instance.addNode((INode)tile);
					ReikaSoundHelper.playSoundFromServer(world, x+0.5, y+0.5, z+0.5, "thaumcraft:runicShieldEffect", 1, 1, false);
					for (Aspect asp : ((INode)tile).getAspects().aspects.keySet()) {
						int color = asp.getColor();
						int rd = ReikaColorAPI.getRed(color);
						int gn = ReikaColorAPI.getGreen(color);
						int bl = ReikaColorAPI.getBlue(color);
						ReikaPacketHelper.sendDataPacket(DragonAPIInit.packetChannel, PacketIDs.COLOREDPARTICLE.ordinal(), tile, rd, gn, bl, 8, 2);
					}
					for (Aspect asp : ((INode)tile).getAspects().aspects.keySet()) {
						((INode)tile).takeFromContainer(asp, 1);
					}
				}
			}
		}

		if (ModList.APPENG.isLoaded() && InterfaceCache.GRIDHOST.instanceOf(tile)) {
			AbilityHelper.instance.saveMESystemLocation(ep, tile, s);
			return true;
		}

		if (!world.isRemote && ModList.FORESTRY.isLoaded() && InterfaceCache.BEEHOUSE.instanceOf(tile)) {
			IBeeHousing ibh = (IBeeHousing)tile;
			CrystalBees.showConditionalStatuses(world, x, y, z, ep, ibh);
		}

		return false;
	}

	@Override
	public ItemStack onEaten(ItemStack is, World world, EntityPlayer ep)
	{
		return is;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack is) {
		return 72000;
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
		int r = Chromabilities.REACH.enabledOn(player) ? 96 : 24;
		MovingObjectPosition mov = ReikaPlayerAPI.getLookedAtBlock(player, r, false);
		if (DragonAPICore.debugtest) {
			if (player.isSneaking())
				player.getEntityData().removeTag("CrystalBuffer");
			else {
				PlayerElementBuffer.instance.addToPlayer(player, CrystalElement.elements[count%16], PlayerElementBuffer.instance.getElementCap(player), false);
			}
		}

		if (ProgressStage.PYLON.isPlayerAtStage(player)) {
			if (mov != null) {
				World world = player.worldObj;
				int x = mov.blockX;
				int y = mov.blockY;
				int z = mov.blockZ;

				Block b = world.getBlock(x, y, z);
				TileEntity te = world.getTileEntity(x, y, z);

				if (b == ChromaBlocks.POWERTREE.getBlockInstance()) {
					te = ((TileEntityPowerTreeAux)te).getCenter();
				}

				if (te instanceof ChargingPoint) {
					ChargingPoint cp = (ChargingPoint)te;
					ChromaAux.chargePlayerFromPylon(player, cp, cp.getDeliveredColor(player, world, x, y, z), count);
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	private void spawnParticles(EntityPlayer player, CrystalElement e) {
		double rx = ReikaRandomHelper.getRandomPlusMinus(player.posX, 0.8);
		double ry = ReikaRandomHelper.getRandomPlusMinus(player.posY, 1.5);
		double rz = ReikaRandomHelper.getRandomPlusMinus(player.posZ, 0.8);
		Minecraft.getMinecraft().effectRenderer.addEffect(new EntityRuneFX(player.worldObj, rx, ry, rz, e));
	}

	@Override
	public ItemStack onItemRightClick(ItemStack is, World world, EntityPlayer ep)
	{
		ep.setItemInUse(is, this.getMaxItemUseDuration(is));
		return is;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack is) {
		return EnumAction.bow;
	}

}
