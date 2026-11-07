package brova.toshiba.client.modules.impl.player;

import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.settings.impl.BooleanSetting;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BeaconBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.CakeBlock;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.CartographyTableBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ChiseledBookshelfBlock;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.CrafterBlock;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.DecoratedPotBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.DropperBlock;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.GrindstoneBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.SmithingTableBlock;
import net.minecraft.block.StonecutterBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;

public final class NoInteract extends Module {

    public static NoInteract INSTANCE = new NoInteract();

    public final BooleanSetting blocks = new BooleanSetting("Блоки", true);

    public final BooleanSetting armorStands = new BooleanSetting("Стойки для брони", true);

    public final BooleanSetting frames = new BooleanSetting("Рамки", true);

    public NoInteract() {
        super("NoInteract", "Отключает взаимодействие со стойками с броней, верстаками и другими интерактивными блоками", ModuleCategory.PLAYER);
        this.addSettings(this.blocks, this.armorStands, this.frames);
    }

    public boolean shouldBlockBlock(Block block) {
        return this.isEnable() && this.blocks.isState() ? block instanceof ChestBlock || block instanceof EnderChestBlock || block instanceof BarrelBlock || block instanceof ShulkerBoxBlock || block instanceof CraftingTableBlock || block instanceof AnvilBlock || block instanceof AbstractFurnaceBlock || block instanceof LoomBlock || block instanceof CartographyTableBlock || block instanceof GrindstoneBlock || block instanceof StonecutterBlock || block instanceof SmithingTableBlock || block instanceof HopperBlock || block instanceof DispenserBlock || block instanceof DropperBlock || block instanceof BrewingStandBlock || block instanceof EnchantingTableBlock || block instanceof LecternBlock || block instanceof BeaconBlock || block instanceof JukeboxBlock || block instanceof NoteBlock || block instanceof RepeaterBlock || block instanceof ComparatorBlock || block instanceof LeverBlock || block instanceof ButtonBlock || block instanceof DoorBlock || block instanceof TrapdoorBlock || block instanceof FenceGateBlock || block instanceof BedBlock || block instanceof CakeBlock || block instanceof RespawnAnchorBlock || block instanceof ChiseledBookshelfBlock || block instanceof CrafterBlock || block instanceof DecoratedPotBlock || block instanceof CampfireBlock || block instanceof FlowerPotBlock || block instanceof AbstractCauldronBlock || block instanceof ComposterBlock || block instanceof BellBlock : false;
    }

    public boolean shouldBlockEntity(Entity entity) {
        if (!this.isEnable()) {
            return false;
        } else {
            return this.armorStands.isState() && entity instanceof ArmorStandEntity ? true : this.frames.isState() && entity instanceof ItemFrameEntity;
        }
    }
}
