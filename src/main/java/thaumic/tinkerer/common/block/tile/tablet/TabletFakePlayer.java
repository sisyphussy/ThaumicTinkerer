/**
 * This class was created by <Vazkii>. It's distributed as part of the ThaumicTinkerer Mod.
 *
 * ThaumicTinkerer is Open Source and distributed under a Creative Commons Attribution-NonCommercial-ShareAlike 3.0
 * License (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 *
 * ThaumicTinkerer is a Derivative Work on Thaumcraft 4. Thaumcraft 4 (c) Azanor 2012
 * (http://www.minecraftforum.net/topic/1585216-)
 *
 * File Created @ [9 Sep 2013, 15:54:36 (GMT)]
 */
package thaumic.tinkerer.common.block.tile.tablet;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

import com.mojang.authlib.GameProfile;

public class TabletFakePlayer extends FakePlayer {

    private final TileAnimationTablet tablet;

    public TabletFakePlayer(TileAnimationTablet tablet) {
        super(
                (WorldServer) tablet.getWorldObj(),
                new GameProfile(UUID.fromString("a8f026a0-135b-11e4-9191-0800200c9a66"), "[ThaumcraftTablet]"));
        this.tablet = tablet;
        this.playerNetServerHandler = new DummyNetHandlerPlayServer(MinecraftServer.getServer(), this);
        this.inventory.currentItem = 0;
        this.width = 0;
        this.height = 0;
        this.posX = tablet.xCoord + 0.5f;
        this.posY = tablet.yCoord + 0.5f;
        this.posZ = tablet.zCoord + 0.5f;
        this.boundingBox.setBounds(this.posX, this.posY, this.posZ, this.posX, this.posY, this.posZ);

    }

    @Override
    public void onUpdate() {
        capabilities.isCreativeMode = false;

        motionX = motionY = motionZ = 0;

        int meta = tablet.getBlockMetadata() & 7;
        int rotation = meta == 2 ? 180 : meta == 3 ? 0 : meta == 4 ? 90 : -90;
        rotationYaw = rotationYawHead = rotation;
        rotationPitch = -15;

        // Drop everything except for held item
        for (int i = 1; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack != null) {
                entityDropItem(stack, 0);
                inventory.setInventorySlotContents(i, null);
            }
        }
        syncSlots();
    }

    public void syncSlots() {
        this.inventory.mainInventory[0] = tablet.heldItem;
    }

    @Override
    public ChunkCoordinates getPlayerCoordinates() {
        return new ChunkCoordinates(tablet.xCoord, tablet.yCoord, tablet.zCoord);
    }

    @Override
    protected boolean isMovementBlocked() {
        return true;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public void setPosition(double x, double y, double z) {
        // NO-OP (the position is set at creation)
    }

    @Override
    public void setDead() {
        // NO-OP (this should never happen...?)
    }

    @Override
    public void mountEntity(Entity entityIn) {
        // NO-OP
    }

    @Override
    public void setHealth(float p_70606_1_) {
        // NO-OP
    }

    @Override
    public void onLivingUpdate() {
        // NO-OP
    }

    @Override
    public void addChatMessage(IChatComponent var1) {
        // NO-OP
    }

    @Override
    public void addChatComponentMessage(IChatComponent chatmessagecomponent) {
        // NO-OP
    }

    private static final class DummyNetHandlerPlayServer extends NetHandlerPlayServer {

        public DummyNetHandlerPlayServer(MinecraftServer server, FakePlayer player) {
            super(server, new NetworkManager(false), player);
        }

        @Override
        public void onNetworkTick() {
            // NO-OP
        }

        @Override
        public void sendPacket(Packet packetIn) {
            // NO-OP
        }
    }
}
