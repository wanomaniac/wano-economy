package com.wanomaniac.economy.neoforge.interfaces;
import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.interfaces.IPlatformMenuFactory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class PlatformMenuFactoryNeoForge implements IPlatformMenuFactory {
    @SuppressWarnings("rawtypes")
    private final Map<Supplier<? extends MenuType<?>>, StreamCodec> CODEC_REGISTRY = new ConcurrentHashMap<>();
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, CommonEconomy.MOD_ID);


    public record ScreenObject <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> (MenuType<M> type, ScreenFactory<M, U> factory){

    }
    public static final ArrayList<ScreenObject<? extends AbstractContainerMenu, ?>> registeredScreens = new ArrayList<>();

    // java is a shitshow.
    @SuppressWarnings("unchecked")
    private static <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerSingleScreen(
            RegisterMenuScreensEvent event,
            ScreenObject<?, ?> rawObject
    ) {
        ScreenObject<M, U> screenObject = (ScreenObject<M, U>) rawObject;

        MenuType<M> type = screenObject.type();
        ScreenFactory<M, U> factory = screenObject.factory();

        // Explicit type arguments force Java to match M and U exactly
        event.register(
                type,
                factory::create
        );
    }

    public static void registerScreens(RegisterMenuScreensEvent event){
        registeredScreens.forEach((screenObject -> registerSingleScreen(event, screenObject)));
    }

    @Override
    public <T extends AbstractContainerMenu, D> Supplier<MenuType<T>> createExtended(
            String id,
            ExtendedMenuFactory<T, D> factory,
            StreamCodec<? super RegistryFriendlyByteBuf, D> codec
    ) {
        DeferredHolder<MenuType<?>, MenuType<T>> menuTypeSupplier = MENUS.register(id, () -> IMenuTypeExtension.create((syncId, inv, buf) -> {
            D data = codec.decode(buf);
            return factory.create(syncId, inv, data);
        }));
        CODEC_REGISTRY.put(menuTypeSupplier, codec);

        return menuTypeSupplier;
    }

    @Override
    public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerScreen(MenuType<M> type, ScreenFactory<M, U> factory) {
        registeredScreens.add(new ScreenObject<>(type, factory));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends AbstractContainerMenu, T extends AbstractContainerMenu, D> void openExtendedMenu(ServerPlayer player, Supplier<MenuType<T>> menuType, Component title, ExtendedMenuFactory<M, D> factory, D data) {
        // Look up the codec registered for this menu
        StreamCodec<RegistryFriendlyByteBuf, D> codec = CODEC_REGISTRY.get(menuType);

        if (codec == null) {
            throw new IllegalArgumentException("No StreamCodec registered for MenuType: " + menuType);
        }

        player.openMenu(
                new SimpleMenuProvider(
                        (syncId, inv, p) -> factory.create(menuType.get(), syncId, inv, data),
                        title
                ),
                buf -> codec.encode(buf, data) // Encodes the data automatically!
        );
    }
}
