package com.centit.support.network;

/**
 * @author 范春峰
 * @author codefan
 */
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Sequence;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.UidGenerator;
@SuppressWarnings("unused")
public class OutlookMeetingUtils {

	protected static final Logger logger = LoggerFactory.getLogger(OutlookMeetingUtils.class);
	public static String mailHost = "mail.centit.com";
	public static String mailUser = "accounts@centit.com";
	public static String mailPassword = "yhs@yhs1";
	public static int smtpPort = 25;

	public static void setOutlookServer(String mailHost, String mailUser, String mailPassword) {
		OutlookMeetingUtils.mailHost = mailHost;
		OutlookMeetingUtils.mailUser = mailUser;
		OutlookMeetingUtils.mailPassword = mailPassword;
	}

	public static void setOutlookServer(String mailHost, String mailUser, String mailPassword, int smtpPort) {
		OutlookMeetingUtils.mailHost = mailHost;
		OutlookMeetingUtils.mailUser = mailUser;
		OutlookMeetingUtils.mailPassword = mailPassword;
		OutlookMeetingUtils.smtpPort = smtpPort;
	}

	/***
	 * 通过smtp协议发送邮件的方法
	 * 
	 * @param content:邮件内容
	 * @param subject:主题
	 * @param sendMail:发送邮件地址
	 * @param password:发送邮件的登陆密码
	 * @param receiveMail:接收邮件的地址,可以包含多个地址,
	 * @param mailHost:发送邮件的smtp服务器
	 * @param port:端口号
	 * @throws MessagingException 异常
	 * @throws IOException 异常
	 **/

