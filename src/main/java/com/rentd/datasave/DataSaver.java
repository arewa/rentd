package com.rentd.datasave;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.UniqueFactory;
import org.neo4j.graphdb.index.UniqueFactory.UniqueNodeFactory;
import org.neo4j.helpers.collection.IteratorUtil;

import com.rentd.datamodel.DataPiece;
import com.rentd.datamodel.Offer;
import com.rentd.datamodel.Realtor;
import com.rentd.datamodel.RelTypes;

public class DataSaver implements Runnable {

	private BlockingQueue<DataPiece> dataToSave;
	private GraphDatabaseService db;
	private ExecutionEngine engine;

	public DataSaver(BlockingQueue<DataPiece> dataToSave,
			GraphDatabaseService db) {
		this.dataToSave = dataToSave;
		this.db = db;
	}

	@Override
	public void run() {

		engine = new ExecutionEngine(db);

		if (dataToSave == null) {
			return;
		}

		int writtenObjects = 0;

		try {

			do {

				if (dataToSave.isEmpty()) {
					// System.out.println("Waiting for data to save into DB...");
					Thread.sleep(1000);
				}

				DataPiece dp = dataToSave.take();

				final long start = System.nanoTime();

				// putIntoDB(dp.offer, dp.realtor);
				putIntoDB1(dp.offer, dp.realtor);

				writtenObjects++;

				if (writtenObjects % 100 == 0) {
					System.out
							.println("Total objects saved: " + writtenObjects);
				}

				final long end = System.nanoTime();

				final double timeSpent = (end - start) / 1.0e9;

				if (timeSpent > 5) {
					System.out.println("INSERT TIME " + timeSpent + "s");
					System.out.println(dp);
				}
			} while (true);

		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}
	}

