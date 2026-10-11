package com.bidly.filter;

import com.bidly.model.SessionUser;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        SessionUser user = session != null ? (SessionUser) session.getAttribute(SessionUser.SESSION_KEY) : null;

        if (user == null) {
            String url = req.getRequestURI();
            String qs = req.getQueryString();
            if (qs != null) {
                url += "?" + qs;
            }

            // LoginBean sends them back here after they log in
            req.getSession(true).setAttribute("redirectUrl", url);
            res.sendRedirect(req.getContextPath() + "/login.xhtml");
            return;
        }

        chain.doFilter(request, response);
    }
}

