package de.gdiservice.wfs;

import java.io.IOException;
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

import de.gdiservice.bplan.ImportLogger;


public class WFSClient {

    final static Logger logger = LoggerFactory.getLogger(WFSClient.class);

    public static <T> List<T> read(String sUrl, WFSFactory<T> factory, ImportLogger importLogger) throws Exception {
        
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
            final Object o = it.next();
            if (o instanceof SimpleFeature) {
                SimpleFeature f = (SimpleFeature) o;
                try {
                    list.add(factory.build(f));
                } catch (IOException e) {
                    e.printStackTrace();
                    if (importLogger!=null) {
                        importLogger.addError("SimpleFeature has an error: "+e.getMessage());
                    }
                }
            } else {
                if (importLogger!=null) {
                    importLogger.addError("Object is not a SimpleFeature");
                }
                else {
                    System.err.println("Object is not a SimpleFeature");
                }
            }
        }
        return list;

    }  

}
