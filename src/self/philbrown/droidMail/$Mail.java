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

import org.json.JSONException;

import self.philbrown.droidMail.Mailer.MailListener;
import self.philbrown.droidQuery.$;
import self.philbrown.droidQuery.$Extension;

/**
 * Mail Extension for <a href="https://github.com/phil-brown/droidQuery">droidQuery</a>.<br>
 * Adds the ability to send email messages without <em>Intent</em>, and minimal (if any) user input. 
 * For example, the following will send an email from <em>john.doe@gmail.com</em> to 
 * <em>jane.doe@yahoo.com</em>:
 * <pre>
 * $.extend("mail", "self.philbrown.droidMail.$Mail");
 * $.with(this).ext("mail", new MailOptions("{ email: 'john.doe@gmail.com',
 *                                             username: 'john.doe',
 *                                             password: 'idkmypsswd',
 *                                             provider: 'gmail',
 *                                             destination: 'jane.doe@yahoo.com',
 *                                             subject: 'I love you',
 *                                             message: 'Have a great day at work!',
 *                                             attachment: 'path/to/file.txt'
 *                                            }"));
 * </pre>
 * Alternatively, one can create the {@code $Mail} instance, and use it later to send messages:
 * <pre>
 * $.extend("mail", "self.philbrown.droidMail.$Mail");
 * $Mail mail = ($Mail) $.with(this).ext("mail", new MailOptions("{ email: 'john.doe@gmail.com',
 *                                                                  username: 'john.doe',
 *                                                                  password: 'idkmypsswd',
 *                                                                  provider: 'gmail' }"));
 * mail.send("{ destination: 'jane.doe@yahoo.com',
 *              subject: 'I love you',
 *              message: 'Have a great day at work!',
 *              attachment: 'path/to/file.txt'
 *            }");
 * </pre>
 * @author Phil Brown
 *
 */
public class $Mail extends $Extension
{
	/** Mail Configuration Options */
	private MailOptions options;

	/**
	 * Constructor
	 * @param droidQuery
	 */
	public $Mail($ droidQuery) {
		super(droidQuery);
	}

	@Override
	protected void invoke(Object... args) {
		try
		{
			options = (MailOptions) args[0];
			if (options.destinations != null && options.message != null)
			{
				Mailer mailer = options.getMailer();
				mailer.setMailListener(new MailListener() {

					@Override
					public void onSuccess(Mailer m) {
						options.success.invoke();
					}

					@Override
					public void onError(Mailer m) {
						options.error.invoke();
					}

					@Override
					public void onComplete(Mailer m) {
						options.error.invoke();
					}
					
				});
				File attachment = null;
				if (options.attachment != null)
				{
					attachment = new File(options.attachment);
				}
				mailer.send(options.destinations, options.subject, options.message, attachment);
			}
			
		}
		catch (Throwable t)
		{
			//error
		}
	}
	
	/**
	 * Send a Mail Message with the JSON Options
	 * @param json Used for creating a new MailOptions object
	 * @see MailOptions
	 */
	public void send(String json)
	{
		try {
			MailOptions temp = new MailOptions(json);
			File attachment = null;
			if (temp.attachment != null)
			{
				attachment = new File(temp.attachment);
			}
			send(temp.destinations, temp.subject, temp.message, attachment);
		} catch (JSONException e) {
			//error
		}
	}
	
	/**
	 * Send a message
	 * @param destinations array of destination addresses
	 * @param subject the email subject
	 * @param message the email message
	 * @param attachment the email attachment
	 */
	public void send(final String[] destinations, final String subject, final String message, final File attachment)
	{
		Mailer mailer = options.getMailer();
		mailer.setMailListener(new MailListener() {

			@Override
			public void onSuccess(Mailer m) {
				options.success.invoke();
			}

			@Override
			public void onError(Mailer m) {
				options.error.invoke();
			}

			@Override
			public void onComplete(Mailer m) {
				options.error.invoke();
			}
			
		});
		mailer.send(destinations, subject, message, attachment);
	}

}
