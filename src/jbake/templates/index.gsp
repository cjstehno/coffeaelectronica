<%include "header.gsp"%>

	<%include "menu.gsp"%>

	<%(published_posts[0..10]).each {post ->%>
		<a href="${post.uri}"><h1>${post.title}</h1></a>
		<p><em>${post.date.format("dd MMMM yyyy")}</em> ~ <%= post.tags.collect { t-> "<a href='/tags/${t}.html'>$t</a>" }.join(', ') %></p>
		<p>${post.body}</p>
  	<%}%>
	
	<hr />
	
	<p>Older posts are available in the <a href="/${config.archive_file}">archive</a>.</p>

<%include "footer.gsp"%>