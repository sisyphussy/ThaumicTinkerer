package thaumic.tinkerer.client.render.block;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import thaumcraft.api.aspects.Aspect;
import thaumic.tinkerer.client.lib.LibRenderIDs;
import thaumic.tinkerer.common.block.BlockInfusedGrain;

/**
 * Created by pixlepix on 8/4/14.
 */
public class RenderInfusedCrops implements ISimpleBlockRenderingHandler {

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {}

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
            RenderBlocks renderer) {
        GL11.glPushMatrix();
        Aspect aspect = BlockInfusedGrain.getAspect(world, x, y, z);

        GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT);
        final Tessellator tess = Tessellator.instance;
        if (aspect != null && !aspect.isPrimal()) {
            // Hex to RGB code from vanilla tesselator
            float r = (aspect.getColor() >> 16 & 0xFF) / 255.0F;
            float g = (aspect.getColor() >> 8 & 0xFF) / 255.0F;
            float b = (aspect.getColor() & 0xFF) / 255.0F;

            GL11.glColor4f(r, g, b, 1F);
            tess.setColorRGBA_I(aspect.getColor(), 255);
        }

        final int metadata = world.getBlockMetadata(x, y, z);
        renderer.setOverrideBlockTexture(block.getIcon(world, x, y, z, metadata));
        tess.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
        renderer.renderBlockCropsImpl(block, metadata, x, y - 0.0625F, z);
        renderer.clearOverrideBlockTexture();
        // tess.setColorOpaque_I(0xFFFFFF);

        GL11.glPopAttrib();
        GL11.glPopMatrix();
        return true;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return false;
    }

    @Override
    public int getRenderId() {
        return LibRenderIDs.idGrain;
    }
}
