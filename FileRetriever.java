import java.io.IOException;
import java.util.Properties;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class FileRetriever {

	public static final String  TO = "xxxxxxxxxxxxxxxxxxxx"; //email of authorized user. ex: "johndoe@gmail.com"
	public static final String TO_NAME = "xxxxxxxxxxxxxxxxxxxx"; // the name of the the authorized user. ex: "John Doe"
	public static final String FROM = "xxxxxxxxxxxxxxxxxxxx"; // the gmail address of that FileRetriever uses
	public static final String USERNAME = "xxxxxxxxxxxxxxxxxxxx"; // username of gmail account that FileRetriever uses
	public static final String PWORD = "xxxxxxxxxxxxxxxxxxxx"; // password of gmail account that FileRetriever uses
	

	public static void main(String[] args) throws IOException{

		String host = "imap.gmail.com";

		Properties props = new Properties();

		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", "587");//Apparently 587 is the default port


		//creates the session object
		Session session = Session.getInstance(props,new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(USERNAME, PWORD);
			}
		});

		fetch(session, host);
	}

	//fetches the filename from body text, ensure that message is sent in plain text
	public static void fetch(final Session session, String host) throws IOException{

		try {

			//opens store
			final IMAPStore store = (IMAPStore) session.getStore("imaps");
			store.connect(host, FROM, PWORD);

			//accesses the inbox folder
			final IMAPFolder folder = (IMAPFolder) store.getFolder("INBOX");
			folder.open(folder.READ_WRITE);

			RefreshIdle refresh = new RefreshIdle("Refresher", session, folder);
			refresh.start();

			//listens for new messages
			folder.addMessageCountListener(new MessageCountAdapter() {

				public void messagesAdded(MessageCountEvent ev) {

					System.out.println("Message Added Triggered");

					Message mostRecent = null;
					Address[] from = null;
					String fname = "";

					try {

						//returns the most recent message
						mostRecent = folder.getMessage(folder.getMessageCount());
						from = mostRecent.getFrom();
						System.out.println(from[0]);

						//if the message is from me, then will process it
						if(from[0].toString().equals(TO_NAME + " " + "<" + TO + ">")) {

							try {
								fname = mostRecent.getContent().toString();

							} catch (IOException e) {
								
								System.out.println("Cannot read file name");
							}

							//substring so it removes the newline after file name
							int spaceLoc = fname.indexOf("\n");
							fname = fname.substring(0, spaceLoc-1);
							reply(session, fname);
						}

					} catch (MessagingException e) {

						e.printStackTrace();
					} 
				}
			});

			//re idles if thread has no interruptions
			while(!refresh.isInterrupted()){

				folder.idle();
			}

			//closes the thread if thread is interrupted 
			if(refresh.isAlive())
				refresh.interrupt();

		} 

		catch (NoSuchProviderException e) {

			e.printStackTrace();

		} 
		catch (MessagingException e) {
			
			fetch(session, host);
			e.printStackTrace();
		}

	}

	public static void reply(Session session, String fname){

		System.out.println(fname);

		try{

			//creates the message object and the multiparts
			//http://www.codejava.net/java-ee/javamail/send-e-mail-with-attachment-in-java (good tutorial)
			Message message = new MimeMessage(session);
			Multipart multipart = new MimeMultipart();

			//sets the sender, recipient, subject
			message.setFrom(new InternetAddress(FROM));
			message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(TO));
			message.setSubject("File Robot @ your service");
			message.setText("You're welcome...");

			//creates bodypart, attaches file
			MimeBodyPart attachment = new MimeBodyPart();
			attachment.attachFile(fname);

			//attaches bodyparts to the multipart
			multipart.addBodyPart(attachment);

			message.setContent(multipart);

			Transport.send(message);
			System.out.println("Message Sent");

		}
		catch(MessagingException e){

			sendIOErrorMessage(session);
		} 
		catch (IOException e) {

			e.printStackTrace();
		}
	}

	//notifies sender if there is an IO error
	public static void sendIOErrorMessage(Session session){

		System.out.println("sendIOErrorMessage");

		Message errorMessage = new MimeMessage(session);

		try {

			errorMessage.setFrom(new InternetAddress(FROM));
			errorMessage.setRecipients(Message.RecipientType.TO,InternetAddress.parse(TO));
			errorMessage.setSubject("FileIO Error");
			errorMessage.setText("Please fix filepath");

			Transport.send(errorMessage);

			System.out.println("Error Message Sent");

		} catch (AddressException e) {

			e.printStackTrace();
		} catch (MessagingException e) {

			e.printStackTrace();
		}

	}
}
