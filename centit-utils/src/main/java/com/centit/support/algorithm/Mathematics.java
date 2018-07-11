package com.centit.support.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.Comparator;

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

        Collections.sort(listSouce, comparable);
        int len = listSouce.size();
        List<Integer> comPos = new ArrayList<>(len);
        List<List<T>> subList = new ArrayList<>(len);
        List<T> comRes = new ArrayList<>(len);

        for(int i=0;i<len;i++){
            comPos.add(-1);
            subList.add(new ArrayList<>(len));
            comRes.add(null);
        }
        comPos.set(0,0);
        subList.set(0,listSouce);

        int sortIndex = 0;

        while(sortIndex >=0 ){
            comRes.set(sortIndex, subList.get(sortIndex).get( comPos.get(sortIndex)));
            if( sortIndex == len - 2){ // 如果获得一个排序
                comRes.set( sortIndex +1,
                        subList.get(sortIndex).get(
                                (comPos.get(sortIndex) +1) % (len - sortIndex ) ) );
                consumer.accept(comRes);
                //回退
                while(sortIndex >=0 ) {

                    //当前pos ++
                    while (comPos.get(sortIndex) + 1 < len - sortIndex && comparable.compare(
                            subList.get(sortIndex).get(comPos.get(sortIndex)),
                            subList.get(sortIndex).get(comPos.get(sortIndex) + 1)
                    ) == 0) {
                        comPos.set(sortIndex, comPos.get(sortIndex) + 1);
                    }

                    comPos.set(sortIndex, comPos.get(sortIndex) + 1);
                    // 如果已经到上线，继续回退
                    if (comPos.get(sortIndex)  < len - sortIndex) {
                        //重新计算下个列表
                        subList.get(sortIndex + 1).clear();
                        subList.get(sortIndex + 1).addAll(subList.get(sortIndex));
                        subList.get(sortIndex + 1).remove( comPos.get(sortIndex).intValue());
                        comPos.set(sortIndex + 1,0);
                        break;
                    }else{
                        sortIndex--;
                        //comPos.set(sortIndex, comPos.get(sortIndex) + 1);
                    }
                }
            }else {
                subList.get(sortIndex + 1).clear();
                subList.get(sortIndex + 1).addAll(subList.get(sortIndex));
                subList.get(sortIndex + 1).remove( comPos.get(sortIndex).intValue());
                comPos.set(sortIndex + 1,0);
                sortIndex++;
            }

        }

    }
}
