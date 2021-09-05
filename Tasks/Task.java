package pinqer.Tasks;

import pinqer.BBD;

public abstract class Task {

    protected BBD main;

    public Task(BBD main) {
        this.main = main;
    }

    public abstract boolean activate();

    public abstract void execute();

    public abstract String getName();

}