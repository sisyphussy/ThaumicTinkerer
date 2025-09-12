/**
 * This class was created by <Vazkii>. It's distributed as part of the ThaumicTinkerer Mod.
 *
 * ThaumicTinkerer is Open Source and distributed under a Creative Commons Attribution-NonCommercial-ShareAlike 3.0
 * License (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 *
 * ThaumicTinkerer is a Derivative Work on Thaumcraft 4. Thaumcraft 4 (c) Azanor 2012
 * (http://www.minecraftforum.net/topic/1585216-)
 *
 * File Created @ [9 Sep 2013, 15:51:34 (GMT)]
 */
package thaumic.tinkerer.common.block.tile.tablet;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import appeng.api.movable.IMovableTile;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import thaumic.tinkerer.common.ThaumicTinkerer;
import thaumic.tinkerer.common.lib.LibBlockNames;
import thaumic.tinkerer.common.registry.TTRegistry;

@Optional.InterfaceList({ @Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers"),
        @Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft"),
        @Optional.Interface(iface = "appeng.api.movable.IMovableTile", modid = "appliedenergistics2") })
public class TileAnimationTablet extends TileEntity implements IInventory, IMovableTile, IPeripheral, SimpleComponent {

    private static final String TAG_LEFT_CLICK = "leftClick";
    private static final String TAG_REDSTONE = "redstone";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_MOD = "mod";

    private static final int[][] LOC_INCREASES = new int[][] { { 0, -1 }, { 0, +1 }, { -1, 0 }, { +1, 0 } };

    private static final byte SWING_SPEED = 3;
    private static final byte MAX_DEGREE = 45;
    public boolean leftClick = true;
    public boolean redstone = false;
    private byte prevSwingProgress;
    private byte swingProgress;
    private byte swingMod;

    private MovingObjectPosition hit;
    public ItemStack heldItem;

    private TabletFakePlayer player;
    private boolean isBreaking = false;
    private int ticksElapsed;

    private final Vec3 tempVec = Vec3.createVectorHelper(0, 0, 0);
    private final Vec3 positionVec = Vec3.createVectorHelper(0, 0, 0);

    private static Field durabilityRemainingOnBlock;

    @SideOnly(Side.CLIENT)
    public final float getRenderSwingProgress(float partialTicks) {
        return prevSwingProgress + (swingProgress - prevSwingProgress) * partialTicks;
    }

    @SideOnly(Side.CLIENT)
    public final int getTicksExisted() {
        return ticksElapsed;
    }

