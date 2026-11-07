package brova.toshiba.client.modules.impl.render;

import brova.toshiba.Toshiba;
import brova.toshiba.client.event.EventHandler;
import brova.toshiba.client.event.impl.HudRenderEvent;
import brova.toshiba.client.gui.click.BackgroundSettings;
import brova.toshiba.client.gui.hud.BindListWidget;
import brova.toshiba.client.gui.hud.CooldownsWidget;
import brova.toshiba.client.gui.hud.CoordinatesWidget;
import brova.toshiba.client.gui.hud.HudWidgetBase;
import brova.toshiba.client.gui.hud.KeybindsWidget;
import brova.toshiba.client.gui.hud.NotificationsWidget;
import brova.toshiba.client.gui.hud.PotionsWidget;
import brova.toshiba.client.gui.hud.TargetHudWidget;
import brova.toshiba.client.gui.hud.WatermarkWidget;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.settings.impl.ColorSetting;
import brova.toshiba.client.settings.impl.MultiSetting;
import brova.toshiba.client.utils.profile.PerformanceProfiler;
import brova.toshiba.client.utils.render.transform.TransformStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Hud extends Module {

    public static Hud INSTANCE = new Hud();

    private static final ConcurrentHashMap<String, Long> longEntries = new ConcurrentHashMap<>();

    private static final boolean stateFlag = Boolean.parseBoolean(System.getProperty("toshiba.perf.debug", "false"));

    private static final long lastUpdateTimestampForHudRender = Long.getLong("toshiba.perf.hudMs", 5L) * 1000000L;

    private static final long lastUpdateTimestampForUpdateSimpleNameWithClass = Long.getLong("toshiba.perf.cooldownMs", 1000L) * 1000000L;

    private final WatermarkWidget waterMark;

    private final BindListWidget keyBinds;

    private final PotionsWidget potions;

    private final CooldownsWidget cooldowns;

    private final NotificationsWidget notifications;

    private final TargetHudWidget targetHud;

    private final CoordinatesWidget information;

    private final KeybindsWidget screenBinds;

    public final MultiSetting hudModules = new MultiSetting("Элементы", new BooleanSetting("Ватермарка", true), new BooleanSetting("Горячие клавиши", true), new BooleanSetting("Зелья", true), new BooleanSetting("Кулдаун", true), new BooleanSetting("Таргет худ", true), new BooleanSetting("Уведомления", true), new BooleanSetting("Информация", true));

    public final MultiSetting notificationActions = new MultiSetting("Действия в Notifications", new BooleanSetting("Окончание эффекта", true), new BooleanSetting("При использывание предмета", true), new BooleanSetting("При потере тотема", true)).visible(() -> this.hudModules.is("Уведомления"));

    public final BooleanSetting showOnHover = new BooleanSetting("Показывать при наведении", false).visible(() -> this.hudModules.is("Таргет худ"));

    public final BooleanSetting hideSettingBinds = new BooleanSetting("Убрать бинды настроек", false).visible(() -> this.hudModules.is("Горячие клавиши"));

    public final ColorSetting colorSetting = new ColorSetting("Цвет темы", -10184961);

    public Hud() {
        super("Hud", "Красивый,удобный вспомогательный HUD клиента.", ModuleCategory.RENDER);
        this.addSettings(this.hudModules, this.notificationActions, this.showOnHover, this.hideSettingBinds, this.colorSetting);
        this.waterMark = new WatermarkWidget(Toshiba.draggable(this, "WaterMark", 10.0F, 10.0F));
        this.keyBinds = new BindListWidget(Toshiba.draggable(this, "KeyBinds", 30.0F, 30.0F));
        this.potions = new PotionsWidget(Toshiba.draggable(this, "Potions", 30.0F, 60.0F));
        this.cooldowns = new CooldownsWidget(Toshiba.draggable(this, "Cooldowns", 130.0F, 60.0F));
        this.information = new CoordinatesWidget(Toshiba.draggable(this, "Information", 50.0F, 100.0F));
        this.notifications = new NotificationsWidget(Toshiba.draggable(this, "Notifications", 0.0F, 0.0F));
        this.targetHud = new TargetHudWidget(Toshiba.draggable(this, "TargetHud", 30.0F, 90.0F));
        this.screenBinds = new KeybindsWidget(Toshiba.draggable(this, "ScreenBinds", 100.0F, 150.0F));
    }

    private void onHudRender(HudWidgetBase hudWidgetBase, HudRenderEvent event) {
        PerformanceProfiler INSTANCE = PerformanceProfiler.INSTANCE;
        boolean flag = INSTANCE.isRunning() || stateFlag;
        long longValue = flag ? System.nanoTime() : 0L;
        TransformStack.push(event.getContext().getMatrices(), BackgroundSettings.HUD_SCALE.get(), hudWidgetBase.draggable.getX(), hudWidgetBase.draggable.getY());
        try {
            hudWidgetBase.onRender(event);
        } finally {
            TransformStack.pop(event.getContext().getMatrices());
            if (flag) {
                long elapsedNanos = System.nanoTime() - longValue;
                INSTANCE.record("Hud:" + hudWidgetBase.getClass().getSimpleName(), elapsedNanos);
                if (elapsedNanos >= lastUpdateTimestampForHudRender) {
                    this.updateSimpleNameWithClass(hudWidgetBase, elapsedNanos);
                }
            }
        }
    }

    private void updateSimpleNameWithClass(HudWidgetBase hudWidgetBase, long longValue) {
        String simpleName = hudWidgetBase.getClass().getSimpleName();
        long currentTimeNanos = System.nanoTime();
        Long timestamp = longEntries.get(simpleName);
        if (timestamp == null || currentTimeNanos - timestamp >= lastUpdateTimestampForUpdateSimpleNameWithClass) {
            longEntries.put(simpleName, currentTimeNanos);
            System.err.println(String.format(Locale.ROOT, "[PerfDebug] Slow HUD element: Hud -> %s took %.2f ms", simpleName, longValue / 1000000.0));
        }
    }

    public Map<String, HudWidgetBase> getConfigurableHudElements() {
        LinkedHashMap<String, brova.toshiba.client.gui.hud.HudWidgetBase> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put("waterMark", this.waterMark);
        linkedHashMap.put("keyBinds", this.keyBinds);
        linkedHashMap.put("potions", this.potions);
        linkedHashMap.put("cooldowns", this.cooldowns);
        linkedHashMap.put("notifications", this.notifications);
        linkedHashMap.put("targetHud", this.targetHud);
        linkedHashMap.put("information", this.information);
        linkedHashMap.put("screenBinds", this.screenBinds);
        return linkedHashMap;
    }

    @EventHandler(priority = -200)
    public void onEvent(HudRenderEvent event) {
        boolean flag = this.hudModules.is("Ватермарка");
        boolean flagValue = this.hudModules.is("Горячие клавиши");
        boolean flagResult = this.hudModules.is("Зелья");
        boolean flagItem = this.hudModules.is("Кулдаун");
        boolean flagEntry = this.hudModules.is("Информация");
        boolean flagCopy = this.hudModules.is("Уведомления");
        boolean flag2 = this.hudModules.is("Таргет худ");
        boolean state = ModuleRegistry.serverHelper.getScreenBindsSetting().isState();
        boolean flag3 = flag || flagValue || flagResult || flagItem || flagEntry || flagCopy || flag2 || state;
        if (flag3) {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            try {
                if (flag) {
                    this.onHudRender(this.waterMark, event);
                }
                if (flagValue) {
                    this.onHudRender(this.keyBinds, event);
                }
                if (flagResult) {
                    this.onHudRender(this.potions, event);
                }
                if (flagItem) {
                    this.onHudRender(this.cooldowns, event);
                }
                if (flagEntry) {
                    this.onHudRender(this.information, event);
                }
                if (flagCopy) {
                    this.onHudRender(this.notifications, event);
                }
                if (flag2) {
                    this.onHudRender(this.targetHud, event);
                }
                if (state) {
                    this.onHudRender(this.screenBinds, event);
                }
            } finally {
                RenderSystem.depthMask(true);
                RenderSystem.enableDepthTest();
            }
        }
    }
}
