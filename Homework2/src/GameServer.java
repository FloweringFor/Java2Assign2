import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class GameServer {
    ServerSocket socket;
    int port = 8792;
    Connection conn;
    // socket的合集
    Map<String, Socket> socketMap = new HashMap<>();
    // 战局
    ArrayList<Battle> battles = new ArrayList<>();
    // 是否上线
    Map<String, Boolean> able = new Hashtable<>();

    public GameServer() {
        try{
            this.conn = DBUtil.getCollection();
            this.socket = new ServerSocket(port);
            System.out.println("游戏服务器启动成功");
        } catch (SQLException e) {
            System.out.println("数据库连接失败");
            e.printStackTrace();
        } catch (IOException e){
            System.out.println("游戏服务器启动失败");
            e.printStackTrace();
        }
    }

    public void connect(){
        while (true){
            try{
                Socket s = socket.accept();
                System.out.println("客户端成功连接游戏服务器");
                Scanner in = new Scanner(s.getInputStream());
                PrintWriter out = new PrintWriter(s.getOutputStream());
                String username = in.nextLine();
                Set<String> keys = socketMap.keySet();
                /*
                if (socketMap.containsKey(username)){
                    for (String k : keys){
                        if(!k.equals(username)){
                            out.println("NEW_PLAYER " + k);
                            out.flush();
                        }
                    }
                } else {
                    for (String k : keys){
                        Socket i = socketMap.get(k);
                        PrintWriter out1 = new PrintWriter(i.getOutputStream());
                        out1.println("NEW_PLAYER " + username);
                        out1.flush();
                        out.println("NEW_PLAYER " + k);
                        out.flush();
                    }
                }*/
                for (String k : keys){
                    if (able.get(k)){
                        Socket i = socketMap.get(k);
                        PrintWriter out1 = new PrintWriter(i.getOutputStream());
                        out1.println("NEW_PLAYER " + username);
                        out1.flush();
                        out.println("NEW_PLAYER " + k);
                        out.flush();
                    }
                }
                socketMap.put(username, s);
                able.put(username, true);
                // System.out.println(socketMap.size());
                new ClientThread(username, s,in, out, socketMap, battles, conn, able).start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    public static void main(String[] args) {
        new GameServer().connect();
    }




}

class ClientThread extends Thread{
    String name;
    Socket socket;
    Scanner in;
    PrintWriter out;
    Map<String, Socket> socketMap;
    ArrayList<Battle> battles;
    Connection conn;
    Map<String, Boolean> able;
    public ClientThread(String name, Socket socket, Scanner in, PrintWriter out, Map<String, Socket> socketMap, ArrayList<Battle> battles, Connection conn, Map<String, Boolean> able) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.socketMap = socketMap;
        this.battles = battles;
        this.name = name;
        this.conn = conn;
        this.able = able;
    }


    @Override
    public void run(){
        while (true){
            if(in.hasNext()){
                String back = in.nextLine();
                String[] message = back.split(" ");
                if (message[0].equals("CHOOSE_OPPONENT")){   // CHOOSE_OPPONENT 发起挑战的人 响应挑战的人
                    Battle battle = null;
                    for (Battle b : battles){
                        if (b.active && (b.player1.equals(name) || b.player2.equals(name))){
                            battle = b;
                        }
                    }
                    if (battle != null && able.get(battle.player1) && able.get(battle.player2)){
                        String now = null;
                        if (battle.step % 2 == 0){
                            now = battle.player1;
                        } else {
                            now = battle.player2;
                        }
                        String oppo = null;
                        if (name.equals(battle.player1)){
                            oppo = battle.player2;
                        } else {
                            oppo = battle.player1;
                        }
                        out.println("CONTINUE_GAME " + now + " " + battle.player1 + " " + battle.player2 + " " + battle.toString());
                        out.flush();

                        try{
                            PrintWriter os = new PrintWriter(socketMap.get(oppo).getOutputStream());
                            os.println("CONTINUE_GAME " + now + " " + battle.player1 + " " + battle.player2 + " " + battle.toString());
                            os.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Battle bb = null;
                        for (Battle b : battles){
                            if (b.active && (b.player1.equals(message[2]) || b.player2.equals(message[2]))){
                                bb = b;
                            }
                        }
                        if (bb == null){
                            try{
                                PrintWriter os = new PrintWriter(socketMap.get(message[2]).getOutputStream());
                                os.println("ACCEPT_REJECT " + message[1]);
                                os.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            out.println("BUSY");
                            out.flush();
                        }
                    }
                } else if (message[0].equals("ACCEPT_CHALLENGE")){ // ACCEPT_CHALLENGE 发起挑战的人 响应挑战的人
                    try{
                        Battle b = new Battle(message[1], message[2]);
                        battles.add(b);
                        PrintWriter os1 = new PrintWriter(socketMap.get(message[1]).getOutputStream());
                        os1.println("ENTER_GAME " + message[1] + " " + message[2]);
                        os1.flush();
                        PrintWriter os2 = new PrintWriter(socketMap.get(message[2]).getOutputStream());
                        os2.println("ENTER_GAME " + message[1] + " " + message[2]);
                        os2.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (message[0].equals("REJECT_CHALLENGE")){ // REJECT_CHALLENGE 发起挑战的人 响应挑战的人
                    try{
                        PrintWriter os = new PrintWriter(socketMap.get(message[1]).getOutputStream());
                        os.println("REJECT " + message[2]);
                        os.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (message[0].equals("WANT_PUT")){
                    Battle b = null;
                    for(Battle battle : battles){
                        if (battle.active && (battle.player1.equals(message[1]) || battle.player2.equals(message[1]))){
                            b = battle;
                            break;
                        }
                    }
                    if (b != null){
                        int x = Integer.parseInt(message[2]);
                        int y = Integer.parseInt(message[3]);
                        if (b.chess[x][y] == 0){
                            b.draw(message[1], x, y);
                            try{
                                PrintWriter os1 = new PrintWriter(socketMap.get(b.player1).getOutputStream());
                                os1.println("PUT " + message[1] + " " + message[2] + " " + message[3]);
                                os1.flush();
                                PrintWriter os2 = new PrintWriter(socketMap.get(b.player2).getOutputStream());
                                os2.println("PUT " + message[1] + " " +  message[2] + " " + message[3]);
                                os2.flush();
                                boolean isFull = b.isFull();
                                String isWin = b.win();
                                if (!isWin.equals("")){  // 说明已经有人赢了
                                    os1.println("GAME_OVER " + isWin);
                                    os1.flush();
                                    os2.println("GAME_OVER " + isWin);
                                    os2.flush();
                                    b.active = false;
                                    if (isWin.equals(b.player1)){
                                        DBUtil.update(conn, b.player1, 1, 0, 0);
                                        DBUtil.update(conn, b.player2, 0, 1, 0);
                                    } else {
                                        DBUtil.update(conn, b.player1, 0, 1, 0);
                                        DBUtil.update(conn, b.player2, 1, 0, 0);
                                    }
                                } else if (isFull){  // 虽然没有人赢，但是棋盘已经满了，平局
                                    os1.println("TIE");
                                    os1.flush();
                                    os2.println("TIE");
                                    os2.flush();
                                    b.active = false;
                                    DBUtil.update(conn,b.player1, 0,0,1);
                                    DBUtil.update(conn, b.player2, 0, 0,1);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } else {
                able.put(name, false);
                String oppo = null;
                for (Battle b : battles){
                    if (b.active && (b.player1.equals(name) || b.player2.equals(name))){
                        if (b.player1.equals(name)){
                            oppo = b.player2;
                        } else {
                            oppo = b.player1;
                        }
                    }
                }
                if (oppo != null){
                    try{
                        PrintWriter os1 = new PrintWriter(socketMap.get(oppo).getOutputStream());
                        os1.println("LEAVE");
                        os1.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Set<String> keys = socketMap.keySet();
                StringBuilder mess = new StringBuilder();
                mess.append("UPDATE ");
                for(String k : keys){
                    if (able.get(k)){
                        mess.append(k).append(" ");
                    }
                }
                for(String k : keys){
                    if (able.get(k)){
                        try{
                            PrintWriter oss = new PrintWriter(socketMap.get(k).getOutputStream());
                            oss.println(mess);
                            oss.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                System.out.println(name + "退出啦！！！");
                break;
            }
        }
    }
}
