package de.unioninvestment.portal.hook;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.liferay.portal.kernel.events.Action;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;

public class RoleServicePreAction extends Action {
	
	static User loggedInUser = null;
	
	public static User getLoggedInUser() {
		return loggedInUser;
	}

	public void run(HttpServletRequest req, HttpServletResponse res) throws ActionException {
		loggedInUser = (User) req.getAttribute(WebKeys.USER);
	}

}
