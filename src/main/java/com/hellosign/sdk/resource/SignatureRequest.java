package com.hellosign.sdk.resource;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.hellosign.sdk.HelloSignException;
import com.hellosign.sdk.resource.support.Document;
import com.hellosign.sdk.resource.support.FormField;
import com.hellosign.sdk.resource.support.ResponseData;
import com.hellosign.sdk.resource.support.Signature;
import com.hellosign.sdk.resource.support.Signer;

/**
 * Represents a HelloSign signature request. This object is used to both 
 * submit a request and to represent the request object returned from the server.
 * 
 * @author "Chris Paul (chris@hellosign.com)"
 */
public class SignatureRequest extends AbstractRequest {
	
	public static final String SIGREQ_KEY = "signature_request";
	public static final String SIGREQ_ID = "signature_request_id";
	public static final String SIGREQ_SIGNERS = "signers";
	public static final String SIGREQ_SIGNER_EMAIL = "email_address";
	public static final String SIGREQ_SIGNER_NAME = "name";
	public static final String SIGREQ_SIGNER_ORDER = "order";
	public static final String SIGREQ_CCS = "cc_email_addresses";
	public static final String SIGREQ_FILES = "file";
	public static final String SIGREQ_FORM_FIELDS = "form_fields_per_document";
	public static final String SIGREQ_IS_COMPLETE = "is_complete";
	public static final String SIGREQ_HAS_ERROR = "has_error";
	public static final String SIGREQ_RESPONSE_DATA = "response_data";
	public static final String SIGREQ_FINAL_COPY_URL = "final_copy_url";
	public static final String SIGREQ_SIGNING_URL = "signing_url";
	public static final String SIGREQ_DETAILS_URL = "details_url";
	public static final String SIGREQ_REQUESTER_EMAIL = "requester_email_address";
	
	// Fields specific to request
	private List<Signer> signers = new ArrayList<Signer>();
	private List<Document> documents = new ArrayList<Document>();
	private boolean orderMatters = false;
	
	public SignatureRequest() {
		super();
	}
	
	public SignatureRequest(JSONObject json) throws HelloSignException {
		super(json, SIGREQ_KEY);
	}
	
	public SignatureRequest(JSONObject json, String key) throws HelloSignException {
		super(json, key);
	}
	
	/**
	 * Returns the ID for this request.
	 */
	public String getId() {
		return getString(SIGREQ_ID);
	}
	
	/**
	 * Returns true if this request has an ID. Useful if checking to see if 
	 * this request is for submission or is the result of a call to HelloSign.
	 * @return true if the request has an ID, false otherwise
	 */
	public boolean hasId() {
		return has(SIGREQ_ID);
	}
	
	/**
	 * Returns the CC email addresses for this request.
	 * @return List<String>
	 */
	public List<String> getCCs() {
		return getList(String.class, SIGREQ_CCS);
	}
	
	/**
	 * Adds a CC'd email address to this request.
	 * @param email String email address
	 */
	public void addCC(String email) {
		add(SIGREQ_CCS, email);
	}
	
	/**
	 * Returns a list of signatures for this request.
	 * @return List<Signature>
	 */
	public List<Signature> getSignatures() {
		return getList(Signature.class, "signatures");
	}
	
	/**
	 * Returns the signature for the given email/name combination, or 
	 * null if not found on this request.
	 * @param email String email address
	 * @param name String name
	 * @return Signature or null if not found
	 * @throws HelloSignException if the email or name are empty
	 */
	public Signature getSignature(String email, String name) 
			throws HelloSignException {
		if (email == null || "".equals(email)) {
			throw new HelloSignException("Email address cannot be empty");
		}
		if (name == null || "".equals(name)) {
			throw new HelloSignException("Name cannot be empty");
		}
		for (Signature s : getSignatures()) {
			if (email.equalsIgnoreCase(s.getEmail()) && name.equalsIgnoreCase(s.getName())) {
				return s;
			}
		}
		return null;
	}
	
	/**
	 * Adds the signer to the list of signers for this request.
	 * @param email String
	 * @param name String
	 * @throws HelloSignException 
	 */
	public void addSigner(String email, String name) throws HelloSignException {
		signers.add(new Signer(email, name));
	}
	
