title=Mapping Large Data Sets
date=2013-06-09
type=post
tags=blog,javascript
status=published
~~~~~~
Recently, I was tasked to resolve some performance issues related to displaying a large set of geo-location data on a map. Basically, the existing implementation was taking the simple approach of fetching all the location data from the server and rendering it on the map. While, there is nothing inherently wrong with this approach, it does not scale well as the number of data points increases, which was the problem at hand. 

The map needed to be able to render equally well whether there were 100 data points or a million. With this direct approach, the browser started to bog down at just over a thousand points, and failed completely at 100-thousand. A million was out of the question. So, what can be done?

I have created a small demo application to help present the concepts and techniques I used in solving this problem. I intend to focus mostly on the concepts and keep the discussion of the code to a minimum. This will not really be much of an OpenLayers tutorial unless you are faced with a similar task. See the sidebar for more information about how to setup and run the application - it's only necessary if you want to run the demo yourself.

> The demo application is available on [GitHub](https://github.com/cjstehno/coffeaelectronica/tree/master/mapping-large-data) and its README file contains the information you need to build and run it.

First, let's look at the problem itself. If you fire up the demo "V1" with a data set of 10k or less, you will see something like the following:

![V1 View](https://raw.github.com/cjstehno/coffeaelectronica/master/mapping-large-data/src/main/webapp/img/v1_view.png)

You can see that even with only ten thousand data points it is visually cluttered and a bit sluggish to navigate. If you build a larger data set of 100k or better yet, a million data points and try to run the demo, at best it will take a long time, most likely it will crash your browser. This approach is just not practical for this volume of data.

The code for this version simply makes an ajax request to the data service to retrieve all the data points:

```javascript
$.ajax('poi/v1/fetch', { contentType:'application/json' }).done(function(data){
	updateMarkers(map, data);
});
```

 and then renders the markers for each data point on the map:

```javascript
function updateMarkers( map, data ){
	var layer = map.getLayersByName('Data')[0];

	var markers = $.map(data, function(item){
		return new OpenLayers.Feature.Vector(
			new OpenLayers.Geometry.Point(
				item.longitude, item.latitude
			).transform(PROJECTION_EXTERNAL, PROJECTION_INTERNAL),
			{ 
				item:item 
			},
			OpenLayers.Util.applyDefaults(
				{ fillColor:'#0000ff' }, 
				OpenLayers.Feature.Vector.style['default']
			)
		);
	});

	layer.addFeatures(markers);
}
```

What we really need to do is reduce the amount of data being processed without losing any visual information? The key is to consider the scope of your view. Other than at the lowest zoom levels (whole Earth view) you are only viewing a relatively limited part of the whole map, which means that only a sub-set of the data is visible at any given time. So why fetch it all from the server when it just adds unnecessary load on the JavaScript mapping library?

The answer is that you don't have to. If you listen to map view change events and fetch the data for only your current view by passing the view bounding box to your query, you can limit the data down to only what you currently see. The "V2" demo uses this approach to limit the volume of data.

```javascript
eventListeners:{
	moveend:function(){
		var bounds = map.getExtent().transform(PROJECTION_INTERNAL, PROJECTION_EXTERNAL).toString();

		$.ajax('poi/v2/fetch/' + bounds, { contentType:'application/json' }).done(function(data){
			updateMarkers(map, data);
		});
	}
}
```

The `updateMarkers()` function remains unchanged in this version.

Visually, this version of the application is the same; however, it will handle larger data sets with less pain. This approach increases the number of requests for data but will reduce the amount of data retrieved as the user zooms into their target area of interest.

This approach is still a bit flawed; this method works fine for cases where the user is zoomed in on a state or small country; however, it is still possible to view the whole large data set when your view is at the lower zoom levels (whole Earth). There is still more work to be done.

In order to reduce the number of data points when viewing the lower zoom levels, we need to consider how useful all this data really is. Considering the image from V1, which is still valid for V2, is there any use in rendering all of those data points? This is just random distributed data, but even real data would probably be as dense or even more so in areas around population centers which would only compound the problem. How can you clean up this display mess while also reducing the amount of data being sent, oh, and without any lose of useful information?

The first part of the answer is clustering (see [Cluster Analysis](http://en.wikipedia.org/wiki/Cluster_analysis)). We needed to group the data together in a meaningful way such that we present a representative point for a nearby group of points, otherwise known as a cluster. After some research and peer discussion, it was decided that the [K-Means Clustering Algorithm](http://en.wikipedia.org/wiki/K-means_clustering) was the approach for our needs, and the [Apache Commons - Math](http://commons.apache.org/proper/commons-math/) library provided a stable and generic implementation that would work well for our requirements. It is also what I have used here for this demo.

The clustering provides a means of generating a fixed-size data set representing the whole around a common center point. With this, you can limit your clustered data set down to something like 200, which can easily be displayed on the map, and will still provide an accurate representation of the location data.

Notice, though, I said that clustering was the first part of the answer... what is the second? Consider the effect of clustering on your data set as you zoom in from whole Earth view down to city street level. Clustering combined with view-bounds limiting will cause your overall data set to change. When the data points used in the cluster calculation change, the results change, which causes the location points to jump. I called this "jitter". Even just panning the map at a constant zoom level would cause map markers to move around like they were doing some sort of annoying square dance. To overcome the jittery cluster markers, you need to keep the data set used in the cluster calculation constant.

A hybrid approach is required. Basically, add the zoom level to the fetch request.

```javascript
eventListeners:{
	moveend:function(){
		var bounds = map.getExtent().transform(PROJECTION_INTERNAL, PROJECTION_EXTERNAL).toString();
		var zoom = map.getZoom();

		$.ajax('poi/v3/fetch/' + bounds + '/' + zoom, { contentType:'application/json' }).done(function(data){
			updateMarkers(map, data);
		});
	}
}
```

At the lower zoom levels, up to a configured threshold, you calculate the clusters across the whole data set (not bound by view) and cache this cluster data so that the calculation will only be done on the first call. Since zoom is not a function of this calculation, there can be one cached data set for all of the zoom levels below the specified threshold. Then, when the user zooms into the higher zoom levels (over the threshold), the actual data points (filtered by the view bounds) are returned by the fetch.

If you look at demo V3, you can see this in action, for 10-thousand points:

![V3 10k View](https://raw.github.com/cjstehno/coffeaelectronica/master/mapping-large-data/src/main/webapp/img/v3_10k.png)

And if you run the demo with a one-million point data set, you will see the same view. The initial load will take a bit longer but once loaded, it should perform nicely. What you may notice, though is that once you cross the clustered threshold you may suddenly get a large data set again... not overly so, but just more than you might expect. This is an area that you would want to tune to your specific needs so that you have a balance of when this change occurs to get the best perceived results.

You could stop here and be done with it, but depending on how your data is distributed you could still run into some overly-dense visual areas. Consider the case where you generate a million data points, but only in the Western Hemisphere.

If you build a one-million point data set for only the Americas, you can see that there are still some overly-dense areas even with the clustering. Since I am using [OpenLayers](http://openlayers.org/) as the mapping API, I can use their client-side clustering mechanism to help resolve this. With the client-side clustering enabled, the mapping API will groups markers together by distance to help de-clutter the view. If you look at V3 again, you can see the cluster clutter problem:

![V3 West](https://raw.github.com/cjstehno/coffeaelectronica/master/mapping-large-data/src/main/webapp/img/v3_west.png)

You can see that there are still some areas of high marker density. The client-side clustering strategy in OpenLayers can help relieve the clutter a bit:

```javascript
new OpenLayers.Layer.Vector('Data',{
	style: OpenLayers.Util.applyDefaults(
		{
			fillColor:'#00ff00'
		},
		OpenLayers.Feature.Vector.style['default']
	),
	strategies:[
		new OpenLayers.Strategy.Cluster({
			distance:50,
			threshold:3
		})
	]
})
```

as can be seen in V4:

![V4 West](https://raw.github.com/cjstehno/coffeaelectronica/master/mapping-large-data/src/main/webapp/img/v4_west.png)

But, it is more apparent when you zoom in:

![V4 West Zoom](https://raw.github.com/cjstehno/coffeaelectronica/master/mapping-large-data/src/main/webapp/img/v4_west_zoom.png)

You can see now that the green markers are client-side clusters and the blue markers are server-side points (clusters or single locations).

At the end of all that you have a map with client-side clustering to handle visual density at the local level. You have server-side clustering at more-global zoom levels, with caching to remove jitter and reduce calculation time and you have actual location points being served filtered by bounds. It seems like a lot of effort, but overall the code itself is fairly simple and straight-forward... and now we can support a million data points with no real issues or loss of information.

One thing I have not mentioned here is the use of GIS databases or extensions. My goal here was more conceptual, but should you be faced with this kind of problem, you should look into the GIS support for your data storage solution since being able to run queries directly on the bounding shape can be more efficient with GIS solutions in place.