package walksanator.tealshard;

import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.g2d.PixmapPacker;
import arc.math.geom.Rect;
import arc.struct.OrderedMap;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.graphics.MultiPacker;
import mindustry.type.Item;

import java.util.HashMap;
import java.util.Map;

public class DummyContent extends Item {
    public DummyContent() {
        super("dummy_item");
        hidden = true;
        hideDetails = true;
        buildable = false;
    }

    @Override
    public void createIcons(MultiPacker packer) {
        Log.warn("replacing sharded textures");
        super.createIcons(packer);
        final int[] i = {0};

        Map<String, Integer> teamregions = new HashMap<>();
        Map<String, Integer> missed = new HashMap<>();

        Seq<PixmapPacker.Page> pages = packer.getPacker(
                MultiPacker.PageType.main
        ).getPages();
        pages.each(page -> {
            OrderedMap<String, PixmapPacker.PixmapPackerRect> rects = page.getRects();
            Seq<String> names = rects.keys().toSeq(); // make a list of rects with names
            //get all -sharded blocks that will need to be recolored
            Seq<Pair<String,PixmapPacker.PixmapPackerRect>> pairs = names.copy()
                    .retainAll(it -> it.endsWith("-sharded"))
                    .map(key -> new Pair<>(key.substring(0,key.length()-8), rects.get(key)));
            //get the -team regions just incase the one we need is on another page
            names.retainAll(it -> it.endsWith("-team")).each(it -> teamregions.put(it, i[0]));

            // iterate over every sharded block texture to recolor it
            for (Pair<String,PixmapPacker.PixmapPackerRect> pair : pairs) {
                PixmapPacker.PixmapPackerRect rect = pair.b;
                Pixmap pixmap = page.getPixmap();
                PixmapPacker.PixmapPackerRect rec2 = rects.get(pair.a);
                if (rec2 != null) {
                    Pixmap recolor = new Pixmap(Math.round(rect.width),Math.round(rect.height));
                    TextureGenerator.runForPixel(recolor, (x,y) -> {
                        Color clr = new Color();
                        clr.rgba8888(pixmap.get(x + Math.round(rec2.x),y + Math.round(rec2.y)));
                        clr.mul(Team.sharded.color);
                        recolor.set(x,y,clr);
                    });
                    Log.warn(String.format("Replaced texture %s-sharded", pair.a));
                    packer.add(MultiPacker.PageType.main,String.format("%s-sharded",pair.a),recolor);
                } else {
                    missed.put(String.format("%s-sharded",pair.a), i[0]);
                }
            }

            i[0]++;
        });

        for (String miss : missed.keySet()) {
            String search = miss.substring(0,miss.length()-8);
            if (teamregions.containsKey(search)) {
                PixmapPacker.Page page_from = pages.get(teamregions.get(search));
                Pixmap from = page_from.getPixmap();
                PixmapPacker.PixmapPackerRect rect = page_from.getRects().get(search);
                Pixmap recolor = new Pixmap(Math.round(rect.width),Math.round(rect.height));
                TextureGenerator.runForPixel(recolor, (x,y) -> {
                    Color clr = new Color();
                    clr.rgba8888(from.get(x + Math.round(rect.x),y + Math.round(rect.y)));
                    clr.mul(Team.sharded.color);
                    recolor.set(x,y,clr);
                });
                Log.warn(String.format("Replaced texture %s-sharded", search));
                packer.add(MultiPacker.PageType.main,String.format("%s-sharded",search),recolor);
//                Log.warn(String.format("Cross-page time (page,id) %s %s -> %s %s",
//                        missed.get(miss), miss,
//                        teamregions.get(search),search
//                ));
            } else {
                Log.err(String.format("Failed to remap shard highlight for texture: %s page: %s",miss,missed.get(miss)));
            }
        }
    }

    private class Pair<A,B> {
        public A a; public B b;
        Pair(A a, B b){
            this.a = a;this.b = b;
        }
    }
    private class Tri<A,B,C> {
        public A a; public B b;public C c;
        Tri(A a, B b, C c){
            this.a = a;this.b = b;this.c = c;
        }
    }
}
