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

import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.BASE64EncoderStream;

/**
 * Defines an e-mail account configuration for sending a receiving 
 * e-mails without using the built-in Android mail service.
 * @author Phil Brown
 */
public class MailConfiguration implements Parcelable
{
	/** the host server */
	public String host_smtp_server;
	/** the host port */
	public int smtp_port;
	/** whether or not to authenticate on SMTP (SSL/TLS) */
	public boolean smtp_auth;
	
	/** server for receiving mail via the POP3 protocol */
	public String popServer;
	/** port for getting mail via POP3 */
	public int popPort;
	/** whether or not authentication is required for getting mail via POP3 */
	public boolean popAuth;
	
	/** server for receiving mail via the IMAP protocol */
	public String imapServer;
	/** port for getting mail via IMAP */
	public int imapPort;
	/** whether or not authentication is required for getting mail via IMAP */
	public boolean imapAuth;
	
	/** the {@code SocketFactory} port. Defaults to 465. */
	public int socket_port;

	/** the username for this account */
	private String username;
	/** the password for this account */
	private String password;
	/** the email address for this account */
	private String emailAddress;
	
	/**
	 * Used to lookup partial configurations for common providers and protocols
	 */
	protected static Map<Provider, MailConfiguration> commonProviders = new HashMap<Provider, MailConfiguration>();
	static
	{
		//gmail
		MailConfiguration google = new MailConfiguration();
		google.host_smtp_server = "smtp.gmail.com";
		google.smtp_port = 456;
		google.smtp_auth = true;
		google.popServer = "pop.gmail.com";
		google.popAuth = true;
		google.popPort = 995;
		google.imapServer = "imap.gmail.com";
		google.imapAuth = true;
		google.imapPort = 993;
		
		commonProviders.put(Provider.gmail, google);
		
		//yahoo
		MailConfiguration yahoo = new MailConfiguration();
		yahoo.host_smtp_server = "smtp.mail.yahoo.com";
		yahoo.smtp_port = 465;
		yahoo.smtp_auth = true;
		yahoo.popServer = "plus.pop.email.yahoo.com";
		yahoo.popAuth = true;
		yahoo.popPort = 995;
		yahoo.imapServer = "imap.mail.yahoo.com";
		yahoo.imapAuth = true;
		yahoo.imapPort = 993;
		
		commonProviders.put(Provider.yahoo, yahoo);
		
		//aol
		MailConfiguration aol = new MailConfiguration();
		aol.host_smtp_server = "smtp.aol.com";
		aol.smtp_port = 587;
		aol.smtp_auth = true;
		aol.popServer = "pop.aol.com";
		aol.popAuth = true;
		aol.popPort = 995;
		aol.imapServer = "imap.aol.com";
		aol.imapAuth = true;
		aol.imapPort = 993;
		
		commonProviders.put(Provider.aol, aol);
		
		//hotmail
		MailConfiguration hotmail = new MailConfiguration();
		hotmail.host_smtp_server = "smtp.live.com";
		hotmail.smtp_auth = true;
		hotmail.smtp_port = 587;
		hotmail.popServer = "pop3.live.com";
		hotmail.popAuth = true;
		hotmail.popPort = 995;
		//hotmail does not support imap
		
		commonProviders.put(Provider.hotmail, hotmail);
	}
	
	/** Common <em>.com</em> mail providers for which most configuration settings can be automatically set up */
	public static enum Provider
	{
		/** example@gmail.com */
		gmail,
		/** example@yahoo.com */
		yahoo,
		/** example@aol.com */
		aol,
		/** example@hotmail.com */
		hotmail
	}
	
	/** This CREATOR is used to parcel this Object. */
	public static final Parcelable.Creator<MailConfiguration> CREATOR =
        new Parcelable.Creator<MailConfiguration>() {
     
		/** 
		 * Construct and return an Ad from a Parcel<br>
		 */
		public MailConfiguration createFromParcel(Parcel in) {
			return new MailConfiguration(in);
		}//createFromParcel

		/**
		 * Creates a new array of Adds<br>
		 */
		public MailConfiguration[] newArray(int size) {
			return new MailConfiguration[size];
		}//newArray
	};
	
