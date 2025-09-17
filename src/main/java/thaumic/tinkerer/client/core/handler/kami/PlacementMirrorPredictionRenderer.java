package thaumic.tinkerer.client.core.handler.kami;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import thaumic.tinkerer.common.item.kami.ItemPlacementMirror;
import thaumic.tinkerer.common.registry.TTRegistry;

public final class PlacementMirrorPredictionRenderer {

    private final RenderBlocks blockRender;

    public PlacementMirrorPredictionRenderer() {
        this.blockRender = new RenderBlocks();
        blockRender.useInventoryTint = false;
    }

    @SubscribeEvent
    public void onWorldRenderLast(RenderWorldLastEvent event) {
        World world = Minecraft.getMinecraft().theWorld;
        List<EntityPlayer> playerEntities = world.playerEntities;
        for (EntityPlayer player : playerEntities) {
            ItemStack currentStack = player.getCurrentEquippedItem();
            if (currentStack != null && currentStack.getItem() == TTRegistry.itemPlacementMirror) {
                renderPlayerLook(player, currentStack);
            }
        }
    }

    private void renderPlayerLook(EntityPlayer player, ItemStack stack) {
        Block requiredBlock = ItemPlacementMirror.getBlock(stack);
        if (requiredBlock == null) return;
        int requiredMetadata = ItemPlacementMirror.getBlockMeta(stack);
        List<ChunkCoordinates> coords = ItemPlacementMirror.getBlocksToPlace(stack, player);
        if (ItemPlacementMirror.hasBlocks(player, requiredBlock, requiredMetadata, coords.size())) {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(1F, 1F, 1F, 0.6F);
            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            for (ChunkCoordinates coord : coords) {
                renderBlockAt(requiredBlock, requiredMetadata, coord);
            }
            GL11.glColor4f(1F, 1F, 1F, 1F);
        }
    }

    private void renderBlockAt(Block block, int metadata, ChunkCoordinates pos) {
        GL11.glPushMatrix();
        GL11.glTranslated(
                pos.posX + 0.5 - RenderManager.renderPosX,
                pos.posY + 0.5 - RenderManager.renderPosY,
                pos.posZ + 0.5 - RenderManager.renderPosZ);
        blockRender.renderBlockAsItem(block, metadata, 1F);
        GL11.glPopMatrix();
    }
}
