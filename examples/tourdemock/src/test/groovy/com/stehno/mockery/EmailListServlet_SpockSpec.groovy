package com.stehno.mockery

import com.stehno.mockery.service.EmailListService
import spock.lang.*

import javax.servlet.ServletConfig
import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class EmailListServlet_SpockSpec extends Specification {

    private static final LIST = ['larry@stooge.com', 'moe@stooge.com', 'curley@stooge.com']
    private emailListServlet, request, response

    def setup() {
        def emailListService = Mock(EmailListService) {
            _ * getListByName(null) >> { throw new IOException() }
            _ * getListByName('foolist') >> LIST
        }

        def servletContext = Mock(ServletContext) {
            1 * getAttribute(EmailListService.KEY) >> emailListService
        }

        def servletConfig = Mock(ServletConfig) {
            1 * getServletContext() >> servletContext
        }

        emailListServlet = new EmailListServlet()
        emailListServlet.init servletConfig

        request = Mock(HttpServletRequest)
        response = Mock(HttpServletResponse)
    }

    def 'doGet: without list'() {
        setup:
        1 * request.getParameter('listName') >> null

        when:
        emailListServlet.doGet request, response

        then:
        thrown(IOException)
    }

    def 'doGet: with list'() {
        setup:
        1 * request.getParameter('listName') >> 'foolist'

        def writer = Mock(PrintWriter)

        1 * response.getWriter() >> writer

        when:
        emailListServlet.doGet request, response

        then:
        1 * writer.println(LIST[0])
        1 * writer.println(LIST[1])
        1 * writer.println(LIST[2])
    }
}