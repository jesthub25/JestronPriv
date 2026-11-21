package com.jestron.jestronpriv.jc.util;

import com.jestron.jestronpriv.jc.JestronClient;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/** Oyun penceresinin ikonu Jestron logosu olur (görev çubuğu / Alt+Tab). */
public final class WindowBranding {
    private static final int[] SIZES = {16, 32, 48, 128, 256};

    private WindowBranding() {
    }

    public static void applyIcon(Minecraft mc) {
        // macOS ve Wayland pencere ikonunu desteklemez (GLFW hata verir)
        if (Util.getPlatform() == Util.OS.OSX) return;
        try {
            if (GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND) return;
        } catch (Throwable ignored) {
        }
        List<ByteBuffer> files = new ArrayList<>();
        List<ByteBuffer> pixels = new ArrayList<>();
        GLFWImage.Buffer images = null;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            List<int[]> dims = new ArrayList<>();
            for (int s : SIZES) {
                try (InputStream in = WindowBranding.class.getResourceAsStream("/assets/jestronpriv/jc/icons/icon_" + s + ".png")) {
                    if (in == null) continue;
                    byte[] bytes = in.readAllBytes();
                    ByteBuffer file = MemoryUtil.memAlloc(bytes.length);
                    file.put(bytes).flip();
                    files.add(file);
                    IntBuffer w = stack.mallocInt(1), h = stack.mallocInt(1), c = stack.mallocInt(1);
                    ByteBuffer px = STBImage.stbi_load_from_memory(file, w, h, c, 4);
                    if (px == null) continue;
                    pixels.add(px);
                    dims.add(new int[]{w.get(0), h.get(0)});
                }
            }
            if (pixels.isEmpty()) return;
            images = GLFWImage.malloc(pixels.size());
            for (int i = 0; i < pixels.size(); i++) {
                images.position(i);
                images.width(dims.get(i)[0]);
                images.height(dims.get(i)[1]);
                images.pixels(pixels.get(i));
            }
            images.position(0);
            GLFW.glfwSetWindowIcon(mc.getWindow().getWindow(), images);
        } catch (Throwable t) {
            JestronClient.LOG.warn("[Jestron] Pencere ikonu ayarlanamadı", t);
        } finally {
            if (images != null) images.free();
            for (ByteBuffer px : pixels) STBImage.stbi_image_free(px);
            for (ByteBuffer f : files) MemoryUtil.memFree(f);
        }
    }
}
