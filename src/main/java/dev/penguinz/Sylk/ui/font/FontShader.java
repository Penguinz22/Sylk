package dev.penguinz.Sylk.ui.font;

import dev.penguinz.Sylk.graphics.shader.Shader;
import dev.penguinz.Sylk.graphics.shader.uniforms.*;
import dev.penguinz.Sylk.ui.UIShader;

import java.util.ArrayList;
import java.util.List;

public class FontShader {

    private static final List<ShaderUniform<?>> uniforms = new ArrayList<>();

    public static final String position = "u_position";
    public static final String lineThickness = "u_thickness";
    public static final String overflow = "u_overflow";
    public static final String overflowBounds = "u_overflowBounds";

    static {
        uniforms.add(new ShaderUniformVec4(UniformConstants.color));
        uniforms.add(new ShaderUniformSampler2D(UniformConstants.texture0));
        uniforms.add(new ShaderUniformFloat(FontShader.lineThickness));

        uniforms.add(new ShaderUniformMat4(UniformConstants.projectionMatrix));
        uniforms.add(new ShaderUniformMat4(FontShader.position));
        uniforms.add(new ShaderUniformBool(FontShader.overflow));
        uniforms.add(new ShaderUniformVec4(FontShader.overflowBounds));
    }

    public static Shader create() {
        return new Shader(
                "#version 400 core\n" +
                        "layout (location = 0) in vec2 in_position;\n" +
                        "layout (location = 1) in vec2 in_texCoord;\n" +
                        "out vec2 pass_texCoord;\n"+
                        "uniform mat4 "+UniformConstants.projectionMatrix+";\n"+
                        "uniform mat4 "+FontShader.position+";\n"+
                        "void main()\n"+
                        "{\n" +
                        "  pass_texCoord = in_texCoord;\n"+
                        "  gl_Position = "+ UniformConstants.projectionMatrix+" * "+FontShader.position+" * vec4(in_position.x, in_position.y, 0, 1);\n" +
                        "}\n"
                ,
                "#version 400 core\n" +
                        "in vec2 pass_texCoord;\n"+
                        "out vec4 fragColor;\n" +
                        "uniform vec4 "+UniformConstants.color+";\n"+
                        "uniform sampler2D "+UniformConstants.texture0 +";\n"+
                        "uniform float "+FontShader.lineThickness+";\n"+
                        "uniform bool "+FontShader.overflow+";\n"+
                        "uniform vec4 "+FontShader.overflowBounds+";\n"+
                        "void main()\n"+
                        "{\n" +
                        "  vec4 fragCoord = gl_FragCoord;\n"+
                        "  float inBounds = step("+FontShader.overflowBounds+".x, fragCoord.x) * step(fragCoord.x, "+FontShader.overflowBounds+".z) * " +
                        "step(fragCoord.y, "+FontShader.overflowBounds+".w) * step("+FontShader.overflowBounds+".y, fragCoord.y);\n "+
                        "  if(!"+FontShader.overflow+" && inBounds == 0)\n"+
                        "    discard;\n"+
                        "  vec4 textureColor = texture("+UniformConstants.texture0 +", pass_texCoord);\n"+
                        "  if(textureColor.r < 0.25)\n"+
                        "    discard;\n"+
                        "  fragColor = vec4(1, 1, 1, textureColor.r * "+FontShader.lineThickness+") * "+UniformConstants.color+";\n" +
                        "}\n", uniforms);
    }

}
