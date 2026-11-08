package brova.toshiba.client.modules;

import brova.toshiba.client.modules.impl.combat.AimAssist;
import brova.toshiba.client.modules.impl.combat.AntiBot;
import brova.toshiba.client.modules.impl.combat.AttackAura;
import brova.toshiba.client.modules.impl.combat.AutoExplosion;
import brova.toshiba.client.modules.impl.combat.AutoSwap;
import brova.toshiba.client.modules.impl.combat.AutoTotem;
import brova.toshiba.client.modules.impl.combat.CrystalAura;
import brova.toshiba.client.modules.impl.combat.ElytraMotion;
import brova.toshiba.client.modules.impl.combat.ElytraTarget;
import brova.toshiba.client.modules.impl.combat.HitBox;
import brova.toshiba.client.modules.impl.combat.MaceAura;
import brova.toshiba.client.modules.impl.combat.MaceSwap;
import brova.toshiba.client.modules.impl.combat.PacketCriticals;
import brova.toshiba.client.modules.impl.combat.TapeMouse;
import brova.toshiba.client.modules.impl.combat.TpsSync;
import brova.toshiba.client.modules.impl.combat.TriggerBot;
import brova.toshiba.client.modules.impl.combat.WebTrap;
import brova.toshiba.client.modules.impl.ghost.Panic;
import brova.toshiba.client.modules.impl.misc.AppleFarmer;
import brova.toshiba.client.modules.impl.misc.AuctionHelper;
import brova.toshiba.client.modules.impl.misc.AutoCodes;
import brova.toshiba.client.modules.impl.misc.AutoLeave;
import brova.toshiba.client.modules.impl.misc.AutoTPAccept;
import brova.toshiba.client.modules.impl.misc.ClickPearl;
import brova.toshiba.client.modules.impl.misc.ClientSounds;
import brova.toshiba.client.modules.impl.misc.ElytraSwap;
import brova.toshiba.client.modules.impl.misc.FakePlayer;
import brova.toshiba.client.modules.impl.misc.NameProtect;
import brova.toshiba.client.modules.impl.misc.ServerHelper;
import brova.toshiba.client.modules.impl.misc.SpaceJoiner;
import brova.toshiba.client.modules.impl.render.ESP;
import brova.toshiba.client.modules.impl.movement.AirStuck;
import brova.toshiba.client.modules.impl.movement.Fly;
import brova.toshiba.client.modules.impl.movement.FreeCam;
import brova.toshiba.client.modules.impl.movement.GuiMove;
import brova.toshiba.client.modules.impl.movement.HighJump;
import brova.toshiba.client.modules.impl.movement.NoSlow;
import brova.toshiba.client.modules.impl.movement.NoWeb;
import brova.toshiba.client.modules.impl.movement.Speed;
import brova.toshiba.client.modules.impl.movement.Sprint;
import brova.toshiba.client.modules.impl.movement.SuperFirework;
import brova.toshiba.client.modules.impl.movement.Velocity;
import brova.toshiba.client.modules.impl.player.AutoEat;
import brova.toshiba.client.modules.impl.player.AutoFish;
import brova.toshiba.client.modules.impl.player.AutoSell;
import brova.toshiba.client.modules.impl.player.AutoTool;
import brova.toshiba.client.modules.impl.player.ChestStealer;
import brova.toshiba.client.modules.impl.player.CommandFix;
import brova.toshiba.client.modules.impl.player.CordDropper;
import brova.toshiba.client.modules.impl.player.FastBreak;
import brova.toshiba.client.modules.impl.player.ItemScroller;
import brova.toshiba.client.modules.impl.player.LockSlot;
import brova.toshiba.client.modules.impl.player.NoDelay;
import brova.toshiba.client.modules.impl.player.NoInteract;
import brova.toshiba.client.modules.impl.player.NoPush;
import brova.toshiba.client.modules.impl.player.PvPSave;
import brova.toshiba.client.modules.impl.player.ScoreboardHP;
import brova.toshiba.client.modules.impl.player.ServerRPSpoofer;
import brova.toshiba.client.modules.impl.player.TargetPearl;
import brova.toshiba.client.modules.impl.player.WindHop;
import brova.toshiba.client.modules.impl.render.Arrows;
import brova.toshiba.client.modules.impl.render.AspectRatio;
import brova.toshiba.client.modules.impl.render.AttackEffect;
import brova.toshiba.client.modules.impl.render.BlockESP;
import brova.toshiba.client.modules.impl.render.BlockOverlay;
import brova.toshiba.client.modules.impl.render.ColorSaturation;
import brova.toshiba.client.modules.impl.render.CustomSize;
import brova.toshiba.client.modules.impl.render.CustomWorld;
import brova.toshiba.client.modules.impl.render.DamagePop;
import brova.toshiba.client.modules.impl.render.FireworkESP;
import brova.toshiba.client.modules.impl.render.FullBright;
import brova.toshiba.client.modules.impl.render.Hud;
import brova.toshiba.client.modules.impl.render.ItemTime;
import brova.toshiba.client.modules.impl.render.JumpCircle;
import brova.toshiba.client.modules.impl.render.NameTags;
import brova.toshiba.client.modules.impl.render.NoRender;
import brova.toshiba.client.modules.impl.render.Particle;
import brova.toshiba.client.modules.impl.render.Predictions;
import brova.toshiba.client.modules.impl.render.SaturationEat;
import brova.toshiba.client.modules.impl.render.SeeInvisibles;
import brova.toshiba.client.modules.impl.render.ShaderESP;
import brova.toshiba.client.modules.impl.render.ShaderHands;
import brova.toshiba.client.modules.impl.render.ShulkerPreview;
import brova.toshiba.client.modules.impl.render.Souls;
import brova.toshiba.client.modules.impl.render.SpinBot;
import brova.toshiba.client.modules.impl.render.SwingAnimations;
import brova.toshiba.client.modules.impl.render.TargetESP;
import brova.toshiba.client.modules.impl.render.TotemPop;
import brova.toshiba.client.modules.impl.render.Trajectory;
import brova.toshiba.client.modules.impl.render.ViewModel;
import brova.toshiba.client.modules.impl.render.WardenEsp;
import brova.toshiba.client.storages.ObjectStorage;

