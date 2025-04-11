package de.gdiservice.wfs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.emf.common.util.EList;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xsd.Configuration;
import org.geotools.xsd.Parser;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import de.gdiservice.bplan.ImportLogger;
import net.opengis.wfs20.impl.SimpleFeatureCollectionTypeImpl;


public class WFSClient {

    final static Logger logger = LoggerFactory.getLogger(WFSClient.class);
    
    
    static <T> List<T> readFeatureCollection(FeatureCollection<?,?> features, WFSFactory<T> factory, ImportLogger importLogger ) {
        
        List<T> list = new ArrayList<>();
        FeatureIterator<?> it = features.features();

        
        while (it.hasNext()) {
            final Object o = it.next();
            if (o instanceof SimpleFeature) {
                SimpleFeature f = (SimpleFeature) o;
                try {
                    list.add(factory.build(f));
                } catch (Exception e) {
                    if (importLogger!=null) {
                        Object gmlId = f.getAttribute("gml_id");
                        Object name = f.getAttribute("name");
                        if (gmlId==null) {
                            logger.error("SimpleFeature with name=\"" + name + "\" has an error.", e);
                            importLogger.addError("SimpleFeature with name=\"" + name + "\" has an error: "+e.getMessage());
                        } else {
                            logger.error("SimpleFeature with gmlId=\"" + gmlId + "\" has an error.", e);
                            importLogger.addError("SimpleFeature with gmlId=\"" + gmlId + "\" has an error: "+e.getMessage());
                        }
                    }
                }
            } else {
                if (importLogger!=null) {
                    importLogger.addError("Object is not a SimpleFeature");
                }
                else {
                    logger.error("Object is not a SimpleFeature");
                }
            }
        }
        return list;
    }
    
    public static <T> List<T> read(String sUrl, WFSFactory<T> factory, ImportLogger importLogger) throws Exception {
        logger.info(String.format("WFSClient reading \"%s\"", sUrl));
        URL url = new URL(sUrl);
        return read(url, factory, importLogger);
    }

    public static <T> List<T> read(URL url, WFSFactory<T> factory, ImportLogger importLogger) throws IOException, SAXException, ParserConfigurationException  {
        
        logger.info(String.format("WFSClient reading URL \"%s\"", url));
        List<T> list = new ArrayList<>();
        
        InputStream in = url.openStream();

//        Configuration gml = (factory instanceof GeolexBPlanFactory) ? new org.geotools.wfs.v2_0.WFSConfiguration() : new GMLConfiguration();
        Configuration gml = (url.getQuery().indexOf("2.0.0")>0) ? new org.geotools.wfs.v2_0.WFSConfiguration() : new GMLConfiguration();
        Parser parser = new Parser(gml);
        parser.setStrict(false);

        Object parserResult = parser.parse(in);
        if (parserResult instanceof FeatureCollection<?, ?>) {
            FeatureCollection<?,?> features = (FeatureCollection<?,?>) parserResult;
            list = readFeatureCollection(features, factory, importLogger);
        } else if (parserResult instanceof SimpleFeatureCollectionTypeImpl) {
            SimpleFeatureCollectionTypeImpl r = (SimpleFeatureCollectionTypeImpl)parserResult;
            @SuppressWarnings("rawtypes")
            EList<FeatureCollection> elist = r.getMember();            
            for (int i=0; i<elist.size(); i++) {
                logger.debug("elist "+i+"  "+elist.get(i));
                org.geotools.feature.DefaultFeatureCollection col = (org.geotools.feature.DefaultFeatureCollection)elist.get(i);
                list = readFeatureCollection(col, factory, importLogger);
            }
            
        }
        return list;

    }  

}
