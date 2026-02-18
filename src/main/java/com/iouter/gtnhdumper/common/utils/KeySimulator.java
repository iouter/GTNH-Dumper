package com.iouter.gtnhdumper.common.utils;

import com.iouter.gtnhdumper.GTNHDumper;
import org.lwjglx.input.KeyCodes;
import org.lwjglx.input.Keyboard;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * 双模式按键模拟器 - 同时兼容 LWJGL2 (原版 1.7.10) 和 LWJGL3 (lwjgl3ify)
 * 通过运行时检测自动选择合适的实现
 */
public final class KeySimulator {

    // 兼容模式
    private enum Mode {
        UNKNOWN,
        LWJGL2,    // 原版 1.7.10 (使用 keyDownBuffer)
        LWJGL3_SDL // lwjgl3ify (使用 sdlKeyPressedArray)
    }

    private static Mode mode = Mode.UNKNOWN;
    private static final Map<Integer, Boolean> ORIGINAL_STATES_LWJGL2 = new HashMap<>();
    private static final Map<Integer, Byte> ORIGINAL_STATES_LWJGL3 = new HashMap<>();

    // LWJGL2 字段
    private static Field keyDownBufferField;

    private KeySimulator() {
    }

    /**
     * 初始化 - 检测运行时环境
     */
    private static synchronized void init() {
        if (mode != Mode.UNKNOWN) {
            return;
        }

        try {
            // 尝试检测 LWJGL3 (lwjgl3ify) 模式
            Class<?> keyboardClass = Class.forName("org.lwjglx.input.Keyboard");
            mode = Mode.LWJGL3_SDL;
            GTNHDumper.LOG.info("[KeySimulator] Detected LWJGL3 SDL mode (lwjgl3ify)");
            return;
        } catch (Exception e) {
            GTNHDumper.LOG.debug("[KeySimulator] SDL mode not available", e);
        }

        try {
            // 尝试检测 LWJGL2 (原版) 模式
            Class<?> keyboardClass = Class.forName("org.lwjgl.input.Keyboard");
            keyDownBufferField = keyboardClass.getDeclaredField("keyDownBuffer");
            keyDownBufferField.setAccessible(true);

            mode = Mode.LWJGL2;
            GTNHDumper.LOG.info("[KeySimulator] Detected LWJGL2 mode (vanilla)");
            return;
        } catch (Exception e) {
            GTNHDumper.LOG.error("[KeySimulator] Failed to detect input mode!", e);
            mode = Mode.UNKNOWN;
        }
    }

    /**
     * 在按键按下状态下执行操作
     */
    public static void withKeyPressed(Runnable action, int... keys) {
        if (action == null || keys == null || keys.length == 0) {
            action.run();
            return;
        }

        init();
        if (mode == Mode.UNKNOWN) {
            GTNHDumper.LOG.warn("[KeySimulator] Unknown input mode - executing action directly");
            action.run();
            return;
        }

        switch (mode) {
            case LWJGL2:
                simulateLwjgl2(keys, action);
                break;
            case LWJGL3_SDL:
                simulateLwjgl3(keys, action);
                break;
        }
    }

    /**
     * LWJGL2 模式实现 (原版 1.7.10)
     */
    private static void simulateLwjgl2(int[] keys, Runnable action) {
        boolean[] buffer = null;
        try {
            buffer = (boolean[]) keyDownBufferField.get(null);

            // 保存原始状态并设置新状态
            for (int key : keys) {
                if (key >= 0 && key < buffer.length) {
                    ORIGINAL_STATES_LWJGL2.put(key, buffer[key]);
                    buffer[key] = true;
                }
            }

            action.run();
        } catch (Exception e) {
            GTNHDumper.LOG.error("[KeySimulator] LWJGL2 simulation failed", e);
        } finally {
            // 100% 恢复原始状态
            if (buffer != null) {
                for (int key : keys) {
                    Boolean original = ORIGINAL_STATES_LWJGL2.get(key);
                    if (original != null && key >= 0 && key < buffer.length) {
                        buffer[key] = original;
                    }
                }
            }
            ORIGINAL_STATES_LWJGL2.clear();
        }
    }

    /**
     * LWJGL3 模式实现 (lwjgl3ify)
     */
    private static void simulateLwjgl3(int[] keys, Runnable action) {
        ByteBuffer buffer = Keyboard.sdlKeyPressedArray;
        try {
            if (buffer == null) {
                GTNHDumper.LOG.error("[KeySimulator] SDL key pressed array is null!");
                action.run();
                return;
            }

            // 保存原始状态并设置新状态
            for (int lwjglKey : keys) {
                int sdlScancode = KeyCodes.lwjglToSdlScancode(lwjglKey);
                if (sdlScancode > 0 && sdlScancode < buffer.limit()) {
                    byte original = buffer.get(sdlScancode);
                    ORIGINAL_STATES_LWJGL3.put(sdlScancode, original);
                    buffer.put(sdlScancode, (byte) 1); // 1 = pressed
                }
            }

            action.run();
        } catch (Exception e) {
            GTNHDumper.LOG.error("[KeySimulator] LWJGL3 simulation failed", e);
        } finally {
            // 100% 恢复原始状态
            if (buffer != null) {
                for (int lwjglKey : keys) {
                    int sdlScancode = KeyCodes.lwjglToSdlScancode(lwjglKey);
                    Byte original = ORIGINAL_STATES_LWJGL3.get(sdlScancode);
                    if (original != null && sdlScancode > 0 && sdlScancode < buffer.limit()) {
                        buffer.put(sdlScancode, original);
                    }
                }
            }
            ORIGINAL_STATES_LWJGL3.clear();
        }
    }

    public static void test() {
        GTNHDumper.LOG.info("[KeySimulator] Dual-mode diagnostic started");
        GTNHDumper.LOG.info("[KeySimulator] Current mode: {}", mode);

        withKeyPressed(() -> {
            boolean shiftState = org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_LSHIFT);
            GTNHDumper.LOG.info("[KeySimulator] TEST RESULT: Left Shift is {}",
                shiftState ? "PRESSED" : "RELEASED");
        }, org.lwjgl.input.Keyboard.KEY_LSHIFT);

        GTNHDumper.LOG.info("[KeySimulator] Dual-mode diagnostic completed");
    }
}
