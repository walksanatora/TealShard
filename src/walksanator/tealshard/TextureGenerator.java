package walksanator.tealshard;

import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.TextureData;
import arc.graphics.g2d.TextureRegion;
import arc.struct.Seq;
import arc.util.Log;

import java.util.function.BiConsumer;

public class TextureGenerator {
    private static Pixmap pixmap;
    private static Texture oldTexture;
    public static final Seq<Color> grays = new Seq<>(new Color[]{
            new Color(0xb0b9c0ff),
            new Color(0x989aa4ff),
            new Color(0x6f7181ff),
            new Color(0x6e7080ff),
    });

    public static void runForPixel(TextureRegion region, BiConsumer<Integer, Integer> consumer) {
        for (int y = 0; y < region.height; y++) {
            for (int x = 0; x < region.width; x++) {
                consumer.accept(x, y);
            }
        }
    }

    public static void runForPixel(Pixmap region, BiConsumer<Integer, Integer> consumer) {
        for (int y = 0; y < region.height; y++) {
            for (int x = 0; x < region.width; x++) {
                consumer.accept(x, y);
            }
        }
    }

    public static void changeHue(TextureRegion region, float hue) {
        if (oldTexture != region.texture) {
            pixmap = region.texture.getTextureData().consumePixmap();
            oldTexture = region.texture;
        }
        Pixmap newPixmap = new Pixmap(region.width, region.height);
        runForPixel(region, (x, y) -> {
            Color color = new Color();
            color.rgba8888(pixmap.get(x + region.getX(), y + region.getY()));
            color.hue(hue);
            newPixmap.set(x, y, color);
        });
        oldTexture.draw(newPixmap, region.getX(), region.getY());
    }

    public static void recolor(TextureRegion base, TextureRegion target, Color recolor) {
        TextureData readtex = base.texture.getTextureData();
        Pixmap read = readtex.consumePixmap();
        Pixmap write = read.copy().crop(base.getX(),base.getY(),base.width,base.height);
//        runForPixel(base, (x,y) -> {
//            Color color = Color.valueOf("ff0000");
//            int icolor = write.get(x, y);
//            color.rgba8888(icolor);
//            write.set(x,y,color);
//        });
        Pixmap red = new Pixmap(base.width,base.height);
        red.fill(recolor);
        base.texture.draw(red,base.getX(),base.getY());
        target.texture.draw(write,target.getX(), target.getY());
    }

    public static boolean isGray(Color color) {
        for (Color gray : grays)
            if (gray.equals(color)) return true;
        return false;
    }
}
