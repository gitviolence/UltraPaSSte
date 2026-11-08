package brova.toshiba.client.utils.neuro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RotationPatternRecorder {

    private final List<RecordedRotationStep> recordedRotationStepEntries = new ArrayList<>();

    private final List<List<RecordedRotationStep>> presetPatterns = new ArrayList<>();

    private boolean recording = false;

    private long lastUpdateMillis = 0L;

    private int patternSampleIndex = 0;

    private int activePresetIndex = 0;

    public RotationPatternRecorder() {
        this.updateLocalState();
    }

    private void updateLocalState() {
        ArrayList<RecordedRotationStep> values = new ArrayList<>();
        values.add(new RecordedRotationStep(0.12F, 0.04F, 10L));
        values.add(new RecordedRotationStep(0.35F, 0.11F, 12L));
        values.add(new RecordedRotationStep(0.68F, -0.05F, 15L));
        values.add(new RecordedRotationStep(0.24F, 0.08F, 11L));
        values.add(new RecordedRotationStep(-0.08F, 0.15F, 14L));
        values.add(new RecordedRotationStep(0.42F, -0.03F, 10L));
        this.presetPatterns.add(values);
        ArrayList<RecordedRotationStep> valuesValue = new ArrayList<>();
        valuesValue.add(new RecordedRotationStep(0.25F, -0.08F, 12L));
        valuesValue.add(new RecordedRotationStep(0.55F, 0.22F, 14L));
        valuesValue.add(new RecordedRotationStep(0.31F, 0.04F, 10L));
        valuesValue.add(new RecordedRotationStep(-0.15F, 0.18F, 16L));
        valuesValue.add(new RecordedRotationStep(0.48F, -0.1F, 11L));
        this.presetPatterns.add(valuesValue);
    }

    public void startRecording() {
        this.recordedRotationStepEntries.clear();
        this.recording = true;
        this.lastUpdateMillis = System.currentTimeMillis();
    }

    public void stopRecording() {
        this.recording = false;
        if (!this.recordedRotationStepEntries.isEmpty()) {
            this.presetPatterns.add(new ArrayList<>(this.recordedRotationStepEntries));
        }
    }

    public boolean isRecording() {
        return this.recording;
    }

    public void recordPoint(float floatValue, float floatValueValue) {
        if (this.recording) {
            long lastUpdateMillis = System.currentTimeMillis();
            long longValue = this.lastUpdateMillis == 0L ? 10L : Math.max(1L, lastUpdateMillis - this.lastUpdateMillis);
            this.lastUpdateMillis = lastUpdateMillis;
            this.recordedRotationStepEntries.add(new RecordedRotationStep(floatValue, floatValueValue, longValue));
        }
    }

    public RecordedRotationStep getNextRecordedPoint() {
        if (this.presetPatterns.isEmpty()) {
            return new RecordedRotationStep(0.0F, 0.0F, 10L);
        } else {
            var values = this.presetPatterns.get(this.activePresetIndex % this.presetPatterns.size());
            if (values.isEmpty()) {
                return new RecordedRotationStep(0.0F, 0.0F, 10L);
            } else {
                RecordedRotationStep recordedRotationStep = values.get(this.patternSampleIndex % values.size());
                this.patternSampleIndex++;
                if (this.patternSampleIndex >= values.size()) {
                    this.patternSampleIndex = 0;
                    this.activePresetIndex = ThreadLocalRandom.current().nextInt(this.presetPatterns.size());
                }
                return recordedRotationStep;
            }
        }
    }

    public List<List<RecordedRotationStep>> getPresetPatterns() {
        return Collections.unmodifiableList(this.presetPatterns);
    }
}
