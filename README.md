## droidMail

---------------------

__droidMail__ is a [droidQuery](https://github.com/phil-brown/droidQuery) extension for 
sending emails in *Android* without using `Intent`.

To use, add as a *droidQuery* extension:

    try {
        $.extend("mail", "self.philbrown.droidMail.$Mail");
    }
    catch (Throwable t) {
        Log.e("MyApp", "Could not add mail extension");
    }
    
Then to send an email message:

    $.with(this).ext("mail", new MailOptions("{ email: 'john.doe@gmail.com',
                                                username: 'john.doe',
                                                password: 'idkmypsswd',
                                                provider: 'gmail',
                                                destination: 'jane.doe@yahoo.com',
                                                subject: 'I love you',
                                                message: 'Have a great day at work!',
                                                attachment: 'path/to/file.txt'
                                              }"));
                                              
Alternatively, one can create the `$Mail` instance, and use it later to send messages:

    $Mail mail = ($Mail) $.with(this).ext("mail", new MailOptions("{ email: 'john.doe@gmail.com',
                                                                     username: 'john.doe',
                                                                     password: 'idkmypsswd',
                                                                     provider: 'gmail' }"));
    mail.send("{ destination: 'jane.doe@yahoo.com',
                 subject: 'I love you',
                 message: 'Have a great day at work!',
                 attachment: 'path/to/file.txt'
              }");