	/**
	 * Adds the signer with the given order to the list of signers for 
	 * this request. NOTE: The order refers to the 1-base index, not 0-base.
	 * This is to reflect the indexing used by the HelloSign API. 
	 * This means that adding an item at order 1 will place it in the 0th
	 * index of the list (it will be the first item).
	 * 
	 * @param email String
	 * @param name String
	 * @param order int
	 * @throws HelloSignException 
	 */
	public void addSigner(String email, String name, int order) 
			throws HelloSignException {
		try {
			signers.add((order - 1), new Signer(email, name));
		} catch (Exception ex) {
			throw new HelloSignException(ex);
		}
	}
	
	/**
	 * Returns a reference to the signers list. This can be modified and 
	 * re-added to the request using setSigners(). Useful for more explicit 
	 * modification.
	 * @return List<Signer>
	 */
	public List<Signer> getSigners() {
		return signers;
	}
	
	/**
	 * Overwrites the current list of signers for this request with the
	 * given list.
	 * @param signers List<Signer>
	 */
	public void setSigners(List<Signer> signers) {
		this.signers = signers;
	}
	
	/**
	 * Removes the signer from the list. If that user does not exist,
	 * this will throw a HelloSignException.
	 * @param email String
	 * @throws HelloSignException
	 */
	public void removeSigner(String email) throws HelloSignException {
		if (email == null) {
			throw new HelloSignException("Cannot remove null signer");
		}
		for (int i = 0; i < signers.size(); i++) {
			if (email.equalsIgnoreCase(signers.get(i).getEmail())) {
				signers.remove(i);
			}
		}
	}
	
	/**
	 * Adds the file to the request. 
	 * @param file File
	 * @throws HelloSignException
	 */
	public void addFile(File file) throws HelloSignException {
		addFile(file, null);
	}
	
	/**
	 * Adds the file to the request in the given order. 
	 * 
	 * The order should be a 0-based index into the file list. 
	 * Therefore, the first item of the file list is 0, and so forth.
	 * 
	 * If order is null, the document is appended to the end of the file list.
	 * 
	 * @param file File
	 * @param order Integer or null
	 * @throws HelloSignException
	 */
	public void addFile(File file, Integer order) throws HelloSignException {
		Document doc = new Document();
		doc.setFile(file);
    	if (order == null) {
    		addDocument(doc);
    	} else {
    		addDocument(doc, order);
    	}
	}
	
	/**
	 * Adds a Document to the signature request.
	 * @param doc
	 * @throws HelloSignException
	 */
	public void addDocument(Document doc) throws HelloSignException {
		if (doc == null) {
			throw new HelloSignException("Document cannot be null");
		}
		documents.add(doc);
	}
	
	/**
	 * Adds a Document to the signature request at the specific order. 
	 * @param doc
	 * @param order
	 * @throws HelloSignException
	 */
	public void addDocument(Document doc, int order) throws HelloSignException {
		if (doc == null) {
			throw new HelloSignException("Document cannot be null");
		}
		try {
			documents.add(order, doc);
		} catch (Exception ex) {
			throw new HelloSignException(ex);
		}
	}
	
	/**
	 * Returns a reference to the list of documents for this request. 
	 * Modifying this list will modify the list that will be sent with the
	 * request. Useful for more fine-grained modification.
	 * @return List<Document>
	 */
	public List<Document> getDocuments() {
		return documents;
	}
	
	/**
	 * Overwrites this requests document list with the provided document list.
	 * @param docs List<Document>
	 */
	public void setDocuments(List<Document> docs) {
		documents = docs;
	}
	
	/**
	 * Remove all documents from this request.
	 */
	public void clearDocuments() {
		documents = new ArrayList<Document>();
	}

	/**
	 * Determines whether the order of the signers list is to be enforced.
	 * @param b true if the order matters, false otherwise
	 */
	public void setOrderMatters(boolean b) {
		orderMatters = b;
	}
	
	/**
	 * A flag that determines whether order of the signers list is enforced.
	 * @return true if the order matters, false otherwise
	 */
	public boolean getOrderMatters() {
		return orderMatters;
	}
	
