package com.rentd;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.rentd.datamodel.DataPiece;
import com.rentd.datasave.DataSaver;
import com.rentd.siteparsers.SiteParser;
import com.rentd.siteparsers.ria.RiaUaAppartments;

public class Main {

	private static final String DB_PATH			= 	"D:\\tmp\\rentdb";

	public static void main(String[] args) throws Exception {

		final long start = System.nanoTime();

		GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabase(DB_PATH);
		
		BlockingQueue<DataPiece> dataToSave = new ArrayBlockingQueue<DataPiece>(1024);
		
		registerShutdownHook(graphDb);

		final int numberOfCores = Runtime.getRuntime().availableProcessors();
		final double blockingCoefficient = 0.9;
		final int poolSize = (int) (numberOfCores / (1 - blockingCoefficient));
		
		Thread thread = new Thread(new DataSaver(dataToSave, graphDb));
		thread.start();

		System.out.println("Number of Cores available is " + numberOfCores);
		System.out.println("Pool size is " + poolSize);
		
		BlockingQueue<Runnable> parsersThreadQueue = new ArrayBlockingQueue<Runnable>(poolSize);
		ThreadPoolExecutor ex = new ThreadPoolExecutor(4, poolSize, 20, TimeUnit.SECONDS, parsersThreadQueue, new ThreadPoolExecutor.CallerRunsPolicy());
		
		SiteParser sp = new RiaUaAppartments(1);
		int totalPages = sp.getPagesCount();
		System.out.println("Found " + totalPages + " pages of Ria Ua Appartments");
		
		StringBuffer logMes;
		
		for (int pageNum = 1; pageNum <= totalPages; pageNum ++) {
			SiteParser riaUa = new RiaUaAppartments(pageNum);
			riaUa.setDataToSave(dataToSave);

			ex.execute(riaUa);
			
			logMes = new StringBuffer();
			logMes.append(new Date().toString());
			logMes.append(" added new command to execute. Page number is ");
			logMes.append(pageNum);
			
			System.out.println(logMes.toString());
		}
		
		ex.shutdown();

		final long end = System.nanoTime();

		final double timeSpent = (end - start) / 1.0e9;
		System.out.println("FINISHED. Time spent " + timeSpent + "s");
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}
}
