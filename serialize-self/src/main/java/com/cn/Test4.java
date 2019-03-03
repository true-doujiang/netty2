package com.cn;

import com.cn.entity.Player;

import java.util.Arrays;

public class Test4 {

    /**
     * 自定义序列化
     */
    public static void main(String[] args) {

        Player player = new Player();
        player.setPlayerId(10001);
        player.setAge(22);
        player.setName("<<netty权威指南>>");
        player.getSkills().add(101);
        player.getSkills().add(102);
        player.getResource().setGold(99999);
        player.getLanauge().put("a", "Java");
        player.getLanauge().put("b", "Python");

        // 序列化
        byte[] bytes = player.getBytes();

        System.out.println(Arrays.toString(bytes));

        //==============================================

        Player player2 = new Player();
        // 反序列化
        player2.readFromBytes(bytes);

        System.out.println(
                              player2.getPlayerId()
                    + "   " + player2.getAge()
                    + "   " + player2.getName()
                    + "   " + Arrays.toString(player2.getSkills().toArray())
                    + "   " + player2.getResource().getGold()
                    + "   " + player2.getLanauge()
        );

    }

}
