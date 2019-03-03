package com.cn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;


public class Test1 {


    /**
     * 利用ByteArrayOutputStream序列化， 可以自动扩容，但是灭幼readInt() .....
     */
	public static void main(String[] args) throws IOException {
		int id = 101;
		int age = 21;
		
		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
		arrayOutputStream.write(int2bytes(id));
		arrayOutputStream.write(int2bytes(age));
		
		byte[] byteArray = arrayOutputStream.toByteArray();
		
		System.out.println(Arrays.toString(byteArray));
		
		//==============================================================
		ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(byteArray);
		byte[] idBytes = new byte[4];
		arrayInputStream.read(idBytes);
		System.out.println("id:" + bytes2int(idBytes));
		
		byte[] ageBytes = new byte[4];
		arrayInputStream.read(ageBytes);
		System.out.println("age:" + bytes2int(ageBytes));
		
		//==============================================================
        System.out.println("====================整数===================================");
		int a = 5;
		System.out.println(Integer.toBinaryString(a));
		int b = a << 4;   // b=a*2*2*2*2
		System.out.println(b);
		System.out.println(Integer.toBinaryString(b));

        //==============================================================
        System.out.println("====================负数===================================");
        int aa = -5;
        System.out.println(Integer.toBinaryString(aa));
        int bb = aa << 4;   // b=a*2*2*2*2
        System.out.println(bb);
        System.out.println(Integer.toBinaryString(bb));

        System.out.println("====================移位操作================================");
        int ab = 500000;
        System.out.println(Integer.toBinaryString(ab));
        byte b1 = (byte)(ab >> 3*8); //最高的8位  以为操作原来的数字不会变
        byte b2 = (byte)(ab >> 2*8);
        byte b3 = (byte)(ab >> 1*8);
        byte b4 = (byte)(ab >> 0*8); //最低的8位
        System.out.println(b1 + " 补码: " + Integer.toBinaryString(b1));
        System.out.println(b2 + " 补码: " + Integer.toBinaryString(b2));
        System.out.println(b3 + " 补码: " + Integer.toBinaryString(b3));
        System.out.println(b4 + " 补码: " + Integer.toBinaryString(b4));
        System.out.println(Integer.toBinaryString(ab));
	}
	
	/*
	 *如果对 long类型、double类型都要在写相应的方法  太麻烦了 
	 */
	
	/**
	 * 大端字节序列(先写高位，再写低位)
	 * 百度下 大小端字节序列
	 * @param i
	 * @return
	 */
	public static byte[] int2bytes(int i){
		byte[] bytes = new byte[4];
		//向右移动  3个字节一个字节8位
		bytes[0] = (byte)(i >> 3*8);
		bytes[1] = (byte)(i >> 2*8);
		bytes[2] = (byte)(i >> 1*8);
		bytes[3] = (byte)(i >> 0*8);
		return bytes;
	}
	
	
	/**
	 * 大端
	 * @param bytes
	 * @return
	 */
	public static int bytes2int(byte[] bytes){
		// | 或运算  就是合并
		return (bytes[0] << 3*8) |
				(bytes[1] << 2*8) |
				(bytes[2] << 1*8) |
				(bytes[3] << 0*8);
	}

}
