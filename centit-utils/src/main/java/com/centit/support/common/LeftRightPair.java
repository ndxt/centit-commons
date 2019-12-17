package com.centit.support.common;

/**
 * left right 数值对; 我们经常想在一个方法中返回多个值，比如返回错误编号和错误文字说明，有 KeyValuePair 就很方便
 * return new KeyValuePair(Integer,String)(5,"error message");
 * 返回三个值可以用
 * return new KeyValuePair ( Integer,KeyValuePair(String,Object))(5,
 * new KeyValuePair(String,Object)("error message",otherObje));
 * 以此类推可以返回多个数值
 * <p>
 * 建议使用
 *
 * @param <L> 左边的类型
 * @param <R> 右边的类型
 * @author codefan
 * @see org.apache.commons.lang3.tuple.MutablePair
 * 为什么会有这个类是因为写这个类的时候我不知道有MutablePair类。
 * 这个键值用于返回多个值，这样的变量一般都是不可变的在这种情况下可以使用 ImmutablePair类
 */
@SuppressWarnings("unused")
public class LeftRightPair<L, R> {
    private L left;
    private R right;

    public LeftRightPair() {

    }

    public LeftRightPair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> LeftRightPair<L, R> of(final L left, final R right) {
        return new LeftRightPair<>(left, right);
    }

    public L getLeft() {
        return left;
    }

    public void setLeft(L left) {
        this.left = left;
    }

    public R getRight() {
        return right;
    }

    public void setRight(R right) {
        this.right = right;
    }

    /*public V setRight(V right) {
        V oldValue = this.right;
        this.right = right;
        return oldValue;
    }*/

}
