package steamenums;

enum Game{
    COUNTER_STRIKE(730);

    private final int game_id;

    Game(int game_id) {
        this.game_id = game_id;
    }
    public int getGameId() {
        return this.game_id;
    }
}
