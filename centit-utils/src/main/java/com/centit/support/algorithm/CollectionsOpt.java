package com.centit.support.algorithm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.centit.support.common.TreeNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;

/**
 * 一些通用的算法，这些算法都和具体的业务数据结构解耦
 * 1. sortAsTree* 将list按照树形结构进行排序，这个方式是这个类最重要的一个方法，也是这个类存在的一个原因。
 * 2. compareTwoList 比较两个list，将他们相同的、删除的、新增的分别找出来，
 * 刚好对应sql的 update、delete和insert操作。
 * 3. listToArray 和 arrayToList 通过反射的方式简化了传入的参数。
 * 4. remove* 一组对集合元素清理操作。
 * 5. moveListItem 和 changeListItem ，前者为移动元素位置两个元素之间的所有item位置都有变化，
 * 后者为仅仅交换两个元素的位置
 * 6. clone* 复制集合。
 * 7. storedAsTree、treeToJSONArray 对属性结构的存储或者序列化。
 *
 * @author codefan
 * @version 2.2.5
 */
@SuppressWarnings("unused")
public abstract class CollectionsOpt {

    public CollectionsOpt() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * 移动List一个对象到新的位置
     *
     * @param <T>  泛型类型
     * @param list 输入列表
     * @param item 位置1
     * @param pos  位置2
     */
    public static <T> void moveListItem(List<T> list, int item, int pos) {
        if (item == pos || item < 0 || pos < 0 || item >= list.size() || pos >= list.size())
            return;
        /*T tmp = list.remove(item);
        list.add(pos, tmp);    */
        if (item > pos) {
            T tmp = list.get(item);
            for (int i = item; i > pos; i--)
                list.set(i, list.get(i - 1));
            list.set(pos, tmp);
        } else {
            T tmp = list.get(item);
            for (int i = item; i < pos; i++)
                list.set(i, list.get(i + 1));
            list.set(pos, tmp);
        }
    }

    /**
     * 交换List中两个对象的位置
     *
     * @param <T>  泛型类型
     * @param list 输入列表
     * @param p1   位置1
     * @param p2   位置2
     */
    public static <T> void changeListItem(List<T> list, int p1, int p2) {
        if (p1 == p2)
            return;
        Collections.swap(list, p1, p2);
        /*T tmp = list.get(p1);
        list.set(p1, list.get(p2));
        list.set(p2, tmp);    */
    }

    public static <T, U> ParentChild<? super T> mapParentANdChild(
        Function<? super T, ? extends U> pkExtractor,
        Function<? super T, ? extends U> parentPkExtractor) {
        //Objects.requireNonNull(parentExtractor);
        //Objects.requireNonNull(childExtractor);
        return (p, c) ->
            GeneralAlgorithm.equals(pkExtractor.apply(p), parentPkExtractor.apply(c));
    }

    /**
     * 将数组结构按照树形展示的形式进行排序，将所有孩子元素放到父元素的下面
     * 深度优先的排序
     *
     * @param <T>  泛型类型
     * @param list 输入列表
     * @param c    对比接口
     */
    public static <T> void sortAsTree(List<T> list, ParentChild<? super T> c) {
        int n = list.size();
        if (n < 2)
            return;
        //sorted 已经排序好的数量
        int sortedInd = 0;
        int[] parentInds = new int[n];
        while (sortedInd < n - 1) {
            // 找到所有的根节点
            int parentInd = -1;
            for (int i = sortedInd; i < n; i++) {
                boolean isParent = true;
                for (int j = sortedInd; j < n; j++) {
                    if (i != j && c.parentAndChild(list.get(j), list.get(i))) {
                        isParent = false;
                        break;
                    }
                }
                if (isParent) {
                    parentInd = i;
                    break;
                }
            }
            if (parentInd == -1)
                break;

            moveListItem(list, parentInd, sortedInd);
            parentInds[0] = sortedInd;
            sortedInd++;
            int pathDeep = 1;
            while (pathDeep > 0) {
                int newInsert = 0;
                for (int i = sortedInd; i < n; i++) {
                    if (c.parentAndChild(list.get(parentInds[pathDeep - 1]), list.get(i))) {
                        moveListItem(list, i, sortedInd);
                        parentInds[pathDeep] = sortedInd;
                        pathDeep++;
                        sortedInd++;
                        newInsert++;
                    }
                }
                if (newInsert == 0) {
                    pathDeep--;
                }
            }
            // 查找根节点的所有子元素
            //sortedInd = sortAsTreePiece(list,c,sortedInd);
        }
    }

