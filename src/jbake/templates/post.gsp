<%include "header.gsp"%>
	
	<h1>${content.title}</h1>

	<p><em><span class="glyphicon glyphicon-calendar"></span> ${content.date.format("dd MMMM yyyy")}</em> ~ <%= content.tags.collect { t->
		"<a href='/tags/${t}.html'><span class='label label-success'><span class='glyphicon glyphicon-tag'></span> $t</span></a>"
	}.join(', ') %></p>

	<p>${content.body}</p>

	<hr />
	
<%include "footer.gsp"%>