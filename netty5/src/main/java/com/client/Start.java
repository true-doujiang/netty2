package com.client;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 启动类
 * @author -琴兽-
 */
public class Start {

    public static void main(String[] args) throws IOException {
        MultClient client = new MultClient();
        client.init(5);


        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("please inout !");
            String s = reader.readLine();

            client.nextChannel().writeAndFlush(s);
        }

    }
}
