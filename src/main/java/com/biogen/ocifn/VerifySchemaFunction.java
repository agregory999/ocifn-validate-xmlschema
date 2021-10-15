package com.biogen.ocifn;

import org.xml.sax.InputSource;
import java.util.Base64;
import java.io.StringReader;
import oracle.xml.parser.schema.*;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLParser;

public class VerifySchemaFunction {

    // Config Param - if exists and "true"
    boolean debug = ((System.getenv("DEBUG") != null && System.getenv("DEBUG").equals("true")) ? true : false);

    public Response handleRequest(Input input) {

        // Set up Response
        Response resp = new Response(false);
        StringBuffer messages = new StringBuffer();

        // Local variables
        String inputXML = "";
        String inputXSD = "";
        XMLSchema schemadoc;

        // Get InputXSD
        if (input.getInputXSD() != null) {
            // Using URLs - works
            try {
                XSDBuilder builder = new XSDBuilder();
                schemadoc = (XMLSchema) builder.build(new java.net.URL(input.getInputXSD()));
                
            } catch (Exception e) {
                resp.setDetail("Error parsing XSD from URL: " + e.getMessage());
                return resp;
            }
        } else if (input.getBase64InputXSD() != null) {

            // process Base64 input
            inputXSD = new String(Base64.getDecoder().decode(input.getBase64InputXSD()));
            if (debug) messages.append("XSD from Base64:" + inputXSD);

            // Use as String - XSD
            try {
                XSDBuilder builder = new XSDBuilder();
                InputSource inputSource = new InputSource( new StringReader(inputXSD) );
                schemadoc = (XMLSchema) builder.build(inputSource);
            } catch (Exception e) {
                resp.setDetail("Error parsing XSD from text: " + e.getMessage());
                return resp;
            }
        }
        else {
            resp.setDetail("Required XSD (URLs or Strings) not supplied");
            return resp;
        }
        // Check Schema doc
        if (schemadoc == null) {
            resp.setDetail("XSD Schema not correct");
            return resp;
        } else {
            if (debug) messages.append("Schema Doc: " + schemadoc.toString());
        }

        // Parse XML as either String or URL
        try {
            
            DOMParser dp = new DOMParser();
            dp.setXMLSchema(schemadoc);
            dp.setValidationMode(XMLParser.SCHEMA_VALIDATION);
            dp.setErrorStream(System.out);
            if (input.getInputXML() != null ) {
                // Parse using URL
                if (debug) messages.append("XML URL:" + input.getInputXML());
                dp.parse(new java.net.URL(input.getInputXML()));
            } else if (input.getBase64InputXML() != null) {
                // Parse using String
                inputXML = new String(Base64.getDecoder().decode(input.getBase64InputXML()));
                if (debug) messages.append("XML from Base64:" + inputXML);
                InputSource inputSource = new InputSource( new StringReader(inputXML));
                dp.parse(inputSource);
            }
        } catch (Exception e) {
            resp.setDetail("Error parsing XML: " + e.getMessage());
            return resp;
        }

        // If we got this far, return ok
        resp.setVerified(true);
        resp.setDetail("Parsed ok:" + messages);
        return resp;

     }

    // Input class
    public static class Input {
        private String inputXML;
        private String inputXSD;
        private String base64InputXSD;
        private String base64InputXML;
        
        public String getInputXML() {
            return inputXML;
        }
        public void setInputXML(String inputXML) {
            this.inputXML = inputXML;
        }
        public String getInputXSD() {
            return inputXSD;
        }
        public void setInputXSD(String inputXSD) {
            this.inputXSD = inputXSD;
        }

        public String getBase64InputXML() {
            return base64InputXML;
        }
        public void setBase64InputXML(String base64InputXML) {
            this.base64InputXML = base64InputXML;
        }
        public String getBase64InputXSD() {
            return base64InputXSD;
        }
        public void setBase64InputXSD(String base64InputXSD) {
            this.base64InputXSD = base64InputXSD;
        }
    }

    // Response Class
    public static class Response {
        private boolean verified = false;
        private String detail;
        public boolean getVerified() {
            return verified;
        }
        public Response(boolean ver) {
            this.verified = ver;
        }

        public String getDetail() {
            return detail;
        }
        public void setDetail(String detail) {
            this.detail = detail;
        }
        public void setVerified(boolean verified) {
            this.verified = verified;
        }
    }

}