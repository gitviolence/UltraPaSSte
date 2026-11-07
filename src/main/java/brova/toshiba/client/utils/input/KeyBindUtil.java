package brova.toshiba.client.utils.input;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.core.PanicController;
import brova.toshiba.client.event.impl.InputDevice;
import brova.toshiba.client.event.impl.KeyBind;
import brova.toshiba.client.gui.click.ClickGuiScreen;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.ModuleRegistry;
import brova.toshiba.client.utils.audio.SoundUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.lwjgl.glfw.GLFW;

public final class KeyBindUtil implements MinecraftAccess {

    public static final int MOUSE_BUTTON_OFFSET = 1000;

    public static void call(int intValue, int intValueValue) {
        if (!PanicController.isActive() && intValue > -1 && intValueValue == 1) {
            if (intValue == 344) {
                SoundUtil.playSound("opengui.wav", 0.6, 1.0F);
                mc.setScreen(new ClickGuiScreen());
            }
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(new KeyBind(intValue, InputDevice.KEYBOARD), new Object[] {}, (accessReceiver, accessArguments) -> {
                ((KeyBind) accessReceiver).call();
            });
            var object = ModuleRegistry.INSTANCE.getObject();
            int intValueResult = 0;
            for (int size = object.size(); intValueResult < size; intValueResult++) {
                Module module = object.get(intValueResult);
                if (module.getKey() == intValue) {
                    module.toggle();
                }
            }
        }
    }

    public static String getKeyName(int intValue) {
        if (intValue == -1) {
            return "None";
        } else {
            String text = GLFW.glfwGetKeyName(intValue, 0);
            if (text != null) {
                return text.toUpperCase();
            } else {
                return switch(intValue) {
                    case 32 ->
                        "SPACE";
                    case 256 ->
                        "ESC";
                    case 340 ->
                        "LSHIFT";
                    case 341 ->
                        "LCTRL";
                    case 344 ->
                        "RSHIFT";
                    case 345 ->
                        "RCTRL";
                    default ->
                        "KEY" + intValue;
                };
            }
        }
    }

    public static void callMouse(int intValue, int intValueValue) {
        if (!PanicController.isActive() && mc.currentScreen == null && intValue >= 0 && intValueValue == 1) {
            int intValueResult = 1000 + intValue;
            new KeyBind(intValueResult, InputDevice.MOUSE).call();
            var object = ModuleRegistry.INSTANCE.getObject();
            int intValueItem = 0;
            for (int size = object.size(); intValueItem < size; intValueItem++) {
                Module module = object.get(intValueItem);
                if (module.getKey() == intValueResult) {
                    brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(module, new Object[] {}, (accessReceiver, accessArguments) -> {
                        ((Module) accessReceiver).toggle();
                    });
                }
            }
        }
    }

    public static boolean isBindHeld(int intValue) {
        if (intValue == -1) {
            return false;
        } else {
            long handle = mc.getWindow().getHandle();
            if (intValue >= 1000) {
                int intValueValue = intValue - 1000;
                return GLFW.glfwGetMouseButton(handle, intValueValue) == 1;
            } else {
                return GLFW.glfwGetKey(handle, intValue) == 1;
            }
        }
    }

    public static boolean isBindPressed(int intValue) {
        return isBindHeld(intValue);
    }

    public static String getBindName(int intValue) {
        if (intValue == -1) {
            return "n/a";
        } else if (intValue >= 1000) {
            int intValueValue = intValue - 1000;
            return switch(intValueValue) {
                case 0 ->
                    "ЛКМ";
                case 1 ->
                    "ПКМ";
                case 2 ->
                    "СКМ";
                case 3 ->
                    "MOUSE4";
                case 4 ->
                    "MOUSE5";
                default ->
                    "MOUSE" + (intValueValue + 1);
            };
        } else if (intValue >= 65 && intValue <= 90) {
            return String.valueOf((char) (65 + (intValue - 65)));
        } else if (intValue >= 48 && intValue <= 57) {
            return String.valueOf((char) (48 + (intValue - 48)));
        } else {
            String text = switch(intValue) {
                case 39 ->
                    "'";
                case 44 ->
                    ",";
                case 45 ->
                    "-";
                case 46 ->
                    ".";
                case 47 ->
                    "/";
                case 59 ->
                    ";";
                case 61 ->
                    "=";
                case 91 ->
                    "[";
                case 92 ->
                    "\\";
                case 93 ->
                    "]";
                case 96 ->
                    "`";
                default ->
                    null;
            };
            if (text != null) {
                return text;
            } else {
                return switch(intValue) {
                    case 32 ->
                        "SPACE";
                    case 256 ->
                        "ESC";
                    case 257 ->
                        "ENTER";
                    case 258 ->
                        "TAB";
                    case 259 ->
                        "BACKSPACE";
                    case 260 ->
                        "INSERT";
                    case 261 ->
                        "DELETE";
                    case 262 ->
                        "RIGHT";
                    case 263 ->
                        "LEFT";
                    case 264 ->
                        "DOWN";
                    case 265 ->
                        "UP";
                    case 266 ->
                        "PAGEUP";
                    case 267 ->
                        "PAGEDOWN";
                    case 268 ->
                        "HOME";
                    case 269 ->
                        "END";
                    case 280 ->
                        "CAPS";
                    case 290 ->
                        "F1";
                    case 291 ->
                        "F2";
                    case 292 ->
                        "F3";
                    case 293 ->
                        "F4";
                    case 294 ->
                        "F5";
                    case 295 ->
                        "F6";
                    case 296 ->
                        "F7";
                    case 297 ->
                        "F8";
                    case 298 ->
                        "F9";
                    case 299 ->
                        "F10";
                    case 300 ->
                        "F11";
                    case 301 ->
                        "F12";
                    case 320 ->
                        "NUM0";
                    case 321 ->
                        "NUM1";
                    case 322 ->
                        "NUM2";
                    case 323 ->
                        "NUM3";
                    case 324 ->
                        "NUM4";
                    case 325 ->
                        "NUM5";
                    case 326 ->
                        "NUM6";
                    case 327 ->
                        "NUM7";
                    case 328 ->
                        "NUM8";
                    case 329 ->
                        "NUM9";
                    case 330 ->
                        "NUM.";
                    case 331 ->
                        "NUM/";
                    case 332 ->
                        "NUM*";
                    case 333 ->
                        "NUM-";
                    case 334 ->
                        "NUM+";
                    case 335 ->
                        "NUMENTER";
                    case 340 ->
                        "LSHIFT";
                    case 341 ->
                        "LCTRL";
                    case 342 ->
                        "LALT";
                    case 344 ->
                        "RSHIFT";
                    case 345 ->
                        "RCTRL";
                    case 346 ->
                        "RALT";
                    default ->
                        "KEY" + intValue;
                };
            }
        }
    }

    public static boolean isMouseButton(int intValue) {
        return intValue >= 1000;
    }

    public static int getMouseButtonFromKey(int intValue) {
        return isMouseButton(intValue) ? intValue - 1000 : -1;
    }

    public static int createMouseBind(int intValue) {
        return 1000 + intValue;
    }

    private KeyBindUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
