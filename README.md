## DroidMail

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

    $.with(this).ext("mail", new MailOptions("{ email: "john.doe@gmail.com",
                                                username: "john.doe",
                                                password: "idkmypsswd",
                                                provider: "gmail",
                                                destination: "jane.doe@yahoo.com",
                                                subject: "I love you",
                                                message: "Have a great day at work!",
                                                attachment: "path/to/file.txt"
                                              }"));