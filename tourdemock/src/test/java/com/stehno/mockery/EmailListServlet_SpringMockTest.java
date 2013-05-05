package com.stehno.mockery;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import com.stehno.mockery.service.EmailListService;

public class EmailListServlet_SpringMockTest {

	private static final String sep = System.getProperty("line.separator");
	private EmailListServlet servlet;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@Before
	public void before() throws ServletException {
		final MockEmailListService emailListService = new MockEmailListService();

		final MockServletConfig servletConfig = new MockServletConfig();
		servletConfig.getServletContext().setAttribute(EmailListService.KEY, emailListService);

		this.servlet = new EmailListServlet();
		servlet.init(servletConfig);

		this.request = new MockHttpServletRequest();
		this.response = new MockHttpServletResponse();
	}

	@Test(expected=IOException.class)
	public void doGet_without_list() throws Exception {
		servlet.doGet(request, response);
	}

	@Test
	public void doGet_with_list() throws Exception {
		request.setParameter("listName", "foolist");

		servlet.doGet(request, response);

		assertEquals("larry@stooge.com" + sep + "moe@stooge.com" + sep + "curley@stooge.com" + sep,response.getContentAsString());
	}

	@After
	public void after(){
		this.servlet = null;
		this.request = null;
		this.response = null;
	}

	private static class MockEmailListService implements EmailListService {

		@Override
		public List<String> getListByName(final String listName) throws IOException {
			if(listName == null){
				throw new IOException();
			} else {
				return Arrays.asList("larry@stooge.com","moe@stooge.com","curley@stooge.com");
			}
		}
	}
}
