package brova.toshiba;

import brova.toshiba.client.configs.CloudConfigSync;
import brova.toshiba.client.configs.LocalConfigStorage;
import brova.toshiba.client.core.Initializer;
import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.core.PanicController;
import brova.toshiba.client.data.LoaderUserInfo;
import brova.toshiba.client.data.UserInfo;
import brova.toshiba.client.event.EventBus;
import brova.toshiba.client.gui.hud.Draggable;
import brova.toshiba.client.gui.hud.DraggableStorage;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.storages.implement.CommandStorage;
import brova.toshiba.client.storages.implement.FreeLookStorage;
import brova.toshiba.client.storages.implement.FriendStorage;
import brova.toshiba.client.storages.implement.MacroStorage;
import brova.toshiba.client.storages.implement.ModuleStorage;
import brova.toshiba.client.storages.implement.RotationStorage;
import brova.toshiba.client.storages.implement.ServerStorage;
import brova.toshiba.client.storages.implement.StaffStorage;
import brova.toshiba.client.storages.implement.ThemeStorage;
import brova.toshiba.client.storages.implement.WaypointStorage;
import brova.toshiba.client.utils.discord.DiscordPresenceController;
import brova.toshiba.client.utils.time.TpsCalculator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import org.lwjgl.glfw.GLFW;

public enum Toshiba implements ModInitializer, MinecraftAccess {

    INSTANCE;

    public boolean isServer;

    private static double prevTime = 0.0;

    public static double deltaTime = 0.0;

    public Initializer initializer;

    public ModuleStorage moduleStorage;

    public ThemeStorage themeStorage;

    public TpsCalculator tpsCalc;

    public ServerStorage serverStorage;

    public RotationStorage rotationStorage;

    public FreeLookStorage freeLookStorage;

    public CommandStorage commandStorage;

    public FriendStorage friendStorage;

    public MacroStorage macroStorage;

    public LocalConfigStorage localConfigStorage;

    public StaffStorage staffStorage;

    public WaypointStorage waypointStorage;

    private CloudConfigSync cloudConfigSync;

    public UserInfo userInfo = UserInfo.empty();

    public CloudConfigSync getCloudConfigSync() {
        return this.cloudConfigSync;
    }

    public void onInitialize() {
        this.initStorage();
        WorldRenderEvents.START.register(input -> {
            double doubleValue = GLFW.glfwGetTime();
            deltaTime = doubleValue - prevTime;
            prevTime = doubleValue;
            deltaTime = mc.isPaused() ? 0.0 : Math.min(0.05, deltaTime);
        });
    }

    private void initStorage() {
        this.userInfo = new UserInfo(LoaderUserInfo.getUserName(), this.parseUid(LoaderUserInfo.getUID()), LoaderUserInfo.getRole(), "", LoaderUserInfo.getSubTill());
        EventBus.register(this);
        this.initializer = new Initializer();
        this.initializer.onInitialize();
        this.cloudConfigSync = new CloudConfigSync();
        this.cloudConfigSync.start();
        if (!PanicController.isActive()) {
            try {
                DiscordPresenceController.startup();
            } catch (Throwable failure) {
            }
        }
    }

    private int parseUid(String text) {
        try {
            return Integer.parseInt(text);
        } catch (Exception failure) {
            return 0;
        }
    }

    public void closeMinecraft() {
        if (this.cloudConfigSync != null) {
            this.cloudConfigSync.stop();
        }
        DiscordPresenceController.shutdown();
    }

    public static Draggable draggable(Module module, String text, float floatValue, float floatValueValue) {
        DraggableStorage.draggables.put(text, new Draggable(module, text, floatValue, floatValueValue));
        return DraggableStorage.draggables.get(text);
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo == null ? UserInfo.empty() : userInfo;
    }

    public UserInfo getUserInfo() {
        return this.userInfo;
    }
}
