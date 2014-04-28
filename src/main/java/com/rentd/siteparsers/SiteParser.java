package com.rentd.siteparsers;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import org.neo4j.graphdb.GraphDatabaseService;

import com.rentd.datamodel.DataPiece;

public interface SiteParser extends Runnable {
	void setDataToSave(BlockingQueue<DataPiece> dataToSave);
	int getPagesCount() throws Exception ;
}
