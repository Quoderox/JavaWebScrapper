/**
g * This class build a robust text scraper that will connect to a page on 
 * www.shopping.com. The program will firstly take the input query from user
 * and return results about a given keyword. 
 * There are two queries that will be performed:
 *  
 *  Query 1: Total number of results
 *  Given a keyword, such as "digital camera", return the total number of 
 *  results found.
 *  
 *  Query 2: Result Object
 *  Given a keyword (e.g. "digital cameras") and page number (e.g. "1"), 
 *  return the results in a result object and then print results on screen. 
 *  For each result, return the following information:
 *      Title/Product Name (e.g. "Samsung TL100 Digital Camera")
 *      Price of the product
 *      Shipping Price (e.g. "Free Shipping", "$3.50")
 *      Vendor (e.g. "Amazon", "5 stores")
 *      
 * @author Yongxiang Tang
 * @date Oct 22, 2014
 */

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ResultTextScrapper {
	private static enum Query {QUERY1,QUERY2}; //Query Options
	private static Query command = Query.QUERY1; //Default command is Query1
	
    public static void main(String[] args) throws IOException {
    	
    	try {
    		/* Handle user request and create query*/
    		String query = createQuery(args);
    		
    		/* Process query using Jsoup and get results */
    		Document doc = Jsoup.connect(query).get();
    		if(command == Query.QUERY1) {
    			System.out.println("Total Number of Result Found: " 
    					+ getTotalResultNumber(doc));
    		}
    		else { //case for Query2 
    			System.out.println(getAllResultDetail(doc));
    		}
    	} catch (InvalidPageNumberException e) {
    		System.out.println(e.getMessage());
    	} catch (PageNotExistException e) {
    		System.out.println(e.getMessage());
		} catch (InvalidArgumentException e) {
			System.out.println(e.getMessage());
			System.out.println("Usage:");
			System.out.println("Query1: java -jar Assignment.jar <keyword> "
					+ "(e.g. java -jar Assignment.jar baby strollers)");
			System.out.println("Query2: java -jar Assignment.jar <keyword> "
					+ "<page number> (e.g. java -jar Assignment.jar baby "
					+ "strollers 2)");
		}
	}
    
    /**
     * Query1: Get total number of results
     * @param doc webpage content
     * @return result for Query1
     * @throws PageNotExistException 
     */
	private static String getTotalResultNumber(Document doc) 
			throws PageNotExistException
	{
    	/* Handle the case that page doesn't exists */
    	Element noMatch = doc.getElementsByClass("nomatch").first();
    	if(noMatch != null) {
    		throw new PageNotExistException("No Result Found on This Page"); 
    	}
		
		String totalNumText = doc.getElementsByClass("numTotalResults")
				.first().text();
		/*Get the text after "of ", which is the count of total result*/ 
		return totalNumText.split("of ")[1];
	}
    
    /**
     * Query2: create result object and print all result on screen
     * @param doc webpage content
     * @return result for Query2
     * @throws PageNotExistException 
     */
    private static String getAllResultDetail(Document doc) 
    		throws PageNotExistException
	{
    	/* Handle the case that page doesn't exists */
    	Element noMatch = doc.getElementsByClass("nomatch").first();
    	if(noMatch != null) {
    		throw new PageNotExistException("No Result Found on This Page"); 
    	}
    	
		/* Determine the number of results in one page */
		int size = doc.getElementsByClass("gridBox").size();
		
		/* Create Product Object and save into list */
		ArrayList<Product> productList = new ArrayList<Product>();
		for(int i = 1; i <= size; i++) {
			
			/* Get product title */
			String itemID = "nameQA" + i;
			Element item = doc.getElementById(itemID);
			String title = item.attr("title");
			
			/* Get product price */
			Elements itemPrices = doc.getElementsByClass("productPrice");
			String price = itemPrices.get(i-1).text();
			
			/* Get vendor name */
			Elements vendorNames = doc.getElementsByClass("newMerchantName");
			String vendor = vendorNames.get(i-1).text();
			
			/* Get shipping price information */
			String qLItemID = "quickLookItem-" + i;
			Element qLItem = doc.getElementById(qLItemID);
			String shipping = "";
			Element shipPrice = qLItem.getElementsByClass("freeShip").first();
			if(shipPrice == null) { 
				// shipping info of item is not in "freeShip" class
				shipPrice 
					= qLItem.getElementsByClass("taxShippingArea").first();
				if(shipPrice == null) { 
					//shipping info is not available on grid of search result
					shipping = "Multiple Options, click stores for detail";
				}
				else { 
					//shipping info of item is available in "taxShippingArea"
					shipping = shipPrice.text();
				}
			}
			else { 
				// item is free shipping
				shipping = shipPrice.text();
			}
			
			/* Create Product object */
			Product product = new Product(title, price, shipping, vendor); 
			productList.add(product);
		} // end of for loop
		
		return printAllProduct(productList);
	}
    
    /**
     * Create a string of all product info for printing
     * @param productList list of products
     * @return string of all product info 
     */
	private static String printAllProduct(ArrayList<Product> productList)
	{
		String results = "";
		for(Product product : productList) {
			results += "Product Title:  " + product.getTitle() + "\n";
			results += "Product Price:  " + product.getPrice() + "\n";
			results += "Shipping Price: " + product.getShippingPrice() + "\n";
			results += "Vendor Name:    " + product.getVendor() + "\n";
			results += "\n";
		}
		return results;
	}

	/**
     * Take input from user, handle request to determine
     * the query type and search key, finally generate query
     * @param args user input 
     * @return search query
     * @throws InvalidPageNumberException
	 * @throws InvalidArgumentException 
     */
	private static String createQuery(String[] args) 
			throws InvalidPageNumberException, InvalidArgumentException {
		
		/* Handle the case that no input argument */
		if(args.length == 0) {
			throw new InvalidArgumentException("No query is found"); 
		}
		
		/*Determine the last argument is an integer*/
		if(isInteger(args[args.length-1])) {
			command = Query.QUERY2;
		}
		
		/*Obtain search key */
		int keySize = args.length;//The default is keySize is arguments' length
		int pageNum = 0; 
		String searchKey = "";
		if(command == Query.QUERY2) {
			keySize = args.length-1; 
			pageNum = Integer.parseInt(args[args.length-1]);
			if(pageNum < 1) {
				throw new InvalidPageNumberException("Invalid Page Number");
			}
		}
		
		/*Generate search key */
		searchKey = args[0];
		for(int i = 1; i < keySize; i++) {
			searchKey += "%20" + args[i];
		}
		
		String host = "http://www.shopping.com";
		if(command == Query.QUERY1) {
			return host + "/products?CLT=SCH&KW=" + searchKey;
		}
		else {
			return host + "/products~PG-" + pageNum + "?KW=" + searchKey;
		}
	}

	/**
	 * Determine the argument is an integer
	 * @param arg argument
	 * @return true if argument is an integer, otherwise return false;
	 * @throws InvalidPageNumberException 
	 */
	private static boolean isInteger(String arg) 
			throws InvalidPageNumberException {
		for(int i = 0; i < arg.length(); i++) {
			
			/* if number is negative, throw invalid page number exception */
			if(i == 0 && arg.charAt(i) == '-') {
				throw new InvalidPageNumberException(
						"Page Number cannot be Negative");
			}
			
			/*Determine each character is digit */ 
			if(!Character.isDigit(arg.charAt(i))) {
				return false;
			}
		}
		return true;
	}
}

