package com.iouter.gtnhdumper.common.utils;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class KeySimulator {
    private static final Logger logger = LogManager.getLogger("KeySimulator");

    // 抽象按键访问器
    private interface KeyAccessor {
        boolean isKeyDown(int key);
        void setKeyDown(int key, boolean state);
        void saveState(int[] keys, boolean[] states);
        void restoreState(int[] keys, boolean[] states);
    }

    private static KeyAccessor accessor;
    private static boolean initialized = false;

    static {
        try {
            init();
        } catch (Throwable t) {
            logger.error("KeySimulator initialization failed!", t);
        }
    }

    private static void init() throws Exception {
        Field bufferField;
        Object buffer;

        try {
            // 尝试标准 LWJGL 2 字段
            bufferField = Keyboard.class.getDeclaredField("keyDownBuffer");
            bufferField.setAccessible(true);
            buffer = bufferField.get(null);
        } catch (NoSuchFieldException e1) {
            try {
                // 尝试 lwjgl3ify (LWJGL 3) 的字段
                bufferField = Keyboard.class.getDeclaredField("keys");
                bufferField.setAccessible(true);
                buffer = bufferField.get(null);
            } catch (NoSuchFieldException e2) {
                throw new RuntimeException(
                    "Failed to find key buffer field. Unsupported LWJGL version.",
                    e2
                );
            }
        }

        // 根据实际类型创建访问器
        if (buffer instanceof boolean[]) {
            logger.info("[KeySimulator] Using LWJGL 2 boolean[] buffer");
            accessor = new BooleanArrayAccessor((boolean[]) buffer);
        } else if (buffer instanceof ByteBuffer) {
            logger.info("[KeySimulator] Using LWJGL 3 ByteBuffer buffer (lwjgl3ify)");
            accessor = new ByteBufferAccessor((ByteBuffer) buffer);
        } else {
            throw new UnsupportedOperationException(
                "Unsupported key buffer type: " + buffer.getClass().getName()
            );
        }

        initialized = true;
    }

    // =============== 通用 API ===============
    public static void withKeysPressed(Map<Integer, Boolean> keyStates, Runnable action) {
        if (!initialized || accessor == null) {
            logger.warn("[KeySimulator] Not initialized, running action directly");
            action.run();
            return;
        }

        int[] keys = new int[keyStates.size()];
        boolean[] originalStates = new boolean[keyStates.size()];
        boolean[] newStates = new boolean[keyStates.size()];

        int i = 0;
        for (Map.Entry<Integer, Boolean> entry : keyStates.entrySet()) {
            keys[i] = entry.getKey();
            newStates[i] = entry.getValue();
            i++;
        }

        // 保存原始状态
        accessor.saveState(keys, originalStates);

        try {
            // 应用新状态
            for (i = 0; i < keys.length; i++) {
                accessor.setKeyDown(keys[i], newStates[i]);
            }
            action.run();
        } finally {
            // 恢复原始状态
            accessor.restoreState(keys, originalStates);
        }
    }

    public static void withKeyPressed(Runnable action, int key) {
        Map<Integer, Boolean> states = new HashMap<>();
        states.put(key, true);
        withKeysPressed(states, action);
    }

    public static void withKeysPressed(Runnable action, int... keys) {
        Map<Integer, Boolean> states = new HashMap<>();
        for (int key : keys) {
            states.put(key, true);
        }
        withKeysPressed(states, action);
    }

    // =============== 具体实现 ===============
    private static class BooleanArrayAccessor implements KeyAccessor {
        private final boolean[] buffer;

        BooleanArrayAccessor(boolean[] buffer) {
            this.buffer = buffer;
        }

        @Override
        public boolean isKeyDown(int key) {
            return key >= 0 && key < buffer.length && buffer[key];
        }

        @Override
        public void setKeyDown(int key, boolean state) {
            if (key >= 0 && key < buffer.length) {
                buffer[key] = state;
            }
        }

        @Override
        public void saveState(int[] keys, boolean[] states) {
            for (int i = 0; i < keys.length; i++) {
                if (keys[i] >= 0 && keys[i] < buffer.length) {
                    states[i] = buffer[keys[i]];
                }
            }
        }

        @Override
        public void restoreState(int[] keys, boolean[] states) {
            for (int i = 0; i < keys.length; i++) {
                if (keys[i] >= 0 && keys[i] < buffer.length) {
                    buffer[keys[i]] = states[i];
                }
            }
        }
    }

    private static class ByteBufferAccessor implements KeyAccessor {
        private final ByteBuffer buffer;

        ByteBufferAccessor(ByteBuffer buffer) {
            this.buffer = buffer;
            logger.debug("[KeySimulator] ByteBuffer limit: " + buffer.limit());
        }

        @Override
        public boolean isKeyDown(int key) {
            if (key < 0 || key >= buffer.limit()) return false;
            return buffer.get(key) != 0;
        }

        @Override
        public void setKeyDown(int key, boolean state) {
            if (key < 0 || key >= buffer.limit()) return;
            buffer.put(key, state ? (byte) 1 : (byte) 0);
        }

        @Override
        public void saveState(int[] keys, boolean[] states) {
            for (int i = 0; i < keys.length; i++) {
                int key = keys[i];
                if (key >= 0 && key < buffer.limit()) {
                    states[i] = buffer.get(key) != 0;
                }
            }
        }

        @Override
        public void restoreState(int[] keys, boolean[] states) {
            for (int i = 0; i < keys.length; i++) {
                int key = keys[i];
                if (key >= 0 && key < buffer.limit()) {
                    buffer.put(key, states[i] ? (byte) 1 : (byte) 0);
                }
            }
        }
    }

    // =============== 安全检查 ===============
    public static boolean isInitialized() {
        return initialized;
    }

    // =============== 使用示例 ===============
    public static void test() {
        withKeyPressed(() -> {
            logger.info("Shift is pressed: " + Keyboard.isKeyDown(Keyboard.KEY_LSHIFT));
        }, Keyboard.KEY_LSHIFT);
    }
}
