package brova.toshiba.client.core;

public interface OptimizationFlags {

    boolean toshiba$hideArmor();

    boolean toshiba$hideHeldItems();

    boolean toshiba$hideCape();

    boolean toshiba$hideElytra();

    boolean toshiba$hideHeadItems();

    boolean toshiba$hideVanillaNameTag();

    void toshiba$setOptimizationFlags(boolean flag, boolean flagValue, boolean flagResult, boolean flagItem, boolean flagEntry, boolean flagCopy);
}