public class ModuleRegistry extends ObjectStorage<Module> {

    public static final ModuleRegistry INSTANCE = new ModuleRegistry();

    public static final AntiBot antibot = AntiBot.INSTANCE;

    public static final AirStuck airStuck = AirStuck.INSTANCE;

    public static final Arrows arrows = Arrows.INSTANCE;

    public static final AspectRatio aspectRatio = new AspectRatio();

    public static final AimAssist aimAssist = AimAssist.INSTANCE;

    public static final HitBox hitBox = new HitBox();

    public static final AttackAura aura = AttackAura.INSTANCE;

    public static final MaceAura maceAura = MaceAura.INSTANCE;

    public static final AutoTool autoTool = new AutoTool();

    public static final MaceSwap maceSwap = new MaceSwap();

    public static final TapeMouse tapeMouse = TapeMouse.INSTANCE;

    public static final AutoTPAccept autoAccept = AutoTPAccept.INSTANCE;

    public static final AuctionHelper auctionHelper = AuctionHelper.INSTANCE;

    public static final AutoFish autoFish = AutoFish.INSTANCE;

    public static final AutoCodes autoCodes = AutoCodes.INSTANCE;

    public static final TriggerBot triggerBot = new TriggerBot();

    public static final SpaceJoiner spaceJoiner = new SpaceJoiner();

    public static final AutoEat autoEat = AutoEat.INSTANCE;

    public static final AutoExplosion autoExplosion = AutoExplosion.INSTANCE;

    public static final CrystalAura crystalAura = new CrystalAura();

    public static final WebTrap webTrap = new WebTrap();

    public static final NameProtect nameProtect = NameProtect.INSTANCE;

    public static final ChestStealer chestStealer = ChestStealer.INSTANCE;

    public static final ClientSounds clientSounds = ClientSounds.INSTANCE;

    public static final ColorSaturation colorSaturation = new ColorSaturation();

    public static final DamagePop damagePop = DamagePop.INSTANCE;

    public static final AttackEffect attackEffect = AttackEffect.INSTANCE;

    public static final Panic panic = new Panic();

    public static final AutoTotem autoTotem = AutoTotem.INSTANCE;

    public static final BlockOverlay blockOverlay = BlockOverlay.INSTANCE;

    public static final ClickPearl clickPearl = ClickPearl.INSTANCE;

    public static final BlockESP blockesp = BlockESP.INSTANCE;

    public static final SuperFirework elytraBoost = SuperFirework.INSTANCE;

    public static final ElytraMotion elytraMotion = ElytraMotion.INSTANCE;

    public static final ElytraSwap elytraSwap = ElytraSwap.INSTANCE;

    public static final ElytraTarget elytraTarget = ElytraTarget.INSTANCE;

    public static final NameTags nameTags = NameTags.INSTANCE;

    public static final Fly fly = Fly.INSTANCE;

    public static final FreeCam freeCam = FreeCam.INSTANCE;

    public static final FullBright fullBright = FullBright.INSTANCE;

    public static final HighJump highJump = HighJump.INSTANCE;

    public static final Hud interfaceModule = Hud.INSTANCE;

    public static final AutoLeave autoLeave = AutoLeave.INSTANCE;

    public static final GuiMove guiMove = GuiMove.INSTANCE;

    public static final ItemScroller itemScroller = ItemScroller.INSTANCE;

    public static final JumpCircle jumpCircle = JumpCircle.INSTANCE;

    public static final NoDelay noDelay = NoDelay.INSTANCE;

    public static final NoPush noPush = NoPush.INSTANCE;

    public static final Velocity velocity = Velocity.INSTANCE;

