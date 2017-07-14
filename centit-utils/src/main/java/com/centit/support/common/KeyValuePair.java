package com.centit.support.common;

/**
 * key value 数值对; 我们经常想在一个方法中返回多个值，比如返回错误编号和错误文字说明，有 KeyValuePair 就很方便
 * 	return new KeyValuePair(Integer,String)(5,"error message");
 * 返回三个值可以用
 *  return new KeyValuePair ( Integer,KeyValuePair(String,Object))(5,
 *  	new KeyValuePair(String,Object)("error message",otherObje));
 * 以此类推可以返回多个数值
 * 
 * 建议使用 org.apache.commons.lang3.tuple.MutablePair
 * 为什么会有这个类是因为写这个类的时候我不知道有MutablePair类。
 * 这个键值用于返回多个值，这样的变量一般都是不可变的在这种情况下可以使用 ImmutablePair类
 * 
 * @author codefan
 * @param <K>  左边的类型
 * @param <V>   右边的类型
 */
public class KeyValuePair<K,V>{
	private K key;
	private V value;
	
	public KeyValuePair(){
		
	}
	
	public KeyValuePair(K key,V value){
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}
	
	public K getLeft() {
		return key;
	}
	
	public void setKey(K key) {
		this.key = key;
	}
	
	public V getValue() {
		return value;
	}
	
	public V getRight() {
		return value;
	}
	
	public void setValue(V value) {
		this.value = value;
	}
	
	/*public V setValue(V value) {
		V oldValue = this.value;
		this.value = value;
		return oldValue;
	}*/

}
