package com.server_client;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * 客户端读取服务器信息的线程
 */
class ClientRead implements  Runnable{
    private Socket client;

    public ClientRead(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            //拿到输入流，读取服务端信息
            Scanner scanner=new Scanner(client.getInputStream());
            //设置分隔符
            scanner.useDelimiter("\n");
            while(true){
                if(scanner.hasNext()){
                    System.out.println(scanner.next());
                }
                if(client.isClosed()){
                    break;
                }
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/**
 * 向服务器发送信息
 */
class ClientWrite implements Runnable{
   private Socket client;

    public ClientWrite(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            //拿到输出流对象，向服务器发送消息
            PrintStream ps=new PrintStream(client.getOutputStream(),true);
            Scanner scanner=new Scanner(System.in);
            while(true){
                System.out.println("请输入要发送的信息...");
                String strToSrver=null;
                if(scanner.hasNext()){
                    strToSrver=scanner.next().trim();
                    System.out.println("要发送的数据为:"+strToSrver);
                    ps.println(strToSrver);
                    //终止标志，下线
                    if(strToSrver.equals("byebye")){
                        System.out.println("关闭客户端");
                        scanner.close();
                        ps.close();
                        client.close();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
public class client {
    public static void main(String[] args) {
        try {
            //连接服务器，获取Socket对象
            Socket client = new Socket("127.0.0.1", 6666);
           Thread readInfoThread =new Thread(new ClientRead(client));
           Thread writeInfoThread=new Thread(new ClientWrite(client));
           readInfoThread.start();
           writeInfoThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
