package ol.world.blocks.pressure;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import ol.content.*;
import ol.content.blocks.*;
import ol.input.*;
import ol.utils.*;
import ol.utils.pressure.*;

import java.util.function.*;

import static mindustry.Vars.*;

public class PressurePipe extends PressureBlock implements PressureReplaceable{
    public TextureRegion[] cache;//null if headless
    public @Nullable Block junctionReplacement, bridgeReplacement;

    /** draw connections? */
    public boolean mapDraw = true;

    int timer = timers++;

    public PressurePipe(String name){
        super(name);

        conveyorPlacement = underBullets = rotate = solid = true;
        drawArrow = false;
        group = BlockGroup.power;
        priority = TargetPriority.transport;
    }

    @Override
    public void load(){
        cache = new TextureRegion[0b1_00_00];
        super.load();

    }

    public void drawT(int x, int y, int rotation){
        TextureRegion pressureIcon = Core.atlas.find("ol-arrow");

        float dx = x * 8;
        float dy = y * 8;
        float ds = size * 8;

        if(rotation == 1 || rotation == 3){
            Draw.rect(pressureIcon, dx, dy + ds, -90);
            Draw.rect(pressureIcon, dx, dy - ds, 90);
        }

        if(rotation == 0 || rotation == 2){
            Draw.rect(pressureIcon, dx + ds, dy, 180);
            Draw.rect(pressureIcon, dx - ds, dy, 0);
        }
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);
        drawT(x, y, rotation);
    }

    @Override
    public boolean canReplace(Block other){
        boolean valid = true;
        if(other instanceof PressurePipe pipe){
            valid = PressureAPI.tierAble(pipe, tier);
        }

        return canBeReplaced(other) && valid;
    }

    @Override
    public void init(){
        super.init();

        if(junctionReplacement == null){
            junctionReplacement = OlPressure.pressureJunction;
        }

        //I did this because there will be no other pipes for pressure anyway.
        //If there is an idea that it is better to implement, implement it.
        if(bridgeReplacement == null || !(bridgeReplacement instanceof PressureBridge)){
            bridgeReplacement = switch(tier){
                case 2 -> OlPressure.improvedPressureBridge;
                case 3 -> OlPressure.reinforcedPressureBridge;
                default -> OlPressure.pressureBridge;
            };
        }
    }

    public boolean alignX(int rotation){
        return rotation == 0 || rotation == 2;
    }

    public boolean alignY(int rotation){
        return rotation == 1 || rotation == 3;
    }

    public boolean inBuildPlanNet(BuildPlan s, int x, int y, int rotation){
        int ox = s.x - x;
        int oy = s.y - y;

        if(ox == 0 && oy == 0){
            return true;
        }

        ox = ox < 0 ? -ox : ox;
        return (ox == 1) ? (alignX(rotation) || alignX(s.rotation))
                   : (alignY(rotation) || alignY(s.rotation));
    }

    @Override
    public Block getReplacement(BuildPlan req, Seq<BuildPlan> plans){
        if(junctionReplacement == null){
            return this;
        }

        Boolf<Point2> cont = p -> plans.contains(o ->
                o.x == req.x + p.x && o.y == req.y + p.y && o.rotation
                == req.rotation && (req.block instanceof PressurePipe || req.block instanceof PressureJunction)
        );

        return cont.get(Geometry.d4(req.rotation)) &&
                   cont.get(Geometry.d4(req.rotation - 2)) &&
                   req.tile() != null &&
                   req.tile().block() instanceof PressurePipe &&
                   Mathf.mod(req.build().rotation - req.rotation, 2) == 1 ? junctionReplacement : this;
    }

    @Override
    public void handlePlacementLine(Seq<BuildPlan> plans){
        if(bridgeReplacement == null) return;
        OLPlacement.calculateBridges(plans, (PressureBridge)bridgeReplacement);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        if(this.mapDraw){
            OlPlans.set(plan, list);

            BuildPlan
                top = OlPlans.get(0, 1),
                bottom = OlPlans.get(0, -1),
                left = OlPlans.get(-1, 0),
                right = OlPlans.get(1, 0);

            boolean
                validTop = top != null,
                validBottom = bottom != null,
                validLeft = left != null,
                validRight = right != null;

            Function<BuildPlan, Boolean> canWork = plnFunc -> {
                Block block = plnFunc.block;

                if(block instanceof PressureBlock pressureBlock){
                    boolean valid = PressureAPI.tierAble(pressureBlock, tier);

                    if(block instanceof PressurePipe pipe){
                        valid &= pipe.inBuildPlanNet(plan, plnFunc.x, plnFunc.y, plnFunc.rotation);
                    }

                    return valid;
                }

                return block instanceof PressureJunction;
            };

            if(validTop){
                validTop = canWork.apply(top);
            }

            if(validBottom){
                validBottom = canWork.apply(bottom);
            }

            if(validLeft){
                validLeft = canWork.apply(left);
            }

            if(validRight){
                validRight = canWork.apply(right);
            }

            TextureRegion reg = getRegion(validTop, validBottom, validLeft, validRight);
            Draw.rect(reg, plan.drawx(), plan.drawy());

            if(plan.worldContext && player != null && teamRegion != null && teamRegion.found()){
                if(teamRegions[player.team().id] == teamRegion){
                    Draw.color(player.team().color);
                }

                Draw.rect(teamRegions[player.team().id], plan.drawx(), plan.drawy());
                Draw.color();
            }

            drawPlanConfig(plan, list);
        }else{
            super.drawPlanRegion(plan, list);
        }
    }

    private TextureRegion getRegion(boolean top, boolean bottom, boolean left, boolean right){
        int
            b = bottom ? 0b0001 : 0,
            t = top ? 0b0010 : 0,
            r = right ? 0b0100 : 0,
            l = left ? 0b1000 : 0;

        int spriteID = l | r | t | b;

        if(cache[spriteID] == null){
            cache[spriteID] = Core.atlas.find(name + "-" + spriteID);
        }

        TextureRegion reg = cache[spriteID];
        return reg;
    }

    public class PressurePipeBuild extends PressureBlockBuild{
        @Override
        public void nextBuildings(Building income, Cons<Building> consumer){
            Building left = nearby(-1, 0);
            Building right = nearby(1, 0);
            Building bottom = nearby(0, -1);
            Building top = nearby(0, 1);

            boolean bLeft = (avalible(left) || left instanceof PressureJunction.PressureJunctionBuild) && avalibleX();
            boolean bRight = (avalible(right) || right instanceof PressureJunction.PressureJunctionBuild) && avalibleX();
            boolean bTop = (avalible(top) || top instanceof PressureJunction.PressureJunctionBuild) && avalibleY();
            boolean bBottom = (avalible(bottom) || bottom instanceof PressureJunction.PressureJunctionBuild) && avalibleY();
            if(left != income && bLeft) consumer.get(left);
            if(right != income && bRight) consumer.get(right);
            if(bottom != income && bBottom) consumer.get(bottom);
            if(top != income && bTop) consumer.get(top);
        }

        @Override
        public void updateTile(){
            super.updateTile();

            if(PressureAPI.overload(this)){
                float random = Mathf.random(-3, 3);

                if(timer(PressurePipe.this.timer, Mathf.random(35, 65))){
                    OlFx.pressureDamage.at(x + random / 2, y + random / 2, this.totalProgress() * random, Layer.blockUnder);
                }
            }
        }

        public boolean avalible(Building b){
            return PressureAPI.netAble(this, b);
        }


        /**
         * pipes name based on connections <name>-[L*8+R*4+T*2+B]
         * <p>
         * example: if connected to all sides when loaded 15
         * if connected only right when loaded 4
         * ...
         * if connected only right and left when loaded 12...
         */

        public boolean avalibleX(){
            return true;
        }

        public boolean avalibleY(){
            return true;
        }

        @Override
        public void draw(){
            if(!mapDraw){
                super.draw();
                this.drawTeamTop();
                return;
            }

            Building left = nearby(-1, 0);
            Building right = nearby(1, 0);
            Building bottom = nearby(0, -1);
            Building top = nearby(0, 1);

            boolean bLeft = avalible(left) || left instanceof PressureJunction.PressureJunctionBuild;
            boolean bRight = avalible(right) || right instanceof PressureJunction.PressureJunctionBuild;
            boolean bTop = avalible(top) || top instanceof PressureJunction.PressureJunctionBuild;
            boolean bBottom = avalible(bottom) || bottom instanceof PressureJunction.PressureJunctionBuild;

            TextureRegion region = getRegion(avalibleY() && bTop, avalibleY() && bBottom, avalibleX() && bLeft, avalibleX() && bRight);

            if(pressure > maxPressure && canExplode){
                if(state.is(GameState.State.paused)){
                    Draw.rect(region, this.x, this.y);
                }else{
                    Draw.rect(region, this.x, this.y, Mathf.random(-4, 4));
                }
            }else{
                Draw.rect(region, this.x, this.y);
            }
            this.drawTeamTop();


        }
    }
/*@Struct
    class PressureSpriteIndexStruct{
        public boolean bottom;
        public boolean top;
        public boolean right;
        public boolean left;
    }*/
}
