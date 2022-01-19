package net.arathain.tot.common.util.config;

import draylar.omegaconfig.api.Config;
import draylar.omegaconfig.api.Syncing;
import net.arathain.tot.TomeOfTiamatha;

public class ToTConfig implements Config {

    @Syncing
    public int maxStringRange = 20;
    @Syncing
    public float stringHangAmount = 80.0F;

    @Override
    public String getName() {
        return "tot";
    }
    @Override
    public String getModid() {
        return TomeOfTiamatha.MODID;
    }
    @Override
    public String getExtension() {
        return "toml";
    }
}
