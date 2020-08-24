package dev.penguinz.Sylk.graphics.shader.uniforms;

import dev.penguinz.Sylk.assets.Texture;
import dev.penguinz.Sylk.graphics.shader.Shader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class ShaderUniformSampler2D extends ShaderUniform<Texture> {

    public ShaderUniformSampler2D(String location) {
        super(location, "sampler2D");
    }

    @Override
    public void loadUniform(Shader shader) {
        GL30.glActiveTexture(GL13.GL_TEXTURE0);
        value.bind();
    }
}