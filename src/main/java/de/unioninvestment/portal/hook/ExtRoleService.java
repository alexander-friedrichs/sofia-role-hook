package de.unioninvestment.portal.hook;


import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.liferay.portal.DuplicateRoleException;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Role;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.RoleService;
import com.liferay.portal.service.RoleServiceWrapper;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.naming.InitialContext;

public class ExtRoleService extends RoleServiceWrapper {


	private static final Logger logger = Logger.getLogger("ExtRoleService");
	private static final String ERROR_ROLES_ADD = "Error while adding ROLE: ";

    private Session mailSession;

	public ExtRoleService(RoleService roleService) {
		super(roleService);
	}

	public Role addRole(String name, Map<Locale, String> titleMap,Map<Locale, String> descriptionMap, int type) {
		try {
			// check if role already exist
			try {
				getRole(getDefaultCompanyId(), name);
			} catch (com.liferay.portal.NoSuchRoleException e) {
				logger.log(Level.INFO, "Add a new role: " + name);
				sendMail(name);
			}
			return super.addRole(name, titleMap, descriptionMap, type);
		} catch (PortalException e) {
			if (e instanceof DuplicateRoleException){
				// do nothing - alles bestens 
			}
			else
			   logger.log(Level.SEVERE, ERROR_ROLES_ADD + e.toString());
		} catch (SystemException e) {
			logger.log(Level.SEVERE, ERROR_ROLES_ADD + e.toString());
		} catch (AddressException e) {
			logger.log(Level.SEVERE, "Error sending Mail " + e.toString());
		} catch (MessagingException e) {
			logger.log(Level.SEVERE, "Error sending Mail " + e.toString());
		}
		return null;
	}
	
	@SuppressWarnings(value="unchecked")
	public long getDefaultCompanyId() {
		try {
			DynamicQuery query = DynamicQueryFactoryUtil.forClass(Company.class).add(PropertyFactoryUtil.forName("active").eq(Boolean.TRUE));
			List<Company> users = CompanyLocalServiceUtil.dynamicQuery(query);
			Company c = users.get(0);
			return c.getCompanyId();
		} catch (SystemException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	
	private void sendMail(String roleName) throws AddressException,	MessagingException {
		try {
			
			InitialContext ic = new InitialContext();
	       	mailSession = (Session)ic.lookup("java:jboss/mail/Default");

			StringBuffer sb = new StringBuffer();
			sb.append("Hallo, \nes gibt eine neue Sofia Rolle auf der Abnahme : \n");
			sb.append(roleName + "\n \n");
			sb.append("Bitte Dokumentation auf http://wiki.de/index.php/Rollenkonzept pruefen.\n \n");
			sb.append("Ihre Sofia");

			MimeMessage m = new MimeMessage(mailSession);
			Address from = new InternetAddress(	"sofiarolecheck@mail.de");
			m.setFrom(from);
			m.setRecipient(Message.RecipientType.TO,new javax.mail.internet.InternetAddress("irgendwer@mail.de"));
			m.setSubject(MimeUtility.encodeText("Neue Sofia Rolle auf Abnahme : " + roleName,"utf-8", null));

			m.setContent(sb.toString(), "text/plain");
			Transport.send(m);
			logger.log(Level.INFO, "MailAPI: ExtRoleService send succesful");

			ic.close();
		} catch (Exception e) {
			logger.log(Level.INFO, "ExtRoleService mail send failed " + e.toString());
		}
	}
	
}