package com.baidu.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @ClassName aaa
 * @Description: TODO
 * @Author wangshanle
 * @Date 2020/10/31
 * @Version V1.0
 **/
public class aaa {
    private static List<String> list=new ArrayList<String>();

    public static void addList(){
        for(int i=0;i<1000;i++){
            list.add("测试"+i);
        }
    }
    public static void ergodicTest1(){
        long startTime=System.currentTimeMillis();
        for(int i=0;i<list.size();i++){
            System.out.print(list.get(i));
        }
        System.out.println();
        long endTime=System.currentTimeMillis();

        System.out.println("花费时间为1:"+(endTime-startTime));
    }
    public static void ergodicTest2(){
        long startTime=System.currentTimeMillis();
        for(String str:list){
            System.out.print(str);
        }
        System.out.println();
        long endTime=System.currentTimeMillis();

        System.out.println("花费时间为2:"+(endTime-startTime));
    }
    public static void ergodicTest3(){
        long startTime=System.currentTimeMillis();
        Iterator<String> itr=list.iterator();
        while(itr.hasNext()){
            System.out.print(itr.next());
        }
        long endTime=System.currentTimeMillis();
        System.out.println();
        System.out.println("花费时间为3:"+(endTime-startTime));
    }
    public static void main(String[] args) {
        HashMap hashMap = new HashMap(20,1);
        System.out.println(hashMap.size());
//        addList();
//        ergodicTest1();
//        ergodicTest2();
//        ergodicTest3();
    }
}
