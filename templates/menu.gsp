	<!-- Fixed navbar -->
    <div class="navbar navbar-default navbar-fixed-top" role="navigation">
      <div class="container">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="/">CoffeaElectronica.com</a>
        </div>
        <div class="navbar-collapse collapse">
          <ul class="nav navbar-nav">
            <li><a href="/index.html">Home</a></li>
            <li><a href="/about.html">About</a></li>
            <li><a href="/archive.html">Archive</a></li>
            
             <li role="presentation" class="dropdown">
	      <a class="dropdown-toggle" data-toggle="dropdown" href="#" role="button" aria-expanded="false">
		Tags <span class="caret"></span>
	      </a>
	      <ul class="dropdown-menu" role="menu">
		<% 
		def allTags = [] as HashSet
		published_content.each { pc->
		  if( pc.tags ){ allTags.addAll(pc.tags) }
		} 
		
		allTags.sort().each { t-> %>
		  <li><a href="/tags/${t}.html">${t}</a></li>
		<% } %>
		
	      </ul>
	    </li>
            
            <li><a href="/${config.feed_file}">Subscribe</a></li>
          </ul>
        </div><!--/.nav-collapse -->
      </div>
    </div>
    <div class="container">
    