    /**
     * 将数组结构按照树形展示的形式进行排序，将所有孩子元素放到父元素的下面
     * 深度优先的排序
     *
     * @param list              输入列表
     * @param pkExtractor       父对象 获取 自己的主键
     * @param parentPkExtractor 子对象 获取 父对象主键
     * @param <T>               泛型类型
     * @param <U>               主键类型
     */
    public static <T, U> void sortAsTree(List<T> list, Function<? super T, ? extends U> pkExtractor,
                                         Function<? super T, ? extends U> parentPkExtractor) {
        CollectionsOpt.sortAsTree(list,
            CollectionsOpt.mapParentANdChild(pkExtractor, parentPkExtractor));
    }

    /**
     * 对排序好的树形数组结构 找到JQueryTree要的Indexes
     *
     * @param <T>  泛型类型
     * @param list 输入列表
     * @param c    对比接口
     * @return 排序号的树的索引
     */
    public static <T> List<Integer> makeJqueryTreeIndex(List<T> list, ParentChild<? super T> c) {
        List<Integer> indexes = new ArrayList<Integer>();
        int n = list.size();
        for (int i = 0; i < n; i++) {
            int ind = 0;
            for (int j = 0; j < i; j++) {
                if (c.parentAndChild(list.get(j), list.get(i))) {
                    ind = j + 1;
                    break;
                }
            }
            indexes.add(ind);
        }
        return indexes;
    }

    /**
     * 移除List中的所有null对象
     *
     * @param <T>  泛型类型
     * @param list 输入列表
     * @return 输出列表
     */
    public static <T> List<T> removeNullItem(List<T> list) {
        if (list == null || list.size() < 1)
            return null;
        List<T> retList = new ArrayList<>(list.size());
        for (T t : list) {
            if (t != null)
                retList.add(t);
        }
        return retList;
    }

    /**
     * 移除List中的所有null对象
     *
     * @param <T>  泛型类型
     * @param list 输入数组
     * @return 输出数组
     */
    public static <T> T[] removeNullItem(T[] list) {

        if (list == null || list.length < 1)
            return null;
        int notNullItemPos = -1, size = 0;
        for (int i = 0; i < list.length; i++) {
            if (list[i] != null) {
                size++;
                notNullItemPos = i;
            }
        }
        if (notNullItemPos < 0) {
            return null;
        }
        @SuppressWarnings("unchecked")
        T[] ta = (T[]) Array.newInstance(list[notNullItemPos].getClass(), size);
        size = 0;
        for (int i = 0; i < list.length; i++) {
            if (list[i] != null) {
                ta[size] = list[i];
                size++;
            }
        }
        return ta;
    }

    /**
     * 移除String List中的所有 Blank 对象
     *
     * @param list 输入字符串列表
     * @return 输出字符串列表
     */
    public static List<String> removeBlankString(List<String> list) {
        if (list == null || list.size() < 1)
            return null;
        List<String> retList = new ArrayList<String>();
        for (String t : list) {
            if (StringUtils.isNoneBlank(t))
                retList.add(t);
        }
        return retList;
    }

