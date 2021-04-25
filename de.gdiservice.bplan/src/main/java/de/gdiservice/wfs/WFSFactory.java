package de.gdiservice.wfs;

import java.io.IOException;

import org.opengis.feature.simple.SimpleFeature;

public interface WFSFactory<T> {
	
	T build(SimpleFeature f) throws IOException;

}
