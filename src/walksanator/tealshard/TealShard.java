package walksanator.tealshard;


import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.mod.Mod;
import mindustry.type.Item;
import mindustry.ui.dialogs.BaseDialog;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static mindustry.Vars.content;


public class TealShard extends Mod {
    static int ver = Integer.parseInt(System.getProperty("java.version").split("\\.")[0]);
    static Item stopgap;
    static void setField(Object instance, Field target, Object value) throws IllegalAccessException, NoSuchFieldException {
        target.setAccessible(true);
        if (ver <= 12) {
            Log.info("java is less then 12 so we add a extra little hack just in case");
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(target, target.getModifiers() & ~Modifier.FINAL);
        }
        target.set(instance,value);
    }

    public TealShard() throws NoSuchFieldException, IllegalAccessException {
        Log.level = Log.LogLevel.debug;
        // reflection to make sharded's color teals
        {
            Log.info("Making the shard teal");
            Team shard = Team.sharded;

            Field color_field = Team.class.getDeclaredField("color");
            Color color =  Color.valueOf("00ffaa");
            setField(shard, color_field,color);

            Color[] palette = new Color[3];
            palette[0] = color;
            palette[1] = color.cpy().mul(0.75F);
            palette[2] = color.cpy().mul(0.5F);

            Field palett_field = Team.class.getDeclaredField("palette");
            setField(shard,palett_field,palette);

            int[] palettei = new int[3];
            for(int i = 0; i < 3; ++i) {
                palettei[i] = palette[i].rgba();
            }
            Field paletti_field = Team.class.getDeclaredField("palettei");
            setField(shard,paletti_field,palettei);
        }

        //listen for game load event
        Events.on(EventType.ClientLoadEvent.class, e -> {
            Vars.mods.getMod(this.getClass()).meta.hidden = true;

            //show dialog upon startup
            Time.runTask(10f, () -> {
                BaseDialog dialog = new BaseDialog("frog");
                dialog.cont.add("behold").row();
                //mod sprites are prefixed with the mod name (this mod is called 'example-java-mod' in its config)
                dialog.cont.image(Core.atlas.find("core-nucleus-team-sharded")).pad(20f).row();
                dialog.cont.button("I see", dialog::hide).size(100f, 50f);
                dialog.show();
            });
        });
    }

    @Override
    public void loadContent(){
        stopgap = new DummyContent();

    }

}
