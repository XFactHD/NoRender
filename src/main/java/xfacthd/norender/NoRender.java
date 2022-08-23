package xfacthd.norender;

import com.google.common.base.Suppliers;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

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

        ModLoadingContext.get().registerExtensionPoint(
                IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(
                        () -> "dontcare",
                        (remote, network) -> network
                )
        );
    }

    @Mod.EventBusSubscriber(modid = NoRender.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientEventHandler
    {
        private static final Supplier<KeyMapping> NO_RENDER_KEY = Suppliers.memoize(
                () -> new KeyMapping("key.norender.switch_render", GLFW.GLFW_KEY_F12, "key.categories.misc")
        );

        @SubscribeEvent
        public static void onClientSetup(final FMLClientSetupEvent event)
        {
            MinecraftForge.EVENT_BUS.addListener(ClientEventHandler::onClientTick);
            MinecraftForge.EVENT_BUS.addListener(ClientEventHandler::onMovementUpdate);
            MinecraftForge.EVENT_BUS.addListener(ClientEventHandler::onScreenClose);

            ClientRegistry.registerKeyBinding(NO_RENDER_KEY.get());
        }

        private static void onClientTick(final TickEvent.ClientTickEvent event)
        {
            if (event.phase != TickEvent.Phase.START) { return; }

            if (Minecraft.getInstance().level != null && NO_RENDER_KEY.get().consumeClick())
            {
                Minecraft.getInstance().noRender = !Minecraft.getInstance().noRender;
                if (Minecraft.getInstance().noRender)
                {
                    Minecraft.getInstance().getMainRenderTarget().clear(Minecraft.ON_OSX);
                }
            }
        }

        private static void onMovementUpdate(final MovementInputUpdateEvent event)
        {
            if (Minecraft.getInstance().noRender)
            {
                Input input = event.getInput();

                input.leftImpulse = 0;
                input.forwardImpulse = 0;
                input.up = false;
                input.down = false;
                input.left = false;
                input.right = false;
                input.jumping = false;
                input.shiftKeyDown = false;
            }
        }

        private static void onScreenClose(final ScreenOpenEvent event)
        {
            if (Minecraft.getInstance().noRender && event.getScreen() == null)
            {
                // Resetting the flag when opening a screen is handled by MC
                Minecraft.getInstance().noRender = false;
            }
        }
    }
}
