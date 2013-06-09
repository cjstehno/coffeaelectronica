package com.stehno.oldemo.tools;

import com.stehno.oldemo.dto.PointOfInterest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Used to generate a random data set with points focused around major world landmasses.
 *
 * The generated file should be src/main/resources/data.ser before so that it will be available on the classpath at
 * runtime.
 *
 * Usage:
 *
 * args[0] - number of items to generate
 * args[1] - file path to save data file
 * args[2] - number of region data sets to use (default is all if not specified)
 */
public class DataGenerator {

    private static final Random random = new Random();
    private static final String POINT_PREFIX = "Point-%d";

    private static final List<LonLat[]> regions = new ArrayList<>();

    private static void populateRegions( int num ){
        // min, max
        switch (num){
            case 5:
                regions.add(new LonLat[]{ new LonLat( -11.0, -32.0), new LonLat( 46.0,  34.0) });
            case 4:
                regions.add(new LonLat[]{ new LonLat( 114.0, -39.0), new LonLat(154.0, -10.0) });
            case 3:
                regions.add(new LonLat[]{ new LonLat(  -9.0,  15.0), new LonLat(144.0,  67.0) });
            case 2:
                regions.add(new LonLat[]{ new LonLat(-128.0,  14.0), new LonLat(-70.0,  62.0) });
            default:
                regions.add(new LonLat[]{ new LonLat( -81.0, -54.0), new LonLat(-38.0,   5.0) });
        }
    }

    public static void main( final String[] args ) throws IOException {
        final int count = Integer.valueOf(args[0]);
        final File file = new File(args[1]);

        populateRegions( args.length > 2 ? Integer.parseInt(args[2]) : 5 );

        final ArrayList<PointOfInterest> dataPoints = new ArrayList<>(count);

        for( int i=0; i<count; i++){
            final LonLat randPoint = rand();
            final PointOfInterest point = new PointOfInterest();
            point.setName(String.format(POINT_PREFIX, i));
            point.setLongitude( randPoint.lon );
            point.setLatitude( randPoint.lat );
            dataPoints.add(point);
        }

        final byte[] data = SerializationUtils.serialize(dataPoints);

        FileUtils.writeByteArrayToFile(file, data);
    }

    private static LonLat randWithin( final LonLat min, final LonLat max ){
        return new LonLat(
            randBetween( min.lon, max.lon ),
            randBetween( min.lat, max.lat )
        );
    }

    private static double randBetween( double min, double max ){
        // ints acceptable since we just want the general area
        return random.nextInt( (int)max - (int)min + 1 ) + min;
    }

    private static LonLat rand(){
        final LonLat[] region = regions.get( random.nextInt(regions.size()) );
        return randWithin( region[0], region[1] );
    }

    private static class LonLat {
        double lon, lat;

        LonLat( double lon, double lat ){
            this.lon = lon;
            this.lat = lat;
        }
    }
}