	public static void sendTextEmail(String content, String subject, final String sendMail, final String password,
			List<String> receiveMail, String mailHost, int port)
			throws MessagingException, IOException {
		Properties props = new Properties();
		props.setProperty("mail.smtp.auth", "true");
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", mailHost);
		props.setProperty("mail.smtp.port", String.valueOf(port));// 获取系统环境
		Session session = Session.getInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(sendMail, password);
			}
		});
		session.setDebug(true); // 调试信息,打印发送过程,实际中不需要
		Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(sendMail));
		msg.setSubject(subject);
		// Address[] tos = null;
		// 邮件群发功能,添加多个接受地址,有些邮件服务器可能会屏蔽
		/*
		 * if(receiveMail!=null&&!"".equals(receiveMail)){ String[] receivers =
		 * receiveMail.split(";"); if (receivers != null){ // 为每个邮件接收者创建一个地址 tos
		 * = new InternetAddress[receivers.length]; for (int i=0;
		 * i<receivers.length; i++){ tos[i] = new InternetAddress(receivers[i]);
		 * } } }
		 */
		if (receiveMail != null) {
			for (int i = 0; i < receiveMail.size(); i++) {
				InternetAddress toAddr = new InternetAddress(receiveMail.get(i));
				msg.addRecipient(Message.RecipientType.TO, toAddr);
			}
		}

		// msg.setRecipients(RecipientType.TO,
		// InternetAddress.parse(receiveMail));//一个地址时

		BodyPart messageBodyPart = new MimeBodyPart();
		// 测试下来如果不这么转换的话，会以纯文本的形式发送过去，
		// 如果没有method=REQUEST;charset=\"UTF-8\"，outlook会议附件的形式存在，而不是直接打开就是一个会议请求
		messageBodyPart.setDataHandler(
				new DataHandler(new ByteArrayDataSource(content, "text/calendar;method=REQUEST;charset=\"UTF-8\"")));
		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart);
		msg.setContent(multipart);

		Transport.send(msg);

	}

	public static void sendTextEmail(String content, String subject, List<String> receiveMail)
			throws AddressException, MessagingException, IOException {
		sendTextEmail(content, subject, mailUser, mailPassword, receiveMail, mailHost, smtpPort);
	}

	public static DateTime utilDateToIcalDatetime(Date date) {
		return new DateTime(date);
	}

	/**
	 * 将会议邀请信息生成标准格式的字符串
	 * 
	 * @param uid
	 *            会议唯一标示,uid == null 为新增,uid != null 为修改
	 * @param startTime
	 *            开始时间 格式yyyy-MM-dd-HH-mm-ss
	 * @param endTime
	 *            结束时间
	 * @param title
	 *            会议名称
	 * @param organizerName
	 *            组织者名称
	 * @param organizerEmail
	 *            组织者Email
	 * @param location
	 *            地点
	 * @param description
	 *            描述
	 * @param participators Pair<姓名 ，email地址>
	 *            参与者名称列表，
	 * @Return 文本
	 * @throws IOException 异常
	 */
	private static String makeICalMeetingText(Uid uid, Date startTime, Date endTime, String title,
			String organizerName, String organizerEmail, String location, String description,
			List<Pair<String, String>> participators) throws IOException {
		// 创建事件
		DateTime start = utilDateToIcalDatetime(startTime);
		DateTime end = utilDateToIcalDatetime(endTime);
		String eventTitle = "";
		if (StringUtils.isNotBlank(title)) {
			eventTitle = title;
		}
		VEvent meeting = new VEvent(start, end, eventTitle);
		// 添加时区信息
		// meeting.getProperties().add(tz.getTimeZoneId());
		// 创建日历
		Calendar icsCalendar = new Calendar();
		icsCalendar.getProperties().add(new ProdId("-//Events Calendar//iCal4j 1.0//EN"));
		icsCalendar.getProperties().add(Version.VERSION_2_0);
		// icsCalendar.getProperties().add(Method.REQUEST);
		icsCalendar.getProperties().add(CalScale.GREGORIAN);
		icsCalendar.getProperties().add(Method.REQUEST);


		meeting.getProperties().add(uid);
		// 组织者
		Organizer organizer = new Organizer(URI.create("mailto:" + organizerEmail));
		organizer.getParameters().add(new Cn(organizerName));
		meeting.getProperties().add(organizer);
		if (StringUtils.isNotBlank(location)) {
			meeting.getProperties().add(new Location(location));
		}
		if (StringUtils.isNotBlank(description)) {
			meeting.getProperties().add(new Description(description));
		}
		// 添加参加者 .
		for (Pair<String, String> p : participators) {
			Attendee dev = new Attendee(URI.create("mailto:" + p.getRight()));
			dev.getParameters().add(Role.REQ_PARTICIPANT);
			dev.getParameters().add(new Cn(p.getLeft()));
			meeting.getProperties().add(dev);
		}

		// 提醒,提前10分钟
		/*
		 * VAlarm valarm = new VAlarm(new Dur(0, 0, -10, 0));
		 * valarm.getProperties().add(new Repeat(1));
		 * valarm.getProperties().add(new Duration(new Dur(0, 0, 10,
		 * 0))); //提醒窗口显示的文字信息 valarm.getProperties().add(new
		 * Summary("Event Alarm"));
		 * valarm.getProperties().add(Action.DISPLAY);
		 * valarm.getProperties().add(new Description("会议将在10分钟后 开始"));
		 * meeting.getAlarms().add(valarm);//将VAlarm加入VEvent
		 * //从2016-3-17开始，每个一周开一次会议，持续4次 Recur recur = new
		 * Recur(Recur.WEEKLY, 4); recur.setInterval(2); RRule rule =
		 * new RRule(recur); meeting.getProperties().add(rule);
		 */
		// 添加事件
		icsCalendar.getComponents().add(meeting);
		CalendarOutputter outputter = new CalendarOutputter();
		Writer writer = new StringWriter();
		outputter.output(icsCalendar, writer);
		return writer.toString();

	}


	/*	 *
	 * 删除已预订过的会议
	 */
	private static String makeICalCancelMeetingText(Uid uid, 
			Date startTime, Date endTime, String title, String organizerName,
			String organizerEmail, String location, String description) throws IOException {

		// 创建一个时区（TimeZone)
		// 创建事件
		DateTime start = utilDateToIcalDatetime(startTime);
		DateTime end = utilDateToIcalDatetime(endTime);
		VEvent meeting = new VEvent(start, end, "delete");
		// VEvent meeting = new VEvent(start,"delete");
		// 添加时区信息
		// meeting.getProperties().add(tz.getTimeZoneId());

		// 创建日历
		Calendar icsCalendar = new Calendar();
		icsCalendar.getProperties().add(new ProdId("-//Events Calendar//iCal4j 1.0//EN"));
		icsCalendar.getProperties().add(Version.VERSION_2_0);
		icsCalendar.getProperties().add(CalScale.GREGORIAN);

		// 删除
		icsCalendar.getProperties().add(Method.CANCEL);
		meeting.getProperties().add(new Sequence());
		meeting.getProperties().add(uid);
		// 组织者
		Organizer organizer = new Organizer(URI.create("mailto:" + organizerEmail));
		organizer.getParameters().add(new Cn(organizerName));
		meeting.getProperties().add(organizer);
		// meeting.getProperties().add(new Organizer());
		if (StringUtils.isNotBlank(location)) {
			meeting.getProperties().add(new Location(location));
		}
		if (StringUtils.isNotBlank(description)) {
			meeting.getProperties().add(new Description(description));
		}
	
		icsCalendar.getComponents().add(meeting);
		CalendarOutputter outputter = new CalendarOutputter();
		Writer writer = new StringWriter();
		outputter.output(icsCalendar, writer);
		return writer.toString();
	}
	
	/**
	 * 新增
	 * 
	 * @param startTime
	 *            开始时间 格式yyyy-MM-dd-HH-mm-ss
	 * @param endTime
	 *            结束时间
	 * @param title
	 *            会议名称
	 * @param organizerName
	 *            组织者名称
	 * @param organizerEmail
	 *            组织者Email
	 * @param location
	 *            地点
	 * @param description
	 *            描述
	 * @param participators Pair 姓名 ，email地址
	 *            参与者名称列表，
	 * @return String 返回 会议id  长度 60
	 * @throws MessagingException 异常
	 * @throws IOException 异常
	 */
	public static String createMeeting(Date startTime, Date endTime, String title, String organizerName,
			String organizerEmail, String location, String description, List<Pair<String, String>> participators)
			throws MessagingException, IOException {
		UidGenerator ug = new UidGenerator("uidGen");
		Uid uid = ug.generateUid();
		String content = makeICalMeetingText(uid, startTime, endTime, title, organizerName, organizerEmail,
				location, description, participators);
		List<String> receiveMail = new ArrayList<String>();
		for (Pair<String, String> p : participators) {
			receiveMail.add(p.getRight());
		}
		sendTextEmail(content, title, receiveMail);
		return uid.toString();
	}

	/**
	 * 更新已预订过的会议
	 * 
	 * @param uidStr
	 *            会议ID
	 * @param startTime
	 *            开始时间 格式yyyy-MM-dd-HH-mm-ss
	 * @param endTime
	 *            结束时间
	 * @param title
	 *            会议名称
	 * @param organizerName
	 *            组织者名称
	 * @param organizerEmail
	 *            组织者Email
	 * @param location
	 *            地点
	 * @param description 
	 *            会议内容描述
	 * @param participators Pair 姓名 ，email地址
	 *            参与者名称列表，
	 * @throws MessagingException 异常
	 * @throws IOException 异常
	 */
	public static void updateMeeting(String uidStr, Date startTime, Date endTime, String title, String organizerName,
			String organizerEmail, String location, String description, List<Pair<String, String>> participators)
			throws MessagingException, IOException {
		Uid uid = new Uid(uidStr);
		String content = makeICalMeetingText(uid, startTime, endTime, title, organizerName, organizerEmail,
				location, description, participators);
		List<String> receiveMail = new ArrayList<String>();
		for (Pair<String, String> p : participators) {
			receiveMail.add(p.getRight());
		}
		sendTextEmail(content, title, receiveMail);
		//return uid.toString();
	}

	/**
	 * 
	 * @param uidStr
	 *            会议ID
	 * @param startTime
	 *            开始时间 格式yyyy-MM-dd-HH-mm-ss
	 * @param endTime
	 *            结束时间
	 * @param title
	 *            会议名称
	 * @param organizerName
	 *            组织者名称
	 * @param organizerEmail
	 *            组织者Email
	 * @param location
	 *            地点
	 * @param description 
	 *            会议内容描述
	 * @param participators Pair 姓名 ，email地址
	 *            参与者名称列表，
	 * @throws MessagingException 异常
	 * @throws IOException 异常
	 */
	public static void deleteMeeting(String uidStr, Date startTime, Date endTime, String title, String organizerName,
			String organizerEmail, String location, String description,List<Pair<String, String>> participators)
			throws  MessagingException, IOException {
		Uid uid = new Uid(uidStr);
		String content = makeICalCancelMeetingText(uid, startTime, endTime, title, organizerName, organizerEmail,
				location,description);
		List<String> receiveMail = new ArrayList<String>();
		for (Pair<String, String> p : participators) {
			receiveMail.add(p.getRight());
		}
		sendTextEmail(content, title, receiveMail);
	}
}
