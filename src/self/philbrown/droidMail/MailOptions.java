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
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import self.philbrown.droidMail.MailConfiguration.Provider;
import self.philbrown.droidQuery.$;
import self.philbrown.droidQuery.Function;
import android.util.Log;

/**
 * Used to configure a droidMail email account.
 * @author Phil Brown
 *
 */
public class MailOptions 
{
	/** <em>this</em> MailConfiguration */
	public MailConfiguration configuration;
	/** Email Password */
	private String password;
	/** Callback for a successfully sent message */
	public Function success;
	/** Callback for a message that could not be sent */
	public Function error;
	/** Callback for after the message has been sent (either successfully or not) */
	public Function complete;
	/** Email Subject */
	public String subject = null;
	/** Email Message content */
	public String message = null;
	/** Email attachment */
	public String attachment = null;
	/** Email destination addresses */
	public String[] destinations = null;

	/**
	 * Constructor.<br>
	 * Use JSON string to create:
	 * <pre>
	 * new MailOptions("{
	 *                    email: 'john.doe@gmail.com',
	 *                    username: 'john.doe',
	 *                    password: 'idkmypsswd',
	 *                    provider: 'gmail', //note: must be one of the available providers
	 *                    destinations: [ 'jane.doe@yahoo.com', 'bill.doe@yahoo.com' ],
	 *                    subject: 'I love you',
	 *                    message: 'Have a great day at work!',
	 *                    attachment: 'path/to/file.txt'
	 *                  }").success(new Function() {
	 *                  	public void invoke(Object... args) {
	 *                  		$.alert(this, "message sent!");
	 *                  	}
	 *                  }).error(new Function() {
	 *                  	public void invoke(Object... args) {
	 *                  		$.alert(this, "message failed!");
	 *                  	}
	 *                  }).complete(new Function() {
	 *                  	public void invoke(Object... args) {
	 *                  		Log.i("Mail", "Message complete");
	 *                  	}
	 *                  });
	 * </pre>
	 * <h1>JSON Options</h1>
	 * <ul>
	 * 	<li> <b>email:</b> string representation sender's email address
	 * 	<li> <b>username:</b> string representation of the sender's username
	 * 	<li> <b>password:</b> string representation of the sender's password
	 * 	<li> <b>provider:</b> string representation of a {@link MailConfiguration.Provider}
	 * 	<li> <b>destination:</b> a single string representation of the destination address
	 * 	<li> <b>destinations:</b> use instead of {@code destination} to represent, in array form, a list of
	 * destination addresses. For example: [ "jane.doe@yahoo.com", "foobar@example.com" ]
	 * 	<li> <b>subject:</b> string subject of the email
	 * 	<li> <b>message:</b> string message of the email
	 * 	<li> <b>attachment:</b> path to attachment file
	 * </ul>
	 * @param json the JSON string
	 * @throws JSONException if JSON string is malformed
	 */
	public MailOptions(String json) throws JSONException
	{
		this($.map(new JSONObject(json)));
	}
	
	/**
	 * Constructor. Use Key-Value pairings for configuration.<br>
	 * 
	 * <h1>JSON Options</h1>
	 * <ul>
	 * 	<li> <b>email:</b> string representation sender's email address
	 * 	<li> <b>username:</b> string representation of the sender's username
	 * 	<li> <b>password:</b> string representation of the sender's password
	 * 	<li> <b>provider:</b> string representation of a {@link MailConfiguration.Provider} <b>OR</b>
	 * an instance of {@link MailConfiguration}. This is useful for custom configurations.
	 * 	<li> <b>destination:</b> a single string representation of the destination address
	 * 	<li> <b>destinations:</b> use instead of {@code destination} to represent, in array form, a list of
	 * destination addresses. For example: [ "jane.doe@yahoo.com", "foobar@example.com" ]
	 * 	<li> <b>subject:</b> string subject of the email
	 * 	<li> <b>message:</b> string message of the email
	 * 	<li> <b>attachment:</b> path to attachment file
	 * </ul>
	 * @param json the dictionary of Options
	 */
	public MailOptions(Map<String, Object> json)
	{
		String email = null;
		String username = null;
		Object provider = null;
		
		
		for (Entry<String, Object> entry : json.entrySet())
		{
			String key = entry.getKey();
			Object value = entry.getValue();
			if (key.equalsIgnoreCase("email"))
			{
				email = (String) value;
			}
			else if (key.equalsIgnoreCase("username"))
			{
				username = (String) value;
			}
			else if (key.equalsIgnoreCase("password"))
			{
				password = (String) value;
			}
			else if (key.equalsIgnoreCase("provider"))
			{
				if (value instanceof String)
					provider = Provider.valueOf(Provider.class, ((String) value).toLowerCase(Locale.US));
				else if (value instanceof MailConfiguration)
					provider = value;
			}
			else if (key.equalsIgnoreCase("destination"))
			{
				destinations = new String[]{(String) value};
			}
			else if (key.equalsIgnoreCase("destinations"))
			{
				if (value instanceof JSONArray)
				{
					JSONArray array = (JSONArray) value;
					destinations = new String[array.length()];
					for (int i = 0; i < array.length(); i++)
					{
						try {
							destinations[i] = array.getString(i);
						} catch (JSONException e) {
							destinations[i] = "";
						}
					}
					
				}
				else if (value instanceof String[])
				{
					destinations = (String[]) value;
				}
				else if (value instanceof String)
				{
					//allow a comma-separated string
					destinations = ((String) value).split(",");
				}
			}
			else if (key.equalsIgnoreCase("subject"))
			{
				subject = (String) value;
			}
			else if (key.equalsIgnoreCase("message"))
			{
				message = (String) value;
			}
			else if (key.equalsIgnoreCase("attachment"))
			{
				attachment = (String) value;
			}
			
			try {
				if (provider instanceof Provider)
					this.configuration = new MailConfiguration(email, username, password, (Provider) provider);
				else if (provider instanceof MailConfiguration)
					this.configuration = new MailConfiguration(email, username, password, (MailConfiguration) provider);
			} catch (InvalidKeyException e) {
				Log.e("$Mail", "Invalid Provider");
			}
		}
	}
	
	/**
	 * Set the function to call when the message has been sent (whether or not is was successful 
	 * or failed)
	 * @param complete the Function
	 * @return this
	 */
	public MailOptions complete(Function complete)
	{
		this.complete = complete;
		return this;
	}
	
	/**
	 * Set the function to call when the message has been sent successfully
	 * @param success the Function
	 * @return this
	 */
	public MailOptions success(Function success)
	{
		this.success = success;
		return this;
	}
	
	/**
	 * Set the function to call when the message has failed to send
	 * @param error the Function
	 * @return this
	 */
	public MailOptions error(Function error)
	{
		this.error = error;
		return this;
	}
	
	/**
	 * Gets the mailer required to send the message. This is placed here to avoid passing around the
	 * password argument (which is now kept secure).
	 * @return
	 */
	public Mailer getMailer()
	{
		return new Mailer(configuration, password);
	}
}
