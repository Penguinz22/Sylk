package dev.penguinz.Sylk.graphics.post;

import dev.penguinz.Sylk.Application;
import dev.penguinz.Sylk.event.Event;
import dev.penguinz.Sylk.graphics.post.effects.BloomEffect;
import dev.penguinz.Sylk.graphics.post.effects.PostEffect;
import dev.penguinz.Sylk.util.Disposable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class EffectsLayer implements Disposable {

    private int fbo, texture, blurFbo, blurTexture;

    private final List<PostEffect> effects = new ArrayList<>();

    public EffectsLayer() {
        createFrameBuffer();
    }

    private void createFrameBuffer() {
        this.fbo = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);

        this.texture = glGenTextures();
        glBindTexture(GL11.GL_TEXTURE_2D, texture);
        glTexImage2D(
                GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA,
                (int) Application.getInstance().getWindowWidth(),
                (int) Application.getInstance().getWindowHeight(),
                0, GL11.GL_RGBA, GL11.GL_FLOAT, (ByteBuffer) null);

        setTextureParam();

        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);

        blurFbo = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, blurFbo);

        blurTexture = GL11.glGenTextures();
        glBindTexture(GL_TEXTURE_2D, blurTexture);
        glTexImage2D(
                GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA,
                (int) Application.getInstance().getWindowWidth(),
                (int) Application.getInstance().getWindowHeight(),
                0, GL11.GL_RGBA, GL11.GL_FLOAT, (ByteBuffer) null);

        setTextureParam();

        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, blurTexture, 0);
    }

    private void setTextureParam() {
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL15.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL15.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    }
    private void disposeFrameBuffer() {
        GL30.glDeleteFramebuffers(fbo);
        GL30.glDeleteTextures(texture);
        GL30.glDeleteFramebuffers(blurFbo);
        GL30.glDeleteTextures(blurTexture);
    }

    public void addEffect(PostEffect effect) {
        this.effects.add(effect);
    }

    public void bindBuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
    }

    public int getBlurTexture() {
        return blurTexture;
    }

    public int process() {
        int workingTexture = texture;
        for (PostEffect effect : effects) {
            effect.clearBuffers();
            if(effect instanceof BloomEffect)
                blurTexture = effect.processEffect(workingTexture);
            else
                workingTexture = effect.processEffect(workingTexture);
        }
        return workingTexture;
    }

    public void clearBuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
        GL30.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT0);
        GL30.glClearBufferfv(GL_COLOR, 0, new float[] {0,0,0,0});
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, blurFbo);
        GL30.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT0);
        GL30.glClearBufferfv(GL_COLOR, 0, new float[] {0,0,0,0});
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public void onEvent(Event event) {
        disposeFrameBuffer();
        createFrameBuffer();
        for (PostEffect effect : effects) {
            effect.onEvent(event);
        }
    }

    @Override
    public void dispose() {
        for (PostEffect effect : effects) {
            effect.dispose();
        }
    }
}