	/**
	 * Default constructor. Initializes all values. 
	 * Strings become "", ints become 0, booleans become false, 
	 * and {@link #socket_port} becomes 465.<p>
	 * This constructor should only be used to pass information to 
	 * {@link #MailConfiguration(String, String, String, MailConfiguration)}, since there is no
	 * way to publicly set the password without one of the other constructors.
	 * @see #MailConfiguration(String, String, String, Provider)
	 */
	public MailConfiguration()
	{
		socket_port = 465;
		emailAddress = "";
		username = "";
		password = "";
		host_smtp_server = "";
		smtp_port = 0;
		smtp_auth = false;
		popServer = "";
		popPort = 0;
		popAuth = false;
		imapServer = "";
		imapPort = 0;
		imapAuth = false;
	}
	
	/**
	 * Constructor<p>Creates a new {@code MailConfiguration} Object with the given email address, username, and password, 
	 * that is preconfigured for the given provider
	 * @param emailAddress host email address
	 * @param username account username
	 * @param password account password
	 * @param provider common provider
	 * @throws NullPointerException if any of the given parameters are <em>null</em>
	 * @throws InvalidKeyException if the given provider is invalid
	 */
	public MailConfiguration(String emailAddress, String username, String password, Provider provider) throws InvalidKeyException
	{
		this();
		if (emailAddress == null || username == null || password == null || provider == null)
		{
			throw new NullPointerException("Invalid parameters");
		}
		if (commonProviders.containsKey(provider))
		{
			MailConfiguration conf = commonProviders.get(provider);
			host_smtp_server = conf.host_smtp_server;
			smtp_port = conf.smtp_port;
			smtp_auth = conf.smtp_auth;
			popServer = conf.popServer;
			popAuth = conf.popAuth;
			popPort = conf.popPort;
			imapServer = conf.imapServer;
			imapAuth = conf.imapAuth;
			imapPort = conf.imapPort;
		}
		else
		{
			throw new InvalidKeyException("Unknown Provider");
		}
		this.emailAddress = emailAddress;
		this.username = username;
		this.password = password;
		
	}
	
	/**
	 * Constructor<p>Creates a new {@code MailConfiguration} with the given email address, username, password, and configuration.
	 * This configuration can be created using the default constructor {@link #MailConfiguration()}.
	 * @param emailAddress the address for this account
	 * @param username the username for this account
	 * @param password the password for this account
	 * @param conf the configuration information
	 * @throws NullPointerException if the given {@code password} is <em>null</em> or {@code conf} is <em>null</em>, or if 
	 * either {@code emailAddress} is <em>null</em> AND {@code conf} does not contain an email address, OR if {@code username}
	 * is <em>null</em> AND {@code conf} does not contain a username.
	 */
	public MailConfiguration(String emailAddress, String username, String password, MailConfiguration conf)
	{
		this();
		if (!(conf != null 
				&& (emailAddress != null || conf.getAddress() != null) //allows conf to contain the address
				&& (username != null || conf.getUsername() != null) //allows conf to contain the username
				&& password != null))
		{
			throw new NullPointerException("Invalid parameters");
		}
		this.emailAddress = emailAddress;
		this.username = username;
		this.password = password;
		host_smtp_server = conf.host_smtp_server;
		smtp_port = conf.smtp_port;
		smtp_auth = conf.smtp_auth;
		popServer = conf.popServer;
		popAuth = conf.popAuth;
		popPort = conf.popPort;
		imapServer = conf.imapServer;
		imapAuth = conf.imapAuth;
		imapPort = conf.imapPort;
	}
	