    /**
     * 移除List中的所有null对象
     *
     * @param list 输入字符串数组
     * @return 输出字符串数组
     */
    public static String[] removeBlankString(String[] list) {
        if (list == null || list.length < 1)
            return null;
        int notNullItemPos = -1, size = 0;
        for (int i = 0; i < list.length; i++) {
            if (StringUtils.isNoneBlank(list[i])) {
                size++;
                notNullItemPos = i;
            }
        }
        if (notNullItemPos < 0) {
            return null;
        }
        String[] ta = new String[size];
        size = 0;
        for (int i = 0; i < list.length; i++) {
            if (StringUtils.isNoneBlank(list[i])) {
                ta[size] = list[i];
                size++;
            }
        }
        return ta;
    }

    /**
     * 将TreeList转换为JSONArray
     *
     * @param <T>                  泛型类型
     * @param treeList             必须是已经通过 sortAsTree 排序好的list
     * @param c                    比较算法，需要实现接口 CollectionsOpt.ParentChild T
     * @param childrenPropertyName 为孩子的 属性名
     * @return JSONArray
     */
    public static <T> JSONArray treeToJSONArray
    (List<T> treeList, ParentChild<? super T> c, String childrenPropertyName) {
        JSONArray jsonTree = new JSONArray();
        Stack<T> treePath = new Stack<T>();
        Stack<JSONObject> jsonPath = new Stack<JSONObject>();
        int pathSum = 0;
        for (T treeNode : treeList) {
            JSONObject jsonNode;
            if (ReflectionOpt.isScalarType(treeNode.getClass())) {
                jsonNode = new JSONObject();
                jsonNode.put("value", StringBaseOpt.objectToString(treeNode));
            } else
                jsonNode = (JSONObject) JSON.toJSON(treeNode);

            while (true) {
                if (pathSum == 0 ||
                    (pathSum > 0 && c.parentAndChild(treePath.peek(), treeNode))) {
                    if (pathSum == 0) {
                        jsonTree.add(jsonNode);
                    } else {
                        JSONObject parentJson = jsonPath.peek();
                        JSONArray children = (JSONArray) parentJson.get(childrenPropertyName);
                        if (children == null)
                            children = new JSONArray();
                        children.add(jsonNode);
                        parentJson.put(childrenPropertyName, children);
                    }
                    treePath.push(treeNode);
                    jsonPath.push(jsonNode);

                    pathSum++;
                    break;
                } else {
                    treePath.pop();
                    jsonPath.pop();
                    pathSum--;
                }
            }
        }
        return jsonTree;
    }

    /**
     * 将列表转换为tree结构的json
     *
     * @param <T>                  泛型类型
     * @param treeList             待排序的List
     * @param c                    比较算法，需要实现接口 CollectionsOpt.ParentChild T
     * @param childrenPropertyName 为孩子的 属性名
     * @return JSONArray
     */
    public static <T> JSONArray srotAsTreeAndToJSON
    (List<T> treeList, ParentChild<? super T> c, String childrenPropertyName) {
        sortAsTree(treeList, c);
        return treeToJSONArray(treeList, c, childrenPropertyName);
    }

    /**
     * 将数组结构按照树形展示的形式进行排序，将所有孩子元素放到父元素的下面
     * 深度优先的排序
     *
     * @param <T>  泛型类型
     * @param list 输入数组
     * @param c    对比接口
     * @return 排序号的列表
     */
    public static <T> List<TreeNode<T>> storedAsTree(List<T> list, ParentChild<? super T> c) {

        List<TreeNode<T>> treeList = new ArrayList<TreeNode<T>>();
        for (T m : list) {
            treeList.add(new TreeNode<T>(m));
        }
        for (TreeNode<T> cNode : treeList) {
            for (TreeNode<T> pNode : treeList) {
                if (pNode != cNode && c.parentAndChild(pNode.getValue(), cNode.getValue())) {
                    pNode.addChild(cNode);
                    break;
                }
            }
        }
        List<TreeNode<T>> resList = new ArrayList<TreeNode<T>>();
        for (TreeNode<T> node : treeList) {
            if (node.isRoot())
                resList.add(node);
        }
        return resList;
    }

