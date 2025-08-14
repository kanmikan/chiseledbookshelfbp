package com.mikanon.chiseledbookshelf.blocks;

import com.mikanon.chiseledbookshelf.Main;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class TileEntityChiseledBookshelfRenderer extends TileEntitySpecialRenderer {

    private static final ResourceLocation FULL_TEXTURE = new ResourceLocation(Main.MODID + ":textures/blocks/chiseled_bookshelf_occupied.png");
    
    private static final float QUAD_DEPTH = 15.98F / 16F;
    private static final float PIXEL_UNIT = 1.0F / 16.0F;
    private static final float BOOKW = 4.0F;
    private static final float BOOKH = 6.0F;
    private static final float HORIZONTAL_SPACING = 1.0F;
    private static final float VERTICAL_SPACING = 2.0F;
    private static final float BORDER = 1.0F;

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks) {
        TileEntityChiseledBookshelf bookshelf = (TileEntityChiseledBookshelf) tileEntity;

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        int meta = bookshelf.getBlockMetadata() % 4;

        GL11.glTranslatef(0.5F, 0F, 0.5F);
        GL11.glRotatef(meta * -90F, 0F, 1F, 0F);
        GL11.glTranslatef(-0.5F, 0.0F, -1.5F);

        this.bindTexture(FULL_TEXTURE);

        //GL11.glEnable(GL11.GL_BLEND); //necesario?
        //GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        renderBooks(bookshelf);

        //GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    private void renderBooks(TileEntityChiseledBookshelf bookshelf) {
        Tessellator tess = Tessellator.instance;
        
        World world = bookshelf.getWorldObj();
        int meta = bookshelf.getBlockMetadata() % 4;
        
        for (int i = 0; i < 6; i++) {
            if (bookshelf.hasBookInSlot(i)) {
                int col = i % 3;
                int row = i / 3;
                int columnIndex = 2 - col;
                
                float u1Tex = BORDER + col * (BOOKW + HORIZONTAL_SPACING);
                float u2Tex = u1Tex + BOOKW;
                float v1Tex = BORDER + row * (BOOKH + VERTICAL_SPACING);
                float v2Tex = v1Tex + BOOKH;

                float u1 = u1Tex * PIXEL_UNIT;
                float u2 = u2Tex * PIXEL_UNIT;
                float v1 = v1Tex * PIXEL_UNIT;
                float v2 = v2Tex * PIXEL_UNIT;

                float startX = BORDER * PIXEL_UNIT + columnIndex * (BOOKW + HORIZONTAL_SPACING) * PIXEL_UNIT;
                float endX = startX + BOOKW * PIXEL_UNIT;
                float offsetY = BORDER + row * (BOOKH + VERTICAL_SPACING);
                float topY = 1.0F - (offsetY * PIXEL_UNIT);
                float bottomY = topY - (BOOKH * PIXEL_UNIT);

                int frontX = bookshelf.xCoord, frontZ = bookshelf.zCoord;
                switch (meta) {
                    case 0: frontZ--; break;
                    case 1: frontX++; break;
                    case 2: frontZ++; break;
                    case 3: frontX--; break;
                }
                int brightness = world.getLightBrightnessForSkyBlocks(frontX, bookshelf.yCoord, frontZ, 0);

                tess.startDrawingQuads();
                tess.setBrightness(brightness);
                tess.setColorOpaque(255, 255, 255);
                tess.setNormal(0.0f, 0.0f, 1.0f);
                tess.addVertexWithUV(startX, topY, QUAD_DEPTH, u2, v1);
                tess.addVertexWithUV(endX, topY, QUAD_DEPTH, u1, v1);
                tess.addVertexWithUV(endX, bottomY, QUAD_DEPTH, u1, v2);
                tess.addVertexWithUV(startX, bottomY, QUAD_DEPTH, u2, v2);
                tess.draw();
            }
        }
    }

}

