/**
 * 
 */
package uk.ac.imperial.dws04.utils.convert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import biz.source_code.base64Coder.Base64Coder;

/**
 * Class to allow serialization to/from strings
 * 
 * See http://www.source-code.biz/base64coder/java/ for Base64Coder implementation
 * 
 * @author dws04
 *
 */
public class StringSerializer {

	/** Read the object from Base64 string. */
    public static Object fromString( String s ) throws IOException , ClassNotFoundException {
        byte [] data = Base64Coder.decode( s );
        ObjectInputStream ois = new ObjectInputStream( 
                                        new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
    }

    /** Write the object to a Base64 string. */
    public static String toString( Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return new String( Base64Coder.encode( baos.toByteArray() ) );
    }
}