    /**
     * 将TreeList转换为JSONArray
     *
     * @param <T>                  泛型类型
     * @param treeList             需要排序的对象列表 必须是 List
     * @param childrenPropertyName 为孩子的 属性名
     * @return JSONArray
     */
    public static <T> JSONArray treeToJSONArray
    (List<TreeNode<T>> treeList, String childrenPropertyName) {
        if (treeList == null || treeList.size() == 0)
            return null;

        JSONArray ja = new JSONArray();
        for (TreeNode<T> c : treeList) {
            ja.add(c.toJSONObject(childrenPropertyName));
        }
        return ja;
    }

    /**
     * 将列表转换为tree结构的json
     * 和 srotAsTreeAndToJSON 用不同的算法实现，这个需要额外的空间，用递归实现。
     *
     * @param <T>                  泛型类型
     * @param treeList             待排序的List
     * @param c                    比较算法，需要实现接口 CollectionsOpt.ParentChild T
     * @param childrenPropertyName 为孩子的 属性名
     * @return JSONArray
     */
    public static <T> JSONArray srotAsTreeAndToJSON2
    (List<T> treeList, ParentChild<? super T> c, String childrenPropertyName) {

        List<TreeNode<T>> sortTree = storedAsTree(treeList, c);
        return treeToJSONArray(sortTree, childrenPropertyName);
    }

    /*
     * 克隆 一个 list
     */
    public static <T> List<T> cloneList(Collection<T> souList) {
        if (souList == null) {
            return null;
        }
        ArrayList<T> deslist = new ArrayList<>(souList.size() + 1);
        deslist.addAll(souList);
        return deslist;
    }

    /*
     * 克隆 一个 array
     */
    public static <T> T[] cloneArray(T[] souList) {
        if (souList == null) {
            return null;
        }
        return souList.clone();
        /*if(souList==null || souList.length==0)
            return null;

        @SuppressWarnings("unchecked")
        T[] ta =(T[]) Array.newInstance(souList[0].getClass(), souList.length);
        for(int i=0;i<souList.length;i++)
            ta[i] = souList[i];
        return ta;*/
    }

    /**
     * 对比两个列表，判断哪些需要新增、哪些需要删除、哪些需要更新
     *
     * @param <T>     泛型类型
     * @param oldList 原始list
     * @param newList 新的list
     * @param compare 为对象T的主键排序对比函数，
     * @return 返回三个list， 第一个是 需要新增的，第二个是 新旧对 他们拥有相同的排序值（主键），第三为新值中没有的，即需要删除的
     * insert T update(old,new) T,T  delete T
     */
    public static <T> Triple<List<T>, List<Pair<T, T>>, List<T>>
    compareTwoList(List<T> oldList, List<T> newList, Comparator<T> compare) {
        if (oldList == null || oldList.size() == 0)
            return new ImmutableTriple<>(
                newList, null, null);
        if (newList == null || newList.size() == 0)
            return new ImmutableTriple<>(
                null, null, oldList);
        List<T> souList = cloneList(oldList);
        List<T> desList = cloneList(newList);
        Collections.sort(souList, compare);
        Collections.sort(desList, compare);
        //---------------------------------------
        int i = 0;
        int sl = souList.size();
        int j = 0;
        int dl = desList.size();
        List<T> insertList = new ArrayList<>();
        List<T> delList = new ArrayList<>();
        List<Pair<T, T>> updateList = new ArrayList<>();
        while (i < sl && j < dl) {
            int n = compare.compare(souList.get(i), desList.get(j));
            if (n < 0) {
                delList.add(souList.get(i));
                i++;
            } else if (n == 0) {
                updateList.add(new ImmutablePair<>(souList.get(i), desList.get(j)));
                i++;
                j++;
            } else /*if(n>0)*/ {
                insertList.add(desList.get(j));
                j++;
            }
        }

        while (i < sl) {
            delList.add(souList.get(i));
            i++;
        }

        while (j < dl) {
            insertList.add(desList.get(j));
            j++;
        }

        return new ImmutableTriple<>(insertList, updateList, delList);
    }