/**
 * A class to handle invalid page number exception
 * @author Yongxiang Tang
 */
class InvalidPageNumberException extends Exception {

	private String message = null; 
	
	/**
	 * Constructor for creating InvalidPageNumberException
	 * @param message exception message
	 */
	public InvalidPageNumberException(String message) {
		super();
		this.message = message;
	}
	
	/**
	 * Get exception message 
	 * @return message text
	 */
	public String getMessage() {
		return message.toString();
	}
}

/**
 * A class to handle page not exist exception
 * @author Yongxiang Tang
 */
class PageNotExistException extends Exception {

	private String message = null; 
	
	/**
	 * Constructor for creating PageNotExistException
	 * @param message exception message
	 */
	public PageNotExistException(String message) {
		super();
		this.message = message;
	}
	
	/**
	 * Get exception message 
	 * @return message text
	 */
	public String getMessage() {
		return message.toString();
	}
}

/**
 * An exception class to handle invalid arguments; 
 * @author Yongxiang Tang
 */
class InvalidArgumentException extends Exception {

	private String message = null; 
	
	/**
	 * Constructor for creating InvalidArgumentException
	 * @param message exception message
	 */
	public InvalidArgumentException(String message) {
		super();
		this.message = message;
	}
	
	/**
	 * Get exception message 
	 * @return message text
	 */
	public String getMessage() {
		return message.toString();
	}
}

