package com.austinv11.peripheralsplusplus.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * mag_reader_writer - Rolando Islas
 * Created using Tabula 7.0.0
 */
public class ModelMagReaderWriter extends ModelBase {
    public ModelRenderer base;
    public ModelRenderer left;
    public ModelRenderer right;

    public ModelMagReaderWriter() {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.right = new ModelRenderer(this, 0, 15);
        this.right.setRotationPoint(1.0F, 19.0F, -6.0F);
        this.right.addBox(0.0F, 0.0F, 0.0F, 4, 2, 12, 0.0F);
        this.left = new ModelRenderer(this, 30, 3);
        this.left.setRotationPoint(-4.0F, 19.0F, -6.0F);
        this.left.addBox(0.0F, 0.0F, 0.0F, 4, 2, 12, 0.0F);
        this.base = new ModelRenderer(this, 0, 0);
        this.base.setRotationPoint(-4.0F, 21.0F, -6.0F);
        this.base.addBox(0.0F, 0.0F, 0.0F, 9, 3, 12, 0.0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) { 
        this.right.render(f5);
        this.left.render(f5);
        this.base.render(f5);
    }

    /**
     * This is a helper function from Tabula to set the rotation of model parts
     */
    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
