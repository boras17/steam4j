package steamenums;

import lombok.Builder;

public enum Game{
    COUNTER_STRIKE(730, 2),
    DOTA2(570, 2),
    RUST(252490, 2),
    PUBG(578080, 2),
    TF2(440, 2);

    private final int game_id;
    private final int contextId;

    Game(int game_id, int contextId) {
        this.game_id = game_id;
        this.contextId = contextId;
    }
    public int getGameId() {
        return this.game_id;
    }
    public int getContextId() {
        return this.contextId;
    }

    public static Game fromInt(int gameNum){
        return switch (gameNum){
            case 730 -> COUNTER_STRIKE;
            case 570 -> DOTA2;
            case 252490 -> RUST;
            case 578080 -> PUBG;
            case 440 -> TF2;
            default -> throw new IllegalStateException("Unexpected value: " + gameNum);
        };
    }
}
