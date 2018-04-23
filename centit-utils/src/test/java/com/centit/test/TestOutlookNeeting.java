package com.centit.test;

import com.centit.support.network.OutlookMeetingUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestOutlookNeeting {
     /**
    *
    * 通过对比，不难发现发送更新，其实日历头结构只需要改变DTSTART（开始时间）和DTEND（结束时间）到新的时间点就行 唯一需要注意的是要保持UID一致。
    * 取消约会稍有不同的是：METHOD:CANCEL 取消的时候METHOD属性需要修改为 CANCEL,操作代码如下：
    * icsCalendar.getProperties().add(Method.CANCEL);
    * 同样需要注意的是UID必须要和之前的邀请约会日历头保持一致
    * @param args
    * @throws Exception
    */
   public static void main(String[] args) throws Exception {
       testDelete();
       //testUpdate("20160620T111107Z-uidGen@fe80:0:0:0:510b:a9b8:b8ab:d716%4");
//       testUpdate("20160429T042917Z-uidGen@fe80:0:0:0:9c7c:67ff:fe46:f13f%8");
       //deleteTask("20160620T111107Z-uidGen@fe80:0:0:0:510b:a9b8:b8ab:d716%4");
       //20160429T055513Z-uidGen@fe80:0:0:0:9c7c:67ff:fe46:f13f%8
//       deleteTask("20160429T055513Z-uidGen@fe80:0:0:0:9c7c:67ff:fe46:f13f%8");
//       deleteTask("20160429T055618Z-uidGen@fe80:0:0:0:9c7c:67ff:fe46:f13f%8");
   }
   public static void testNew() throws ParseException, AddressException, MessagingException, IOException {
       SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
       Date startTime = sdf.parse("2017-05-15 13:35:00.123");
       Date endTime = sdf.parse("2017-05-15 15:45:00.123");
       String title = "Outlook 日历测试";
       String organizerName = "范春锋";
       String organizerEmail = "fan_cf@centit.com";
       String location = "复星大厦11楼VIP会议室";
       String description = "测试下日历怎么样，好像不行,什么情况";
       List<Pair<String,String>> participators = new ArrayList<>();
       participators.add(new MutablePair<String,String>( "杨淮生","codefan@centit.com"));  
       participators.add(new MutablePair<String,String>( "范春锋","fan_cf@centit.com"));  
       
       OutlookMeetingUtils.setOutlookServer("mail.centit.com","fan_cf@centit.com", "BSD@2008redhat");
       
       String uidString =  OutlookMeetingUtils.createMeeting(
               startTime, endTime, title, organizerName, organizerEmail, location, description,participators);

       location = "南大先腾会议室";
       description = "会议室被占用，换一个会议室";
       
       OutlookMeetingUtils.updateMeeting(uidString,
               startTime, endTime, title, organizerName, organizerEmail, location, description, participators);
     
       System.out.println(uidString);
       
   }
   
   
   public static void testDelete() throws ParseException, AddressException, MessagingException, IOException {
       SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
       Date startTime = sdf.parse("2017-05-15 13:35:00.123");
       Date endTime = sdf.parse("2017-05-15 15:45:00.123");
       String title = "Outlook 日历测试";
       String organizerName = "范春锋";
       String organizerEmail = "fan_cf@centit.com";
       String location = "会议取消";
       String description = "会议取消";
       List<Pair<String,String>> participators = new ArrayList<>();
       participators.add(new MutablePair<String,String>( "杨淮生","codefan@centit.com"));  
       participators.add(new MutablePair<String,String>( "范春锋","fan_cf@centit.com")); 

       OutlookMeetingUtils.setOutlookServer("mail.centit.com","fan_cf@centit.com", "BSD@2008redhat");
       
       OutlookMeetingUtils.deleteMeeting("UID:20170512T102523Z-uidGen@fe80:0:0:0:cdc0:b539:4f9b:dc5b%2",
               startTime, endTime, title, organizerName, organizerEmail, location, description, participators);
   }
   
}
