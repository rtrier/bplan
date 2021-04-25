package de.gdiservice.wfs;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xsd.Parser;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WFSClient {

    final static Logger logger = LoggerFactory.getLogger(WFSClient.class);

    public static <T> List<T> read(String sUrl, WFSFactory<T> factory) throws Exception {
        
        logger.info(String.format("WFSClient reading \"%s\"", sUrl));

        List<T> list = new ArrayList<>();

        URL url = new URL(sUrl);

        InputStream in = url.openStream();

        GMLConfiguration gml = new GMLConfiguration();
        Parser parser = new Parser(gml);
        parser.setStrict(false);

        FeatureCollection<?,?> features = (FeatureCollection<?,?>) parser.parse(in);
        FeatureIterator<?> it = features.features();

        while (it.hasNext()) {
            SimpleFeature f = (SimpleFeature) it.next();
            list.add(factory.build(f));
        }
        return list;

    }  

}
