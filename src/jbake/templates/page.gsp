<% include "header.gsp" %>

<h1><% if(content.icon){ %><span class="${content.icon}"></span> <% } %>${content.title}</h1>

<p>${content.body}</p>

<% include "footer.gsp" %>