/*
 * Copyright 2013 Phil Brown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package self.philbrown.droidMail;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Send and receive mail without using Android intent. Closely follows Model-Controller-Store framework
 * @author Phil Brown
 */
public class Mailer extends javax.mail.Authenticator
{
	/** logging tag */
	public static final String TAG = "Mailer";
	
	/** used to allow the addition of attachments to the email message */
	private Multipart multipart;
	
	/** if set to true, mail operations will log verbosely */
	private boolean DEBUG;
	
	/** The {@link MailConfiguration} for which this {@code Mailer} can send and receive messages */
	private MailConfiguration config;
	
	/** The password associated with {@link #config} */
	private String password;
	
	private MailListener listener;
	
	/**
	 * specifies how to get mail
	 */
	public static enum Protocol
	{
		POP3, 
		IMAP
	}
	
	/**
	 * Constructor: Creates a new {@code Mailer} with the given configuration. The password must be the same password used when constructing
	 * the {@link MailConfiguration}. To avoid hardcoding the password, or passing it around as a public or protected variable, 
	 * use {@code MailConfiguration.createMailer(Context)} or {@code MailConfigStore.createMailer(Context, String)}.
	 * @param context used to access the store
	 * @param config the configuration that provides the source account information
	 * @param password the password associated with {@code config}
	 * @see MailConfiguration#createMailer(Context)
	 * @see MailConfigStore#createMailer(Context, String)
	 */
	public Mailer(Context context, MailConfiguration config, String password)
	{
		if (context == null)
			throw new NullPointerException("Cannot create new Mailer with null context!");
		
		if (config == null)
			throw new NullPointerException("Cannot create new Mailer with null configuration!");
		
		this.config = config;
		this.password = password;
		
		multipart = new MimeMultipart(); 
		 
	    // There is something wrong with MailCap, javamail can not find a handler for the multipart/mixed part, so this bit needs to be added. 
	    MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap(); 
	    mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html"); 
	    mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml"); 
	    mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain"); 
	    mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed"); 
	    mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822"); 
	    CommandMap.setDefaultCommandMap(mc);
	}
	
	public void setMailListener(MailListener listener)
	{
		this.listener = listener;
	}
	
	@Override 
	public PasswordAuthentication getPasswordAuthentication() { 
	    return new PasswordAuthentication(config.getUsername(), password);
	}
	
	/**
	 * Send a message to a single recipient
	 * @param destinationAddress the email address to which to send the message
	 * @param subject the email subject
	 * @param message the email body
	 * @param attachment the file to attach, if any
	 */
	public void send(String destinationAddress, String subject, String message, File attachment)
	{
		send(new String[]{destinationAddress}, subject, message, attachment);
	}
	
