package com.centit.support.algorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class Mathematics {

    /**
     * 非递归的排列组合
     * @param listSouce 可 排序的 列表
     * @param comparable 比较函数
     * @param consumer 消费排序结果
     * @param <T> 泛型
     */
    public static <T> void  combination(List<T> listSouce ,
                                 Comparator<? super T> comparable,
                                 Consumer<List<T>> consumer){
        if(listSouce.size()<2){
            consumer.accept(listSouce);
            return;
        }
        listSouce.sort(comparable);
        int len = listSouce.size();
        //标记排序位置的栈
        List<Integer> comPos = new ArrayList<>(len);
        //标记已经排好序的元素
        List<Boolean> usedItem = new ArrayList<>(len);
        //记录排序结果
        List<T> comRes = new ArrayList<>(len);

        for(int i=0;i<len;i++){
            comPos.add(-1);
            usedItem.add(false);
            comRes.add(null);
        }
        comPos.set(0,0);
        int sortIndex = 0;
        usedItem.set(0, true);
        while(sortIndex >=0 ){
            comRes.set(sortIndex, listSouce.get( comPos.get(sortIndex)));
            if( sortIndex == len - 2){ // 如果获得一个排序
                for(int i=0; i< len; i++){
                    if(!usedItem.get(i)){// 将最后一个未使用的添加到排列的最后
                        comRes.set( sortIndex +1, listSouce.get(i));
                        break;
                    }
                }
                consumer.accept(comRes);
                //usedItem.set(comPos.get(sortIndex), false);
                while(sortIndex >=0 ) {
                    //下一个
                    int prePos = comPos.get(sortIndex);
                    usedItem.set(prePos, false);
                    //当前pos ++ （步进）
                    while (comPos.get(sortIndex) + 1  < len &&
                            ( usedItem.get(comPos.get(sortIndex) + 1) || comparable.compare(
                                    listSouce.get(prePos),
                                    listSouce.get(comPos.get(sortIndex) + 1)
                    ) == 0 )) {
                        comPos.set(sortIndex, comPos.get(sortIndex) + 1);
                    }

                    comPos.set(sortIndex, comPos.get(sortIndex) + 1);
                    // 如果已经到上线，继续回退
                    if (comPos.get(sortIndex)  < len ) {
                        //重新计算下个列表
                        usedItem.set(comPos.get(sortIndex), true);
                        comRes.set( sortIndex, listSouce.get(comPos.get(sortIndex)));
                        break;
                    }else{ // 回退
                        sortIndex--;
                        //comPos.set(sortIndex, comPos.get(sortIndex) + 1);
                    }
                }
            } else { // 下一个
                for(int i=0; i< len; i++){
                    if(!usedItem.get(i)){
                        comPos.set(sortIndex + 1,i);
                        usedItem.set(i,true);
                        break;
                    }
                }
                sortIndex++;
            }
        }
    }
}
