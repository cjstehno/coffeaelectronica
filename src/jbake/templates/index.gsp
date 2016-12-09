<%include "header.gsp"%>

	<%(published_posts[0..10]).each {post ->%>
        <div class="row">
            <div class="col-lg-12 col-md-12 col-sm-12">
                <h1><a href="${post.uri}">${post.title}</a></h1>
                <p><em><span class="glyphicon glyphicon-calendar"></span> ${post.date.format("dd MMMM yyyy")}</em> ~ <%= post.tags.collect { t->
                    "<a href='/tags/${t}.html'><span class='label label-success'><span class='glyphicon glyphicon-tag'></span> $t</span></a>"
                }.join(', ') %>
                <p>${post.body}</p>
            </div>
        </div>
  	<%}%>

<div class="well well-sm">
    <p><span class="glyphicon glyphicon-hourglass"></span> Older posts are available in the <a href="/archive.html">archives</a>.</p>
</div>


<%include "footer.gsp"%>