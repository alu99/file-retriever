/*Refreshes the idleManager because 
 * RFC2177 states that gmail logs off
 * client every 10 minutes
 */
import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.Session;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;

public class RefreshIdle extends Thread {

	private String threadName;
	private IMAPFolder folder;

	public RefreshIdle(String name, Session session, IMAPFolder folder) throws IOException{

		threadName = name;
		this.folder = folder;

		System.out.println("Creating: " + threadName);

	}

	public void run() {

		System.out.println("running thread");

		while(!Thread.interrupted()){
			
			try {
				
				Thread.sleep(300000);
				
				Object val = folder.doCommand(new IMAPFolder.ProtocolCommand() {

					public Object doCommand(IMAPProtocol p) throws ProtocolException {

						p.simpleCommand("NOOP", null);
						return null;
					}


				});

			} catch (MessagingException e) {

				e.printStackTrace();
			} catch (InterruptedException e) {
				
				e.printStackTrace();
				return;
			}
		}
	}

}



