package brova.toshiba.client.gui.click;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.utils.audio.SoundUtil;
import brova.toshiba.client.utils.render.BackgroundCapture;
import brova.toshiba.client.utils.render.animation.Animation;
import brova.toshiba.client.utils.render.animation.PowerEasing;
import brova.toshiba.client.utils.render.font.SystemFontManager;
import brova.toshiba.client.utils.render.transform.TransformStack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class ClickGuiScreen extends Screen implements MinecraftAccess {

    private static final ClickGuiController clickGuiController = new ClickGuiController();

    private final int position = ModuleCategory.values().length;

    private final ClickGuiController renderOffsetYClickGuiController = clickGuiController;

    private final ThemeSelector themeSelector = new ThemeSelector();

    private final ProfilePanel profilePanel = new ProfilePanel(this.renderOffsetYClickGuiController);

    private final ClickGuiRenderer clickGuiRenderer = new ClickGuiRenderer(this.renderOffsetYClickGuiController, new SettingRenderer(), this.themeSelector, this.profilePanel);

    private final ClickGuiInputHandler mouseClickedClickGuiInputHandler = new ClickGuiInputHandler(this.renderOffsetYClickGuiController, this.themeSelector);

    private final Animation animation = new Animation(0.0F, 7.5F, PowerEasing.CUBIC_OUT);

    private final BackgroundCapture backgroundCapture = new BackgroundCapture();

    private boolean stateFlagForRender;

    private boolean stateFlagForRemoved;

    private float mouseDragged = 1.0F;

    public ClickGuiScreen() {
        super(Text.of("ClickGui"));
        this.renderOffsetYClickGuiController.refreshModules();
    }

    public static void preloadResources() {
        updateText("Manrope-Bold.ttf", 12, 14, 16, 17);
        updateText("icon2.ttf", 14, 23, 25);
        updateText("rich_reg.ttf", 14, 25);
        updateText("relic.ttf", 14, 25);
        updateText("rain.ttf", 14, 25);
        updateText("rain2.ttf", 14, 25);
    }

    private static void updateText(String text, int... intValue) {
        for (int intValueValue : intValue) {
            SystemFontManager.getFont(text, intValueValue);
        }
    }

    private Window createWindow() {
        return mc == null ? null : mc.getWindow();
    }

    private void updatePosition() {
        Window window = this.createWindow();
        if (window != null) {
            this.renderOffsetYClickGuiController.updatePosition(window, this.position);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (mc != null && mc.player != null && mc.currentScreen == this) {
            KeyBinding[] keyBindings = new KeyBinding[] { mc.options.forwardKey, mc.options.backKey, mc.options.leftKey, mc.options.rightKey, mc.options.jumpKey, mc.options.sprintKey };
            for (KeyBinding keyBinding : keyBindings) {
                boolean pressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), keyBinding.getDefaultKey().getCode());
                keyBinding.setPressed(pressed);
            }
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Window window = this.createWindow();
        if (window != null) {
            this.updateEasingWithUpdate();
            float floatValue = this.calculateFloat();
            if (!this.stateFlagForRender || !(floatValue <= 0.001F)) {
                this.renderOffsetYClickGuiController.updatePosition(window, this.position);
                this.renderOffsetYClickGuiController.setRenderOffsetY(this.calculateFloat(floatValue));
                this.mouseDragged = MathHelper.clamp(BackgroundSettings.GUI_SCALE.get(), 0.3F, 2.0F);
                int intValue = (int) Math.round(this.calculateScaledWidth(mouseX));
                int intValueValue = (int) Math.round(this.calculateScaledHeight(mouseY));
                boolean flag = this.backgroundCapture.captureBackground(context, floatValue);
                TransformStack.push(context.getMatrices(), this.mouseDragged, window.getScaledWidth() * 0.5F, window.getScaledHeight() * 0.5F);
                try {
                    this.clickGuiRenderer.render(context, intValue, intValueValue, window, flag ? 1.0F : floatValue);
                } finally {
                    TransformStack.pop(context.getMatrices());
                }
                super.render(context, mouseX, mouseY, delta);
                if (flag) {
                    this.backgroundCapture.render(context, floatValue);
                }
            } else if (mc != null) {
                mc.setScreen(null);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.stateFlagForRender) {
            return true;
        } else {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                ((ClickGuiScreen) accessReceiver).updatePosition();
            });
            mouseX = this.calculateScaledWidth(mouseX);
            mouseY = this.calculateScaledHeight(mouseY);
            this.renderOffsetYClickGuiController.setRenderOffsetY(this.calculateFloat(this.calculateFloat()));
            return this.profilePanel.mouseClicked(mouseX, mouseY, button, this.renderOffsetYClickGuiController.getX(), this.renderOffsetYClickGuiController.getY() + this.renderOffsetYClickGuiController.getRenderOffsetY()) || this.mouseClickedClickGuiInputHandler.mouseClicked(mouseX, mouseY, button, this.createWindow()) || super.mouseClicked(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.updatePosition();
        mouseX = this.calculateScaledWidth(mouseX);
        mouseY = this.calculateScaledHeight(mouseY);
        if (this.profilePanel.mouseReleased(mouseX, mouseY, button, this.renderOffsetYClickGuiController.getX(), this.renderOffsetYClickGuiController.getY() + this.renderOffsetYClickGuiController.getRenderOffsetY())) {
            return true;
        } else {
            return this.stateFlagForRender ? true : this.mouseClickedClickGuiInputHandler.mouseReleased(button) || super.mouseReleased(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!this.stateFlagForRender && !this.profilePanel.isAvatarHeld()) {
            this.updatePosition();
            mouseX = this.calculateScaledWidth(mouseX);
            mouseY = this.calculateScaledHeight(mouseY);
            this.renderOffsetYClickGuiController.setRenderOffsetY(this.calculateFloat(this.calculateFloat()));
            return this.profilePanel.mouseDragged(mouseX, button, this.renderOffsetYClickGuiController.getX()) || this.mouseClickedClickGuiInputHandler.mouseDragged(mouseX, mouseY, button) || super.mouseDragged(mouseX, mouseY, button, deltaX / this.mouseDragged, deltaY / this.mouseDragged);
        } else {
            return true;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!this.stateFlagForRender && !this.profilePanel.isAvatarHeld()) {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] {}, (accessReceiver, accessArguments) -> {
                ((ClickGuiScreen) accessReceiver).updatePosition();
            });
            mouseX = this.calculateScaledWidth(mouseX);
            mouseY = this.calculateScaledHeight(mouseY);
            this.renderOffsetYClickGuiController.setRenderOffsetY(this.calculateFloat(this.calculateFloat()));
            return this.mouseClickedClickGuiInputHandler.mouseScrolled(mouseX, mouseY, verticalAmount) || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        } else {
            return true;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.stateFlagForRender) {
            return true;
        } else if (keyCode == 256 && this.profilePanel.closeProfilePanel()) {
            return true;
        } else if (this.mouseClickedClickGuiInputHandler.keyPressed(keyCode, modifiers)) {
            return true;
        } else if (keyCode == 256) {
            this.updateBeginClosingWithEasing();
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return this.stateFlagForRender ? true : this.mouseClickedClickGuiInputHandler.charTyped(chr) || super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        this.updateBeginClosingWithEasing();
    }

    @Override
    public void removed() {
        this.backgroundCapture.close();
        this.profilePanel.resetOverlays();
        if (!this.stateFlagForRemoved) {
            this.stateFlagForRemoved = true;
            SoundUtil.playSound("closegui.wav", 0.6, 1.0F);
        }
        super.removed();
    }

    private void updateBeginClosingWithEasing() {
        if (!this.stateFlagForRender) {
            this.stateFlagForRender = true;
            this.profilePanel.beginClosing();
            this.animation.setEasing(PowerEasing.CUBIC_IN);
            if (!this.stateFlagForRemoved) {
                this.stateFlagForRemoved = true;
                SoundUtil.playSound("closegui.wav", 0.6, 1.0F);
            }
        }
    }

    private void updateEasingWithUpdate() {
        if (this.stateFlagForRender) {
            float floatValue = 0.0F;
            Animation animation = this.animation;
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(animation, new Object[] { floatValue }, (accessReceiver, accessArguments) -> {
                ((Animation) accessReceiver).update(((float) accessArguments[0]));
            });
        } else {
            this.animation.setEasing(PowerEasing.CUBIC_OUT);
            this.animation.update(1.0F);
        }
    }

    private float calculateFloat() {
        return MathHelper.clamp(this.animation.getValue(), 0.0F, 1.0F);
    }

    private float calculateFloat(float floatValue) {
        return (1.0F - floatValue) * 22.0F;
    }

    private double calculateScaledWidth(double doubleValue) {
        Window window = this.createWindow();
        if (window == null) {
            return doubleValue;
        } else {
            double doubleValueValue = window.getScaledWidth() * 0.5;
            return doubleValueValue + (doubleValue - doubleValueValue) / this.mouseDragged;
        }
    }

    private double calculateScaledHeight(double doubleValue) {
        Window window = this.createWindow();
        if (window == null) {
            return doubleValue;
        } else {
            double doubleValueValue = window.getScaledHeight() * 0.5;
            return doubleValueValue + (doubleValue - doubleValueValue) / this.mouseDragged;
        }
    }
}
