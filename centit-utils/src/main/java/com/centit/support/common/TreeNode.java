package com.centit.support.common;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.centit.support.algorithm.ReflectionOpt;
import com.centit.support.algorithm.StringBaseOpt;
@SuppressWarnings("unused")
public class TreeNode<T> {
	/**
	 * 存储节点的值
	 */
	private T value;
	/**
	 * 指向父节点，null表示根节点
	 */
	private TreeNode<T> praent;
	/**
	 * 子节点列表
	 */
	private List<TreeNode<T>> children;
	
	public TreeNode(){
		this.children = null;
		this.praent = null;
	}
	
	public TreeNode(T value){
		this.value = value;
		this.children = null;
		this.praent = null;
	}
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
	
	public List<TreeNode<T>> getChildren() {
		return children;
	}	
	
	public void setChildren(List<TreeNode<T>> children) {
		this.children = children;
	}
	
	public TreeNode<T> addChild(T child) {
		TreeNode<T> treeNode = new TreeNode<>(child);
		this.addChild(treeNode);
		return treeNode;
	}
	
	public void addChild(TreeNode<T> child) {
		if(this.children ==null)
			this.children = new ArrayList<>();
		child.praent = this;
		this.children.add(child);
	}

	public TreeNode<T> getPraent() {
		return praent;
	}

	public void setPraent(TreeNode<T> praent) {
		this.praent = praent;
	}
	
	public boolean isLeaf(){
		return children==null || children.size()==0;
	}
	/**
	 * 判断是否为根节点
	 * @return boolean 是否为跟节点
	 */
	public boolean isRoot(){
		return praent==null;
	}
	
	public JSONObject toJSONObject(String childrenPropertyName){
		JSONObject jo ;
		
		if(ReflectionOpt.isScalarType(this.getValue().getClass())){
			jo = new JSONObject();
			jo.put("value", StringBaseOpt.objectToString(this.getValue()));
		}else
			jo = (JSONObject) JSON.toJSON(this.getValue()); 
		
		if(this.children!=null && this.children.size()>0){
			JSONArray ja = new JSONArray();
			for(TreeNode c: this.children){
				ja.add( c.toJSONObject(childrenPropertyName));
			}
			jo.put(childrenPropertyName, ja);
		}
		
		return jo;
	}
	
	public JSONObject toJSONObject(){				
		return toJSONObject("children");
	}
	
	public String toString(){				
		return toJSONObject("children").toJSONString();
	}
	/**
	 * 计算节点到根节点之间的层数，包括自己和根节点
	 * @return 节点到根节点之间的层数，包括自己和根节点
	 */
	public int getPathCount() {
        int result = 0;
        for(TreeNode<T> path = this; path != null; path = path.getPraent()) {
            result++;
        }
        return result;
    }
	/**
	 *  获得从当前节点一直到跟节点路径上所有节点
	 * @return 从当前节点一直到跟节点路径上所有节点
	 */
	public TreeNode<T>[] getTreeNodePath() {
        int i = getPathCount();
        @SuppressWarnings("unchecked")
		TreeNode<T>[]  result = new TreeNode [i--];        
        for(TreeNode<T> path = this; path != null; path = path.getPraent()) {
        	result[i--] = path;
        }
        return result;
    }
	/**
	 * 获得根节点
	 * @return 根节点
	 */
	public TreeNode<T> getRootTreeNode() {
         for(TreeNode<T> path = this; path != null; path = path.getPraent()) {
        	if(path.isRoot())
        		return path;
        }
        return this;
    }
	
	/**
	 * 获得从当前节点一直到跟节点路径上所有节点的值
	 * @return 从当前节点一直到跟节点路径上所有节点的值
	 */
	public T [] getPath() {
        int i = getPathCount();
        @SuppressWarnings("unchecked")
		T []  result =(T[]) new Object[i--];        
        for(TreeNode<T> path = this; path != null; path = path.getPraent()) {
        	result[i--] = path.getValue();
        }
        return result;
    }
	/**
	 * 获得根节点的值
	 * @return 根节点的值
	 */
	public T getRootValue() {
       for(TreeNode<T> path = this; path != null; path = path.getPraent()) {
    	   if(path.isRoot())
    		   return path.getValue();
       }
       return this.getValue();
   }
	
}
