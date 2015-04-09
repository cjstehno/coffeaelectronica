package com.stehno.mockery

import spock.lang.*

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import com.stehno.mockery.service.EmailListService;

class EmailListServlet_SpockSpec extends Specification {

    private LIST = ['larry@stooge.com', 'moe@stooge.com', 'curley@stooge.com']

    def 'doGet: without list'() {
        setup:
        def emailListService = Mock(EmailListService) {
            1 * getListByName(_) >> { throw new IOException() }
        }

        def servletContext = Mock(ServletContext){
            1 * getAttribute(EmailListService.KEY) >> emailListService
        }

        def servletConfig = Mock(ServletConfig){
            1 * getServletContext() >> servletContext
        }

        def emailListServlet = new EmailListServlet()
        emailListServlet.init servletConfig

        def request = Mock(HttpServletRequest){
            1 * getParameter('listName') >> null
        }

        def response = Mock(HttpServletResponse)

        when:
        emailListServlet.doGet request, response

        then:
        thrown(IOException)
    }

    def 'doGet: with list'() {
        setup:
        def emailListService = Mock(EmailListService) {
            1 * getListByName('foolist') >> LIST
        }

        def servletContext = Mock(ServletContext){
            1 * getAttribute(EmailListService.KEY) >> emailListService
        }

        def servletConfig = Mock(ServletConfig){
            1 * getServletContext() >> servletContext
        }

        def emailListServlet = new EmailListServlet()
        emailListServlet.init servletConfig

        def request = Mock(HttpServletRequest){
            1 * getParameter('listName') >> 'foolist'
        }

        def writer = Mock(PrintWriter)

        def response = Mock(HttpServletResponse){
            1 * getWriter() >> writer
        }

        when:
        emailListServlet.doGet request, response

        then:
        1 * writer.println(LIST[0])
        1 * writer.println(LIST[1])
        1 * writer.println(LIST[2])
    }
}