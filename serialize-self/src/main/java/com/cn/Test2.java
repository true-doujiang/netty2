package com.cn;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Test2 {

    /**
     * 利用java.nio的 ByteBuffer 有putInt() , getInt() ... 但是空间固定
     */
	public static void main(String[] args) {
		int id = 101;
		int age = 21;
		
		ByteBuffer buffer = ByteBuffer.allocate(8);//8个字节   ，不会自动扩容 写多了会报错
		buffer.putInt(id);
		buffer.putInt(age);
		
		byte[] array = buffer.array();
		System.out.println(Arrays.toString(buffer.array()));
		
		//====================================================
		
		ByteBuffer buffer2 = ByteBuffer.wrap(array);
		System.out.println("id:"+buffer2.getInt());
		System.out.println("age:"+buffer2.getInt());
		
		
		//====================================================
		//输出的补码
		System.out.println(Integer.toBinaryString(id));    
		//System.out.println(Integer.MAX_VALUE);
		//System.out.println(Integer.toBinaryString(Integer.MAX_VALUE + 1));
	}

}
