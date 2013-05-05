package com.stehno.mockery;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnit44Runner;

import com.stehno.mockery.service.EmailListService;

@RunWith(MockitoJUnit44Runner.class)
public class EmailListServlet_MockitoTest {

	private EmailListServlet servlet;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private EmailListService emailListService;

	@Before
	public void before() throws ServletException {
		final ServletConfig servletConfig = mock(ServletConfig.class);
		final ServletContext servletContext = mock(ServletContext.class);

		when(servletConfig.getServletContext()).thenReturn(servletContext);
		when(servletContext.getAttribute(EmailListService.KEY)).thenReturn(emailListService);

		this.servlet = new EmailListServlet();
		servlet.init(servletConfig);
	}

	@Test(expected=IOException.class)
	public void doGet_without_list() throws Exception {
		when(request.getParameter("listName")).thenReturn(null);
		when(emailListService.getListByName(null)).thenThrow(new IOException());

		servlet.doGet(request, response);
	}

	@Test
	public void doGet_with_list() throws Exception {
		final PrintWriter writer = mock(PrintWriter.class);

		when(request.getParameter("listName")).thenReturn("foolist");

		final List<String> list = Arrays.asList("larry@stooge.com","moe@stooge.com","curley@stooge.com");
		when(emailListService.getListByName("foolist")).thenReturn(list);

		when(response.getWriter()).thenReturn(writer);

		servlet.doGet(request, response);

		final InOrder order = inOrder(writer);

		order.verify(writer).println("larry@stooge.com");
		order.verify(writer).println("moe@stooge.com");
		order.verify(writer).println("curley@stooge.com");
		order.verify(writer).close();
	}

	@After
	public void after(){
		this.servlet = null;
	}
}
