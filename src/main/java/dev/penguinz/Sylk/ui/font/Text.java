package dev.penguinz.Sylk.ui.font;

import dev.penguinz.Sylk.graphics.VAO;
import dev.penguinz.Sylk.graphics.VBO;
import dev.penguinz.Sylk.graphics.VBOType;
import dev.penguinz.Sylk.util.Color;
import dev.penguinz.Sylk.util.RefContainer;

import java.util.Arrays;

public class Text {

    public final Color color;
    public final int pixelHeight;
    private final String text;
    public final RefContainer<Font> font;

    private VAO generatedVAO;
    private float width = 0;

    public Text(String text, int pixelHeight, Color color, RefContainer<Font> font) {
        this.text = text;
        this.pixelHeight = pixelHeight;
        this.color = color;
        this.font = font;

        if(font == null)
            throw new RuntimeException("Text font reference must be initialized");

        calculateVao();
    }

    private void calculateVao() {
        if(this.font.value == null)
            return;
        CharacterSet font = this.font.value.getFont(pixelHeight);
        float[] positions = new float[text.length()*12];
        float[] textureCoords = new float[text.length()*12];

        int posIndex = 0;

        float xPos = 0;

        for (int i = 0; i < text.length(); i++) {
            int codepoint = text.charAt(i);

            Character charData = font.getCharacterData()[codepoint-Font.START_CHAR];

            float width = (charData.position.z - charData.position.x);
            float height = (charData.position.w - charData.position.y);
            float descent = charData.descent;

            // Keep in mind descent is negative
            float baseline = (font.getLineHeight() + font.getDescent());

            // Array only used to determine the positioning of the vertices in the triangle
            int[] indices = new int[] {
                    0, 1,
                    1, 0,
                    0, 0,
                    0, 1,
                    1, 0,
                    1, 1
            };
            for(int index = 0; index < indices.length; index++) {
                positions[posIndex] = xPos + (indices[index] == 0 ? 0 : width);
                textureCoords[posIndex++] = indices[index++] == 0 ? charData.texturePosition.x : charData.texturePosition.z;

                positions[posIndex] = baseline - (indices[index] == 0 ? 0 : height) + descent;
                textureCoords[posIndex++] = indices[index] == 0 ? charData.texturePosition.w : charData.texturePosition.y;
            }

            xPos += width + charData.advance;
            if(i + 1 == text.length())
                xPos -= charData.advance;
        }
        this.width = xPos;
        this.generatedVAO = new VAO(new VBO(positions, 2, VBOType.VERTICES), new VBO(textureCoords, 2, VBOType.TEXTURE_COORDS));
    }

    public VAO getVAO() {
        if(this.generatedVAO == null)
            calculateVao();
        return this.generatedVAO == null ? new VAO() : this.generatedVAO;
    }

    public float getWidth() {
        return width;
    }

    public String getText() {
        return text;
    }
}
