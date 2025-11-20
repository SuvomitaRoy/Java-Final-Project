public enum PenaltyStatus {
    NONE(0),
    WARNING(1),
    SUSPENDED(2),
    BANNED(3);

    private final int level;

    PenaltyStatus(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}