package brova.toshiba.client.utils.player;

import brova.toshiba.client.core.MinecraftAccess;
import brova.toshiba.client.modules.ModuleRegistry;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;

public class HealthUtil implements MinecraftAccess {

    public static float getHealth(LivingEntity entity) {
        if (entity == null) {
            return 0.0F;
        } else {
            return ModuleRegistry.scoreboardHP != null && ModuleRegistry.scoreboardHP.isEnable() ? calculateHealthWithObjectiveForSlot(entity) : entity.getHealth();
        }
    }

    private static float calculateHealthWithObjectiveForSlot(LivingEntity entity) {
        if (entity instanceof ClientPlayerEntity) {
            return entity.getHealth();
        } else if (entity instanceof PlayerEntity player && mc.getCurrentServerEntry() != null) {
            try {
                Scoreboard scoreboard = player.getScoreboard();
                ScoreboardObjective objectiveForSlot = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);
                if (objectiveForSlot == null) {
                    objectiveForSlot = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.LIST);
                }
                if (objectiveForSlot == null) {
                    return entity.getHealth();
                } else {
                    ReadableScoreboardScore score = scoreboard.getScore(player, objectiveForSlot);
                    return score == null ? entity.getHealth() : score.getScore();
                }
            } catch (Exception failure) {
                return entity.getHealth();
            }
        } else {
            return entity.getHealth();
        }
    }

    public static float getHealthWithAbsorption(LivingEntity entity) {
        return Math.max(0.0F, getHealth(entity) + entity.getAbsorptionAmount());
    }
}
