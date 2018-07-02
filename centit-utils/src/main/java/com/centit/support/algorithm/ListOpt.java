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


/**
 * 
 * 一些通用的算法，这些算法都和具体的业务数据结构解耦
 *1. sortAsTree* 将list按照树形结构进行排序，这个方式是这个类最重要的一个方法，也是这个类存在的一个原因。
 2. compareTwoList 比较两个list，将他们相同的、删除的、新增的分别找出来，
        刚好对应sql的 update、delete和insert操作。
 3. listToArray 和 arrayToList 通过反射的方式简化了传入的参数。
 4. remove* 一组对集合元素清理操作。
 5. moveListItem 和 changeListItem ，前者为移动元素位置两个元素之间的所有item位置都有变化，
        后者为仅仅交换两个元素的位置
 6. clone* 复制集合。
 7. storedAsTree、treeToJSONArray 对属性结构的存储或者序列化。

    @see com.centit.support.algorithm.CollectionsOpt
 * @author codefan
 * @version  2.2.5
 */
@Deprecated
public abstract class ListOpt extends CollectionsOpt {

}
