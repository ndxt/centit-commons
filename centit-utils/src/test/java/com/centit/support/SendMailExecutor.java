package com.centit.support;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

@SuppressWarnings("unused")
public abstract class SendMailExecutor {

    protected static final Logger logger = LoggerFactory.getLogger(SendMailExecutor.class);
    public static String mailHost = "";
    public static String mailUser = "";
    public static String mailPassword = "";
    public static int smtpPort = 25;
    private SendMailExecutor() {
        throw new IllegalAccessError("Utility class");
    }

    public static void setMailServer(String mailHost, String mailUser, String mailPassword) {
        SendMailExecutor.mailHost = mailHost;
        SendMailExecutor.mailUser = mailUser;
        SendMailExecutor.mailPassword = mailPassword;
    }

    public static void setMailServer(String mailHost, String mailUser, String mailPassword, int smtpPort) {
        SendMailExecutor.mailHost = mailHost;
        SendMailExecutor.mailUser = mailUser;
        SendMailExecutor.mailPassword = mailPassword;
        SendMailExecutor.smtpPort = smtpPort;
    }


    public static boolean sendEmail(String[] mailTo, String mailFrom,
                                    String msgSubject, String msgContent) {

        MultiPartEmail multMail = new MultiPartEmail();
        // SMTP
        multMail.setHostName(mailHost);
        multMail.setSmtpPort(smtpPort);
        // 需要提供公用的邮件用户名和密码
        multMail.setAuthentication(
            mailUser,
            mailPassword);
        try {
            //multMail.setFrom(CodeRepositoryUtil.getRight("SysMail", "admin_email"));
            multMail.setFrom(mailFrom);
            multMail.addTo(mailTo);
            multMail.setSubject(msgSubject);
            msgContent = msgContent.trim();
            if (msgContent.endsWith("</html>") || msgContent.endsWith("</HTML>")) {
                multMail.addPart(msgContent, "text/html;charset=utf-8");
            } else {
                multMail.setMsg(msgContent);
            }
            multMail.send();
            return true;
        } catch (EmailException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
        }
        return false;
    }

    public static boolean sendEmail(String mailTo, String mailFrom,
                                    String msgSubject, String msgContent) {
        return sendEmail(new String[]{mailTo}, mailFrom,
            msgSubject, msgContent);
    }

    public static boolean sendEmail(String[] mailTo, String mailFrom,
                                    String msgSubject, String msgContent, List<File> annexs) {

        MultiPartEmail multMail = new MultiPartEmail();
        // SMTP
        multMail.setHostName(mailHost);
        multMail.setSmtpPort(smtpPort);
        // 需要提供公用的邮件用户名和密码
        multMail.setAuthentication(
            mailUser,
            mailPassword);
        try {
            //multMail.setFrom(CodeRepositoryUtil.getRight("SysMail", "admin_email"));
            multMail.setFrom(mailFrom);
            multMail.addTo(mailTo);
            multMail.setSubject(msgSubject);
            multMail.setMsg(msgContent);

            for (File attachment : annexs) {
                multMail.attach(attachment);
            }

            multMail.send();

            return true;
        } catch (EmailException e) {
            logger.error(e.getMessage(), e);//e.printStackTrace();
        }
        return false;
    }

    public static boolean sendEmail(String mailTo, String mailFrom,
                                    String msgSubject, String msgContent, List<File> annexs) {
        return sendEmail(new String[]{mailTo}, mailFrom,
            msgSubject, msgContent, annexs);
    }

}
