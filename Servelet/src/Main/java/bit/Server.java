package bit;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {
    //建立一个集合类，用来存储客户端对象
    private static Map<String, Socket> clientMap = new HashMap<String, Socket>();

    static class ExecuteClientThread implements Runnable {
        private Socket client;

        public ExecuteClientThread(Socket client) {
            this.client = client;
        }

        /**
         * 处理客户端请求的核心方法
         */
        @Override
        public void run() {
            try {
                //获取输入流
                Scanner scanner = new Scanner(client.getInputStream());
                String str = null;
                while (true) {
                    if (scanner.hasNext()) {
                        str = scanner.next();
                        Pattern pattern=Pattern.compile("\r");
                        Matcher matcher=pattern.matcher(str);
                        //注册流程userName:zhangsan
                        if (str.startsWith("userName")) {
                            String userName = str.split("\\:")[1];
                            userRegist(userName, client);
                            continue;
                        }
                        //群聊流程G:userName-hello
                        if (str.startsWith("G")) {
                            String[] temp = str.split("\\-");
                            String userName = temp[0].split("\\:")[1];
                            String msg = temp[1];
                            groupChat(userName, msg);
                            continue;
                        }
                        //私聊流程P:user-userName-hello
                        if (str.startsWith("P")) {
                            String[] temp = str.split("\\:")[1].split("-");
                            String user = temp[0];
                            String userName = temp[1];
                            String msg = temp[2];
                            privateChat(user, userName, msg);
                            continue;
                        }
                        //用户退出
                        if (str.contains("byebye")) {
                            //先根据Socket找到userName
                            String userName = null;
                            for (String getKey : clientMap.keySet()) {
                                if (clientMap.get(getKey).equals(client)) {
                                    userName = getKey;
                                }
                            }
                            System.out.println("用户:" + userName + "下线了...，当前人数:"+clientMap.size());
                            //将此实例从clientMap中移出
                            clientMap.remove(userName);
                            continue;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param client：用户注册方法--具体方法实现
     */
    public static void userRegist(String userName, Socket client) {
        System.out.println("用户名为:"+userName+"上线了...");
        System.out.println("用户Socket为:" + client);
        System.out.println("当前群聊人数为:" + (clientMap.size() + 1) + "人");
        //将用户信息保存到ClientMap中
        clientMap.put(userName, client);
    }

    /**
     * 群聊方法
     */
    public static void groupChat(String userName, String msg) {
        Set<Map.Entry<String, Socket>> entry = clientMap.entrySet();
        Iterator<Map.Entry<String, Socket>> iterator = entry.iterator();
        for (Map.Entry<String, Socket> stringSocketEntry : entry) {
            Socket socket = stringSocketEntry.getValue();
            try {
                PrintStream ps = new PrintStream(socket.getOutputStream(), true);
                ps.println("用户" + userName + "说:" + msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 私聊方法
     */
    public static void privateChat(String user, String userName, String msg) {
        //找到对应userName的Socket
        Socket client = clientMap.get(userName);
        try {
            PrintStream ps = new PrintStream(client.getOutputStream(), true);
            ps.println(user + "-" + msg);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //开启20个线程
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        try {
            ServerSocket serverSocket = new ServerSocket(6666);
            for (int i = 0; i < 20; i++) {
                System.out.println("等待客户端连接...");
                //返回客户端Socket的实例
                Socket client = serverSocket.accept();
                System.out.println("有新的用户连接:" + client.getInetAddress() + "端口号为:" + client.getPort());
                executorService.execute(new ExecuteClientThread(client));
            }
            executorService.shutdown();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
