package ru.multivarka.showitemsmod;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(showitemsmod.MODID)
public class showitemsmod {
    public static final String MODID = "showitemsmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public showitemsmod(ModContainer container) {
        Network.init();
    }
}