    /**
     * 将list(Collection 所以 set也可以) 转换为数组， list.toArray(T[]) 感觉不太好用，要new一个接受的数组对象
     *
     * @param <T>       类型
     * @param listObj   Collection 对象 可以是list 也可以是 set
     * @param classType T 的类型
     * @return T[] 数组
     */
    public static <T> T[] listToArray(Collection<T> listObj, Class<T> classType) {
        if (listObj == null || listObj.size() == 0)
            return null;
        @SuppressWarnings("unchecked")
        T[] ta = (T[]) Array.newInstance(classType, listObj.size());
        return listObj.toArray(ta);
    }

    /**
     * 将list(Collection 所以 set也可以) 转换为数组， list.toArray(T[]) 感觉不太好用，要new一个接受的数组对象
     *
     * @param listObj Collection 对象 可以是list 也可以是 set
     * @param <T>     类型
     * @return 数组
     * 注意，如果这个 T 是一个 接口，并且 Collection 中的内容是这个接口的不同实现，这个方法将抛异常,
     * 这时候需要调用   &lt;T&gt; T[] listToArray(Collection&lt;T&gt; listObj, Class&lt;T&gt; classType)其中 classType 传入接口类
     */
    public static <T> T[] listToArray(Collection<T> listObj) {
        if (listObj == null || listObj.size() == 0)
            return null;
        @SuppressWarnings("unchecked")
        T[] ta = (T[]) Array.newInstance(listObj.iterator().next().getClass(), listObj.size());

        return listObj.toArray(ta);
        /*for(int i=0;i<listObj.size();i++)
            ta[i] = listObj.get(i);
        return ta;*/
    }

    public static <T> List<T> arrayToList(T[] arrayObj) {
        if (arrayObj == null || arrayObj.length == 0)
            return null;
        List<T> listObj = new ArrayList<>(arrayObj.length);
        Collections.addAll(listObj, arrayObj);
        return listObj;
    }

    /**
     * 参数必须是 string object string object ....
     *
     * @param objs 参数必须是 string object string object ....
     * @return Map &lt; String,Object &gt;
     */
    public static Map<String, Object> createHashMap(Object... objs) {
        if (objs == null || objs.length < 2)
            return null;
        Map<String, Object> paramsMap = new HashMap<>(objs.length);
        for (int i = 0; i < objs.length / 2; i++) {
            paramsMap.put(String.valueOf(objs[i * 2]), objs[i * 2 + 1]);
        }
        return paramsMap;
    }

    public static <T> Map<String, T> createHashMap(List<T> listData, Function<T, String> func){
        if(listData==null) {
            return null;
        }
        Map<String, T> appendMap = new HashMap<>(listData.size());
        for(T d : listData){
            appendMap.put(func.apply(d), d);
        }
        return appendMap;
    }

    public static <T> Map<String, T> createHashMap(String[] keys, T[] values) {
        if (keys == null || values == null)
            return null;
        int len = Math.min(keys.length, values.length);
        Map<String, T> paramsMap = new HashMap<>(len + 1);
        for (int i = 0; i < len; i++) {
            paramsMap.put(keys[i], values[i]);
        }
        return paramsMap;
    }

    public static <K, V> HashMap<K, V> cloneHashMap(Map<K, V> souMap) {
        if (souMap == null) {
            return null;
        }
        HashMap<K, V> paramsMap = new HashMap<>(souMap.size() + 1);
        paramsMap.putAll(souMap);
        //paramsMap.clone()
        return paramsMap;
    }

    public static <K, V> Map<K, V> unionTwoMap(Map<K, V> map1, Map<K, V> map2) {
        if(map1 == null){
            return map2;
        }
        if(map2 == null){
            return map1;
        }
        Map<K, V> paramsMap = new HashMap<>(map1.size() + map2.size() + 2);
        paramsMap.putAll(map2);
        paramsMap.putAll(map1);
        return paramsMap;
    }

