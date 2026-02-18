package com.iouter.gtnhdumper.common.dumper;

import com.gtnewhorizons.angelica.glsm.GLStateManager;
import com.iouter.gtnhdumper.GTNHDumper;
import com.iouter.gtnhdumper.Utils;
import com.iouter.gtnhdumper.common.utils.FBOHelper;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemIconDumper {

    private static final char[] BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int OUTPUT_LENGTH = 15;
    private static final BigInteger BASE = BigInteger.valueOf(BASE62.length);

    private ItemIconDumper() {
    }

    public static String getIconFileName(ItemStack stack) {
        StringBuilder sb = new StringBuilder();
        sb.append("ICON");
        sb.append("_");
        sb.append(Utils.getItemKey(stack));
        String hashNBT = hashNBT(stack);
        if (hashNBT != null) {
             sb.append('_');
             sb.append(hashNBT);
        }
        sb.append(".png");
        return sb.toString();
    }

    public static String getIconFileNameInHuijiUpdater(ItemStack stack) {
        return Utils.replaceIllegalChars(getIconFileName(stack));
    }

    public static String hashNBT(ItemStack stack) {
        String nbt = Utils.getItemNBT(stack);
        if (nbt == null) {
            return null;
        }
        try {
            byte[] fullHash = MessageDigest.getInstance("SHA-256")
                .digest(nbt.getBytes(StandardCharsets.UTF_8));

            byte[] folded = new byte[16];
            for (int i = 0; i < 16; i++) {
                folded[i] = (byte) (fullHash[i] ^ fullHash[i + 16]);
            }

            BigInteger value = new BigInteger(1, folded);
            char[] buffer = new char[OUTPUT_LENGTH];
            for (int i = OUTPUT_LENGTH - 1; i >= 0; i--) {
                BigInteger[] div = value.divideAndRemainder(BASE);
                buffer[i] = BASE62[div[1].intValue()];
                value = div[0];
            }
            return new String(buffer);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void testHashNBT() {
        Map<String, String> hashMap = new HashMap<>();
        int collisionCount = 0;
        List<ItemStack> itemStacks = new ArrayList<>();

        for (Object temp : GameData.getItemRegistry()) {
            if (!(temp instanceof Item)) continue;
            Item item = (Item) temp;
            List<ItemStack> sub = new ArrayList<>();
            item.getSubItems(item, CreativeTabs.tabAllSearch, sub);
            itemStacks.addAll(sub);
        }

        for (ItemStack stack : itemStacks) {
            String nbt = Utils.getItemNBT(stack);
            String hashNBT = hashNBT(stack);
            if (hashNBT == null) {
                continue;
            }
            String temp = hashMap.get(nbt);
            if (temp == null) {
                hashMap.put(nbt, hashNBT);
            } else if (!hashNBT.equals(temp)){
                collisionCount++;
                GTNHDumper.error("冲突发生在" + nbt + "，对应编码为：" + hashNBT);
            }
        }

        GTNHDumper.debug("冲突数量: " + collisionCount);
        GTNHDumper.debug("唯一率: " + (100 - (collisionCount * 100.0 / 200_000)) + "%");
    }

    /**
     * Created by Jerrell Fang on 2/23/2015.
     *
     * @author Meow J
     * <p>
     * Borrowed from <a href="https://github.com/Snownee/Item-Render-Dark/blob/master/src/main/java/itemrender/rendering/Renderer.java>Item-Render-Rebirth</a>
     */
    public static void renderItem(ItemStack itemStack, FBOHelper fbo, RenderItem itemRenderer) {
        Minecraft minecraft = FMLClientHandler.instance().getClient();
        float scale = 1f;

        // 【关键】记录初始矩阵堆栈深度（投影矩阵）
        int initialStackDepth = GL11.glGetInteger(GL11.GL_PROJECTION_STACK_DEPTH);

        fbo.begin();
        try {
            GLStateManager.glMatrixMode(GL11.GL_PROJECTION);
            GLStateManager.glPushMatrix();
            GLStateManager.glLoadIdentity();
            GLStateManager.glOrtho(0, 16, 0, 16, -150.0F, 150.0F);

            GLStateManager.glMatrixMode(GL11.GL_MODELVIEW);
            GLStateManager.glPushMatrix(); // 【新增】保护模型视图矩阵
            try {
                GLStateManager.glLoadIdentity(); // 【新增】重置模型视图

                RenderHelper.enableGUIStandardItemLighting();
                GLStateManager.enableRescaleNormal();
                GLStateManager.enableColorMaterial();
                GLStateManager.enableLighting();

                GLStateManager.glTranslated(8 * (1 - scale), 8 * (1 - scale), 0);
                GLStateManager.glScaled(scale, scale, scale);

                itemRenderer.renderItemAndEffectIntoGUI(
                    minecraft.fontRenderer,
                    minecraft.renderEngine,
                    itemStack,
                    0,
                    0
                );

                GLStateManager.disableLighting();
                RenderHelper.disableStandardItemLighting();
            } finally {
                // 【关键】确保模型视图矩阵恢复
                GLStateManager.glMatrixMode(GL11.GL_MODELVIEW);
                GLStateManager.glPopMatrix();
            }

            // 【关键】确保投影矩阵恢复
            GLStateManager.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPopMatrix();

        } catch (Exception e) {
            e.printStackTrace();
            // 【关键】异常时强制恢复堆栈到安全状态
            forceRestoreMatrixStack(GL11.GL_PROJECTION, initialStackDepth);
            forceRestoreMatrixStack(GL11.GL_MODELVIEW, 1); // 模型视图通常深度为1
        } finally {
            int error = GL11.glGetError();

            if (error != 0) {
                GTNHDumper.error(itemStack.getDisplayName());
                GTNHDumper.error(getIconFileName(itemStack));
            }
            fbo.end();
        }

        BufferedImage image = null;

//        IIcon iIcon = itemStack.getIconIndex();
//        if (iIcon instanceof TextureAtlasSprite) {
//            TextureAtlasSprite texture = (TextureAtlasSprite) iIcon;
//            if (texture.hasAnimationMetadata()) {
//                AnimationMetadataSection animation = texture.animationMetadata;
//                int frameSize = animation.getFrameCount() * animation.getFrameTime();
//                BufferedImage[] images = new BufferedImage[frameSize];
//                for (int i = 0; i < frameSize; i++) {
//                    images[i] = fbo.saveToImage();
//                    texture.updateAnimation();
//                }
//                image = concatenateImages(images);
//            }
//        }

        if (image == null) {
            image = fbo.saveToImage();
        }

        fbo.saveToFile(new File("dumps/icons/" + getIconFileNameInHuijiUpdater(itemStack)), image);
        fbo.restoreTexture();
    }

    // 【新增】安全恢复矩阵堆栈的工具方法
    private static void forceRestoreMatrixStack(int matrixMode, int targetDepth) {
        try {
            GLStateManager.glMatrixMode(matrixMode);
            int currentDepth = GL11.glGetInteger(
                matrixMode == GL11.GL_PROJECTION ?
                    GL11.GL_PROJECTION_STACK_DEPTH :
                    GL11.GL_MODELVIEW_STACK_DEPTH
            );

            // 弹出多余矩阵直到达到目标深度（保留至少1层）
            while (currentDepth > targetDepth && currentDepth > 1) {
                GL11.glPopMatrix();
                currentDepth--;
            }
        } catch (Exception e) {
            // 即使恢复失败也不应阻塞主线程
            System.err.println("Failed to restore matrix stack for mode " + matrixMode);
        }
    }

    /**
     * 横向拼接BufferedImage数组（自动处理高度不一致、透明度）
     *
     * @param images        要拼接的图像数组
     * @return 拼接后的大图
     */
    public static BufferedImage concatenateImages(BufferedImage[] images) {
        if (images == null || images.length == 0) {
            throw new IllegalArgumentException("Image array cannot be empty");
        }

        // 1. 计算目标图像尺寸
        int totalWidth = Arrays.stream(images).mapToInt(BufferedImage::getWidth).sum();
        int maxHeight = Arrays.stream(images).mapToInt(BufferedImage::getHeight).max().orElse(0);

        // 2. 创建带透明通道的目标图像 (ARGB)
        BufferedImage result = new BufferedImage(totalWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();

        // 关键渲染设置（消除锯齿，保持透明度）
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setComposite(AlphaComposite.Src); // 保持源图像透明度

        // 3. 逐个绘制图像
        int currentX = 0;
        for (BufferedImage img : images) {
            int y = 0;
            g2d.drawImage(img, currentX, y, null);
            currentX += img.getWidth();
        }

        g2d.dispose();
        return result;
    }
}
