package game.player;

/**
 * @author Pavlo Putrenkov
 */
public enum DefaultValue {
    MESSAGE("MESSAGE");

    private String value;

    DefaultValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
