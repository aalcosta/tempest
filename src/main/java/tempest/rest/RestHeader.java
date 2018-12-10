package tempest.rest;

/**
 * Agnostic representation of HttpHeader.
 * 
 * Created by alexandre on 1/27/17.
 */
public class RestHeader {

    public String name;
    public String value;

    public RestHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
