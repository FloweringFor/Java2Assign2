import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;
import java.util.Scanner;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class Main extends Application {
    // login/register界面的组件
    Label welcome = new Label("欢 迎");
    Label username = new Label("用户名");
    Label password = new Label("密码");
    TextField user = new TextField();
    PasswordField pass = new PasswordField();
    Button login = new Button("登录");
    Button register = new Button("注册");
    GridPane gridPane = new GridPane();
    Scene scene = new Scene(gridPane, 600, 400);

    // 个人信息和对手选择
    SplitPane splitPane = new SplitPane();
    GridPane left = new GridPane();
    Scene scene1 = new Scene(splitPane, 600, 400);
    ListView<String> listView = new ListView<>();
    Label playername = new Label();
    Label winLabel = new Label();
    Label loseLabel = new Label();
    Label tieLabel = new Label();
    Button button = new Button("确定选择对手");
    VBox right = new VBox(listView, button);

    // 棋盘
    private final int numberOfColumn = 3; //定义常量列数
    private final int numberOfRow = 3;    //定义常量行数
    MyRectangle[][] rectangles = new MyRectangle[numberOfRow][numberOfColumn];
    BorderPane root = new BorderPane();
    BorderPane bottom = new BorderPane();
    Label information = new Label("轮到你了！");
    Pane chessboard = new Pane(); 		//创建一个面板实例方法
    int size = 90;
    Scene scene2 = new Scene(root); 			//创建一个场景实例方法

    Client client = new Client();
    boolean turn = true;



    @Override
    public void start(Stage stage) throws Exception {
        client.stage = stage;
        // 登录界面组件————————————————————————————————————
        welcome.setFont(Font.font(30));
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setPadding(new Insets(11, 12, 13, 14));
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.add(welcome, 0, 0);
        gridPane.add(username, 0, 1);
        gridPane.add(password, 0, 2);
        gridPane.add(login, 0, 3);
        gridPane.add(user, 1, 1);
        gridPane.add(pass, 1, 2);
        gridPane.add(register, 1, 3);

        // 登录
        login.setOnAction(event -> {
            String name = user.getText().trim();
            String pswd = pass.getText().trim();
            if (name.equals("") || pswd.equals("")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setHeaderText("错误提示");
                alert.setContentText("用户名和密码不能为空！");
                alert.show();
            } else if (name.contains(" ") || pswd.contains(" ")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setHeaderText("错误提示");
                alert.setContentText("用户名和密码不能含有空格！");
                alert.show();
            } else {
                client.send("login", name, pswd);
                String result = client.receive();
                // System.out.println(result);
                if (result.startsWith("登录成功")) {
                    String[] info = result.split(" ");
                    login.setDisable(true);
                    register.setDisable(true);
                    client.setInfo(name, Integer.parseInt(info[1]), Integer.parseInt(info[2]), Integer.parseInt(info[3]));
                    client.connect_game();
                    playername.setText("用户名：" + client.username);
                    winLabel.setText("胜局：" + client.win);
                    loseLabel.setText("败局：" + client.lose);
                    tieLabel.setText("平局：" + client.tie);
                    stage.setTitle(client.username);
                    stage.setScene(scene1);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("错误");
                    alert.setHeaderText("错误提示");
                    alert.setContentText(result);
                    alert.show();
                }
            }
        });
        // 注册
        register.setOnAction(event -> {
            String name = user.getText().trim();
            String pswd = pass.getText().trim();
            if (name.equals("") || pswd.equals("")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setHeaderText("错误提示");
                alert.setContentText("用户名和密码不能为空！");
                alert.show();
            } else if (name.contains(" ") || pswd.contains(" ")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setHeaderText("错误提示");
                alert.setContentText("用户名或密码错误！");
                alert.show();
            } else {
                client.send("register", name, pswd);
                String result = client.receive();
                // System.out.println(result);
                if (result.equals("注册成功")) {
                    register.setDisable(true);
                    login.setDisable(true);
                    client.setInfo(name, 0, 0, 0);
                    client.connect_game();
                    playername.setText("用户名：" + client.username);
                    winLabel.setText("胜局：" + client.win);
                    loseLabel.setText("败局：" + client.lose);
                    tieLabel.setText("平局：" + client.tie);
                    stage.setTitle(client.username);
                    stage.setScene(scene1);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("错误");
                    alert.setHeaderText("错误提示");
                    alert.setContentText(result);
                    alert.show();
                }
            }
        });



        // 玩家列表页面组件————————————————————————————————————
        playername.setFont(Font.font("Amble CN", FontWeight.BOLD, 24));
        winLabel.setFont(Font.font("Amble CN", FontWeight.BOLD, 15));
        loseLabel.setFont(Font.font("Amble CN", FontWeight.BOLD, 15));
        tieLabel.setFont(Font.font("Amble CN", FontWeight.BOLD, 15));
        left.setPadding(new Insets(40, 10, 20, 30));
        left.setVgap(1);
        left.setHgap(4);
        left.add(playername, 0, 0);
        left.add(winLabel, 0, 1);
        left.add(loseLabel, 0, 2);
        left.add(tieLabel, 0, 3);

        button.setOnAction(event -> {
            // 获取username
            String opponent = listView.getSelectionModel().getSelectedItem();
            if (opponent != null) {
                client.send_choice_request(opponent);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(client.username);
                alert.setHeaderText("信息提示");
                alert.setContentText("请求已发送！");
                alert.show();
                button.setDisable(true);
            }
        });
        splitPane.getItems().addAll(left, right);


        // 游戏页面组件————————————————————————————————————
        bottom.setCenter(information);
        root.setBottom(bottom);
        for (int i = 0; i < numberOfRow ; i++) {
            for (int j = 0; j < numberOfColumn; j++) {
                MyRectangle rectangle = new MyRectangle(i * size, j * size, size, size, i, j);
                rectangle.setFill(Color.WHITE);
                rectangle.setStroke(Color.BLACK);
                chessboard.getChildren().add(rectangle);
                rectangles[i][j] = rectangle;
                rectangle.setOnMouseClicked(event -> {
                    if (turn) {
                        client.send_chess_choice(rectangle.x, rectangle.y);
                    }
                    System.out.println("点击的坐标是(" + rectangle.x + "," + rectangle.y + ")");
                });
            }
        }
        root.setCenter(chessboard);

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                System.exit(0);
            }
        });


        stage.setScene(scene);
        stage.setTitle(client.username);
        stage.setResizable(false);
        stage.show();

    }

    public void drawLine(int x, int y) {
        rectangles[x][y].setFill(new ImagePattern(new Image(getClass().getResource("cross.png").toExternalForm(), size, size, false, false)));
    }

    public void drawCircle(int x, int y) {
        rectangles[x][y].setFill(new ImagePattern(new Image(getClass().getResource("circle.png").toExternalForm(), size, size, false, false)));

    }

    public void drawWhite(int x, int y) {
        rectangles[x][y].setFill(Color.WHITE);
    }


    class Client {
        String username;
        int win;
        int lose;
        int tie;
        Socket login_socket;
        Scanner login_in;
        PrintWriter login_out;
        int LoginPort = 8790;
        Stage stage;

        Socket game_socket;
        Scanner game_in;
        PrintWriter game_out;
        int GamePort = 8792;

        public Client() {
            try {
                this.login_socket = new Socket("127.0.0.1", LoginPort);
                this.login_in = new Scanner(login_socket.getInputStream());
                this.login_out = new PrintWriter(login_socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setInfo(String username, int win, int lose, int tie) {
            this.username = username;
            this.win = win;
            this.lose = lose;
            this.tie = tie;
        }

        // 发送登录/注册信息给LoginServer
        public void send(String command, String username, String password) {
            this.login_out.println(command + " " + username + " " + password);
            this.login_out.flush();
        }

        // 接收LoginServer的信息
        public String receive() {
            if (this.login_in.hasNext()) {
                return this.login_in.nextLine();
            } else {
                return "服务器意外退出，请尝试重新连接";
            }

        }

        // 和GameServer建立连接
        public void connect_game() {
            try {
                this.game_socket = new Socket("127.0.0.1", GamePort);
                this.game_in = new Scanner(game_socket.getInputStream());
                this.game_out = new PrintWriter(game_socket.getOutputStream());
                send_enter();
                new ObtainMessage(game_socket, game_in, game_out, username, stage, win, lose, tie).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public void send_enter(){
            this.game_out.println(username);
            this.game_out.flush();
        }

        public void send_choice_request(String opponent) {
            this.game_out.println("CHOOSE_OPPONENT " + this.username + " " + opponent);
            this.game_out.flush();
        }

        public void send_chess_choice(int x, int y) {
            this.game_out.println("WANT_PUT " + this.username + " " + x + " " + y);
            this.game_out.flush();
        }
    }


    class ObtainMessage extends Thread {
        Socket socket;
        Scanner in;
        PrintWriter out;
        String username;
        Stage stage;
        int win;
        int lose;
        int tie;

        public ObtainMessage(Socket socket, Scanner in, PrintWriter out, String username, Stage stage, int win, int lose, int tie) {
            this.socket = socket;
            this.in = in;
            this.out = out;
            this.username = username;
            this.stage = stage;
            this.win = win;
            this.lose = lose;
            this.tie = tie;
        }

        @Override
        public void run() {
            while (true) {
                if (in.hasNext()) {
                    String back = in.nextLine();
                    String[] message = back.split(" ");
                    System.out.println(back);
                    if (message[0].equals("NEW_PLAYER")) {  // NEW_PLAYER username
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                //更新JavaFX的主线程的代码放在此处
                                if (!message[1].equals(username)) {
                                    listView.getItems().add(message[1]);
                                }
                            }
                        });
                    } else if (message[0].equals("ACCEPT_REJECT")) {  // ACCEPT_REJECT username
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                //更新JavaFX的主线程的代码放在此处
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setTitle(username);
                                alert.setHeaderText("信息提示");
                                alert.setContentText(message[1] + "想和你对战一局，你愿意接受吗？");
                                Optional<ButtonType> result = alert.showAndWait();
                                if (result.isPresent()) {
                                    if (result.get() == ButtonType.OK) { // 同意接受挑战
                                        out.println("ACCEPT_CHALLENGE " + message[1] + " " + username);
                                    } else {  // 拒绝挑战
                                        out.println("REJECT_CHALLENGE " + message[1] + " " + username);
                                    }
                                    out.flush();
                                }
                            }
                        });
                    } else if (message[0].equals("REJECT")) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle(username);
                                alert.setHeaderText("信息提示");
                                alert.setContentText(message[1] + "已经拒绝你的请求，请重新选择！");
                                alert.show();
                                button.setDisable(false);
                            }
                        });
                    } else if (message[0].equals("ENTER_GAME")) {
                        button.setDisable(false);
                        if (message[1].equals(username)) {
                            turn = true;
                            information.setText("轮到你了");
                        } else {
                            turn = false;
                            information.setText("轮到对方了");
                        }
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < 3; i++) {
                                    for (int j = 0; j < 3; j++) {
                                        drawWhite(i, j);
                                    }
                                }
                                stage.setScene(scene2);
                            }
                        });
                    } else if (message[0].equals("PUT")) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                // System.out.println(message[1]);
                                if (message[1].equals(username)) {
                                    drawCircle(Integer.parseInt(message[2]), Integer.parseInt(message[3]));
                                    information.setText("轮到对方了");
                                } else {
                                    drawLine(Integer.parseInt(message[2]), Integer.parseInt(message[3]));
                                    information.setText("轮到你了");
                                }
                            }
                        });
                        turn = !turn;
                    } else if (message[0].equals("GAME_OVER")) {
                        if (message[1].equals(username)) {  // 自己赢了
                            win++;
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle(username);
                                    alert.setHeaderText("信息提示");
                                    alert.setContentText("恭喜你，获得胜利！");
                                    alert.showAndWait();
                                    winLabel.setText("胜局：" + win);
                                    stage.setScene(scene1);
                                }
                            });
                        } else {  // 自己输了
                            lose++;
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle(username);
                                    alert.setHeaderText("信息提示");
                                    alert.setContentText("很遗憾未能获胜，再来一局？");
                                    alert.showAndWait();
                                    loseLabel.setText("败局：" + lose);
                                    stage.setScene(scene1);
                                }
                            });
                        }
                    } else if (message[0].equals("TIE")) {  // 平局
                        tie++;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle(username);
                                alert.setHeaderText("信息提示");
                                alert.setContentText("平局");
                                alert.showAndWait();
                                tieLabel.setText("平局：" + tie);
                                stage.setScene(scene1);
                            }
                        });
                    } else if (message[0].equals("LEAVE")) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle(username);
                                alert.setHeaderText("信息提示");
                                alert.setContentText("抱歉！对手掉线了，请等待对手重新连接");
                                alert.show();
                            }
                        });
                    } else if (message[0].equals("CONTINUE_GAME")) {
                        if (message[1].equals(username)) {
                            information.setText("轮到你了");
                            turn = true;
                        } else {
                            information.setText("轮到对方了");
                            turn = false;
                        }
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle(username);
                                alert.setHeaderText("信息提示");
                                alert.setContentText("棋局未完成，请继续");
                                alert.show();
                                stage.setScene(scene2);
                                int index = 4;

                                if (message[2].equals(username)) {
                                    for (int i = 0; i < 3; i++) {
                                        for (int j = 0; j < 3; j++) {
                                            if (message[index].equals("1")) {
                                                drawCircle(i, j);
                                            } else if (message[index].equals("-1")) {
                                                drawLine(i, j);
                                            } else {
                                                drawWhite(i, j);
                                            }
                                            index++;
                                        }
                                    }
                                } else {
                                    for (int i = 0; i < 3; i++) {
                                        for (int j = 0; j < 3; j++) {
                                            if (message[index].equals("-1")) {
                                                drawCircle(i, j);
                                            } else if (message[index].equals("1")) {
                                                drawLine(i, j);
                                            } else {
                                                drawWhite(i, j);
                                            }
                                            index++;
                                        }
                                    }
                                }
                            }
                        });
                    } else if (message[0].equals("BUSY")) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle(username);
                                alert.setHeaderText("信息提示");
                                alert.setContentText("对方有未完成的战局，请重新选择对手");
                                alert.show();
                                button.setDisable(false);
                            }
                        });
                    } else if (message[0].equals("UPDATE")) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                listView.getItems().clear();
                                for (int i = 1; i < message.length; i++) {
                                    if (!message[i].equals(username)) {
                                        listView.getItems().add(message[i]);
                                    }
                                }
                            }
                        });
                    }
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle(username);
                            alert.setHeaderText("错误提示");
                            alert.setContentText("游戏服务器意外退出，客户端即将关闭");
                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isPresent()) {
                                if (result.get() == ButtonType.OK) {
                                    System.exit(0);
                                }
                            }
                        }
                    });
                    break;
                }
            }
        }
    }


}


class MyRectangle extends Rectangle {
    int x;
    int y;

    public MyRectangle(double v, double v1, double v2, double v3, int x, int y) {
        super(v, v1, v2, v3);
        this.x = x;
        this.y = y;
    }

}
