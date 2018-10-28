package shoe.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest

@RestController
class TimeController {

    @RequestMapping('/time')
    String time( HttpServletRequest request ){
        "<${request.getAttribute('request-count')}> Current-time: ${new Date()}"
    }
}
