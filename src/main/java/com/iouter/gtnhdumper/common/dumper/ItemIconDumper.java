package com.iouter.gtnhdumper.common.dumper;

import bartworks.system.material.BWMetaGeneratedOres;
import com.gtnewhorizons.angelica.glsm.GLStateManager;
import com.iouter.gtnhdumper.CommonProxy;
import com.iouter.gtnhdumper.GTNHDumper;
import com.iouter.gtnhdumper.common.utils.DynamicTexture;
import com.iouter.gtnhdumper.common.utils.FBOHelper;
import com.iouter.gtnhdumper.common.utils.Utils;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.registry.GameData;
import gregtech.common.blocks.GTBlockOre;
import gregtech.common.items.ItemVolumetricFlask;
import gtPlusPlus.core.block.base.BlockBaseOre;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ItemIconDumper {

    private static final Object2IntOpenHashMap<ItemStack> frameCountMap = new Object2IntOpenHashMap<>();
    private static final Map<Integer, FBOHelper> fbos = new HashMap<>();

    private ItemIconDumper() {
    }

    public static String getIconFileName(ItemStack stack) {
        return getIconFileName(stack, false);
    }

    public static String getIconFileName(ItemStack stack, boolean isDynamic) {
        return "icon_" + Utils.replaceHuijiIllegalChars(Utils.getItemStackShortKey(stack)) + (isDynamic ? "_dynamic" : "") + ".png";
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
            String hashNBT = Utils.hashNBT(stack);
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
        if (CommonProxy.isGTLoaded && stack.getItem() instanceof ItemVolumetricFlask) {
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
            if (CommonProxy.isGTLoaded && (block instanceof GTBlockOre || block instanceof BWMetaGeneratedOres || block instanceof BlockBaseOre)) {
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
        if (!getIconFileName(itemStack).contains("Botania:prismarine") && dynamicTexture.isDynamic()) {
            renderDynamicItem(itemStack, fbo, itemRenderer, dynamicTexture);
            return;
        }
        frameCountMap.put(itemStack, 1);
        BufferedImage image = renderItem(itemStack, fbo, itemRenderer, 1f, null);
        FBOHelper.saveToFile(new File("dumps/icons/" + getIconFileName(itemStack)), image);
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
            images = Arrays.stream(images).filter(Objects::nonNull).toArray(BufferedImage[]::new);
        }
        image = concatenateImages(images);
        frameCountMap.put(itemStack, image.getWidth() / image.getHeight());
        FBOHelper.saveToFile(new File("dumps/icons/" + getIconFileName(itemStack)), images[0]);
        FBOHelper.saveToFile(new File("dumps/icons/" + getIconFileName(itemStack, true)), image);
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

        if (deduplicateByPixel(images)) {
            return images[0];
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

    /**
     * 如果数组中所有图像像素完全相同，返回仅包含第一个元素的数组；否则返回原数组。
     *
     * @param images BufferedImage 数组
     * @return 是否有重复
     */
    public static boolean deduplicateByPixel(BufferedImage[] images) {
        if (images == null || images.length <= 1) {
            return true;
        }

        BufferedImage first = images[0];
        // 如果第一张为 null，则直接返回原数组（或根据需求处理）
        if (first == null) {
            return false;
        }

        int width = first.getWidth();
        int height = first.getHeight();

        // 逐张比较
        for (int i = 1; i < images.length; i++) {
            BufferedImage curr = images[i];
            if (curr == null) {
                return false; // 存在 null，视为不同
            }
            // 尺寸不一致直接判定不同
            if (curr.getWidth() != width || curr.getHeight() != height) {
                return false;
            }
            // 快速比较：获取像素数组整数表示
            int[] firstPixels = first.getRGB(0, 0, width, height, null, 0, width);
            int[] currPixels = curr.getRGB(0, 0, width, height, null, 0, width);
            if (!Arrays.equals(firstPixels, currPixels)) {
                return false;
            }
        }

        // 全部相同，返回只含第一张的数组
        return true;
    }

    public static int getItemFrameCount(ItemStack stack) {
        return frameCountMap.getInt(stack);
    }
}
