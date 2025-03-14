package ol.world.blocks.sandbox;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.io.*;

import mindustry.annotations.Annotations.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.*;

import ol.gen.*;
import ol.utils.pressure.*;
import ol.world.blocks.pressure.*;

//maxPressure in this block is max value in the config (!!!)
public class SandboxCompressor extends PressurePipe {
    @Load("@-void")
    public TextureRegion voidRegion;

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);

        rotation++;
        if(rotation > 3) {
            rotation -= 4;
        }

        drawT(x, y, rotation);
    }

    @Override
    public void drawBase(Tile tile) {
        if(tile.build != null) {
            tile.build.draw();
        }else {
            Draw.rect(
                    variants == 0 ? region :
                            variantRegions[Mathf.randomSeed(tile.pos(), 0, Math.max(0, variantRegions.length - 1))],
                    tile.drawx(), tile.drawy());
        }
    }

    public SandboxCompressor(String name) {
        super(name);
        rotate = false;
        canExplode = false;
        copyConfig = true;

        configurable = true;

        mapDraw = false;

        config(Integer.class, (SandboxCompressorBuild b, Integer i) -> b.val = i);

        config(Integer.class, (SandboxCompressorBuild b, Integer i) -> b.val = i);

        config(String.class, (SandboxCompressorBuild b, String str) -> {
            try {
                b.val = Integer.parseInt(str);
            } catch(Exception ignored){}
        });
    }

    public class SandboxCompressorBuild extends PressurePipeBuild {
        public int val = 0;

        public boolean voidMode() {
            return val == 0;
        }

        @Override
        public float pressureThread() {
            return enabled ? (voidMode() ? Integer.MIN_VALUE : val) : 0;
        }

        @Override
        public void draw() {
            Draw.rect(voidMode() ? voidRegion : region, this.x, this.y, this.drawrot());
            this.drawTeamTop();
        }

        @Override
        public Integer config() {
            return val;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(val);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            val = read.i();
        }

        @Override
        public boolean producePressure() {
            return true;
        }

        @Override
        public void buildConfiguration(Table table) {
            if(table == null) {
                return;
            }

            table.pane(t -> {
                t.setBackground(Styles.black5);

                //text
                t.add("@stat.pressure").pad(6f).growY();

                //field to set pressure
                t.field("@stat.pressure", str -> {
                    try {
                        configure(Integer.parseInt(str));
                    } catch(Exception ignored) {
                    }
                }).valid(val -> {
                    try {
                        Integer.parseInt(val);
                        return true;
                    } catch(Exception ignored) {
                        return false;
                    }
                }).pad(6f).get().setText(val + "");

                //added if it "crashed"
                t.button(Icon.refresh, PressureUpdater::reload).width(40f).pad(6f);
            });
        }

        @Override
        public boolean inNet(Building b, PressureAblec p, boolean j) {
            return true;
        }
    }
}