    public static final NoWeb noWeb = NoWeb.INSTANCE;

    public static final PacketCriticals packetCriticals = PacketCriticals.INSTANCE;

    public static final Particle particle = Particle.INSTANCE;

    public static final FireworkESP fireworkESP = FireworkESP.INSTANCE;

    public static final FastBreak fastBreak = FastBreak.INSTANCE;

    public static final NoRender noRender = NoRender.INSTANCE;

    public static final ServerRPSpoofer rPSpoofer = ServerRPSpoofer.INSTANCE;

    public static final SeeInvisibles seeInvisibles = SeeInvisibles.INSTANCE;

    public static final ShaderESP shaderEsp = ShaderESP.INSTANCE;

    public static final ShaderHands shaderHands = ShaderHands.INSTANCE;

    public static final ESP esp = new ESP();

    public static final ShulkerPreview shulkerPreview = ShulkerPreview.INSTANCE;

    public static final Souls souls = Souls.INSTANCE;

    public static final Speed speed = Speed.INSTANCE;

    public static final SpinBot spinBot = new SpinBot();

    public static final Sprint sprint = Sprint.INSTANCE;

    public static final SwingAnimations swingAnimations = SwingAnimations.INSTANCE;

    public static final TargetESP targetESP = TargetESP.INSTANCE;

    public static final TargetPearl targetPearl = TargetPearl.INSTANCE;

    public static final ScoreboardHP scoreboardHP = ScoreboardHP.INSTANCE;

    public static final TotemPop totemAngel = TotemPop.INSTANCE;

    public static final TpsSync tpsSync = TpsSync.INSTANCE;

    public static final ViewModel viewModel = ViewModel.INSTANCE;

    public static final CustomSize customSize = CustomSize.INSTANCE;

    public static final CustomWorld worldTweaks = CustomWorld.INSTANCE;

    public static final AutoSwap autoSwap = AutoSwap.INSTANCE;

    public static final NoSlow noSlow = NoSlow.INSTANCE;

    public static final ServerHelper serverHelper = ServerHelper.INSTANCE;

    public static final Trajectory trajectories = Trajectory.INSTANCE;

    public static final Predictions predictions = Predictions.INSTANCE;

    public static final FakePlayer fakePlayer = FakePlayer.INSTANCE;

    public static final CommandFix commandFix = CommandFix.INSTANCE;

    public static final WindHop windHop = WindHop.INSTANCE;

    public static final NoInteract noInteract = NoInteract.INSTANCE;

    public static final WardenEsp wardenEsp = WardenEsp.INSTANCE;

    public static final AutoSell autoSell = AutoSell.INSTANCE;

    public static final AppleFarmer appleFarmer = AppleFarmer.INSTANCE;

    public static final ItemTime itemTime = ItemTime.INSTANCE;

    public static final PvPSave pvpSave = new PvPSave();

    public static final LockSlot lockSlot = LockSlot.INSTANCE;

    public static final SaturationEat saturationEat = SaturationEat.INSTANCE;

    public static final CordDropper cordDropper = CordDropper.INSTANCE;

    public void initialize() {
        this.getObject().addAll(java.util.Arrays.asList(
            antibot,
            airStuck,
            arrows,
            aspectRatio,
            aimAssist,
            hitBox,
            aura,
            maceAura,
            autoTool,
            maceSwap,
            tapeMouse,
            autoAccept,
            auctionHelper,
            autoFish,
            autoCodes,
            triggerBot,
            spaceJoiner,
            autoEat,
            autoExplosion,
            crystalAura,
            webTrap,
            nameProtect,
            chestStealer,
            clientSounds,
            colorSaturation,
            damagePop,
            attackEffect,
            panic,
            autoTotem,
            blockOverlay,
            clickPearl,
            blockesp,
            elytraBoost,
            elytraMotion,
            elytraSwap,
            elytraTarget,
            nameTags,
            fly,
            freeCam,
            fullBright,
            highJump,
            interfaceModule,
            autoLeave,
            guiMove,
            itemScroller,
            jumpCircle,
            noDelay,
            noPush,
            velocity,
            noWeb,
            packetCriticals,
            particle,
            fireworkESP,
            fastBreak,
            noRender,
            rPSpoofer,
            seeInvisibles,
            shaderEsp,
            shaderHands,
            esp,
            shulkerPreview,
            souls,
            speed,
            spinBot,
            sprint,
            swingAnimations,
            targetESP,
            targetPearl,
            scoreboardHP,
            totemAngel,
            tpsSync,
            viewModel,
            customSize,
            worldTweaks,
            autoSwap,
            noSlow,
            serverHelper,
            trajectories,
            predictions,
            fakePlayer,
            commandFix,
            windHop,
            noInteract,
            wardenEsp,
            autoSell,
            appleFarmer,
            itemTime,
            pvpSave,
            lockSlot,
            saturationEat,
            cordDropper
        ));
    }
}
