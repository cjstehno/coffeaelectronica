package com.stehno.mockery;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.checkOrder;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import com.stehno.mockery.service.EmailListService;

public class EmailListServlet_EasyMockTest {

	private EmailListServlet servlet;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private EmailListService emailListService;

	@Before
	public void before() throws ServletException {
		this.emailListService = createMock(EmailListService.class);

		final ServletConfig servletConfig = createMock(ServletConfig.class);
		final ServletContext servletContext = createMock(ServletContext.class);

		expect(servletConfig.getServletContext()).andReturn(servletContext);
		expect(servletContext.getAttribute(EmailListService.KEY)).andReturn(emailListService);

		replay(servletConfig,servletContext);

		this.servlet = new EmailListServlet();
		servlet.init(servletConfig);

		this.request = createMock(HttpServletRequest.class);
		this.response = createMock(HttpServletResponse.class);
	}

	@Test(expected=IOException.class)
	public void doGet_without_list() throws Exception {
		expect(request.getParameter("listName")).andReturn(null);
		expect(emailListService.getListByName(null)).andThrow(new IOException());

		replay(request,emailListService);

		servlet.doGet(request, response);
	}

	@Test
	public void doGet_with_list() throws Exception {
		expect(request.getParameter("listName")).andReturn("foolist");

		final List<String> list = Arrays.asList("larry@stooge.com","moe@stooge.com","curley@stooge.com");
		expect(emailListService.getListByName("foolist")).andReturn(list);

		final PrintWriter writer = createMock(PrintWriter.class);
		checkOrder(writer,true);
		expect(response.getWriter()).andReturn(writer);

		writer.println("larry@stooge.com");
		writer.println("moe@stooge.com");
		writer.println("curley@stooge.com");
		writer.close();

		replay(request,response,writer,emailListService);

		servlet.doGet(request, response);

		verify(writer);
	}
}
