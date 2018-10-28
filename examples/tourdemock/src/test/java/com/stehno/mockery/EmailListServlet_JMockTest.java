package com.stehno.mockery;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.stehno.mockery.service.EmailListService;

@RunWith(JMock.class)
public class EmailListServlet_JMockTest {

	private Mockery mockery = new JUnit4Mockery();
	private EmailListServlet servlet;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private EmailListService emailListService;

	public EmailListServlet_JMockTest(){
		mockery.setImposteriser(ClassImposteriser.INSTANCE);
	}

	@Before
	public void before() throws ServletException {
		this.emailListService = mockery.mock(EmailListService.class);

		final ServletConfig servletConfig = mockery.mock(ServletConfig.class);
		mockery.checking(new Expectations(){
			{
				final ServletContext servletContext = mockery.mock(ServletContext.class);

				one(servletConfig).getServletContext(); will(returnValue(servletContext));

				one(servletContext).getAttribute(EmailListService.KEY); will(returnValue(emailListService));
			}
		});

		this.servlet = new EmailListServlet();
		servlet.init(servletConfig);

		this.request = mockery.mock(HttpServletRequest.class);
		this.response = mockery.mock(HttpServletResponse.class);
	}

	@Test(expected=IOException.class)
	public void doGet_without_list() throws Exception {
		mockery.checking(new Expectations(){
			{
				one(request).getParameter("listName"); will(returnValue(null));
				one(emailListService).getListByName(null); will(throwException(new IOException()));
			}
		});

		servlet.doGet(request, response);
	}

	@Test
	public void doGet_with_list() throws Exception {
		final PrintWriter writer = mockery.mock(PrintWriter.class);

		final Sequence printSequence = mockery.sequence("printSequence");

		mockery.checking(new Expectations(){
			{
				one(request).getParameter("listName"); will(returnValue("foolist"));

				final List<String> list = Arrays.asList("larry@stooge.com","moe@stooge.com","curley@stooge.com");
				one(emailListService).getListByName("foolist"); will(returnValue( list ));

				one(response).getWriter(); will(returnValue(writer));

				one(writer).println("larry@stooge.com"); inSequence(printSequence);
				one(writer).println("moe@stooge.com"); inSequence(printSequence);
				one(writer).println("curley@stooge.com"); inSequence(printSequence);
				one(writer).close(); inSequence(printSequence);
			}
		});

		servlet.doGet(request, response);
	}

	@After
	public void after(){
		this.servlet = null;
		this.request = null;
		this.response = null;
		this.emailListService = null;
	}
}