    /**
     * @param objs 参数类型需要一致
     * @param <T>  参数类型
     * @return List &lt; T &gt;
     */
    @SafeVarargs
    public static <T> List<T> createList(T... objs) {
        if (objs == null)
            return null;
        return Arrays.asList(objs);
    }

    /**
     * 这个按道理可以同 stream来处理, 但是类型转换不太好弄
     * 获取一个list中的所有对象的一个属性，并组成一个新的数组
     * @param list 数组
     * @param propExtractor 对象方法
     * @param <T> 数组类型
     * @param <U> 属性类型
     * @return 属性列表
     */
    public static <T, U> List<U> extraListProperties(List<T> list, Function<T,U> propExtractor) {
        if(list == null){
            return null;
        }
        //list.stream().map(propExtractor).toArray() object[]
        List<U> uList = new ArrayList<>(list.size());
        for(T t : list){
            uList.add(propExtractor.apply(t));
        }
        return uList;
    }

    /*public static <T, U> U[] extraArrayProperties(T [] array, Function<T,U> propExtractor) {
        if(array == null){
            return null;
        }
        U[] uList = (U[]) Array.newInstance(Object.class, array.length);
        int i=0;
        for(T t : array){
            uList[i++] = propExtractor.apply(t);
        }
        return uList;
    }*/

    public static <T> HashSet<T> cloneSet(Collection<T> souCollection) {
        if (souCollection == null) {
            return null;
        }
        HashSet<T> paramsSet = new HashSet<>(souCollection.size() + 1);
        paramsSet.addAll(souCollection);
        //paramsSet.clone()
        return paramsSet;
    }

    /**
     * @param objs 参数类型需要一致
     * @param <T>  参数类型
     * @return HashSet &lt; T &gt;
     */
    @SafeVarargs
    public static <T> HashSet<T> createHashSet(T... objs) {
        if (objs == null)
            return null;
        HashSet<T> paramsSet = new HashSet<>(objs.length * 2 + 1);
        Collections.addAll(paramsSet, objs);
        return paramsSet;
    }

