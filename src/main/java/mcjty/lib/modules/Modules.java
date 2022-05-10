package mcjty.lib.modules;

import java.util.ArrayList;
import java.util.List;

public class Modules {

    private final List<IModule> modules = new ArrayList<>();

    public void register(IModule module) {
        modules.add(module);
    }

    public void init() {
        modules.forEach(m -> m.init());
    }

    public void initClient() {
        modules.forEach(m -> m.initClient());
    }

    public void initConfig() {
        modules.forEach(IModule::initConfig);
    }
}