	/**
	 * Utility method that allows you to search for a Signature object 
	 * on this request by email and name. It requires both because neither
	 * alone is enough to guarantee uniqueness (some requests can have 
	 * multiple signers using the same email address or name).
	 * @param email String
	 * @param name String
	 * @return Signature, if found on this request, or null
	 */
	public Signature getSignatureBySigner(String email, String name) {
		if (email == null || name == null) {
			return null;
		}
	    for (Signature s : getSignatures()) {
	        if (name.equalsIgnoreCase(s.getName()) && 
	        	email.equalsIgnoreCase(s.getEmail())) {
	            return s;
	        }
	    }
	    return null;
	}
	
	/**
	 * Internal method used to retrieve the necessary POST fields to submit the
	 * signature request. 
	 * @return Map<String, Serializable>
	 * @throws HelloSignException
	 */
	public Map<String, Serializable> getPostFields() throws HelloSignException {
		Map<String, Serializable> fields = new HashMap<String, Serializable>();
		try {
			if (hasTitle()) {
				fields.put(REQUEST_TITLE, getTitle());
			}
			if (hasSubject()) {
				fields.put(REQUEST_SUBJECT, getSubject());
			}
			if (hasMessage()) {
				fields.put(REQUEST_MESSAGE, getMessage());
			}
			List<Signer> signerz = getSigners();
			for (int i = 0; i < signerz.size(); i++) {
				Signer s = signerz.get(i);
				
				// The signers are being ID'd starting at 1, instead of zero. 
				// This is because the API generates signer IDs for templates starting at 1.
				// Let's keep this consistent with the API for now.
				
				fields.put(SIGREQ_SIGNERS + 
						"[" + (i + 1) + "][" + SIGREQ_SIGNER_EMAIL + "]", s.getEmail());
				fields.put(SIGREQ_SIGNERS + 
						"[" + (i + 1) + "][" + SIGREQ_SIGNER_NAME + "]", s.getNameOrRole());
				if (getOrderMatters()) {
					fields.put(SIGREQ_SIGNERS + 
							"[" + (i + 1) + "][" + SIGREQ_SIGNER_ORDER + "]", i);
				}
			}
			List<String> ccz = getCCs();
			for (int i = 0; i < ccz.size(); i++) {
				String cc = ccz.get(i);
				fields.put(SIGREQ_CCS + "[" + (i + 1) + "]", cc);
			}
			JSONArray reqFormFields = new JSONArray(); // Main array for the request
			boolean hasFormFields = false;
			List<Document> docs = getDocuments();
			for (int i = 0; i < docs.size(); i++) {
				Document d = docs.get(i);
				fields.put(SIGREQ_FILES + "[" + (i + 1) + "]", d.getFile());
				JSONArray docFormFields = new JSONArray();
				for (FormField ff : d.getFormFields()) {
					hasFormFields = true;
					docFormFields.put(ff.getJSONObject());
				}
				reqFormFields.put(docFormFields);
			}
			if (hasFormFields) {
				fields.put(SIGREQ_FORM_FIELDS, reqFormFields.toString());
			}
			if (isTestMode()) {
				fields.put(REQUEST_TEST_MODE, true);	
			}
			if (hasRequesterEmail()) {
				fields.put(SIGREQ_REQUESTER_EMAIL, getRequesterEmail());
			}
		} catch (Exception ex) {
			throw new HelloSignException(
					"Could not extract form fields from SignatureRequest.", ex);
		}
		return fields;
	}

	/**
	 * Returns the HelloSign-designated signature status, indicating
	 * whether all signers have signed the document.  
	 * @return true, if all signers have signed the document, false
	 * otherwise.
	 */
	public boolean isComplete() {
		return getBoolean(SIGREQ_IS_COMPLETE);
	}
	public boolean hasError() {
		return getBoolean(SIGREQ_HAS_ERROR);
	}
	public List<ResponseData> getResponseData() {
		return getList(ResponseData.class, SIGREQ_RESPONSE_DATA);
	}
	public String getFinalCopyUrl() {
		return getString(SIGREQ_FINAL_COPY_URL);
	}
	public String getSigningUrl() {
		return getString(SIGREQ_SIGNING_URL);
	}
	public String getDetailsUrl() {
		return getString(SIGREQ_DETAILS_URL);
	}
	public String getRequesterEmail() {
		return getString(SIGREQ_REQUESTER_EMAIL);
	}
	public boolean hasRequesterEmail() {
		return has(SIGREQ_REQUESTER_EMAIL);
	}
	public void setRequesterEmail(String email) {
		set(SIGREQ_REQUESTER_EMAIL, email);
	}
}