	/**
	 * Inflates a {@code MailConfiguration} from a {@link Parcel}
	 * @param in the {@code Parcel} to unpack
	 */
	public MailConfiguration(Parcel in)
	{
		this();
		emailAddress = in.readString();
		username = in.readString();
		password = in.readString();
		host_smtp_server = in.readString();
		smtp_port = in.readInt();
		smtp_auth = (in.readInt() == 1 ? true : false);
		popServer = in.readString();
		popPort = in.readInt();
		popAuth = (in.readInt() == 1 ? true : false);
		imapServer = in.readString();
		imapPort = in.readInt();
		imapAuth = (in.readInt() == 1 ? true : false);
		socket_port = in.readInt();
	}

	/**
	 * {@inheritDoc}
	 */
	public int describeContents() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public void writeToParcel(Parcel out, int flags) {
		out.writeString((emailAddress == null ? "" : emailAddress));
		out.writeString((username == null ? "" : username));
		out.writeString((password == null ? "" : password));
		out.writeString((host_smtp_server == null ? "" : host_smtp_server));
		
		out.writeInt(smtp_port);
		out.writeInt((smtp_auth == true ? 1 : 0));
		out.writeString((popServer == null ? "" : popServer));
		out.writeInt(popPort);
		out.writeInt((popAuth == true ? 1 : 0));
		out.writeString((imapServer == null ? "" : imapServer));
		out.writeInt(imapPort);
		out.writeInt((imapAuth == true ? 1 : 0));
		
		out.writeInt(socket_port);
	}
	
	/**
	 * {@link #username} getter
	 * @return {@link #username}
	 */
	public String getUsername()
	{
		return username;
	}
	
	/**
	 * {@link #username} setter. If you are manipulating a saved configuration, be sure you update the store.
	 * @param uname new username
	 */
	public void setUsername(String uname)
	{
		username = uname;
	}
	
	/**
	 * Encrypts this configuration's password using the given key phrase
	 * @param key_phrase used to encrypt the password, and needed to decrypt
	 * @return the encrypted password String
	 * @throws Throwable
	 */
	public String encryptPassword(String key_phrase) throws Throwable
	{
		DESKeySpec keySpec = new DESKeySpec(key_phrase.getBytes("UTF8"));
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey key = keyFactory.generateSecret(keySpec);
		byte[] cleartext = password.getBytes("UTF8");      
		Cipher cipher = Cipher.getInstance("DES");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return new String(BASE64EncoderStream.encode(cipher.doFinal(cleartext)));
	}
	
	/**
	 * Decrypts the given encrypted String using the given encryption key, and sets the password to the resulting String
	 * @param encryptedString the encrypted password
	 * @param key_phrase the String key used to encrypt this password
	 * @throws Throwable
	 */
	public void decryptPassword(String encryptedString, String key_phrase) throws Throwable
	{
		DESKeySpec keySpec = new DESKeySpec(key_phrase.getBytes("UTF8"));
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey key = keyFactory.generateSecret(keySpec);
		byte[] encrypedPwdBytes = BASE64DecoderStream.decode(encryptedString.getBytes());

		Cipher cipher = Cipher.getInstance("DES");
		cipher.init(Cipher.DECRYPT_MODE, key);
		password = new String(cipher.doFinal(encrypedPwdBytes));
	}
	
	/**
	 * {@link #emailAddress} getter
	 * @return {@link #emailAddress}
	 */
	public String getAddress()
	{
		return emailAddress;
	}
	
	/**
	 * {@link #emailAddress} setter. If you are manipulating a saved configuration, be sure you update the store.
	 * @param address new email address
	 */
	public void setAddress(String address)
	{
		emailAddress = address;
	}

	/**
	 * Creates a new {@link Mailer} Object. This method enables developers to handle the
	 * password field properly by not keeping a reference to it anywhere else. It is kept private in 
	 * this Configuration, cannot be unparceled by an outside class, is encrypted in the store, and
	 * is private in Mailer.
	 * @param context needed to create the Mailer
	 * @return a new Mailer Object using this configuration
	 */
	public Mailer createMailer(Context context)
	{
		return new Mailer(context, this, password);
	}
	
}
