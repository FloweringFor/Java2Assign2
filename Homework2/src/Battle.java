import java.util.Arrays;

public class Battle {
    boolean active = true;
    String player1;  // 1
    String player2;  // -1
    int[][] chess = new int[3][3];
    int step = 0;

    public Battle(String player1, String player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++){
                result.append(chess[i][j]);
                result.append(" ");
            }
        }
        return result.toString();
    }

    /*public static void main(String[] args) {
        System.out.println(new Battle("", "").toString());
    }*/

    public void draw(String name, int x, int y){
        step++;
        if (name.equals(player1)){
            chess[x][y] = 1;
        } else if (name.equals(player2)){
            chess[x][y] = -1;
        }
    }

    public boolean isFull(){
        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++){
                if (chess[i][j] == 0){
                    return false;
                }
            }
        }
        return true;
    }

    public String win(){
        if (chess[0][0] + chess[0][1] + chess[0][2] == 3){
            return player1;
        }

        if (chess[1][0] + chess[1][1] + chess[1][2] == 3){
            return player1;
        }

        if (chess[2][0] + chess[2][1] + chess[2][2] == 3){
            return player1;
        }


        if (chess[0][0] + chess[1][0] + chess[2][0] == 3){
            return player1;
        }

        if (chess[0][1] + chess[1][1] + chess[2][1] == 3){
            return player1;
        }

        if (chess[0][2] + chess[1][2] + chess[2][2] == 3){
            return player1;
        }

        if(chess[0][0] + chess[1][1] + chess[2][2] == 3){
            return player1;
        }

        if(chess[0][2] + chess[1][1] + chess[2][0] == 3){
            return player1;
        }


        if (chess[0][0] + chess[0][1] + chess[0][2] == -3){
            return player2;
        }

        if (chess[1][0] + chess[1][1] + chess[1][2] == -3){
            return player2;
        }

        if (chess[2][0] + chess[2][1] + chess[2][2] == -3){
            return player2;
        }


        if (chess[0][0] + chess[1][0] + chess[2][0] == -3){
            return player2;
        }

        if (chess[0][1] + chess[1][1] + chess[2][1] == -3){
            return player2;
        }

        if (chess[0][2] + chess[1][2] + chess[2][2] == -3){
            return player2;
        }

        if(chess[0][0] + chess[1][1] + chess[2][2] == -3){
            return player2;
        }

        if(chess[0][2] + chess[1][1] + chess[2][0] == -3){
            return player2;
        }
        return "";
    }

}
