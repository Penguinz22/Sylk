package dev.penguinz.Sylk;

import dev.penguinz.Sylk.event.Event;
import dev.penguinz.Sylk.event.window.WindowCloseEvent;
import dev.penguinz.Sylk.event.window.WindowResizeEvent;
import dev.penguinz.Sylk.graphics.texture.Texture;
import dev.penguinz.Sylk.input.InputManager;
import dev.penguinz.Sylk.util.Disposable;
import org.lwjgl.glfw.*;
import org.lwjgl.openal.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window implements Disposable {

    private final String title;
    private int windowedWidth, windowedHeight;
    private int width, height;
    private final boolean resizable;
    private boolean fullscreen;
    private final String icon;

    private long windowHandle;
    private Cursor cursorType = Cursor.ARROW;
    private long cursor;
    private boolean hasContext = false;
    private boolean glfwInitialized = false;

    private long audioDevice;

    private InputManager inputManager;

    private final GLFWWindowCloseCallback closeCallback = new GLFWWindowCloseCallback() {
        @Override
        public void invoke(long window) {
            dispatchEvent(new WindowCloseEvent());
        }
    };

    private final GLFWFramebufferSizeCallback resizeCallback = new GLFWFramebufferSizeCallback() {
        @Override
        public void invoke(long window, int width, int height) {
            dispatchEvent(new WindowResizeEvent(width, height));
        }
    };

    public Window(String title, int width, int height, boolean resizable, boolean fullscreen, String icon) {
        this.title = title;
        this.windowedWidth = this.width = width;
        this.windowedHeight = this.height = height;
        this.resizable = resizable;
        this.fullscreen = fullscreen;
        this.icon = icon;

        init();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if(!glfwInit() && !glfwInitialized)
            throw new IllegalStateException("Could not initialize GLFW");
        this.glfwInitialized = true;

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

        if(fullscreen)
            if(vidmode == null)
                throw new IllegalArgumentException("Exception trying to get primary monitor video mode");

        windowHandle = glfwCreateWindow(fullscreen ? vidmode.width() : width, fullscreen ? vidmode.height() : height, title, fullscreen ? glfwGetPrimaryMonitor() : NULL, NULL);
        if(windowHandle == NULL)
            throw new RuntimeException("Window could not be created");

        glfwSetWindowCloseCallback(windowHandle, closeCallback);
        glfwSetFramebufferSizeCallback(windowHandle, resizeCallback);

        cursor = glfwCreateStandardCursor(cursorType.glfwType);
        glfwSetCursor(windowHandle, cursor);

        if(icon != null) {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                GLFWImage image = GLFWImage.mallocStack(stack);
                GLFWImage.Buffer imageBuf = GLFWImage.mallocStack(1, stack);
                Texture iconTexture = new Texture(null, null);
                iconTexture.loadAsync(icon);
                if (iconTexture.getChannels() != 4)
                    throw new RuntimeException("Icon texture must be in RGBA format.");

                image.set(iconTexture.getWidth(), iconTexture.getHeight(), iconTexture.getData());
                imageBuf.put(0, image);
                glfwSetWindowIcon(windowHandle, imageBuf);
            }
        }

        this.inputManager = new InputManager(windowHandle, this::dispatchEvent);

        if(!fullscreen)
            centerWindow();

        updateFramebufferSize();

        glfwMakeContextCurrent(windowHandle);

        glfwSwapInterval(1);

        glfwShowWindow(windowHandle);

        GL.createCapabilities();
        audioDevice = ALC11.alcOpenDevice((ByteBuffer) null);
        ALCCapabilities capabilities = ALC.createCapabilities(audioDevice);
        ALC11.alcMakeContextCurrent(ALC11.alcCreateContext(audioDevice, (IntBuffer) null));
        AL.createCapabilities(capabilities);
        hasContext = true;

        setViewport();
    }

    private void centerWindow() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(windowHandle, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            if(vidmode != null)
                glfwSetWindowPos(windowHandle,
                        (vidmode.width() - pWidth.get(0))/2,
                        (vidmode.height() - pHeight.get(0))/2);
        }
    }

    private void updateFramebufferSize() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);

            glfwGetFramebufferSize(windowHandle, width, height);

            this.width = width.get(0);
            this.height = height.get(0);
        }
    }

    private void setViewport() {
        GL11.glViewport(0, 0, this.width, this.height);
    }

    public void prepare() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    }

    public void update(Cursor cursor) {
        if(cursor != this.cursorType) {
            glfwDestroyCursor(this.cursor);
            this.cursorType = cursor;
            this.cursor = glfwCreateStandardCursor(cursor.glfwType);
            glfwSetCursor(this.windowHandle, this.cursor);
        }

        glfwSwapBuffers(windowHandle);
        inputManager.clearInputs();
        glfwPollEvents();
    }

    public void onEvent(Event event) {
        if(event instanceof WindowResizeEvent) {
            this.width = ((WindowResizeEvent) event).width;
            this.height = ((WindowResizeEvent) event).height;
            if(!fullscreen) {
                this.windowedWidth = this.width;
                this.windowedHeight = this.height;
            }
            if(hasContext)
                setViewport();
        }
    }

    public void dispatchEvent(Event event) {
        onEvent(event);

        Application.getInstance().onEvent(event);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void toggleFullscreen() {
        setFullscreen(!fullscreen);
    }

    public void setFullscreen(boolean fullscreen) {
        if(fullscreen == this.fullscreen)
            return;
        this.fullscreen = fullscreen;

        if(this.fullscreen) {
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            if(vidmode == null)
                throw new IllegalArgumentException("Exception trying to get primary monitor video mode");

            glfwSetWindowMonitor(windowHandle, glfwGetPrimaryMonitor(), 0, 0, vidmode.width(), vidmode.height(), vidmode.refreshRate());
            glfwSetWindowSize(windowHandle, vidmode.width(), vidmode.height());

            updateFramebufferSize();
        } else {
            glfwSetWindowMonitor(windowHandle, NULL, 50, 50, windowedWidth, windowedHeight, 0);
            glfwSetWindowSize(windowHandle, windowedWidth, windowedHeight);

            centerWindow();
        }
    }

    public InputManager getInputManager() {
        return inputManager;
    }

    @Override
    public void dispose() {
        ALC11.alcCloseDevice(this.audioDevice);

        Callbacks.glfwFreeCallbacks(windowHandle);
        glfwDestroyWindow(windowHandle);

        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }
}
