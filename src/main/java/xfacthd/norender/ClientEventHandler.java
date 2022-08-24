package xfacthd.norender;

import com.google.common.base.Suppliers;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.lwjgl.glfw.GLFW;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = NoRender.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler
{
    private static final Supplier<KeyMapping> NO_RENDER_KEY = Suppliers.memoize(
            () -> new KeyMapping("key.norender.switch_render", GLFW.GLFW_KEY_F12, "key.categories.misc")
    );
    private static final MethodHandle MH_GET_KEYMAPPINGS;
    private static final MethodHandle MH_SET_CLICKCOUNT;

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event)
    {
        MinecraftForge.EVENT_BUS.addListener(ClientEventHandler::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(ClientEventHandler::onMovementUpdate);
        MinecraftForge.EVENT_BUS.addListener(ClientEventHandler::onScreenClose);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(final RegisterKeyMappingsEvent event)
    {
        event.register(NO_RENDER_KEY.get());
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

        if (Minecraft.getInstance().noRender)
        {
            unpressKeyMappings();
        }
    }

    @SuppressWarnings("unchecked")
    private static void unpressKeyMappings()
    {
        try
        {
            for (KeyMapping key : ((Map<String, KeyMapping>) MH_GET_KEYMAPPINGS.invokeExact()).values())
            {
                if (key != Minecraft.getInstance().options.keyInventory)
                {
                    MH_SET_CLICKCOUNT.invokeExact(key, 0);
                    key.setDown(false);
                }
            }
        }
        catch (Throwable e)
        {
            throw new RuntimeException("Failed to unpress keymappings", e);
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

    private static void onScreenClose(final ScreenEvent.Closing event)
    {
        if (Minecraft.getInstance().noRender)
        {
            // Resetting the flag when opening a screen is handled by MC
            Minecraft.getInstance().noRender = false;
        }
    }

    static
    {
        try
        {
            MH_GET_KEYMAPPINGS = MethodHandles.publicLookup().unreflectGetter(
                    ObfuscationReflectionHelper.findField(KeyMapping.class, "f_90809_")
            );

            MH_SET_CLICKCOUNT = MethodHandles.publicLookup().unreflectSetter(
                    ObfuscationReflectionHelper.findField(KeyMapping.class, "f_90818_")
            );
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException("Failed to reflect KeyMapping fields", e);
        }
    }
}
