package shoe.servlet

import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class HelloServlet extends HttpServlet {

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException{
        resp.writer.withPrintWriter { w->
            w.println "<${req.getAttribute('request-count')}> Hello, ${req.getParameter('name')}"
        }
    }
}