	private void putIntoDB1(Offer o, Realtor r) {

		ExecutionResult result;
		Iterator<Node> res;
		Map<String, Object> params = new HashMap<String, Object>();
		String query;

		try (Transaction tx = db.beginTx()) {

			params.put("value", o.city);
			query = "MATCH (n:`city`) WHERE n.name = {value} RETURN n";
			result = engine.execute(query, params);

			Node city = null;

			res = result.columnAs("n");
			for (Node node : IteratorUtil.asIterable(res)) {
				city = node;
				break;
			}

			if (city == null) {
				city = db.createNode();
				city.addLabel(DynamicLabel.label("city"));
				city.setProperty("name", o.city);
			}

			params = new HashMap<String, Object>();
			params.put("value", r.uuid);
			query = "MATCH (n:`realtor`) WHERE n.ria_realt_id = {value} RETURN n";
			result = engine.execute(query, params);

			Node realtor = null;

			res = result.columnAs("n");
			for (Node node : IteratorUtil.asIterable(res)) {
				realtor = node;
				break;
			}

			if (realtor == null) {
				realtor = db.createNode();
				realtor.addLabel(DynamicLabel.label("realtor"));
				realtor.setProperty("ria_realt_id", r.uuid);
				realtor.setProperty("ria_realt_name", r.name);
				realtor.setProperty("ria_realt_url", r.url);
			}

			for (String p : o.phones) {
				params = new HashMap<String, Object>();
				params.put("value", p);
				query = "MATCH (n:`phone`) WHERE n.number = {value} RETURN n";
				result = engine.execute(query, params);

				Node phone = null;

				res = result.columnAs("n");
				for (Node node : IteratorUtil.asIterable(res)) {
					phone = node;
					break;
				}

				if (phone == null) {
					phone = db.createNode();
					phone.addLabel(DynamicLabel.label("phone"));
					phone.setProperty("number", p);
					phone.createRelationshipTo(city, RelTypes.LOCATES);
				}

				realtor.createRelationshipTo(phone, RelTypes.OWNS);
			}

			Node offer = db.createNode();
			offer.addLabel(DynamicLabel.label("offer"));
			offer.setProperty("ria_offer_title", o.title);
			offer.setProperty("ria_offer_price", o.price);
			offer.setProperty("ria_offer_url", o.url);
			offer.setProperty("ria_offer_descr", o.description);
			offer.setProperty("ria_offer_type", o.type);

			realtor.createRelationshipTo(offer, RelTypes.OWNS);

			tx.success();
		}
	}

//	private void putIntoDB(Offer o, Realtor r) {
//
//		long start = System.nanoTime();
//
//		UniqueNodeFactory cityFactory = getCityNodeFactory();
//		UniqueNodeFactory phoneFactory = getPhoneNodeFactory();
//		UniqueNodeFactory realtorFactory = getRealtorNodeFactory();
//		UniqueNodeFactory offerFactory = getOfferNodeFactory();
//
//		Node city;
//		Node realtor;
//		Node offer;
//
//		try (Transaction tx = db.beginTx()) {
//			city = cityFactory.getOrCreate("city", o.city);
//
//			tx.success();
//		}
//
//		long end = System.nanoTime();
//		double timeSpent = (end - start) / 1.0e9;
//		System.out.println("INSERT CITY TIME " + timeSpent + "s");
//		start = System.nanoTime();
//
//		try (Transaction tx = db.beginTx()) {
//			realtor = realtorFactory.getOrCreate("ria_realt_id", r.uuid);
//			realtor.setProperty("name", r.name);
//			realtor.setProperty("ria_realt_url", r.url);
//
//			tx.success();
//		}
//
//		end = System.nanoTime();
//		timeSpent = (end - start) / 1.0e9;
//		System.out.println("INSERT REALTOR TIME " + timeSpent + "s");
//		start = System.nanoTime();
//
//		try (Transaction tx = db.beginTx()) {
//			for (String p : o.phones) {
//				Node phone = phoneFactory.getOrCreate("phone", p);
//				phone.createRelationshipTo(city, RelTypes.LOCATES);
//				realtor.createRelationshipTo(phone, RelTypes.OWNS);
//			}
//
//			tx.success();
//		}
//
//		end = System.nanoTime();
//		timeSpent = (end - start) / 1.0e9;
//		System.out.println("INSERT PHONE TIME " + timeSpent + "s");
//		start = System.nanoTime();
//
//		try (Transaction tx = db.beginTx()) {
//			offer = offerFactory.getOrCreate("ria_offer_id", o.uuid);
//			offer.setProperty("ria_offer_title", o.title);
//			offer.setProperty("ria_offer_price", o.price);
//			offer.setProperty("ria_offer_url", o.url);
//			offer.setProperty("ria_offer_descr", o.description);
//			offer.setProperty("ria_offer_type", o.type);
//
//			realtor.createRelationshipTo(offer, RelTypes.OWNS);
//
//			tx.success();
//		}
//
//		end = System.nanoTime();
//		timeSpent = (end - start) / 1.0e9;
//		System.out.println("INSERT OFFER TIME " + timeSpent + "s");
//	}
//
//	private UniqueNodeFactory getCityNodeFactory() {
//		try (Transaction tx = db.beginTx();) {
//			UniqueFactory.UniqueNodeFactory result = new UniqueFactory.UniqueNodeFactory(
//					db, "cities") {
//				@Override
//				protected void initialize(Node created,
//						Map<String, Object> properties) {
//					created.addLabel(DynamicLabel.label("City"));
//					created.setProperty("city", properties.get("city"));
//				}
//			};
//			tx.success();
//			return result;
//		}
//	}
//
//	private UniqueNodeFactory getPhoneNodeFactory() {
//		try (Transaction tx = db.beginTx();) {
//			UniqueFactory.UniqueNodeFactory result = new UniqueFactory.UniqueNodeFactory(
//					db, "phones") {
//				@Override
//				protected void initialize(Node created,
//						Map<String, Object> properties) {
//					created.addLabel(DynamicLabel.label("Phone"));
//					created.setProperty("phone", properties.get("phone"));
//				}
//			};
//			tx.success();
//			return result;
//		}
//	}
//
//	private UniqueNodeFactory getRealtorNodeFactory() {
//		try (Transaction tx = db.beginTx();) {
//			UniqueFactory.UniqueNodeFactory result = new UniqueFactory.UniqueNodeFactory(
//					db, "realtors") {
//				@Override
//				protected void initialize(Node created,
//						Map<String, Object> properties) {
//					created.addLabel(DynamicLabel.label("Realtor"));
//					created.setProperty("ria_realt_id",
//							properties.get("ria_realt_id"));
//				}
//			};
//			tx.success();
//			return result;
//		}
//	}
//
//	private UniqueNodeFactory getOfferNodeFactory() {
//		try (Transaction tx = db.beginTx();) {
//			UniqueFactory.UniqueNodeFactory result = new UniqueFactory.UniqueNodeFactory(
//					db, "offers") {
//				@Override
//				protected void initialize(Node created,
//						Map<String, Object> properties) {
//					created.addLabel(DynamicLabel.label("Offer"));
//					created.setProperty("ria_offer_id",
//							properties.get("ria_offer_id"));
//				}
//			};
//			tx.success();
//			return result;
//		}
//	}
}
