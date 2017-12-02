package com.austinv11.peripheralsplusplus.client.models;

import com.austinv11.peripheralsplusplus.init.ModItems;
import com.austinv11.peripheralsplusplus.reference.Reference;
import com.austinv11.peripheralsplusplus.tiles.TileEntityMagReaderWriter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderMagReaderWriter extends TileEntitySpecialRenderer<TileEntityMagReaderWriter> {

    private ModelMagReaderWriter model = new ModelMagReaderWriter();

    @Override
    public void render(TileEntityMagReaderWriter te, double x, double y, double z, float partialTicks,
                       int destroyStage, float alpha) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5f, (float) y + 1.2f, (float) z + .5f);
        GL11.glRotatef(180, 0, 0, 1);
        GL11.glRotatef(0, 0, 1, 0);
        GL11.glScalef(.5f, .5f, .5f);
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(
                Reference.MOD_ID.toLowerCase()+":textures/models/mag_reader_writer.png"));
        model.render(null, 0, 0, 0, 0, 0, .1f);
        GL11.glPopMatrix();
    }

    public static class RenderMagReaderWriterItem extends TileEntityItemStackRenderer {
        private static TileEntityMagReaderWriter magReaderWriter = new TileEntityMagReaderWriter();
        private final TileEntityItemStackRenderer previousInstance;

        public RenderMagReaderWriterItem(TileEntityItemStackRenderer instance) {
            previousInstance = instance;
        }

        @Override
        public void renderByItem(ItemStack stack, float partialTicks) {
            if (!stack.getItem().equals(ModItems.MAG_READER_WRITER)) {
                previousInstance.renderByItem(stack, partialTicks);
                return;
            }
            TileEntityRendererDispatcher.instance.render(magReaderWriter, 0, 0, 0, partialTicks);
        }
    }
}
