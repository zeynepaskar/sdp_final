package com.thanos.participant3.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.Map;

/** Pixel art as rows of palette keys, top row first; '.' is transparent. */
public final class PixelSprite {
    public interface Colors {
        int rgba(char key);
    }

    private final String[] rows;
    private final Map<Character, Integer> palette;
    private final Color color = new Color();

    public PixelSprite(Map<Character, Integer> palette, String... rows) {
        if (palette == null || rows == null || rows.length == 0) {
            throw new IllegalArgumentException("Sprite needs a palette and rows");
        }
        for (String row : rows) {
            if (row.length() != rows[0].length()) throw new IllegalArgumentException("Sprite rows differ in width");
            for (char key : row.toCharArray()) {
                if (key != '.' && !palette.containsKey(key)) {
                    throw new IllegalArgumentException("No colour for '" + key + "'");
                }
            }
        }
        this.rows = rows.clone();
        this.palette = Map.copyOf(palette);
    }

    public int width() { return rows[0].length(); }
    public int height() { return rows.length; }
    public char key(int x, int y) { return rows[y].charAt(x); }
    public int rgba(char key) { return palette.get(key); }

    public void draw(ShapeRenderer shapes, float x, float y, float scale) { draw(shapes, x, y, scale, this::rgba); }

    /** Bottom left corner at x, y; colors may recolour keys. Runs of one key are drawn as one rectangle. */
    public void draw(ShapeRenderer shapes, float x, float y, float scale, Colors colors) {
        for (int row = 0; row < rows.length; row++) {
            String line = rows[row];
            float top = y + (rows.length - 1 - row) * scale;
            int start = 0;
            while (start < line.length()) {
                char key = line.charAt(start);
                int end = start + 1;
                while (end < line.length() && line.charAt(end) == key) end++;
                if (key != '.') {
                    shapes.setColor(color.set(colors.rgba(key)));
                    shapes.rect(x + start * scale, top, (end - start) * scale, scale);
                }
                start = end;
            }
        }
    }
}
