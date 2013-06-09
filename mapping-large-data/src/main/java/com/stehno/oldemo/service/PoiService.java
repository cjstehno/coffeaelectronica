package com.stehno.oldemo.service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.stehno.oldemo.dto.PointOfInterest;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.stat.clustering.Cluster;
import org.apache.commons.math3.stat.clustering.EuclideanDoublePoint;
import org.apache.commons.math3.stat.clustering.KMeansPlusPlusClusterer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Provides access to points of interest stored in pre-built data file.
 *
 * This will load the data from the class path (/data.ser).
 */
@Service
public class PoiService {

    private static final Logger log = LogManager.getLogger(PoiService.class);
    private static final PoiToEuclidean POI_TO_EUCLIDEAN = new PoiToEuclidean();
    private static final int CLUSTERING_ITERATIONS = 5;
    private static final int CLUSTER_COUNT = 200;
    private static final EuclideanToPoi EUCLIDEAN_TO_POI = new EuclideanToPoi();

    private final Random random = new Random();
    private final ClassPathResource dataFile = new ClassPathResource("/data.ser");
    private List<PointOfInterest> points = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init(){
        try ( final InputStream inputStream = dataFile.getInputStream() ){
            final StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            points = (List<PointOfInterest>)SerializationUtils.deserialize(inputStream);

            stopWatch.stop();
            System.out.printf("Load-time: %d ms%n", stopWatch.getTime());

            log.info("Loaded {} points of interest...", points.size());

        } catch (IOException ioe ){
            log.fatal("Unable to load database: {}", ioe.getMessage(), ioe);
        }
    }

    /**
     * Retrieves all items in the data file.
     * @return
     */
    public PointOfInterest[] fetchAll(){
        return points.toArray(new PointOfInterest[points.size()]);
    }

    /**
     * Retrieves all items contained withing the location bounds.
     *
     * @param left
     * @param bottom
     * @param right
     * @param top
     * @return
     */
    public PointOfInterest[] fetchByBoundingBox( final double left, final double bottom, final double right, final double top ){
        final List<PointOfInterest> results = new LinkedList<>();

        for( final PointOfInterest poi: points ){
            if( poi.getLatitude() < top && poi.getLatitude() > bottom && poi.getLongitude() < right && poi.getLongitude() > left ){
                results.add(poi);
            }
        }

        return results.toArray(new PointOfInterest[results.size()]);
    }

    /**
     * Calculates the clusters for all points in the data file and caches the results after the first call.
     * Uses K-Means clustering algorithm to calculate clusters.
     * @return
     */
    @Cacheable("clusters")
    public PointOfInterest[] fetchClusters(){
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final KMeansPlusPlusClusterer<EuclideanDoublePoint> clusterer = new KMeansPlusPlusClusterer<>(random);

        final List<Cluster<EuclideanDoublePoint>> clusters = clusterer.cluster(
            Lists.transform(points, POI_TO_EUCLIDEAN),
            CLUSTER_COUNT,
            CLUSTERING_ITERATIONS
        );

        final Collection<PointOfInterest> poiClusters = Lists.transform(clusters, EUCLIDEAN_TO_POI);

        stopWatch.stop();

        System.out.printf("Fetch-time: %d ms%n", stopWatch.getTime());

        return poiClusters.toArray(new PointOfInterest[poiClusters.size()]);
    }

    private static class PoiToEuclidean implements Function<PointOfInterest, EuclideanDoublePoint> {
        @Override
        public EuclideanDoublePoint apply(PointOfInterest poi) {
            return new EuclideanDoublePoint(new double[]{ poi.getLongitude(), poi.getLatitude() });
        }
    }

    private static class EuclideanToPoi implements Function<Cluster<EuclideanDoublePoint>, PointOfInterest> {
        @Override
        public PointOfInterest apply(Cluster<EuclideanDoublePoint> cluster) {
            final int count = cluster.getPoints().size();

            final PointOfInterest poi = new PointOfInterest();
            poi.setName(String.format("Cluster of %d", count != 0 ? count : 1));
            poi.setLongitude(cluster.getCenter().getPoint()[0]);
            poi.setLatitude(cluster.getCenter().getPoint()[1]);

            return poi;
        }
    }
}
