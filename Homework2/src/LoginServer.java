import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class LoginServer {
    ServerSocket socket;
    int port = 8790;
    Connection conn;

    public LoginServer() {
        try {
            this.conn = DBUtil.getCollection();
            this.socket = new ServerSocket(port);
            System.out.println("登录服务器启动成功");
        } catch (SQLException e) {
            System.out.println("数据库连接失败");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("登录服务器启动失败");
            e.printStackTrace();
        }
    }

    public void connect() {
        while (true) {
            try {
                Socket s = this.socket.accept();
                System.out.println("客户端成功连接登录服务器");
                new ReadMessage(s, conn).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        /*
        Runtime.getRuntime().addShutdownHook(new Thread(){

            @Override
            public void run() {
                // 在关闭钩子中执行收尾工作
                // 注意事项：
                // 1.在这里执行的动作不能耗时太久
                // 2.不能在这里再执行注册，移除关闭钩子的操作
                // 3 不能在这里调用System.exit()
                System.out.println("do shutdown hook");
            }
        });*/
        new LoginServer().connect();
    }
}


class ReadMessage extends Thread {
    Socket s;
    Scanner in;
    PrintWriter out;
    Connection conn;
    public ReadMessage(Socket s, Connection conn) {
        this.s = s;
        this.conn = conn;
        try {
            this.in = new Scanner(s.getInputStream());
            this.out = new PrintWriter(s.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            if (in.hasNext()) {
                String[] message = in.nextLine().split(" ");
                User user = DBUtil.select(conn,message[1]);
                if (message[0].equals("login")) {
                    if (user == null) {
                        out.println("用户名或者密码错误");
                        out.flush();
                    } else {
                        if (user.password.equals(message[2])) {
                            out.println("登录成功 " + user.win + " " + user.lose + " " + user.tie);
                            out.flush();
                            break;
                        } else {
                            out.println("用户名或者密码错误");
                            out.flush();
                        }
                    }

                } else if (message[0].equals("register")) {
                    if (user == null) {
                        DBUtil.add(conn,message[1], message[2]);
                        out.println("注册成功");
                        out.flush();
                        break;
                    } else {
                        out.println("用户名已存在");
                        out.flush();
                    }
                }
            }

        }

    }
}