    @SuppressWarnings("unchecked")
    public static <T> T unmodifiableObject(T obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof List) {
            return (T) Collections.unmodifiableList((List) obj);
        }
        if (obj instanceof Map) {
            return (T) Collections.unmodifiableMap((Map) obj);
        }
        if (obj instanceof Set) {
            return (T) Collections.unmodifiableSet((Set) obj);
        }
        if (obj instanceof Collection) {
            return (T) Collections.unmodifiableCollection((Collection) obj);
        }
        return obj;
    }

    /**
     * @param object map的key必须是string类型否则会报错
     * @return 返回map
     */
    public static Map<String, Object> objectToMap(Object object) {
        if (object instanceof Map) {
            return (Map<String, Object>) object;
        }
        if (ReflectionOpt.isScalarType(object.getClass())) {
            if(object instanceof String){
                String objStr = (String) object;
                if(objStr.startsWith("{") && objStr.endsWith("}")){
                    //JSONObject
                    Object mapObj = JSON.parse(objStr);
                    if(mapObj instanceof Map){
                        return (Map<String, Object>) mapObj;
                    }
                }
            }
            return CollectionsOpt.createHashMap("scalar", object);
        }
        if (object.getClass().isArray()) {
            int len = Array.getLength(object);
            HashMap<String, Object> map = new HashMap<>(len * 5 / 4 + 1);
            for (int i = 0; i < len; i++) {
                map.put(String.valueOf(i), Array.get(object, i));
            }
            return map;
        }
        if (object instanceof Collection) {
            HashMap<String, Object> map = new HashMap<>();
            int i = 0;
            for (Object po : (Collection<?>) object) {
                map.put(String.valueOf(i++), po);
            }
            return map;
        }
        Object obj = JSON.toJSON(object);
        if (obj instanceof JSONObject) {
            return (JSONObject) obj;
        }
        return CollectionsOpt.createHashMap("data", object);
    }

    public static List<Object> objectToList(Object object) {
        if(object instanceof List){
            return (List<Object>)object;
        } else if(object instanceof Collection){
            Collection<?> collection = (Collection<?>)object;
            List<Object> objlist = new ArrayList<>(collection.size());
            objlist.addAll(collection);
            return objlist;
        } else {
            Class<?> clazz = object.getClass();
            if (clazz.isArray()) {
                int len = Array.getLength(object);
                List<Object> objlist = new ArrayList<>(len);
                if (len > 0) {
                    for (int i = 0; i < len; i++) {
                        objlist.add(Array.get(object, i));
                    }
                }
                return objlist;
            } else {
                return createList(object);
            }
        }
    }

    public static Map<String, String> objectMapToStringMap(Map<? extends Object, ? extends Object> objectMap) {
        if(objectMap==null){
            return null;
        }
        Map<String, String> stringMap = new HashMap<>(objectMap.size());
        for(Map.Entry<? extends Object, ? extends Object> ent : objectMap.entrySet()){
            stringMap.put(StringBaseOpt.objectToString(ent.getKey()),
                StringBaseOpt.objectToString(ent.getValue()));
        }
        return stringMap;
    }

    public static <K,V> Map<K,V> translateMapType(Map<? extends Object, ? extends Object> objectMap,
                                                  Function<Object, K> transKey , Function<Object, V> transValue) {
        if(objectMap==null){
            return null;
        }
        Map<K, V> stringMap = new HashMap<>(objectMap.size());
        for(Map.Entry<? extends Object, ? extends Object> ent : objectMap.entrySet()){
            stringMap.put(transKey.apply(ent.getKey()),
                transValue.apply(ent.getValue()));
        }
        return stringMap;
    }


    public static <T> T fetchFirstItem(Collection<T> collection) {
        if (collection == null || collection.isEmpty())
            return null;
        return collection.iterator().next();
    }

    public static <T> T fetchFirstItem(T[] array) {
        if (array == null || array.length < 1)
            return null;
        return array[0];
    }

    public static <T, R> Set<R> mapArrayToSet(T[] array, Function<T, R> func){
        if(array == null){
            return null;
        }
        Set<R> retSet = new HashSet<>();
        for(T obj : array){
            retSet.add(func.apply(obj));
        }
        return retSet;
    }

    public static <T, R> Set<R> mapCollectionToSet(Collection<T> array, Function<T, R> func){
        if(array == null){
            return null;
        }
        Set<R> retSet = new HashSet<>();
        for(T obj : array){
            retSet.add(func.apply(obj));
        }
        return retSet;
    }

    public static <T, R> List<R> mapArrayToList(T[] array, Function<T, R> func){
        if(array == null){
            return null;
        }
        List<R> retList = new ArrayList<>();
        for(T obj : array){
            retList.add(func.apply(obj));
        }
        return retList;
    }

    public static <T, R> List<R> mapCollectionToList(Collection<T> array, Function<T, R> func, boolean ignoreNull){
        if(array == null){
            return null;
        }
        List<R> retList = new ArrayList<>();
        for(T obj : array){
            R v = func.apply(obj);
            if(!ignoreNull || v !=null) {
                retList.add(func.apply(obj));
            }
        }
        return retList;
    }

    public static <T, R> List<R> mapCollectionToList(Collection<T> array, Function<T, R> func){
       return mapCollectionToList(array, func, false);
    }
    /**
     * 判断两个对象是否是父子关系，用于针对树形展示的数据结构进行排序
     *
     * @author codefan
     * @version $Rev$ <br>
     * $Id$
     */
    public interface ParentChild<T> {
        boolean parentAndChild(T p, T c);
    }
}
