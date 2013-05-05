package com.stehno.mockery;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static junit.framework.Assert.*

import org.junit.After
import org.junit.Test
import org.junit.Before;

import com.stehno.mockery.service.EmailListService;

class EmailListServlet_GroovyMockTest {

	private def LIST = ['larry@stooge.com','moe@stooge.com','curley@stooge.com']
	private def emailListServlet
	private def emailListService

	@Before
	void before(){
		emailListService = [
			getListByName:{ name->
				if( 'foolist' == name ){ return LIST }
				throw new IOException()
			}
		] as EmailListService

		def servletContext = [
			getAttribute:{ key->
				assertEquals EmailListService.KEY, key
				emailListService
			}
		] as ServletContext

		def servletConfig = [ getServletContext:{servletContext} ] as ServletConfig

		emailListServlet = new EmailListServlet()
		emailListServlet.init servletConfig
	}

	@Test(expected=IOException.class)
	void doGet_without_list(){
		def request = [
			getParameter:{ pname->
				assertEquals 'listName', pname
				null
			}
		] as HttpServletRequest

		emailListServlet.doGet request, [] as HttpServletResponse
	}

	@Test
	void doGet_with_list(){
		def request = [
			getParameter:{ pname->
				assertEquals 'listName', pname
				'foolist'
			}
		] as HttpServletRequest

		def outcalls = [].addAll( LIST )
		PrintWriter.metaClass.println = { out-> assertEquals outcalls.remove(0), out }

		def response = [ getWriter:{ new PrintWriter(new StringWriter()) } ] as HttpServletResponse

		emailListServlet.doGet request, response
	}

	@After
	void after(){
		emailListServlet = null
		emailListService = null
	}
}
