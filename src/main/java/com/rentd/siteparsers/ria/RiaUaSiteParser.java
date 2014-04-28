package com.rentd.siteparsers.ria;

import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.UniqueFactory;
import org.neo4j.graphdb.index.UniqueFactory.UniqueNodeFactory;

import com.rentd.datamodel.DataPiece;
import com.rentd.datamodel.Offer;
import com.rentd.datamodel.Realtor;
import com.rentd.datamodel.RelTypes;
import com.rentd.siteparsers.SiteParser;
import com.rentd.utils.Utils;

public abstract class RiaUaSiteParser implements SiteParser {

	public final static String SITE_ROOT_URL = "http://dom.ria.com";
	public final static String OFFERS_PAGE_URL_TEMPLATE = "/ru/search/?state_id=0&language=ru&view=simple&limit=10&page=";
	public final static String ID_PREF = "ria";

	protected int category;
	protected int pageNum = 0;
	private BlockingQueue<DataPiece> dataToSave;

	public RiaUaSiteParser(int pageNum) {
		this.pageNum = pageNum;
	}
	
	@Override
	public void run() {
		try {
			doParse();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setDataToSave(BlockingQueue<DataPiece> dataToSave) {
		this.dataToSave = dataToSave;
	}

	private String getOffersPageUrlTemplate() {
		StringBuffer workingUrl = new StringBuffer();
		workingUrl.append(SITE_ROOT_URL);
		workingUrl.append(OFFERS_PAGE_URL_TEMPLATE);
		workingUrl.append(pageNum);
		workingUrl.append("&category=");
		workingUrl.append(getCategory());

		return workingUrl.toString();
	}

	protected abstract int getCategory();

	protected abstract String getType();

	public int getPagesCount() throws Exception {
		HtmlCleaner htmlCleaner = new HtmlCleaner();
		TagNode rootNode = htmlCleaner
				.clean(new URL(getOffersPageUrlTemplate()));

		int pages = 0;
		Object[] els = rootNode
				.evaluateXPath("//div[@class='page']/a[@class='item'][last()]");
		for (Object e : els) {
			if (e instanceof TagNode) {
				TagNode t = (TagNode) e;
				pages = Integer.valueOf(t.getText().toString()).intValue();
			}
		}

		return pages;
	}

	public void doParse() throws Exception {
		if (pageNum == 0) {
			return;
		}

		HtmlCleaner htmlCleaner = new HtmlCleaner();
		TagNode rootNode = htmlCleaner
				.clean(new URL(getOffersPageUrlTemplate()));

		Object[] els = rootNode
				.evaluateXPath("//div[@class='content-bar']/div[@class='ticket-photo']/a[1]");
		Set<String> pageOffers = new HashSet<String>();
		for (Object e : els) {
			if (e instanceof TagNode) {
				TagNode t = (TagNode) e;
				pageOffers.add(t.getAttributeByName("href"));
			}
		}

		for (String offerUrl : pageOffers) {

			Offer o = new Offer();
			Realtor r = new Realtor();

			StringBuffer workingUrl = new StringBuffer();
			workingUrl.append(SITE_ROOT_URL);
			workingUrl.append(offerUrl);

			rootNode = htmlCleaner.clean(new URL(workingUrl.toString()));

			o.url = workingUrl.toString();
			o.uuid = new StringBuffer()
					.append(ID_PREF)
					.append(offerUrl.replace("/ru/realty-", "").replace(
							".html", "")).toString();
			o.title = Utils.getHtmlText(rootNode, "//h1[@class='head-cars']");
			o.price = Utils.getHtmlText(rootNode,
					"//div[@class='price-seller']/span[1]");
			o.phones = Utils.getHtmlTexts(rootNode, "//strong[@class='phone']");
			o.description = Utils.getHtmlText(rootNode,
					"//div[@class='box-panel rocon description-view']/p[1]");
			o.city = Utils.getHtmlText(rootNode,
					"//dl[@class='info-user']/dd[@class='item']/strong[1]");

			if (o.city == null || "".equals(o.city)) {
				o.city = "Не указан город";
			}

			o.type = getType();

			r.name = Utils.getHtmlText(rootNode,
					"//dl[@class='info-user']/dt[@class='user-name']/a[1]");
			r.url = new StringBuffer()
					.append(SITE_ROOT_URL)
					.append(Utils
							.getHtmlAttr(rootNode,
									"//dl[@class='info-user']/dt[@class='user-name']/a[1]/@href"))
					.toString();
			r.uuid = new StringBuffer()
					.append(ID_PREF)
					.append(r.url.replace("/ru/realtor-", "").replace(".html",
							"")).toString();

			DataPiece dp = new DataPiece();
			dp.offer = o;
			dp.realtor = r;

			dataToSave.put(dp);
		}
	}
}
