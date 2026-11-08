package brova.toshiba.client.utils.neuro;

public final class CombatContextKey {

    private final String targetType;

    private final String distanceBucket;

    private final String movementState;

    private final String critState;

    private final String healthState;

    @Override
    public String toString() {
        return this.targetType + "_" + this.distanceBucket + "_" + this.movementState + "_" + this.critState + "_" + this.healthState;
    }

    public CombatContextKey(String targetType, String distanceBucket, String movementState, String critState, String healthState) {
        this.targetType = targetType;
        this.distanceBucket = distanceBucket;
        this.movementState = movementState;
        this.critState = critState;
        this.healthState = healthState;
    }

    public String getTargetType() {
        return this.targetType;
    }

    public String getDistanceBucket() {
        return this.distanceBucket;
    }

    public String getMovementState() {
        return this.movementState;
    }

    public String getCritState() {
        return this.critState;
    }

    public String getHealthState() {
        return this.healthState;
    }

    @Override
    public boolean equals(Object value) {
        if (value == this) {
            return true;
        } else if (!(value instanceof CombatContextKey combatContextKey)) {
            return false;
        } else {
            String targetType = this.getTargetType();
            String targetTypeValue = combatContextKey.getTargetType();
            if (targetType == null ? targetTypeValue == null : targetType.equals(targetTypeValue)) {
                String distanceBucket = this.getDistanceBucket();
                String distanceBucketValue = combatContextKey.getDistanceBucket();
                if (distanceBucket == null ? distanceBucketValue == null : distanceBucket.equals(distanceBucketValue)) {
                    String movementState = this.getMovementState();
                    String text = brova.toshiba.client.utils.runtime.AccessGuard.invoke(combatContextKey, new Object[] {}, (accessReceiver, accessArguments) -> ((CombatContextKey) accessReceiver).getMovementState());
                    if (movementState == null ? text == null : movementState.equals(text)) {
                        String critState = this.getCritState();
                        String critStateValue = combatContextKey.getCritState();
                        if (critState == null ? critStateValue == null : critState.equals(critStateValue)) {
                            String textValue = brova.toshiba.client.utils.runtime.AccessGuard.invoke(this, new Object[] {}, (accessReceiver, accessArguments) -> ((CombatContextKey) accessReceiver).getHealthState());
                            String healthState = combatContextKey.getHealthState();
                            return textValue == null ? healthState == null : textValue.equals(healthState);
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    @Override
    public int hashCode() {
        byte byteValue = 59;
        int intValue = 1;
        String targetType = this.getTargetType();
        intValue = intValue * 59 + (targetType == null ? 43 : targetType.hashCode());
        String distanceBucket = this.getDistanceBucket();
        intValue = intValue * 59 + (distanceBucket == null ? 43 : distanceBucket.hashCode());
        String movementState = this.getMovementState();
        intValue = intValue * 59 + (movementState == null ? 43 : movementState.hashCode());
        String critState = this.getCritState();
        intValue = intValue * 59 + (critState == null ? 43 : critState.hashCode());
        String healthState = this.getHealthState();
        return intValue * 59 + (healthState == null ? 43 : healthState.hashCode());
    }
}
