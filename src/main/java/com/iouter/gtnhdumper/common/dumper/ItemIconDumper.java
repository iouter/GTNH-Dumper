package com.iouter.gtnhdumper.common.dumper;

import bartworks.system.material.BWMetaGeneratedOres;
import com.gtnewhorizons.angelica.glsm.GLStateManager;
import com.iouter.gtnhdumper.GTNHDumper;
import com.iouter.gtnhdumper.Utils;
import com.iouter.gtnhdumper.common.utils.DynamicTexture;
import com.iouter.gtnhdumper.common.utils.FBOHelper;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.registry.GameData;
import gregtech.common.blocks.GTBlockOre;
import gregtech.common.items.ItemVolumetricFlask;
import gtPlusPlus.core.block.base.BlockBaseOre;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ItemIconDumper {

    private static final char[] BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int OUTPUT_LENGTH = 15;
    private static final BigInteger BASE = BigInteger.valueOf(BASE62.length);

    private static final Map<Integer, FBOHelper> fbos = new HashMap<>();

    private static Field frameCounterField;
    private static Field tickCounterField;
    private static Field animationMetadataField;

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
        if (nbt == null) return null;
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
            if (hashNBT == null) continue;
            String temp = hashMap.get(nbt);
            if (temp == null) {
                hashMap.put(nbt, hashNBT);
            } else if (!hashNBT.equals(temp)){
                collisionCount++;
                GTNHDumper.error("冲突发生在" + nbt + "，对应编码为：" + hashNBT);
            }
        }

        GTNHDumper.debug("冲突数量: " + collisionCount);
        GTNHDumper.debug("唯一率: " + (100 - (collisionCount * 100.0 / Math.max(1, itemStacks.size()))) + "%");
    }

    private static boolean isValidRender(ItemStack stack) {
        if (stack.getItem() instanceof ItemVolumetricFlask) {
            ItemVolumetricFlask flask = (ItemVolumetricFlask) stack.getItem();
            if (flask != null) {
                FluidStack fs = flask.getFluid(stack);
                if (fs != null) {
                    return fs.getFluid().getIcon(fs) == null;
                }
            }
        }
        return false;
    }

    public static void prepareRenderItem(ItemStack itemStack, RenderItem itemRenderer) {
        IIcon iIcon = itemStack.getIconIndex();
        if (iIcon == null) {
            iIcon = Objects.requireNonNull(itemStack.getItem()).getIconFromDamageForRenderPass(itemStack.getItemDamage(), 0);
        }

        int size;
        if (iIcon == null) {
            GTNHDumper.LOG.error("Can't find {}'s icon, set render size to 128", itemStack.getDisplayName());
            size = 128;
        } else {
            int width = iIcon.getIconWidth();
            int height = iIcon.getIconHeight();
            size = Math.max(width, height);
            if (itemStack.getItem() instanceof ItemBlock) {
                if (RenderBlocks.renderItemIn3d(((ItemBlock) itemStack.getItem()).field_150939_a.getRenderType())) {
                    size = 256;
                }
            }
        }

        FBOHelper fbo = fbos.get(size);
        if (fbo == null) {
            fbo = new FBOHelper(size);
            fbos.put(size, fbo);
        }
        renderGeneralItem(itemStack, fbo, itemRenderer);
    }

    /**
     * Created by Jerrell Fang on 2/23/2015.
     *
     * @author Meow J
     * <p>
     * Borrowed from <a href="https://github.com/Snownee/Item-Render-Dark/blob/master/src/main/java/itemrender/rendering/Renderer.java">Item-Render-Rebirth</a>
     */
    public static BufferedImage renderItem(ItemStack itemStack, FBOHelper fbo, RenderItem itemRenderer, float scale, DynamicTexture dynamicTexture) {
        Minecraft minecraft = FMLClientHandler.instance().getClient();
        fbo.begin();
        GLStateManager.glMatrixMode(GL11.GL_PROJECTION);
        GLStateManager.glPushMatrix();
        GLStateManager.glLoadIdentity();
        GLStateManager.glOrtho(0, 16, 0, 16, -150.0F, 150.0F);
        GLStateManager.glMatrixMode(GL11.GL_MODELVIEW);
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
        if (dynamicTexture != null) {
            dynamicTexture.updateAnimation();
        }
        GLStateManager.disableLighting();
        RenderHelper.disableStandardItemLighting();
        GLStateManager.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        fbo.end();
        if (itemStack.getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock) itemStack.getItem()).field_150939_a;
            if (block instanceof GTBlockOre || block instanceof BWMetaGeneratedOres || block instanceof BlockBaseOre) {
                return makeNonTransparentOpaque(fbo.saveToImage());
            }
        }
        return fbo.saveToImage();
    }

    public static void renderGeneralItem(ItemStack itemStack, FBOHelper fbo, RenderItem itemRenderer) {
        if (isValidRender(itemStack)) {
            return;
        }
        DynamicTexture dynamicTexture = new DynamicTexture(itemStack);
        if (dynamicTexture.isDynamic()) {
            renderDynamicItem(itemStack, fbo, itemRenderer, dynamicTexture);
            return;
        }
        BufferedImage image = renderItem(itemStack, fbo, itemRenderer, 1f, null);
        fbo.saveToFile(new File("dumps/icons/" + getIconFileNameInHuijiUpdater(itemStack)), image);
        fbo.restoreTexture();
    }

    public static void renderDynamicItem(ItemStack itemStack, FBOHelper fbo, RenderItem itemRenderer, DynamicTexture dynamicTexture){
        int frameCount = dynamicTexture.lcm;
        BufferedImage[] images = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            images[dynamicTexture.getIndex()] = renderItem(itemStack, fbo, itemRenderer, 1f, dynamicTexture);
        }
        BufferedImage image;
        if (Arrays.stream(images).anyMatch(Objects::isNull)) {
            GTNHDumper.LOG.warn("{}'s frames has null frame", itemStack.getDisplayName());
            image = concatenateImages(Arrays.stream(images).filter(Objects::nonNull).toArray(BufferedImage[]::new));
        } else {
            image = concatenateImages(images);
        }

        fbo.saveToFile(new File("dumps/icons/" + getIconFileNameInHuijiUpdater(itemStack)), image);
        fbo.restoreTexture();
    }

    /**
     * 横向拼接BufferedImage数组（自动处理高度不一致、透明度）
     *
     * @param images 要拼接的图像数组
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
            int y = (maxHeight - img.getHeight()) / 2;  // 垂直居中对齐
            g2d.drawImage(img, currentX, y, null);
            currentX += img.getWidth();
        }

        g2d.dispose();
        return result;
    }

    public static BufferedImage makeNonTransparentOpaque(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = img.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                if (alpha != 0) { // 不是完全透明
                    argb = (0xFF << 24) | (argb & 0x00FFFFFF);
                    img.setRGB(x, y, argb);
                }
            }
        }
        return img;
    }
}
