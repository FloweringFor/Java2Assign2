public class User {
    String username;
    String password;
    int win;
    int lose;
    int tie;

    public User(String username, String password, int win, int lose, int tie) {
        this.username = username;
        this.password = password;
        this.win = win;
        this.lose = lose;
        this.tie = tie;
    }
}
