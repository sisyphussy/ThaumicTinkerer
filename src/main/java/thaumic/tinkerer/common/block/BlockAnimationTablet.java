/**
 * This class was created by <Vazkii>. It's distributed as part of the ThaumicTinkerer Mod.
 *
 * ThaumicTinkerer is Open Source and distributed under a Creative Commons Attribution-NonCommercial-ShareAlike 3.0
 * License (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 *
 * ThaumicTinkerer is a Derivative Work on Thaumcraft 4. Thaumcraft 4 (c) Azanor 2012
 * (http://www.minecraftforum.net/topic/1585216-)
 *
 * File Created @ [9 Sep 2013, 15:52:53 (GMT)]
 */
package thaumic.tinkerer.common.block;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumic.tinkerer.client.core.helper.IconHelper;
import thaumic.tinkerer.common.ThaumicTinkerer;
import thaumic.tinkerer.common.block.tile.tablet.TileAnimationTablet;
import thaumic.tinkerer.common.lib.LibBlockNames;
import thaumic.tinkerer.common.lib.LibGuiIDs;
import thaumic.tinkerer.common.lib.LibResearch;
import thaumic.tinkerer.common.registry.TTRegistry;
import thaumic.tinkerer.common.registry.ThaumicTinkererArcaneRecipe;
import thaumic.tinkerer.common.registry.ThaumicTinkererRecipe;
import thaumic.tinkerer.common.research.IRegisterableResearch;
import thaumic.tinkerer.common.research.ResearchHelper;
import thaumic.tinkerer.common.research.TTResearchItem;

public class BlockAnimationTablet extends BlockModContainer {

    IIcon iconBottom;
    IIcon iconTop;
    IIcon iconSides;

    Random random;

    public BlockAnimationTablet() {
        super(Material.iron);
        setBlockBounds(0F, 0F, 0F, 1F, 1F / 16F * 2F, 1F);
        setHardness(3F);
        setResistance(50F);
        setStepSound(soundTypeMetal);

        random = new Random();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        iconBottom = IconHelper.forBlock(reg, this, 0);
        iconTop = IconHelper.forBlock(reg, this, 1);
        iconSides = IconHelper.forBlock(reg, this, 2);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        byte b0 = 0;
        int l1 = MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

        if (l1 == 0) b0 = 2;

        if (l1 == 1) b0 = 5;

        if (l1 == 2) b0 = 3;

        if (l1 == 3) b0 = 4;

        world.setBlockMetadataWithNotify(x, y, z, b0, 2);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block blockBroken, int meta) {
        TileAnimationTablet tablet = (TileAnimationTablet) world.getTileEntity(x, y, z);

        if (tablet != null) {
            ItemStack itemstack = tablet.heldItem;
            if (itemstack != null) {
                float f = random.nextFloat() * 0.8F + 0.1F;
                float f1 = random.nextFloat() * 0.8F + 0.1F;
                EntityItem entityitem;

                for (float f2 = random.nextFloat() * 0.8F + 0.1F; itemstack.stackSize > 0; world
                        .spawnEntityInWorld(entityitem)) {
                    int k1 = random.nextInt(21) + 10;

                    if (k1 > itemstack.stackSize) k1 = itemstack.stackSize;

                    itemstack.stackSize -= k1;
                    entityitem = new EntityItem(
                            world,
                            x + f,
                            y + f1,
                            z + f2,
                            new ItemStack(itemstack.getItem(), k1, itemstack.getItemDamage()));
                    float f3 = 0.05F;
                    entityitem.motionX = (float) random.nextGaussian() * f3;
                    entityitem.motionY = (float) random.nextGaussian() * f3 + 0.2F;
                    entityitem.motionZ = (float) random.nextGaussian() * f3;

                    if (itemstack.hasTagCompound())
                        entityitem.getEntityItem().setTagCompound((NBTTagCompound) itemstack.getTagCompound().copy());
                }
            }

            // Look here if something breaks in 1.7
            world.func_147453_f(x, y, z, blockBroken);
        }

        super.breakBlock(world, x, y, z, blockBroken, meta);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (world.isRemote) return;

        boolean power = world.isBlockIndirectlyGettingPowered(x, y, z)
                || world.isBlockIndirectlyGettingPowered(x, y + 1, z);
        int meta = world.getBlockMetadata(x, y, z);
        boolean on = (meta & 8) != 0;

        if (power && !on) {
            world.scheduleBlockUpdate(x, y, z, this, tickRate(world));
            world.setBlockMetadataWithNotify(x, y, z, meta | 8, 4);
        } else if (!power && on) world.setBlockMetadataWithNotify(x, y, z, meta & 7, 4);
    }

