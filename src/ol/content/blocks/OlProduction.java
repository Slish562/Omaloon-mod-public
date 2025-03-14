package ol.content.blocks;

import arc.graphics.*;
import arc.util.Tmp;

import mindustry.content.*;
import mindustry.entities.effect.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;

import ol.content.*;
import ol.gen.*;
import ol.world.blocks.crafting.*;
import ol.world.draw.*;

import static mindustry.type.ItemStack.*;

public class OlProduction {
    public static Block
            //crafters
            multiFactory,
            zariniBoiler,
            valconPress,
            fuser,
            centrifuge;

    public static void load() {

        //crafters
        multiFactory = new MultiCrafter("multi-factory") {{
            requirements(Category.crafting, ItemStack.with(
                    OlItems.grumon, 12,
                    Items.titanium, 11,
                    Items.silicon, 5
            ));

            size = 2;
            itemCapacity = 20;
            liquidCapacity = 20;
            outputsLiquid = true;
            hasItems = true;
            health = 310;

            tier = 1;
            maxPressure = 50;

            crafts = crafts.add(
                    //Magnetic Combination Craft
                    new Craft() {{
                        outputItems = ItemStack.with(
                                OlItems.magneticCombination, 1
                        );

                        consumeItems = ItemStack.with(
                                Items.titanium, 1,
                                OlItems.grumon, 1
                        );

                        drawer = new DrawMulti(
                                new DrawDefault(),
                                new DrawGlowRegion("-glow1"){{
                                    color = Color.valueOf("a2e1aa");
                                }}
                        );

                        consumePower = 1.2f;
                        craftTime = 65f;
                        warmupSpeed = 0.02f;
                    }},
                    //Zarini Craft
                    new Craft() {{
                        outputItems = ItemStack.with(
                                OlItems.zarini, 1
                        );

                        outputLiquids = LiquidStack.with(
                                Liquids.water, 8/60f
                        );

                        consumeItems = ItemStack.with(
                                OlItems.grumon, 1
                        );

                        consumeLiquids = LiquidStack.with(
                                OlLiquids.dalanii, 12/60f
                        );

                        drawer = new DrawMulti(
                                new DrawRegion("-bottom"),
                                new DrawLiquidTile(OlLiquids.dalanii),
                                new DrawLiquidTile(Liquids.water),
                                new DrawBoiling(){{
                                    bubblesColor = Color.valueOf("5e929d");
                                    bubblesSize = 0.4f;
                                    bubblesAmount = 25;
                                }},
                                new DrawRegion("-top"),
                                new DrawGlowRegion("-glow2"){{
                                    color = Color.valueOf("a2e1aa");
                                }}
                        );

                        consumePower = 1.1f;
                        craftTime = 75f;
                        warmupSpeed = 0.2f;
                    }},
                    //Valcon Craft
                    new Craft() {{
                        outputItems = ItemStack.with(
                                OlItems.valkon, 1
                        );

                        consumeItems = ItemStack.with(
                                Items.tungsten, 1,
                                OlItems.zarini, 1
                        );

                        drawer = new DrawMulti(
                                new DrawDefault()
                        );

                        consumePower = 0.7f;

                        downPressure = true;
                        pressureConsume = 40;

                        craftTime = 82f;
                        warmupSpeed = 0.2f;
                    }}
            );
        }};

        zariniBoiler = new GenericCrafter("zarini-boiler") {{
            requirements(Category.crafting, with(
                    Items.surgeAlloy, 20,
                    OlItems.omalite, 50,
                    Items.titanium, 80,
                    Items.thorium, 65
            ));

            size = 3;
            drawer = new DrawMulti(
                    new DrawRegion("-bottom"),
                    new DrawLiquidTile(OlLiquids.dalanii),
                    new DrawLiquidTile(Liquids.water),

                    new DrawBoiling() {{
                        bubblesColor = Color.valueOf("5e929d");
                        bubblesSize = 0.8f;
                        bubblesAmount = 55;
                    }},

                    new DrawDefault(),
                    new DrawGlowRegion("-light") {{
                        color = Color.valueOf("a2e1aa");
                    }}
            );

            consumePower(4.6f);
            consumeItems(new ItemStack(
                    OlItems.grumon, 2
            ));

            consumeLiquids(new LiquidStack(
                    OlLiquids.dalanii, 30/60f
            ));

            outputLiquid = new LiquidStack(
                    Liquids.water, 17/60f
            );

            outputItem = new ItemStack(
                    OlItems.zarini, 2
            );

            craftTime = 170f;
            ambientSound = OlSounds.boiler;

            ambientSoundVolume = 1f;
            itemCapacity = 10;
            liquidCapacity = 50;

            hasPower = hasLiquids = hasItems = true;
        }};

        valconPress = new PressureCrafter("valcon-press") {{
            requirements(Category.crafting, with(
                    Items.surgeAlloy, 20,
                    OlItems.omalite, 50,
                    Items.titanium, 80,
                    Items.thorium, 65
            ));

            size = 3;
            tier = 2;
            maxPressure = 125;
            craftTime = 60f;
            pressureConsume = 110;

            downPressure = true;
            downPercent = 0.15f;

            consumePower(3.6f);

            consumeItems(with(
                    Items.tungsten, 1,
                    OlItems.zarini, 1
            ));

            outputItem = new ItemStack(
                    OlItems.valkon, 2
            );
        }};

        fuser = new GenericCrafter("fuser") {{
            requirements(Category.crafting, with(
                    Items.surgeAlloy, 20,
                    OlItems.omalite, 50,
                    Items.titanium, 80,
                    Items.thorium, 65
            ));

            craftTime = 185f;
            size = 3;

            drawer = new DrawMulti(
                    new DrawRegion("-bottom"),
                    new DrawLiquidTile(Liquids.water),

                    new DrawLiquidTile(OlLiquids.liquidOmalite) {{
                        drawLiquidLight = true;
                    }},

                    new DrawRegion("-rotator"){{
                        spinSprite = true;
                        rotateSpeed = 1f;
                    }},

                    new DrawDefault(),
                    new DrawRegion("-top")
            );

            itemCapacity = 35;
            liquidCapacity = 45;
            hasPower = hasLiquids = hasItems = true;

            consumeLiquid(Liquids.water, 22f / 60f);

            consumeItems(new ItemStack(
                    OlItems.omalite, 2
            ));

            outputLiquid = new LiquidStack(
                    OlLiquids.liquidOmalite,  19.5f / 60f
            );

            consumePower(2.4f);
        }};

        centrifuge = new ImpactCrafter("centrifuge") {{
            size = 4;
            health = 540;
            tier = 3;

            requirements(Category.crafting, with(
                    OlItems.omalite, 80,
                    Items.thorium, 80,
                    Items.titanium, 100
            ));

            stopEffect = new RadialEffect() {{
                effect = OlFx.psh;
                amount = 4;
                lengthOffset = 12;
                rotationOffset = 43;
            }};

            craftTime = 270f;
            craftEffect = Fx.none;
            updateEffectChance = 0.08f;

            ambientSound = OlSounds.centrifuge;
            ambientSoundVolume = 0.1f;

            accelerationSpeed = 0.0003f;
            decelerationSpeed = 0.006125f;
            powerProduction = 22f;

            this.downPressure = true;
            this.pressureConsume = 190;
            this.maxPressure = 240;

            deadlineTime = 7f;

            drawer = new DrawMulti(
                    new DrawDefault(),

                    new DrawRegion("-rotator") {{
                        rotateSpeed = 6;
                        spinSprite = true;
                    }},

                    new DrawRegion("-top")
            );

            onCraft = tile -> {
                Tmp.v1.setToRandomDirection().setLength(27f / 3.4f);

                Fx.pulverize.at(tile.x + Tmp.v1.x, tile.y + Tmp.v1.y);
                Fx.hitLancer.at(tile.x + Tmp.v1.x, tile.y + Tmp.v1.y);
            };

            consumePower(14f);

            consumeItems(with(
                    Items.titanium, 4,
                    OlItems.omalite, 2
            ));


            consumeLiquid(
                    OlLiquids.liquidOmalite,
                    0.18f
            );

            outputItems = with(
                    OlItems.omaliteAlloy, 5
            );

            outputsPower = true;
            powerProduction = 6f;

            itemCapacity = 30;
        }};
        //end crafters
    }
}