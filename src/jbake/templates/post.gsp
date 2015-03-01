<%include "header.gsp"%>
	
	<%include "menu.gsp"%>
	
	<div class="page-header">
		<h1>${content.title}</h1>
	</div>

	<p><em>${content.date.format("dd MMMM yyyy")}</em> ~ <%= content.tags.collect { t-> "<a href='/tags/${t}.html'>$t</a>" }.join(', ') %></p>

	<p>${content.body}</p>

	<hr />
	
<%include "footer.gsp"%>