	/**
	 * Send a message to the given recipients
	 * @param destinationAddress the email address to which to send this email
	 * @param subject the email subject (optional)
	 * @param message the email body
	 * @param attachment an attachment to send (optional)
	 * @return
	 */
	public void send(String[] destinationAddresses, String subject, String message, File attachment)
	{
		if (destinationAddresses.length == 0)
			return;
		try
		{
			
			Properties props = new Properties(); 
			props.put("mail.smtp.host", config.host_smtp_server); 
			props.put("mail.debug", DEBUG); 
			props.put("mail.smtp.auth", config.smtp_auth); 
			props.put("mail.smtp.port", config.smtp_port); 
			props.put("mail.smtp.socketFactory.port", config.socket_port); 
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); 
			props.put("mail.smtp.socketFactory.fallback", "false"); 
			
			Session session = Session.getInstance(props, this); 
	 
			MimeMessage msg = new MimeMessage(session); 
 
			msg.setFrom(new InternetAddress(config.getAddress())); 
       
			InternetAddress[] recipients = new InternetAddress[destinationAddresses.length]; 
			for (int i = 0; i < destinationAddresses.length; i++) { 
				recipients[i] = new InternetAddress(destinationAddresses[i]); 
			} 
			msg.setRecipients(MimeMessage.RecipientType.TO, recipients); 
			
			if (subject != null)
				msg.setSubject(subject); 
			
			msg.setSentDate(new Date()); 
      
			// setup message body 
			BodyPart messageBodyPart = new MimeBodyPart(); 
			messageBodyPart.setText(message); 
			multipart.addBodyPart(messageBodyPart); 
			
			if (attachment != null)
			{
				try
				{
					BodyPart attachmentBodyPart = new MimeBodyPart(); 
					DataSource source = new FileDataSource(attachment); 
					attachmentBodyPart.setDataHandler(new DataHandler(source)); 
					attachmentBodyPart.setFileName(attachment.getName()); 
			 
					multipart.addBodyPart(attachmentBodyPart); 
				} catch (Throwable t)
				{
					t.printStackTrace();
					//Could not attach file
				}
			}
 
			// Put parts in message 
			msg.setContent(multipart); 
  
			// send email 
			Transport transport = session.getTransport("smtps");
			Transporter t = new Transporter(transport);
			t.execute(msg);
			if (listener != null)
				listener.onSuccess(this);
		} 
		catch (Throwable t)
		{
			t.printStackTrace();
			if (listener != null)
				listener.onError(this);
		}
		if (listener != null)
			listener.onComplete(this);
	}
	
	/**
	 * set {@link #DEBUG} to true
	 */
	public void enableDebugMode()
	{
		DEBUG = true;
	}
	
	/**
	 * set {@link #DEBUG} to false
	 */
	public void disableDebugMode()
	{
		DEBUG = false;
	}
	
	/**
	 * Sends an email message in a background thread
	 * @author Phil Brown
	 */
	class Transporter extends AsyncTask<MimeMessage, Void, Void>
	{
		/** Used to send the message */
		protected Transport transport;
		
		/**
		 * Constructor
		 * @param transport the {@link Transport} to use for sending the message(s)
		 */
		public Transporter(Transport transport)
		{
			this.transport = transport;
		}
		
		/**
		 * Sends the given messages on a background thread.<br>
		 */
		@Override
		protected Void doInBackground(MimeMessage... msgs) {
			
			for (MimeMessage m : msgs)
			{
				try {
					transport.connect(config.host_smtp_server, config.getUsername(), password);
					transport.sendMessage(m, m.getAllRecipients());
					transport.close();
					//Transport.send(m);
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
		
	}
	
	/**
	 * FIXME this is untested. It will also FAIL unless run in an AsyncTask, like the send method above.
	 * Retrieves messages from the given account's server
	 * @param protocol specifies whether IMAPE or POP3 should be used. If <em>null</em>, POP3 is used
	 * @param start starting range of messages to get. Defaults to 0.
	 * @param stop stopping range of messages to receive. Defaults to 0. If both {@code start} and {@code stop}
	 * are set to {@code zero}, all messages will be retrieved.
	 * 
	 * @return the retrieved messages, or null if there was an error.
	 */
	public Message[] getMessages(Protocol protocol, int start, int stop)
	{
		if (start < 0 || stop < 0)
			return null;
		String p = "pop3";
		if (protocol != null)
		{
			switch(protocol)
			{
			case IMAP : p = "imap";
						break;
			case POP3 : p = "pop3";
						break;
			}
		}
		
		Properties props = new Properties();  
		
		Session session = Session.getInstance(props, this); 
		
		Store store = null;
		
		try {
			store = session.getStore(p);
			store.connect(config.host_smtp_server, config.getAddress(), password);
			Folder f = store.getDefaultFolder();
			f.open(Folder.READ_ONLY);
			
			if (start == 0)
			{
				if (stop != 0)
				{
					return f.getMessages(0, stop);
				}
				else
				{
					return f.getMessages();
				}
			}
			else
			{
				if (stop != 0)
				{
					return f.getMessages(start, stop);
				}
				else
				{
					int[] array = new int[stop - start];
					for (int i = 0; i < array.length; i++)
					{
						array[i] = i;
					}
					return f.getMessages(array);
				}
			}
			
		} catch (Throwable t) {
			Log.w(TAG, "Could not complete request", t);
			return null;
		}
	}
	
	public interface MailListener 
	{
		public void onSuccess(Mailer m);
		public void onError(Mailer m);
		public void onComplete(Mailer m);
	}
}
