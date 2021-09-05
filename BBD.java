package pinqer;


import java.util.*;
import java.util.List;

import com.google.common.eventbus.Subscribe;
import org.powbot.api.Color;
import org.powbot.mobile.drawing.Graphics;
import org.powbot.api.Condition;
import org.powbot.api.event.RenderEvent;
import org.powbot.api.rt4.*;
import org.powbot.api.script.AbstractScript;
import org.powbot.api.script.ScriptManifest;
import org.powbot.mobile.script.ScriptManager;
import org.powbot.mobile.service.ScriptUploader;
import org.powbot.api.rt4.Npc;
import pinqer.Tasks.BankArea;
import pinqer.Tasks.FightArea;
import pinqer.Tasks.Task;
import pinqer.Tasks.Travel;

@ScriptManifest(name = "Brutal black dragons", description = "Slays brutal black dragons for profit", version = "1.0.0")

public class BBD extends AbstractScript {

    public String currentTask = "Starting...";
    public long sRangedXp = Skills.experience(Constants.SKILLS_RANGE);
    public Npc currentNpc;
    public GameObject hole;
    public int gpGained = 0;
    public Npc finalCurrentNpc;
    long startTime = System.currentTimeMillis();
    public List<Task> taskList = new ArrayList<Task>();
    public long beginTime;

    @Override
    public void onStart() {
        taskList.add(new FightArea(this));
        taskList.add(new Travel(this));
        taskList.add(new BankArea(this));

        sRangedXp = Skills.experience(Constants.SKILLS_RANGE);
        startTime = System.currentTimeMillis();
    }

    @Override
    public void onStop() {

    }


    public void poll() {
        for (Task t : taskList) {
            if (t.activate()) {
                t.execute();
                break;
            }
        }

    }


    public void attack() {

        List<Npc> npcs = Npcs.stream().name("Brutal black dragon").filter(npc -> npc.interacting().equals(Players.local())
                || (!npc.interacting().valid() && npc.healthPercent() > 0)).nearest().list();

        if (npcs.size() > 0) currentNpc = npcs.get(0);

        for (Npc n : npcs) {
            if (n.interacting().equals(Players.local())) {
                currentNpc = n;
            }
        }

        if (currentNpc != null) {
            if (currentNpc.inViewport()) {
                if (currentNpc.interact("Attack")) {
                    Condition.wait(() -> currentNpc.interacting().equals(Players.local()), 500, 10);
                }
            }
            else {
                Camera.turnTo(currentNpc);
                Condition.wait(() -> finalCurrentNpc.inViewport(), 500, 10);
            }
        }
    }

    @Subscribe
    public void onRender(RenderEvent e) {
        Graphics g = e.getGraphics();
        long cRangedXp = Skills.experience(Constants.SKILLS_RANGE);

        // Paint window
        //g.fillRect(140, 252, 480, 150);
        g.setColor(Color.getWHITE());
        g.drawRect(40, 152, 480, 200);

        // Info text
        g.setColor(Color.getORANGE());
        g.drawString("Pinq's Brutal Blacks", 102, 180);
        g.setColor(Color.getWHITE());
        g.drawString("Ranged exp. gained: " + (cRangedXp-sRangedXp) + " (" + getPerHour(cRangedXp-sRangedXp, ScriptManager.INSTANCE.getRuntime(true)) + "/pH)", 50, 220);
        g.drawString("Time running: " + formatTime(ScriptManager.INSTANCE.getRuntime(true)  / 1000), 50, 250);
        g.drawString("Current task: " + currentTask, 50, 280);
        g.drawString("GP. gained: " + gpGained + " (" + getPerHour(gpGained, ScriptManager.INSTANCE.getRuntime(true)) + "/pH)", 50, 310);
    }

    private long getPerHour(long in, long time) {
        return (int) ((in) * 3600000D / time);
    }

    private String formatTime(long time) {
        return String.format("%d:%02d:%02d", time / 3600, (time % 3600) / 60, (time % 60));
    }

    public static void main(String[] args) {

        new ScriptUploader().uploadAndStart("Brutal black dragons", "bob", "127.0.0.1:5555", true, false);
    }
}
