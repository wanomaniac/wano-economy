package com.wanomaniac.economy.fabric.interfaces;

import com.wanomaniac.economy.CommonEconomy;
import com.wanomaniac.economy.registry.Menu;
import com.wanomaniac.economy.interfaces.IPlatformRegistryInit;


public class PlatformRegisterInitFabric implements IPlatformRegistryInit {
    @Override
    public void register() {
        Menu.register();
    }
}
