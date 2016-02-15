package com.cga;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

class responseIpAndBody{
  static class Returner{
    String ip;
    String body;
  }

  public responseIpAndBody(){    
  }

  public static Returner setVals(String ip, String body){
    Returner myReturner = new Returner();
    myReturner.ip = ip;
    myReturner.body = body;
    return myReturner;
  }
}

@WebServlet(name = "MainServlet", urlPatterns = {""})
public class MainServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {              
        String nodeUrl = System.getenv("NODE_URL");
        if (nodeUrl == null) {  
           nodeUrl = "http://www.google.co.uk";  
        }
      
        responseIpAndBody.Returner nodeApp = callURL(nodeUrl);
      
        ServletOutputStream out = resp.getOutputStream();
        out.write(nodeApp.body.getBytes());
        out.flush();
        out.close();
        
        String ipAddress = req.getHeader("X-FORWARDED-FOR");  
        if (ipAddress == null) {  
           ipAddress = req.getRemoteAddr();  
        }
        
        resp.setContentType("application/json");
        resp.setHeader("X-WAR-App-Request-Ip", ipAddress);
        resp.setHeader("X-Node-App-Request-Ip", nodeApp.ip);
    }
    
  	public static responseIpAndBody.Returner callURL(String myURL) {
  		System.out.println("Requested URL:" + myURL);
  		StringBuilder sb = new StringBuilder();
  		URLConnection urlConn = null;
  		InputStreamReader in = null;
      String nodeRequestIpAddress = null;
  		try {
  			URL url = new URL(myURL);
  			urlConn = url.openConnection();
  			if (urlConn != null)
  				urlConn.setReadTimeout(60 * 1000);
  			if (urlConn != null && urlConn.getInputStream() != null) {
          nodeRequestIpAddress = urlConn.getHeaderField("X-Node-App-Request-Ip");
          
  				in = new InputStreamReader(urlConn.getInputStream(),
  						Charset.defaultCharset());
  				BufferedReader bufferedReader = new BufferedReader(in);
  				if (bufferedReader != null) {
  					int cp;
  					while ((cp = bufferedReader.read()) != -1) {
  						sb.append((char) cp);
  					}
  					bufferedReader.close();
  				}
  			}
  		in.close();
  		} catch (Exception e) {
  			throw new RuntimeException("Exception while calling URL:"+ myURL, e);
  		} 
 
      responseIpAndBody.Returner ret = responseIpAndBody.setVals(nodeRequestIpAddress, sb.toString());
 
  		return ret;
  	}

}
