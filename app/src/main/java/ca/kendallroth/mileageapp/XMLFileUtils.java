package ca.kendallroth.mileageapp;

import android.content.Context;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * File utilities for working with DOM4J XML documents
 */
public abstract class XMLFileUtils {

  // File name for authentication (users) file
  public static String USERS_FILE_NAME = "users.xml";

  /**
   * Get an XML file and prepare it for parsing with DOM4J
   * @param context   Android context
   * @param fileName  File name (path)
   * @return XML document for DOM4J
   * @throws FileNotFoundException
   * @throws DocumentException
   */
  public static Document getFile(Context context, String fileName) throws FileNotFoundException, DocumentException {
    // Retrieve the specified file and convert it to an XML document for parsing
    FileInputStream fis = context.openFileInput(fileName);
    SAXReader reader = new SAXReader();
    Document document = reader.read(fis);

    return document;
  }

  /**
   * Create a file from a DOM4J XML document
   * @param context   Android context
   * @param fileName  File name (path)
   * @param document  DOM4J XML document
   * @throws IOException
   */
  public static void createFile(Context context, String fileName, Document document) throws IOException {
    // Create a file output steam at the specified Android filepath
    FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);

    // Prettify the XML file
    OutputFormat format = OutputFormat.createPrettyPrint();

    // Write the document into the file output stream
    XMLWriter writer = new XMLWriter(fos, format);
    writer.write(document);
    writer.close();

    return;
  }
}
