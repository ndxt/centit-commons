package com.centit.support.algorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class Mathematics {


    private static <T> void swap(List<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }

    private static <T> void reverse(List<T> list, int i, int j) {
        while (i < j) {
            swap(list, i++, j--);
        }
    }

    /*
     * 编写一个类似C++ STL中的两个关于全排列的函数,分别为next_permutation， prev_permutation
     */
    public static <T> boolean nextPermutation(List<T> list, Comparator<? super T> comparable) {
        int i = list.size() - 1;
        while (i > 0 && comparable.compare(list.get(i - 1), list.get(i))  >=  0) {
            i--;
        }

        if (i == 0) {
            return false;
        }

        int j = list.size() - 1;
        while (comparable.compare(list.get(j), list.get(i - 1)) <= 0) {
            j--;
        }

        swap(list, i - 1, j);
        reverse(list, i, list.size() - 1);
        return true;
    }
    /*
     * 编写一个类似C++ STL中的两个关于全排列的函数, prev_permutation
     */
    public static <T> boolean prevPermutation(List<T> list, Comparator<? super T> comparable) {
        if (list == null || list.isEmpty()) {
            return false;
        }
        int __last = list.size();
        int __i = __last-1;

        while (true)
        {
            int __ip1 = __i;
            if (comparable.compare(list.get(--__i), list.get(__ip1)) >= 0)
            {
                int __j = __last;
                while (comparable.compare(list.get(__i), list.get(--__j))<=0)
                    ;
                swap(list, __i, __j);
                reverse(list, __ip1, __last-1);
                return true;
            }
            if (__i == 0)
            {
                reverse(list,0, __last-1);
                return false;
            }
        }
    }

    /**
     * 非递归的排列
     *
     * @param listSouce  可 排序的 列表
     * @param comparable 比较函数
     * @param consumer   消费排序结果
     * @param <T>        泛型
     */
    public static <T> void permutation(List<T> listSouce,
                                       Comparator<? super T> comparable,
                                       Consumer<List<T>> consumer) {
        int len = listSouce.size();
        if (len < 2) {
            consumer.accept(listSouce);
            return;
        }
        listSouce.sort(comparable);
        //标记排序位置的栈
        List<Integer> comPos = new ArrayList<>(len);
        //标记已经排好序的元素
        List<Boolean> usedItem = new ArrayList<>(len);
        //记录排序结果
        List<T> comRes = new ArrayList<>(len);

        for (int i = 0; i < len; i++) {
            comPos.add(-1);
            usedItem.add(false);
            comRes.add(null);
        }
        comPos.set(0, 0);
        int sortIndex = 0;
        usedItem.set(0, true);
        while (sortIndex >= 0) {
            comRes.set(sortIndex, listSouce.get(comPos.get(sortIndex)));
            if (sortIndex == len - 2) { // 如果获得一个排序
                for (int i = 0; i < len; i++) {
                    if (!usedItem.get(i)) {// 将最后一个未使用的添加到排列的最后
                        comRes.set(sortIndex + 1, listSouce.get(i));
                        break;
                    }
                }
                consumer.accept(comRes);
                //usedItem.set(comPos.get(sortIndex), false);
                while (sortIndex >= 0) {
                    //下一个
                    int prePos = comPos.get(sortIndex);
                    usedItem.set(prePos, false);
                    //当前pos ++ （步进）
                    while (comPos.get(sortIndex) + 1 < len &&
                        (usedItem.get(comPos.get(sortIndex) + 1) || comparable.compare(
                            listSouce.get(prePos),
                            listSouce.get(comPos.get(sortIndex) + 1)
                        ) == 0)) {
                        comPos.set(sortIndex, comPos.get(sortIndex) + 1);
                    }

                    comPos.set(sortIndex, comPos.get(sortIndex) + 1);
                    // 如果已经到上线，继续回退
                    if (comPos.get(sortIndex) < len) {
                        //重新计算下个列表
                        usedItem.set(comPos.get(sortIndex), true);
                        comRes.set(sortIndex, listSouce.get(comPos.get(sortIndex)));
                        break;
                    } else { // 回退
                        sortIndex--;
                        //comPos.set(sortIndex, comPos.get(sortIndex) + 1);
                    }
                }
            } else { // 下一个
                for (int i = 0; i < len; i++) {
                    if (!usedItem.get(i)) {
                        comPos.set(sortIndex + 1, i);
                        usedItem.set(i, true);
                        break;
                    }
                }
                sortIndex++;
            }
        }
    }

    /**
     * 非递归的组合
     *
     * @param listSouce  待选择的集合
     * @param selected   选择组合的数量
     * @param comparable 比较函数，用于去重
     * @param consumer   消费者函数，处理输出结果
     * @param <T>        泛型参数
     */
    public static <T> void combination(List<T> listSouce, int selected,
                                       Comparator<? super T> comparable,
                                       Consumer<List<T>> consumer) {
        int len = listSouce.size();
        if (len < selected || selected < 1) {
            return;
        }
        if (len == selected) {
            consumer.accept(listSouce);
            return;
        }
        listSouce.sort(comparable);
        //标记排序位置的栈
        List<Integer> selectPos = new ArrayList<>(selected);
        List<T> comRes = new ArrayList<>(selected);
        for (int i = 0; i < selected; i++) {
            selectPos.add(i);
            comRes.add(listSouce.get(i));
        }
        int sortIndex = selected - 1;
        while (sortIndex >= 0) {
            if (sortIndex == selected - 1) {
                consumer.accept(comRes);
            }
            while (selectPos.get(sortIndex) + 1 < len && comparable.compare(
                listSouce.get(selectPos.get(sortIndex)),
                listSouce.get(selectPos.get(sortIndex) + 1)) == 0) {
                selectPos.set(sortIndex, selectPos.get(sortIndex) + 1);
            }
            selectPos.set(sortIndex, selectPos.get(sortIndex) + 1);
            if (selectPos.get(sortIndex) <= len - selected + sortIndex) {
                //重新计算下个列表
                comRes.set(sortIndex, listSouce.get(selectPos.get(sortIndex)));
                int startPos = selectPos.get(sortIndex) + 1;
                for (int i = sortIndex + 1; i < selected; i++) {
                    selectPos.set(i, startPos);
                    comRes.set(i, listSouce.get(startPos));
                    startPos++;
                }
                sortIndex = selected - 1;
                //continue;
            } else { // 回退
                sortIndex--;
                //comPos.set(sortIndex, comPos.get(sortIndex) + 1);
            }
        }
    }

    /**
     * 非递归的排列与组合
     *
     * @param listSouce  待选择的集合
     * @param selected   选择组合的数量
     * @param comparable 比较函数，用于去重
     * @param consumer   消费者函数，处理输出结果
     * @param <T>        泛型参数
     */
    public static <T> void permutationAndCombination(List<T> listSouce, int selected,
                                                     Comparator<? super T> comparable,
                                                     Consumer<List<T>> consumer) {
        combination(listSouce, selected, comparable,
            (oneCom) -> permutation(oneCom, comparable, consumer));
    }
}
