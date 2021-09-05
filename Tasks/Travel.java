package pinqer.Tasks;

import org.powbot.api.Condition;
import org.powbot.api.Random;
import org.powbot.api.Tile;
import org.powbot.api.rt4.*;
import pinqer.BBD;
import pinqer.Utility.Consts;

public class Travel extends pinqer.Tasks.Task {
    public Travel(BBD main) {
        super(main);
    }

    public GameObject hole;
    
    @Override
    public boolean activate() {
        return (Consts.LOVAKENGJ_AREA.contains(Players.local()));
    }

    @Override
    public void execute() {
        if (Movement.energyLevel() > 20 && !Movement.running()) {
            Movement.running(true);
            Condition.sleep(Random.nextInt(900,1500));
            return;
        }

        GameObject hole = Objects.stream(15, GameObject.Type.FLOOR_DECORATION).name("Hole").first();
        /*
        Tile tile = new Tile(1563, 3791,0);
        if (tile.loaded()) {
            try {
                hole = Objects.get(tile, 0, GameObject.Type.FLOOR_DECORATION).get(0);
            }
            catch (Exception e) {
                System.out.println(e.toString());
            }
        }

         */

        if (hole != null && hole.valid() && hole.tile().distanceTo(Players.local()) < 15) {
            if (!Prayer.prayerActive(Prayer.Effect.PROTECT_FROM_MAGIC)) {
                main.currentTask = "Enable pray";
                Prayer.prayer(Prayer.Effect.PROTECT_FROM_MAGIC, true);
                Condition.wait(() -> Prayer.prayerActive(Prayer.Effect.PROTECT_FROM_MAGIC), 500, 3);
                return;
            }
            main.currentTask = "Clicking hole";
            if (hole.inViewport()) {
                if (hole.interact("Enter")) {
                    Condition.wait(() -> !Consts.LOVAKENGJ_AREA.contains(Players.local()), 500, 10);
                }
            }
            else {
                Movement.step(hole);
                Camera.turnTo(hole);
                Condition.wait(() -> hole.inViewport(), 500, 10);
            }
        }
        else {
            main.currentTask = "Walking to hole";
            Movement.walkTo(Consts.HOLE_TILE);
            //Condition.sleep(Random.nextInt(1500,2300));
        }
    }

    public String getName() {
        return "Traveling";
    }
}