package ol.world.blocks.pressure;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import ol.gen.*;

import static mindustry.Vars.world;

public class PressureJunction extends PressureBlock implements PressureReplaceable{
    public boolean canExplode = true;
    public Effect boomEffect = Fx.none;
    public boolean noNetDestroy = true;

    public PressureJunction(String name){
        super(name);

        replaceable = true;
        solid = true;
    }

    @Override
    public void setBars(){
        super.setBars();
        barMap.remove("pressure");
    }

    @Override
    public boolean canReplace(Block other){
        return other instanceof PressurePipe;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);
        TextureRegion pressureIcon = Core.atlas.find("ol-arrow");

        float dx = x * 8;
        float dy = y * 8;
        float ds = size * 8;

        Draw.rect(pressureIcon, dx, dy + ds, -90);
        Draw.rect(pressureIcon, dx, dy - ds, 90);

        Draw.color(Pal.lightishGray);
        Draw.rect(pressureIcon, dx + ds, dy, 180);
        Draw.rect(pressureIcon, dx - ds, dy, 0);

        Draw.reset();
    }

    /*@Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        if(!noNetDestroy) {
            return super.canPlaceOn(tile, team, rotation);
        }

        int tx = (int) (tile.drawx() / 8);
        int ty = (int) (tile.drawy() / 8);

        Building left = world.tile(tx - 1, ty).build;
        Building right = world.tile(tx + 1, ty).build;
        Building bottom = world.tile(tx, ty - 1).build;
        Building top = world.tile(tx, ty + 1).build;

        if(rotation == 0 || rotation == 2) {
            return left instanceof PressureAble || right instanceof PressureAble;
        }

        if(rotation == 1 || rotation == 3) {
            return top instanceof PressureAble || bottom instanceof PressureAble;
        }

        return false;
    }*/

    public class PressureJunctionBuild extends PressureBlockBuild{
        @Override
        public void nextBuildings(Building income, Cons<Building> consumer){
            consumer.get(getInvert(income));
        }

        public Building getInvert(Building other){
            return nearby((relativeTo(other) + 2) % 4);
        }

        public void netKill(){
            if(!canExplode){
                return;
            }

            if(boomEffect != null){
                boomEffect.at(x, y);
            }

            kill();
        }

        public boolean notValid(Building b){
            return !(b instanceof PressureAblec);
        }

        @Override
        public void updateTile(){
            super.updateTile();

            Building left = world.tile(tileX() - 1, tileY()).build;
            Building right = world.tile(tileX() + 1, tileY()).build;
            Building bottom = world.tile(tileX(), tileY() - 1).build;
            Building top = world.tile(tileX(), tileY() + 1).build;

            if(noNetDestroy && notValid(left) && notValid(right) && notValid(bottom) && notValid(top)){
                kill();
            }
        }

        @Override
        public boolean canExplode(){
            return false;
        }
    }
}