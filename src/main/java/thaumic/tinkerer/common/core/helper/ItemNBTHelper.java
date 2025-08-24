/**
 * This class was created by <Vazkii>. It's distributed as part of the ThaumicTinkerer Mod.
 *
 * ThaumicTinkerer is Open Source and distributed under a Creative Commons Attribution-NonCommercial-ShareAlike 3.0
 * License (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 *
 * ThaumicTinkerer is a Derivative Work on Thaumcraft 4. Thaumcraft 4 (c) Azanor 2012
 * (http://www.minecraftforum.net/topic/1585216-)
 *
 * File Created @ [8 Sep 2013, 19:36:25 (GMT)]
 */
package thaumic.tinkerer.common.core.helper;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public final class ItemNBTHelper {

    /**
     * Gets the NBTTagCompound in an ItemStack. Tries to init it previously in case there isn't one present *
     */
    public static NBTTagCompound getNBT(ItemStack stack) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        return stack.getTagCompound();
    }

    // SETTERS ///////////////////////////////////////////////////////////////////

    public static void setBoolean(ItemStack stack, String tag, boolean b) {
        getNBT(stack).setBoolean(tag, b);
    }

    public static void setByte(ItemStack stack, String tag, byte b) {
        getNBT(stack).setByte(tag, b);
    }

    public static void setShort(ItemStack stack, String tag, short s) {
        getNBT(stack).setShort(tag, s);
    }

    public static void setInt(ItemStack stack, String tag, int i) {
        getNBT(stack).setInteger(tag, i);
    }

    public static void setLong(ItemStack stack, String tag, long l) {
        getNBT(stack).setLong(tag, l);
    }

    public static void setFloat(ItemStack stack, String tag, float f) {
        getNBT(stack).setFloat(tag, f);
    }

    public static void setDouble(ItemStack stack, String tag, double d) {
        getNBT(stack).setDouble(tag, d);
    }

    public static void setCompound(ItemStack stack, String tag, NBTTagCompound cmp) {
        getNBT(stack).setTag(tag, cmp);
    }

    public static void setString(ItemStack stack, String tag, String s) {
        getNBT(stack).setString(tag, s);
    }

    // GETTERS ///////////////////////////////////////////////////////////////////

    public static boolean verifyExistance(ItemStack stack, String tag) {
        return getNBT(stack).hasKey(tag);
    }

    public static boolean getBoolean(ItemStack stack, String tag, boolean defaultExpected) {
        NBTTagCompound nbt = getNBT(stack);
        return nbt.hasKey(tag) ? nbt.getBoolean(tag) : defaultExpected;
    }

    public static byte getByte(ItemStack stack, String tag, byte defaultExpected) {
        NBTTagCompound nbt = getNBT(stack);
        return nbt.hasKey(tag) ? nbt.getByte(tag) : defaultExpected;
    }

    public static short getShort(ItemStack stack, String tag, short defaultExpected) {
        NBTTagCompound nbt = getNBT(stack);
        return nbt.hasKey(tag) ? nbt.getShort(tag) : defaultExpected;
    }

    public static int getInt(ItemStack stack, String tag, int defaultExpected) {
        NBTTagCompound nbt = getNBT(stack);
        return nbt.hasKey(tag) ? nbt.getInteger(tag) : defaultExpected;
    }

    public static long getLong(ItemStack stack, String tag, long defaultExpected) {
        NBTTagCompound nbt = getNBT(stack);
        return nbt.hasKey(tag) ? nbt.getLong(tag) : defaultExpected;
    }

    public static float getFloat(ItemStack stack, String tag, float defaultExpected) {
        NBTTagCompound nbt = getNBT(stack);
        return nbt.hasKey(tag) ? nbt.getFloat(tag) : defaultExpected;
    }

    public static double getDouble(ItemStack stack, String tag, double defaultExpected) {
        NBTTagCompound nbt = getNBT(stack);
        return nbt.hasKey(tag) ? nbt.getDouble(tag) : defaultExpected;
    }

    public static NBTTagCompound getCompound(ItemStack stack, String tag) {
        NBTTagCompound nbt = getNBT(stack);
        return nbt.hasKey(tag) ? nbt.getCompoundTag(tag) : new NBTTagCompound();
    }

    public static NBTTagCompound getCompoundOrNull(ItemStack stack, String tag) {
        NBTTagCompound nbt = getNBT(stack);
        return nbt.hasKey(tag) ? nbt.getCompoundTag(tag) : null;
    }

    public static String getString(ItemStack stack, String tag, String defaultExpected) {
        NBTTagCompound nbt = getNBT(stack);
        return nbt.hasKey(tag) ? nbt.getString(tag) : defaultExpected;
    }
}
