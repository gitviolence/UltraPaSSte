package brova.toshiba.client.data;

import net.minecraft.text.Text;

public class ScoreboardEntry {

    public final Text name;

    public final Text score;

    public final int scoreWidth;

    public ScoreboardEntry(Text name, Text score, int scoreWidth) {
        this.name = name;
        this.score = score;
        this.scoreWidth = scoreWidth;
    }

    public Text name() {
        return this.name;
    }

    public Text score() {
        return this.score;
    }

    public int scoreWidth() {
        return this.scoreWidth;
    }
}