    @Override
    public int tickRate(World par1World) {
        return 1;
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileAnimationTablet tablet) {
            if (tablet.redstone && tablet.isIdle()) {
                tablet.initiateSwing();
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float subX,
            float subY, float subZ) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileAnimationTablet tablet) {
                if (player.getCurrentEquippedItem() != null
                        && player.getCurrentEquippedItem().getItem() instanceof ItemWandCasting) {
                    int meta = world.getBlockMetadata(x, y, z);
                    boolean activated = (meta & 8) != 0;
                    if (!activated && !tablet.getIsBreaking() && tablet.isIdle()) {
                        world.setBlockMetadataWithNotify(x, y, z, meta == 5 ? 2 : meta + 1, 1 | 2);
                        world.playSoundEffect(x, y, z, "thaumcraft:tool", 0.6F, 1F);
                    } else player.addChatMessage(new ChatComponentTranslation("ttmisc.animationTablet.notRotatable"));
                    // Rare chance this might happen, but better to cope for it.

                    return true;
                } else {
                    player.openGui(ThaumicTinkerer.instance, LibGuiIDs.GUI_ID_TABLET, world, x, y, z);
                }
            }
        }

        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return side == ForgeDirection.UP.ordinal() ? iconTop
                : side == ForgeDirection.DOWN.ordinal() ? iconBottom : iconSides;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileAnimationTablet();
    }

    @Override
    public ArrayList<Object> getSpecialParameters() {
        return null;
    }

    @Override
    public String getBlockName() {
        return LibBlockNames.ANIMATION_TABLET;
    }

    @Override
    public boolean shouldRegister() {
        return true;
    }

    @Override
    public boolean shouldDisplayInTab() {
        return true;
    }

    @Override
    public Class<? extends ItemBlock> getItemBlock() {
        return null;
    }

    @Override
    public Class<? extends TileEntity> getTileEntity() {
        return TileAnimationTablet.class;
    }

    @Override
    public IRegisterableResearch getResearchItem() {
        return (IRegisterableResearch) new TTResearchItem(
                LibResearch.KEY_ANIMATION_TABLET,
                new AspectList().add(Aspect.MECHANISM, 2).add(Aspect.METAL, 1).add(Aspect.MOTION, 1)
                        .add(Aspect.ENERGY, 1),
                -8,
                2,
                4,
                new ItemStack(TTRegistry.dynamismTablet)).setWarp(1).setParents(LibResearch.KEY_MAGNETS).setPages(
                        new ResearchPage("0"),
                        ResearchHelper.arcaneRecipePage(LibResearch.KEY_ANIMATION_TABLET));
    }

    @Override
    public ThaumicTinkererRecipe getRecipeItem() {
        return new ThaumicTinkererArcaneRecipe(
                LibResearch.KEY_ANIMATION_TABLET,
                LibResearch.KEY_ANIMATION_TABLET,
                new ItemStack(TTRegistry.dynamismTablet),
                new AspectList().add(Aspect.AIR, 25).add(Aspect.ORDER, 15).add(Aspect.FIRE, 10),
                "GIG",
                "ICI",
                'G',
                new ItemStack(Items.gold_ingot),
                'I',
                new ItemStack(Items.iron_ingot),
                'C',
                new ItemStack(ConfigItems.itemGolemCore, 1, 100));
    }
}
