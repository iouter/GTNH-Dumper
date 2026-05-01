package com.iouter.gtnhdumper.common.utils;

import cpw.mods.fml.relauncher.ReflectionHelper;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DynamicTexture {

    private static final Field frameCounterField = ReflectionHelper.findField(TextureAtlasSprite.class, "frameCounter", "field_110973_g");
    private static final Field tickCounterField = ReflectionHelper.findField(TextureAtlasSprite.class, "tickCounter", "field_110983_h");
    private static final Field animationMetadataField = ReflectionHelper.findField(TextureAtlasSprite.class, "animationMetadata", "field_110982_k");

    public final Set<TextureAtlasSprite> textures = new HashSet<>();
    public final Object2IntOpenHashMap<TextureAtlasSprite> textureSizeMap = new Object2IntOpenHashMap<>();
    public final Map<TextureAtlasSprite, int[]> framesArrayMap = new HashMap<>();
    public final Object2IntOpenHashMap<TextureAtlasSprite> framesCountMap = new Object2IntOpenHashMap<>();
    public int lcm = 1;

    public DynamicTexture(ItemStack stack) {
        this(getTextures(stack));
    }

    public DynamicTexture(TextureAtlasSprite... textures) {
        for (TextureAtlasSprite texture : textures) {
            if (getAnimationMetadata(texture) != null && texture.getFrameCount() > 1) {
                this.textures.add(texture);
            }
        }
        for (TextureAtlasSprite texture : this.textures) {
            AnimationMetadataSection animation = getAnimationMetadata(texture);
            int textureSize = animation.getFrameCount() == 0 ? texture.getFrameCount() : animation.getFrameCount();
            textureSizeMap.put(texture, textureSize);
            int[] framesArray = new int[textureSize];
            for (int i = 0; i < framesArray.length; i++) {
                framesArray[i] = animation.getFrameTimeSingle(i);
            }
            framesArrayMap.put(texture, framesArray);
            int sum = Arrays.stream(framesArray).sum();
            framesCountMap.put(texture, sum);
        }
        if (!this.textures.isEmpty()) {
            BigInteger bigIntegerLCM = BigInteger.ONE;
            for (int sum : framesCountMap.values()) {
                BigInteger bSum = BigInteger.valueOf(sum);
                bigIntegerLCM = bigIntegerLCM.divide(bigIntegerLCM.gcd(bSum)).multiply(bSum);
            }
            lcm = bigIntegerLCM.intValue();
        }
    }

    public void updateAnimation() {
        textures.forEach(TextureAtlasSprite::updateAnimation);
    }

    public int getTextureIndex(TextureAtlasSprite texture) {
        int frameCounter = getFrameCount(texture);
        int tickCounter = getTickCounter(texture);
        int sum = 0;
        int[] frames = framesArrayMap.get(texture);
        for (int i = 0; i < frameCounter; i++) {
            sum += frames[i];
        }
        sum += tickCounter;
        return sum;
    }

    public int getIndex() {
        if (textures.isEmpty()) return 0;
        if (textures.size() == 1) {
            TextureAtlasSprite tex = textures.iterator().next();
            return getTextureIndex(tex);
        }

        long curMod = -1, curRem = -1;
        for (TextureAtlasSprite tex : textures) {
            long period = framesCountMap.getInt(tex);   // 周期长度
            long remainder = getTextureIndex(tex);      // 当前余数
            if (curMod == -1) {
                curMod = period;
                curRem = remainder % curMod;
            } else {
                long[] res = merge(curRem, curMod, remainder, period);
                if (res == null) {
                    // 无解：理论上动画纹理不会无解，但若出现可记录日志并返回-1
                    return -1;
                }
                curMod = res[0];
                curRem = res[1];
            }
        }
        return (int) curRem;
    }

    private static TextureAtlasSprite[] getTextures(ItemStack stack) {
        List<TextureAtlasSprite> textures = new ArrayList<>();
        Item item = stack.getItem();
        if (item instanceof ItemBlock) {
            Block block = ((ItemBlock) item).field_150939_a;
            if (RenderBlocks.renderItemIn3d(block.getRenderType())) {
                for (int i = 0; i < 6; i++) {
                    IIcon icon = block.getIcon(i, stack.getItemDamage());
                    if (icon instanceof TextureAtlasSprite) {
                        textures.add((TextureAtlasSprite) icon);
                    }
                }
            }

        } else {
            int passes= stack.getItem().getRenderPasses(stack.getItemDamage());
            if (passes == 1) {
                IIcon icon = stack.getIconIndex();
                if (icon instanceof TextureAtlasSprite) {
                    textures.add((TextureAtlasSprite) icon);
                }
            } else {
                for (int i = 0; i < passes; i++) {
                    IIcon icon = stack.getItem().getIconFromDamageForRenderPass(stack.getItemDamage(), i);
                    if (icon instanceof TextureAtlasSprite) {
                        textures.add((TextureAtlasSprite) icon);
                    }
                }
            }
        }
        return textures.toArray(new TextureAtlasSprite[0]);
    }

    public static AnimationMetadataSection getAnimationMetadata(TextureAtlasSprite textureAtlasSprite) {
        try {
            return (AnimationMetadataSection) animationMetadataField.get(textureAtlasSprite);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getFrameCount(TextureAtlasSprite textureAtlasSprite) {
        try {
            return frameCounterField.getInt(textureAtlasSprite);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getTickCounter(TextureAtlasSprite textureAtlasSprite) {
        try {
            return tickCounterField.getInt(textureAtlasSprite);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isDynamic() {
        return !this.textures.isEmpty();
    }

    private long[] merge(long a, long m, long b, long n) {
        long[] egcd = egcd(m, n);
        long d = egcd[0];
        long r = b - a;
        if (r % d != 0) return null; // 无解
        long m1 = m / d;
        long n1 = n / d;
        long r1 = r / d;
        long inv = modInv(m1, n1);
        long t0 = (r1 * inv) % n1;
        long newMod = m * n1; // = lcm(m, n)
        long newRem = (a + m * t0) % newMod;
        if (newRem < 0) newRem += newMod;
        return new long[]{newMod, newRem};
    }

    private long[] egcd(long a, long b) {
        if (b == 0) return new long[]{a, 1, 0};
        long[] vals = egcd(b, a % b);
        long g = vals[0], x1 = vals[1], y1 = vals[2];
        return new long[]{g, y1, x1 - (a / b) * y1};
    }

    private long modInv(long a, long m) {
        long[] vals = egcd(a, m);
        if (vals[0] != 1) throw new ArithmeticException("逆元不存在");
        long inv = vals[1] % m;
        return inv < 0 ? inv + m : inv;
    }
}
