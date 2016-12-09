<%include 'header.gsp'%>

	<h1><span class="glyphicon glyphicon-calendar"></span> Blog Archives</h1>

	<%
	    def months = [:]

        published_posts.each { post->
            def month = post.date.format("MMMM yyyy")
            def posts = months[month]
            if( !posts ){
                posts = []
                months[month] = posts
            }

            posts << post
        }

        months.each { month, posts-> %>
            <h2>${month}</h2>
            <div>
     <%     posts.each { p->
                def tagString = p.tags.collect { t-> "<a href='/tags/${t}.html'><span class='label label-success'><span class='glyphicon glyphicon-tag'></span> ${t}</span></a>" }.join(' ')
     %>
                <div>${p.date.format('MM/dd')}: <a href="${p.uri}">${p.title}</a> ~ ${tagString}</div>
     <%     } %>
            </div>
     <%
         }
	%>

<%include "footer.gsp"%>
