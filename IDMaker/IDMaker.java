package com.bc.mybatiscrud.utils.makeid;

import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.UUID;

//随机id生成
@Component
public final class IDMaker {
    private final static String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /**
     * 把位数转换可以带入Math.random()的对应范围
     * @param length 位数
     */
    private static long getRangeByLength(int length){
        //10的n-1次方(传入id长度后需-1,否则生成位数会+1(因为用的次方))
        return (long) Math.pow(10, length-1);
    }

    /**
     * 只生成数字
     * @param length 生成id的位数
     * @return long
     */
    public static long onlyNum(int length){
        return (long) ((Math.random()*9+1)*getRangeByLength(length));
    }

    /**
     * 生成数字字母和标识符
     * @return string
     */
    public static String numAndStringRandom(){
        return UUID.randomUUID().toString();
    }

    /**
     * 只生成字符串
     * @param length 生成id的位数
     * @return stringBuilder
     */
    public static StringBuilder onlyString(int length){
        StringBuilder stringBuilder=new StringBuilder(); //stringBuilder保存拼接后的结果
        for (int i = 0; i < length; i++) {
            char charRandom = getCharRandom();
            stringBuilder.append(charRandom); //拼接字符
        }
        return stringBuilder;
    }

    /**
     * 生成 字符串+数字 格式的id
     * @param stringLength 所需字符串的位数
     * @param numLength 所需数字的位数
     * @return string+num
     */
    public static String stringAndNum(int stringLength,int numLength){
        String string = String.valueOf(onlyString(stringLength));
        String num = String.valueOf(onlyNum(numLength));
        return string+num;
    }

    /**
     * 生成 数字+字符串 格式的id
     * @param numLength 所需数字的位数
     * @param stringLength 所需字符串的位数
     * @return num+string
     */
    public static String numAndString(int numLength,int stringLength){
        String string = String.valueOf(onlyString(stringLength));
        String num = String.valueOf(onlyNum(numLength));
        return num+string;
    }

    /**
     * 随机一个字符
     * @return randomChar
     */
    private static char getCharRandom(){
        Random random=new Random();
        int randomInt = random.nextInt(str.length());//以str长度为基准随机生成一个位置
        return str.charAt(randomInt);//根据索引位置提取字符串内的一个字符
    }

    /**
     * 自定义前缀生成id
     * @param prefix 前缀内容
     * @param functionName 随机生成的id种类:
     *                     onlyNum: 后续以数字形式生成;
     *                     numAndStringRandom: 字母和数字、标识符混合;
     *                     onlyString: 后续以字符串形式生成
     * @param length 后续随机生成的id长度
     * @return string
     * @throws NoSuchMethodException 找不到方法(方法名参数传递错误)
     * @throws InvocationTargetException 被调用方法本身抛出异常
     * @throws IllegalAccessException 方法不合要求(方法名错误？传参错误？)
     */
    public static String customizePrefix(Object prefix,String functionName,int length) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (prefix==null){
            prefix="";
        }

        Method method=IDMaker.class.getDeclaredMethod(functionName,int.class);//int.class:位数长度参数整形
        return (String) prefix+method.invoke(null, length);//obj:null 方法都是静态的
    }
}
