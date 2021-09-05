package pinqer.Tasks;

import org.powbot.api.Condition;
import org.powbot.api.Random;
import org.powbot.api.Tile;
import org.powbot.api.rt4.*;
import pinqer.BBD;
import pinqer.Utility.Consts;

import java.util.ArrayList;
import java.util.List;

public class FightArea extends pinqer.Tasks.Task {
    public FightArea(BBD main) {
        super(main);
    }

    @Override
    public boolean activate() {
        return (Consts.BBD_AREA.contains(Players.local()));
    }

    @Override
    public void execute() {

        if (Players.local().animation() == 829)
            return;

        Npc interactingDragon = Npcs.stream().name("Brutal black dragon").filter(npc -> npc.interacting().equals(Players.local()))
                .nearest().first();

        if (Inventory.isFull() || Skills.level(Constants.SKILLS_PRAYER) < 10 || Players.local().healthPercent() < 40) {
            main.currentTask = "Wear Dueling";
            Item dueling = Inventory.stream().filter(n -> n.name().contains("Ring of dueling")).first();
            if (!Inventory.stream().filter(n -> n.name().contains("dueling")).isEmpty()) {
                if (Game.tab(Game.Tab.INVENTORY)) {
                    dueling.interact("Wear");
                    Condition.wait(() -> Inventory.stream().filter(n -> n.name().contains("dueling")).isEmpty(), 500, 5);
                }
            }
            else {
                if (Game.tab(Game.Tab.EQUIPMENT)) {
                    dueling = Equipment.stream().filter(n -> n.name().contains("Ring of dueling")).first();
                    dueling.interact("Ferox Enclave");
                    Condition.wait(() -> !Consts.BBD_FIGHT_AREA.contains(Players.local()), 500, 10);
                }
            }
            return;
        }

        if (!Prayer.prayerActive(Prayer.Effect.PROTECT_FROM_MAGIC)) {
            main.currentTask = "Enable pray";
            Prayer.prayer(Prayer.Effect.PROTECT_FROM_MAGIC, true);
            Condition.wait(() -> Prayer.prayerActive(Prayer.Effect.PROTECT_FROM_MAGIC), 500, 3);
            return;
        }

        Game.tab(Game.Tab.INVENTORY);

        if (!Inventory.stream().filter(n->n.name().contains("antifire")).isEmpty()) {
            if (System.currentTimeMillis() > main.beginTime+10*60*1000) {
                main.currentTask = "Drink antifire";
                Item antifirePot = Inventory.stream().filter(n->n.name().contains("antifire")).first();
                antifirePot.interact("Drink");
                if (Condition.wait(() -> !antifirePot.valid() && Players.local().animation() == -1, 500, 7)) {
                    main.beginTime = System.currentTimeMillis() + Random.nextInt(-60000,60000);
                }
            }
        }

        GroundItem item = GroundItems.stream().filter(n -> (n.name().contains("Dra") || n.name().contains("Rune")
                || n.name().contains("Black") || n.name().contains("rune")
                || n.name().contains("Angler") || n.name().contains("Lava")
                || n.name().contains("Runite") || n.name().contains("half")) && Consts.BBD_AREA.contains(n.tile())).nearest().first();

        if (item != null && item.valid() && Consts.BBD_LOOT_AREA.contains(item.tile())) {
            if ((interactingDragon.valid() && interactingDragon.tile().distanceTo(item.tile()) >= 0) || (!interactingDragon.valid())) {
                main.currentTask = "Looting";
                if (item.inViewport()) {
                    item.interact("Take");
                    long invCount = Inventory.stream().count();

                    // Sometimes GrandExchange is weird for some reason
                    try {
                        if (item.noted())
                            main.gpGained += GrandExchange.getItemPrice(item.id() - 1)*item.stackSize();
                        else
                            main.gpGained += GrandExchange.getItemPrice(item.id())*item.stackSize();
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }

                    if (item.name().contains("Black dragon"))
                        Condition.wait(() -> Inventory.stream().count() != invCount, 500, 10);
                    else
                        Condition.wait(() -> !item.valid(), 500, 10);

                } else {
                    //Movement.step(item);
                    Camera.turnTo(item);
                    if (!Condition.wait(item::inViewport, 500, 5)) {
                        Movement.step(item);
                        Condition.wait(item::inViewport, 500, 5);
                    }
                }
                return;
            }
        }

        if (interactingDragon.valid() && interactingDragon.tile().distanceTo(Players.local()) <= 5) {
            main.currentTask = "Dodging melee attack";
            List<Tile> safeTiles = new ArrayList<>();

            for (Tile tile : Consts.BBD_FIGHT_AREA.getTiles()) {
                if (tile.distanceTo(interactingDragon) > 5 && tile.distanceTo(interactingDragon) < 8) {
                    safeTiles.add(tile);
                }
            }


            outerloop: for (int i = 1; i < 8; i++) {
                for (Tile tile : safeTiles) {
                    if (tile.distanceTo(Players.local().tile()) < i) {
                        Movement.step(tile);
                        Condition.sleep(Random.nextInt(1300,1800));
                        break outerloop;
                    }
                }

            }
            return;
        }

        if (Players.local().healthPercent() < 70 && !Inventory.stream().action("Eat").isEmpty()) {
            main.currentTask = "Eat food";
            Item food = Inventory.stream().action("Eat").first();
            food.interact("Eat");
            long invCount = Inventory.stream().count();
            Condition.wait(() -> invCount != Inventory.stream().count(), 500, 5);
            return;
        }

        if (Skills.level(Constants.SKILLS_PRAYER) < 30 && !Inventory.stream().filter(n -> n.name().contains("Prayer")).isEmpty()) {
            main.currentTask = "Drink pray";
            Item prayerPot = Inventory.stream().filter(n -> n.name().contains("Prayer")).first();
            prayerPot.interact("Drink");
            Condition.sleep(Random.nextInt(700,900));
            Condition.wait( () -> Inventory.stream().name(prayerPot.name()).isEmpty(), 500, 5);
            return;
        }

        if (Skills.level(Constants.SKILLS_RANGE) == Skills.realLevel(Constants.SKILLS_RANGE) && !Inventory.stream().filter(n -> n.name().contains("ranging")).isEmpty()) {
            main.currentTask = "Drink range";
            Item rangePot = Inventory.stream().filter(n -> n.name().contains("ranging")).first();
            rangePot.interact("Drink");
            Condition.wait( () -> Inventory.stream().name(rangePot.name()).isEmpty(), 500, 5);
            return;
        }

        if (!Inventory.stream().name("Vial").isEmpty()) {
            main.currentTask = "Drop vial";
            Item vial = Inventory.stream().name("Vial").first();
            vial.interact("Drop");
            long invCount = Inventory.stream().count();
            Condition.wait(() -> invCount != Inventory.stream().count(), 500, 5);
        }

        if (!Players.local().interacting().valid()) {;
            main.currentTask = "Attack";
            main.attack();
            Condition.wait( () -> Players.local().interacting().valid(), 500, 10);
        }

    }

    public String getName() {
        return "Fight Area";
    }
}