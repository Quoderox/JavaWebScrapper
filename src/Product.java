/**
 * A Class for Product object including its name, price, shipping price 
 * and vendor name
 * @author Yongxiang Tang
 * @date Oct 22, 2014
 */
public class Product {
	
	private String title;     // name of product
	private String price;     // price of product
	private String shipPrice; // shipping price
	private String vendor;    // vendor name
	
	/**
	 * Create a Product object
	 * @param title name of product
	 * @param price price of product
	 * @param ship shipping price of product
	 * @param vendor vendor name of product
	 */
	public Product(String title, String price, String ship, String vendor) {
		this.title = title;
		this.price = price;
		this.shipPrice = ship;
		this.vendor = vendor;
	}
	
	/**
	 * Get the name of product
	 * @return product name
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Get the price of product
	 * @return price of product
	 */
	public String getPrice() {
		return price;
	}
	
	/**
	 * Get the shipping price for product
	 * @return shipping price
	 */
	public String getShippingPrice() {
		return shipPrice;
	}
	
	/**
	 * Get the vendor name of product
	 * @return vendor name 
	 */
	public String getVendor() {
		return vendor; 
	}
}
