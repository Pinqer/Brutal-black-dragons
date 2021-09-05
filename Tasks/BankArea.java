package pinqer.Tasks;

import org.powbot.api.Condition;
import org.powbot.api.Tile;
import org.powbot.api.rt4.*;
import pinqer.BBD;
import pinqer.Utility.Consts;

public class BankArea extends pinqer.Tasks.Task {
    public BankArea(BBD main) {
        super(main);
    }

    @Override
    public boolean activate() {
        return (Consts.FEROX_AREA.contains(Players.local()));
    }

    @Override
    public void execute() {
        if (Widgets.component(187,3,2).visible()) {
            main.currentTask = "Widget";
            Widgets.component(187,3,2).interact("Continue");
            Condition.wait(() -> !Consts.FEROX_AREA.contains(Players.local()), 500, 10);
            return;
        }

        if (Prayer.prayerActive(Prayer.Effect.PROTECT_FROM_MAGIC)) {
            main.currentTask = "disable pray";
            Prayer.prayer(Prayer.Effect.PROTECT_FROM_MAGIC, false);
            Condition.wait(() -> !Prayer.prayerActive(Prayer.Effect.PROTECT_FROM_MAGIC), 500, 3);
            return;
        }

        Game.tab(Game.Tab.INVENTORY);

        if (!Inventory.stream().filter(n->n.name().contains("Archers ring")).isEmpty()) {
            main.currentTask = "Wear ring";
            Item archerRing = Inventory.stream().filter(n->n.name().contains("Archers ring")).first();
            archerRing.interact("Wear");
            Condition.wait(() -> Inventory.stream().filter(n->n.name().contains("Archers ring")).isEmpty(), 500, 5);
            return;
        }

        if (!org.powbot.api.rt4.Bank.opened() && (Inventory.isFull() || Inventory.stream().name("Shark").count() != 3 || Inventory.stream().filter(n -> n.name().contains("antifire")).isEmpty()
                || Inventory.stream().filter(n -> n.name().contains("ranging potion")).isEmpty() || Inventory.stream().filter(n -> n.name().contains("Prayer potion")).isEmpty()
                || !Inventory.stream().name("Black dragonhide", "Dragon bones").isEmpty())) {
            main.currentTask = "Open bank";
            Npc bank = Npcs.stream().name("Banker").first();
            if (bank.inViewport()) {
                if (bank.interact("Bank")) {
                    Condition.wait(org.powbot.api.rt4.Bank::opened, 500, 10);
                }
            }
            else {
                Movement.step(org.powbot.api.rt4.Bank.nearest());
                Camera.turnTo(org.powbot.api.rt4.Bank.nearest());
                Condition.wait(bank::inViewport, 500, 7);
            }
            return;
        }
        if (org.powbot.api.rt4.Bank.opened()) {
            main.currentTask = "Handling bank";
            String rangedPot = Inventory.stream().filter(n -> n.name().contains("ranging potion")).first().name();
            String antifire = Inventory.stream().filter(n -> n.name().contains("antifire")).first().name();
            String rod = Inventory.stream().filter(n -> n.name().contains("dueling")).first().name();

            if (Inventory.stream().count() > 11 || !Inventory.stream().name("Black dragonhide", "Dragon bones").isEmpty()) {
                org.powbot.api.rt4.Bank.depositAllExcept("Shark", "Xeric's talisman", rangedPot, antifire, rod);
            }

            if (Inventory.stream().filter(n -> n.name().contains("Ring of dueling")).isEmpty()) {
                if (org.powbot.api.rt4.Bank.withdraw("Ring of dueling(8)", 1)) {
                    Condition.wait(() -> !Inventory.stream().name("Ring of dueling(8)").isEmpty(), 500, 5);
                }
            }
            else if (Inventory.stream().filter(n -> n.name().contains("Prayer potion")).isEmpty()) {
                if (org.powbot.api.rt4.Bank.withdraw("Prayer potion(4)", 2)) {
                    Condition.wait(() -> !Inventory.stream().name("Prayer potion(4)").isEmpty(), 500, 5);
                }
            }
            else if (Inventory.stream().name("Shark").count() == 0) {
                if (org.powbot.api.rt4.Bank.withdraw("Shark", 3)) {
                    Condition.wait(() -> !Inventory.stream().name("SHark").isEmpty(), 500, 5);
                }
            }
            else if (Inventory.stream().name("Shark").count() == 1) {
                if (org.powbot.api.rt4.Bank.withdraw("Shark", 2)) {
                    Condition.wait(() -> !Inventory.stream().name("Shark").isEmpty(), 500, 5);
                }
            }
            else if (Inventory.stream().name("Shark").count() == 2) {
                if (org.powbot.api.rt4.Bank.withdraw("Shark", 1)) {
                    Condition.wait(() -> !Inventory.stream().name("Shark").isEmpty(), 500, 5);
                }
            }
            else if (Inventory.stream().filter(n -> n.name().contains("antifire")).isEmpty()) {
                if (org.powbot.api.rt4.Bank.withdraw("Extended antifire(4)", 1)) {
                    Condition.wait(() -> !Inventory.stream().name("Extended antifire(4)").isEmpty(), 500, 5);
                }
            }
            else if (!Inventory.stream().name("Extended antifire(1)").isEmpty() && Inventory.stream().name("Extended antifire(4)").isEmpty()) {
                if (org.powbot.api.rt4.Bank.withdraw("Extended antifire(4)", 1)) {
                    Condition.wait(() -> !Inventory.stream().name("Extended antifire(4)").isEmpty(), 500, 5);
                }
            }
            else if (Inventory.stream().filter(n -> n.name().contains("ranging potion")).isEmpty()) {
                if (org.powbot.api.rt4.Bank.withdraw("Divine ranging potion(4)", 1)) {
                    Condition.wait(() -> !Inventory.stream().name("Divine ranging potion(4)").isEmpty(), 500, 5);
                }
            }
            else if (Inventory.stream().name("Shark").count() == 3 && !Inventory.stream().filter(n -> n.name().contains("antifire")).isEmpty()
                    && !Inventory.stream().filter(n -> n.name().contains("ranging potion")).isEmpty() && !Inventory.stream().filter(n -> n.name().contains("Prayer potion")).isEmpty()
                    && !Inventory.stream().filter(n->n.name().contains("dueling")).isEmpty()) {
                org.powbot.api.rt4.Bank.close();
                Condition.wait(() -> !org.powbot.api.rt4.Bank.opened(), 500, 5);
            }
            return;
        }


        if (Players.local().healthPercent() < 90 || Movement.energyLevel() < 90) {
            main.currentTask = "Refreshment pool";
            Tile tile = new Tile(3128, 3634,0);
            GameObject pool = Objects.stream().name("Pool of refreshment").first();
            /*
            if (tile.loaded()) {
                pool = Objects.get(tile, 0, GameObject.Type.INTERACTIVE).get(0);
            }

             */

            if (pool.inViewport()) {
                if (pool.interact("Drink")) {
                    Condition.wait(() -> Players.local().healthPercent() > 95 && Movement.energyLevel() > 95 && Players.local().animation() == -1 , 500, 10);
                }
            } else {
                Movement.step(tile);
                Camera.turnTo(tile);
                Condition.wait(pool::inViewport, 500, 5);
            }
            return;
        }

        if (Inventory.stream().name("Shark").count() == 3 && !Inventory.stream().filter(n -> n.name().contains("antifire")).isEmpty()
                && !Inventory.stream().filter(n -> n.name().contains("ranging potion")).isEmpty() && !Inventory.stream().filter(n -> n.name().contains("Prayer potion")).isEmpty()
                && !Inventory.stream().filter(n->n.name().contains("dueling")).isEmpty()) {
            main.currentTask = "Clicking teleport";
            Item talisman = Inventory.stream().name("Xeric's talisman").first();
            if (talisman.interact("Rub")) {
                Condition.wait(() -> Widgets.component(187, 3, 2).visible(), 500, 5);
            }
            return;
        }
    }

    public String getName() {
        return "Bank Area";
    }
}