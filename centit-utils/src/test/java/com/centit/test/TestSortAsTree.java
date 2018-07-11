package com.centit.test;

import com.centit.support.algorithm.CollectionsOpt;

import java.util.ArrayList;
import java.util.List;

public class TestSortAsTree {

    public static class ListNode{
        private int id;
        private int pid;
        private String name;
        public ListNode(int id,int pid,String name){
            this.id=id;
            this.pid=pid;
            this.name = name;
        }

        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        public int getPid() {
            return pid;
        }
        public void setPid(int pid) {
            this.pid = pid;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        @Override
        public String toString(){
            return String.valueOf(id)+"--"+String.valueOf(pid)+":"+name;
        }
    }

    public static void main(String[] args) {
        List<Integer> nodeList = new ArrayList<Integer>();
        nodeList.add(223);
        nodeList.add(222);
        nodeList.add(221);
        nodeList.add(22);
        nodeList.add(1111);
        nodeList.add(111);
        nodeList.add(11);
        nodeList.add(21);
        nodeList.add(2);
        nodeList.add(1);

        System.out.println( CollectionsOpt.srotAsTreeAndToJSON(nodeList,
                new CollectionsOpt.ParentChild<Integer>() {
                    public boolean parentAndChild(Integer p, Integer c) {
                        return p ==( c / 10);
                    }
                }, "c"));
    }

}
