package brova.toshiba.client.storages.implement;

import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.modules.impl.render.Hud;
import brova.toshiba.client.storages.implement.ThemeStorage.Themes;
import brova.toshiba.client.storages.implement.helpertstorages.Theme;
import brova.toshiba.client.utils.render.ColorMathUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;

public class ThemeStorage {

    private ObjectArrayList<Themes> themeList = new ObjectArrayList<>();

    private Themes themes;

    public ThemeStorage() {
        this.onInitialize();
    }

    private void onInitialize() {
        this.themeList.addAll(Arrays.asList(Themes.Custom, Themes.Purple, Themes.Red, Themes.Blue, Themes.Green, Themes.Pink, Themes.Orange, Themes.Blues, Themes.Yellows));
        this.themes = this.themeList.get(1);
    }

    public ObjectArrayList<Themes> getThemeList() {
        return this.themeList;
    }

    public Themes getThemes() {
        return this.themes;
    }

    public void setThemeList(ObjectArrayList<Themes> themeList) {
        this.themeList = themeList;
    }

    public void setThemes(Themes themes) {
        this.themes = themes;
    }

    public enum Themes {

        Custom(new Theme("Custom", ColorMathUtil.rgba(100, 150, 255, 255), ColorMathUtil.darken(ColorMathUtil.rgba(100, 150, 255, 255), 0.35F))),
        Purple(new Theme("Lavender", ColorMathUtil.rgba(190, 143, 255, 255), ColorMathUtil.darken(ColorMathUtil.rgba(190, 143, 255, 255), 0.35F))),
        Red(new Theme("Blood", ColorMathUtil.rgba(230, 50, 57, 255), ColorMathUtil.darken(ColorMathUtil.rgba(230, 50, 57, 255), 0.35F))),
        Blue(new Theme("Ocean", ColorMathUtil.rgba(95, 113, 191, 255), ColorMathUtil.darken(ColorMathUtil.rgba(95, 113, 191, 255), 0.35F))),
        Green(new Theme("Emerald", ColorMathUtil.rgba(60, 220, 140, 255), ColorMathUtil.darken(ColorMathUtil.rgba(60, 220, 140, 255), 0.35F))),
        Pink(new Theme("Rose", ColorMathUtil.rgba(255, 120, 190, 255), ColorMathUtil.darken(ColorMathUtil.rgba(255, 120, 190, 255), 0.35F))),
        Orange(new Theme("Gold", ColorMathUtil.rgba(252, 192, 88, 255), ColorMathUtil.darken(ColorMathUtil.rgba(252, 192, 88, 255), 0.35F))),
        Blues(new Theme("Diamond", ColorMathUtil.rgba(125, 217, 250, 255), ColorMathUtil.darken(ColorMathUtil.rgba(125, 217, 250, 255), 0.35F))),
        Yellows(new Theme("Sun", ColorMathUtil.rgba(252, 231, 88, 255), ColorMathUtil.darken(ColorMathUtil.rgba(252, 231, 88, 255), 0.35F)));

        public final Theme theme;

        public Theme getTheme() {
            if (this == Custom) {
                try {
                    Hud interfaceModule = ModuleRegistry.interfaceModule;
                    if (interfaceModule != null && interfaceModule.colorSetting != null) {
                        int color = interfaceModule.colorSetting.getColor();
                        brova.toshiba.client.utils.runtime.AccessGuard.read(this.theme, accessReceiver -> accessReceiver.color)[0] = color;
                        brova.toshiba.client.utils.runtime.AccessGuard.read(this.theme, accessReceiver -> accessReceiver.color)[1] = ColorMathUtil.darken(color, 0.35F);
                    }
                } catch (Exception failure) {
                }
            }
            return this.theme;
        }

        private Themes(Theme nullxx) {
            this.theme = nullxx;
        }
    }
}
