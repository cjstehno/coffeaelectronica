package com.stehno.oldemo.controller;

import com.stehno.oldemo.dto.PointOfInterest;
import com.stehno.oldemo.service.PoiService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Simple controller for serving up a data set for "points of interest".
 */
@Controller
@RequestMapping(value="/poi", consumes=MediaType.APPLICATION_JSON_VALUE, produces= MediaType.APPLICATION_JSON_VALUE)
public class PoiController {

    private static final Logger log = LogManager.getLogger(PoiController.class);
    private static final int ZOOM_THRESHOLD = 8;

    @Autowired private PoiService poiService;

    /**
     * Retrieves all of the data in the database each time it's called.
     *
     * @return a response entity containing an array of all PointOfInterest objects
     */
    @RequestMapping(value="/v1/fetch", method=RequestMethod.GET)
    public ResponseEntity<PointOfInterest[]> fetch(){
        final PointOfInterest[] pointsOfInterest = poiService.fetchAll();

        log.info("[v1]: Responding with {} points of interest.", pointsOfInterest.length);

        return new ResponseEntity<>(pointsOfInterest, HttpStatus.OK );
    }

    /**
     *  Retrieves all of the points of interest within the given bounds.
     *
     * @param bounds ( left, bottom, right, top)
     * @return a response entity containing an array of all PointOfInterest objects inside the given bounds
     */
    @RequestMapping(value="/v2/fetch/{bounds}", method=RequestMethod.GET)
    public ResponseEntity<PointOfInterest[]> fetchWithin( @PathVariable final String bounds ){
        final PointOfInterest[] pointsOfInterest = fetchBounded(bounds);

        log.info("[v2]: Responding with {} points of interest for bounds ({})", pointsOfInterest.length, bounds);

        return new ResponseEntity<>(pointsOfInterest, HttpStatus.OK );
    }

    /**
     *  Retrieves all of the points of interest based on the given zoom and bounding box.
     *
     *  If the zoom is less than the configured threshold, it will return a cached cluster view of all data points
     *  regardless of bounds. The results are cached after the first call.
     *
     *  If the zoom is greater than the configured threshold, it will return a view of the data within the given bounds.
     *
     * @param bounds ( left, bottom, right, top)
     * @param zoom the zoom level
     * @return
     */
    @RequestMapping(value="/v3/fetch/{bounds}/{zoom}", method=RequestMethod.GET)
    public ResponseEntity<PointOfInterest[]> fetchWithin( @PathVariable final String bounds, @PathVariable final int zoom ){
        final PointOfInterest[] pointsOfInterest;
        if( zoom < ZOOM_THRESHOLD ){
            pointsOfInterest = poiService.fetchClusters();

        } else {
            pointsOfInterest = fetchBounded(bounds);
        }

        log.info("[v3]: Responding with {} points of interest for bounds ({}) @ zoom {}", pointsOfInterest.length, bounds, zoom);

        return new ResponseEntity<>(pointsOfInterest, HttpStatus.OK );
    }

    private PointOfInterest[] fetchBounded( final String bounds ){
        final double[] box = box(bounds);
        return poiService.fetchByBoundingBox( box[0], box[1], box[2], box[3] );
    }

    private double[] box( final String bounds ){
        final double[] box = new double[4];

        final String[] sides = bounds.split(",");
        for( int i=0; i<4; i++ ){
            box[i] = Double.parseDouble( sides[i].trim() );
        }

        return box;
    }
}
