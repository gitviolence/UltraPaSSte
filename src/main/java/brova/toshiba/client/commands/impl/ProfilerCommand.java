package brova.toshiba.client.commands.impl;

import brova.toshiba.client.commands.CommandBase;
import brova.toshiba.client.utils.profile.PerformanceProfiler;
import brova.toshiba.client.utils.profile.ProfileReport;
import brova.toshiba.client.utils.text.ChatUtil;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;

public final class ProfilerCommand extends CommandBase {

    public ProfilerCommand() {
        super("profiler");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
        PerformanceProfiler INSTANCE = PerformanceProfiler.INSTANCE;
        literalArgumentBuilder.executes(commandSource -> {
            if (INSTANCE.isRunning()) {
                this.updateMessageWithStop(INSTANCE);
            } else {
                this.updateMessageWithStart(INSTANCE, 10);
            }
            return 1;
        });
        literalArgumentBuilder.then(this.literal("start").executes(input -> {
            this.updateMessageWithStart(INSTANCE, 10);
            return 1;
        }).then(this.arg("seconds", IntegerArgumentType.integer(1, 300)).executes(inputValue -> {
            int integer = IntegerArgumentType.getInteger(inputValue, "seconds");
            this.updateMessageWithStart(INSTANCE, integer);
            return 1;
        })));
        literalArgumentBuilder.then(this.literal("stop").executes(inputResult -> {
            this.updateMessageWithStop(INSTANCE);
            return 1;
        }));
        literalArgumentBuilder.then(this.literal("status").executes(inputItem -> {
            ChatUtil.sendMessage(INSTANCE.status());
            return 1;
        }));
        literalArgumentBuilder.then(this.literal("result").executes(inputEntry -> {
            ProfileReport profileReport = INSTANCE.lastReport();
            if (profileReport == null) {
                ChatUtil.sendMessage("Профайлер: готового результата ещё нет.");
            } else {
                profileReport.lines().forEach(ChatUtil::sendMessage);
            }
            return 1;
        }));
        literalArgumentBuilder.then(this.literal("clear").executes(inputCopy -> {
            if (INSTANCE.clearResult()) {
                ChatUtil.sendMessage("Профайлер: прошлый результат очищен.");
            } else {
                ChatUtil.sendMessage("Профайлер: сначала останови активный замер.");
            }
            return 1;
        }));
    }

    private void updateMessageWithStart(PerformanceProfiler performanceProfiler, int intValue) {
        if (performanceProfiler.start(intValue)) {
            ChatUtil.sendMessage("Профайлер запущен на " + intValue + " сек. Воспроизведи проблемный момент.");
        } else {
            ChatUtil.sendMessage("Профайлер уже работает. " + performanceProfiler.status());
        }
    }

    private void updateMessageWithStop(PerformanceProfiler performanceProfiler) {
        if (performanceProfiler.stop()) {
            ChatUtil.sendMessage("Профайлер остановлен, результатом можно выразиться через .profiler result");
            ProfileReport profileReport = performanceProfiler.lastReport();
            if (profileReport != null) {
                profileReport.lines().forEach(ChatUtil::sendMessage);
            }
        } else {
            ChatUtil.sendMessage("Профайлер сейчас не запущен.");
        }
    }
}
