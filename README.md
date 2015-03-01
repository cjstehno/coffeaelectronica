# CoffeaElectronica

Live Site: http://coffeaelectronica.com

This project contains the content and examples for my technical blog.

# Building/Publishing

To generate the site content, run:

  gradle jbake

which will generate the site content into the `build/jbake` directory. Then, to publish the site, run:

  gradle publish

which will publish the generated content into the `gh-pages` branch and push to the site. Obviously, you will need permissions to push to the Git URL you have configured.

> This method of building the site using JBake via Gradle came from http://melix.github.io/blog/2014/02/hosting-jbake-github.html
