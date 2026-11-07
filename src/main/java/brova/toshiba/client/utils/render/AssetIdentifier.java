package brova.toshiba.client.utils.render;

import net.minecraft.util.Identifier;

public class AssetIdentifier {

    public static Identifier of(String text) {
        return Identifier.of("toshiba", text);
    }
}
