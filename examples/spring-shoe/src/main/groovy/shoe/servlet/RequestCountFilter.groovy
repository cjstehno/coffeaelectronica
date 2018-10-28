package shoe.servlet

import org.springframework.web.filter.GenericFilterBean

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import java.util.concurrent.atomic.AtomicInteger

class RequestCountFilter extends GenericFilterBean {

    int startValue = 0

    private AtomicInteger count

    @Override
    protected void initFilterBean() throws ServletException {
        count = new AtomicInteger(startValue)
    }

    @Override
    void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException, ServletException{
        request.setAttribute('request-count', count.incrementAndGet())

        chain.doFilter( request, response )
    }
}
