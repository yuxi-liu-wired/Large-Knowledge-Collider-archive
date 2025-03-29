package eu.larkc.plugin.reason.OWLAPIReasoner;

import java.util.Set;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

public class OWLNodeImpl implements OWLNode {
	


	@Override
	public Boolean IsComplement() {
    Boolean result =false;
		
		String queryType = this.getNodeName();
		
		if (queryType.equalsIgnoreCase("ObjectComplementOf")) {result = true;};
		
		return result;
	}

	@Override
	public Boolean IsIntersection() {
		// TODO Auto-generated method stub
		Boolean result =false;
		
		String queryType = this.getNodeName();
		
		if (queryType.equalsIgnoreCase("ObjectIntersectionOf")) {result = true;};
		
		return result;
	}

	@Override
	public Boolean IsUnion() {
Boolean result =false;
		
		String queryType = this.getNodeName();
		
		if (queryType.equalsIgnoreCase("ObjectUnionOf")) {result = true;};
		
		return result;
	}

	public Boolean IsPrimitiveClass() {
		
		Boolean result = false;
		
	  String queryType = this.getNodeName();
		
		if (queryType.equalsIgnoreCase("owl:Class")) {result = true;};
		
		return result;
	}
	
	public OWLObject getOWLObject() {
		// TODO Auto-generated method stub
		

		OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
		
        OWLObject result =null;
		
		if (IsPrimitiveClass()){result= (OWLObject) this;}
		if (IsIntersection()){Set<OWLClass> b = (Set<OWLClass>) this.getChildNodes();
		OWLObjectIntersectionOf intersection = factory.getOWLObjectIntersectionOf(b);
		result = intersection;
		}
		if (IsUnion()){Set<OWLClass> b = (Set<OWLClass>) this.getChildNodes();
		OWLObjectUnionOf union = factory.getOWLObjectUnionOf(b);
	    result = union;
		};
		
		
		return result;
	}
	
	@Override
	public Node appendChild(Node newChild) throws DOMException {
		// TODO Auto-generated method stub
	    
		return this.appendChild(newChild);
	}

	@Override
	public Node cloneNode(boolean deep) {
		// TODO Auto-generated method stub
		return this.cloneNode(deep);
	}

	@Override
	public short compareDocumentPosition(Node other) throws DOMException {
		// TODO Auto-generated method stub
		return this.compareDocumentPosition(other);
	}

	
	public String getBaseURI() {
		// TODO Auto-generated method stub
		return this.getBaseURI();
	}

	@Override
	public NodeList getChildNodes() {
		// TODO Auto-generated method stub
		return this.getChildNodes();
	}

	@Override
	public Object getFeature(String feature, String version) {
		// TODO Auto-generated method stub
		return this.getFeature(feature, version);
	}

	@Override
	public Node getFirstChild() {
		// TODO Auto-generated method stub
		return this.getFirstChild();
	}

	@Override
	public Node getLastChild() {
		// TODO Auto-generated method stub
		return this.getLastChild();
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		return this.getLocalName();
	}

	@Override
	public String getNamespaceURI() {
		// TODO Auto-generated method stub
		return this.getNamespaceURI();
	}

	@Override
	public Node getNextSibling() {
		// TODO Auto-generated method stub
		return this.getNextSibling();
	}

	@Override
	public String getNodeName() {
		// TODO Auto-generated method stub
		return this.getNodeName();
	}

	@Override
	public short getNodeType() {
		// TODO Auto-generated method stub
		return this.getNodeType();
	}

	@Override
	public String getNodeValue() throws DOMException {
		// TODO Auto-generated method stub
		return this.getNodeValue();
	}

	@Override
	public Document getOwnerDocument() {
		// TODO Auto-generated method stub
		return this.getOwnerDocument();
	}

	@Override
	public Node getParentNode() {
		// TODO Auto-generated method stub
		return this.getParentNode();
	}

	@Override
	public String getPrefix() {
		// TODO Auto-generated method stub
		return this.getPrefix();
	}

	@Override
	public Node getPreviousSibling() {
		// TODO Auto-generated method stub
		return this.getPreviousSibling();
	}

	@Override
	public String getTextContent() throws DOMException {
		// TODO Auto-generated method stub
		return this.getTextContent();
	}

	@Override
	public Object getUserData(String key) {
		// TODO Auto-generated method stub
		return this.getUserData(key);
	}

	@Override
	public boolean hasAttributes() {
		// TODO Auto-generated method stub
		return this.hasAttributes();
	}

	@Override
	public boolean hasChildNodes() {
		// TODO Auto-generated method stub
		return this.hasChildNodes();
	}

	@Override
	public Node insertBefore(Node newChild, Node refChild) throws DOMException {
		// TODO Auto-generated method stub
		return this.insertBefore(newChild, refChild);
	}

	@Override
	public boolean isDefaultNamespace(String namespaceURI) {
		// TODO Auto-generated method stub
		return this.isDefaultNamespace(namespaceURI);
	}

	@Override
	public boolean isEqualNode(Node arg) {
		// TODO Auto-generated method stub
		return this.isEqualNode(arg);
	}

	@Override
	public boolean isSameNode(Node other) {
		// TODO Auto-generated method stub
		return this.isSameNode(other);
	}

	@Override
	public boolean isSupported(String feature, String version) {
		// TODO Auto-generated method stub
		return this.isSupported(feature, version);
	}

	@Override
	public String lookupNamespaceURI(String prefix) {
		// TODO Auto-generated method stub
		return this.lookupNamespaceURI(prefix);
	}

	@Override
	public String lookupPrefix(String namespaceURI) {
		// TODO Auto-generated method stub
		return this.lookupPrefix(namespaceURI);
	}

	@Override
	public void normalize() {
		// TODO Auto-generated method stub
		this.normalize();
	}

	@Override
	public Node removeChild(Node oldChild) throws DOMException {
		// TODO Auto-generated method stub
		return this.removeChild(oldChild);
	}

	@Override
	public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
		// TODO Auto-generated method stub
		return this.replaceChild(newChild, oldChild);
	}

	@Override
	public void setNodeValue(String nodeValue) throws DOMException {
		// TODO Auto-generated method stub
		this.setNodeValue(nodeValue);
	}

	@Override
	public void setPrefix(String prefix) throws DOMException {
		// TODO Auto-generated method stub
		this.setPrefix(prefix);
	}

	@Override
	public void setTextContent(String textContent) throws DOMException {
		// TODO Auto-generated method stub
		this.setTextContent(textContent);
	}

	@Override
	public Object setUserData(String key, Object data, UserDataHandler handler) {
		// TODO Auto-generated method stub
		return this.setUserData(key,data,handler);
	}

	@Override
	public NamedNodeMap getAttributes() {
		// TODO Auto-generated method stub
		return this.getAttributes();
	}

	

	

}