    @Override
    public void updateEntity() {
        if (worldObj.isRemote) {
            ticksElapsed++;
            prevSwingProgress = swingProgress;
            ItemStack stack = heldItem;
            if (stack != null) {
                swingProgress += swingMod;
                if (swingProgress >= MAX_DEGREE) {
                    swingMod = -SWING_SPEED;
                } else if (swingProgress <= 0) {
                    stopSwinging();
                }
            } else {
                stopSwinging();
            }
            return;
        }

        try {
            player.onUpdate();
            calculateHit();
            ItemStack stack = heldItem;
            if (stack != null) {
                swingProgress += swingMod;
                if (swingProgress >= MAX_DEGREE) {
                    swingHit(stack);
                    swingMod = -SWING_SPEED;
                } else if (swingProgress <= 0) {
                    stopSwinging();
                }
            } else {
                if (isBreaking) stopBreaking();
                stopSwinging();
            }
            if (isBreaking) {
                if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
                    stopBreaking();
                    return;
                }
                player.theItemInWorldManager.updateBlockRemoving();
                int durability = (Integer) durabilityRemainingOnBlock.get(player.theItemInWorldManager);
                if (durability >= 10) {
                    this.player.theItemInWorldManager.tryHarvestBlock(hit.blockX, hit.blockY, hit.blockZ);
                }
            }
            if (isIdle() && hit != null && (!redstone || isBreaking)) {
                initiateSwing();
            }
        } catch (Exception e) {
            e.printStackTrace();
            List<EntityPlayer> list = worldObj.getEntitiesWithinAABB(
                    EntityPlayer.class,
                    AxisAlignedBB
                            .getBoundingBox(xCoord - 8, yCoord - 8, zCoord - 8, xCoord + 8, yCoord + 8, zCoord + 8));
            for (EntityPlayer player : list) {
                player.addChatComponentMessage(
                        new ChatComponentText(
                                EnumChatFormatting.RED
                                        + "Something went wrong with a Tool Dynamism Tablet! Check your FML log."));
                player.addChatComponentMessage(
                        new ChatComponentText(
                                EnumChatFormatting.RED.toString() + EnumChatFormatting.ITALIC + e.getMessage()));
            }
        }
    }

    @Override
    public void validate() {
        super.validate();
        if (!worldObj.isRemote) {
            if (player == null) {
                player = new TabletFakePlayer(this);
            }
            player.worldObj = this.worldObj;

            if (durabilityRemainingOnBlock == null) {
                try {
                    Field f;
                    try {
                        f = ItemInWorldManager.class.getDeclaredField("field_73094_o");
                    } catch (Exception e) {
                        f = ItemInWorldManager.class.getDeclaredField("durabilityRemainingOnBlock");
                    }
                    f.setAccessible(true);
                    durabilityRemainingOnBlock = f;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void swingHit(ItemStack stack) {
        if (hit == null) return;

        if (leftClick) {
            if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                player.attackTargetEntityWithCurrentItem(hit.entityHit);
                updateState();
            } else if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                if (!isBreaking) {
                    int x = hit.blockX;
                    int y = hit.blockY;
                    int z = hit.blockZ;
                    Block block = worldObj.getBlock(x, y, z);
                    if (!block.isAir(worldObj, x, y, z) && block.getBlockHardness(worldObj, x, y, z) >= 0) {
                        isBreaking = true;
                        player.theItemInWorldManager.onBlockClicked(x, y, z, hit.sideHit);
                        updateState();
                    }
                }
            }
        } else {
            if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                if (player.interactWith(hit.entityHit)) {
                    updateState();
                }
                return;
            }

            int side = hit.sideHit;

            int x = hit.blockX;
            int y = hit.blockY;
            int z = hit.blockZ;

            // Copied from Minecraft's right click logic
            boolean result = !ForgeEventFactory
                    .onPlayerInteract(player, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, x, y, z, side, worldObj)
                    .isCanceled();

            if (result) {
                Vec3 hitVec = hit.hitVec;
                float hitX = (float) hitVec.xCoord;
                float hitY = (float) hitVec.yCoord;
                float hitZ = (float) hitVec.zCoord;
                if (player.onPlayerRightClick(stack, x, y, z, side, hitX, hitY, hitZ)) {
                    updateState();
                    return;
                }
            }

            if (player.sendUseItem(stack)) {
                updateState();
            }
        }
    }

    public final boolean isIdle() {
        return swingMod == 0;
    }

    private void updateState() {
        ItemStack stack = player.getHeldItem();
        this.heldItem = stack;
        if (stack == null || stack.stackSize <= 0) setInventorySlotContents(0, null);
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        markDirty();
        player.syncSlots();
    }

    public void stopSwinging() {
        swingProgress = 0;
        swingMod = 0;
    }

    // Copied from ItemInWorldManager, seems to do the trick.
    private void stopBreaking() {
        player.theItemInWorldManager.updateBlockRemoving();
        isBreaking = false;
        int[] increase = LOC_INCREASES[(getBlockMetadata() & 7) - 2];
        int x = xCoord + increase[0];
        int y = yCoord;
        int z = zCoord + increase[1];
        worldObj.destroyBlockInWorldPartially(player.getEntityId(), x, y, z, -1);
    }

    /**
     * Logic: Casts a ray between the player and the middle of the AABB to determine the side and hitVec. Works for both
     * entities and blocks. Whichever is closer takes priority.
     * <p>
     * This is similar to how Minecraft calculates the MovingObjectPosition, but instead of yaw/pitch I use the middle
     * of the bounding box to cover edge where blocks have a weird bounding box.
     * <p>
     * hit.typeOfHit will be MISS if it's targeting the block below (this is done so that placing blocks still properly
     * works).
     */
    private void calculateHit() {
        int meta = getBlockMetadata();
        if (meta == 0) {
            ThaumicTinkerer.log
                    .error("Metadata of a Tool Dynamism tablet is in an invalid state. This is a critical error.");
            return;
        }
        int[] increase = LOC_INCREASES[(meta & 7) - 2];
        int x = xCoord + increase[0];
        int y = yCoord;
        int z = zCoord + increase[1];
        Block block = worldObj.getBlock(x, y, z);

        MovingObjectPosition hit = null;
        Vec3 position = positionVec;
        position.xCoord = xCoord + 0.5f;
        position.yCoord = yCoord + 0.5f;
        position.zCoord = zCoord + 0.5f;
        if (!worldObj.isAirBlock(x, y, z)) {
            AxisAlignedBB aabb = block.getSelectedBoundingBoxFromPool(worldObj, x, y, z);
            if (aabb != null) {
                hit = block.collisionRayTrace(worldObj, x, y, z, position, getMiddleOfAABB(aabb));
            }
        }
        List<Entity> entities = (List<Entity>) worldObj.getEntitiesWithinAABBExcludingEntity(
                player,
                AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1));
        if (!entities.isEmpty()) {
            double dist = hit == null ? Double.MAX_VALUE : position.squareDistanceTo(hit.hitVec);

            Entity targetedEntity = null;
            Vec3 hitVec = null;
            for (Entity e : entities) {
                if (e.canBeCollidedWith()) {

                    float borderSize = e.getCollisionBorderSize();
                    AxisAlignedBB aabb = e.boundingBox.expand(borderSize, borderSize, borderSize);
                    MovingObjectPosition intercept = aabb.calculateIntercept(position, getMiddleOfAABB(aabb));

                    if (aabb.isVecInside(position)) {
                        targetedEntity = e;
                        hitVec = intercept == null ? position : intercept.hitVec;
                        break;
                    }
                    if (intercept != null) {
                        double entityDist = position.squareDistanceTo(intercept.hitVec);

                        if (dist > entityDist) {
                            hitVec = intercept.hitVec;
                            targetedEntity = e;
                            dist = entityDist;
                        }
                    }
                }
            }

            if (targetedEntity != null) {
                hit = new MovingObjectPosition(targetedEntity, hitVec);
            }
        }

        if (hit == null) {
            // Use the block beneath
            y--;
            if (!worldObj.isAirBlock(x, y, z)) {
                AxisAlignedBB aabb = block.getSelectedBoundingBoxFromPool(worldObj, x, y, z);
                Vec3 vec = getMiddleOfAABB(aabb);
                vec.yCoord = (aabb.maxY - y);
                hit = new MovingObjectPosition(x, y, z, ForgeDirection.UP.ordinal(), vec, false);
            }
        }

        this.hit = hit;
    }

    private Vec3 getMiddleOfAABB(AxisAlignedBB aabb) {
        tempVec.xCoord = (aabb.minX + aabb.maxX) / 2f;
        tempVec.yCoord = (aabb.minY + aabb.maxY) / 2f;
        tempVec.zCoord = (aabb.minZ + aabb.maxZ) / 2f;
        return tempVec;
    }

    public boolean getIsBreaking() {
        return isBreaking;
    }

    public void initiateSwing() {
        // This calls receiveClientEvent on both sides.
        worldObj.addBlockEvent(xCoord, yCoord, zCoord, TTRegistry.dynamismTablet, 0, 0);
    }

    @Override
    public boolean receiveClientEvent(int id, int type) {
        if (id == 0) {
            swingMod = SWING_SPEED;
            swingProgress = 0;
            return true;
        }

        return tileEntityInvalid;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        swingProgress = tag.getByte(TAG_PROGRESS);
        swingMod = tag.getByte(TAG_MOD);

        readCustomNBT(tag);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        tag.setByte(TAG_PROGRESS, swingProgress);
        tag.setByte(TAG_MOD, swingMod);

        writeCustomNBT(tag);
    }

    public void readCustomNBT(NBTTagCompound tag) {
        leftClick = tag.getBoolean(TAG_LEFT_CLICK);
        redstone = tag.getBoolean(TAG_REDSTONE);
        NBTTagCompound itemTag = tag.getCompoundTag("Item");
        if (!itemTag.hasNoTags()) {
            heldItem = ItemStack.loadItemStackFromNBT(itemTag);
            return;
        }
        // Legacy Compatibility (there is no reason to store it as an array)
        NBTTagList tagList = tag.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        if (tagList.tagCount() != 0) {
            heldItem = ItemStack.loadItemStackFromNBT(tagList.getCompoundTagAt(0));
        }
    }

    @Override
    public S35PacketUpdateTileEntity getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeCustomNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, -999, tag);
    }

    @Override
    public void onDataPacket(NetworkManager manager, S35PacketUpdateTileEntity packet) {
        super.onDataPacket(manager, packet);
        readCustomNBT(packet.func_148857_g());
    }

    public void writeCustomNBT(NBTTagCompound tag) {
        tag.setBoolean(TAG_LEFT_CLICK, leftClick);
        tag.setBoolean(TAG_REDSTONE, redstone);

        if (heldItem != null) {
            tag.setTag("Item", heldItem.writeToNBT(new NBTTagCompound()));
        }
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        return i == 0 ? heldItem : null;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (index == 0 && heldItem != null) {
            ItemStack stackAt;

            if (heldItem.stackSize <= count) {
                stackAt = heldItem;
                heldItem = null;

            } else {
                stackAt = heldItem.splitStack(count);

                if (heldItem.stackSize == 0) heldItem = null;
            }
            if (!worldObj.isRemote) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            return stackAt;
        }

        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i) {
        return getStackInSlot(i);
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack) {
        if (i != 0) return;
        heldItem = itemstack;

        if (!worldObj.isRemote) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public String getInventoryName() {
        return LibBlockNames.ANIMATION_TABLET;
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return true;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && entityplayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public String getType() {
        return "tt_animationTablet";
    }

    @Override
    public String[] getMethodNames() {
        return new String[] { "getRedstone", "setRedstone", "getLeftClick", "setLeftClick", "getRotation",
                "setRotation", "hasItem", "trigger" };
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments)
            throws LuaException {
        return switch (method) {
            case 0 -> new Object[] { redstone };
            case 1 -> setRedstoneImplementation((Boolean) arguments[0]);
            case 2 -> new Object[] { leftClick };
            case 3 -> setLeftClickImplementation((Boolean) arguments[0]);
            case 4 -> new Object[] { getBlockMetadata() - 2 };
            case 5 -> setRotationImplementation((Double) arguments[0]);
            case 6 -> new Object[] { heldItem != null };
            case 7 -> triggerImplementation();
            default -> null;
        };
    }

    private Object[] triggerImplementation() {
        if (!isIdle()) return new Object[] { false };

        initiateSwing();

        return new Object[] { true };
    }

    @Optional.Method(modid = "ComputerCraft")
    private Object[] setRotationImplementation(Double argument) throws LuaException {
        int rotation = (int) argument.doubleValue();

        if (rotation > 3) throw new LuaException("Invalid value: " + rotation + ".");

        worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, rotation + 2, 1 | 2);
        return null;
    }

    private Object[] setLeftClickImplementation(Boolean argument) {
        this.leftClick = argument;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        return null;
    }

    private Object[] setRedstoneImplementation(Boolean argument) {
        this.redstone = argument;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        return null;
    }

    // OC INTEGRATION

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public void attach(IComputerAccess computer) {
        // NO-OP
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public void detach(IComputerAccess computer) {
        // NO-OP
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public boolean equals(IPeripheral other) {
        return this.equals((Object) other);
    }

    @Override
    @Optional.Method(modid = "appliedenergistics2")
    public boolean prepareToMove() {
        stopBreaking();
        return true;
    }

    @Override
    @Optional.Method(modid = "appliedenergistics2")
    public void doneMoving() {}

    @Override
    public String getComponentName() {
        return getType();
    }

    @Callback(doc = "function():boolean -- Returns Whether tablet is redstone activated")
    @Optional.Method(modid = "OpenComputers")
    @SuppressWarnings("unused")
    public Object[] getRedstone(Context context, Arguments args) {
        return new Object[] { redstone };
    }

    @Callback(doc = "function(boolean):Nil -- Sets Whether tablet is redstone activated")
    @Optional.Method(modid = "OpenComputers")
    @SuppressWarnings("unused")
    public Object[] setRedstone(Context context, Arguments args) {
        setRedstoneImplementation(args.checkBoolean(0));
        return new Object[] { redstone };
    }

    @Callback(doc = "function():boolean -- Returns Whether tablet Left clicks")
    @Optional.Method(modid = "OpenComputers")
    @SuppressWarnings("unused")
    public Object[] getLeftClick(Context context, Arguments args) {
        return new Object[] { leftClick };
    }

    @Callback(doc = "function(boolean):Nil -- Sets Whether tablet Left Clicks")
    @Optional.Method(modid = "OpenComputers")
    @SuppressWarnings("unused")
    public Object[] setLeftClick(Context context, Arguments args) {
        setLeftClickImplementation(args.checkBoolean(0));
        return new Object[] { leftClick };
    }

    // TODO {"hasItem", "trigger" };
    @Callback(doc = "function():number -- Returns tablet Rotation")
    @Optional.Method(modid = "OpenComputers")
    @SuppressWarnings("unused")
    public Object[] getRotation(Context context, Arguments args) {
        return new Object[] { getBlockMetadata() - 2 };
    }

    @Callback(doc = "function(number):Nil -- Sets tablet rotation")
    @Optional.Method(modid = "OpenComputers")
    @SuppressWarnings("unused")
    public Object[] setRotation(Context context, Arguments args) throws LuaException {
        setRotationImplementation((double) args.checkInteger(0));
        return new Object[] { getBlockMetadata() - 2 };
    }

    @Callback(doc = "function():boolean -- Returns wether tablet has an item or not")
    @Optional.Method(modid = "OpenComputers")
    @SuppressWarnings("unused")
    public Object[] hasItem(Context context, Arguments args) {
        return new Object[] { heldItem != null };
    }

    @Callback(doc = "function():Nil -- Triggers tablets swing")
    @Optional.Method(modid = "OpenComputers")
    @SuppressWarnings("unused")
    public Object[] trigger(Context context, Arguments args) {
        return triggerImplementation();
    }
}
