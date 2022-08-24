package xfacthd.norender;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(NoRender.MOD_ID)
public class NoRender
{
    public static final String MOD_ID = "norender";
    private static final Logger LOGGER = LogManager.getLogger();

    public NoRender()
    {
        if (FMLEnvironment.dist != Dist.CLIENT)
        {
            LOGGER.warn("NoRender is a client-only mod, it should not be installed on the server!");
        }
    }
}
