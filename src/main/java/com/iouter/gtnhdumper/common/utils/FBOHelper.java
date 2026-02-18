package com.iouter.gtnhdumper.common.utils;

import com.gtnewhorizons.angelica.glsm.GLStateManager;
import com.iouter.gtnhdumper.GTNHDumper;
import net.minecraft.client.renderer.GLAllocation;
import org.apache.commons.codec.binary.Base64;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.glu.GLU;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

/**
 * Copyright (c) 2015 Jerrell Fang
 * <p>
 * This project is Open Source and distributed under The <a href="http://opensource.org/licenses/MIT">MIT</a> License
 * <p>
 * You should have received a copy of the MIT License along with
 * this project.   If not, see <http://opensource.org/licenses/MIT>.
 * <p>
 * Borrowed from <a href="https://github.com/Snownee/Item-Render-Dark/blob/master/src/main/java/itemrender/rendering/FBOHelper.java">Item-Render-Rebirth</a>
 */
public final class FBOHelper {
    private int renderTextureSize = 128;
    private int framebufferID = -1;
    private int depthbufferID = -1;
    private int textureID = -1;

    private IntBuffer lastViewport;
    private int lastTexture;
    private int lastFramebuffer;

    public FBOHelper(int textureSize) {
        this.renderTextureSize = textureSize;
        createFramebuffer();
    }

    private static void checkGlErrors(String message) {
        int error = GL11.glGetError();

        if (error != 0) {
            String error_name = GLU.gluErrorString(error);
            GTNHDumper.error("########## GL ERROR ##########");
            GTNHDumper.error("@ " + message);
            GTNHDumper.error(error + ": " + error_name);
        }
    }

    public void resize(int newSize) {
        deleteFramebuffer();
        renderTextureSize = newSize;
        createFramebuffer();
    }

    public void begin() {
        checkGlErrors("FBO Begin Init");

        // Remember current framebuffer.
        lastFramebuffer = GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);

        // Render to our texture
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, framebufferID);

        // Remember viewport info.
        lastViewport = GLAllocation.createDirectIntBuffer(16);
        GL11.glGetInteger(GL11.GL_VIEWPORT, lastViewport);
        GL11.glViewport(0, 0, renderTextureSize, renderTextureSize);

        GLStateManager.glMatrixMode(GL11.GL_MODELVIEW);
        GLStateManager.glPushMatrix();
        GLStateManager.glLoadIdentity();

        // Remember current texture.
        lastTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

        GLStateManager.glClearColor(0, 0, 0, 0);
        GLStateManager.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GL11.glCullFace(GL11.GL_FRONT);
        GLStateManager.enableDepthTest();
        GLStateManager.enableLighting();
        GLStateManager.enableRescaleNormal();

        checkGlErrors("FBO Begin Final");
    }

    public void end() {
        checkGlErrors("FBO End Init");

        GL11.glCullFace(GL11.GL_BACK);
        GLStateManager.disableDepthTest();
        GLStateManager.disableRescaleNormal();
        GLStateManager.disableLighting();

        GLStateManager.glMatrixMode(GL11.GL_MODELVIEW);
        GLStateManager.glPopMatrix();

        // Revert to last viewport
        GL11.glViewport(lastViewport.get(0), lastViewport.get(1), lastViewport.get(2), lastViewport.get(3));

        // Revert to default framebuffer
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, lastFramebuffer);

        // Revert to last texture
        GLStateManager.glBindTexture(GL11.GL_TEXTURE_2D, lastTexture);

        checkGlErrors("FBO End Final");
    }

    public void bind() {
        GLStateManager.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
    }

    // This is only a separate function because the texture gets messed with
    // after you're done rendering to read the FBO
    public void restoreTexture() {
        GLStateManager.glBindTexture(GL11.GL_TEXTURE_2D, lastTexture);
    }

    public BufferedImage saveToImage() {
        // Bind framebuffer texture
        GLStateManager.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

        int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);

        IntBuffer texture = BufferUtils.createIntBuffer(width * height);
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, texture);

        int[] texture_array = new int[width * height];
        texture.get(texture_array);

        BufferedImage image = new BufferedImage(renderTextureSize, renderTextureSize, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, renderTextureSize, renderTextureSize, texture_array, 0, width);
        return image;
    }

    public void saveToFile(File file, BufferedImage image) {
        file.mkdirs();

        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    String getBase64() {
        BufferedImage image = saveToImage();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "PNG", out);
        } catch (IOException e) {
            // Do nothing
        }

        return Base64.encodeBase64String(out.toByteArray());
    }

    private void createFramebuffer() {
        framebufferID = EXTFramebufferObject.glGenFramebuffersEXT();
        textureID = GL11.glGenTextures();
        int currentFramebuffer = GLStateManager.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);
        int currentTexture = GLStateManager.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, framebufferID);

        // Set our texture up, empty.
        GLStateManager.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        GLStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GLStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GLStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GLStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GLStateManager.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, renderTextureSize, renderTextureSize, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);

        // Restore old texture
        GLStateManager.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture);

        // Create depth buffer
        depthbufferID = EXTFramebufferObject.glGenRenderbuffersEXT();
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, depthbufferID);
        EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, GL11.GL_DEPTH_COMPONENT, renderTextureSize, renderTextureSize);

        // Bind depth buffer to the framebuffer
        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, depthbufferID);

        // Bind our texture to the framebuffer
        EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GL11.GL_TEXTURE_2D, textureID, 0);

        // Revert to default framebuffer
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, currentFramebuffer);
    }

    private void deleteFramebuffer() {
        EXTFramebufferObject.glDeleteFramebuffersEXT(framebufferID);
        GLStateManager.glDeleteTextures(textureID);
        EXTFramebufferObject.glDeleteRenderbuffersEXT(depthbufferID);
    }
}
