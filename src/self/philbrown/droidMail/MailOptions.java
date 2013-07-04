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
import android.content.Context;
import android.util.Log;

/**
 * Useful Object for droidQuery
 * @author Phil Brown
 *
 */
public class MailOptions 
{
	public MailConfiguration configuration;
	private String password;
	public Function success, error, complete;
	public String subject = null;
	public String message = null;
	public String attachment = null;
	public String[] destinations = null;

	/**
	 * Use JSON string to create:
	 * <pre>
	 * new MailOptions($.this, "{
	 *                    email: "john.doe@gmail.com",
	 *                    username: "john.doe",
	 *                    password: "idkmypsswd",
	 *                    provider: "gmail", //note: must be one of the available providers
	 *                    destinations: [ "jane.doe@yahoo.com", "bill.doe@yahoo.com" ],
	 *                    subject: "I love you",
	 *                    message: "Have a great day at work!",
	 *                    attachment: "path/to/file.txt"
	 *                  }").success(new Function() {
	 *                  	@Override
	 *                  	public void invoke(Object... args) {
	 *                  		$.alert("message sent!");
	 *                  	}
	 *                  }).error(new Function() {
	 *                  	@Override
	 *                  	public void invoke(Object... args) {
	 *                  		$.alert("message failed!");
	 *                  	}
	 *                  }).complete(new Function() {
	 *                  	@Override
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
	 * @param json
	 * @throws JSONException if JSON string is malformed
	 */
	public MailOptions(String json) throws JSONException
	{
		this($.map(new JSONObject(json)));
	}
	
	public MailOptions(Map<String, Object> json)
	{
		String email = null;
		String username = null;
		Provider provider = null;
		
		
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
				provider = Provider.valueOf(Provider.class, ((String) value).toLowerCase(Locale.US));
					
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
				this.configuration = new MailConfiguration(email, username, password, provider);
			} catch (InvalidKeyException e) {
				Log.e("$Mail", "Invalid Provider");
			}
		}
	}
	
	public MailOptions complete(Function complete)
	{
		this.complete = complete;
		return this;
	}
	
	public MailOptions success(Function success)
	{
		this.success = success;
		return this;
	}
	
	public MailOptions error(Function error)
	{
		this.error = error;
		return this;
	}
	
	public Mailer getMailer(Context context)
	{
		return new Mailer(context, configuration, password);
	}
}
