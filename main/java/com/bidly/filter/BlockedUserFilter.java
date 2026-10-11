package com.bidly.filter;

import com.bidly.model.SessionUser;
import com.bidly.model.User;
import com.bidly.service.AccountBlockedException;
import com.bidly.service.UserService;
import jakarta.ejb.EJB;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Runs on every page. If an admin blocks (or deletes) a user while they are logged in,
 * this kicks them out on their next request instead of waiting for the session to expire.
 */
public class BlockedUserFilter implements Filter {

    @EJB
    private UserService userService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        SessionUser sessionUser = session != null ? (SessionUser) session.getAttribute(SessionUser.SESSION_KEY) : null;

        if (sessionUser != null) {
            User current = userService.findById(sessionUser.getId());
            if (current == null || current.isBlocked()) {
                session.invalidate();
                req.getSession(true).setAttribute("loginNotice",
                        current == null ? "Your account no longer exists."
                                        : AccountBlockedException.messageFor(current.getBlockReason()));
                res.sendRedirect(req.getContextPath() + "/login.xhtml");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}

