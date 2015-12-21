package aesthetiX;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
//import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//import SimpleErrorHandler;

//import com.sun.jersey.api.container.MappableContainerException;

//import editeur.MethodesAvecCoords;

@Path("/upload")
public class FileUpload {
	private static File usiFile;
	private static String resultStatus = "<html>" + "<title>" + "Hello Jersey"
	+ "</title>" + "<body><h1>" + "Failed!" + "</body></h1>"
	+ "</html>";
	@POST
	@Produces(MediaType.TEXT_HTML)
	public String loadFileH(@Context HttpServletRequest request) {
		// this method uploads the file
		
		String fileRepository = "C:\\Users\\PBenz\\testRepo\\";

		if (ServletFileUpload.isMultipartContent(request)) {

			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			List<FileItem> items = null;
			try {
				items = upload.parseRequest(request);

			} catch (FileUploadException e) {

				e.printStackTrace();

			} catch (Exception e) {

				e.printStackTrace();

			}

			if (items != null) {

				Iterator<FileItem> iter = items.iterator();
				System.out.println(items.toString());
				while (iter.hasNext()) {

					FileItem item = iter.next();
					if (!item.isFormField() && item.getSize() > 0) {
						String fileName = processFileName(item.getName());
						try {
							usiFile = new File(fileRepository + fileName);

							item.write(usiFile);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if(checkWellformedNess())
						resultStatus = evaluate();
						
					}
				}
			}

		}
		return resultStatus;
	}
	
	private String processFileName(String fileNameInput){
		String fileNameOutput=null;
		fileNameOutput = fileNameInput.substring(fileNameInput.lastIndexOf("\\")+1,fileNameInput.length());
		return fileNameOutput;
	}
	public String evaluate(){
		String Result="<html> " + "<title>" + "Hello Jersey" + "</title>"
		+ "<body><h1>" + "Results" + "</h1>"+"<table>";
		
        try {
        	//parse the file 
      		  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      		  DocumentBuilder db = dbf.newDocumentBuilder();
      		  Document doc = db.parse(usiFile);
      		  doc.getDocumentElement().normalize();
      		  /**System.out.println("Root element " + doc.getDocumentElement().getNodeName());*/
      		  NodeList nodeLst = doc.getElementsByTagName("window");
      		 //evaluate the metrics on the file
      		  for (int s = 0; s < nodeLst.getLength(); s++) {
      			  Node wndNode = nodeLst.item(s);
      			  if (wndNode.getNodeType() == Node.ELEMENT_NODE) 
      			  {  		  
      		         Element wndElmnt = (Element)wndNode;
      		         float d = Metrics.density(wndElmnt);
      		         Result +="<tr><td>Density:</td><td>"+d+"</td></tr>"; 
      		         float bal = Metrics.balance(wndElmnt);
      		         Result +="<tr><td>Balance:</td><td>"+bal+"</td></tr>"; 
      		         float u = Metrics.unity(wndElmnt);
      		         Result +="<tr><td>Unity:</td><td>"+u+"</td></tr>"; 
      		         float  sym = Metrics.symmetry(wndElmnt);
      		         Result +="<tr><td>Symmetry:</td><td>"+sym+"</td></tr>"; 
      		         float al = Metrics.alignment(wndElmnt);
      		         Result +="<tr><td>Alignment:</td><td>"+al+"</td></tr>"; 
      		         float gr = Metrics.grouping(wndElmnt);
      		         Result +="<tr><td>Grouping:</td><td>"+gr+"</td></tr>"; 
      		         double rep = Metrics.repartition(wndElmnt);
      		         Result +="<tr><td>Repartition:</td><td>"+rep+"</td></tr>"; 
      		         float sim = Metrics.simplicity(wndElmnt);
      		         Result +="<tr><td>Simplicity:</td><td>"+sim+"</td></tr>"; 
      		         float eco = Metrics.economy(wndElmnt);
      		         Result +="<tr><td>Economy:</td><td>"+eco+"</td></tr>"; 
      		         float p = Metrics.proportion(wndElmnt);
      		         Result +="<tr><td>Proportion:</td><td>"+p+"</td></tr>"; 
      		         double score = d*0.15 + bal*0.35 + u*0.10 + sym*0.10 + al*0.15 + gr*0.03 + rep*0.03 + sim*0.03 + eco*0.03 + p*0.03;
      		         Result +="<tr><td>Score(weighted sum):</td><td>"+score+"</td></tr>"; 
      			  }
      		  	}
      		Result +="</table>"+"<br>"+"Done! (if you cannot see any results please check that the file you uploaded is a valid UsiXML file)"+"</body></html>"; 
      		 } 
        	catch (Exception e){
      		    e.printStackTrace();
      	  }
        	
        	return Result;
	}
	public static boolean checkWellformedNess() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setNamespaceAware(true);

			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new SimpleErrorHandler());

			//builder.parse(new InputSource(usiFile.getPath()));
			builder.parse(usiFile);
		} catch (ParserConfigurationException e) {
			resultStatus = "<html>" + "<title>" + "Hello Jersey"
			+ "</title>" + "<body>" + "This document is not well formed!" +"<br>"+e.toString()+ "</body>"
			+ "</html>";
			return false;
		} catch (SAXException e) {
			resultStatus = "<html>" + "<title>" + "Hello Jersey"
			+ "</title>" + "<body>" + "This document is not well formed!" +"<br>"+e.toString()+ "</body>"
			+ "</html>";//System.out.println("This document is not well formed!");
			return false;
			//e.printStackTrace();
		} catch (IOException e) {
			resultStatus = "<html>" + "<title>" + "Hello Jersey"
			+ "</title>" + "<body>" + "This document is not well formed!" +"<br>"+e.toString()+ "</body>"
			+ "</html>";
			return false;
		}
		return true;
	}
}