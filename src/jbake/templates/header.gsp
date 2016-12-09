<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>CoffeaElectronica.com</title>

    <meta name="description" content="A technical blog.">
    <meta name="author" content="Christopher J. Stehno">
    <meta name="keywords" content="java,groovy,blog">
    <meta name="generator" content="JBake">

    <link href="/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/asciidoctor.css" rel="stylesheet">
    <link href="/css/base.css" rel="stylesheet">
    <link href="/css/prettify.css" rel="stylesheet">

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->

    <link rel="shortcut icon" href="/favicon.ico">
</head>

<body onload="prettyPrint()">

<div class="container-fluid">

    <div class="row">
        <div class="col-lg-12 col-md-12 col-sm-12">
            <img src="/images/coffee-banner.jpg" class="img-responsive" />
        </div>
    </div>

    <nav class="navbar navbar-inverse" style="margin-bottom: 2px;">
        <div class="container-fluid">
            <!-- Brand and toggle get grouped for better mobile display -->
            <div class="navbar-header">
                <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" href="/index.html">&nbsp;CoffeaElectronica</a>
            </div>

            <!-- Collect the nav links, forms, and other content for toggling -->
            <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
                <ul class="nav navbar-nav">
                    <li><a href="/archive.html" title="Archives"><span class="glyphicon glyphicon-calendar"></span></a></li>
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" title="Tags"><span class="glyphicon glyphicon-tags"></span> <span class="caret"></span></a>
                        <ul class="dropdown-menu">
                            <%  def allTags = [] as HashSet
                                published_content.each { pc->
                                    if( pc.tags ){ allTags.addAll(pc.tags) }
                                }
                                allTags.sort().each { t-> %>
                                    <li><a href="/tags/${t}.html">${t}</a></li>
                            <% } %>
                        </ul>
                    </li>
                </ul>

                <ul class="nav navbar-nav navbar-right">
                    <li><a href="http://stehno.com" title="Web Site" target="_blank"><span class="glyphicon glyphicon-user"></span></a></li>
                    <li><a href="http://github.com/cjstehno" title="Projects" target="_blank"><span class="glyphicon glyphicon-wrench"></span></a></li>
                    <li><a href="/${config.feed_file}" title="Feed"><span class="glyphicon glyphicon-bullhorn"></span></a></li>
                </ul>
            </div><!-- /.navbar-collapse -->
        </div><!-- /.container-fluid -->
    </nav>
