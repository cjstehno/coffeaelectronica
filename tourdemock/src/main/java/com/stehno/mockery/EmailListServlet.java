package com.stehno.mockery;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.stehno.mockery.service.EmailListService;

public class EmailListServlet extends HttpServlet {

	private static final long serialVersionUID = -9041780030848464349L;
	private EmailListService emailListService;

	public void init() throws ServletException {
		final ServletContext servletContext = getServletContext();
		this.emailListService = (EmailListService)servletContext.getAttribute(EmailListService.KEY);

		if(emailListService == null) throw new ServletException("No ListService available!");
	}

	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		final String listName = request.getParameter("listName");

		final List<String> list = emailListService.getListByName(listName);

		PrintWriter writer = null;
		try {
			writer = response.getWriter();

			for(final String email : list){
				writer.println(email);
			}

		} finally {
			if(writer != null) writer.close();
		}
	}
}
