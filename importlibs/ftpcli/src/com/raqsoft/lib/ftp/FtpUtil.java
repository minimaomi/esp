package com.raqsoft.lib.ftp;

import java.io.File;  
import java.io.FileNotFoundException;  
import java.io.FileOutputStream;  
import java.io.IOException;  
import java.io.OutputStream;  
import java.net.SocketException;  
import java.util.Properties;  
  
import org.apache.commons.logging.Log;  
import org.apache.commons.logging.LogFactory;  
import org.apache.commons.net.ftp.FTPClient;  
import org.apache.commons.net.ftp.FTPReply;  
  
import com.jcraft.jsch.Channel;  
import com.jcraft.jsch.ChannelSftp;  
import com.jcraft.jsch.JSch;  
import com.jcraft.jsch.JSchException;  
import com.jcraft.jsch.Session;  
  
public class FtpUtil {  
  
    private final static Log logger = LogFactory.getLog(FtpUtil.class);  
  
      
    /* 
     * ��SFTP�����������ļ� 
     *  
     * @param ftpHost SFTP IP��ַ 
     *  
     * @param ftpUserName SFTP �û��� 
     *  
     * @param ftpPassword SFTP�û������� 
     *  
     * @param ftpPort SFTP�˿� 
     *  
     * @param ftpPath SFTP���������ļ�����·�� ��ʽ�� ftptest/aa 
     *  
     * @param localPath ���ص����ص�λ�� ��ʽ��H:/download 
     *  
     * @param fileName �ļ����� 
     */  
    public static void downloadSftpFile(String ftpHost, String ftpUserName,  
            String ftpPassword, int ftpPort, String ftpPath, String localPath,  
            String fileName) throws JSchException {  
        Session session = null;  
        Channel channel = null;  
  
        JSch jsch = new JSch();  
        session = jsch.getSession(ftpUserName, ftpHost, ftpPort);  
        session.setPassword(ftpPassword);
        session.setTimeout(100000);  
        Properties config = new Properties();  
        config.put("StrictHostKeyChecking", "no");  
        session.setConfig(config);  
        session.connect();  
  
        channel = session.openChannel("sftp");  
        channel.connect();  
        ChannelSftp chSftp = (ChannelSftp) channel;  
  
        String ftpFilePath = ftpPath + "/" + fileName;  
        String localFilePath = localPath + File.separatorChar + fileName;  
  
        try {  
            chSftp.get(ftpFilePath, localPath);  
        } catch (Exception e) {  
            e.printStackTrace();  
            logger.info("download error.");  
        } finally {  
            chSftp.quit();  
            channel.disconnect();  
            session.disconnect();  
        }  
  
    }  
	public static void main(String args[]) {
		try {
			File f = new File("d:/ftp/aa.txt");
			System.out.println(f.getAbsolutePath());
			f = new File("D:/ftp/aa.txt");
			System.out.println(f.getAbsolutePath());
			f = new File("D:\\ftp\\aa.txt");
			System.out.println(f.getAbsolutePath());
			//FtpUtil.downloadSftpFile("123.57.218.190", "root", "Carrygame888", 22, "/var/log/", "d:/test/ftp1.txt", "secure");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}  