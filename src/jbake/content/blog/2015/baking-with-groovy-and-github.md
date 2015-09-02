title=Baking Your Blog with JBake, Groovy and GitHub
date=2015-09-02
type=post
tags=blog,groovy
status=published-date
~~~~~~

As a developer, it has always bugged me to have my blog or web site content stored on a server managed by someone else, outside of my control. Granted, WordPress and the like are very stable and generally have means of pulling out your data if you need it, but I really just like to have my content under my own control. Likewise, I have other projects I want to work on, so building content management software is not really on my radar at this point; that's where [JBake](http://jbake.org) comes in.

JBake is a simple JVM-based static site generation tool that makes casual blogging quite simple once you get everything set up. It's a bit of a raw project at this point, so there are a few rough edges to work with, but I will help to file them down in the discussions below.

Getting started with JBake, you have a couple options. You can install JBake locally and use it as a command line tool, or you can use the [JBake Gradle Plugin](https://github.com/jbake-org/jbake-gradle-plugin). The Gradle plugin is currently lacking the local server feature provided by the command line tools; however, it does provide a more portable development environment along with the universe of other Gradle plugins. We will use the Gradle plugin approach here and I will provide some workarounds for the missing features to bring the functionality back on even ground with the command line tool.

The first thing we need is our base project and for that I am going to use a [Lazybones](https://github.com/pledbrook/lazybones) template that I have created (which may be found in my [lazybones-templates](https://github.com/cjstehno/lazybones-templates) repository). You can use the Gradle plugin and do all the setup yourself, but it was fairly simple and having a template for it allowed me to add in the missing features we need.

> If you are unfamiliar with Lazybones, it's a Groovy-based project template framework along the lines of Yeoman and the old Maven Archetype plugin. Details for adding my template repo to your configuration can be found on the [README page](https://github.com/cjstehno/lazybones-templates/blob/master/README.md) for my templates.

Create the empty project with the following:

    lazybones create jbake-groovy cookies
    
where "cookies" is the name of our project and the name of the project directory to be created. You will be asked a few questions related to template generation. You should have something similar to the following:

    $ lazybones create jbake-groovy cookies
    Creating project from template jbake-groovy (latest) in 'cookies'
    Define value for 'JBake Plugin Version' [0.2]:
    Define value for 'JBake Version' [2.3.2]:
    Define value for 'Gradle version' [2.3]:
    GitHub project: [username/projectname.git]: cjstehno/cookies.git

The "username" should reflect the username of your GitHub account, we'll see what this is used for later. If you look at the generated "cookies" directory now you will see a standard-looking Gradle project structure. The JBake source files reside in the `src/jbake` directory with the following sub-directories:

* assets - this is where your static assets live (CSS files, JavaScript files, images, etc)
* templates - this is where your GSP page templates live
* content - this is where your site content lives (will be applied to the templates)

You will see that by default, a simple Bootstrap-based blog site is provided with sample blog posts in HTML, ASCII Doc, and Markdown formats. This is the same sample content as provided by the command line version of the project setup tool. At this point we can build the sample content using:

    ./gradlew jbake
    
The Gradle plugin does _not_ provide a means of serving up the "baked" content yet. There is work in progress so hopefully this will be merged in soon. One of the goodies my template provides is a simple Groovy web server script. This allows you to serve up the content with:

    groovy serve.groovy

which will start a Jetty instance pointed at the content in `build/jbake` on the configured port (8080 by default, which can be changed by adding a port number to the command line). Now when you hit `http://localhost:8080/` you should see the sample content. Also, you can leave this server running in a separate console while you develop, running the jbake command as needed to rebuild the content.

First, let's update the general site information. Our site's title is not "JBake", so let's change it to "JCookies" by updating it in the `src/jbake/templates/header.gsp` and `src/jbake/templates/menu.gsp` files. While we're in there we can also update the site meta information as well:

```jsp
<title><%if (content.title) {%>${content.title}<% } else { %>JCookies<% }%></title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="A site about cookies.">
<meta name="author" content="Chris Stehno">
<meta name="keywords" content="cookies,baking">
<meta name="generator" content="JBake">
```

Then to apply the changes, run `./gradlew jbake` and refresh the browser. Now we see our correct site name.

> Note that JBake makes no requirements about the templates or content to be used. It provides special support for blog-style sites; however, you can remove all the content and make a standard simple static site if you wish.

Let's add a new blog entry. The blog entries are stored in the `src/jbake/content/blog` directory by year so we need to create a new directory for `2015`. Content may be written in HTML, ASCII Doc, or Markdown, based on the file extension. I am a fan of Markdown so we'll use that for our new blog entry. Let's create an entry file named `chocolate-chip.md`.

JBake uses a custom header block at the top of content files to store meta information. For our entry we will use the following:

    title=Chocolate Chip Cookies
    date=2015-05-04
    type=post
    tags=blog,recipe
    status=published
    ~~~~~~

The `title` and `date` are self-explanatory. The `type` can be `post` or `page` to denote a blog post or a standard page. The `tags` are used to provide extra tag information to categorize the content. The `status` field may be `draft` or `published` to denote whether or not the content should be included in the rendered site. Everything below the line of tildes is your standard markdown content.

For the content of our entry we are going to use the [Nestle Chocolate Chip Cookie recipe](https://www.verybestbaking.com/recipes/18476/original-nestle-toll-house-chocolate-chip-cookies/) - it gives us a nice overview of the content capabilities, and they are yummy!

The content in Markdown format, is as follows:

```
## Ingredients

* 2 1/4 cups all-purpose flour
* 1 teaspoon baking soda
* 1 teaspoon salt
* 1 cup (2 sticks) butter, softened
* 3/4 cup granulated sugar
* 3/4 cup packed brown sugar
* 1 teaspoon vanilla extract
* 2 large eggs
* 2 cups (12-oz. pkg.) NESTLÉ® TOLL HOUSE® Semi-Sweet Chocolate Morsels
* 1 cup chopped nuts

## Instructions

1. Preheat oven to 375° F.
1. Combine flour, baking soda and salt in small bowl. Beat butter, granulated sugar, brown sugar and vanilla extract in large mixer bowl until creamy. Add eggs, one at a time, beating well after each addition. Gradually beat in flour mixture. Stir in morsels and nuts. Drop by rounded tablespoon onto ungreased baking sheets. 
1. BAKE for 9 to 11 minutes or until golden brown. Cool on baking sheets for 2 minutes; remove to wire racks to cool completely. 

May be stored in refrigerator for up to 1 week or in freezer for up to 8 weeks.
```

Rebuild/refresh and now you see we have a new blog post. Now, since we <strike>stole</strike>borrowed this recipe from another site, we should provide an attribution link back to the original source. The content header fields are dynamic; you can create your own and use them in your pages. Let's add an `attribution` field and put our link in it.

```
attribution=https://www.verybestbaking.com/recipes/18476/original-nestle-toll-house-chocolate-chip-cookies/
```

Then we will want to add it to our rendered page, so we need to open up the blog entry template, the `src/jbake/templates/post.gsp` file and add the following line after the page header:

```html
<p>Borrowed from: <a href="${content.attribution}">${content.attribution}</a></p>
```

Notice now, that the templates are just GSP files which may have Groovy code embedded into them in order to perform rendering logic. The header data is accessible via the `content` object in the page.

This post is kind of boring at this point. Yes, it's a recipe for chocolate chip cookies, and that's hard to beat, but the page full of text is not selling it to me. Let's add a photo to really make your mouth water. Grab an image of your favorite chocolate chip cookies and save it in `src/jbake/assets/images` as `cookies.jpg`. Static content like images live in the `assets` folder. The contents of the assets folder will be copied into the root of the rendered site directory.

Now, we need to add the photo to the page. Markdown allows simple HTML tags to be used so we can add:

```html
<img src="/images/cookies.jpg" style="width:300px;float:right;"/>
```

to the top of our blog post content, which will add the image at the top of the page, floated to the right of the main content text. Now that looks tasty!

You can also create tandard pages in a similar manner to blog posts; however, they are based on the `page.gsp` template. This allows for different contextual formatting for each content type.

You can customize any of the templates to get the desired content and functionality for your static site, but what about the overall visual theme? As I mentioned earlier, the default templates use the Twitter Bootstrap library and there are quite a few resources available for changing the theme to fit your needs and they range from free to somewhat expensive. We just want a free one for demonstration purposes so let's download the `bootstrap.min.css` file for the [Bootswatch Cerulean](https://bootswatch.com/cerulean/) theme. Overwrite the existing theme in the `src/jbake/assets/css` directory with this new file then rebuild the site and refresh your browser. Now you can see that we have a nice blue banner along with other style changes.

The end result at this point will look something like this:

<div style="text-align:center;margin-bottom:15px;"><img src="/images/cookiesblog.png"/></div>

All-in-all not too bad for a few minutes of coding work!

Another nice feature of JBake is delayed publishing. The `status` field in the content header has three accepted values:

* published - the content will be rendered and listed in the list of available pages.
* draft - the content will be rendered, but with a "-draft" suffix added to the file name. The content will not appear in the list of available pages.
* published-date - the content will be rendered normally, but will not appear in the list of available pages until the site is built after the value of its `date` field has passed.

We used the `published` option since we wanted our content to be available right away. You could easily create a bunch of blog entries ahead of time, specifying the `date` values for when they should be published but having the `status` values set to `published-date` so that they are released only after the appropriate date. The downside of this is that since JBake is a static generator, you would have to be sure and build the site often enough to pick up the newly available content - maybe with a nightly scheduled build and deployment job.

When you are ready to release your site out into the greater internet wilderness, you will need a way to publish it; this is another place where my lazybones template comes in handy. If you are hosting your site as [github-pages](https://pages.github.com/), the template comes with a publishing task built-in, based on the gradle-git plugin. This is where the GitHub username and repository information from the initial project creation comes into play. For this to work, you need a repository named "cookies" associated with your GitHub account. You will also want to double check that the repo clone URL is correct in the `publish.gradle` file. Then, to publish your site you simply run:

    ./gradlew publish
    
and then go check your project site for the updated content (sometimes it takes a minute or two, though it's usually instantaneous).

At this point we have a easily managed static web site; what's left to be done? Well, you could associate it with your own custom domain name rather than the one GitHub provides. I will not go into that here, since I really don't want to purchase a domain name just for this demo; however, I do have a blog post ([Custom GitHub Hosting](http://coffeaelectronica.com/blog/2015/custom-github-hosting.html)) that goes into how it's done (at least on GoDaddy).

JBake and GitHub with a dash of Groovy provide a nice environment for quick custom blogs and web sites, with little fuss. Everything I have shown here is what I use to create and manage this blog, so, I'd say it works pretty well.

> Portions of this discussion are based on a blog post by Cédric Champeau, "[Authoring your blog on GitHub with JBake and Gradle](http://melix.github.io/blog/2014/02/hosting-jbake-github.html)", who is also a contributor to JBake (among other things). 
